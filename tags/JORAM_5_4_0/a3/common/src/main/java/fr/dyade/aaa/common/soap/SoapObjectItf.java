/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package fr.dyade.aaa.common.soap;

import java.util.Hashtable;


/**
 * The <code>SoapObjectItf</code> interface must be implemented by any
 * object which may be registered and/or retrieved to/from JNDI through the
 * SOAP protocol.
 */
public interface SoapObjectItf {
  /**
   * Method coding the <code>SoapObjectItf</code> object into a Hashtable
   * transportable by the SOAP procotol.
   */
  public Hashtable code();

  /**
   * Initializes a <code>SoapObjectItf</code> object given a coded Hashtable.
   */
  public void decode(Hashtable h);
}
