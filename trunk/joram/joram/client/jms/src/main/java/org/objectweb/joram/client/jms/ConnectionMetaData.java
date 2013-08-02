/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2013 ScalAgent Distributed Technologies
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

import java.util.Enumeration;
import java.util.Vector;

import javax.jms.JMSException;

import org.objectweb.joram.shared.stream.MetaData;

/**
 * Implements the <code>javax.jms.ConnectionMetaData</code> interface.
 */
public final class ConnectionMetaData implements javax.jms.ConnectionMetaData {
  /** JMS major version number */
  public final static int jmsMajorVersion = 2;
  /** JMS minor version number */
  public final static int jmsMinorVersion = 0;
  /** JMS API version, currently 1.1 */
  public final static String jmsVersion = "2.0";
  /** JMS provider name: Joram */
  public final static String providerName = "Joram";
  /** Joram's major version number. */
  public final static int providerMajorVersion;
  /** Joram's minor version number. */
  public final static int providerMinorVersion;
  /** Joram's implementation version. */
  public final static String providerVersion;
  
  /**
   * Enumeration of the Joram's JMSX property names, currently  JMSXDeliveryCount,
   * JMSXGroupID and JMSXGroupSeq.
   */
  private final static Vector jmsxProperties = new Vector();
  
  static {
  	jmsxProperties.add("JMSXDeliveryCount");
  	jmsxProperties.add("JMSXGroupID");
  	jmsxProperties.add("JMSXGroupSeq");

  	providerVersion = MetaData.version;
  	providerMajorVersion = MetaData.major;
  	providerMinorVersion = MetaData.minor;
  }

  /**
   * API method: Gets the JMS major version number.
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getJMSMajorVersion() throws JMSException {
    return jmsMajorVersion;
  }

  /**
   * API method: Gets the JMS minor version number
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getJMSMinorVersion() throws JMSException {
    return jmsMinorVersion;
  }

  /**
   * API method: Gets the JMS API version, currently 1.1.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getJMSVersion() throws JMSException {
    return jmsVersion;
  }

  /**
   * API method: Gets the JMS provider name: Joram.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getJMSProviderName() throws JMSException {
    return providerName;
  }

  /**
   * API method: Gets an enumeration of the JMSX property names.
   *
   * @exception JMSException  Actually never thrown.
   */
  public Enumeration getJMSXPropertyNames() throws JMSException {
    return jmsxProperties.elements();
  }

  /**
   * API method: Gets the Joram's major version number.
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getProviderMajorVersion() throws JMSException {
  	return providerMajorVersion;
  }

  /**
   * API method: Gets the Joram's minor version number.
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getProviderMinorVersion() throws JMSException {
  	return providerMinorVersion;
  }

  /**
   * API method: Gets the Joram's implementation version.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getProviderVersion() throws JMSException {
    return providerVersion;
  }
}


