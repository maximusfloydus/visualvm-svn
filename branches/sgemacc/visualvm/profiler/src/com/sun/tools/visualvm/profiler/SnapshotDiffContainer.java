/*
 *  Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Sun designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Sun in the LICENSE file that accompanied this code.
 * 
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 * 
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 *  CA 95054 USA or visit www.sun.com if you need additional information or
 *  have any questions.
 */

package com.sun.tools.visualvm.profiler;

import com.sun.tools.visualvm.core.datasource.DataSource;
import org.netbeans.lib.profiler.results.ResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.AllocMemoryResultsDiff;
import org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsDiff;
import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot;
import org.netbeans.modules.profiler.LoadedSnapshot;

/**
 *
 * @author Jiri Sedlacek
 */
class SnapshotDiffContainer extends DataSource {
    
    private ResultsSnapshot diffSnapshot;
    private ProfilerSnapshot snapshot1;
    private ProfilerSnapshot snapshot2;
    
    public SnapshotDiffContainer(ProfilerSnapshot ps1, ProfilerSnapshot ps2, DataSource master) {
        super(master);
        diffSnapshot = createDiff(ps1, ps2);
        if (diffSnapshot == null) throw new UnsupportedOperationException(
                                  "Unable to create diff from " + ps1 + " and " + ps2); // NOI18N
        snapshot1 = ps1;
        snapshot2 = ps2;
    }
    
    
    public ResultsSnapshot getDiff() {
        return diffSnapshot;
    }
    
    public ProfilerSnapshot getSnapshot1() {
        return snapshot1;
    }
    
    public ProfilerSnapshot getSnapshot2() {
        return snapshot2;
    }
    
    
    private static ResultsSnapshot createDiff(ProfilerSnapshot ps1, ProfilerSnapshot ps2) {
        LoadedSnapshot s1 = ps1.getLoadedSnapshot();
        LoadedSnapshot s2 = ps2.getLoadedSnapshot();
        
        if (s1.getSnapshot() instanceof AllocMemoryResultsSnapshot &&
            s2.getSnapshot() instanceof AllocMemoryResultsSnapshot)
            return new AllocMemoryResultsDiff((AllocMemoryResultsSnapshot) s1.getSnapshot(),
                                              (AllocMemoryResultsSnapshot) s2.getSnapshot());
        else if (s1.getSnapshot() instanceof LivenessMemoryResultsSnapshot &&
                 s2.getSnapshot() instanceof LivenessMemoryResultsSnapshot)
            return new LivenessMemoryResultsDiff((LivenessMemoryResultsSnapshot) s1.getSnapshot(),
                                                 (LivenessMemoryResultsSnapshot) s2.getSnapshot());
        return null;
    }

}