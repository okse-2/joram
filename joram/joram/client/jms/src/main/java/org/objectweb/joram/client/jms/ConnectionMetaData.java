/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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
import java.util.StringTokenizer;
import java.util.Vector;

import javax.jms.JMSException;

/**
 * Implements the <code>javax.jms.ConnectionMetaData</code> interface.
 */
public final class ConnectionMetaData implements javax.jms.ConnectionMetaData {
  /** JMS major version number */
  public final static int jmsMajorVersion = 1;
  /** JMS minor version number */
  public final static int jmsMinorVersion = 1;
  /** JMS API version, currently 1.1 */
  public final static String jmsVersion = "1.1";
  /** JMS provider name: Joram */
  public final static String providerName = "Joram";
  /** Joram's major version number, currently ${major.filter.value} */
  public static int providerMajorVersion = 0;//major
  /** Joram's minor version number, currently ${minor.filter.value} */
  public static int providerMinorVersion = 0;//minor
  /** Joram's implementation version, currently ${build.filter.value}. */
  public static String providerVersion = "x.x.x"; // version
  /** Enumeration of the Joram's JMSX property names */
  private final static Vector jmsxProperties = new Vector();
  
  static {
    jmsxProperties.add("JMSXDeliveryCount");
    jmsxProperties.add("JMSXGroupID");
    jmsxProperties.add("JMSXGroupSeq");
    getVersion();
  }

  private static void getVersion() {
  	// Read version from the package
  	Package pkg = ConnectionMetaData.class.getPackage();
  	if (pkg != null) {
  		String implVersion = pkg.getImplementationVersion();
  		if (implVersion != null) {
  			providerVersion = implVersion;
  			StringTokenizer st = new StringTokenizer(implVersion, ".");
  			providerMajorVersion = Integer.parseInt((String) st.nextElement());
  			providerMinorVersion = Integer.parseInt((String) st.nextElement());
  		}
  	}
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
   * API method: Gets the Joram's implementation version, currently 5.0.10.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getProviderVersion() throws JMSException {
    return providerVersion;
  }
}


