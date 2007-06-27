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
public class ConnectionMetaData implements javax.jms.ConnectionMetaData {
  /** JMS major version number */
  public final static int jmsMajorVersion = 1;
  /** JMS minor version number */
  public final static int jmsMinorVersion = 1;
  /** JMS provider name: Joram */
  public final static String jmsProviderName = "Joram";
  /** JMS API version, currently 1.1 */
  public final static String jmsVersion = "1.1";
  /** Joram's major version number */
  public static int providerMajorVersion = 5;
  /** Joram's minor version number */
  public static int providerMinorVersion = 0;
  /** Joram's implementation version, currently @version@. */
  public final static String providerVersion = "@version@";
  /** Enumeration of the Joram's JMSX property names */
  public final static Vector jmsxProperties = new Vector();

  static {
    int idx1 = 0;
    int idx2 = 0;
    try {
      idx1 = providerVersion.indexOf('.');
      if (idx1 != -1) {
        providerMajorVersion =
          Integer.parseInt(providerVersion.substring(0, idx1));
      }
    } catch (Exception exc) {
    }
    try {
      idx2 = providerVersion.indexOf('.', idx1 +1);
      if (idx2 != -1) {
        providerMinorVersion =
          Integer.parseInt(providerVersion.substring(idx1 +1, idx2));
      }
    } catch (Exception exc) {
    }

    jmsxProperties.add("JMSXDeliveryCount");
    jmsxProperties.add("JMSXGroupID");
    jmsxProperties.add("JMSXGroupSeq");
  }

  /**
   * API method: Gets the JMS major version number..
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getJMSMajorVersion() throws JMSException {
    return jmsMajorVersion;
  }

  /**
   * API method: Gets the JMS minor version number..
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getJMSMinorVersion() throws JMSException {
    return jmsMinorVersion;
  }

  /**
   * API method: Gets the JMS provider name: Joram.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getJMSProviderName() throws JMSException {
    return jmsProviderName;
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
   * API method: Gets the Joram's implementation version, currently @version@.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getProviderVersion() throws JMSException {
    return providerVersion;
  }
}


