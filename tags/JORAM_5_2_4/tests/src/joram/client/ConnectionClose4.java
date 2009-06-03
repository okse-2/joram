/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
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
package joram.client;

import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import framework.TestCase;



/**
 * @author feliot
 *
 *
 */
public class ConnectionClose4 extends TestCase {
  
  public static void main(String[] args) {
    new ConnectionClose4().run();
  }
  
  private Connection connection;

  private Destination dest;
  
  public void run() {
    try {
      startAgentServer(
        (short)0, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});
      
      Thread.sleep(4000);
      
      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);
      
      connection = cf.createConnection(
          "root", "root");
      
      connection.start();
      
      connection.setExceptionListener(new ExceptionListener() {
        public void onException(JMSException exception) {
          try {
            System.out.println("Close connection");
            connection.close();
          } catch (Exception exc) {
            exc.printStackTrace();
          }
        }
      });
      
      stopAgentServer((short)0);
      
      Thread.sleep(2000);
      
      // Must not hang
      connection.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      endTest();     
    }
  }

}
