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

package com.sun.tools.visualvm.core.datasource;

import com.sun.tools.visualvm.core.datasupport.DataChangeEvent;
import com.sun.tools.visualvm.core.datasupport.DataChangeListener;
import java.util.Set;

/**
 * Central repository of all known DataSources.
 * Each DataSourceProvider which wants to publish created DataSources
 * should register into DataSourceRepository. This allows other (depending) providers
 * to discover new DataSource and process it. For example Host provider registers
 * new Host instances into DataSourceRepository, the instances are the discovered
 * by Application provider which tries to detect all applications running on the Host.
 *
 * @author Jiri Sedlacek
 */
public final class DataSourceRepository extends DataSourceProvider implements DataChangeListener<DataSource> {

    private static DataSourceRepository sharedInstance;


    /**
     * Returns singleton instance of DataSourceRepository.
     * 
     * @return singleton instance of DataSourceRepository.
     */
    public synchronized static DataSourceRepository sharedInstance() {
        if (sharedInstance == null) sharedInstance = new DataSourceRepository();
        return sharedInstance;
    }

    
    public void dataChanged(DataChangeEvent<DataSource> event) {
        changeDataSources(event.getAdded(), event.getRemoved());
    }
    
    
    protected void registerDataSourcesImpl(Set<? extends DataSource> added) {
        System.err.println(">>> Registered " + added);
        super.registerDataSourcesImpl(added);
        for (DataSource dataSource : added) dataSource.getRepository().addDataChangeListener(this, DataSource.class);
    }
    
    protected void unregisterDataSourcesImpl(Set<? extends DataSource> removed) {
        System.err.println(">>> Unregistered " + removed);
        super.unregisterDataSourcesImpl(removed);
        for (DataSource dataSource : removed) dataSource.getRepository().removeDataChangeListener(this);
    }
    
    
    private DataSourceRepository() {
        registerDataSource(DataSource.ROOT);
    }

}
