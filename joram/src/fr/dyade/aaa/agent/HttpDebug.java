/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Daemon;

/**
 * A <code>HttpDebug</code> service provides an HTTP interface to
 * access to debugging functions in running agent servers.
 * <p>
 * The <code>HttpDebug</code> service needs an argument: the TCP port number.
 * Moreover, The <code>HttpDebug</code> service exports its connecting
 * parameters in a file named server<serverId>HttpProxy.cnx.
 * It may be accessed using a HTTP browser client.
 * <p>
 * Actually, there is only one thread running, which reads and analyses
 * commands from the input flow,and writes results synchronously onto the
 * output flow.
 */
public class HttpDebug {
  static HttpDebug httpd = null;

  static int port = 8090;
  HttpDebugMonitor monitors[] = null;
  ServerSocket listen = null;

  static boolean debug = true;

  DebugMonitor dmon = null;

  static Logger xlogmon = null;

  /**
   * Initializes the package as a well known service.
   * <p>
   * Creates a <code>HttpDebug</code> proxy.
   *
   * @param args	parameters from the configuration file
   * @param firstTime	<code>true</code> when service starts anew
   */
  public static void init(String args, boolean firstTime) throws Exception {
    if (args.length()!=0) {
      try {
	port = Integer.parseInt(args);
      } catch (NumberFormatException exc) {}
    }

    // Get the logging monitor from current server MonologLoggerFactory
    xlogmon = Debug.getLogger(Debug.A3Service + ".HttpDebug");

    if (httpd == null)
      httpd = new HttpDebug(port);
    startService();
  }

  public static void startService() {
    for (int i=0; i<httpd.monitors.length; i++) {
      httpd.monitors[i].start();
    }
//     if (debug) dmon.start();
  }

  public static void stopService() {
    for (int i=0; i<httpd.monitors.length; i++) {
      if (httpd.monitors[i] != null) httpd.monitors[i].stop();
    }
//     if (debug && (dmon != null)) dmon.stop();
    httpd = null;
  }

  String host = null;
  String base = null;

  /**
   * Creates an HttpDebug service.
   *
   * @param port  TCP listen port of this proxy.
   */
  private HttpDebug(int port) throws IOException {
    if (port != 0)
      this.port = port;

    host = InetAddress.getLocalHost().getHostName();
    base = "http://" + host + ":" + port;
    listen = new ServerSocket(port);

    monitors = new HttpDebugMonitor[1];
    for (int i=0; i<monitors.length; i++) {
      monitors[i] = new HttpDebugMonitor("HttpDebug#" +
                                         AgentServer.getServerId() + '.' + i);
    }

//     if (debug) dmon = new DebugMonitor();
  }

  /**
   * Provides a string image for this object.
   */
  public String toString() {
    StringBuffer strBuf = new StringBuffer();

    strBuf.append("(").append(super.toString());
    strBuf.append(",port=").append(port);
    strBuf.append(",monitors=[");
    for (int i=0; i<monitors.length; i++) {
      strBuf.append(monitors[i].toString()).append(",");
    }
    strBuf.append("]");
    strBuf.append(")");

    return strBuf.toString();
  }

  final static String CMD_HELP = "/HELP";

  final static String CMD_ADMIN = "/ADMIN";
  final static String CMD_AGENTS = CMD_ADMIN + "/AGENTS";
  final static String CMD_SERVERS = CMD_ADMIN + "/SERVERS";
  final static String CMD_MSG_CONS = CMD_ADMIN + "/MSGCONS";
  final static String CMD_SERVICES = CMD_ADMIN + "/SERVICES";

  final static String CMD_DUMP_AGENT = CMD_ADMIN + "/DUMP_AGENT";

  final static String CMD_DEBUG = "/DEBUG";
  final static String CMD_DEBUG_WAIT = "/XEBUGWAIT";
  final static String CMD_DEBUG_RUN = "/XEBUGRUN";

  final static String CMD_CLASS = "/CLASS";

  final static String CMD_STOP = "/STOP";
  final static String CMD_START = "/START";
  final static String CMD_QUEUE = "/QUEUE";
  final static String CMD_DUMP = "/DUMP";
  final static String CMD_REMOVE = "/REMOVE";

  final static String CMD_THREADS = "/THREADS";

  class HttpDebugMonitor extends Daemon {
    Socket socket = null;
    BufferedReader reader = null;
    PrintWriter writer = null;

    /**
     * Constructor.
     */
    protected HttpDebugMonitor(String name) {
      super(name, HttpDebug.xlogmon);
      // Get the logging monitor from HttpDebug (overload Daemon setup)
      this.setThreadGroup(AgentServer.getThreadGroup());
    }

    /**
     * Provides a string image for this object.
     *
     * @return	printable image of this object
     */
    public String toString() {
      return "(" + super.toString() +
	",socket=" + socket + ")";
    }

