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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, fr.dyade.aaa.ns,
 * fr.dyade.aaa.jndi and fr.dyade.aaa.joram, released September 11, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */


package fr.dyade.aaa.jndi;

import javax.naming.spi.*;
import javax.naming.*;
import java.util.*;

public class NamingContextFactory implements InitialContextFactory {
  /**
   *  This property which defines the listener port must be passed
   *  when creating an initial context using this factory.
   */
  public final static String PORT_PROPERTY = "java.naming.factory.port";
  /**
   *  This property which defines the host name must be passed
   *  when creating an initial context using this factory.
   */
  public final static String HOST_PROPERTY = "java.naming.factory.host";
  
  /**
   *@param  env  This contains the hostname and the port.
   *@return  A JNDI initial context.
   *@exception  NamingException  Thrown if the host and port properties 
   * aren't strings, if the port string does not represent a valid number, 
   * or if an exception is thrown from the NamingContext constructor.
   */
  public Context getInitialContext(Hashtable env)
	throws NamingException {
	try {
	  String host = (String) env.get(HOST_PROPERTY);	  
	  if (host == null) {
		host = "localhost";//default host
	  }
	  String portStr = (String) env.get(PORT_PROPERTY);
	  if (portStr == null) {
		portStr = "16400";//default port
	  }
	  int port = Integer.parseInt(portStr);
	  return new fr.dyade.aaa.jndi.NamingContext(host, port);
	}
	catch (ClassCastException e) {
	  NamingException nx = 
		new NamingException("ClassCastException!  Are " + 
							HOST_PROPERTY + " and " + 
							PORT_PROPERTY + " String objects?");
	  nx.setRootCause(e);
	  throw nx;
	}
	catch (NumberFormatException e) {
	  NamingException nx = 
		new NamingException("the " + PORT_PROPERTY + 
							" is not a valid integer");
	  nx.setRootCause(e);
	  throw nx;
	}
	catch (Exception e) {
	  NamingException nx = 
		new NamingException("exception creating NamingContext: " +
							e.toString());
	  nx.setRootCause(e);
	  throw nx;
	}
  }
}
