/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */

package fr.dyade.aaa.ip;

import java.net.*;
import fr.dyade.aaa.agent.*;

/**
 * Notification used to notify a new connection.
 *
 * @author	Freyssinet Andre
 * @version	v1.0
 *
 * @see		TcpMultiServerProxy
 */

public class ConnectNot extends Notification {
public static final String RCS_VERSION="@(#)$Id: ConnectNot.java,v 1.13 2003-09-11 09:53:50 fmaistre Exp $";
  Integer key;
  String header;

  /**
   * Constructor.
   *   
   * @param key connected socket key.
   */
  public ConnectNot(Integer key) {
    this.key = key;
  }

  /**
   * Constructor.
   * 
   * @param key connected socket key.
   * @param header received header after accepting the connection.
   */
  public ConnectNot(Integer key, String header) {
    this(key);
    this.header = header;
  }  


  /**
   * Provides a string image for this object.
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    output.append(super.toString());
    output.append(",key=");
    output.append(key);
    output.append(",header=");
    output.append(header);
    output.append(")");
    return output.toString();
  }
}
