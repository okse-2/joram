/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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
public static final String RCS_VERSION="@(#)$Id: ConnectNot.java,v 1.9 2002-10-21 08:41:13 maistrfr Exp $";
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
