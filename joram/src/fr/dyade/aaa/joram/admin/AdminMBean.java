package fr.dyade.aaa.joram.admin;

import java.util.Enumeration;
import fr.dyade.aaa.joram.*;

/**
 * This interface describes administration operations available for JORAM.
 * 
 * It's to be noted that it is implicitely required that all operations can only work:
 * <ul>
 *   <li>after one of the <code>connect</code> method has been called</li>
 *   <li>before a call to the <code>disconnect</code> method
 * </ul>
 */
public interface AdminMBean {

    /**
     * connects on the server specified by serverUrl and port..
     *
     * @param serverUrl URL of the server
     * @param port port of the server
     * @param name name of the administrator
     * @param password password of the administrator
     */
    public void connect(String serverUrl, int port, String name, String password);
    
    /**  
     * connects on the server specified by serverUrl with default port >
     *
     * @param serverUrl URL of the server
     * @param name name of the administrator
     * @param password password of the administrator
     */
    public void connect(String serverUrl, String name, String password);

    /**  
     * connects on localhost with default port.
     *
     * @param serverUrl URL of the server
     * @param name name of the administrator
     * @param password password of the administrator
     */
    public void connect(String name, String password);
    
    /**
     * disconnects from a previously connected server.
     * 
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public void disconnect() throws NotConnectedException;
    
    /**
     * creates an new administrator ID.
     *
     * @param name name of the administrator
     * @param password password of the administrator
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public void createAdminId(String name, String password) throws NotConnectedException;

    /**
     * deletes an administrator ID.
     *
     * @param name name of the administrator ID to delete
     * 
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public void deleteAdminId(String name) throws NotConnectedException;

    /**
     * creates a new queue on the server.
     *
     * @param name name of the queue
     * 
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public Queue createQueue(String name) throws NotConnectedException;

    /**
     * gets a queue from the server.
     *
     * @param name name of the queue
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public Queue getQueue(String name) throws NotConnectedException;

    /**
     * deletes a queue on the server.
     *
     * @param name name of the queue
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public void deleteQueue(String name) throws NotConnectedException;

    /**
     * creates a new topic on the server.
     *
     * @param name name of the topic
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public Topic createTopic(String name) throws NotConnectedException;

    /**
     * gets a topic from the server.
     *
     * @param name name of the topic
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public Topic getTopic(String name) throws NotConnectedException;

    /**
     * deletes a topic on the server.
     *
     * @param name name of the topic
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public void deleteTopic(String name) throws NotConnectedException;
    
    /** 
     * gets names of the queues on the server.
     *
     * @return names of all queues on the server
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public String[] getQueuesNames() throws NotConnectedException;

    /** 
     * gets names of the topics on the server.
     *
     * @return names of all topics on the server
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public String[] getTopicsNames() throws NotConnectedException;
    
    /**
     * tests if a queue with the given name exists on the server.
     *
     * @param name name of the queue
     * @return <code>true</code> if the queue exists on the server, <code>false</code> else.
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public boolean queueExists(String name) throws NotConnectedException;

    /**
     * tests if a topic with the given name exists on the server.
     *
     * @param name name of the topic
     * @return <code>true</code> if the topic exists on the server, <code>false</code> else.
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public boolean topicExists(String name) throws NotConnectedException;
    
    /**
     * creates an user for the server.
     *
     * @param name name of the user
     * @param password password of the user
     *
     * @return a representation of the created user
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public User createUser(String name, String pass) throws NotConnectedException;

    /**
     * gets an already existing user from the server.
     *
     * @param name name of the user
     *
     * @return a representation of the user
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public User getUser(String name) throws NotConnectedException;

    /**
     * deletes an user from the server.
     *
     * @param name name of the user
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public void deleteUser(String name) throws NotConnectedException;

    /**
     * tests if an user with the given name exists on the server.
     *
     * @param name name of the user
     * @return <code>true</code> if the user exists on the server, <code>false</code> else/
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public boolean userExists(String name) throws NotConnectedException;

    /** 
     * gets names of the users on the server.
     *
     * @return names of all users on the server
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public String[] getUsersNames() throws NotConnectedException;

    /**
     * sets/unsets <em>read</em> right for a <strong>given user</strong> on a destination.
     *
     * @param userName name of the user
     * @param right if <code>true</code>, user has right to read on destination, else he's not allowed.
     * @param destinationName name of the destination
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public void setReadingRight(String userName, boolean right, String destinationName) throws NotConnectedException;

    /**
     * sets/unsets <em>read</em> right for <strong>all users</strong> on a destination.
     *
     * @param right if <code>true</code>, everybody has right to read on destination, else nobody is allowed.
     * @param destinationName name of the destination
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public void setReadingRight(boolean right, String dest) throws NotConnectedException;

    /**
     * sets/unsets <em>write</em> right for a <strong>given user</strong> on a destination.
     *
     * @param userName name of the user
     * @param right if <code>true</code>, user has right to write on destination, else he's not allowed.
     * @param destinationName name of the destination
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public void setWritingRight(String userName, boolean right, String destinationName) throws NotConnectedException;

    /**
     * sets/unsets <em>write</em> right for <strong>all users</strong> on a destination.
     *
     * @param right if <code>true</code>, everybody has right to write on destination, else nobody is allowed.
     * @param destinationName name of the destination
     *
     * @throws NotConnectedException if the administrator is not connected to the server
     */
    public void setWritingRight(boolean right, String dest) throws NotConnectedException;
    
    public QueueConnectionFactory createQueueConnectionFactory() throws NotConnectedException;
    public XAQueueConnectionFactory createXAQueueConnectionFactory() throws NotConnectedException;
    public TopicConnectionFactory createTopicConnectionFactory() throws NotConnectedException;    
    public XATopicConnectionFactory createXATopicConnectionFactory() throws NotConnectedException;
    
    public void createCluster(Cluster cluster) throws NotConnectedException;
    public void deleteCluster(String name) throws NotConnectedException;
}





                                                 
