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
 * WITHOUT WARRANTY OF ANY KIND, either expre4ss or implied. See the License for
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
import java.text.*;
import java.util.*;
import org.apache.xerces.parsers.* ;
import org.w3c.dom.* ;
import org.apache.xml.serialize.* ;
import org.xml.sax.* ;

/**
  * Class which realizes the <code>HttpProxy</code> functions in an input
  * driver thread. This version of HttpDriver works with a template file.
  * <p>
  *
  * @author	Paulet Jerome
  * @version	v2.0
  *
  * @see	fr.dyade.aaa.ip.HttpProxy
  */

public class HttpDriver implements NotificationInputStream {

  /** keep stream to be able to close it */
  private InputStream in;

  /** output stream */
  protected PrintWriter out;

  /** variables needed for dynamic html */
  private String title ;
  private Vector subtitle = new Vector() ;
  private String proto ;
  private String localhost;
  private String portServer ;
  private String path ;
  private String text ;
  private Vector listAgents = new Vector() ;
  private Vector listNetworkServers = new Vector() ;
  private Vector listTransientServers = new Vector() ;
  private int typeList = 0 ;

  /**
   * Creates a driver. 
   * The driver input and output streams are set afterwards
   * by calls to <code>setInputStream</code> and <code>setOutputStream</code>.
   */
  public HttpDriver() {
    in = null;
    out = null;
  }

  /** Returns a sorted list of attributes. */
   protected Attr[] sortAttributes(NamedNodeMap attrs) {

      int len = (attrs != null) ? attrs.getLength() : 0;
      Attr array[] = new Attr[len];
      for ( int i = 0; i < len; i++ ) {
         array[i] = (Attr)attrs.item(i);
      }
      for ( int i = 0; i < len - 1; i++ ) {
         String name  = array[i].getNodeName();
         int    index = i;
         for ( int j = i + 1; j < len; j++ ) {
            String curName = array[j].getNodeName();
            if ( curName.compareTo(name) < 0 ) {
               name  = curName;
               index = j;
            }
         }
         if ( index != i ) {
            Attr temp    = array[i];
            array[i]     = array[index];
            array[index] = temp;
         }
      }

      return (array);

   } // sortAttributes(NamedNodeMap):Attr[]



