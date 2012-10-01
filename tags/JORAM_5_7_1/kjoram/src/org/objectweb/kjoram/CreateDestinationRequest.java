/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

import java.io.IOException;

/**
 * A <code>CreateDestinationRequest</code> instance requests the creation of a
 * destination on a given server.
 */
public class CreateDestinationRequest extends AdminRequest {
  private static final long serialVersionUID = 1L;

  /** Id of the server where deploying the destination. */
  private int serverId;

  /** Name attributed to the destination. */
  private String name;

  /** Name of the class to be instantiated. */
  private String className;
 
  /** Properties needed to create destination object. */
  private Properties props;

  private String expectedType;

  /**
   * Constructs a <code>CreateDestinationRequest</code> instance.
   *
   * @param serverId   The id of the server where deploying the destination.
   * @param name  Name attributed to the destination.
   * @param className  Name of the class to be instantiated.
   */
  public CreateDestinationRequest(int serverId,
                                  String name,
                                  String className,
                                  Properties props,
                                  String expectedType) {
    this.serverId = serverId;
    this.name = name;
    this.className = className;
    this.props = props;
    this.expectedType = expectedType;
  }

  public CreateDestinationRequest() { }
  
  /** Returns the id of the server where deploying the destination. */
  public final int getServerId() {
    return serverId;
  }

  /** Returns the name attributed to the destination. */
  public final String getDestinationName() {
    return name;
  }

  /** Returns the class name of destination (queue, topic, ...). */
  public final String getClassName() {
    return className;
  }

  /** Returns the destination properties. */
  public final Properties getProperties() {
    return props;
  }

  public final String getExpectedType() {
    return expectedType;
  }
  
  protected int getClassId() {
    return CREATE_DESTINATION_REQUEST;
  }
  
  public void readFrom(InputXStream is) throws IOException {
    serverId = is.readInt();
    name = is.readString();
    className = is.readString();
    props = is.readProperties();
    expectedType = is.readString();
  }

  public void writeTo(OutputXStream os) throws IOException {
    os.writeInt(serverId);
    os.writeString(name);
    os.writeString(className);
    os.writeProperties(props);
    os.writeString(expectedType);
  }
}
