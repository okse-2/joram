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

package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;
import java.text.ParseException;

import fr.dyade.aaa.util.*;

/**
 * AgentServer
 * @version	v1.3, 23 Jan 97
 * @author	Freyssinet Andr*
 * @see		Server
 */
public class AgentServer {

public static final String RCS_VERSION="@(#)$Id: AgentServer.java,v 1.3 2000-10-05 15:15:19 tachkeni Exp $"; 


  /**
   * Main for a standard agent server.
   * The start arguments include in first position the identifier of the
   * agent server to start, and in second position the directory name where
   * the agent server stores its persistent data.
   *
   * @param args	start arguments
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static void main (String args[]) throws Exception {
    try {
      Server.init(args);
      Server.start();
    } catch (ParseException exc) {
      System.err.println(exc.getMessage());
      System.exit(1);
    } catch (Exception exc) {
      System.err.println(exc.toString());
      exc.printStackTrace();
      System.exit(1);
    }
  }
}
