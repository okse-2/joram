/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Jeff Mesnil (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.messages;

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
