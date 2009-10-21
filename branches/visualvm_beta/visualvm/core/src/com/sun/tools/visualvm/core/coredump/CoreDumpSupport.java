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

import com.sun.tools.visualvm.core.datasupport.Storage;
import com.sun.tools.visualvm.core.model.dsdescr.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.core.snapshot.RegisteredSnapshotCategories;
import com.sun.tools.visualvm.core.snapshot.SnapshotCategory;
import java.io.File;

/**
 *
 * @author Tomas Hurka
 */
public final class CoreDumpSupport {
    
    private static final String COREDUMPS_STORAGE_DIRNAME = "coredumps";
    
    private static File coredumpsStorageDirectory;
    private static String coredumpsStorageDirectoryString;
    
    private static CoreDumpCategory category = new CoreDumpCategory();
    private static CoreDumpProvider provider;
    private static String currentJDKHome;
    
    
    public static SnapshotCategory getCategory() {
        return category;
    }
    
    static CoreDumpCategory getCoreDumpCategory() {
        return category;
    }
    
    static CoreDumpProvider getProvider() {
        return provider;
    }
    
    // TODO: should be moved to some public Utils class
    static String getCurrentJDKHome() {
        if (currentJDKHome == null) {
            currentJDKHome = System.getProperty("java.home");
            String jreSuffix = File.separator + "jre";
            if (currentJDKHome.endsWith(jreSuffix)) currentJDKHome = currentJDKHome.substring(0, currentJDKHome.length() - jreSuffix.length());
        }
        return currentJDKHome;
    }
    
    static String getStorageDirectoryString() {
        if (coredumpsStorageDirectoryString == null)
            coredumpsStorageDirectoryString = Storage.getPersistentStorageDirectoryString() + File.separator + COREDUMPS_STORAGE_DIRNAME;
        return coredumpsStorageDirectoryString;
    }
    
    static File getStorageDirectory() {
        if (coredumpsStorageDirectory == null) {
            String snapshotsStorageString = getStorageDirectoryString();
            coredumpsStorageDirectory = new File(snapshotsStorageString);
            if (coredumpsStorageDirectory.exists() && coredumpsStorageDirectory.isFile())
                throw new IllegalStateException("Cannot create coredumps storage directory " + snapshotsStorageString + ", file in the way");
            if (coredumpsStorageDirectory.exists() && (!coredumpsStorageDirectory.canRead() || !coredumpsStorageDirectory.canWrite()))
                throw new IllegalStateException("Cannot access coredumps storage directory " + snapshotsStorageString + ", read&write permission required");
            if (!coredumpsStorageDirectory.exists() && !coredumpsStorageDirectory.mkdirs())
                throw new IllegalStateException("Cannot create coredumps storage directory " + snapshotsStorageString);
        }
        return coredumpsStorageDirectory;
    }
    
    static boolean storageDirectoryExists() {
        return new File(getStorageDirectoryString()).isDirectory();
    }

    
    public static void register() {
        DataSourceDescriptorFactory.getDefault().registerFactory(new CoreDumpDescriptorProvider());
        CoreDumpsContainerProvider.register();
        CoreDumpActionsProvider.register();
        provider = new CoreDumpProvider();
        provider.register();
        RegisteredSnapshotCategories.sharedInstance().addCategory(category);
    }

}