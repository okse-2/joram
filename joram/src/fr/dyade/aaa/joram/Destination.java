/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.joram;

import java.util.Hashtable;

import javax.naming.*;

import org.objectweb.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.Destination</code> interface.
 */
public abstract class Destination implements javax.jms.Destination,
                                             javax.naming.Referenceable,
                                             java.io.Serializable
{
  /**
   * Class table holding the <code>Destination</code> instances, needed by the
   * naming service.
   * <p>
   * <b>Key:</b> destination's name<br>
   * <b>Object:</b> destination's instance
   */
  protected static Hashtable instancesTable = new Hashtable();

  /** Identifier of the destination agent. */
  protected String agentId;


  /**
   * Constructs a destination.
   *
   * @param agentId  Identifier of the agent destination.
   */ 
  public Destination(String agentId)
  {
    this.agentId = agentId;

    // Registering this instance in the table:
    instancesTable.put(agentId, this);

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }


  /** Sets the naming reference of this destination. */
  public Reference getReference() throws NamingException
  {
    Reference ref = new Reference(this.getClass().getName(),
                                  "fr.dyade.aaa.joram.ObjectFactory",
                                  null);
    ref.add(new StringRefAddr("dest.name", agentId));
    return ref;
  }

  /** Returns a destination to the name service. */
  public static Object getInstance(String name)
  {
    return instancesTable.get(name);
  }

  /** Returns the name of the destination. */
  public String getName()
  {
    return agentId;
  }

  /**
   * Returns <code>true</code> if the parameter object is a Joram destination
   * wrapping the same agent identifier.
   */
  public boolean equals(Object obj)
  {
    if (! (obj instanceof fr.dyade.aaa.joram.Destination))
      return false;

    return (agentId.equals(((fr.dyade.aaa.joram.Destination) obj).getName()));
  }
}
