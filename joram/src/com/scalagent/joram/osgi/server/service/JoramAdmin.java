package com.scalagent.joram.osgi.server.service;

import java.io.Reader;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.User;

/**
 * Interface of the OSGI service that enables to ..
 */
public interface JoramAdmin {
  /**
   *
   */
  public ConnectionFactory getLocalConnectionFactory() throws Exception;

  /**
   *
   */
  public void setRootUserName(String root);

  /**
   *
   */
  public void setRootPassword(String password);

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
  public Destination[] getDestinations() throws Exception;

  /**
   * Returns the list of all users that exist on the server.
   */
  public User[] getUsers() throws Exception;
}
