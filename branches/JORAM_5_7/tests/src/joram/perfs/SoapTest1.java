/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Freyssinet Andre (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.perfs;

import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.soap.SoapConnectionFactory;

/**
 *
 */
public class SoapTest1 extends Test1 {

  public static void main(String[] args) throws Exception {
    new SoapTest1().run(args);
  }

  protected void AdminConnect() throws Exception {
    AdminModule.connect("root", "root", 60);
  }

  protected ConnectionFactory createConnectionFactory() throws Exception {
    return SoapConnectionFactory.create("localhost", 8080, 30);
//     return TcpConnectionFactory.create("localhost", 16010);
  }
}
