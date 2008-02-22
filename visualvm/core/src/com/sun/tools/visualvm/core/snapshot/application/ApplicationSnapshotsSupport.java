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

package com.sun.tools.visualvm.core.snapshot.application;

import com.sun.tools.visualvm.core.model.dsdescr.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.core.snapshot.RegisteredSnapshotCategories;
import com.sun.tools.visualvm.core.snapshot.SnapshotCategory;

/**
 * Support for snapshots in VisualVM.
 *
 * @author Jiri Sedlacek
 */
public final class ApplicationSnapshotsSupport {
    
    private static ApplicationSnapshotsSupport instance;

    private ApplicationSnapshotProvider snapshotProvider;
    private SnapshotCategory snapshotCategory;


    /**
     * Returns singleton instance of ApplicationSnapshotsSupport.
     * 
     * @return singleton instance of ApplicationSnapshotsSupport.
     */
    public static synchronized ApplicationSnapshotsSupport getInstance() {
        if (instance == null) instance = new ApplicationSnapshotsSupport();
        return instance;
    }
    
    
    /**
     * Returns SnapshotCategory instance for application snapshots.
     * 
     * @return SnapshotCategory instance for application snapshots.
     */
    public SnapshotCategory getCategory() {
        return snapshotCategory;
    }
    
    
    ApplicationSnapshotProvider getSnapshotProvider() {
        return snapshotProvider;
    }
    
    
    private ApplicationSnapshotsSupport() {
        snapshotProvider = ApplicationSnapshotProvider.sharedInstance();
        snapshotCategory = new ApplicationSnapshotCategory(snapshotProvider);
        DataSourceDescriptorFactory.getDefault().registerFactory(new ApplicationSnapshotDescriptorProvider());
        RegisteredSnapshotCategories.sharedInstance().addCategory(snapshotCategory);
        
        snapshotProvider.initialize();
        ApplicationSnapshotActionProvider.getInstance().initialize();
    }

}
