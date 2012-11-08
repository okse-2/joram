/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package org.objectweb.joram.mom.proxies;

import org.objectweb.joram.shared.client.AbstractJmsReply;


/**
 * A <code>SyncReply</code> is a notification used by a proxy for
 * synchronizing a reply.
 */
class SyncReply extends fr.dyade.aaa.agent.Notification
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * The identifier of the client context within which which the reply will
   * have to be sent.
   */
  int key;
  /** The reply to send. */
  AbstractJmsReply reply;

  /**
   * Constructs a <code>SyncReply</code> instance.
   *
   * @param key  The identifier of the client context within which which the
   *          reply will have to be sent.
   * @param reply  The reply to send.
   */
  SyncReply(int key, AbstractJmsReply reply)
  {
    this.reply = reply;
    this.key = key;
  }
}
