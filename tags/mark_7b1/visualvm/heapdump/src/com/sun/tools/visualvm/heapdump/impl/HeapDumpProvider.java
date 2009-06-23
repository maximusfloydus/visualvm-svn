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

package com.sun.tools.visualvm.heapdump.impl;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.ApplicationSnapshot;
import com.sun.tools.visualvm.coredump.CoreDump;
import com.sun.tools.visualvm.core.datasource.DataSourceRepository;
import com.sun.tools.visualvm.core.datasupport.DataChangeEvent;
import com.sun.tools.visualvm.core.datasupport.DataChangeListener;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import com.sun.tools.visualvm.core.ui.DataSourceWindowManager;
import com.sun.tools.visualvm.heapdump.HeapDumpSupport;
import com.sun.tools.visualvm.tools.sa.SaModel;
import com.sun.tools.visualvm.tools.sa.SaModelFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
public class HeapDumpProvider implements DataChangeListener<ApplicationSnapshot> {
    
    
    public void dataChanged(DataChangeEvent<ApplicationSnapshot> event) {
        Set<ApplicationSnapshot> snapshots = event.getAdded();
        for (ApplicationSnapshot snapshot : snapshots) processNewSnapshot(snapshot);
    }
    
    
    private void processNewSnapshot(ApplicationSnapshot snapshot) {
        Set<HeapDumpImpl> heapDumps = new HashSet();
        File[] files = snapshot.getFile().listFiles(HeapDumpSupport.getInstance().getCategory().getFilenameFilter());
        for (File file : files) heapDumps.add(new HeapDumpImpl(file, snapshot));
        snapshot.getRepository().addDataSources(heapDumps);
    }
    
    
    public void createHeapDump(final Application application, final boolean openView) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Jvm jvm = JvmFactory.getJVMFor(application);
                if (!jvm.isTakeHeapDumpSupported()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            NetBeansProfiler.getDefaultNB().displayError("Cannot take heap dump for " + DataSourceDescriptorFactory.getDescriptor(application).getName());
                        }
                    });
                    return;
                }
                
                ProgressHandle pHandle = null;
                try {
                    pHandle = ProgressHandleFactory.createHandle("Creating Heap Dump...");
                    pHandle.setInitialDelay(0);
                    pHandle.start();
                    try {
                        final HeapDumpImpl heapDump = new HeapDumpImpl(jvm.takeHeapDump(), application);
                        application.getRepository().addDataSource(heapDump);
                        if (openView) SwingUtilities.invokeLater(new Runnable() {
                            public void run() { DataSourceWindowManager.sharedInstance().openDataSource(heapDump); }
                        });
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                } finally {
                    final ProgressHandle pHandleF = pHandle;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() { if (pHandleF != null) pHandleF.finish(); }
                    });
                }
            }
        });
    }
    
    public void createHeapDump(final CoreDump coreDump, final boolean openView) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProgressHandle pHandle = null;
                try {
                    pHandle = ProgressHandleFactory.createHandle("Creating Heap Dump...");
                    pHandle.setInitialDelay(0);
                    pHandle.start();
                    File snapshotDir = coreDump.getStorage().getDirectory();
                    String name = HeapDumpSupport.getInstance().getCategory().createFileName();
                    File dumpFile = new File(snapshotDir,name);
                    SaModel saAget = SaModelFactory.getSAAgentFor(coreDump);
                    try {
                        if (saAget.takeHeapDump(dumpFile.getAbsolutePath())) {
                            final HeapDumpImpl heapDump = new HeapDumpImpl(dumpFile, coreDump);
                            coreDump.getRepository().addDataSource(heapDump);
                            if (openView) SwingUtilities.invokeLater(new Runnable() {
                                public void run() { DataSourceWindowManager.sharedInstance().openDataSource(heapDump); }
                            });
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                } finally {
                    final ProgressHandle pHandleF = pHandle;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() { if (pHandleF != null) pHandleF.finish(); }
                    });
                }
            }
        });
    }
    
    public void initialize() {
        DataSourceRepository.sharedInstance().addDataChangeListener(this, ApplicationSnapshot.class);
    }
    
}