package com.scalagent.joram.osgi.client.service;

import java.io.Reader;
import java.util.List;
import java.util.Hashtable;

import javax.naming.Context;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;

/**
 * Interface of the OSGI service that enables to ..
 */
public interface JoramClient {
  /**
   *
   */
  public void connect(String host, int port,
                      String name, String password,
                      int cnxTimer) throws Exception;

  /**
   *
   */
  public void disconnect();

  /**
   *
   */
  public ConnectionFactory getTcpConnectionFactory(String hostname, int port) throws Exception;

  /**
   *
   */
  public Context getInitialContext() throws Exception;

  /**
   *
   */
  public Context getInitialContext(Hashtable prop) throws Exception;

  /**
   *
   */
  public boolean executeAdminXML(Reader reader) throws Exception;

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @exception Exception   If the creation fails.
   */
  public void createUser(String name, String password) throws Exception;

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
  public List getDestinations() throws Exception;

  /**
   * Returns the list of all users that exist on the server.
   */
  public List getUsers() throws Exception;
}
