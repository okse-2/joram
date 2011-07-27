/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): Feliot David  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */

package joram.tcp;


import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import framework.TestCase;



/**
 * Check that the security exception is raised
 * when creating a connection with a bad password.
 * 
 * @author feliot
 *
 */
public class Test5 extends TestCase {

  public Test5() {
    super();
  }
  
  public void run() {
    try {
      startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});
      Thread.sleep(2500);
      
      ConnectionFactory qcf =
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
            "localhost", 2560);
      
      JMSException se = null;
      try {
        qcf.createConnection("root", "toto");
      } catch (JMSSecurityException exc) {
        se = exc;
      }
      assertTrue("JMSSecurityException not raised", se != null);
      
    } catch (Exception exc) {
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }

  public static void main(String args[]) {
    new Test5().run();
  }
}
