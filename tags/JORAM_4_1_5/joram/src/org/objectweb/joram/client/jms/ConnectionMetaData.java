/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import java.util.*;

import javax.jms.JMSException;


/**
 * Implements the <code>javax.jms.ConnectionMetaData</code> interface.
 */
public class ConnectionMetaData implements javax.jms.ConnectionMetaData
{
  public final static int jmsMajorVersion = 1;
  public final static int jmsMinorVersion = 1;
  public final static String jmsProviderName = "Joram";
  public final static String jmsVersion = "1.1";
  public final static int providerMajorVersion = 4;
  public final static int providerMinorVersion = 1;
  public final static String providerVersion = "4.1.5";
  public final static Vector jmsxProperties = new Vector();

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


