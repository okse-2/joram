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
 
import fr.dyade.aaa.agent.*;
import java.util.*; 
 
/**
 * A <code>Theme</code> is a node of a <code>Topic</code>
 * tree. It is on a <code>Theme</code> that messages are
 * actually published, and to a <code>Theme</code> that
 * clients actually subscribe.
 *
 * @see  fr.dyade.aaa.mom.Topic
 */
public class Theme implements java.io.Serializable
{ 
  /** The <code>Theme</code> name. */
  private String nameTheme;

  /** The absolute name of the <code>Theme</code>. */
  private String absoluteNameTheme;

  /** Table holding the subscriptions to this <code>Theme</code>. */
  private Hashtable subscriptionTable ; 

  /** Table holding the daugther themes of this <code>Theme</code>. */
  private Hashtable themeDaughterTable;

  /** Constructor. */
  public Theme(String nameTheme, String absoluteNameTheme)
  {
    this.nameTheme = nameTheme;
    this.absoluteNameTheme = absoluteNameTheme;
    subscriptionTable = new Hashtable();
    themeDaughterTable = new Hashtable();
  }

  
  /** Method returning the <code>Theme</code> name. */
  public String getNameTheme()
  {
    return nameTheme;
  }


  /** Method returning the absolute theme name of the <code>Theme</code>. */
  public String getAbsoluteNameTheme()
  {
    return absoluteNameTheme;
  }

 
  /** Method returning the table of this <code>Theme</code> daughters. */
  public Hashtable getThemeDaughterTable() {
    return themeDaughterTable;
  }


  /** Method adding a daughter theme to this <code>Theme</code>. */
  public void addThemeDaughter(String nameThemeDaugther, Theme ThemeDaugther)
    throws Exception
  {
    if (!themeDaughterTable.containsKey(nameThemeDaugther))
      themeDaughterTable.put(nameThemeDaugther, ThemeDaugther);
    else
      throw (new Exception("Error Construction Theme Tree"));
  }


  /** Method getting the daughter theme from its name.  */
  public Theme getThemeDaughter(String nameThemeDaugther)
  {
    return ((Theme) themeDaughterTable.get(nameThemeDaugther));
  }


  /**
   * Method adding a durable <code>TopicSubscription</code> to 
   * the <code>Theme</code>.
   
  public void addSubscription(String nameSubscription,
    TopicSubscription sub) throws Exception
  {
    KeyTopicSubscription key = new KeyTopicSubscription(nameSubscription);

    if (!subscriptionTable.containsKey(key))
      subscriptionTable.put(key, sub);
    else
      throw (new MOMException("A subscription to the theme " + nameTheme +
        " was already done",MOMException.SUBSCRIPTION_ALREADY_EXIST));
  }*/

  /**
   * Method adding a temporary <code>TopicSubscription</code> to 
   * the <code>Theme</code>.
   */
  public void addSubscription(AgentId clientId, String nameSubscription,
    TopicSubscription sub, int drvKey, boolean durable) throws Exception
  {
    TopicSubscriptionKey key = new TopicSubscriptionKey(clientId, nameSubscription, drvKey, durable);

    if (!subscriptionTable.containsKey(key))
      subscriptionTable.put(key, sub);
    else
      throw (new MOMException("A subscription to the theme " + nameTheme +
        " was already done",MOMException.SUBSCRIPTION_ALREADY_EXIST));
  }


  /**
   * Method returning the <code>TopicSubscription</code> corresponding
   * to the subscription name and connection key parameters.
   */
  public TopicSubscription getSubscription(AgentId clientId, String nameSubscription, int drvKey)
  {
    // Constructing the key as if the subscription was durable.
    TopicSubscriptionKey key = new TopicSubscriptionKey(clientId, nameSubscription, drvKey, true);
    TopicSubscription tSub = (TopicSubscription) subscriptionTable.get(key);

    // If no durable subscription has been found, looking for a
    // temporary one.
    if (tSub == null) {
      key = new TopicSubscriptionKey(clientId, nameSubscription, drvKey, false);
      tSub = (TopicSubscription) subscriptionTable.get(key);
    }
    
    return tSub;
  }

	
  /**
   * Method returning all the <code>TopicSubscription</code>s to
   * this <code>Theme</code>.
   */
  public Enumeration getAllSubscriptions()
  {
    return subscriptionTable.elements();
  }


  /**
   * Method removing a <code>TopicSubscription</code> to this
   * <code>Theme</code>.
   */
  public TopicSubscription removeSubscription(AgentId clientId, String nameSubscription, int drvKey)
  {
    // Constructing the key as if the subscription was durable.
    TopicSubscriptionKey key = new TopicSubscriptionKey(clientId, nameSubscription, drvKey, true);
    TopicSubscription tSub = (TopicSubscription) subscriptionTable.remove(key);

    // If no durable subscription has been found, looking for a
    // temporary one.
    if (tSub == null) {
      key = new TopicSubscriptionKey(clientId, nameSubscription, drvKey, false);
      tSub = (TopicSubscription) subscriptionTable.remove(key);
    }

    return tSub;
  }
	
}

