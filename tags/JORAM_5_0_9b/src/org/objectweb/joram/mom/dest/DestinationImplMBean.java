/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2005 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.joram.mom.dest;

public interface DestinationImplMBean {
  /**
   * Returns a string representation of this destination.
   */
  String toString();

  /**
   * Returns the unique identifier of the destination.
   *
   * @return the unique identifier of the destination.
   */
  String getDestinationId();

  /**
   * Tests if this destination is free for reading.
   *
   * @return true if anyone can receive messages from this destination;
   * 	     false otherwise.
   */
  boolean isFreeReading();

  /**
   * Sets the <code>FreeReading</code> attribute for this destination.
   *
   * @param on	if true anyone can receive message from this destination.
   */
  void setFreeReading(boolean on);

  /**
   * Tests if this destination is free for writing.
   *
   * @return true if anyone can send messages to this destination;
   * 	     false otherwise.
   */
  boolean isFreeWriting();

  /**
   * Sets the <code>FreeWriting</code> attribute for this destination.
   *
   * @param on	if true anyone can send message to this destination.
   */
  void setFreeWriting(boolean on);

  /**
   * Returns a string representation of all rights set on this destination.
   *
   * @return the rights set on this destination.
   */
  String[] getRights();

  /**
   * Returns a string representation of rights set on this destination for a
   * particular user. The user is pointed out by its unique identifier.
   *
   * @param userid The user's unique identifier.
   * @return the rights set on this destination.
   */
  String getRight(String userid);
//   void setRight(String userid, String right);

  /**
   * Return the unique identifier of DMQ set for this destnation if any.
   *
   * @return the unique identifier of DMQ set for this destnation if any;
   *	     null otherwise.
   */
  String getDMQId();

  /**
   * Returns this destination creation time as a long.
   *
   * @return the destination creation time as UTC milliseconds from the epoch.
   */
  long getCreationTimeInMillis();

  /**
   * Returns this destination creation time through a <code>String</code> of
   * the form: <code>dow mon dd hh:mm:ss zzz yyyy</code>.
   *
   * @return the destination creation time.
   */
  String getCreationDate();

  /**
   * Returns the number of messages received since creation time of this
   * destination.
   *
   * @return the number of messages received since creation time.
   */
  long getNbMsgsReceiveSinceCreation();

  /**
   * Returns the number of messages delivered since creation time of this
   * destination. It includes messages all delivered messages to a consumer,
   * already acknowledged or not.
   *
   * @return the number of messages delivered since creation time.
   */
  long getNbMsgsDeliverSinceCreation();

  /**
   * Returns the number of erroneous messages forwarded to the DMQ since
   * creation time of this destination..
   *
   * @return the number of erroneous messages forwarded to the DMQ.
   */
  long getNbMsgsSentToDMQSinceCreation();
}
