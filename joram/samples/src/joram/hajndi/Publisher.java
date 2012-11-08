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
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;


public class Publisher {

  public static void main(String[] arg) throws Exception {
    System.out.println();
    System.out.println("Publishes messages on topic...");

    Properties props = new Properties();
    props.put("java.naming.factory.initial","fr.dyade.aaa.jndi2.haclient.HANamingContextFactory");
    props.put("java.naming.provider.url","hascn://localhost:16400,localhost:16410");
    
    javax.naming.Context jndiCtx = new javax.naming.InitialContext(props);
    javax.jms.TopicConnectionFactory tcf = (javax.jms.TopicConnectionFactory) jndiCtx.lookup("tcf");
    Topic topic = (Topic) jndiCtx.lookup("topic");
    jndiCtx.close();

    Connection cnx = tcf.createConnection("anonymous", "anonymous");
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer pub = sess.createProducer(topic);

    TextMessage msg = sess.createTextMessage();

    int i;
    for (i = 0; i < 5000; i++) {
      msg.setText("Msg " + i);
      pub.send(msg);
      Thread.sleep(250L);
      System.out.println("publish message " + i);
    }
  }
}
