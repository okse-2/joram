/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Jose Carlos Waeny
 * Contributor(s): ScalAgent Distributed Technologies
 */
package chat;

import javax.jms.*;
import javax.naming.*;
import java.io.*;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Launching Simple Chat:
 * connecting to JORAM server, creating chat agents, creating topic
 *
 * @author	JC Waeny 
 * @email       jc@waeny.2y.net
 * @version     1.0
 */
public class SimpleChat implements javax.jms.MessageListener{
    
    private ConnectionFactory conFactory;
    private Session           pubSession;
    private Session           subSession;
    private MessageProducer   publisher;
    private MessageConsumer   subscriber;
    private Connection        connection;
    private String            userName;
    
    /* Constructor. Establish JMS publisher and subscriber */
    public SimpleChat(String topicName, String username) throws Exception {
        
        InitialContext jndi = null;
        
        try {
            jndi = new InitialContext();
        } catch( Exception e) {
            System.out.println( e.toString() );
            System.exit(2);
        }
        
        // Look up a JMS connection factory
        conFactory = (ConnectionFactory)jndi.lookup("factoryChat");
        
        // Create a JMS connection
        connection = conFactory.createConnection();
        
        // Create two JMS session objects
        pubSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        subSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        
        // Look up a JMS topic
        Topic chatTopic = (Topic)jndi.lookup(topicName);
        
        // Create a JMS publisher and subscriber
        publisher = pubSession.createProducer(chatTopic);
        subscriber = subSession.createConsumer(chatTopic);
        
        // Set a JMS message listener
        subscriber.setMessageListener(this);
        
        // Start the JMS connection; allows messages to be delivered
        userName = username;
        connection.start( );
        
    }
    
    /* Receive message from topic subscriber */
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText( );
            System.out.println(textMessage.getStringProperty("User") + ": " + text);
        } catch (JMSException jmse) {
            jmse.printStackTrace( );
        }
    }
    
    /* Create and send message using topic publisher */
    protected void writeMessage(String text) throws JMSException {
        TextMessage message = pubSession.createTextMessage( );
        message.setText(text);
        message.setStringProperty("User", userName);
        publisher.send(message);
    }
    
    /* Close the JMS connection */
    public void close( ) throws JMSException {
        connection.close( );
    }
    
    /* Run the Chat client */
    public static void main(String [] args){
        
        SimpleChat chat = null;
        String     user = null;
        
        try {
            user = args[0];
        } catch(Exception e) {
            user = "No name";
        }
         
        try {
            
            chat = new SimpleChat("topicChat", user);
            
            // Read from command line
            BufferedReader commandLine = new
            java.io.BufferedReader(new InputStreamReader(System.in));
            
            // Loop until the word "exit" is typed
            
            System.out.println("User: " + user + " connected !");
            System.out.println("Type your phrases and press 'ENTER' ...");
            System.out.println("Type 'exit' or 'quit' to abandon the chat.");
            
            while(true) {
                String s = commandLine.readLine( );
                if ( s.equalsIgnoreCase("exit") || s.equalsIgnoreCase("quit") ) {
                    chat.close( ); // close down connection
                    System.exit(0);// exit program
                } else
                    chat.writeMessage(s);
            }
            
        } catch(Exception e) {
            System.out.println( e.toString());
        }
    }
}
