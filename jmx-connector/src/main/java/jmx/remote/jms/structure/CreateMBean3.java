/**
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

package jmx.remote.jms.structure;

import java.io.Serializable;

import javax.management.ObjectName;

/**
 * <b>CreateMBean3</b> is the object that is sent by a requestor who wishes to
 * appeal JMX createMBean(String className, ObjectName name,ObjectName
 * loaderName,Object[] parametres,String[] signature)
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 * 
 */
public class CreateMBean3 extends CreateMBean implements Serializable {
  public ObjectName loaderName;
  public Object[] parametres;
  public String[] signature;

  public CreateMBean3(String className, ObjectName name, ObjectName loaderName, Object[] parametres,
      String[] signature) {
    super(className, name);
    this.loaderName = loaderName;
    this.parametres = parametres;
    this.signature = signature;
    // TODO Auto-generated constructor stub
  }
}
