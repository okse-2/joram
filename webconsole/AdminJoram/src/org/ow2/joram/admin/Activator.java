/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.ow2.joram.admin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

  private static final String LOGIN_PROPERTY = "org.ow2.joram.web.admin.name";
  private static final String PASSWORD_PROPERTY = "org.ow2.joram.web.admin.password";

  private static BundleContext m_context;

  private static String adminName = "joram";

  private static String adminPass = "joram";

  public static BundleContext getContext() {
    return m_context;
  }

  public static boolean checkCredentials(String user, String pass) {
    return user.equals(adminName) && pass.equals(adminPass);
  }

  public void start(BundleContext context) throws Exception {
    m_context = context;
    if (context.getProperty(LOGIN_PROPERTY) != null)
      adminName = context.getProperty(LOGIN_PROPERTY);
    if (context.getProperty(PASSWORD_PROPERTY) != null)
      adminPass = context.getProperty(PASSWORD_PROPERTY);
  }

  public void stop(BundleContext context) throws Exception {
  }

}
