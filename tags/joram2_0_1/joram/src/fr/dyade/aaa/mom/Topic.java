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
 * A <code>Topic</code> is a <code>Destination</code> agent
 * to which clients can publish messages and/or subscribe for
 * receiving messages.<br>
 * <br>
 * The tree architecture, made of cascading <code>Theme</code>s,
 * is currently de-activated (see the commented code). Thus,
 * a <code>Topic</code> is only made of its root <code>Theme</code>.
 *
 * @see  fr.dyade.aaa.mom.Theme
 * @see  fr.dyade.aaa.mom.Destination	
 */
public class Topic extends fr.dyade.aaa.mom.Destination
{ 
  /** Topic tree root. */
  fr.dyade.aaa.mom.Theme rootTheme;
	
  /** Constructor. */
  public Topic()
  {
    rootTheme = new Theme(".",".");
  }


  /** Agent's reactions. */
  public void react(AgentId from, Notification not) throws Exception
  {
    if (not instanceof NotificationSubscription) { 
      notificationSubscription(from, (NotificationSubscription) not); 
    } else if (not instanceof NotificationUnsubscription) { 
      notificationUnsubscription(from, (NotificationUnsubscription) not);
    } else if (not instanceof NotificationUpdateSubscription) { 
      notificationUpdateSubscription(from, (NotificationUpdateSubscription) not);	 
    } else if (not instanceof NotificationTopicSend) { 
      notificationSend(from, (NotificationTopicSend) not);
    } else  if (not instanceof NotificationAdminDeleteDestination) {
      delete();
    } else { 
      super.react(from, not); 
    } 
  }

 
  /**
   * Method reacting to a <code>NotificationTopicSend</code> wrapping a
   * message sent by a client.
   */ 
  protected void notificationSend(AgentId from,
    NotificationTopicSend not) throws Exception
  { 
    try {
      TopicNaming topic = (TopicNaming) not.msg.getJMSDestination();
     
      /* De-activated  
      Theme theme = searchTheme(topic.getTheme());
      if (theme == null)
        throw (new MOMException("No existing Theme", MOMException.THEME_NO_EXIST));
      */
	
      if(checkMessage(not.msg)) {

        // Delivering the message to root.
        Theme themeNode = rootTheme;
        deliveryTopicMessage(themeNode, not.msg, from);

        /* De-activated
        StringTokenizer st = new StringTokenizer(topic.getTheme(), "/", false);
        st.nextToken();
        while (st.hasMoreTokens()) {
          if(Debug.debug)
            if(Debug.topicSend && (not.msg instanceof TextMessage))
              System.out.println("Ancester: " +
                ((fr.dyade.aaa.mom.TextMessage)not.msg).getText());

          themeNode = (Theme) themeNode.getThemeDaughter(st.nextToken());
          deliveryTopicMessage(themeNode, not.msg, from);
        }
        */
      }

      // Delivering an agreement to the client if message is persistent.
      //if (not.msg.getJMSDeliveryMode() == Message.PERSISTENT)
        //deliveryAgreement(from, not);

    } catch (MOMException exc) { 
      deliveryException (from, not, exc);
    } 
  }

 
  /**
   * Method reacting to a <code>NotificationSubscription</code> 
   * wrapping a client subscription.
   */
  protected void notificationSubscription(AgentId from, 
    NotificationSubscription not) throws Exception 
  { 
    try {

      fr.dyade.aaa.mom.Theme theme = rootTheme;
      /* De-activated
      fr.dyade.aaa.mom.Theme theme = searchTheme(not.theme);
      if (theme == null)
        throw (new MOMException("Subscribe : No existing Theme",
          MOMException.THEME_NO_EXIST));
      */

      fr.dyade.aaa.mom.TopicSubscription sub = 
        new fr.dyade.aaa.mom.TopicSubscription(not.noLocal, not.selector, 
        from, not.nameSubscription, not.driverKey);

      theme.addSubscription(from, not.nameSubscription, sub, not.driverKey, not.durable);

      // Delivering an agreement to the client.
      deliveryAgreement(from, not);

    } catch (MOMException exc) { 
      deliveryException (from, not, exc);
    }
  }

