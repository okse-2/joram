/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2005 Bull SA
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.client.connector.utils;

import java.io.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;


/**
 * XML Wrapper.
 */
public class Wrapper {
  private boolean debug = false;
  private Map map = null;

  public Wrapper(boolean debug) {
    this.debug = debug;
  }

  /**
   *
   * @param reader   Reader
   * @exception Exception  unspecialized error
   */
  public String parse(InputStream in) throws Exception {
    StringBuffer buff = new StringBuffer();
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(in);

    NodeList nl = doc.getElementsByTagName("resourceadapter");
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      buff.append(explore(node));
    }
    return buff.toString();
  }

  public String explore(Node node) throws Exception {
//      if (debug)
//        System.out.println("Wrapper.explore(" + node + ")");

    StringBuffer buff = new StringBuffer();
    NodeList nl = node.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      if (n.getNodeName().equals("config-property")) {
        buff.append(extractPropertyInfo(n.getChildNodes()));
      } else {
        if (n.getNodeName().equals("resourceadapter-class")
            || n.getNodeName().equals("managedconnectionfactory-class")) {
          String nodeName = n.getFirstChild().getNodeValue();
          buff.append("[");
          buff.append(nodeName);
          buff.append("]");
          buff.append("\n");
        }
        buff.append(explore(n));
      }
    }
    return buff.toString();
  }

  public String extractPropertyInfo(NodeList nodeList) throws Exception {
    String name = null;
    String value = null;
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node n = nodeList.item(i);
      if (n.getNodeName().equals("config-property-name")) {
        name = n.getFirstChild().getNodeValue();
      } else if (n.getNodeName().equals("config-property-value")) {
        value = n.getFirstChild().getNodeValue();
      }
    }

    StringBuffer buff = new StringBuffer();
    buff.append("  ");
    buff.append(name);
    buff.append("\t");
    if (value != null)
      buff.append(value);
    buff.append("\n");
    return buff.toString();
  }


 /**
   *
   * @param reader   Reader
   * @param map      update by this properties
   * @exception Exception  unspecialized error
   */
  public String update(InputStream in, Map map) throws Exception {
    this.map = map;
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(in);

    NodeList nl = doc.getElementsByTagName("resourceadapter");
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      browse(node);
    }

    Transformer transformer =
      TransformerFactory.newInstance().newTransformer();
    Source source = new DOMSource(doc);

    CharArrayWriter writer = new CharArrayWriter();
    Result output = new StreamResult(writer);
    transformer.transform(source, output);
    writer.flush();

    return writer.toString();
  }

  public void browse(Node node) throws Exception {
    if (debug)
      System.out.println("Wrapper.browse(" + node + ")");
    NodeList nl = node.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      String nodeValue = n.getNodeValue();
      if (nodeValue != null && map.containsKey(nodeValue)) {
        if (debug)
          System.out.println("NodeName=" + nodeValue);
        explore(n.getParentNode().getParentNode(),nodeValue);
      } else if (nodeValue != null 
                 && !nodeValue.equals("config-property")) {
        continue;
      } else {
        browse(n);
      }
    }
  }

  public void explore(Node node, String key) throws Exception {
    if (debug)
      System.out.println(">>>> Wrapper.explore(" + node + "," + key + ")");
    NodeList nl = node.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      String nodeName = n.getNodeName();
      if (nodeName.equals("config-property")) {
        setPropertiesValue(n.getChildNodes(),key);
      } else if (nodeName.equals("outbound-resourceadapter")
                 || nodeName.equals("inbound-resourceadapter")) {
        continue;
      } else {
        explore(n,key);
      }
    }
    if (debug)
      System.out.println("<<<<<< Wrapper.explore node = " + node);
  }


  public void setPropertiesValue(NodeList nodeList, String key) throws Exception {
    if (debug)
      System.out.println("Wrapper.setPropertiesValue(" + nodeList + "," + key + ")");
    String name = null;
    String value = null;
    Map prop = (Map) map.get(key);
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node n = nodeList.item(i);
      if (n.getNodeName().equals("config-property-name")) {
        name = n.getFirstChild().getNodeValue();
      } else if (n.getNodeName().equals("config-property-value")
                 && prop.containsKey(name)) {
        n.getFirstChild().setNodeValue((String) prop.get(name));
      }
    }
    if (debug)
      System.out.println("Wrapper.setPropertiesValue name=" + name + ", value=" + value);
  }
}
