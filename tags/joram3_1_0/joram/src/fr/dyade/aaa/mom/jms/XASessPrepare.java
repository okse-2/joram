/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
package fr.dyade.aaa.mom.jms;

import java.util.Vector;

/**
 * An <code>XASessPrepare</code> instance is used by an <code>XASession</code>
 * for sending messages and acknowledgements to the proxy.
 */
public class XASessPrepare extends AbstractJmsRequest
{
  /** Identifier of the resource and the preparing transaction. */
  private String id;
  /** Vector of <code>ProducerMessages</code> instances. */
  private Vector sendings;
  /** Vector of <code>SessAckRequest</code> instances. */
  private Vector acks;
  

  /**
   * Constructs an <code>XASessPrepare</code> instance.
   *
   * @param id  Identifier of the resource and the preparing transaction.
   * @param sendings  Vector of <code>ProducerMessages</code> instances.
   * @param acks  Vector of <code>SessAckRequest</code> instances.
   */
  public XASessPrepare(String id, Vector sendings, Vector acks)
  {
    super(null);
    this.id = id;
    this.sendings = sendings;
    this.acks = acks;
  }


  /** Returns the identifier of the resource and the commiting transaction. */
  public String getId()
  {
    return id;
  }

  /** Returns the vector of <code>ProducerMessages</code> instances. */
  public Vector getSendings()
  {
    return sendings;
  }

  /** Returns the vector of <code>SessAckRequest</code> instances. */
  public Vector getAcks()
  {
    return acks;
  }
}
