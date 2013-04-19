/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.util;

import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.messages.MessageBodyTxId;
import org.objectweb.joram.mom.messages.MessageTxId;
import org.objectweb.joram.mom.notifications.ReceiveRequest;
import org.objectweb.joram.mom.proxies.ClientContext;
import org.objectweb.joram.mom.proxies.ClientContextTxId;
import org.objectweb.joram.mom.proxies.ClientSubscription;
import org.objectweb.joram.mom.proxies.ClientSubscriptionTxId;
import org.objectweb.joram.mom.proxies.UserAgent;

import fr.dyade.aaa.common.encoding.EncodableFactoryRepository;

//JORAM_PERF_BRANCH
public class JoramEncodableFactoryInit {
  
  public static void init() {
    EncodableFactoryRepository.putFactory(JoramHelper.CLIENTCONTEXT_CLASS_ID, new ClientContext.ClientContextFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.CLIENTSUBSCRIPTION_CLASS_ID, new ClientSubscription.ClientSubscriptionFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.QUEUE_CLASS_ID, new Queue.QueueFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.RECEIVEREQUEST_CLASS_ID, new ReceiveRequest.ReceiveRequestFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.USERAGENT_CLASS_ID, new UserAgent.UserAgentFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.MESSAGE_CLASS_ID, new Message.MessageFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.MESSAGETXID_CLASS_ID, new MessageTxId.MessageTxIdFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.MESSAGEBODYTXID_CLASS_ID, new MessageBodyTxId.MessageBodyTxIdFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.CLIENTCONTEXTTXID_CLASS_ID, new ClientContextTxId.ClientContextTxIdEncodableFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.CLIENTSUBSCRIPTIONTXID_CLASS_ID, new ClientSubscriptionTxId.ClientSubscriptionTxIdEncodableFactory());
  }

}
