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


package fr.dyade.aaa.mom;

import java.lang.*;

/**	this object contains all the informations about
 *	the different versions of the MOM, JMS, ...
 */

public class MetaData implements java.io.Serializable {
	
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
	
	/** constructor 
	 *	parameters set ofr the first time 
	 */
	public MetaData() {
		this.JMSVersion = "version 1.0";
		this.JMSMajorVersion = 1;
		this.JMSMinorVersion = 1;
		this.providerName = "MOM-AAA";
		this.providerVersion = "version 1.0";
		this.providerMajorVersion = 1;
		this.providerMinorVersion = 1;
	
	}
	
	/** get the JMS version */
	public String getJMSVersion() {
		return JMSVersion;
	}
	
	/** set the JMS version */
	public void setJMSVersion(String JMSVersion) {
		this.JMSVersion = JMSVersion;
	}
	
	/** get the major version supported by JMS */
	public int getJMSMajorVersion(){
		return JMSMajorVersion;
	}
	
	/** set the major version supported by JMS */
	public void setJMSMajorVersion(int JMSMajorVersion){
		this.JMSMajorVersion = JMSMajorVersion;
	}
	
	/** get the minor version supported by JMS */
	public int getJMSMinorVersion() {
		return JMSMinorVersion;
	}
	
	/** set the minor version supported by JMS */
	public void setJMSMinorVersion(int JMSMinorVersion) {
		this.JMSMinorVersion = JMSMinorVersion;
	}
	
	/** get the name of the provider */
	public String getJMSProviderName() {
		return providerName;
	}
	
	/** set the name of the provider */
	public void setJMSProviderName(String providerName) {
		this.providerName = providerName;
	}
	
	/** get the version of the provider */
	public String getProviderVersion() {
		return providerVersion;
	}
	
	/** set the version of the provider */
	public void setProviderVersion(String providerVersion) {
		this.providerVersion = providerVersion;
	}
	
	/** get the major version supported by the provider */
	public int getProviderMajorVersion() {
		return providerMajorVersion;
	}
	
	/** set the major version supported by the provider */
	public void setProviderMajorVersion(int providerMajorVersion) {
		this.providerMajorVersion = providerMajorVersion;
	}
	
	/** get the minor version supported by the provider */
	public int getProviderMinorVersion() {
		return providerMinorVersion;
	}
	
	/** set the minor version supported by the provider */
	public void setProviderMinorVersion(int providerMinorVersion) {
		this.providerMinorVersion = providerMinorVersion;
	}
}

