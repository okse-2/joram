package com.scalagent.joram.osgi.client.service;

import java.io.Reader;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.User;

/**
 *  Interface of the OSGI service that enables the handling and the administration
 * of Joram through a TCP connection.
 */
public interface JoramClient {
  /**
   * Performs a static connection for administration.
   */
  public void connect(String host, int port,
                      String name, String password,
                      int cnxTimer) throws Exception;

  /**
   * Closes the administration static connection.
   */
  public void disconnect();

  /**
   *
   */
  public ConnectionFactory getTcpConnectionFactory(String hostname, int port) throws Exception;

  /**
   * Get a default initial context for JNDI requests.
   */
  public Context getInitialContext() throws Exception;

  /**
   *
   */
  public Context getInitialContext(Hashtable prop) throws Exception;

  /**
   * Retrieves the named object.
   * 
   * @param name  the name of the object to look up
   * @return  the object bound to name
   * 
   * @throws NamingException  if a naming exception is encountered
   * 
   * @see Context#lookup(String)
   */
  public Object lookup(String name) throws NamingException;

  /**
   *
   */
  public boolean executeAdminXML(Reader reader) throws Exception;

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @exception Exception   If the creation fails.
   */
  public User createUser(String name, String password) throws Exception;

  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param name       The name of the queue.
   *
   * @exception Exception   If the creation fails.
   */
  public Queue createQueue(String name) throws Exception;

  /**
   * Creates or retrieves a topic destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Topic</code> instance.
   *
   * @exception Exception   If the creation fails.
   */
  public Topic createTopic(String name) throws Exception;

  /**
   * Returns the list of all destinations that exist on the server.
   */
  public Destination[] getDestinations() throws Exception;

  /**
   * Returns the list of all users that exist on the server.
   */
  public User[] getUsers() throws Exception;
}
