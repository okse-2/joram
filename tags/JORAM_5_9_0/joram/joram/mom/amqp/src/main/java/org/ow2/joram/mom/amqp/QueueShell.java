/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2011 ScalAgent Distributed Technologies
 * Copyright (C) 2009 CNES
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
package org.ow2.joram.mom.amqp;

public class QueueShell {

  private Queue queue;

  private String queueName;

  public QueueShell(Queue queue) {
    this.queue = queue;
  }

  public QueueShell(String queueName) {
    this.queueName = queueName;
  }

  public boolean islocal() {
    return queue != null;
  }

  public Queue getReference() {
    return queue;
  }
  
  public String getName() {
    return queueName;
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof QueueShell)) {
      return false;
    }
    QueueShell other = (QueueShell) obj;
    if (other.islocal() != islocal()) {
      return false;
    }
    if (islocal()) {
      return queue == other.queue;
    }
    return queueName.equals(other.queueName);
  }

  public int hashCode() {
    if (islocal())
      return queue.hashCode();
    else
      return queueName.hashCode();
  }

  public String toString() {
    if (islocal()) {
      return queue.getName() + "(local)";
    }
    return queueName + "(distant)";
  }

}
