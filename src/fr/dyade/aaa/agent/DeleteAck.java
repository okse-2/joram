/*
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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
 * DeleteAck - Acknowledge for deleting an agent
 */
public class DeleteAck extends Notification { 

  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  public AgentId agent = null;

  public static final int OK = 0;
  public static final int DENIED = 1;
  public static final int EXCEPTION = 2;

  public int status = OK;

  public Object extraInformation;

  public Throwable exc;

  public DeleteAck(AgentId agent) {
    this.status = OK;
    this.agent = agent;
  }

  public DeleteAck(AgentId agent, Object extraInformation) {
    this.status = OK;
    this.agent = agent;
    this.extraInformation = extraInformation;
  }

  public DeleteAck(AgentId agent, Throwable exc) {
    this.status = EXCEPTION;
    this.agent = agent;
    this.exc = exc;
  }

  public DeleteAck(AgentId agent, int status) {
    this.status = status;
    this.agent = agent;
  }
}
