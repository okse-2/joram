package fr.dyade.aaa.joram.admin;

import fr.dyade.aaa.joram.*;
import java.net.ConnectException


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
     *
     * @throws ConnectException if the administrator was not able to connect to the server
     */
    public void connect(String serverUrl, int port, String name, String password) throws ConnectException;
    
    /**  
     * connects on the server specified by serverUrl with default port >
     *
     * @param serverUrl URL of the server
     * @param name name of the administrator
     * @param password password of the administrator
     *
     * @throws ConnectException if the administrator was not able to connect to the server
     */
    public void connect(String serverUrl, String name, String password) throws ConnectException;

    /**  
     * connects on localhost with default port.
     *
     * @param serverUrl URL of the server
     * @param name name of the administrator
     * @param password password of the administrator
     *
     * @throws ConnectException if the administrator was not able to connect to the server
     */
    public void connect(String name, String password) throws ConnectException;
    
    /**
     * disconnects from a previously connected server.
     * 
     * @throws ConnectException if the administrator is not connected to the server
     */
    public void disconnect() throws ConnectException;
    
    /**
     * creates an new administrator ID.
     *
     * @param name name of the administrator
     * @param password password of the administrator
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public void createAdminId(String name, String password) throws ConnectException;

    /**
     * deletes an administrator ID.
     *
     * @param name name of the administrator ID to delete
     * 
     * @throws ConnectException if the administrator is not connected to the server
     */
    public void deleteAdminId(String name) throws ConnectException;

    /**
     * creates a new queue on the server.
     *
     * @param name name of the queue
     * 
     * @throws ConnectException if the administrator is not connected to the server
     */
    public Queue createQueue(String name) throws ConnectException;

    /**
     * gets a queue from the server.
     *
     * @param name name of the queue
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public Queue getQueue(String name) throws ConnectException;

    /**
     * deletes a queue on the server.
     *
     * @param name name of the queue
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public void deleteQueue(String name) throws ConnectException;

    /**
     * creates a new topic on the server.
     *
     * @param name name of the topic
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public Topic createTopic(String name) throws ConnectException;

    /**
     * gets a topic from the server.
     *
     * @param name name of the topic
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public Topic getTopic(String name) throws ConnectException;

    /**
     * deletes a topic on the server.
     *
     * @param name name of the topic
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public void deleteTopic(String name) throws ConnectException;
    
    /** 
     * gets names of the queues on the server.
     *
     * @return names of all queues on the server
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public String[] getQueuesNames() throws ConnectException;

    /** 
     * gets names of the topics on the server.
     *
     * @return names of all topics on the server
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public String[] getTopicsNames() throws ConnectException;
    
    /**
     * tests if a queue with the given name exists on the server.
     *
     * @param name name of the queue
     * @return <code>true</code> if the queue exists on the server, <code>false</code> else.
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public boolean queueExists(String name) throws ConnectException;

    /**
     * tests if a topic with the given name exists on the server.
     *
     * @param name name of the topic
     * @return <code>true</code> if the topic exists on the server, <code>false</code> else.
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public boolean topicExists(String name) throws ConnectException;
    
    /**
     * creates an user for the server.
     *
     * @param name name of the user
     * @param password password of the user
     *
     * @return a representation of the created user
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public User createUser(String name, String pass) throws ConnectException;

    /**
     * gets an already existing user from the server.
     *
     * @param name name of the user
     *
     * @return a representation of the user
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public User getUser(String name) throws ConnectException;

    /** 
     * change password fon a given user
     *
     * @param name name of the user
     * @param newPassword new password for the user
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public changeUserPassword(String name, String newPassword) throws ConnectException

    /**
     * deletes an user from the server.
     *
     * @param name name of the user
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public void deleteUser(String name) throws ConnectException;

    /**
     * tests if an user with the given name exists on the server.
     *
     * @param name name of the user
     * @return <code>true</code> if the user exists on the server, <code>false</code> else/
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public boolean userExists(String name) throws ConnectException;

    /** 
     * gets names of the users on the server.
     *
     * @return names of all users on the server
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public String[] getUsersNames() throws ConnectException;

    /**
     * sets/unsets <em>read</em> right for a <strong>given user</strong> on a destination.
     *
     * @param userName name of the user
     * @param right if <code>true</code>, user has right to read on destination, else he's not allowed.
     * @param destinationName name of the destination
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public void setReadingRight(String userName, boolean right, String destinationName) throws ConnectException;

    /**
     * sets/unsets <em>read</em> right for <strong>all users</strong> on a destination.
     *
     * @param right if <code>true</code>, everybody has right to read on destination, else nobody is allowed.
     * @param destinationName name of the destination
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public void setReadingRight(boolean right, String dest) throws ConnectException;

    /**
     * sets/unsets <em>write</em> right for a <strong>given user</strong> on a destination.
     *
     * @param userName name of the user
     * @param right if <code>true</code>, user has right to write on destination, else he's not allowed.
     * @param destinationName name of the destination
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public void setWritingRight(String userName, boolean right, String destinationName) throws ConnectException;

    /**
     * sets/unsets <em>write</em> right for <strong>all users</strong> on a destination.
     *
     * @param right if <code>true</code>, everybody has right to write on destination, else nobody is allowed.
     * @param destinationName name of the destination
     *
     * @throws ConnectException if the administrator is not connected to the server
     */
    public void setWritingRight(boolean right, String dest) throws ConnectException;
    
    public QueueConnectionFactory createQueueConnectionFactory() throws ConnectException;
    public XAQueueConnectionFactory createXAQueueConnectionFactory() throws ConnectException;
    public TopicConnectionFactory createTopicConnectionFactory() throws ConnectException;    
    public XATopicConnectionFactory createXATopicConnectionFactory() throws ConnectException;
    
    public void createCluster(Cluster cluster) throws ConnectException;
    public void deleteCluster(String name) throws ConnectException;
}





                                                 
