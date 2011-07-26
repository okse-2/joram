/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): Djamel-Eddine Boumchedda
 * 
 */

package jmx.remote.jms;

import java.net.ConnectException;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * the <b>Simple Admin </b> class creates and registers the connectionfactory in
 * the Jndi.
 * 
 * @author Djamel-Eddine Boumchedda
 * 
 */
public class SimpleAdmin {

  public static void main(String[] args) throws ConnectException, AdminException, Exception {
    System.out.println();
    System.out.println("Launch of Simple administration...");
    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);// on
                                                                                     // crée
                                                                                     // la
                                                                                     // connectionFactory
    AdminModule.connect(cf, "root", "root");// il faut etre administrateur pour
                                            // crée les files d'attente et les
                                            // sujets
    User.create("anonymous", "anonymous");
    // On enregistre la connextionFactory dans la JNDI
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("ConnectionFactory", cf);
    jndiCtx.close();
    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