   /** Prints the specified node, recursively. */
   public void print (Node node) throws Exception {

      // is there anything to do?
      if ( node == null ) {
         return;
      }

      int type = node.getNodeType();
      boolean known =true; // true if the node.element is known 
      switch ( type ) {
         // print document
      case Node.DOCUMENT_NODE: {           
	  print(((Document)node).getDocumentElement());
	  out.flush();
	  break;
      }
      
      // print element with attributes
      case Node.ELEMENT_NODE: {
	  
	  String Name = node.getNodeName();
	  Name = Name.toUpperCase();
	  if(Name.startsWith("DEBUG_")){
	      known =false ;
	      if(Name.equals("DEBUG_TITLE")){
		  NamedNodeMap attribut = node.getAttributes();
		  if(attribut.getLength() == 1 && attribut.getNamedItem("title") != null )		      
		      out.print(attribut.getNamedItem("title").getNodeValue());
		  else
		      out.print(title);
	      }else if (Name.equals("DEBUG_SUBTITLE")){
		  NamedNodeMap attribut = node.getAttributes();
		  if(attribut.getLength() == 1 && attribut.getNamedItem("subtitle") != null ){
		      out.print(attribut.getNamedItem("subtitle").getNodeValue());
		      out.print("<hr WIDTH=\"100%\"/>");		    
		  }else{
		      if(!subtitle.isEmpty()){
			  out.println((String)subtitle.firstElement());
			  subtitle.removeElementAt(0);
		      }
		  }
	      }else if(Name.equals("DEBUG_BODY")){
		      switch(typeList){
		      case 1 : // list of Agents requested
			  {	
			      String listid,listid2;
			      if(listAgents.isEmpty())
				  out.println("No agents in this server<br><br>");
			      else{
				  for(int  i=0;i<listAgents.size();i++){
				      listid=(String)listAgents.elementAt(i);
				      listid2=listid.replace('#','+'); // # is a special symbol (anchor) in URL
				      out.println("<br><a href=\"CMD_DUMP"+listid2+"*\">"+listid+"</a>");
				  }
			      } 
			      typeList = 0;
			      break ;
			  }
		      case 2 : // list of Servers requested
			  {
			      String list ;
			      if(!listNetworkServers.isEmpty()){
				  out.println("<hr><center><h3>NETWORK SERVERS</h3></center><br/>");
				  out.println("<ul>");
				  for (int i=0; i<listNetworkServers.size();i++ ){
				      out.print("<br/><li>");
				      list=(String)listNetworkServers.elementAt(i);
				      if(list.indexOf("fr.dyade.aaa.ip.HttpProxy")==-1) // this server launched the HttpProxy service 
					  out.println("<br/>"+list);
				      else{
					  StringTokenizer st = new StringTokenizer(list,",=",true);
					  out.println("<br/>");
					  // the serverid sid is the 5th token
					  for(int j=1;j<5;j++) 
					      out.println(st.nextToken());
					  short sid=(short)Integer.parseInt(st.nextToken()); 
					  int port=Integer.parseInt(Server.getServiceArgs(sid,"fr.dyade.aaa.ip.HttpProxy"));
					  String hostname = Server.getHostname(sid);
					  out.println("<a href=\""+proto+"://"+ hostname +":"+port+"/CMD_LISTA\">"+sid+"</a>");
					  while(st.hasMoreTokens())
					      out.println(st.nextToken());	 	   
				      }
				      out.print("</li>");
				  }
				  out.print("</ul>");
			      }
			      if(!listTransientServers.isEmpty()){
				  out.println("<hr><center><h3>TRANSIENT SERVERS</h3></center><br>");
				  out.println("<ul>");
				  for (int i=0; i<listTransientServers.size();i++ ){
				      out.print("<br/><li>");
				      list=(String)listTransientServers.elementAt(i);
				      if(list.indexOf("fr.dyade.aaa.ip.HttpProxy")==-1) // this server launched the HttpProxy service 
					  out.println("<br/>"+list);				      
				      else{
					  StringTokenizer st = new StringTokenizer(list,",=",true);
					  out.println("<br/>");
					  // the serverid sid is the 5th token
					  for(int j=1;j<5;j++)
					      out.println(st.nextToken());
					  short sid=(short)Integer.parseInt(st.nextToken());
					  int port=Integer.parseInt(Server.getServiceArgs(sid,"fr.dyade.aaa.ip.HttpProxy"));
					  String hostname = Server.getHostname(sid);
					  out.println("<a href=\""+proto+"://" + hostname + ":"+port+"/CMD_LISTA\">"+sid+"</a>");	
					  while(st.hasMoreTokens())
					      out.println(st.nextToken());
				      }
				      out.print("</li>");
				  }
				  out.print("</ul>");
			      } 
			      typeList = 0 ;
			      break ;
			  }
		      case 3 : // Informations on Queues requested
			  {
			    if(!Server.isTransient(Server.getServerId())) {
			      out.println("<br/><ul><li> Numbers of messages in the queue in : " +
					  Server.qin.size()+"</li>" +
					  "<br/><li>   Numbers of messages in the queue out : " +
					  Server.qout.size()+"</li></ul>");
			    }
			    else {
			      out.println("<br/> No message queue in a transient server.");
			    }
			    typeList = 0 ;
			    break ;
			  }
		      case 4 : // dump action requested
			  {
			      String request = (String)listAgents.elementAt(0);
			      String subs = (String)listAgents.elementAt(1);
			      String s ;
			      int start = 0;
			      int end = 0 ;
			      StringTokenizer st = new StringTokenizer(request,"(),",true);
			      while(st.hasMoreTokens()){
				  s = st.nextToken();
				  start=s.indexOf('#');
				  if(start==-1) // no agentid in this token
				      out.println(s);
				  else{
				      end = s.indexOf('.');
				      if (end==-1)  // no agentid in this token
					  out.println(s);
				      else{
					  end=s.indexOf(' ',start);
					  if (end==-1)
					      end=s.length();
					  out.println(s.substring(0,start));
					  subs=s.substring(start,end); // this string contains the agentid 
					  int startdot = subs.indexOf('.');
					  int enddot = subs.indexOf('.',startdot+1);
					  short sid=(short)Integer.parseInt(subs.substring(startdot+1,enddot)); // the serverid where the agent is deployed is between the dots
					  int port=Integer.parseInt(Server.getServiceArgs(sid,"fr.dyade.aaa.ip.HttpProxy"));
					  String hostname = Server.getHostname(sid);
					  out.println("<a href=\""+proto+"://"+ hostname +":"+port+"/CMD_DUMP+"+s.substring(start+1,end)+"*\">"+s.substring(start,end)+"</a>");
					  if(end!=s.length())
					      out.println(s.substring(end,s.length()));   
				      }
				  }
			      }
			      typeList = 0 ;
			      break ;
			  }
		      default : typeList = 0 ; break ;
		      }		  				
	      }else if (Name.equals("DEBUG_REF")){
		  NamedNodeMap attribut = node.getAttributes();
		  if(attribut.getNamedItem("proto")!=null)
		      if(attribut.getNamedItem("proto").getNodeValue()!=null)
			  proto = attribut.getNamedItem("proto").getNodeValue();
		  if(attribut.getNamedItem("server")!=null)
		      if(attribut.getNamedItem("server").getNodeValue()!=null)
			  localhost = attribut.getNamedItem("server").getNodeValue();
		  if(attribut.getNamedItem("port")!=null)
		      if(attribut.getNamedItem("port").getNodeValue()!=null)
			  portServer = attribut.getNamedItem("port").getNodeValue();
		  if(attribut.getNamedItem("path")!=null)
		      if(attribut.getNamedItem("path").getNodeValue()!=null)
			  path = attribut.getNamedItem("path").getNodeValue();
		  if(attribut.getNamedItem("text")!=null)
		      if(attribut.getNamedItem("text").getNodeValue()!=null)
			  text = attribut.getNamedItem("text").getNodeValue();		  
		  out.print("<a href=\""+proto+"://"+ localhost +":"+portServer+"/"+path+"\">"+text+"</a>");
	      }else if (Name.equals("DEBUG_DATE")){
		  Date date=new Date();
		  out.print(date.getDate()+"/"+(date.getMonth()+1)+"/"+(date.getYear()+1900)+"  "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds());
	      }else{
		  out.print("<h1><font color=\"red\"Error : no reference for "+Name+"</font></h1>");
	      }
	  }else{
	      out.print('<');
	      out.print(Name);
	      Attr attrs[] = sortAttributes(node.getAttributes());
	      for ( int i = 0; i < attrs.length; i++ ) {
		  Attr attr = attrs[i];
		  out.print(' ');
		  out.print(attr.getNodeName());
		  out.print("=\"");
		  out.print(attr.getNodeValue());
		  out.print('"');
	      }
	      out.print('>');
	  }
	  NodeList children = node.getChildNodes();
	  if ( children != null ) {
	      int len = children.getLength();
	      for ( int i = 0; i < len; i++ ) {
		  print(children.item(i));
	      }
	  }
	  break;
      }
      // print text
      case Node.TEXT_NODE: {	  
	  out.print(node.getNodeValue());
	  break;
      }
      // Comment
      case Node.COMMENT_NODE: {
	  out.print("<!-- ");
	  out.print(node.getNodeValue());
	  out.print("-->");
	  break;
      }
      }
      // Tag read is a HTML tag
      if ( known ) {
	  out.print("</");
	  out.print(node.getNodeName());
	  out.print('>');
      }
      
      out.flush();
      
   } // print(Node)
    
    
   /**
    * Gets a <code>Notification</code> from the stream.
    * <p>
    * This function usually returns notifications to be handled be the
    * associated proxy agent. Here it is used as a kind of main loop, never
    * returning a notification. Instead commands are executed synchronously
    * and results are written onto the output stream.
    */
  public Notification readNotification() throws IOException {
      Notification notification = null;
      String request=(new BufferedReader(new InputStreamReader(in))).readLine(); 
      StringTokenizer st=new StringTokenizer(request);
    try{
	String s;
	int count;
	count=st.countTokens();	
	s=st.nextToken();
	if((count<2)||!(s.equals("GET"))){ // request is not valid

		out.println("<HTML><TITLE>AAA debugger with HTML</TITLE><BODY>");
		out.println("<p><FONT COLOR=\"Red\"><center><h1>Bad Request</h1></center></FONT>");
		out.println("</BODY></HTML>");
	}else{
	    s=st.nextToken();
	    DOMParser parser = new DOMParser();
	    parser.setValidating(true);
	    parser.parse(new InputSource("DebugTemplate.html"));
	    Document doc = parser.getDocument();

	    title = "AAA Debugger with HTML";
	    subtitle.removeAllElements();
	    proto ="http";
	    localhost=Server.getHostname(Server.getServerId());
	    portServer=Server.getServiceArgs(Server.getServerId(),"fr.dyade.aaa.ip.HttpProxy");
	    path="" ;
	    text="lien" ;
	    listAgents.removeAllElements() ;
	    listNetworkServers.removeAllElements() ;
	    listTransientServers.removeAllElements() ;	    



	    if(s.equals("/")){ // first page
	

		print(doc);
		

	    }else if(s.equals("/CMD_LISTA")){ // a list of agents has been requested
		typeList = 1;
		subtitle.addElement("List of Agents");
		listAgents.removeAllElements();
		listAgents = listagent();
		print(doc);

	    }else if(s.equals("/CMD_LISTS")){ // a list of servers has been requested
		typeList = 2 ;
		subtitle.addElement("List of Servers");
		listNetworkServers.removeAllElements();
		listTransientServers.removeAllElements();
		listNetworkServers=listNetworkServer(); // list of persistent servers
		listTransientServers=listTransientServer(); // list of transient servers
		print(doc);
	
	    }else if(s.equals("/CMD_LISTQ")){
		typeList = 3 ;
		subtitle.addElement("Queues for the server "+Server.getServerId());
		print(doc);
	
	    }else if(s.startsWith("/CMD_DUMP")){ // a dump command has been requested
		typeList = 4 ;
		int start = 0;
		int end = 0;
		start = s.indexOf('+',end); // start of the string which contains the agentid
		end = s.indexOf('*', start); // end of the string which contains the agentid
		String subs = s.substring(start, end);
		subs=subs.replace('+','#'); // an agentid is like #x.y.z , x,y,z numbers 
		request=dump(subs);
		st = new StringTokenizer(request,"(),");
		String s1 = st.nextToken(); // this string contains the id of the agent
		String s2 = st.nextToken();// this string contains the class  of the agent
		String s3 = st.nextToken();// this string contains the name of the agent

		title = "AGENT ID="+s1.substring(s1.indexOf('#'),s1.indexOf(' '))+" NAME="+s3+" CLASS="+s2 ;
		subtitle.addElement("Agent "+s1.substring(s1.indexOf('#'),s1.indexOf(' '))+" dumped");
		listAgents.addElement(request);
		listAgents.addElement(subs);
		print(doc);
	    }
	}
	in.close();
	out.flush();
	out.close();
    }catch(IOException exc){
	exc.printStackTrace();
    }catch(Exception exc1){
	out.println("<HTML><TITLE>AAA debugger with HTML</TITLE><BODY bgcolor=\"#FFCCCC\">");
	out.println("<p/><FONT COLOR=\"Red\"><center><h1>Bad format in the template.html</h1></center></FONT><br/>");
	exc1.printStackTrace();
	out.println("</BODY></HTML>");
	in.close();
	out.flush();
	out.close();
    }finally{
	return notification; 
    }
  }


    

