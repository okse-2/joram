/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.common;


/**
 * This class is used to register an AgentServer service as an OSGi service. It
 * is needed because AgentServer services are accessed with static methods and
 * are not instantiable.
 */
public class Service {

  /**
   * This property must be added when registering an A3 service to specify the
   * class name of the registered service. Typically, the following is done to
   * register an A3 service in the OSGi world :<br>
   * <br>
   * <code>
   * Properties props = new Properties();<br>
   * props.put(Service.SERVICE_NAME_PROP, MyService.class.getName());<br>
   * bundleContext.registerService(Service.class.getName(), new Service(), props);
   * </code>
   */
  public static final String SERVICE_NAME_PROP = "A3ServiceClass";

}
