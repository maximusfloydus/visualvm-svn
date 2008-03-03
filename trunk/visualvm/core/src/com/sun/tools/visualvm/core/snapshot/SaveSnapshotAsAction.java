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
package com.sun.tools.visualvm.core.snapshot;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.Snapshot;
import com.sun.tools.visualvm.core.explorer.ExplorerSelectionListener;
import com.sun.tools.visualvm.core.explorer.ExplorerSupport;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.openide.util.Utilities;

public final class SaveSnapshotAsAction extends AbstractAction {
    
    private static SaveSnapshotAsAction instance;
    private static SaveSnapshotAsAction noIconInstance;
    
    private static final Image ICON_16 =  Utilities.loadImage("com/sun/tools/visualvm/core/ui/resources/saveSnapshot.png");
    private static final Image ICON_24 =  Utilities.loadImage("com/sun/tools/visualvm/core/ui/resources/saveSnapshot24.png");
    
    
    public static synchronized SaveSnapshotAsAction getInstance() {
        if (instance == null) instance = new SaveSnapshotAsAction();
        return instance;
    }
    
    public static synchronized SaveSnapshotAsAction getNoIconInstance() {
        if (noIconInstance == null) {
            noIconInstance = new SaveSnapshotAsAction();
            noIconInstance.putValue(Action.SMALL_ICON, null);
            noIconInstance.putValue("iconBase", null);
        }
        return noIconInstance;
    }
    
    public void actionPerformed(ActionEvent e) {
        Snapshot selectedSnapshot = getSelectedSnapshot();
        if (selectedSnapshot != null && selectedSnapshot.supportsSaveAs()) selectedSnapshot.saveAs();
    }
    
    void updateEnabled() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Snapshot selectedSnapshot = getSelectedSnapshot();
                setEnabled(selectedSnapshot != null && selectedSnapshot.supportsSaveAs());
            }
        });
    }
    
    private Snapshot getSelectedSnapshot() {
        DataSource selectedDataSource = ExplorerSupport.sharedInstance().getSelectedDataSource();
        if (selectedDataSource == null) return null;
        return selectedDataSource instanceof Snapshot ? (Snapshot)selectedDataSource : null;
    }
    
    
    private SaveSnapshotAsAction() {
        putValue(Action.NAME, "Save As...");
        putValue(Action.SHORT_DESCRIPTION, "Save Snapshot As");
        putValue(Action.SMALL_ICON, new ImageIcon(ICON_16));
        putValue("iconBase", new ImageIcon(ICON_24));
        
        updateEnabled();
        ExplorerSupport.sharedInstance().addSelectionListener(new ExplorerSelectionListener() {
            public void selectionChanged(DataSource selected) {
                updateEnabled();
            }
        });
    }
}
