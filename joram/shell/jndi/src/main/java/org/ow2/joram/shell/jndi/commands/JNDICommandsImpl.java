/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - ScalAgent Distributed Technologies
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
package org.ow2.joram.shell.jndi.commands;

import javax.naming.NamingException;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import fr.dyade.aaa.jndi2.impl.NamingContextMBean;

public class JNDICommandsImpl implements JNDICommands {
  
  private static final int TIMEOUT = 1000;

  public static final String NAMESPACE = "joram:jndi";
  public static final String[] COMMANDS = new String[] {
    "getNamingContext", "getStrOwnerId",
    "setStrOwnerId", "createSubcontext",
    "destroySubcontext", "lookup",
    "unbind"};
  
  private BundleContext bundleContext;
  private ServiceTracker tracker;

  public JNDICommandsImpl(BundleContext bundleContext) {
    this.bundleContext = bundleContext;
    this.tracker = new ServiceTracker(this.bundleContext,
                                      NamingContextMBean.class.getCanonicalName(),
                                      null);
    this.tracker.open();
  }

  public void getNamingContext() {
    try {
      NamingContextMBean nc = (NamingContextMBean) tracker.waitForService(TIMEOUT);
      if(nc!=null) {
        String[] names = nc.getNamingContext();
        if(names != null && names.length > 0) {
          System.out.println("Naming context:");
          for(String n : names) {
//            String detail;
//            try {
//              String s = nc.lookup(n);
//              Pattern p = Pattern.compile("Reference Class Name: (\p{Alnum}{2}");
//              detail = (String) o;
//            } catch (Exception e) {
//              detail="<error>";
//            }
//            System.out.println("\t"+n+" : "+detail);
          System.out.println("\t"+n);
     }
        } else {
          System.out.println("No name in the naming context.");
        }
      } else {
        System.err.println("Error: No NamingContextMBean service found.");
      }
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
    }
  }

  public void getStrOwnerId() {
    try {
      NamingContextMBean nc = (NamingContextMBean) tracker.waitForService(TIMEOUT);
      if(nc!=null) {
        System.out.println(nc.getStrOwnerId());
      } else {
        System.err.println("Error: No NamingContextMBean service found.");
      }
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
    }
  }

  public void setStrOwnerId(String strOwnerId) {
    try {
      NamingContextMBean nc = (NamingContextMBean) tracker.waitForService(TIMEOUT);
      if(nc!=null) {
        nc.setStrOwnerId(strOwnerId);
      } else {
        System.err.println("Error: No NamingContextMBean service found.");
      }
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
    }
  }

  public void createSubcontext(String ctxName) {
    try {
      NamingContextMBean nc = (NamingContextMBean) tracker.waitForService(TIMEOUT);
      if(nc!=null) {
        nc.createSubcontext(ctxName);
      } else {
        System.err.println("Error: No NamingContextMBean service found.");
      }
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
    } catch (NamingException e) {
      System.err.println("Error: Invalid subcontext name.");
    }
  }

  public void destroySubcontext() {
    try {
      NamingContextMBean nc = (NamingContextMBean) tracker.waitForService(TIMEOUT);
      if(nc!=null) {
        nc.destroySubcontext();
      } else {
        System.err.println("Error: No NamingContextMBean service found.");
      }
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
    } catch (NamingException e) {
      System.err.println("Error: Invalid subcontext name.");
    }
  }

  public void lookup(String name) {
    try {
      NamingContextMBean nc = (NamingContextMBean) tracker.waitForService(TIMEOUT);
      if(nc!=null) {
        System.out.println(nc.lookup(name));
      } else {
        System.err.println("Error: No NamingContextMBean service found.");
      }
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
    } catch (NamingException e) {
      System.err.println("Error: Invalid record name.");
    }
  }

  public void unbind(String name) {
    try {
      NamingContextMBean nc = (NamingContextMBean) tracker.waitForService(TIMEOUT);
      if(nc!=null) {
        nc.unbind(name);
      } else {
        System.err.println("Error: No NamingContextMBean service found.");
      }
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
    } catch (NamingException e) {
      System.err.println("Error: Invalid record name.");
    }
  }
}
