/*
 * Copyright (C) 2000 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

// TODO : allow to have only a transient server in a serverSEt

public class AgentServerConfigGenerator{

  public static final String VAR_MARK = "$";
  public static final String CONFIG_TAG = "config";
  public static final String VAR_TAG = "var"; 
  public static final String SET_TAG = "serverSet";
  public static final String PROPERTY_TAG = "property";
  public static final String DOMAIN_TAG = "domain";
  public static final String NETWORK_TAG = "network";
  public static final String SERVER_TAG = "server";
  public static final String TRANSIENT_TAG = "transient";
  public static final String NAME = "name";
  public static final String ID = "id";
  public static final String PORT = "port";
  public static final String SERVER = "server";
  public static final String INFO = "info";
  public static final String VALUE = "value";
  public static final String TYPE = "type";
  public static final String A3_DTD = "a3config.dtd";

  private static final String DOMAINS_PREFIX = "D";
  private static final String ADMIN_SERVER_NAME = "s0";
  private static final String ADMIN_DOMAIN = "D0";
  private static final String GLOBALS = "GLOBALS";
  private int indent = 2;
  private Element templateRoot;
  //private Element previousConfigRoot;
  private Hashtable defaultValues = new Hashtable();
  // to know if default values has been set for a type of block
  private Hashtable varsSet = new Hashtable();
  private String currentDomain = null;
  private boolean merge = false;

  /** 
   * constructor 
   **/
  public AgentServerConfigGenerator(String templateFile) 
    throws SAXException, IOException{
    DOMParser parser = new DOMParser();
    parser.parse(templateFile);
    init(parser);
  }

  public AgentServerConfigGenerator(InputStream template)
    throws SAXException, IOException{
    DOMParser parser = new DOMParser();
    parser.parse(new InputSource(template));
    init(parser);
  }

  private void init(DOMParser parser) throws SAXException, IOException {
    Document doc = parser.getDocument();
    // add specifics variable to template Root
    // the port  name of router, no default value
    Element routerElement = doc.createElement(VAR_TAG);
    routerElement.setAttribute(NAME, "domain.port");
    routerElement.setAttribute(TYPE, "java.lang.Integer");
    routerElement.setAttribute(INFO,"The port required on admin server to connect to new domain");
    // get root element
    templateRoot = doc.getDocumentElement();
    templateRoot.appendChild(routerElement);
  }

  /**
   * set nb of chars for indentation
   */
  public void setIndent(int indent){
    this.indent = indent;
  }

  /**
   * return the list of all set types defined in template
   */
  public String[] getServerSetTypes(){
    NodeList childs = templateRoot.getChildNodes();
    Vector v = new Vector();
    for (int i=0; i<childs.getLength(); i++){
      Node currentNode = childs.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE && 
            currentNode.getNodeName().equals(SET_TAG)){
        v.addElement(currentNode.getAttributes().getNamedItem(NAME).getNodeValue());
      }
    }
    String[] res = new String[v.size()];
    v.copyInto(res);
    return res;
  }



