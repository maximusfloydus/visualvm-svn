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

package com.sun.tools.visualvm.core.snapshot;

import com.sun.tools.visualvm.core.explorer.ExplorerActionDescriptor;
import com.sun.tools.visualvm.core.explorer.ExplorerActionsProvider;
import com.sun.tools.visualvm.core.explorer.ExplorerContextMenuFactory;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Jiri Sedlacek
 */
final class SnapshotActionProvider {

    private static SnapshotActionProvider instance;
    

    public static synchronized SnapshotActionProvider getInstance() {
        if (instance == null) instance = new SnapshotActionProvider();
        return instance;
    }


    void initialize() {
        ExplorerContextMenuFactory.sharedInstance().addExplorerActionsProvider(new SnapshotActionsProvider(), Snapshot.class);
    }
    
    private SnapshotActionProvider() {
    }
    
    
    private class SnapshotActionsProvider implements ExplorerActionsProvider<Snapshot> {
        
        public ExplorerActionDescriptor getDefaultAction(Set<Snapshot> snapshots) {
            if (snapshots.size() == 1 && snapshots.iterator().next().supportsSaveAs())
                return new ExplorerActionDescriptor(SaveSnapshotAsAction.getInstance(), 20);
            else return null;
        }

        public Set<ExplorerActionDescriptor> getActions(Set<Snapshot> snapshots) {
            Set<ExplorerActionDescriptor> actions = new HashSet();
            
            if (DeleteSnapshotAction.getInstance().isEnabled())
                actions.add(new ExplorerActionDescriptor(DeleteSnapshotAction.getInstance(), 100));
            
            return actions;
        }
        
    }

}