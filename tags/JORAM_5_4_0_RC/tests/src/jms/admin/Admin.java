/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2002 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2002 INRIA
 * Contact: joram-team@objectweb.org
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
 * Initial developer(s): Jeff Mesnil (Inria)
 * Contributor(s): Nicolas Tachker (ScalAgent D.T.)
 */

package jms.admin;

import java.net.ConnectException;

import javax.jms.JMSException;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;

/**
 * Simple Administration interface.
 * <br />
 * JMS Provider has to implement this 
 * simple interface to be able to use the test suite.
 */
public interface Admin {
    
    /**
     * Returns the name of the JMS Provider.
     *
     * @return name of the JMS Provider
     */
    public String getName();
    
    /** 
     * Creates a <code>ConnectionFactory</code>.
     *
     * @since JMS 1.1
     * @param name of the <code>ConnectionFactory</code>
     */
    public ConnectionFactory createConnectionFactory(String name) throws ConnectException;

    /** 
     * Creates a <code>QueueConnectionFactory</code>.
     *
     * @param name of the <code>QueueConnectionFactory</code>
     */
    public QueueConnectionFactory createQueueConnectionFactory(String name) throws ConnectException;

    /** 
     * Creates a <code>TopicConnectionFactory</code>.
     *
     * @param name of the <code>TopicConnectionFactory</code>
     */
    public TopicConnectionFactory createTopicConnectionFactory(String name) throws ConnectException;
 
    /** 
     * Creates a <code>Queue</code>.
     *
     * @param name of the <code>Queue</code>
     */
    public Queue createQueue(String name) throws ConnectException, AdminException;

    /** 
     * Creates a <code>Topic</code>.
     *
     * @param name of the <code>Topic</code>
     */
    public Topic createTopic(String name) throws ConnectException, AdminException;
    
    /** 
     * Removes the <code>Queue</code>
     *
     * @param queue
     */
    public void deleteQueue(Destination queue) throws ConnectException, AdminException, JMSException;
    
    /** 
     * Removes the <code>Topic</code>
     *
     * @param topic
     */
    public void deleteTopic(Destination topic) throws ConnectException, AdminException, JMSException;

    /** 
     * Removes the <code>ConnectionFactory</code> of name <code>name</code> from JNDI and deletes it
     *
     * @since JMS 1.1
     * @param name JNDI name of the <code>ConnectionFactory</code>
     */
    public void deleteConnectionFactory (String name);

    /** 
     * Removes the <code>QueueConnectionFactory</code> of name <code>name</code> from JNDI and deletes it
     *
     * @param name JNDI name of the <code>QueueConnectionFactory</code>
     */
    public void deleteQueueConnectionFactory (String name);

    /** 
     * Removes the <code>TopicConnectionFactory</code> of name <code>name</code> from JNDI and deletes it
     *
     * @param name JNDI name of the <code>TopicConnectionFactory</code>
     */    
    public void deleteTopicConnectionFactory (String name);
    
    public void disconnect();
}