//   /**
//    * This fonction is obsolete, it will be automatically a merge
//    * creation of a3config as an xml OutputStream
//    */
//   public void build(OutputStream os, Properties p) 
//     throws IOException, A3configException{
//     Document currentDoc = new DocumentImpl();
//     DocumentType docType = 
//       ((DocumentImpl)currentDoc).createDocumentType(CONFIG_TAG, null, A3_DTD);
//     Element root = currentDoc.createElement(CONFIG_TAG);
//     currentDoc.appendChild(docType);
//     currentDoc.appendChild(root);
//     buildSimpleElement(PROPERTY_TAG, root, p);
//     buildSimpleElement(DOMAIN_TAG, root, p);
//     instantiateServers((short)0, root, p);
//     write(currentDoc, os);
//   }

  /**
   * Generate news a3servers configurations depending of new template,
   * new properties and existing configuration.
   * 
   * @param totalConfig The whole configuration including all applications,
   *  only known by admin server s0
   * @param localConfig The application specific configuration, seen 
   *  by application servers
   * @param previousConfig The name of the previous configuration file 
   *  (a3servers.xml)
   * @param p The properties to instantiate new configuration
   */
  public void merge(OutputStream totalConfig, OutputStream localConfig,
                    Properties p, InputStream previousConfig,
                    String instanceName)
    throws IOException, A3configException, SAXException{
    merge = true;
    // parse existing config
    DOMParser parser = new DOMParser();
    parser.parse(new InputSource(previousConfig));
    Document previousDoc = parser.getDocument();
    Element previousRoot = previousDoc.getDocumentElement();
    // generate new domain name
    //currentDomain = newDomain(previousRoot);
    currentDomain = instanceName;
    DocumentImpl localDoc = new DocumentImpl();
    // create local configuration
//     localDoc.appendChild(
//       localDoc.importNode(previousDoc.getDoctype(), true));
    Element localRoot = localDoc.createElement(CONFIG_TAG);
    localDoc.appendChild(localRoot);
    buildSimpleElement(PROPERTY_TAG, localRoot, p);
    buildSimpleElement(DOMAIN_TAG, localRoot, p);
    short nextId = getNextId(previousRoot);
    nextId = instantiateServers(nextId, localRoot, p);
    previousRoot.setAttribute("nextServerId", "" + nextId);
    // write configs
    updateConfs(localRoot, previousRoot, p);
    write(previousDoc, totalConfig);
    write(localDoc, localConfig);
  }

  ////////////////////////
  // FIXME this method is defined to workaround a xerces bug : deletes
  // attributes on import nodes unless parse attributes of child before
  // should be removed later
  private void updateImport(Element elem){ 
    NamedNodeMap attrs= elem.getAttributes();
    for (int i=0; i<attrs.getLength(); i++){
      attrs.item(i).getNodeName();
      attrs.item(i).getNodeValue();
    }
    NodeList childs = elem.getChildNodes();
    for (int i=0; i<childs.getLength(); i++){
      // recall fonction only for elements
      if (childs.item(i).getNodeType() == Node.ELEMENT_NODE){
        updateImport((Element)childs.item(i));
      }
    }
  }
  //////////////////////
  
  private void write(Document doc, OutputStream out) throws IOException{
    OutputFormat format  = new OutputFormat(doc);
    format.setIndent(indent);
    XMLSerializer  serial = new XMLSerializer(out, format);
    serial.asDOMSerializer();
    serial.serialize(doc);
    out.flush();
  }

  private static Element getChild(Element parent, String tagName, 
                           String attrName, String attrValue) 
    throws A3configException{
    NodeList childs = parent.getChildNodes();
    Node required = null;
    for (int i=0; i<childs.getLength(); i++){
      Node currentNode = childs.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE && 
          currentNode.getNodeName().equals(tagName)){
        Node attr = currentNode.getAttributes().getNamedItem(attrName);
        if (attr != null && attr.getNodeValue().equals(attrValue))
          return (Element)currentNode;
      }
    }
    throw new A3configException ("Unable to find " + tagName + 
                                 " element, with attr " + attrName + 
                                 " = " + attrValue);
  }

  /**
   * returns the list of global variables
   */
  public ConfigVariable[] getGlobalVariables() throws A3configException{
    varsSet.put(GLOBALS, GLOBALS);
    return getVariables(templateRoot, true);
  }

  /**
   * return the list of variables for a type
   */
  public ConfigVariable[] getServerSetVariables(String typeName) throws A3configException{
    varsSet.put(typeName, typeName);
    Element elm = getChild(templateRoot, SET_TAG, NAME, typeName);
    return getVariables(elm, true);
  }

  private short getNextId(Element elem){
    String lastId = elem.getAttribute("nextServerId");
    if (lastId == null || lastId.equals("")){
      return (short)1;
    }else{
      return Short.parseShort(lastId);
    }
//       String lastDomain = existingRoot.getAttribute("domainId");
//       if (lastDomain == null || lastDomain.equals(""))
//         lastDomain = "0";
//       int newDomain = new Integer(lastDomain).intValue() + 1;
//       existingRoot.setAttribute("domainId", "" + newDomain);

//     NodeList childs = elem.getChildNodes();
//     Short maxId = new Short((short)0);
//     for (int i=0; i<childs.getLength(); i++){
//       Node currentNode = childs.item(i);
//       if (currentNode.getNodeType() == Node.ELEMENT_NODE &&
//           (currentNode.getNodeName().equals(SERVER_TAG) ||
//            currentNode.getNodeName().equals(TRANSIENT_TAG))){
//         Short id = new Short(currentNode.getAttributes().
//                              getNamedItem(ID).getNodeValue());
//         if (maxId.compareTo(id) < 0){
//           maxId = id;
//         }
//       }
//     }
//     return (short)(maxId.intValue()+1);
  }

