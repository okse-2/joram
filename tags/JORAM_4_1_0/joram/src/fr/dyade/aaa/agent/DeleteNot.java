/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.agent;

/**
 * This notification is used to ask aaa agent destruction
 * @author  Noel De Palma
 * @version 1.0, 7/10/98
 */
public class DeleteNot extends Notification {

  /**
   * the agent identified by 'reply' will receive a DeleteAck
   * when destruction complete
   */
  public AgentId reply;
 
  public DeleteNot() {
    this(null);
  }

  public DeleteNot(AgentId reply) {
    super();
    this.reply = reply;
  }
}

