/*
 *  JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 - 2014 ScalAgent Distributed Technologies
 * Copyright (C) 2013 - 2014 Université Joseph Fourier
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
 * Initial developer(s): Université Joseph Fourier
 * Contributor(s): ScalAgent Distributed Technologies
 */
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