    public void run() {
      try {
        while (running) {
          canStop = true;
          try {
            socket = listen.accept();
            canStop = false;
          } catch (IOException exc) {
            if (running)
              logmon.log(BasicLevel.ERROR,
                         getName() + ", error during accept", exc);
          }

          if (! running) break;

          try {
            // Get the streams
            reader = new BufferedReader(
              new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
	  
            // Reads then parses the request
            doRequest(reader.readLine());

            writer.flush();
          } catch (Exception exc) {
            logmon.log(BasicLevel.ERROR,
                       getName() + ", error during connection", exc);
          } finally {
            // Closes the connection
            try {
              reader.close();
            } catch (Exception exc) {}
            reader = null;
            try {
              writer.close();
            } catch (Exception exc) {}
            writer = null;
            try {
              socket.close();
            } catch (Exception exc) {}
            socket = null;
          }
        }
      } finally {
        finish();
      }
    }

    protected void close() {
      try {
	listen.close();
      } catch (Exception exc) {}
      listen = null;
    }

    protected void shutdown() {
      close ();
    }

    void header(String title) throws IOException {
      writer.println("<HTML>");
      writer.println("<HEAD>");
      writer.println("   <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"/>");
      writer.println("<STYLE TYPE=\"text/css\">");
      writer.println("<!-- ");
      writer.println("  /* Style definitions */");
      writer.println("  H1 {font-family:Arial,Helvetica;font-weight:bold;font-size:36pt}");
      writer.println("  H2 {font-family:Arial,Helvetica;font-weight:bold;font-size:20pt}");
      writer.println("  H3 {font-family:Arial,Helvetica;font-weight:bold;font-size:16pt}");
      writer.println("  CODE {font-size:14}");

      writer.println("  .NORMAL {font-family:Arial,Helvetica;font-style:normal;font-size:14}");
      writer.println("  .BOTTOM {font-family:Arial,Helvetica;font-style:italic;font-size:10pt}");

      writer.println("  .FONT1 {font-family:Arial,Helvetica;font-weight:normal;font-size:18pt}");
      writer.println("  .FONT2 {font-family:Arial,Helvetica;font-weight:normal;font-size:14pt}");
      writer.println("  .FONT3 {font-family:Arial,Helvetica;font-weight:normal;font-size:12pt}");

      writer.println("  a:link {color:white}");
      writer.println("  a:visited{color:white}");
      writer.println("  a:active{color:white}");
      writer.println("-->");
      writer.println("</STYLE>");
      writer.println("<TITLE>" + title + "</TITLE>");
      writer.println("</HEAD>");

      writer.println("<BODY bgcolor=\"#CCCCCC\">");

      writer.println("<H1>" + title + "</H1>");

      writer.println("<HR WIDTH=\"100%\"/>");
    }

    void menu() throws IOException {
      writer.println("<TABLE BORDER=\"1\" WIDTH=\"100%\" BGCOLOR=\"#3366FF\">");
      writer.println("<TR>");
      writer.println("<TD CLASS=FONT1><CENTER>");
      writer.println("<A href=\"" + base + CMD_SERVERS + "\">Servers</A>");
      writer.println("</CENTER></TD>");

      writer.println("<TD CLASS=FONT1><CENTER>");
      writer.println("<A href=\"" + base + CMD_AGENTS + "\">Agents</A>");
      writer.println("</CENTER></TD>");

      writer.println("<TD CLASS=FONT1><CENTER>");
      writer.println("<A href=\"" + base +
		     CMD_MSG_CONS + "\">Consumers</A>");
      writer.println("</CENTER></TD>");

      writer.println("<TD CLASS=FONT1><CENTER>");
      writer.println("<A href=\"" + base + 
		     CMD_SERVICES + "\">Services</A>");
      writer.println("</CENTER></TD>");

      if (debug) {
// 	writer.println("<TD CLASS=FONT1><CENTER>");
// 	writer.println("<A href=\"" + base + 
// 		       CMD_DEBUG + "\" target=\"A3Debug\">Debug</A>");
// 	writer.println("</CENTER></TD>");

	writer.println("<TD CLASS=FONT1><CENTER>");
	writer.println("<A href=\"" + base + 
		       CMD_THREADS + "\">Threads</A>");
	writer.println("</CENTER></TD>");
      }

      writer.println("</TR>");
      writer.println("</TABLE>");
      writer.println("<HR WIDTH=\"100%\"/>");
    }

    void error(Exception exc) throws IOException {
      writer.println("<H2>Error</H2>");
      writer.println("<BLOCKQUOTE><PRE>");
      exc.printStackTrace(writer);
      writer.println("</PRE></BLOCKQUOTE>");
    }

    void help() throws IOException {
      writer.println("<H2>Help</H2>");
      usage();
    }

    void unknown(String cmd) throws IOException {
      writer.println("<H2>Error</H2>");
      writer.println("<P CLASS=FONT2>");
      writer.println("Unknown command \"" + cmd + "\"");
      writer.println("</P>");
      usage();
    }

    void usage() throws IOException {
      writer.println("<H2>Usage</H2>");
      writer.println("<P CLASS=FONT2>");
      writer.println("To be provided.");
      writer.println("</P>");
    }

    void footer() throws IOException {
      writer.println("<HR WIDTH=\"100%\"/>");
      writer.println("<P CLASS=BOTTOM>");
      writer.println("Generated by fr.dyade.aaa.agent.HttpDebug, version 1.0, date:" + new Date().toString());
      writer.println("</P>");

      writer.println("</BODY>");
      writer.println("</HTML>");
    }

    public void doRequest(String request) {
      String cmd = null;

//       System.out.println("request=" + request);

      try {
	StringTokenizer st = new StringTokenizer(request);
	if ((st.countTokens() >= 2) && st.nextToken().equals("GET")) {
	  cmd = st.nextToken();
	  
	  StringBuffer buf = new StringBuffer();
	  try {
	    if ((cmd.equals("/")) ||
		(cmd.equals(CMD_HELP))) {
	      // first page
	      header("A3 AgentServer #" + AgentServer.getServerId());
	      menu();
	      help();
	    } else if (cmd.startsWith(CMD_HELP)) {
	      header("A3 AgentServer #" + AgentServer.getServerId());
	      // help(cmd.substring(CMD_HELP.length()));
	    } else if (cmd.startsWith(CMD_ADMIN)) {
	      header("A3 AgentServer #" + AgentServer.getServerId() +
		     " Administration");
	      menu();

	      if (cmd.equals(CMD_AGENTS)) {
		// a list of agents has been requested
		listAgents(buf);
	      } else if (cmd.startsWith(CMD_SERVERS)) {
		// a list of servers has been requested
		listServers(cmd.substring(CMD_SERVERS.length()), buf);
		if (cmd.substring(CMD_SERVERS.length()).startsWith(CMD_STOP)) {
                  AgentServer.stop(false);
		}
	      } else if (cmd.startsWith(CMD_MSG_CONS)) {
		// a list of consumers has been requested
		listConsumers(cmd.substring(CMD_MSG_CONS.length()), buf);
	      } else if (cmd.startsWith(CMD_SERVICES)) {
		// a list of services has been requested
		listServices(cmd.substring(CMD_SERVICES.length()), buf);
	      } else if (cmd.startsWith(CMD_DUMP_AGENT)) {
		// a dump command has been requested
		dumpAgent(cmd.substring(CMD_DUMP_AGENT.length()), buf);
	      }
	    } else if (cmd.equals(CMD_DEBUG_WAIT)) {
	      header("A3 AgentServer #" + AgentServer.getServerId());
	      writer.println("<H2>Debug Waiting</H2>");
	    } else if (cmd.equals(CMD_DEBUG_RUN)) {
	      header("A3 AgentServer #" + AgentServer.getServerId());
	      writer.println("<H2>Debug Running</H2>");
	    } else if (cmd.startsWith(CMD_DEBUG)) {
	      // a debug tool has been requested
	      header("A3 AgentServer #" + AgentServer.getServerId() +
		     " Debug Tool");
	      debug(cmd.substring(CMD_DEBUG.length()), buf);
	    } else if (cmd.startsWith(CMD_CLASS)) {
	      loadClass(cmd.substring(CMD_CLASS.length()));
	    } else if (cmd.startsWith(CMD_THREADS)) {
	      // a list of threads has been requested
	      header("A3 AgentServer #" + AgentServer.getServerId());
	      menu();
	      listThread(cmd.substring(CMD_THREADS.length()), buf);
	    } else {
	      unknown(cmd);
	    }
	    writer.println(buf.toString());
	  } catch (Exception exc) {
	    error(exc);
	  }
	  footer();
	}
      } catch(IOException exc) {
        logmon.log(BasicLevel.WARN,
                   getName() + ", error in \"" + cmd + "\"", exc);
      } finally {
      }
    }

    /**
     * List all active threads in current ThreadGroup
     */
    void listThread(String cmd, StringBuffer buf) {
      String group = null;
      if ((cmd.length() > 1) && (cmd.charAt(0) == '/'))
	group = cmd.substring(1);

      ThreadGroup tg = Thread.currentThread().getThreadGroup();
      while (tg.getParent() != null)
	tg = tg.getParent();
      int nbt = tg.activeCount();
      Thread[] tab = new Thread[nbt];
      nbt = tg.enumerate(tab);
      buf.append("<H2>List of threads</H2>\n");
      buf.append("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 WIDTH=\"100%\">\n");
      for (int j=0; j<nbt; j++) {
        if (tab[j] == null) continue;
	if ((group != null) &&
	    ! tab[j].getThreadGroup().getName().equals(group))
	  continue;
	buf.append("<TR><TD CLASS=FONT2><PRE>\n");
	buf.append("name=").append(tab[j].getName()).append("\n")
	  .append("group=").append(tab[j].getThreadGroup().getName()).append("\n")
	  .append("isAlive=").append(tab[j].isAlive()).append("\n")
	  .append("isDaemon=").append(tab[j].isDaemon()).append("\n");
	buf.append("</PRE></TD></TR>\n");
	buf.append("");
      }
      buf.append("</TABLE>");

      return;
    }
  
    /**
     * List all agents deployed on the current server
     */
    void listAgents(StringBuffer buf) {
      buf.append("<H2>List of agents</H2>\n");
      buf.append("<PRE>\n");
      buf.append("now=" + AgentServer.engine.now + "\n");
      buf.append("NumberAgents=" + AgentServer.engine.agents.size() + "\n");
      buf.append("NbMaxAgents=" + AgentServer.engine.NbMaxAgents + "\n");
      buf.append("</PRE>\n");
      buf.append("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 WIDTH=\"100%\">\n");
      AgentId list[] = AgentServer.engine.getLoadedAgentIdlist();
      for (int i=0; i<list.length; i++) {
	Agent agent = (Agent) AgentServer.engine.agents.get(list[i]);
	buf.append("<TR>\n");
	buf.append("<TD CLASS=FONT1 WIDTH=\"20%\"><CENTER>\n");
	if (agent != null) {
	  buf.append(urlAgent(list[i]) + "\n");
	} else {
	  buf.append(list[i] + "\n");
	}
	buf.append("</CENTER></TD>\n");
	buf.append("<TD WIDTH=\"80%\"><PRE>\n");
	if (agent != null) {
	  buf.append("name=" + agent.name + "\n\n");
	  buf.append("\tclass=" + agent.getClass().getName() + "\n\n");
	  buf.append("fixed=" + agent.fixed + "\n");
	  buf.append("last=" + agent.last + "\n");
	}
	buf.append("</PRE></TD>\n");
	buf.append("</TR>\n");
      }
      buf.append("</TABLE>");

      return;
    }

    String urlServer(short serverId) {
      if (serverId == AgentServer.getServerId())
	return base;

      try {
        ServerDesc desc = AgentServer.getServerDesc(serverId);
	int port = Integer.parseInt(
	  AgentServer.getServiceArgs(desc.sid,
				     "fr.dyade.aaa.agent.HttpDebug"));
        if (desc.getHostname().equals("localhost"))
          return new String("http://" +
                            InetAddress.getLocalHost().getHostName() +
                            ":" + port);
        else
          return new String("http://" + desc.getHostname() + ":" + port);
      } catch (Exception exc) {}
      return null;
    }


    String urlAgent(AgentId id) {
      String url = urlServer(id.getTo());
      if (url == null)
	return id.toString();

      return new String("<A href=\"" + urlServer(id.getTo()) +
			CMD_DUMP_AGENT + "/" +
			id.toString().substring(1) + "\">" +
			id + "</A>");
    }

    /**
     * List all servers.
     */
    void listServers(String cmd, StringBuffer buf) {
      buf.append("<H2>List of servers</H2>\n");
      buf.append("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0 WIDTH=\"100%\">\n");
      for (short i=0; i<AgentServer.getServerNb(); i++ ) {
	int port = -1;
	ServerDesc desc = null;
        try {
          desc = (ServerDesc) AgentServer.getServerDesc(i);
        } catch (Exception exc) {
          desc = null;
        }
        if (desc == null) continue;
	buf.append("<TR>\n");
	buf.append("<TD CLASS=FONT1 WIDTH=\"20%\" VALIGN=TOP>\n");

	String url = urlServer(desc.sid);
	if ((url != null) && (i == AgentServer.getServerId()) &&
	    (cmd.startsWith(CMD_STOP))) {
	  // The server will be stopped, prevent all new request.
	  url = null;
	}

	if (url != null) {
	  buf.append("<A href=\"" + url + CMD_SERVERS + "\">Server #" + desc.sid + "</A>\n");
	  buf.append("<BLOCKQUOTE><FONT CLASS=FONT3>\n");
	  buf.append("<P><A href=\"" + url + CMD_AGENTS + "\">Agents</A>\n");
	  buf.append("<P><A href=\"" + url + CMD_MSG_CONS + "\">Messages Consumers</A>\n");
	  buf.append("<P><A href=\"" + url + CMD_SERVICES + "\">Services</A>\n");
	  buf.append("</FONT></BLOCKQUOTE>\n");
	} else {
	  buf.append("Server #" + desc.sid + "\n");
	}
	buf.append("</TD>\n" +
		   "<TD WIDTH=\"80%\" VALIGN=TOP>\n");
	
	if (url != null) {
	  buf.append("<BLOCKQUOTE><FORM ACTION=\"" +
		     url + CMD_SERVERS + CMD_STOP + 
		     "\" METHOD=\"GET\">\n" +
		     "<INPUT TYPE=\"submit\" VALUE=\"STOP\" NAME=\"A\">\n" +
		     "</FORM></BLOCKQUOTE>\n");
	}

	buf.append("<PRE><CODE>\n" +
		   "name=" + desc.name + "\n");
// 	buf.append("isTransient=" + desc.isTransient + "\n");
	if (desc.gateway == desc.sid) {
	  buf.append("domain=<A href=\"" + base + CMD_MSG_CONS +
		     "/" + desc.domain.getName() + "\">" +
		     desc.domain.getName() + "</A>\n");
	  buf.append("hostname=" + desc.getHostname() + "\n");
	  buf.append("port=" + desc.getPort() + "\n");
	  if (desc.active) {
	    buf.append("active=" + desc.active + "\n");
	  } else {
	    buf.append("last=" + desc.last + "\n");
	    buf.append("retry=" + desc.retry);
	  }
	} else {
	  buf.append("gateway=#" + desc.gateway + "\n");
	}
	buf.append("</CODE></PRE></TD>\n");
	buf.append("</TR>\n");
      }
      buf.append("</TABLE>");

      return;
    }

//     int getArg(String cmd) {
//       if (cmd.charAt(0) != '+')
// 	return -1;

//       int i = 1;
//       for (; i < cmd.length();i += 1) {
// 	if ((cmd.charAt(i) < '0') || (cmd.charAt(i) > '9'))
// 	  break;
//       }
//       try {
// 	if (i > 1)
// 	  return Integer.parseInt(cmd.substring(1, i));
//       } catch (Exception exc) {
// 	// Can never happened
//       }
//       return -1;
//     }

    /**
     * 
     */
    void listConsumers(String cmd, StringBuffer buf) {
      String msgConsId = null;
      int sub = -1;

      if (cmd.startsWith(CMD_QUEUE)) {
	sub = 0;
	msgConsId = cmd.substring(CMD_QUEUE.length() +1);
      } else if (cmd.startsWith(CMD_START)) {
	sub = 1;
	msgConsId = cmd.substring(CMD_START.length() +1);
      } else if (cmd.startsWith(CMD_STOP)) {
	sub = 2;
	msgConsId = cmd.substring(CMD_STOP.length() +1);
      }

      buf.append("<H2>List of messages consumers</H2>\n");
      buf.append("<TABLE BORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"0\" WIDTH=\"100%\">\n");
      for (Enumeration c=AgentServer.getConsumers(); c.hasMoreElements(); ) {
	MessageConsumer cons = (MessageConsumer) c.nextElement();

	buf.append("<TR>\n" +
		   "<TD CLASS=FONT1 VALIGN=\"TOP\" WIDTH=\"20%\">\n" +
		   "<CENTER>").append(cons.getName()).append("</CENTER>\n");
	buf.append("</TD>\n");
	buf.append("<TD WIDTH=\"80%\">");

	buf.append("<TABLE BORDER=\"1\" WIDTH=\"100%\"><TR><TD COLSPAN=\"3\">");
	
	if (msgConsId.equals(cons.getName())) {
	  if (sub == 1) {
	    try {
	      cons.start();
	    } catch (Exception exc) {
	      buf.append("<TR><TD COLSPAN=\"3\"><PRE WIDTH=\"80\">");
	      buf.append(exc.toString()).append(" during starting.\n");
	      buf.append("</PRE></TD></TR>");
	    }
	  } else if (sub == 2) {
	      cons.stop();
	  }
	}

	buf.append(cons.getClass().getName());
	if (cons.isRunning()) {
	  buf.append(" is running.");
	} else {
	  buf.append(" is stopped.");
	}
	buf.append("</TD></TR><TR>");

	buf.append("<TD CLASS=FONT1 ALIGN=\"CENTER\" BGCOLOR=\"#3366FF\"><A href=\"")
	  .append(base).append(CMD_MSG_CONS)
          .append(CMD_STOP + "+").append(cons.getName())
	  .append("\">STOP</A>\n")
	  .append("</TD>");

	buf.append("<TD CLASS=FONT1 ALIGN=\"CENTER\" BGCOLOR=\"#3366FF\"><A href=\"")
	  .append(base).append(CMD_MSG_CONS)
          .append(CMD_START + "+").append(cons.getName())
	  .append("\">START</A>\n")
	  .append("</TD>");

	buf.append("<TD CLASS=FONT1 ALIGN=\"CENTER\" BGCOLOR=\"#3366FF\"><A href=\"")
	  .append(base).append(CMD_MSG_CONS)
          .append(CMD_QUEUE + "+").append(cons.getName())
	  .append("\">QUEUE</A>\n")
	  .append("</TD>");

	buf.append("</TR><TR><TD COLSPAN=\"3\">");
	buf.append("<PRE WIDTH=\"80\">\n");
	buf.append(cons.toString());
	buf.append("</PRE>");
	buf.append("</TD></TR>");
	
	if ((msgConsId.equals(cons.getName())) && (sub == 0) && (cons.getQueue().size() > 0)) {
	  buf.append("<TR><TD COLSPAN=\"3\"><PRE WIDTH=\"80\">");
	  dumpQueue(buf, cons.getQueue().toString().toCharArray());
	  buf.append("</PRE></TD></TR>");
	}

	buf.append("</TABLE>");
	buf.append("</TD>");
	buf.append("</TR>\n");
      }
      buf.append("</TABLE>");

      return;
    }

    void dumpQueue(StringBuffer buf,  char dump[]) {
      int j;
      AgentId id;

      try {
	int idx = 0;
	if (dump[idx++] != '(') throw new IllegalArgumentException();
	while (true) {
	  try {
	    if (dump[idx++] != '(') throw new IllegalArgumentException();
	    // get "from="
	    while (dump[idx] != '=') buf.append(dump[idx++]);
	    buf.append(dump[idx++]);
	    // Get AgentId
	    if (dump[idx] != '#') throw new IllegalArgumentException();
	    id = null;
	    j = dumpAgentId(dump, idx);
	    if (j != -1)
	      id = AgentId.fromString(new String(dump, idx, j-idx));
	    if (id == null)
	      throw new IllegalArgumentException();
	    // The rigth URL
	    buf.append(urlAgent(id));
	    idx = j;
	    // get ",to="
	    while (dump[idx] != '=') buf.append(dump[idx++]);
	    buf.append(dump[idx++]);
	    // Get AgentId
	    if (dump[idx] != '#') throw new IllegalArgumentException();
	    id = null;
	    j = dumpAgentId(dump, idx);
	    if (j != -1)
	      id = AgentId.fromString(new String(dump, idx, j-idx));
	    if (id == null)
	      throw new IllegalArgumentException();
	    // The rigth URL
	    buf.append(urlAgent(id));
	    idx = j;
	    // get ",not="
	    if (dump[idx] == ',')
	      buf.append('\n');
	    else
	      throw new IllegalArgumentException();
	    idx++;
	    while (dump[idx] != '=') buf.append(dump[idx++]);
	    buf.append(dump[idx++]);
	    // Copy notification with AgentId transformation
	    if (dump[idx++] != '[') throw new IllegalArgumentException();
	    while (dump[idx] != ']') {
	      if (dump[idx] == '#') {
		id = null;
		j = dumpAgentId(dump, idx);
		if (j != -1)
		  id = AgentId.fromString(new String(dump, idx, j-idx));
		if (id != null) {
		  // The rigth URL
		  buf.append(urlAgent(id));
		  idx = j;
		}
	      }
	      buf.append(dump[idx++]);
	    }
	    idx++; // skip ']'
	    idx++; // skip ','
	    // get ",update="
	    if (dump[idx] == ',')
	      buf.append('\n');
	    else
	      throw new IllegalArgumentException();
	    idx++;
	    while (dump[idx] != '=') buf.append(dump[idx++]);
	    buf.append(dump[idx++]);
	    //
	    if (dump[idx++] != '[') throw new IllegalArgumentException();
	    while (dump[idx] != ']') buf.append(dump[idx++]);
	    idx++;
	    // get deadline if there is one
	    if (dump[idx] != ')') {
	      if (dump[idx] == ',')
		buf.append('\n');
	      else
		throw new IllegalArgumentException();
	      idx++;
	      while (dump[idx] != ')') buf.append(dump[idx++]);
	    }
	    idx++;
	    if (dump[idx] == ')') 
	      break;
	    else
	      buf.append("\n<hr>");
	  } catch (IllegalArgumentException exc) {
	    // search next '('
	    throw exc;
	  }
	}
      } catch (Exception exc) {
	exc.printStackTrace();
      }
    }

    void listServices(String cmd, StringBuffer buf) {
      int sub = -1;
      String sclass = null;

      if (cmd.startsWith(CMD_START)) {
	sub = 1;
	sclass = cmd.substring(CMD_START.length() +1);
      } else if (cmd.startsWith(CMD_STOP)) {
	sub = 2;
	sclass = cmd.substring(CMD_STOP.length() +1);
      } else if (cmd.startsWith(CMD_REMOVE)) {
	sub = 3;
	sclass = cmd.substring(CMD_REMOVE.length() +1);
      }

      buf.append("<H2>List of services</H2>\n");
      buf.append("<TABLE BORDER=\"1\" WIDTH=\"100%\">\n");

      if (sub == 3) {
	buf.append("<TR><TD><PRE>");
	try {
	  ServiceManager.stop(sclass);
	  buf.append("\nService <").append(sclass).append("> stopped.");
	} catch (Exception exc) {
	  buf.append("\nCan't stop service \"")
	    .append(sclass).append("\" :\n\t")
	    .append(exc.getMessage());
	}
	try {
	  ServiceManager.unregister(sclass);
	  buf.append("\nService <").append(sclass).append("> unregistred.");
	} catch (Exception exc) {
	  buf.append("\nCan't unregister service \"")
	    .append(sclass).append("\" :\n\t")
	    .append(exc.getMessage());
	}
	buf.append("</PRE></TD></TR>\n");
      }

      ServiceDesc services[] = ServiceManager.getServices();
      for (int i=0; i<services.length; i++ ){
	buf.append("<TABLE BORDER=\"1\" WIDTH=\"100%\">\n");
	buf.append("<TR><TD CLASS=FONT1 VALIGN=\"TOP\" COLSPAN=\"2\">")
	  .append(services[i].getClassName());

	if (services[i].getClassName().equals(sclass)) {
	  if (sub == 1) {
	    try {
	      ServiceManager.start(sclass);
	    } catch (Exception exc) {
	      buf.append("<PRE>\nCan't start service: \n\t")
		.append(exc.getMessage())
		.append("</PRE>");
	    }
	  } else if (sub == 2) {
	    try {
	      ServiceManager.stop(sclass);
	    } catch (Exception exc) {
	      buf.append("<PRE>\nCan't stop service: \n\t")
		.append(exc.getMessage())
		.append("</PRE>");
	    }	    
	  }
	}

	buf.append("</TD></TR>");

	buf.append("<TR><TD ROWSPAN=\"2\" WIDTH=\"70%\"><PRE>")
	  .append("\narguments=").append(services[i].getArguments())
	  .append("\ninitialized=").append(services[i].isInitialized())
	  .append("\nrunning=").append(services[i].isRunning())
	  .append("</PRE></TD>\n");

	buf.append("<TD CLASS=FONT1 ALIGN=\"CENTER\" BGCOLOR=\"#3366FF\">")
	  .append("<A href=\"")
	  .append(base).append(CMD_SERVICES);
	if (services[i].isRunning()) {
	  buf.append(CMD_STOP + "+")
	    .append(services[i].getClassName())
	    .append("\">STOP</A>\n");
	} else {
	  buf.append(CMD_START + "+")
	    .append(services[i].getClassName())
	    .append("\">START</A>\n");
	}
	buf.append("</TD></TR>\n");

	buf.append("<TR><TD CLASS=FONT1 ALIGN=\"CENTER\" BGCOLOR=\"#3366FF\">")
	  .append("<A href=\"")
	  .append(base).append(CMD_SERVICES)
	  .append(CMD_REMOVE + "+")
	  .append(services[i].getClassName())
	  .append("\">REMOVE</A>\n")
	  .append("</TD></TR>\n");

// 	buf.append("<TR><TD><PRE><CODE>\n");
// 	buf.append("class=" + services[i].getClassName() + "\n");
// 	buf.append("args=" + services[i].getArguments());
// 	buf.append("</CODE></PRE></TD></TR>\n");
	
	buf.append("</TABLE>");
      }
      buf.append("</TABLE>");

      return;
    }

    /**
     *  Parses an AgentId from a char array, it determines the sub-array
     * that contain an AgentId, then you can get it with fromString:
     * <pre>
     * end = dumpAgentId(dump, start);
     * if (end != -1)
     *   id = AgentId.fromString(new String(dump, start, end-start));
     * </pre>
     *
     * @param dump	The char array.
     * @param idx	The current index in array (shoul be the first
     *			charracter of the AgentId: '#').
     *
     * @return		The index of first char after the AgentId, -1 if
     *			thebeginning of the array do not correspond to an
     *			AgentId.
     */
    int dumpAgentId(char dump[], int idx) {
      if (dump[idx] != '#')
	return -1;

      int j = idx+1;
      while ((j < dump.length) &&
	     (dump[j] >= '0') && (dump[j] <= '9')) j++;
      if ((j == (idx +1)) || (dump[j] != '.') || (j == dump.length))
	return -1;

      idx = j; j = j+1;
      while ((j < dump.length) &&
	     (dump[j] >= '0') && (dump[j] <= '9')) j++;
      if ((j == (idx +1)) || (dump[j] != '.') || (j == dump.length))
	return -1;

      idx = j; j = j+1;
      while ((j < dump.length) &&
	     (dump[j] >= '0') && (dump[j] <= '9')) j++;

     if (j == (idx +1)) return -1;

     return j;
    }

    /**
     * Executes a dump command.
     *
     * @ param agentid     String which contains the agentid 
     * like #x.y.z ,x,y,z are numbers.
     */
    void dumpAgent(String cmd, StringBuffer buf) {
      AgentId id = null;
      try {
	if (cmd.charAt(0) != '/') throw new IllegalArgumentException();
	id = AgentId.fromString(new String("#" + cmd.substring(1)));
	if (id == null) throw new IllegalArgumentException();
      } catch (IllegalArgumentException exc) {
	buf.append("<H2>Error</H2>\n" +
		   "<P>\n" +
		   "Can't parse AgentId #x.y.z in \"" + cmd + "\"\n" +
		   "</P>\n");
	return;
      }

      buf.append("<H2>Dump agent " + id + "</H2>\n");

      Agent agent = (Agent) AgentServer.engine.agents.get(id);
      if (agent == null) {
	buf.append("<P CLASS=FONT2>\n" +
		   "Agent not loaded in memory.\n" +
		   "</P>\n");
	return;
      }

      try {
	char dump[] = agent.toString().toCharArray();
	for (int i=0; i<dump.length; i++) {
	  if (dump[i] == '(') {
	    buf.append("<TABLE BORDER=1 CELLSPACING=0 CELLPADDING=0>\n");
	    buf.append("<TR><TD CLASS=FONT1><PRE>\n");
	  } else if (dump[i] == ',') {
	    buf.append("\n");
	  } else if (dump[i] == ')') {
	    buf.append("</PRE></TD></TR>\n");
	    buf.append("</TABLE>");
	  } else if (dump[i] == '#') {
	    id = null;
	    int j = dumpAgentId(dump, i);
	    if (j != -1)
	      id = AgentId.fromString(new String(dump, i, j-i));
	    if (id == null) {
	      buf.append(dump[i]);
	    } else {
	      // TODO: the rigth URL
	      buf.append(urlAgent(id));
	      i = j -1;
	    }
	  } else {
	    buf.append(dump[i]);
	  }
	}
      } catch (IllegalArgumentException exc) {
	buf.setLength(0);
	buf.append("<H2>Error</H2>\n" +
		   "<P>\n" +
		   "Can't parse AgentId #x.y.z in \"" + cmd + "\"\n" +
		   "</P>\n");
	return;
      }
    }

    /**
     *
     */
    void debug(String cmd, StringBuffer buf) {
      ServerSocket server = null;
      int listen = -1;

      buf.append("<H2>Debug Tool</H2>\n");

      try {
	server = new ServerSocket(0);
      } catch (IOException exc) {
	buf.append(exc.toString()).append(" during connection.\n");
	return;
      }
      listen = server.getLocalPort();

      // The applet
      buf.append("<APPLET CODE=\"AppletDebug.class\" CODEBASE=\"")
	.append(CMD_CLASS).append("\" WIDTH=\"800\" HEIGHT=\"200\">\n")
	.append("<PARAM NAME=\"host\" VALUE=\"").append(host).append("\"/>\n")
	.append("<PARAM NAME=\"port\" VALUE=\"").append(listen).append("\"/>\n")
	.append("<PARAM NAME=\"url\" VALUE=\"").append(base).append(CMD_DEBUG_WAIT).append("\"/>\n")
	.append("</APPLET>\n");
      
      // The Internal Frame
      buf.append("<IFRAME name=\"debug\" src=\"")
	.append(CMD_DEBUG_WAIT)
	.append("\" width=\"800\" height=\"400\" scrolling=\"auto\" frameborder=\"1\">\n")
	.append("Your browser does not support internal frames (HTML 4.0)\n.")
	.append("</IFRAME>\n");

//       DebugMonitor dmon = new DebugMonitor(server);
//       bp.start();
    }

    void loadClass(String cmd) {
      BufferedOutputStream bos = null;

//       System.out.println("load:" + cmd);
      try {
	bos = new BufferedOutputStream(socket.getOutputStream());
	File file = new File(cmd.substring(1));
	byte[] c = new byte[(int) file.length()];
	new FileInputStream(file).read(c);
	bos.write(c, 0, c.length);
      } catch (IOException exc) {
	exc.printStackTrace();
      } finally {
	try {
	  bos.flush();
	  bos.close();
	} catch (IOException exc) {}
      }
    }
  }

  class DebugMonitor extends Daemon {
    ServerSocket server = null;
    int listen = -1;

    public DebugMonitor() {
      super("DebugMonitor");

      // Get the logging monitor from HttpDebug (overload Daemon setup)
      logmon = HttpDebug.xlogmon;

      try {
	server = new ServerSocket(0);
	listen = server.getLocalPort();
      } catch (IOException exc) {
	server = null;
	listen = -1;
      }
    }

    public void run() {
      Socket socket = null;
      PrintWriter writer = null;
      
//       while (isRunning) {
// 	canStop = true;
// 	socket = server.accept();
// 	writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

// 	while (isRunning) {
// 	  canStop = false;
// 	  System.out.println("writes:" + base + HttpDebugMonitor.CMD_DEBUG_RUN);
// 	  writer.println(base + HttpDebugMonitor.CMD_DEBUG_RUN);
// 	  if (writer.checkError())
// 	    throw new IOException();
// 	  canStop = true;

// 	  try {
// 	    Thread.currentThread().sleep(1000);
// 	  } catch (InterruptedException exc) {}

// 	  canStop = false;
// 	  System.out.println("writes:" + base + HttpDebugMonitor.CMD_DEBUG_WAIT);
// 	  writer.println(base + HttpDebugMonitor.CMD_DEBUG_WAIT);
// 	  if (writer.checkError())
// 	    throw new IOException();
// 	  canStop = true;

// 	  try {
// 	    System.out.println("wait");
// 	    Thread.currentThread().sleep(10000);
// 	  } catch (InterruptedException exc) {}
// 	}
//       } catch (IOException exc) {
// 	try {
// 	  socket.close();
// 	} catch (IOException exc2) {}
// 	exc.printStackTrace();
//       } finally {
//         finish();
//       }
    }
   
    protected void close() {}

    protected void shutdown() {}
  }
}
