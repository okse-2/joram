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
 * Initial developer(s): Djamel-Eddine Boumchedda
 * 
 */
package org.ow2.joram.jmxconnector.shared;

import java.io.Serializable;

import javax.management.AttributeList;
import javax.management.ObjectName;
/**
 * <b>SetAttributes</b> is the object that is sent by a requestor who wishes to
 * appeal JMX setAttributes(ObjectName name, AttributeList attributes).
 * 
 * @author Djamel-Eddine Boumchedda
 */
public class SetAttributes implements Serializable {
  public ObjectName name;
  public AttributeList attributes;

  public SetAttributes(ObjectName name, AttributeList attributes) {
    this.name = name;
    this.attributes = attributes;
  }
}
