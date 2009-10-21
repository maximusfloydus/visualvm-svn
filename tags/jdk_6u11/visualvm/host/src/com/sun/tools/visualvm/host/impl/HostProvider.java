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

package com.sun.tools.visualvm.host.impl;

import com.sun.tools.visualvm.host.HostsSupport;
import com.sun.tools.visualvm.host.RemoteHostsContainer;
import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.DataSourceRepository;
import com.sun.tools.visualvm.host.Host;
import com.sun.tools.visualvm.core.datasupport.DataChangeEvent;
import com.sun.tools.visualvm.core.datasupport.DataChangeListener;
import com.sun.tools.visualvm.core.datasource.Storage;
import com.sun.tools.visualvm.core.datasupport.Utils;
import com.sun.tools.visualvm.core.explorer.ExplorerSupport;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import java.awt.BorderLayout;
import java.io.File;
import java.io.FilenameFilter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jiri Sedlacek
 */
// A provider for Hosts
public class HostProvider {
    private static final Logger LOGGER = Logger.getLogger(HostProvider.class.getName());
    
    private static final String SNAPSHOT_VERSION = "snapshot_version";  // NOI18N
    private static final String SNAPSHOT_VERSION_DIVIDER = ".";
    private static final String CURRENT_SNAPSHOT_VERSION_MAJOR = "1";   // NOI18N
    private static final String CURRENT_SNAPSHOT_VERSION_MINOR = "0";   // NOI18N
    private static final String CURRENT_SNAPSHOT_VERSION = CURRENT_SNAPSHOT_VERSION_MAJOR + SNAPSHOT_VERSION_DIVIDER + CURRENT_SNAPSHOT_VERSION_MINOR;
    
    private static final String PROPERTY_HOSTNAME = "prop_hostname";    // NOI18N
    
    private static InetAddress localhostAddress2;
    
    private boolean initializingHosts = true;
    private Semaphore initializingHostsSemaphore = new Semaphore(1);


