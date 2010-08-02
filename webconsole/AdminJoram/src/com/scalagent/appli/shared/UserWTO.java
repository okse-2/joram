/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.shared;

import java.util.Arrays;

import com.scalagent.engine.shared.BaseWTO;

/**
 * @author Yohann CINTRE
 */
public class UserWTO extends BaseWTO {

	private String name;
	private long period;
	private long nbMsgsSentToDMQSinceCreation;
	private String[] subscriptionNames;

	public String getName() { return name; }
	public long getPeriod() { return period; }
	public long getNbMsgsSentToDMQSinceCreation() { return nbMsgsSentToDMQSinceCreation; }
	public String[] getSubscriptionNames() { return subscriptionNames; }

	public void setName(String name) { this.name = name; }
	public void setPeriod(long period) { this.period = period; }
	public void setNbMsgsSentToDMQSinceCreation(long nbMsgsSentToDMQSinceCreation) { this.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation; }
	public void setSubscriptionNames(String[] subscriptionNames) { this.subscriptionNames = subscriptionNames; }

	public UserWTO() {}
	public UserWTO(String name, long period, long nbMsgsSentToDMQSinceCreation, String[] subscriptionNames) {
		super();
		this.id = name;
		this.name = name;
		this.period = period;
		this.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation;
		this.subscriptionNames = subscriptionNames;
	}


	public String toStringFullContent() {
		return "UserWTO [name=" + name + ", nbMsgsSentToDMQSinceCreation="
				+ nbMsgsSentToDMQSinceCreation + ", period=" + period
				+ ", subscriptionNames=" + Arrays.toString(subscriptionNames)
				+ ", id=" + id + "]";
	}
	
	@Override
	public String toString() {
		return "UserWTO [name=" + name + ", subscriptionNames=" + Arrays.toString(subscriptionNames) + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime
		* result
		+ (int) (nbMsgsSentToDMQSinceCreation ^ (nbMsgsSentToDMQSinceCreation >>> 32));
		result = prime * result + (int) (period ^ (period >>> 32));
		result = prime * result + Arrays.hashCode(subscriptionNames);
		return result;
	}



	@Override
	public boolean equals(Object anObj){
		if(anObj==null)
			return false;
		if(anObj==this)
			return true;
		if(!(anObj instanceof UserWTO))
			return false;
		UserWTO obj = (UserWTO)anObj;
		if(obj.id.equals(this.id))
			return true;
		return false;
	}

	@Override
	public UserWTO clone() {

		UserWTO usr = new UserWTO();

		usr.id = id;
		usr.id = name;
		usr.name = name;
		usr.period = period;
		usr.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation;
		usr.subscriptionNames = subscriptionNames;

		return usr;
	}

	@Override
	public boolean equalsContent(Object anObj) {

		if(!equals(anObj))
			return false;

		UserWTO obj = (UserWTO)anObj;

		boolean eq =  
			equalsWithNull(this.id, obj.id)
			&& equalsWithNull(this.name, obj.name)
			&& equalsWithNull(this.period, obj.period)
			&& equalsWithNull(this.nbMsgsSentToDMQSinceCreation, obj.nbMsgsSentToDMQSinceCreation)
			&& equalsWithNull(Arrays.asList(subscriptionNames), Arrays.asList(obj.subscriptionNames));

		return eq;
	}
}