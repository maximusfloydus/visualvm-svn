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

package com.sun.tools.visualvm.tools.attach;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.model.ModelFactory;

/**
 * The AttachModelFactory class is a factory class for getting the
 * {@link AttachModel} representation  for the {@link Application}.
 * 
 * @author Tomas Hurka
 */
public final class AttachModelFactory extends ModelFactory<AttachModel, Application> {

    private static AttachModelFactory attachFactory;

    private AttachModelFactory() {
    }

    /**
     * Getter for the default version of the AttachModelFactory.
     * @return instance of {@link AttachModelFactory}.
     */
    public static synchronized AttachModelFactory getDefault() {
        if (attachFactory == null) {
            attachFactory = new AttachModelFactory();
        }
        return attachFactory;
    }
    
    /**
     * Factory method for obtaining {@link AttachModel} for {@link Application}. Note that there
     * is only one instance of {@link AttachModel} for a concrete application. This {@link AttachModel}
     * instance is cached. This method can return <CODE>null</CODE> if there is no AttachModel
     * available
     * @param app application
     * @return {@link AttachModel} instance or <CODE>null</CODE> if there is no
     * {@link AttachModel}
     */
    public static AttachModel getAttachFor(Application app) {
        return getDefault().getModel(app);
    }
    
}
