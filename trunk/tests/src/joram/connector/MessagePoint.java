/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): BADOLLE Fabien ( ScalAgent Distributed Technologies )
 * Contributor(s):
 */
package joram.connector;

import java.lang.reflect.Method;

import javax.jms.Message;
import javax.jms.TextMessage;
import javax.resource.spi.endpoint.MessageEndpoint;

public class MessagePoint implements MessageEndpoint,javax.jms.MessageListener {

  public  void afterDelivery(){}

  public  void beforeDelivery(Method method){}

  public  void release() {}
  
  public void onMessage(Message m){
    try{
      String text = ((TextMessage)m).getText();
      int idx = m.getIntProperty("idx");
      ConnectorTest1.countMessages(text, idx);
    }catch(Exception exc){
      System.out.println("error");
      ConnectorTest1.error(exc);
    }
  }
}
