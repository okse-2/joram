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

import java.net.ConnectException;

import javax.jms.JMSException;

/**
 * Implements the <code>javax.jms.TopicConnectionFactory</code> interface.
 */
public class TopicConnectionFactory extends ConnectionFactory
                                    implements javax.jms.TopicConnectionFactory
{
  /**
   * Constructs a <code>TopicConnectionFactory</code> instance wrapping a 
   * given agent server url.
   *
   * @param url  Url of the agent server.
   * @exception ConnectException  If the url is incorrect.
   */
  public TopicConnectionFactory(String url) throws ConnectException
  {
    super(url);
  }
  
  /** Returns a string view of the connection factory. */
  public String toString()
  {
    return "TCF:" + config.serverAddr.toString();
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.TopicConnection
         createTopicConnection(String name, String password)
         throws JMSException
  {
    return new TopicConnection(config, name, password);
  }

  /**
   * API method.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public javax.jms.TopicConnection createTopicConnection() throws JMSException
  {
    return createTopicConnection("anonymous", "anonymous");
  }
}