  /**
   * Sets the driver input stream.
   *
   * @param in		the underlying input stream
   */
  public void setInputStream(InputStream in) {
    this.in = in;
  }

  /**
   * Sets the driver output stream.
   *
   * @param out		the underlying output stream
   */
  public void setOutputStream(OutputStream out) {
    this.out = new PrintWriter(out, true);
  }


  /**
   * Closes the stream.
   */
  public void close() throws IOException {
    if (in != null) {
      in.close();
      in = null;
    }
    if (out != null) {
      out.close();
      out = null;
    }
  }

  /**
   * List all agents deployed on the current server
   */
  Vector listagent() {
      Vector lista=new Vector();
      AgentId list[] = Agent.getLoadedAgentIdlist();
      for (int i=list.length; --i>=0; ){
	  lista.addElement(list[i].toString());
      }
      return lista;
  }

  /**
   * List all persistent servers.
   */
  Vector listNetworkServer() {
      ServerDesc list[] =Server.a3config.networkServers;
      Vector lists=new Vector();
      if(list!=null){
	  for (int i=0; i<list.length;i++ ){
	    lists.addElement(list[i].toString());
		    }
		}
      return lists;
      }

 /**
   * List all transient servers. 
   */
  Vector listTransientServer() {
      ServerDesc list[] =Server.a3config.transientServers;
      Vector lists=new Vector();
      if(list!=null){
	  for (int i=0; i<list.length;i++ ){
	    lists.addElement(list[i].toString());
	  }
      }
      return lists;
      }


  /**
   * Executes a dump command.
   *
   * @ param agentid     String which contains the agentid 
   * like #x.y.z ,x,y,z are numbers.
   */
    String dump(String agentid) {
	try {
	    AgentId id = AgentId.fromString(agentid);
	    return (Agent.dumpAgent(id));
	    
	} catch (Exception exc) {
	    exc.printStackTrace(out);
	    return null;
	}
    }

  /**
   * Reacts to an exception while parsing an command.
   *
   * @param e	exception to react to
   */
  void parseException(ParseException e) {
    out.println("parse error at " + e.getErrorOffset() + ": " +
		e.getMessage());
  }
}
