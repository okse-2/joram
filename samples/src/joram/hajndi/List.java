/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s): 
 */
package hajndi;

import java.util.Properties;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;


public class List {
  public static void main(String[] args) 
    throws Exception {
    System.out.println();
    System.out.println("JNDI List...");
      
    Properties props = new Properties();
    if (args.length > 0) {
      props.put("java.naming.factory.initial","fr.dyade.aaa.jndi2.haclient.HANamingContextFactory");
      props.put("java.naming.provider.url","hascn://localhost:16400,localhost:16410");
    } else {
      props.put("java.naming.factory.initial","fr.dyade.aaa.jndi2.client.NamingContextFactory");
      props.put("java.naming.provider.url","scn://localhost:16430");
    }
    System.out.println("JNDI props = " + props + "\n");
    
    javax.naming.Context jndiCtx = new javax.naming.InitialContext(props);
     NamingEnumeration e = jndiCtx.list("");
     while (e.hasMore()) {
       NameClassPair ncp = (NameClassPair) e.next();
       System.out.println("ncp = " +ncp);
       System.out.println("  obj = " + jndiCtx.lookup(ncp.getName()));
     }
     jndiCtx.close();

    System.out.println("Admin closed.");
  }
}
