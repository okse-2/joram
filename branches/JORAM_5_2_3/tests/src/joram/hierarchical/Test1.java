/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):Badolle Fabien (ScalAgent D.T.)
 * Contributor(s): 
 */
package joram.hierarchical;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;


import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;


/**
 * Test :               news
 *                     /    \
 *              buisiness  sports
 *                           \
 *                           tennis
 *    
 */
public class Test1 extends TestCase {

   
    public static void main(String[] args) {
	new Test1().run();
    }
          
    public void run() {
	try {
	    System.out.println("server start");
	    startAgentServer((short)0);
	   
	   
	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    Topic news = (Topic) ictx.lookup("news");
	    Topic business = (Topic) ictx.lookup("business");
	    Topic sports = (Topic) ictx.lookup("sports");
	    Topic tennis = (Topic) ictx.lookup("tennis");
	    	
	    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
	    ictx.close();
	    
	    Connection cnx = cf.createConnection();
	    Session sessionp = cnx.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
	    Session sessionc = cnx.createSession(false,
						Session.AUTO_ACKNOWLEDGE);


	    MessageConsumer newsConsumer =sessionc.createConsumer(news);
	    MessageConsumer businessConsumer = sessionc.createConsumer(business);
	    MessageConsumer sportsConsumer = sessionc.createConsumer(sports);
	    MessageConsumer tennisConsumer = sessionc.createConsumer(tennis);
	    MessageProducer producer = sessionp.createProducer(null);
	    cnx.start();

	 
	    // send a news : possible receive :News
	    TextMessage msg = sessionp.createTextMessage();
	    msg.setText("News!");
	    producer.send(news, msg);
	  
	    
	    msg=(TextMessage)tennisConsumer.receive(2000);
	    assertEquals(null,msg);
	    
	    msg=(TextMessage)sportsConsumer.receive(2000);
	    assertEquals(null,msg);
	    
	    msg=(TextMessage) businessConsumer.receive(2000);
	    assertEquals(null,msg);
	    
	    msg=(TextMessage)newsConsumer.receive(2000);
	    assertEquals("News!",msg.getText());
	  

	    // send a Business : possible receive :News & Business
	    msg = sessionp.createTextMessage();
	    msg.setText("Business!");
	    producer.send(business, msg);
	    
	    msg=(TextMessage)tennisConsumer.receive(2000);
	    assertEquals(null,msg);
	    
	    msg=(TextMessage)sportsConsumer.receive(2000);
	    assertEquals(null,msg);
	    
	    msg=(TextMessage)newsConsumer.receive(2000);
	    assertEquals("Business!",msg.getText());

	    msg = sessionp.createTextMessage();
	    msg.setText("Business!");
	    producer.send(business, msg);

	    msg=(TextMessage)businessConsumer.receive(2000);
	    assertEquals("Business!",msg.getText());





	    // send a Sports : possible receive :News & Sports
	    msg = sessionp.createTextMessage();
	    msg.setText("Sports!");
	    producer.send(sports, msg);

	    msg=(TextMessage)tennisConsumer.receive(2000);
	    assertEquals(null,msg);

	    msg=(TextMessage)businessConsumer.receive(2000);
	    assertEquals(null,msg);

	    msg=(TextMessage)newsConsumer.receive(2000);
	    assertEquals("Sports!",msg.getText());

	    msg = sessionp.createTextMessage();
	    msg.setText("Sports!");
	    producer.send(sports, msg);

	    msg=(TextMessage)sportsConsumer.receive(2000);
	    assertEquals("Sports!",msg.getText());



	   // send a Tennis : possible receive :News & Sports & Tennis
	    msg = sessionp.createTextMessage();
	    msg.setText("Tennis!");
	    producer.send(tennis, msg);

	    msg=(TextMessage)businessConsumer.receive(2000);
	    assertEquals(null,msg);

	    msg=(TextMessage)newsConsumer.receive(2000);
	    assertEquals("Tennis!",msg.getText());

	    msg = sessionp.createTextMessage();
	    msg.setText("Tennis!");
	    producer.send(tennis, msg);

	    msg=(TextMessage)tennisConsumer.receive(2000);
	    assertEquals("Tennis!",msg.getText());
  
	    msg = sessionp.createTextMessage();
	    msg.setText("Tennis!");
	    producer.send(tennis, msg);

	    msg=(TextMessage)sportsConsumer.receive(2000);
	    assertEquals("Tennis!",msg.getText());


	    cnx.close();
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	} finally {
	    System.out.println("Server stop ");
	    stopAgentServer((short)0);
	    endTest(); 
	}
    }
    
  
    public void admin() throws Exception {
	// conexion 
	AdminModule.connect("root", "root", 60);

	Topic news = (Topic) Topic.create(0);
	Topic business = (Topic) Topic.create(0);
	Topic sports = (Topic) Topic.create(0);
	Topic tennis = (Topic) Topic.create(0);

	business.setParent(news);
	sports.setParent(news);
	tennis.setParent(sports);

	javax.jms.ConnectionFactory cf =
	    TcpConnectionFactory.create("localhost", 16010);

	User user = User.create("anonymous", "anonymous", 0);

	news.setFreeReading();
	news.setFreeWriting();
	business.setFreeReading();
	business.setFreeWriting();
	sports.setFreeReading();
	sports.setFreeWriting();
	tennis.setFreeReading();
	tennis.setFreeWriting();

	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("news", news);
	jndiCtx.bind("business", business);
	jndiCtx.bind("sports", sports);
	jndiCtx.bind("tennis", tennis);
	jndiCtx.bind("cf", cf);
	jndiCtx.close();

	AdminModule.disconnect();
    }
}