    public Host createHost(final HostProperties hostDescriptor, final boolean interactive) {
        waitForInitialization();
        
        final String hostName = hostDescriptor.getHostName();
        InetAddress inetAddress = null;
        ProgressHandle pHandle = null;

        try {
            pHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(HostProvider.class, "LBL_Searching_for_host") + hostName); // NOI18N
            pHandle.setInitialDelay(0);
            pHandle.start();
            try {
                inetAddress = InetAddress.getByName(hostName);
            } catch (UnknownHostException e) {
                if (interactive) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            String error = NbBundle.getMessage(HostProvider.class, "MSG_Wrong_Host", hostName); // NOI18N
                            NetBeansProfiler.getDefaultNB().displayError(error);
                        }
                    });
                }
            }
        } finally {
            final ProgressHandle pHandleF = pHandle;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() { if (pHandleF != null) pHandleF.finish(); }
            });
        }

        if (inetAddress != null) {
            final Host knownHost = getHostByAddress(inetAddress);
            if (knownHost != null) {
                if (interactive) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ExplorerSupport.sharedInstance().selectDataSource(knownHost);
                            String warning = NbBundle.getMessage(HostProvider.class, "MSG_Already_Monitored",new Object[] {hostName,DataSourceDescriptorFactory.getDescriptor(knownHost).getName()});    // NOI18N
                            NetBeansProfiler.getDefaultNB().displayWarning(warning);
                        }
                    });
                }
                return knownHost;
            } else {
                String ipString = inetAddress.getHostAddress();
                
                String[] propNames = new String[] {
                    SNAPSHOT_VERSION,
                    PROPERTY_HOSTNAME,
                    DataSourceDescriptor.PROPERTY_NAME };
                String[] propValues = new String[] {
                    CURRENT_SNAPSHOT_VERSION,
                    hostName,
                    hostDescriptor.getDisplayName() };
                
                File customPropertiesStorage = Utils.getUniqueFile(HostsSupport.getStorageDirectory(), ipString, Storage.DEFAULT_PROPERTIES_EXT);
                Storage storage = new Storage(customPropertiesStorage.getParentFile(), customPropertiesStorage.getName());
                storage.setCustomProperties(propNames, propValues);
                
                HostImpl newHost = null;
                try {
                    newHost = new HostImpl(hostName, storage);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error creating host", e); // Should never happen  // NOI18N
                }
                
                if (newHost != null) {
                    RemoteHostsContainer.sharedInstance().getRepository().addDataSource(newHost);
                }
                return newHost;
            }
        }
        return null;
    }
    
    void removeHost(HostImpl host, boolean interactive) {
        waitForInitialization();
        
        // TODO: if interactive, show a Do-Not-Show-Again confirmation dialog
        DataSource owner = host.getOwner();
        if (owner != null) owner.getRepository().removeDataSource(host);
    }
    
    public Host getHostByAddress(InetAddress inetAddress) {
        waitForInitialization();
        
        Set<HostImpl> knownHosts = DataSourceRepository.sharedInstance().getDataSources(HostImpl.class);
        for (HostImpl knownHost : knownHosts)
            if (knownHost.getInetAddress().equals(inetAddress)) return knownHost;
        
        if (inetAddress.equals(Host.LOCALHOST.getInetAddress())) return Host.LOCALHOST;
        if (inetAddress.equals(localhostAddress2)) return Host.LOCALHOST;
        if (inetAddress.isLoopbackAddress()) return Host.LOCALHOST;
        
        return null;
    }
    
    public Host createLocalHost() {
        try {
            return new Host("localhost") {};    // NOI18N
        } catch (UnknownHostException e) {
            LOGGER.severe("Critical failure: cannot resolve localhost");    // NOI18N
            return null;
        }
    }
    
    public Host createUnknownHost() {
        try {
            return new Host("unknown", InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 })) {}; // NOI18N
        } catch (UnknownHostException e) {
            LOGGER.severe("Failure: cannot resolve <unknown> host");    // NOI18N
            return null;
        }
    }
    
    private void initLocalHost() {
        try {
            localhostAddress2 = InetAddress.getLocalHost();
        } catch (java.net.UnknownHostException e) {}
        Host localhost = Host.LOCALHOST;
        if (localhost != null) DataSource.ROOT.getRepository().addDataSource(localhost);
    }
    
    private void initUnknownHost() {
        final Host unknownhost = Host.UNKNOWN_HOST;
        if (unknownhost != null) {
            unknownhost.getRepository().addDataChangeListener(new DataChangeListener() {
                public void dataChanged(DataChangeEvent event) {
                    unknownhost.setVisible(!event.getCurrent().isEmpty());
                }
            }, DataSource.class);
            RemoteHostsContainer.sharedInstance().getRepository().addDataSource(unknownhost);
        }
    }
    
    private void initPersistedHosts() {
        if (HostsSupport.storageDirectoryExists()) {
            File[] files = HostsSupport.getStorageDirectory().listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(Storage.DEFAULT_PROPERTIES_EXT);
                }
            });

            Set<File> unresolvedHostsF = new HashSet();
            Set<String> unresolvedHostsS = new HashSet();
            
            Set<HostImpl> hosts = new HashSet();
            for (File file : files) {
                Storage storage = new Storage(file.getParentFile(), file.getName());
                String hostName = storage.getCustomProperty(PROPERTY_HOSTNAME);

                HostImpl persistedHost = null;
                try {
                    persistedHost = new HostImpl(hostName, storage);
                } catch (Exception e) {
                    LOGGER.throwing(HostProvider.class.getName(), "initPersistedHosts", e);    // NOI18N
                    unresolvedHostsF.add(file);
                    unresolvedHostsS.add(hostName);
                }

                if (persistedHost != null) hosts.add(persistedHost);
            }
            
            if (!unresolvedHostsF.isEmpty()) notifyUnresolvedHosts(unresolvedHostsF, unresolvedHostsS);
            
            RemoteHostsContainer.sharedInstance().getRepository().addDataSources(hosts);     
        }
        
        DataSource.EVENT_QUEUE.post(new Runnable() {
            public void run() {
                initializingHostsSemaphore.release();
                initializingHosts = false;
            }
        });
    }
    
    private static void notifyUnresolvedHosts(final Set<File> unresolvedHostsF, final Set<String> unresolvedHostsS) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
                messagePanel.add(new JLabel(NbBundle.getMessage(HostProvider.class, "MSG_Unresolved_Hosts")), BorderLayout.NORTH); // NOI18N
                JList list = new JList(unresolvedHostsS.toArray());
                list.setVisibleRowCount(4);
                messagePanel.add(new JScrollPane(list), BorderLayout.CENTER);
                NotifyDescriptor dd = new NotifyDescriptor(
                        messagePanel, NbBundle.getMessage(HostProvider.class, "Title_Unresolved_Hosts"), // NOI18N
                        NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.ERROR_MESSAGE,
                        null, NotifyDescriptor.YES_OPTION);
                if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.NO_OPTION)
                    for (File file : unresolvedHostsF) Utils.delete(file, true);
                
                unresolvedHostsF.clear();
                unresolvedHostsS.clear();
            }
        }, 1000);
    }
    
    
    private void waitForInitialization() {
        if (initializingHosts) try {
            initializingHostsSemaphore.acquire();
        } catch (InterruptedException ex) {
            LOGGER.throwing(HostProvider.class.getName(), "waitForInitialization", ex); // NOI18N
        }
    }
    
    
    public void initialize() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            public void run() {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        initLocalHost();
                        initUnknownHost();
                        initPersistedHosts();
                    }
                });
            }
        });
    }
    
    public HostProvider() {
        try {
            initializingHostsSemaphore.acquire();
        } catch (InterruptedException ex) {
            LOGGER.throwing(HostProvider.class.getName(), "<init>", ex);    // NOI18N
        }
    }

}