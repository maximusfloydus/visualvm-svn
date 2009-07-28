/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.visualvm.threaddump.impl;

import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.threaddump.ThreadDump;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.core.ui.components.ScrollableContainer;
import com.sun.tools.visualvm.threaddump.ThreadDumpSupport;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
class ThreadDumpView extends DataSourceView {
    private static final Logger LOGGER = Logger.getLogger(ThreadDumpView.class.getName());
    
    private DataViewComponent view;

    public ThreadDumpView(ThreadDump threadDump) {
        this(threadDump, DataSourceDescriptorFactory.getDescriptor(threadDump));
        
    }
    
    private ThreadDumpView(ThreadDump threadDump, DataSourceDescriptor descriptor) {
        super(descriptor.getName(), descriptor.getIcon(), 0);
        view = createViewComponent(threadDump);
        ThreadDumpPluggableView pluggableView = (ThreadDumpPluggableView)ThreadDumpSupport.getInstance().getThreadDumpView();
        pluggableView.makeCustomizations(view, threadDump);
    }
        
    public DataViewComponent getView() {
        return view;
    }
    
    public boolean isClosable() {
        return true;
    }
    
    
    private DataViewComponent createViewComponent(ThreadDump threadDump) {
        DataViewComponent dvc = new DataViewComponent(
                new MasterViewSupport(threadDump).getMasterView(),
                new DataViewComponent.MasterViewConfiguration(true));
        
        return dvc;
    }
    
    
    // --- General data --------------------------------------------------------
    
    private static class MasterViewSupport extends JPanel  {
        
        private JLabel progressLabel;
        private JPanel contentsPanel;
        
        public MasterViewSupport(ThreadDump threadDump) {
            initComponents();
            loadThreadDump(threadDump.getFile());
        }
        
        
        public DataViewComponent.MasterView getMasterView() {
            return new DataViewComponent.MasterView("Thread dump", null, new ScrollableContainer(this));
        }
        
        
        private void initComponents() {
            setLayout(new BorderLayout());
            
            progressLabel = new JLabel("Loading Thread Dump...", SwingConstants.CENTER);
        
            contentsPanel = new JPanel(new BorderLayout());
            contentsPanel.add(progressLabel, BorderLayout.CENTER);
            contentsPanel.setBackground(Color.WHITE);
            
            add(contentsPanel, BorderLayout.CENTER);
        }

        private static String htmlize(String value) {
            return value.replace("&", "&amp;").replace("<", "&lt;");
        }

        private static String transform(String value) {
            StringBuilder sb = new StringBuilder();
            String[] result = value.split("\\n");
            for (int i = 0; i < result.length; i++) {
                String line = result[i];
                if (line.isEmpty()) {
                    sb.append("<span>" + line + "\n</span>");
                } else if (line.substring(0, 1).matches("\\s")) {
                    sb.append("<span style=\"color: #CC3300\">" + line + "\n</span>");
                } else {
                    sb.append("<span style=\"color: #0033CC\">" + line + "\n</span>");
                }
            }
            return sb.toString();
        }

        private void loadThreadDump(final File file) {
            RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
              InputStream is = null;
              try {
                is = new FileInputStream(file);
                byte[] data = new byte[(int)file.length()];
                try {
                  is.read(data);
                } catch (IOException ex) {
                  LOGGER.throwing(ThreadDumpView.class.getName(), "loadThreadDump", ex);
                }
                try {
                  HTMLTextArea area = new HTMLTextArea("<nobr><pre>" +
                          transform(htmlize(new String(data, "UTF-8"))) +
                          "</pre></nobr>");
                  area.setCaretPosition(0);
                  area.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
                  contentsPanel.remove(progressLabel);
                  contentsPanel.add(area, BorderLayout.CENTER);
                  contentsPanel.revalidate();
                  contentsPanel.repaint();
//                  contentsPanel.doLayout();
                } catch (Exception ex) {
                  LOGGER.throwing(ThreadDumpView.class.getName(), "loadThreadDump", ex);
                }
              } catch (FileNotFoundException ex) {
                LOGGER.throwing(ThreadDumpView.class.getName(), "loadThreadDump", ex);
              } finally {
                  if (is != null) {
                      try {
                          is.close();
                      } catch (IOException e) {
                          // ignore
                      }
                  }
              }
           }
          });
        }
        
    }
    
}