  /**
   * Method reacting to a <code>NotificationUnsubscription</code> 
   * wrapping a client unsubscription.
   */
  protected void notificationUnsubscription(AgentId from,
    NotificationUnsubscription not) throws Exception
  { 
    try {
      Theme theme = rootTheme;
      /* De-activated
      fr.dyade.aaa.mom.Theme theme = searchTheme(not.theme);
      if(theme==null)
        throw (new MOMException("Unsubscribe : No existing such Theme",
          MOMException.THEME_NO_EXIST));
      */
		
      if (theme.removeSubscription(from, not.nameSubscription, not.driverKey) == null)
        throw (new MOMException("Unsubscribe : subscription does not exist",
          MOMException.SUBSCRIPTION_NO_EXIST));
 			
      // Delivering an agreement to the client.
      deliveryAgreement(from, not);

    } catch (MOMException exc) { 
      deliveryException (from, not, exc);
    }
  }

 
  /**
   * Method reacting to a <code>NotificationUpdateSubscription</code> 
   * for updating a subscription.
   */
  protected void notificationUpdateSubscription(AgentId from,
    NotificationUpdateSubscription not)
  {
    try {
      fr.dyade.aaa.mom.Theme theme = rootTheme;
      /* De-activated
      fr.dyade.aaa.mom.Theme theme = searchTheme(not.theme);
      if(theme==null)
        throw (new MOMException("Update : No existing such Theme",MOMException.THEME_NO_EXIST));
      */

      fr.dyade.aaa.mom.TopicSubscription sub;
      if ((sub = theme.getSubscription(from, not.nameSubscription, not.driverKey)) == null)
        throw (new MOMException("Update : subscription does not exist",
          MOMException.SUBSCRIPTION_NO_EXIST));
      else
        sub.updateSubscription(not.noLocal, not.selector);
  
      // Delivering an agreement to the client.
      deliveryAgreement(from, not);

    } catch (MOMException exc) { 
      deliveryException (from, not, exc);
    }
  }

 
  /**
   * Method sending a message to all the subscribers to a <code>Theme</code>.
   */
  protected void deliveryTopicMessage(fr.dyade.aaa.mom.Theme theme,
    fr.dyade.aaa.mom.Message msg, AgentId from) throws Exception
  {
    // Getting all the subscriptions to the Theme
    Enumeration subs = theme.getAllSubscriptions();

    fr.dyade.aaa.mom.Selector selecObj = new fr.dyade.aaa.mom.Selector();

    while (subs.hasMoreElements()) {
      fr.dyade.aaa.mom.TopicSubscription sub =
        (fr.dyade.aaa.mom.TopicSubscription) subs.nextElement();

      // Connection key for the current subscription
      int drvKey = sub.getDriverKey();
   

      // Checking the noLocal and agentClient attributes and the selector.
      if ((!sub.getNoLocal() || !(from.equals(sub.getAgentClient())))
        && selecObj.isAvailable(msg, sub.getSelector())) {

        // Delivering the message
        NotifMessageFromTopic notMsgDeliv =
          new NotifMessageFromTopic(sub.getSubscriptionName(),
          msg, theme.getAbsoluteNameTheme(), drvKey);

        sendTo(sub.getAgentClient(), notMsgDeliv);
      }
    }
  }


  //////////////////////////////////////////////////////////////
  // ALL FOLLOWING METHODS HAVE BEEN IMPLEMENTED FOR TOPICS
  // WITH A TREE ARCHITECTURE MADE OF THEMES. THEY ARE CURRENTLY
  // NOT USED. 
  //////////////////////////////////////////////////////////////
  /**
   * Method retrieving a <code>Theme</code> given its name.
   */
  protected fr.dyade.aaa.mom.Theme searchTheme(String nameTheme)
  {
    StringTokenizer st = new StringTokenizer(nameTheme, "/", false);
    fr.dyade.aaa.mom.Theme themeNode = rootTheme;
    String nameNode;		
    String DaughterNode;
    int separator;
		
    // begining case 
    nameNode = st.nextToken();
    if (!st.hasMoreTokens()) {
      // Topic leaf 
      if (nameNode.equals(rootTheme.getNameTheme()))
        return rootTheme;
      else 
        return null;
    }

    while (true) {
      if (!st.hasMoreTokens()) {
        // Topic leaf 
       if (nameNode.equals(themeNode.getNameTheme()))
         return themeNode;
       else
         return null;
      } else {
        // directory 
        DaughterNode = st.nextToken();
        if ((themeNode = themeNode.getThemeDaughter(DaughterNode)) != null) {
          // a new level
          nameNode = DaughterNode;
        } else {
          // No theme Found
          return null;
        }
      }
    }
  }
	
	
	/**	class to take the subdirectories themes one after one  
	 *	lexical analysis 
     */	 
	private class Token {

		transient StringTokenizer st;

		public Token(String line) {
			st = new StringTokenizer(line," \t\n\r;:",true);
		}

		public String get() {
			String tok;
			while (st.hasMoreTokens()) {
				tok = st.nextToken();
				if ((tok.equals(" ")) || (tok.equals("\t")))
					continue;
				return tok;
			}
			return ("");
		}
	}

	
	/** constructs a subject-Theme tree 
	  . subdirectories ; ./directory subdirectories; ./.../directory subdirectories ... ;
    */	 
	public void constructTheme(String line) throws Exception {
		fr.dyade.aaa.mom.Theme themeNode ;
		Token token = new Token(line);
		String tok;
		
		while(!(tok = token.get()).equals("")) {
			
			if(Debug.debug)
				if(Debug.topicTheme)
					System.out.println("tok "+tok);
			
			// find the directory 
			themeNode = searchTheme(tok);
			if(themeNode==null)
				throw (new Exception("Erreur Constuctor Theme Tree"));
			// subdirectories 
			while(!(tok = token.get()).equals(";")) {
				// add a Theme subdirectory 
				themeNode.addThemeDaughter(tok, new Theme(tok,themeNode.getAbsoluteNameTheme()+"/"+tok));
			}
		}
		
		if(Debug.debug)
			if(Debug.topicTheme)
				Debug.printThemeTree(rootTheme,"0");
	}
    
	
}
