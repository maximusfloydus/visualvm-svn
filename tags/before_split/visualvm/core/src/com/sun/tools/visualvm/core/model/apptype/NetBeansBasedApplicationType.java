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

package com.sun.tools.visualvm.core.model.apptype;

import com.sun.tools.visualvm.core.model.jvm.JVM;
import com.sun.tools.visualvm.core.datasource.Application;
import java.awt.Image;
import java.util.Set;
import org.openide.util.Utilities;


/**
 *
 * @author Tomas Hurka
 */
public class NetBeansBasedApplicationType extends ApplicationType {
  Application application;
  String name;
  String branding;
  Set<String> clusters;

  NetBeansBasedApplicationType(Application app,JVM jvm,Set<String> cls, String br) {
    application = app;
    clusters = cls;
    branding = br;
  }

  public Set<String> getClusters() {
    return clusters;
  }

  public String getName() {
    return "NetBeans Platform application";
  }

  public String getVersion() {
    return "<Unknown>";
  }

  public String getDescription() {
    return "";
  }

  public Image getIcon() {
    String iconPath = "com/sun/tools/visualvm/core/ui/resources/application.png";
    return Utilities.loadImage(iconPath, true);
  }
}