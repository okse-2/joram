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
 * Initial developer(s): Jeff Mesnil (INRIA)
 * Contributor(s):
 */
package org.objectweb.joram.shared.messages;

/**
 * The <code>MessageType</code> interface defines the various types of
 * MOM messages.
 * <p>
 * MOM messages are defined by the type of data they actually carry.
 */
public interface MessageType
{
  /** A simple message carries an empty body. */
  public static final int SIMPLE = 0;

  /** A text message carries a String body. */
  public static final int TEXT = 1;

  /** An object message carries a serializable object. */
  public static final int OBJECT = 2;

  /** A map message carries an hashtable. */
  public static final int MAP = 3;

  /** A stream message carries a bytes stream. */
  public static final int STREAM = 4;

  /** A bytes message carries an array of bytes. */
  public static final int BYTES = 5;
}
