/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
 * Initial developer(s): David Feliot (ScalAgent DT)
 * Contributor(s): Frederic Maistre (Bull SA)
 */
package org.objectweb.joram.client.jms.local;

import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.TopicConnection;

import javax.naming.NamingException;

/**
 * A <code>TopicLocalConnectionFactory</code> instance is a factory of
 * local connections for Pub/Sub communication.
 */
public class TopicLocalConnectionFactory
    extends org.objectweb.joram.client.jms.TopicConnectionFactory {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;


  /**
   * Constructs an empty <code>TopicLocalConnectionFactory</code> instance.
   */
  public TopicLocalConnectionFactory()
  {
    super("localhost", -1);
  }

  /**
   * Method inherited from the <code>TopicConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.TopicConnection
         createTopicConnection(String name, String password)
         throws javax.jms.JMSException
  {
    LocalConnection lc = new LocalConnection(name, password);
    return new TopicConnection(params, lc);
  }

  /**
   * Method inherited from the <code>ConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   */
  public javax.jms.Connection createConnection(String name, String password)
                              throws javax.jms.JMSException
  {
    LocalConnection lc = new LocalConnection(name, password);
    return new Connection(params, lc);
  }


  /**
   * Admin method creating a <code>javax.jms.TopicConnectionFactory</code>
   * instance for creating local connections.
   */ 
  public static javax.jms.TopicConnectionFactory create()
  {
    return new TopicLocalConnectionFactory();
  }
}
