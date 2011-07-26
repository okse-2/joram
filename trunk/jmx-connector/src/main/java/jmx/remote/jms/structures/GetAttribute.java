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

package jmx.remote.jms.structures;

import java.io.Serializable;

import javax.management.ObjectName;

/**
 * <b>GetAttribute</b> is the object that is sent by a requestor who wishes to
 * appeal JMX getAttribute(ObjectName n, String a).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 * 
 */
public class GetAttribute implements Serializable {
  public ObjectName name;
  public String attributes;

  public GetAttribute(ObjectName n, String a) {
    name = n;
    attributes = a;
  }

}
