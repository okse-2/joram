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
import java.util.*;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The class <code>A3CMLDomain</code> describes an agent server domain read
 * from the A3CML configuration file.
 */
public class A3CMLDomain {
  /** Name of the domain. */
  public String name = null;
  /** Full name of Java class */
  public String network = null;
  /** Description of alls persistent servers in domain */
  public Vector servers = null;
  /**
   * Server Id. of router (1st hop) to access this domain from current node,
   * if -1 the domain is directly accessible.
   */
  public short gateway = -1;

  A3CMLDomain(Attributes atts) throws SAXException {
    name = atts.getValue(A3CMLHandler.ATT_NAME);
    if (name.equals("transient") || name.equals("local"))
      throw new SAXException("Domain name \"" + name + "\" is reserved.");
    network = atts.getValue(A3CMLHandler.ATT_NETWORK);
    if ((network == null) || network.equals(""))
      network = "fr.dyade.aaa.agent.SingleCnxNetwork";
  }
  
  void addServer(A3CMLPServer server) {
    if (servers == null)
      servers = new Vector();
    servers.addElement(server);
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("Domain \"").append(name);
    strBuf.append("(").append(network).append(")");
    strBuf.append("\"=[").append(servers).append(", ");
    strBuf.append(gateway).append("]");

    return strBuf.toString();
  }
}
