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

package com.sun.tools.visualvm.core.coredump;

import com.sun.tools.visualvm.core.datasource.DataSourceRepository;
import com.sun.tools.visualvm.core.datasource.Snapshot;
import com.sun.tools.visualvm.core.datasupport.Storage;
import com.sun.tools.visualvm.core.datasupport.Utils;
import com.sun.tools.visualvm.core.explorer.ExplorerSupport;
import com.sun.tools.visualvm.core.model.dsdescr.DataSourceDescriptor;
import com.sun.tools.visualvm.core.model.dsdescr.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.core.snapshot.SnapshotProvider;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
// A provider for Coredumps
class CoreDumpProvider extends SnapshotProvider<CoreDumpImpl> {
    
    static final String SNAPSHOT_VERSION = "snapshot_version";
    static final String SNAPSHOT_VERSION_DIVIDER = ".";
    static final String CURRENT_SNAPSHOT_VERSION_MAJOR = "1";
    static final String CURRENT_SNAPSHOT_VERSION_MINOR = "0";
    static final String CURRENT_SNAPSHOT_VERSION = CURRENT_SNAPSHOT_VERSION_MAJOR + SNAPSHOT_VERSION_DIVIDER + CURRENT_SNAPSHOT_VERSION_MINOR;
    
    
    void createCoreDump(final String coreDumpFile, final String displayName, final String jdkHome) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                createCoreDumpImpl(coreDumpFile, displayName, jdkHome);
            }
        });
    }
    
    private void createCoreDumpImpl(final String coreDumpFile, final String displayName, final String jdkHome) {
        
        final CoreDumpImpl knownCoreDump = getCoreDumpByFile(new File(coreDumpFile));
        if (knownCoreDump != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ExplorerSupport.sharedInstance().selectDataSource(knownCoreDump);
                    NetBeansProfiler.getDefaultNB().displayWarning("<html>Core dump " + displayName + " already added as " + DataSourceDescriptorFactory.getDescriptor(knownCoreDump).getName() + "</html>");
                }
            });
            return;
        }
        
        String[] propNames = new String[] { SNAPSHOT_VERSION,
            Snapshot.PROPERTY_FILE, DataSourceDescriptor.PROPERTY_NAME, CoreDumpImpl.PROPERTY_JAVA_HOME };
        String[] propValues = new String[] { CURRENT_SNAPSHOT_VERSION,
            coreDumpFile, displayName, jdkHome };

        File customPropertiesStorage = Utils.getUniqueFile(CoreDumpSupport.getStorageDirectory(), new File(coreDumpFile).getName(), Storage.DEFAULT_PROPERTIES_EXT);
        Storage storage = new Storage(customPropertiesStorage.getParentFile(), customPropertiesStorage.getName());
        storage.setCustomProperties(propNames, propValues);

        CoreDumpImpl newCoreDump = null;
        try {
            newCoreDump = new CoreDumpImpl(storage, customPropertiesStorage);
        } catch (Exception e) {
            System.err.println("Error creating coredump: " + e.getMessage());
            return;
        }

        if (newCoreDump != null) {
            CoreDumpsContainer.sharedInstance().getRepository().addDataSource(newCoreDump);
            registerDataSource(newCoreDump);
        }
    }
    
    private CoreDumpImpl getCoreDumpByFile(File file) {
        if (!file.isFile()) return null;
        Set<CoreDumpImpl> knownCoredumps = getDataSources(CoreDumpImpl.class);
        for (CoreDumpImpl knownCoredump : knownCoredumps)
            if (knownCoredump.getFile().equals(file)) return knownCoredump;
        return null;
    }

    void removeCoreDump(CoreDumpImpl coreDump, boolean interactive) {
        // TODO: if interactive, show a Do-Not-Show-Again confirmation dialog
        unregisterDataSource(coreDump);
        File customPropertiesStorage = coreDump.getCustomPropertiesStorage();
        if (!customPropertiesStorage.delete()) customPropertiesStorage.deleteOnExit();
    }
    
    
    protected <Y extends CoreDumpImpl> void unregisterDataSources(final Set<Y> removed) {
        super.unregisterDataSources(removed);
        for (CoreDumpImpl coreDump : removed) {
            CoreDumpsContainer.sharedInstance().getRepository().removeDataSource(coreDump);
            coreDump.finished();
        }
    }
    
    private void initPersistedCoreDumps() {
        if (!CoreDumpSupport.storageDirectoryExists()) return;
        
        File[] files = CoreDumpSupport.getStorageDirectory().listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(Storage.DEFAULT_PROPERTIES_EXT);
            }
        });
        
        Set<CoreDumpImpl> coredumps = new HashSet();
        for (File file : files) {
            Storage storage = new Storage(file.getParentFile(), file.getName());
            CoreDumpImpl persistedCoredump = null;
            
            try {
                persistedCoredump = new CoreDumpImpl(storage, file);
            } catch (Exception e) {
                System.err.println("Error loading persisted host: " + e.getMessage());
            }
            
            if (persistedCoredump != null) coredumps.add(persistedCoredump);
        }
        
        CoreDumpsContainer.sharedInstance().getRepository().addDataSources(coredumps);
        registerDataSources(coredumps);
    }
    
    
    CoreDumpProvider() {
    }
    
    void register() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                initPersistedCoreDumps();
            }
        });
        DataSourceRepository.sharedInstance().addDataSourceProvider(this);
    }
  
}