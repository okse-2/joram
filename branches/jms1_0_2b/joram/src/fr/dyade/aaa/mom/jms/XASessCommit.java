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

/**
 * An <code>XASessCommit</code> instance is used by an <code>XASession</code>
 * for commiting the messages and acknowledgements it sent to the proxy.
 */
public class XASessCommit extends AbstractJmsRequest
{
  /** Identifier of the resource and the commiting transaction. */
  private String id;


  /**
   * Constructs an <code>XASessCommit</code> instance.
   *
   * @param id  Identifier of the resource and the commiting transaction.
   */
  public XASessCommit(String id)
  {
    super(null);
    this.id = id;
  }


  /** Returns the identifier of the resource and the commiting transaction. */
  public String getId()
  {
    return id;
  }
}