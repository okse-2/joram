/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.widget.record;

import com.scalagent.appli.shared.UserWTO;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * @author Yohann CINTRE
 */
public class UserListRecord extends ListGridRecord {

	public static String ATTRIBUTE_NAME = "name";
	public static String ATTRIBUTE_PERIOD = "period";
	public static String ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION = "nbMsgsSentToDMQSinceCreation";
	public static String ATTRIBUTE_SUBSCRIPTIONNAMES = "subscriptionNames";
	
	private UserWTO user;
	
	public UserListRecord() {}
	public UserListRecord(UserWTO user) {
		super();
		
		setName(user.getName());
		setPeriod((int) user.getPeriod());
		setNbMsgsSentToDMQSinceCreation((int) user.getNbMsgsSentToDMQSinceCreation());
		setSubscriptionNames(user.getSubscriptionNames());
		
		this.user = user;
	}
	

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("name: "+getAttribute(ATTRIBUTE_NAME));
		
		return buffer.toString();
	}
	public String toStringAllContent() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("name: "+getAttribute(ATTRIBUTE_NAME));
		buffer.append("; period: "+getAttribute(ATTRIBUTE_PERIOD));
		buffer.append("; nbMsgsSentToDMQSinceCreation: "+getAttribute(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION));
		buffer.append("; DMQsubscriptionNamesid: "+getAttribute(ATTRIBUTE_SUBSCRIPTIONNAMES));
		return buffer.toString();
	}
	
	
	public UserWTO getUser() { return user; }
	

	public void setUser(UserWTO user) { this.user = user; }
	public void setName(String name) { setAttribute(ATTRIBUTE_NAME, name); }
	public void setPeriod(int period) { setAttribute(ATTRIBUTE_PERIOD, period); }
	public void setNbMsgsSentToDMQSinceCreation(int nbMsgsSentToDMQSinceCreation) { setAttribute(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, nbMsgsSentToDMQSinceCreation); }
	public void setSubscriptionNames(String[] subscriptionNames) { setAttribute(ATTRIBUTE_SUBSCRIPTIONNAMES, subscriptionNames); }
	
	public String getName() { return getAttributeAsString(ATTRIBUTE_NAME); }
    public long getNbMsgsSentToDMQSinceCreation() { return getAttributeAsInt(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION); }
    public long getPeriod() { return getAttributeAsInt(ATTRIBUTE_PERIOD); }
    public void getNbMsgsSentToDMQSinceCreation(int nbMsgsSentToDMQSinceCreation) { setAttribute(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, nbMsgsSentToDMQSinceCreation); }
	public void getSubscriptionNames(String[] subscriptionNames) { setAttribute(ATTRIBUTE_SUBSCRIPTIONNAMES, subscriptionNames); }
	
	 	
	public String getAllAtt() {
		String[] allatt = getAttributes();
		String ret = "";
		for(int i=0; i<allatt.length; i++)
			ret = ret+allatt[i]+" / ";
		return ret;
	}

}