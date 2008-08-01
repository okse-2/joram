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

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;


public class Subscriber {

  public static void main(String[] args) 
    throws Exception {
    System.out.println();
    System.out.println("Subscribes and listens to topic...");

    Properties props = new Properties();
    props.put("java.naming.factory.initial","fr.dyade.aaa.jndi2.haclient.HANamingContextFactory");
    props.put("java.naming.provider.url","hascn://localhost:16400,localhost:16410");
    
    javax.naming.Context jndiCtx = new javax.naming.InitialContext(props);
    javax.jms.TopicConnectionFactory tcf = (javax.jms.TopicConnectionFactory) jndiCtx.lookup("tcf");
    Topic topic = (Topic) jndiCtx.lookup("topic");
    jndiCtx.close();
    
    Connection cnx = tcf.createConnection("anonymous", "anonymous");
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer sub = sess.createConsumer(topic);

    sub.setMessageListener(new Listener());

    cnx.start();

    System.in.read();

    System.out.println();
    System.out.println("Subscriber closed.");
  }
}
