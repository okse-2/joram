/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package fr.dyade.aaa.joram.admin;

import java.util.Vector;
import java.util.Hashtable;

import javax.naming.*;


/**
 * Old administration class.
 *
 * @deprecated  This class is temporary kept but the methods of the new
 *              <code>AdminItf</code> interface should be used instead.
 */
public class Cluster extends AdministeredObject
{
  /** Vector of topic names. */
  Vector topics;
  /** <code>true</code> when the cluster object has been sent to the MOM. */
  boolean locked;


  /**
   * Constructs a <code>Cluster</code> instance.
   *
   * @param name  String name of the cluster.
   */
  public Cluster(String name)
  {
    super(name);
    locked = false;
  }

  /**
   * Adds a topic to the cluster.
   *
   * @param topic  The <code>Topic</code> instance.
   * @exception AdminException  If the cluster has already been created.
   */
  public void addTopic(fr.dyade.aaa.joram.Topic topic) throws AdminException
  {
    if (locked)
      throw new AdminException("Forbidden method call as the cluster has been"
                               + " already created.");
  
    if (topics == null)
      topics = new Vector();
      
    topics.add(topic.getName());
  }


  /** Sets the naming reference of a cluster. */
  public Reference getReference() throws NamingException
  {
    Reference ref = super.getReference();

    String vector = null;
    int i;
    for (i = 0; i < topics.size() - 1; i++)
      vector = vector + (String) topics.get(i) + " ";
    vector = vector + (String) topics.get(i);

    ref.add(new StringRefAddr("cluster.topics", vector));
    ref.add(new StringRefAddr("cluster.locked",
                              (new Boolean(locked)).toString()));
    return ref;
  }


  public Hashtable code() {
    return new Hashtable();
  }

  public Object decode(Hashtable h) { 
    return null;
  }
}
