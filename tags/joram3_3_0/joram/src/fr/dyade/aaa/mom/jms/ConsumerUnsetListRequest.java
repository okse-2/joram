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
package fr.dyade.aaa.mom.jms;

/**
 * A <code>ConsumerUnsetListRequest</code> is sent by a
 * <code>MessageConsumer</code> which listener is unset.
 */
public class ConsumerUnsetListRequest extends AbstractJmsRequest
{
  /**
   * Identifies either the last listener "receive" request (queueMode), or the
   * name of the subscription which listener is unset.
   */
  private String id;
  /** <code>true</code> if the listener was listening to a queue. */
  private boolean queueMode;

  /**
   * Constructs a <code>ConsumerUnsetListRequest</code>.
   *
   * @param id  Identifies either the last listener "receive" request
   *          (queueMode), or the name of the subscription which listener is
   *          unset.
   * @param queueMode  <code>true</code> if the listener was listening to a
   *          queue.
   */
  public ConsumerUnsetListRequest(String id, boolean queueMode)
  {
    super(null);
    this.id = id;
    this.queueMode = queueMode;
  }


  /**
   * Returns the identifier either of the last listener "receive" request
   * (queueMode), or of the name of the subscription which listener is unset.
   */
  public String getId()
  {
    return id;
  }

  /** Returns <code>true</code> if the listener was listening to a queue. */
  public boolean queueMode()
  {
    return queueMode;
  }
}
