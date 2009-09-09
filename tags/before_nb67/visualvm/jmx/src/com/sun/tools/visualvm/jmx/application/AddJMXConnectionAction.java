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

package com.sun.tools.visualvm.jmx.application;

import com.sun.tools.visualvm.core.ui.actions.SingleDataSourceAction;
import com.sun.tools.visualvm.host.Host;
import com.sun.tools.visualvm.jmx.JmxApplicationsSupport;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

    
/**
 *
 * @author Jiri Sedlacek
 */
class AddJMXConnectionAction extends SingleDataSourceAction<Host> {
    
    private static final String ICON_PATH = "com/sun/tools/visualvm/jmx/application/resources/addJmxApplication.png";   // NOI18N
    private static final Image ICON =  ImageUtilities.loadImage(ICON_PATH);
    
    private boolean tracksSelection = false;
    
    private static AddJMXConnectionAction alwaysEnabled;
    private static AddJMXConnectionAction selectionAware;
    
    
    public static synchronized AddJMXConnectionAction alwaysEnabled() {
        if (alwaysEnabled == null) {
            alwaysEnabled = new AddJMXConnectionAction();
            alwaysEnabled.putValue(SMALL_ICON, new ImageIcon(ICON));
            alwaysEnabled.putValue("iconBase", ICON_PATH);  // NOI18N
        }
        return alwaysEnabled;
    }
    
    public static synchronized AddJMXConnectionAction selectionAware() {
        if (selectionAware == null) {
            selectionAware = new AddJMXConnectionAction();
            selectionAware.tracksSelection = true;
        }
        return selectionAware;
    }
    
    
    protected void actionPerformed(Host host, ActionEvent actionEvent) {
        final JmxApplicationConfigurator appConfig =
                JmxApplicationConfigurator.addJmxConnection();
        if (appConfig != null) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    JmxApplicationsSupport.getInstance().createJmxApplicationInteractive(
                            appConfig.getConnection(), appConfig.getDisplayName(),
                            appConfig.getUsername(), appConfig.getPassword(),
                            appConfig.getSaveCredentialsFlag(), true);
                }
            });
        }
    }
    
    protected boolean isEnabled(Host host) {
        return host != Host.UNKNOWN_HOST;
    }
    
    protected void updateState(Set<Host> selectedHosts) {
        if (tracksSelection) super.updateState(selectedHosts);
    }
    
    
    private AddJMXConnectionAction() {
        super(Host.class);
        putValue(NAME, NbBundle.getMessage(AddJMXConnectionAction.class, "MSG_Add_JMX_Connection"));    // NOI18N
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(AddJMXConnectionAction.class, "ToolTip_Add_JMX_Connection"));   // NOI18N
    }
}