//   private String newDomain(Element existingRoot) throws A3configException{
//     // generated domains are of the format D1 D2...
//     // if an application define subdomains, they will have format D1.sub
//     if (merge){
//       // get last generated domain
//       String lastDomain = existingRoot.getAttribute("domainId");
//       if (lastDomain == null || lastDomain.equals(""))
//         lastDomain = "0";
//       int newDomain = new Integer(lastDomain).intValue() + 1;
//       existingRoot.setAttribute("domainId", "" + newDomain);
//       return (DOMAINS_PREFIX + newDomain);
//     }else{
//       return DOMAINS_PREFIX + "1";
//     }
//   }

  private short instantiateServers(short nextId, Element root, Properties p)
    throws A3configException{
    NodeList childs = templateRoot.getChildNodes();
    for (int i=0; i<childs.getLength(); i++){
      Node child = childs.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE && 
          child.getNodeName().equals(SET_TAG)){
        // get the name of the serverSet
        String setName =
          child.getAttributes().getNamedItem(NAME).getNodeValue();
        // Check if variables have been initialized ?
        if (varsSet.get(setName) == null){
          getVariables(child, false);
        }
        // check required instances of this set
        String instances = p.getProperty(setName);
        if (instances != null){
          StringTokenizer st = new StringTokenizer(instances, ",");
          while(st.hasMoreElements()){
            String instanceName = ((String)st.nextElement()).trim();
            nextId = instantiateSet(instanceName, child, root, p, nextId);
          }
        }
      }
    }
    if (nextId++ == Short.MAX_VALUE)
      return (short)1;
    return nextId;
  }


  private short instantiateSet(String instanceName, Node setNode, 
                               Element root, Properties p, 
                               short nextId) throws A3configException{
    NodeList childs = setNode.getChildNodes();
    int serversNb = countChilds(setNode, SERVER_TAG);
    // TODO : should be allowed...
    if (serversNb == 0)
      throw new A3configException(
        "A serverSet element must contain at least one server");
    int transientNb = countChilds(setNode, TRANSIENT_TAG);
    int serverPrefixName = 1;
    int transientPrefixName = 1;
    Short firstServer = new Short(nextId);
    Hashtable relativeIds = new Hashtable();
    // instantiate servers at once to know ids when instantiate transients
    for (int i=0; i<childs.getLength(); i++){
      Node sNode = childs.item(i);
      if (sNode.getNodeType() == Node.ELEMENT_NODE &&
          sNode.getNodeName().equals(SERVER_TAG)){
        Element serverElt = instantiateElement(root.getOwnerDocument(),
                                               instanceName, sNode, p);
        // check if name have been defined (eg was in a var)
        String serverName = null;
        ////////////////////////////
        if (serverElt.getAttributes().getNamedItem(NAME) != null){
          String definedName = 
            serverElt.getAttributes().getNamedItem(NAME).getNodeValue();
          if(! definedName.equals(""))
            serverName =  instanceName + "." + definedName;
        }
        if (serverName == null){
          serverName = instanceName;
          if (serversNb > 1)
            serverName += ("." + serverPrefixName ++);
        }
        ////////////////////////////
//         if (serverElt.getAttributes().getNamedItem(NAME) != null){
//           serverName = instanceName + "." + 
//             serverElt.getAttributes().getNamedItem(NAME).getNodeValue();
//           System.out.println("-1- " + serverName);
//         }
//         if (serverName == null || serverName.equals("")){
//           serverName = instanceName;
//           System.out.println("-2- " + serverName);
//           if (serversNb > 1)
//             serverName += ("." + serverPrefixName ++);
//           System.out.println("-3- " + serverName);
//         }
        serverElt.setAttribute(NAME, serverName);
        Short id = new Short(nextId ++);
        // does this server have a relativeId ?
        Node idNode =  serverElt.getAttributes().getNamedItem(ID);
        if (idNode != null){
          String relId = idNode.getNodeValue();
          if (relId != null && (! relId.equals("")))
            relativeIds.put(relId, id);
        }
        serverElt.setAttribute(ID, id.toString());
        NodeList serverChilds = sNode.getChildNodes();
        for (int j=0; j<serverChilds.getLength(); j++){
          if (serverChilds.item(j).getNodeType() == Node.ELEMENT_NODE)
            serverElt.appendChild(instantiateElement(root.getOwnerDocument(),
                                                     instanceName, 
                                                     serverChilds.item(j), 
                                                     p));
        }
        root.appendChild(serverElt);
      }
    }    
    for (int i=0; i<childs.getLength(); i++){
      Node tNode = childs.item(i);
      if (tNode.getNodeType() == Node.ELEMENT_NODE &&
          tNode.getNodeName().equals(TRANSIENT_TAG)){
        Element transientElt = instantiateElement(root.getOwnerDocument(),
                                                  instanceName, tNode, p);
        // check if name have been defined (eg was in a var)
        String transientName = 
          transientElt.getAttributes().getNamedItem(NAME).getNodeValue();
        if (transientName == null || transientName.equals("")){
          transientName = instanceName + ".transient";
          if (transientNb > 1)
            transientName += ("." + transientPrefixName ++);
          transientElt.setAttribute(NAME, transientName);
        }
        // generate id
        Short id = new Short(nextId ++);
        transientElt.setAttribute(ID, id.toString());
        // associated server
        Node serverNode = transientElt.getAttributes().getNamedItem(SERVER);
        String associatedServer = null;
        Short serverId = null;
        if (serverNode != null)
          associatedServer = serverNode.getNodeValue();
        if (associatedServer == null || associatedServer.equals("")){
          // this was not a var associate to first server
          serverId = firstServer;
        }else{
          serverId = (Short)relativeIds.get(associatedServer);
        }
        if (serverId == null){
          throw new A3configException("transient " + transientName +
                                        " has no associated server");
        }
        transientElt.setAttribute(SERVER, serverId.toString());
        NodeList serverChilds = tNode.getChildNodes();
        for (int j=0; j<serverChilds.getLength(); j++){
          if (serverChilds.item(j).getNodeType() == Node.ELEMENT_NODE)
            transientElt.appendChild(
              instantiateElement(root.getOwnerDocument(),
                                 instanceName, serverChilds.item(j), p));
        }
        root.appendChild(transientElt);
      }
    }
    return nextId;
  }


  private int countChilds(Node currentElt, String tagName){
    int nb = 0;
    NodeList childs = currentElt.getChildNodes();
    for (int i=0; i<childs.getLength(); i++){
      Node currentNode = childs.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE && 
          currentNode.getNodeName().equals(tagName))
        nb++;
    }
    return nb;
  }

  private void buildSimpleElement(String tagName, Element root, Properties p)
    throws A3configException{
    // check if default values for global vars have been initialized
    if (varsSet.get(GLOBALS) == null)
      getVariables(templateRoot, false);
    NodeList childs = templateRoot.getChildNodes();
    for (int i=0; i<childs.getLength(); i++){
      Node currentNode = childs.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE && 
          currentNode.getNodeName().equals(tagName)){
        root.appendChild(instantiateElement(root.getOwnerDocument(),
                                            null, currentNode, p));
      }
    }
  }


  // only for one level
  private Element instantiateElement(Document doc,
                                     String instance,
                                     Node model,
                                     Properties p) throws A3configException{
    Element instanceElt = (Element) doc.importNode(model, false);
    NamedNodeMap attrs = model.getAttributes();
    for (int j=0; j<attrs.getLength(); j++){
      Node attrNode =  attrs.item(j);
      String attr = attrs.item(j).getNodeName();
      String attrValue = attrs.item(j).getNodeValue();
      if (attrNode.getNodeValue().startsWith(VAR_MARK)){
        String varName = attrValue.substring(1, attrValue.length());
        attrValue = null;
        // get config in properties
        if (instance != null)
          attrValue = p.getProperty(instance + "." + varName);
        if (attrValue == null)
          // it may be a global variable
          attrValue = p.getProperty(varName);    
        if (attrValue == null)
          // get default
          attrValue = (String)defaultValues.get(varName);
        if (attrValue == null)
          throw new A3configException("Variable " + 
                                      ((instance!=null) ? instance+"." : "")
                                     +  varName + 
                                      " not defined, no defaultValue");
      }
      // add domain info if any
      if (merge && (! attrValue.equals(TRANSIENT_TAG)) &&
          ((model.getNodeName().equals(NETWORK_TAG) 
            && attr.equals(DOMAIN_TAG)) ||
           (model.getNodeName().equals(DOMAIN_TAG) && attr.equals(NAME)))){
        attrValue = currentDomain + "." + attrValue;
      }
      instanceElt.setAttribute(attr, attrValue);
    }
    return instanceElt;
  }


  private ConfigVariable[] getVariables(Node node, boolean type) 
    throws A3configException{
    NodeList childs = node.getChildNodes();
    Vector v = new Vector();
    for (int i=0; i<childs.getLength(); i++){
      Node currentNode = childs.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE && 
            currentNode.getNodeName().equals(VAR_TAG)){
        NamedNodeMap attrs = currentNode.getAttributes();
        String name = attrs.getNamedItem(NAME).getNodeValue();
        Node valueNode =  attrs.getNamedItem(VALUE);
        String defaultValue = null;
        if (valueNode != null){
          defaultValue = valueNode.getNodeValue();
          defaultValues.put(name, defaultValue);
        }
        if (!type)
          continue;
        Node infoNode =  attrs.getNamedItem(INFO);
        String info = null;
        if (infoNode != null)
        info = infoNode.getNodeValue();
        try{
          Class c = ConfigVariable.getClass(
            attrs.getNamedItem(TYPE).getNodeValue());
          Object defaultObject = null;
          if (defaultValue != null){
            defaultObject = ConfigVariable.getObject(c, defaultValue);
          }
          v.addElement(new ConfigVariable(name, c, defaultObject, info));
        }catch(Exception e){
          throw new A3configException("Error parsing variable type, variable "
                                      + name + " has not a valid type");
        }
      }
    }
    ConfigVariable[] res = new ConfigVariable[v.size()];
    v.copyInto(res);
    return res;
  }


  
  private void updateConfs(Element localRoot,Element previousRoot, Properties p)
    throws A3configException{
    // add general previous config properties to new config if not re-defined
    NodeList childs = previousRoot.getChildNodes();
    for (int i=0; i<childs.getLength(); i++){
      Node currentNode = childs.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE &&
            currentNode.getNodeName().equals(PROPERTY_TAG)){
        try{
          Element oldPropr = getChild(localRoot, PROPERTY_TAG, NAME,
                                      ((Element)currentNode).getAttribute(NAME));
        }catch(Exception e){
          // this property is not redefined in new conf -> redefine it
          updateImport((Element)currentNode);
          localRoot.insertBefore(
            localRoot.getOwnerDocument().importNode(currentNode, true), 
            localRoot.getFirstChild());
        }
      }
    }
    // get main domain from D0
    Element D0Domain = getChild(previousRoot, DOMAIN_TAG, NAME, ADMIN_DOMAIN);
    updateImport(D0Domain);
    // get s0 server 
    Element s0server = 
      getChild(previousRoot, SERVER_TAG, NAME, ADMIN_SERVER_NAME);
    updateImport(s0server);
    // get first domain, common to admin an this appli
    Element newDomain = null;
    childs = localRoot.getChildNodes();
    for (int i=0; i<childs.getLength(); i++){
      Node currentNode = childs.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE &&
          currentNode.getNodeName().equals(DOMAIN_TAG)){
        newDomain = (Element)currentNode;
        break;
      }
    }
    if (newDomain == null)
      throw new A3configException("No domain defined");
    String newDomainName = newDomain.getAttribute(NAME);
    // get port for s0 to route new domain
    String routePort = p.getProperty("domain.port");
    if (routePort == null || Integer.parseInt(routePort)<1)
      throw new A3configException(
        "No port defined for admin server to route new application");
    //check this port is not already defined for a router
    // (appends when deploy 2 instances with same values
    checkRouterValid(previousRoot, routePort);  
    // update s0server
    Element newDomainNetwork = 
      previousRoot.getOwnerDocument().createElement(NETWORK_TAG);
    newDomainNetwork.setAttribute(DOMAIN_TAG, newDomainName);
    newDomainNetwork.setAttribute(PORT, routePort);
    s0server.insertBefore(newDomainNetwork, s0server.getFirstChild());
    // add new servers to previousconfig
    Element firstNewServer = null;
    for (int i=0; i<childs.getLength(); i++){
      Node currentNode = childs.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE &&
          currentNode.getNodeName().equals(SERVER_TAG) ||
          currentNode.getNodeName().equals(TRANSIENT_TAG)){
        if (firstNewServer == null)
          firstNewServer = (Element)currentNode;
        updateImport((Element)currentNode);
        previousRoot.appendChild(
          previousRoot.getOwnerDocument().importNode(currentNode, true));
      }
    }
    // add domain element to previous config
    updateImport(newDomain);
    previousRoot.insertBefore(
      previousRoot.getOwnerDocument().importNode(newDomain, true), s0server);
    // import s0server in local config and remove useless domains
    updateImport(s0server);
    Element localS0server = 
      (Element)(localRoot.getOwnerDocument().importNode(s0server, true));
    childs = localS0server.getChildNodes();
    for (int i=0; i<childs.getLength(); i++){
      Node currentNode = childs.item(i);
       if (currentNode.getNodeType() == Node.ELEMENT_NODE &&
           currentNode.getNodeName().equals(NETWORK_TAG) &&
           ! currentNode.getAttributes().getNamedItem(DOMAIN_TAG).
           getNodeValue().equals(ADMIN_DOMAIN) &&
           ! currentNode.getAttributes().getNamedItem(DOMAIN_TAG).
               getNodeValue().equals(newDomainName)){
         localS0server.removeChild(currentNode);
       }
    }
    localRoot.insertBefore(localS0server, firstNewServer);
    updateImport(D0Domain);
    localRoot.insertBefore(
      localRoot.getOwnerDocument().importNode(D0Domain, true), localS0server);
  }

  private void checkRouterValid(Element previousRoot, String port) 
    throws A3configException{
    Element s0elt = getChild(previousRoot, "server", "id", "0");
    NodeList childs = s0elt.getChildNodes();
    for (int i=0; i<childs.getLength(); i++){
      Node currentNode = childs.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE &&
          currentNode.getNodeName().equals(NETWORK_TAG) &&
          currentNode.getAttributes().getNamedItem(PORT).getNodeValue().
          equals(port))
        throw new A3configException(
          "The port " + port + " is already defined for domain " +
          currentNode.getAttributes().getNamedItem(DOMAIN_TAG).getNodeValue());
    }
  }


  public static void deleteApplicationConfig(InputStream totalConfig,
                                             InputStream localConfig, 
                                             String res)
    throws Exception{
    DOMParser parser = new DOMParser();
    parser.parse(new InputSource(totalConfig));
    Document resDoc = parser.getDocument();
    Element totalRoot = resDoc.getDocumentElement();
    parser.parse(new InputSource(localConfig));
    Element localRoot = parser.getDocument().getDocumentElement();
    // get s0 server of old config
    Element s0server =  getChild(totalRoot, SERVER, NAME, ADMIN_SERVER_NAME);
    // parse local Element
    NodeList childs = localRoot.getChildNodes();
    for (int i=0; i<childs.getLength(); i++){
      Node child = childs.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE){
        if (child.getNodeName().equals(DOMAIN_TAG)){
          String domainName = 
            child.getAttributes().getNamedItem(NAME).getNodeValue();
          if (! domainName.equals(ADMIN_DOMAIN)){
            // remove domain from total config
            removeChild(totalRoot, DOMAIN_TAG, NAME, domainName);
            // remove network from s0server
            removeChild(s0server, NETWORK_TAG, DOMAIN_TAG, domainName);
          }
        }else if (child.getNodeName().equals(SERVER_TAG) 
                  || child.getNodeName().equals(TRANSIENT_TAG)){
          //remove this server from total config
          String sid = child.getAttributes().getNamedItem(ID).getNodeValue();
          if (! sid.equals("0"))
            removeChild(totalRoot, child.getNodeName(), ID, sid);
        }
      }
    }
    // write new config
    OutputFormat format  = new OutputFormat(resDoc);
    //format.setIndent(indent);
    FileOutputStream fos = new FileOutputStream(res);
    XMLSerializer  serial = new XMLSerializer(fos, format);
    serial.asDOMSerializer();
    serial.serialize(resDoc);
    fos.flush();
    fos.close();
  }

  private static void removeChild(Element parent, String tagName,
                                  String attrName, String attrValue) 
    throws Exception{
    NodeList childs = parent.getChildNodes();
    for (int i=0; i<childs.getLength(); i++){
      Node child = childs.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE &&
            child.getNodeName().equals(tagName)){
        // check attributes
        if (child.getAttributes().getNamedItem(attrName).
            getNodeValue().equals(attrValue)){
          parent.removeChild(child);
          return;
        }
      }
    }
    throw new Exception("Unable to find tag " + tagName + " with attr " +
                        attrName + "=" + attrValue);
  }

}
