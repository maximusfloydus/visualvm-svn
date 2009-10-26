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

package com.sun.tools.visualvm.profiler;

import com.sun.tools.visualvm.core.snapshot.SnapshotDescriptor;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.profiler.LoadedSnapshot;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jiri Sedlacek
 */
class ProfilerSnapshotDescriptor extends SnapshotDescriptor<ProfilerSnapshot> {
    private static final Logger LOGGER = Logger.getLogger(ProfilerSnapshotDescriptor.class.getName());
    
    private static final Image CPU_ICON = ImageUtilities.loadImage("org/netbeans/modules/profiler/resources/cpuSmall.png", true);    // NOI18N
    private static final Image MEMORY_ICON = ImageUtilities.loadImage("org/netbeans/modules/profiler/resources/memorySmall.png", true);  // NOI18N
    private static final Image NODE_BADGE = ImageUtilities.loadImage("com/sun/tools/visualvm/core/ui/resources/snapshotBadge.png", true);    // NOI18N
    

    public ProfilerSnapshotDescriptor(ProfilerSnapshot snapshot) {
        super(snapshot, resolveIcon(snapshot));
    }
    
    
    private static Image resolveIcon(ProfilerSnapshot snapshot) {
        try {
            int snapshotType = snapshot.getLoadedSnapshot().getType();
            if (snapshotType == LoadedSnapshot.SNAPSHOT_TYPE_CPU)
                return ImageUtilities.mergeImages(CPU_ICON, NODE_BADGE, 0, 0);
            else if (snapshotType == LoadedSnapshot.SNAPSHOT_TYPE_MEMORY_LIVENESS || snapshotType == LoadedSnapshot.SNAPSHOT_TYPE_MEMORY_ALLOCATIONS)
                return ImageUtilities.mergeImages(MEMORY_ICON, NODE_BADGE, 0, 0);
            else return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to determine profiler snapshot type", e);  // NOI18N
            return null;
        }
    }
        
}