/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is JC Waeny.
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
    
    private TopicConnectionFactory conFactory;
    private TopicSession           pubSession;
    private TopicSession           subSession;
    private TopicPublisher         publisher;
    private TopicSubscriber        subscriber;
    private TopicConnection        connection;
    private String                 userName;
    
    /* Constructor. Establish JMS publisher and subscriber */
    public SimpleChat(String topicName, String username) throws Exception {
        
        InitialContext jndi = null;
        
        try {
            jndi = getHome.context(); //new InitialContext();
        } catch( Exception e) {
            System.out.println( e.toString() );
            System.exit(2);
        }
        
        // Look up a JMS connection factory
        conFactory = (TopicConnectionFactory)jndi.lookup("factoryChat");
        
        // Create a JMS connection
        connection = conFactory.createTopicConnection();
        
        // Create two JMS session objects
        pubSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        subSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        
        // Look up a JMS topic
        Topic chatTopic = (Topic)jndi.lookup(topicName);
        
        // Create a JMS publisher and subscriber
        publisher = pubSession.createPublisher(chatTopic);
        subscriber = subSession.createSubscriber(chatTopic);
        
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
        publisher.publish(message);
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
