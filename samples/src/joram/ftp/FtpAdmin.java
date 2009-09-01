/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2008 ScalAgent Distributed Technologies
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
package ftp;

import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Administers an agent server for the ftp samples.
 */
public class FtpAdmin {
  
  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("ftp administration...");

    Properties prop = null;
    if (args.length >= 2) {
      prop = new Properties();
      prop.setProperty("user", args[0]);
      prop.setProperty("pass", args[1]);
      if (args.length == 3) {
        prop.setProperty("path", args[2]);
      }
    }

    if (args.length > 3) {
      System.out.println("Bad param : FtpAdmin");
      System.out.println("or        : FtpAdmin <user> <pass>");
      System.out.println("or        : FtpAdmin <user> <pass> <path>");
    }
    
    AdminModule.connect("root", "root", 60);

    Queue queue = Queue.create(0, "ftpQueue", "com.scalagent.joram.mom.dest.ftp.FtpQueue", prop);
    
    javax.jms.ConnectionFactory cf =
      TcpConnectionFactory.create("localhost", 16010);

    User.create("anonymous", "anonymous", 0);

    queue.setFreeReading();
    queue.setFreeWriting();

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("ftpQueue", queue);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
