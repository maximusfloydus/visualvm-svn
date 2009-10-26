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

package com.sun.tools.visualvm.jmx;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.datasource.Storage;
import com.sun.tools.visualvm.core.datasupport.Utils;
import java.util.HashMap;
import java.util.Map;
import javax.management.remote.JMXConnector;

/**
 * EnvironmentProvider adding the JMXConnector.CREDENTIALS property to the JMX
 * environment map.
 *
 * There are two subclasses of EnvironmentProvider available, typically you want
 * to use the EnvironmentProvider.Custom class to provide a custom credentials
 * for a JMX connection. The EnvironmentProvider.Persistent class is used for
 * handling credentials for persisted connections.
 *
 * Note that if the credentials provided by this provider are incorrect a dialog
 * requesting new credentials will be displayed by the framework. If the
 * user-provided credentials are correct they will override the credentials
 * defined by this provider. The user-provided credentials are never persisted.
 *
 * @since VisualVM 1.2
 * @author Jiri Sedlacek
 */
public abstract class CredentialsProvider extends EnvironmentProvider {

    private static final String PROPERTY_USERNAME = "prop_credentials_username"; // NOI18N
    private static final String PROPERTY_PASSWORD = "prop_credentials_password"; // NOI18N

    private static Persistent PERSISTENT_PROVIDER;


    static synchronized Persistent persistent() {
        if (PERSISTENT_PROVIDER == null) PERSISTENT_PROVIDER = new Persistent();
        return PERSISTENT_PROVIDER;
    }


    /**
     * Returns an unique String identifying the CredentialsProvider. Must be
     * overriden to return a different identificator when subclassing the
     * CredentialsProvider.
     *
     * @return unique String identifying the CredentialsProvider
     */
    public String getId() {
        return CredentialsProvider.class.getName();
    }
    
    
    abstract String getUsername(Storage storage);
    
    abstract boolean hasPassword(Storage storage);

    abstract boolean isPersistent(Storage storage);


    /**
     * CredentialsProvider to provide custom settings.
     *
     * @since VisualVM 1.2
     * @author Jiri Sedlacek
     */
    public static class Custom extends CredentialsProvider {

        private final String username;
        private final char[] password;
        private final boolean persistent;


        /**
         * Creates new instance of CredentialsProvider.Custom.
         *
         * @param username username
         * @param password password
         * @param persistent true if the credentials should be persisted for another VisualVM sessions, false otherwise
         */
        public Custom(String username, char[] password, boolean persistent) {
            this.username = username;
            this.password = encodePassword(password);
            this.persistent = persistent;
        }


        public Map<String, ?> getEnvironment(Application application, Storage storage) {
            return createMap(username, password != null ? new String(password) : null);
        }

        public String getEnvironmentId(Storage storage) {
            if (username != null) return username;
            return super.getEnvironmentId(storage);
        }

        public void saveEnvironment(Storage storage) {
            if (!persistent) return;
            storage.setCustomProperty(PROPERTY_USERNAME, username);
            storage.setCustomProperty(PROPERTY_PASSWORD, new String(password));
        }
        
        
        String getUsername(Storage storage) { return username; }
    
        boolean hasPassword(Storage storage) { return password != null &&
                                               password.length > 0; }

        boolean isPersistent(Storage storage) { return persistent; }

    }


    /**
     * CredentialsProvider to provide custom settings.
     *
     * @since VisualVM 1.2
     * @author Jiri Sedlacek
     */
    public static class Persistent extends CredentialsProvider {

        public Map<String, ?> getEnvironment(Application application, Storage storage) {
            String username = storage.getCustomProperty(PROPERTY_USERNAME);
            String password = storage.getCustomProperty(PROPERTY_PASSWORD);
            return createMap(username, password);
        }

        public String getEnvironmentId(Storage storage) {
            if (storage != null) {
                String username = storage.getCustomProperty(PROPERTY_USERNAME);
                if (username != null) return username;
            }
            return super.getEnvironmentId(storage);
        }


        String getUsername(Storage storage) { return storage.getCustomProperty(
                                                     PROPERTY_USERNAME); }

        boolean hasPassword(Storage storage) {
            String password = storage.getCustomProperty(PROPERTY_PASSWORD);
            return password != null && password.length() > 0;
        }

        boolean isPersistent(Storage storage) {
            return getUsername(storage) != null || hasPassword(storage);
        }

    }


    // --- Private implementation ----------------------------------------------

    private static Map<String, ?> createMap(String username, String password) {
        Map map = new HashMap();

        if (username != null || password != null)
            map.put(JMXConnector.CREDENTIALS,
                    new String[] { username, decodePassword(password) });

        return map;
    }

    private static char[] encodePassword(char[] password) {
        if (password == null) return null;
        return Utils.encodePassword(new String(password)).toCharArray();
    }

    private static String decodePassword(String password) {
        if (password == null) return null;
        return Utils.decodePassword(password);
    }

}