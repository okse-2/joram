/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram.admin;

import fr.dyade.aaa.joram.JoramTracing;

import javax.naming.*;

import java.util.Hashtable;
import java.util.Vector;

/**
 * The <code>AdministeredObject</code> class is the parent class of all
 * JORAM administered objects.
 */
public abstract class AdministeredObject implements java.io.Serializable,
                                                    Referenceable
{
  /**
   * Class table holding the <code>AdministeredObject</code> instances.
   * <p>
   * <b>Key:</b> object's identifier<br>
   * <b>Object:</b> object's instance
   */
  protected static Hashtable instancesTable = new Hashtable();

  /** Identifier of the object. */
  protected String id;


  /**
   * Constructs an administered object.
   *
   * @param id  Identifier of the object.
   */ 
  protected AdministeredObject(String id)
  {
    this.id = this.getClass().getName() + ":" + id;

    // Registering this instance in the table:
    instancesTable.put(this.id, this);
  }

  
  /** Sets the naming reference of an administered object. */
  public Reference getReference() throws NamingException
  {
    Reference ref = new Reference(this.getClass().getName(),
                                  "fr.dyade.aaa.joram.admin.ObjectFactory",
                                  null);
    ref.add(new StringRefAddr("adminObj.id", id));
    return ref;
  }
  
  /** Retrieves an instance from the table. */
  public static Object getInstance(String name)
  {
    if (name == null)
      return null;
    return instancesTable.get(name);
  }

  /**
   * Codes an <code>AdministeredObject</code> as a vector for travelling 
   * through the SOAP protocol.
   *
   * @exception NamingException  If the object is not codable.
   */
  public abstract Vector code() throws NamingException;

  /**
   * Decodes a coded <code>AdministeredObject</code>.
   *
   * @exception NamingException  If the coded object is not an
   *              AdministeredObject.
   */
  public static AdministeredObject decode(Vector vec) throws NamingException
  {
    String className = (String) vec.remove(0);

    if (className.equals("TcpConnectionFactory"))
      return fr.dyade.aaa.joram.tcp.TcpConnectionFactory.decode(vec);
    else if (className.equals("QueueTcpConnectionFactory"))
      return fr.dyade.aaa.joram.tcp.QueueTcpConnectionFactory.decode(vec);
    else if (className.equals("TopicTcpConnectionFactory"))
      return fr.dyade.aaa.joram.tcp.TopicTcpConnectionFactory.decode(vec);
    else if (className.equals("XATcpConnectionFactory"))
      return fr.dyade.aaa.joram.tcp.XATcpConnectionFactory.decode(vec);
    else if (className.equals("XAQueueTcpConnectionFactory"))
      return fr.dyade.aaa.joram.tcp.XAQueueTcpConnectionFactory.decode(vec);
    else if (className.equals("XATopicTcpConnectionFactory"))
      return fr.dyade.aaa.joram.tcp.XATopicTcpConnectionFactory.decode(vec);
    else if (className.equals("SoapConnectionFactory"))
      return fr.dyade.aaa.joram.soap.SoapConnectionFactory.decode(vec);
    else if (className.equals("QueueSoapConnectionFactory"))
      return fr.dyade.aaa.joram.soap.QueueSoapConnectionFactory.decode(vec);
    else if (className.equals("TopicSoapConnectionFactory"))
      return fr.dyade.aaa.joram.soap.TopicSoapConnectionFactory.decode(vec);
    else if (className.equals("Queue"))
      return fr.dyade.aaa.joram.Queue.decode(vec);
    else if (className.equals("Topic"))
      return fr.dyade.aaa.joram.Topic.decode(vec);
    else if (className.equals("TemporaryQueue"))
      return fr.dyade.aaa.joram.TemporaryQueue.decode(vec);
    else if (className.equals("TemporaryTopic"))
      return fr.dyade.aaa.joram.TemporaryTopic.decode(vec);
    else if (className.equals("DeadMQueue"))
      return fr.dyade.aaa.joram.admin.DeadMQueue.decode(vec);
    else if (className.equals("User"))
      return fr.dyade.aaa.joram.admin.User.decode(vec);
    else
      throw new NamingException("Coded object is not an AdministeredObject.");
  }
}
