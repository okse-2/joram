/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package org.objectweb.joram.shared.excepts;

import org.objectweb.joram.shared.client.MomExceptionReply;

/**
 * The <code>MomException</code> class is the exception class thrown by the
 * MOM actors.
 */
public class MomException extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  protected int type;

  public MomException(int type, String info) {
    super(info);
    this.type = type;
  }

  /**
   * Constructs a <code>MomException</code> containing a given description.
   */
  public MomException(String info) {
    super(info);
    type = MomExceptionReply.MomException;
  }

  public int getType() {
    return type;
  }
}
