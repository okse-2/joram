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
import fr.dyade.aaa.agent.*;
import java.util.*; 
 
/**
 *	a theme is a node or a leaf for the tree of a Topic 
 *	the HashTable methods were wrapped in new methods so as to
 * 	add checking of identity of the administrator later 
 *	
 * @see         fr.dyade.aaa.mom.Topic
 */


public class Theme implements java.io.Serializable { 
 
 	/** the name of a theme */
	String nameTheme;
	
	/** the absolute name of the theme */
	String absoluteNameTheme;
	
	/**  
	 * the list of subscription which contains all of the subscriptions of
	 *	the clients
	 */ 
	private Hashtable subscriptionTable ; 
 
 	/**
	 *	the list of the theme-daugther of the mother theme
	 */
	private Hashtable themeDaughterTable;
 
 	public Theme(String nameThemeNew, String absoluteNameThemeNew) {
		nameTheme = nameThemeNew;
		absoluteNameTheme = absoluteNameThemeNew;
		subscriptionTable = new Hashtable();
		themeDaughterTable = new Hashtable();
	}
 
 	/** get the name of the theme */
	public String getNameTheme() {
		return nameTheme;
	}
 
 	/** get the absolute name of the theme */
	public String getAbsoluteNameTheme() {
		return absoluteNameTheme;
	}
 
 	/** return the list of the theme Daughter 
	 *	only for administrator
	 */
	public Hashtable getThemeDaughterTable() {
		return themeDaughterTable;
	}
	
	/** return the table with all the Subscriptions 
	 *	only for administrator
	 */
	public Hashtable getSubscriptionTable() {
		return subscriptionTable;
	}
	
	/** add a Theme-Daughter to a Theme  */
	public void addThemeDaughter(String nameThemeDaugther, Theme ThemeDaugther) throws Exception{
		if(!themeDaughterTable.containsKey(nameThemeDaugther))
			themeDaughterTable.put(nameThemeDaugther, ThemeDaugther);
		else
			throw (new Exception("Error Construction Theme Tree"));
	}
	
 	/** get the Theme object associated to the name */
 	public Theme getThemeDaughter(String nameThemeDaugther) {
		return ((Theme) themeDaughterTable.get(nameThemeDaugther));
	}
 	
	/** add a subscription to a Theme 
	 *	if a subscrption was already done by a client an exception was thrown
	 */
	public void addSubscription(String nameSubscription, AgentId client, Subscription sub) throws Exception{
		KeySubscription key = new KeySubscription(nameSubscription, client);
		if(!subscriptionTable.containsKey(key))
			subscriptionTable.put(key, sub);
		else
			throw (new MOMException("A subscription to the theme "+nameTheme+" was already done",MOMException.SUBSCRIPTION_ALREADY_EXIST));
	}
	
	/** get a particular subscription from a client */
	public Subscription getSubscription(String nameSubscription, AgentId client) {
		KeySubscription key = new KeySubscription(nameSubscription, client);
		return ((Subscription) subscriptionTable.get(key));
	}
	
	/** return all the subscriptions of the hashtable */
	public Enumeration getAllSubscriptions() {
		return subscriptionTable.elements();
	}
	
	/** remove a subscription from a client in the theme */
	public Subscription removeSubscription(String nameSubscription, AgentId client) {
		KeySubscription key = new KeySubscription(nameSubscription, client);
		return ((Subscription) subscriptionTable.remove(key));
	}
	
}

