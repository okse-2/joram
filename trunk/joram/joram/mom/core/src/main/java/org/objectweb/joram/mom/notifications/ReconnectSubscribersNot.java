package org.objectweb.joram.mom.notifications;

import java.util.ArrayList;

import org.objectweb.joram.shared.messages.Message;

import fr.dyade.aaa.agent.Notification;

/**
 * Used by ElasticTopic
 * 
 * @author Ahmed El Rheddane
 *
 */
public class ReconnectSubscribersNot extends Notification {
	private static final long serialVersionUID = 1L;
	
	private String subName;
	
	ArrayList<Integer> subs;
	ArrayList<Message> msgs;
	
	public ReconnectSubscribersNot(String subName, Message msg) {
		this.subName = subName;
		msgs = new ArrayList<Message>();
		msgs.add(msg);
	}
	
	public ReconnectSubscribersNot(ArrayList<Integer> subs, ArrayList<Message> msgs) {
		this.subs = subs;
		this.msgs = msgs;
	}
	
	public String getSubName() {
		return subName;
	}
	
	public ArrayList<Integer> getSubs() {
		return subs;
	}
	
	public ArrayList<Message> getMsgs() {
		return msgs;
	}
}
