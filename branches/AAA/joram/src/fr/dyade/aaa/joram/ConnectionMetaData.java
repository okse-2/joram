/*
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */


package fr.dyade.aaa.joram; 

import java.lang.*; 

/** 
 *	a ConnectionMetaData is as JMS specifications 
 * 
 *	@see fr.dyade.aaa.mom.ConnectionMetaData
 *	@see javax.jms.ConnectionMetaData 
 *	@see fr.dyade.aaa.mom.CommonClient
 */ 
 
public class ConnectionMetaData implements javax.jms.ConnectionMetaData { 
	
	/** the JMSVersion */
	private String JMSVersion;
	
	/** the JMSMajorVersion */
	private int JMSMajorVersion;
	
	/** the JMSMinorVersion */
	private int JMSMinorVersion;
	
	/** the providerName */
	private String providerName;
	
	/** the providerVersion */
	private String providerVersion;
	
	/** the providerMajorVersion */
	private int providerMajorVersion;
	
	/** the providerMinorVersion */
	private int providerMinorVersion;
	
	/** constructor */
	public ConnectionMetaData(String JMSVersion, int JMSMajorVersion, int JMSMinorVersion, String providerName, String providerVersion, int providerMajorVersion ,int providerMinorVersion) {
		this.JMSVersion = JMSVersion;
		this.JMSMajorVersion = JMSMajorVersion;
		this.JMSMinorVersion = JMSMinorVersion;
		this.providerName = providerName;
		this.providerVersion = providerVersion;
		this.providerMajorVersion = providerMajorVersion;
		this.providerMinorVersion = providerMinorVersion;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public String getJMSVersion() throws javax.jms.JMSException {
		return JMSVersion;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public int getJMSMajorVersion() throws javax.jms.JMSException {
		return JMSMajorVersion;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public int getJMSMinorVersion() throws javax.jms.JMSException {
		return JMSMinorVersion;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public String getJMSProviderName() throws javax.jms.JMSException {
		return providerName;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public String getProviderVersion() throws javax.jms.JMSException {
		return providerVersion;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public int getProviderMajorVersion() throws javax.jms.JMSException {
		return providerMajorVersion;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public int getProviderMinorVersion() throws javax.jms.JMSException {
		return providerMinorVersion;
	}

    /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public java.util.Enumeration getJMSXPropertyNames() throws javax.jms.JMSException {
	throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available",JMSAAAException.NOT_YET_AVAILABLE));
    }
}
