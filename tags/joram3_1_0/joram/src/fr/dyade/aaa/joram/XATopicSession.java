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

import fr.dyade.aaa.mom.jms.*;

import java.util.*;

import javax.jms.JMSException;
import javax.transaction.xa.*;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.XATopicSession</code> interface.
 */
public class XATopicSession extends XASession
                            implements javax.jms.XATopicSession
{
  /**
   * Constructs an <code>XATopicSession</code> instance.
   *
   * @param cnx  The connection the session belongs to.
   *
   * @exception JMSException  Actually never thrown.
   */
  XATopicSession(XATopicConnection cnx) throws JMSException
  {
    super(cnx, new TopicSession(cnx, true, 0));
  }

  
  /** Returns a String image of this session. */
  public String toString()
  {
    return "XATopicSess:" + ident;
  }


  /** API method. */ 
  public javax.jms.TopicSession getTopicSession() throws JMSException
  {
    return (TopicSession) sess;
  }
}
