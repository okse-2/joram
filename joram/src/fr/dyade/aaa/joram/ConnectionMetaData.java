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
package fr.dyade.aaa.joram;

import java.util.*;

import javax.jms.JMSException;

/**
 * Implements the <code>javax.jms.ConnectionMetaData</code> interface.
 */
public class ConnectionMetaData implements javax.jms.ConnectionMetaData
{
  private static int jmsMajorVersion = 1;
  private static int jmsMinorVersion = 1;
  private static String jmsProviderName = "Joram";
  private static String jmsVersion = "1.1";
  private static int providerMajorVersion = 3;
  private static int providerMinorVersion = 2;
  private static String providerVersion = "3.2";
  private static Vector jmsxProperties = new Vector();

  static
  {
    jmsxProperties.add("JMSXDeliveryCount");
    jmsxProperties.add("JMSXGroupID");
    jmsxProperties.add("JMSXGroupSeq");
  }

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
  public Enumeration getJMSXPropertyNames() throws JMSException
  {
    return jmsxProperties.elements();
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


