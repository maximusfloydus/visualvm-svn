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

package com.sun.tools.visualvm.host.views.overview;

import com.sun.tools.visualvm.host.Host;
import com.sun.tools.visualvm.core.datasupport.DataRemovedListener;
import com.sun.tools.visualvm.host.model.HostOverview;
import com.sun.tools.visualvm.host.model.HostOverviewFactory;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.core.ui.components.NotSupportedDisplayer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.lib.profiler.ui.components.HTMLLabel;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Jiri Sedlacek
 */
class HostOverviewView extends DataSourceView implements DataRemovedListener<Host> {
    
    private static final String IMAGE_PATH = "com/sun/tools/visualvm/host/views/resources/overview.png";    // NOI18N

    private Timer timer;
    private HostOverview hostOverview;
    

    public HostOverviewView(Host host) {
        super(host, NbBundle.getMessage(HostOverviewView.class, "LBL_Overview"), new ImageIcon(Utilities.loadImage(IMAGE_PATH, true)).getImage(), 0, false);    // NOI18N
    }
    
    protected void willBeAdded() {
        hostOverview = HostOverviewFactory.getSystemOverviewFor((Host)getDataSource());
    }
        
    protected void removed() {
        timer.stop();
    }
    
    public void dataRemoved(Host dataSource) {
        timer.stop();
    }
    
    
    protected DataViewComponent createComponent() {
        DataViewComponent dvc = new DataViewComponent(
                new MasterViewSupport((Host)getDataSource()).getMasterView(),
                new DataViewComponent.MasterViewConfiguration(false));
        
        final CpuLoadViewSupport cpuLoadViewSupport = new CpuLoadViewSupport(hostOverview);
        dvc.configureDetailsArea(new DataViewComponent.DetailsAreaConfiguration(NbBundle.getMessage(HostOverviewView.class, "LBL_CPU"), true), DataViewComponent.TOP_LEFT); // NOI18N
        dvc.addDetailsView(cpuLoadViewSupport.getDetailsView(), DataViewComponent.TOP_LEFT);
        
        final PhysicalMemoryViewSupport physicalMemoryViewSupport = new PhysicalMemoryViewSupport();
        dvc.configureDetailsArea(new DataViewComponent.DetailsAreaConfiguration(NbBundle.getMessage(HostOverviewView.class, "LBL_Memory"), true), DataViewComponent.TOP_RIGHT); // NOI18N
        dvc.addDetailsView(physicalMemoryViewSupport.getDetailsView(), DataViewComponent.TOP_RIGHT);
        
        final SwapMemoryViewSupport swapMemoryViewSupport = new SwapMemoryViewSupport();
        dvc.addDetailsView(swapMemoryViewSupport.getDetailsView(), DataViewComponent.TOP_RIGHT);
        
        timer = new Timer(2000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final long time = System.currentTimeMillis();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        cpuLoadViewSupport.refresh(hostOverview, time);
                        physicalMemoryViewSupport.refresh(hostOverview, time);
                        swapMemoryViewSupport.refresh(hostOverview, time);
                    }
                });
            }
        });
        timer.setInitialDelay(800);
        timer.start();
        ((Host)getDataSource()).notifyWhenRemoved(this);
        
        return dvc;
    }
    
    
    // --- General data --------------------------------------------------------
    
    private static class MasterViewSupport extends JPanel  {
        
        public MasterViewSupport(Host host) {
            initComponents(host);
        }
        
        
        public DataViewComponent.MasterView getMasterView() {
            return new DataViewComponent.MasterView(NbBundle.getMessage(HostOverviewView.class, "LBL_Overview"), null, this);   // NOI18N
        }
        
        
        private void initComponents(Host host) {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            HTMLTextArea area = new HTMLTextArea("<nobr>" + getGeneralInfo(host) + "</nobr>");  // NOI18N
            area.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
            
            add(area, BorderLayout.CENTER);
        }
        
        String getGeneralInfo(Host host) {
            HostOverview so = HostOverviewFactory.getSystemOverviewFor(host);
            StringBuilder data = new StringBuilder();
            String hostIp = NbBundle.getMessage(HostOverviewView.class, "LBL_Host_IP"); // NOI18N
            data.append("<b>"+ hostIp + ":</b>"+ so.getHostAddress() + "<br>"); // NOI18N
            
            String hostname = NbBundle.getMessage(HostOverviewView.class, "LBL_Hostname");  // NOI18N
            data.append("<b>"+ hostname + ":</b>" + so.getHostName() + "<br><br>"); // NOI18N

            String name = so.getName();
            String ver = so.getVersion();
            String patch = so.getPatchLevel();

            patch = "unknown".equals(patch) ? "" : patch;   // NOI18N
            String os = NbBundle.getMessage(HostOverviewView.class, "LBL_OS");  // NOI18N
            String arch = NbBundle.getMessage(HostOverviewView.class, "LBL_Architecture");  // NOI18N
            String proc = NbBundle.getMessage(HostOverviewView.class, "LBL_Processors");    // NOI18N
            String memory = NbBundle.getMessage(HostOverviewView.class, "LBL_Total_memory_size");    // NOI18N
            String swap = NbBundle.getMessage(HostOverviewView.class, "LBL_Swap_size"); // NOI18N
            String mb = NbBundle.getMessage(HostOverviewView.class, "LBL_MB");  // NOI18N
            data.append("<b>"+os+":</b> " + name + " (" + ver + ")" + " " + patch + "<br>");    // NOI18N
            data.append("<b>"+arch+":</b> " + so.getArch() + "<br>");   // NOI18N
            data.append("<b>"+proc+":</b> " + so.getAvailableProcessors() + "<br><br>");    // NOI18N
            data.append("<b>"+memory+":</b> " + formatBytes(so.getTotalPhysicalMemorySize()) + " "+mb+"<br>");  // NOI18N
            data.append("<b>"+swap+":</b> " + formatBytes(so.getTotalSwapSpaceSize()) + " "+mb+"<br>"); // NOI18N

            return data.toString();
        }
        
        private String formatBytes(long l) {
            return NumberFormat.getInstance().format(((10*l)/1024/1024)/10.0);
        }
        
    }
    
    
    // --- CPU load ------------------------------------------------------------
    
    private static class CpuLoadViewSupport extends JPanel  {
        
        private boolean cpuMonitoringSupported;
        private ChartsSupport.Chart cpuMetricsChart;
        private HTMLLabel loadLabel;
        private static final NumberFormat formatter = NumberFormat.getNumberInstance();
        private static final int refLabelHeight = new HTMLLabel("X").getPreferredSize().height; // NOI18N
        private static final String LOAD_AVERAGE = NbBundle.getMessage(HostOverviewView.class, "LBL_Load_average"); // NOI18N
        
        public CpuLoadViewSupport(HostOverview hostOverview) {
            cpuMonitoringSupported = hostOverview.getSystemLoadAverage() >= 0;
            initComponents();
        }        
        
        public DataViewComponent.DetailsView getDetailsView() {
            return new DataViewComponent.DetailsView(NbBundle.getMessage(HostOverviewView.class, "LBL_CPU_load"), null, 10, this, null);    // NOI18N
        }
        
        public void refresh(HostOverview hostOverview, long time) {
            if (cpuMonitoringSupported) {
                double load = hostOverview.getSystemLoadAverage();
                cpuMetricsChart.getModel().addItemValues(time, new long[] { (long)(load*1000) });
                loadLabel.setText("<nobr><b>"+LOAD_AVERAGE+":</b> " + formatter.format(load) + " </nobr>"); // NOI18N
                cpuMetricsChart.setToolTipText("<html><nobr><b>"+LOAD_AVERAGE+":</b> " + formatter.format(load) + " </nobr></html>");   // NOI18N
            }
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            JComponent contents;
            
            if (cpuMonitoringSupported) {
              // CPUMetricsPanel
              loadLabel = new HTMLLabel() {
                public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, refLabelHeight); }
                public Dimension getMinimumSize() { return getPreferredSize(); }
                public Dimension getMaximumSize() { return getPreferredSize(); }
              };
              loadLabel.setText("<nobr><b>"+LOAD_AVERAGE+":</b></nobr>");   // NOI18N
              loadLabel.setOpaque(false);
              final JPanel cpuMetricsDataPanel = new JPanel(new GridLayout(1, 2));
              cpuMetricsDataPanel.setOpaque(false);
              cpuMetricsDataPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
              cpuMetricsDataPanel.add(loadLabel);

              cpuMetricsChart = new ChartsSupport.CPUMetricsChart();
              cpuMetricsChart.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20), cpuMetricsChart.getBorder()));
              JPanel cpuMetricsLegendContainer = new JPanel(new FlowLayout(FlowLayout.TRAILING));
              cpuMetricsLegendContainer.setOpaque(false);
              cpuMetricsLegendContainer.add(cpuMetricsChart.getBigLegendPanel());
              final JPanel cpuMetricsPanel = new JPanel(new BorderLayout());
              cpuMetricsPanel.setOpaque(true);
              cpuMetricsPanel.setBackground(Color.WHITE);
              cpuMetricsPanel.add(cpuMetricsDataPanel, BorderLayout.NORTH);
              cpuMetricsPanel.add(cpuMetricsChart, BorderLayout.CENTER);
              cpuMetricsPanel.add(cpuMetricsLegendContainer, BorderLayout.SOUTH);

              final boolean[] cpuMetricsPanelResizing = new boolean[] { false };
              cpuMetricsPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
              cpuMetricsPanel.addComponentListener(new ComponentAdapter() {
                  public void componentResized(ComponentEvent e) {
                      if (cpuMetricsPanelResizing[0] == true) {
                          cpuMetricsPanelResizing[0] = false;
                          return;
                      }

                      boolean shouldBeVisible = cpuMetricsPanel.getSize().height > ChartsSupport.MINIMUM_CHART_HEIGHT;
                      if (shouldBeVisible == cpuMetricsDataPanel.isVisible()) return;

                      cpuMetricsPanelResizing[0] = true;
                      cpuMetricsDataPanel.setVisible(shouldBeVisible);
                  }
              });
              contents = cpuMetricsPanel;
            } else {
                contents = new NotSupportedDisplayer(NotSupportedDisplayer.HOST);
            }
            
            add(contents, BorderLayout.CENTER);
        }
        
    }
    
    
    // --- Physical memory -----------------------------------------------------
    
    private static class PhysicalMemoryViewSupport extends JPanel  {
        
        private ChartsSupport.Chart memoryMetricsChart;
        private HTMLLabel usedMemoryLabel;
        private HTMLLabel totalMemoryLabel;
        private static final NumberFormat formatter = NumberFormat.getNumberInstance();
        private static final int refLabelHeight = new HTMLLabel("X").getPreferredSize().height; // NOI18N
        private static String USED_MEMORY = NbBundle.getMessage(HostOverviewView.class, "LBL_Used_memory"); // NOI18N
        private static String TOTAL_MEMORY = NbBundle.getMessage(HostOverviewView.class, "LBL_Total_memory");   // NOI18N

        public PhysicalMemoryViewSupport() {
            initComponents();
        }        
        
        public DataViewComponent.DetailsView getDetailsView() {
            return new DataViewComponent.DetailsView(NbBundle.getMessage(HostOverviewView.class, "LBL_Physical_memory"), null, 10, this, null); // NOI18N
        }
        
        public void refresh(HostOverview hostOverview, long time) {
            long memoryMax = hostOverview.getTotalPhysicalMemorySize();
            long memoryUsed = memoryMax - hostOverview.getFreePhysicalMemorySize();

            memoryMetricsChart.getModel().addItemValues(time, new long[] { memoryUsed });
            usedMemoryLabel.setText("<nobr><b>"+USED_MEMORY+":</b> " + formatter.format(memoryUsed) + " </nobr>");  // NOI18N
            totalMemoryLabel.setText("<nobr><b>"+TOTAL_MEMORY+":</b> " + formatter.format(memoryMax) + " </nobr>"); // NOI18N
            memoryMetricsChart.setToolTipText(
                            "<html><nobr><b>"+USED_MEMORY+":</b> " + formatter.format(memoryUsed) + " </nobr>" + "<br>" +   // NOI18N
                            "<nobr><b>"+TOTAL_MEMORY+":</b> " + formatter.format(memoryMax) + " </nobr></html>");   // NOI18N
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            setOpaque(false);
            
              // MemoryMetricsPanel
              usedMemoryLabel = new HTMLLabel() {
                public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, refLabelHeight); }
                public Dimension getMinimumSize() { return getPreferredSize(); }
                public Dimension getMaximumSize() { return getPreferredSize(); }
              };
              usedMemoryLabel.setText("<nobr><b>"+USED_MEMORY+":</b></nobr>");  // NOI18N
              usedMemoryLabel.setOpaque(false);
              totalMemoryLabel = new HTMLLabel() {
                public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, refLabelHeight); }
                public Dimension getMinimumSize() { return getPreferredSize(); }
                public Dimension getMaximumSize() { return getPreferredSize(); }
              };
              totalMemoryLabel.setText("<nobr><b>"+TOTAL_MEMORY+":</b></nobr>");    // NOI18N
              totalMemoryLabel.setOpaque(false);
              final JPanel memoryMetricsDataPanel = new JPanel(new GridLayout(1, 2));
              memoryMetricsDataPanel.setOpaque(false);
              memoryMetricsDataPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
              memoryMetricsDataPanel.add(usedMemoryLabel);
              memoryMetricsDataPanel.add(totalMemoryLabel);

              memoryMetricsChart = new ChartsSupport.PhysicalMemoryMetricsChart();
              memoryMetricsChart.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20), memoryMetricsChart.getBorder()));
              JPanel memoryMetricsLegendContainer = new JPanel(new FlowLayout(FlowLayout.TRAILING));
              memoryMetricsLegendContainer.setOpaque(false);
              memoryMetricsLegendContainer.add(memoryMetricsChart.getBigLegendPanel());
              final JPanel memoryMetricsPanel = new JPanel(new BorderLayout());
              memoryMetricsPanel.setOpaque(true);
              memoryMetricsPanel.setBackground(Color.WHITE);
              memoryMetricsPanel.add(memoryMetricsDataPanel, BorderLayout.NORTH);
              memoryMetricsPanel.add(memoryMetricsChart, BorderLayout.CENTER);
              memoryMetricsPanel.add(memoryMetricsLegendContainer, BorderLayout.SOUTH);

              final boolean[] memoryMetricsPanelResizing = new boolean[] { false };
              memoryMetricsPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
              memoryMetricsPanel.addComponentListener(new ComponentAdapter() {
                  public void componentResized(ComponentEvent e) {
                      if (memoryMetricsPanelResizing[0] == true) {
                          memoryMetricsPanelResizing[0] = false;
                          return;
                      }

                      boolean shouldBeVisible = memoryMetricsPanel.getSize().height > ChartsSupport.MINIMUM_CHART_HEIGHT;
                      if (shouldBeVisible == memoryMetricsDataPanel.isVisible()) return;

                      memoryMetricsPanelResizing[0] = true;
                      memoryMetricsDataPanel.setVisible(shouldBeVisible);
                  }
              });
            
            add(memoryMetricsPanel, BorderLayout.CENTER);
        }
        
    }
    
    
    // --- Swap memory ---------------------------------------------------------
    
    private static class SwapMemoryViewSupport extends JPanel  {
        
        private ChartsSupport.Chart memorySwapMetricsChart;
        private HTMLLabel usedSwapMemoryLabel;
        private HTMLLabel totalSwapMemoryLabel;
        private static final NumberFormat formatter = NumberFormat.getNumberInstance();
        private static final int refLabelHeight = new HTMLLabel("X").getPreferredSize().height; // NOI18N
        private static final String USED_SWAP = NbBundle.getMessage(HostOverviewView.class, "LBL_Used_swap");   // NOI18N
        private static final String TOTAL_SWAP = NbBundle.getMessage(HostOverviewView.class, "LBL_Total_swap"); // NOI18N

        public SwapMemoryViewSupport() {
            initComponents();
        }        
        
        public DataViewComponent.DetailsView getDetailsView() {
            return new DataViewComponent.DetailsView(NbBundle.getMessage(HostOverviewView.class, "LBL_Swap_memory"), null, 20, this, null); // NOI18N
        }
        
        public void refresh(HostOverview hostOverview, long time) {
            long memorySwapMax = hostOverview.getTotalSwapSpaceSize();
            long memorySwapUsed = memorySwapMax - hostOverview.getFreeSwapSpaceSize();

            memorySwapMetricsChart.getModel().addItemValues(time, new long[] { memorySwapUsed });
            usedSwapMemoryLabel.setText("<nobr><b>"+USED_SWAP+":</b> " + formatter.format(memorySwapUsed) + " </nobr>");    // NOI18N
            totalSwapMemoryLabel.setText("<nobr><b>"+TOTAL_SWAP+":</b> " + formatter.format(memorySwapMax) + " </nobr>");   // NOI18N
            memorySwapMetricsChart.setToolTipText(
                            "<html><nobr><b>"+USED_SWAP+":</b> " + formatter.format(memorySwapUsed) + " </nobr>" + "<br>" +     // NOI18N
                            "<nobr><b>"+TOTAL_SWAP+":</b> " + formatter.format(memorySwapMax) + " </nobr></html>"); // NOI18N
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            setOpaque(false);
            
              // SwapMetricsPanel
              usedSwapMemoryLabel = new HTMLLabel() {
                public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, refLabelHeight); }
                public Dimension getMinimumSize() { return getPreferredSize(); }
                public Dimension getMaximumSize() { return getPreferredSize(); }
              };
              usedSwapMemoryLabel.setText("<nobr><b>"+USED_SWAP+":</b></nobr>");    // NOI18N
              usedSwapMemoryLabel.setOpaque(false);
              totalSwapMemoryLabel = new HTMLLabel() {
                public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, refLabelHeight); }
                public Dimension getMinimumSize() { return getPreferredSize(); }
                public Dimension getMaximumSize() { return getPreferredSize(); }
              };
              totalSwapMemoryLabel.setText("<nobr><b>"+TOTAL_SWAP+":</b></nobr>");  // NOI18N
              totalSwapMemoryLabel.setOpaque(false);
              final JPanel memorySwapMetricsDataPanel = new JPanel(new GridLayout(1, 2));
              memorySwapMetricsDataPanel.setOpaque(false);
              memorySwapMetricsDataPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
              memorySwapMetricsDataPanel.add(usedSwapMemoryLabel);
              memorySwapMetricsDataPanel.add(totalSwapMemoryLabel);

              memorySwapMetricsChart = new ChartsSupport.SwapMemoryMetricsChart();
              memorySwapMetricsChart.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20), memorySwapMetricsChart.getBorder()));
              JPanel memorySwapMetricsLegendContainer = new JPanel(new FlowLayout(FlowLayout.TRAILING));
              memorySwapMetricsLegendContainer.setOpaque(false);
              memorySwapMetricsLegendContainer.add(memorySwapMetricsChart.getBigLegendPanel());
              final JPanel memorySwapMetricsPanel = new JPanel(new BorderLayout());
              memorySwapMetricsPanel.setOpaque(true);
              memorySwapMetricsPanel.setBackground(Color.WHITE);
              memorySwapMetricsPanel.add(memorySwapMetricsDataPanel, BorderLayout.NORTH);
              memorySwapMetricsPanel.add(memorySwapMetricsChart, BorderLayout.CENTER);
              memorySwapMetricsPanel.add(memorySwapMetricsLegendContainer, BorderLayout.SOUTH);

              final boolean[] memorySwapMetricsPanelResizing = new boolean[] { false };
              memorySwapMetricsPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
              memorySwapMetricsPanel.addComponentListener(new ComponentAdapter() {
                  public void componentResized(ComponentEvent e) {
                      if (memorySwapMetricsPanelResizing[0] == true) {
                          memorySwapMetricsPanelResizing[0] = false;
                          return;
                      }

                      boolean shouldBeVisible = memorySwapMetricsPanel.getSize().height > ChartsSupport.MINIMUM_CHART_HEIGHT;
                      if (shouldBeVisible == memorySwapMetricsDataPanel.isVisible()) return;

                      memorySwapMetricsPanelResizing[0] = true;
                      memorySwapMetricsDataPanel.setVisible(shouldBeVisible);
                  }
              });
            
            add(memorySwapMetricsPanel, BorderLayout.CENTER);
        }
        
    }

}
