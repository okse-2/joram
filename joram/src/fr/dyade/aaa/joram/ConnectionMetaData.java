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

import javax.jms.JMSException;

/**
 * Implements the <code>javax.jms.ConnectionMetaData</code> interface.
 */
public class ConnectionMetaData implements javax.jms.ConnectionMetaData
{
  private int jmsMajorVersion = 1;
  private int jmsMinorVersion = 1;
  private String jmsProviderName = "Joram";
  private String jmsVersion = "1.1";
  private int providerMajorVersion = 3;
  private int providerMinorVersion = 1;
  private String providerVersion = "3.1";

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getJMSMajorVersion() throws JMSException
  {
    return jmsMajorVersion;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getJMSMinorVersion() throws JMSException
  {
    return jmsMinorVersion;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getJMSProviderName() throws JMSException
  {
    return jmsProviderName;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getJMSVersion() throws JMSException
  {
    return jmsVersion;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public java.util.Enumeration getJMSXPropertyNames() throws JMSException
  {
    return null;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getProviderMajorVersion() throws JMSException
  {
    return providerMajorVersion;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getProviderMinorVersion() throws JMSException
  {
    return providerMinorVersion;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getProviderVersion() throws JMSException
  {
    return providerVersion;
  }
}
