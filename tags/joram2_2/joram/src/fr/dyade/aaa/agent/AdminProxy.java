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
import java.text.*;
import java.util.*;
import java.lang.reflect.*;

import org.objectweb.monolog.api.BasicLevel;
import org.objectweb.monolog.api.Monitor;

import fr.dyade.aaa.util.Daemon;

/**
 * A <code>AdminProxy</code> service provides an interface to access
 * to administration functions in running agent servers.
 * <p>
 * The <code>AdminProxy</code> service can be configured by the way of
 * service argument:
 * the TCP port number, by default this port is 8091.
 * the number of monitor needed to handled requests.
 */
public class AdminProxy {
  /** RCS version number of this file: $Revision: 1.4 $ */
  public static final String RCS_VERSION="@(#)$Id: AdminProxy.java,v 1.4 2002-01-16 12:46:47 joram Exp $"; 

  static AdminProxy proxy = null;

  public static boolean debug = true;

  /** Property that define the TCP listen port */
  public final static String LISTENPORT = "fr.dyade.aaa.agent.AdminProxy.port";
  /** The TCP listen port */
  private static int port = 8091;
  /** The number of monitors.*/
  private static int nbm = 1;
  /** Property that define the number of monitor */
  public final static String NBMONITOR = "fr.dyade.aaa.agent.AdminProxy.nbm";
  /** Hashtable that contain all <code>Process</code> of running AgentServer */
  Hashtable ASP = null;

  AdminMonitor monitors[] = null;
  ServerSocket listen = null;

  static Monitor xlogmon = null;

  /**
   * Initializes the package as a well known service.
   * <p>
   * Creates a <code>AdminProxy</code> proxy listen on .
   *
   * @param args	parameters from the configuration file
   * @param firstTime	<code>true</code> when service starts anew
   */
  public static void init(String args, boolean firstTime) throws Exception {
    try {
      if (args.length()!=0) {
	port = Integer.parseInt(args);
      } else {
        port = Integer.parseInt(AgentServer.getProperty(LISTENPORT, "8091"));
      }
    } catch (NumberFormatException exc) {
      port = 8091;
    }

    try {
      nbm = Integer.parseInt(AgentServer.getProperty(NBMONITOR, "1"));
    } catch (NumberFormatException exc) {
      nbm = 1;
    }

    // Get the logging monitor from current server MonologMonitorFactory
    xlogmon = Debug.getMonitor(Debug.A3Service + ".AdminProxy" +
                               ".#" + AgentServer.getServerId());

    if (proxy == null)
      proxy = new AdminProxy();
    start();
  }

  /**
   * Creates an AdminProxy service.
   *
   * @param port  TCP listen port of this proxy
   */
  private AdminProxy() throws IOException {
    for (int i=0; ; i++) {
      try {
        listen = new ServerSocket(port);
        break;
      } catch (BindException exc) {
        if (i > 5) throw exc;
        try {
	  // Wait ~15s: n*(n+1)*500 ms with n=5
          Thread.currentThread().sleep(i * 500);
        } catch (InterruptedException e) {}
      }
    }

    ASP = new Hashtable();

    monitors = new AdminMonitor[nbm];
    for (int i=0; i<monitors.length; i++) {
      monitors[i] = new AdminMonitor("AdminProxy#" +
                                     AgentServer.getServerId() + '.' + i);
    }
  }

  public static void start() {
    for (int i=0; i<proxy.monitors.length; i++) {
      proxy.monitors[i].start();
    }
  }

  public static void stop() {
    for (int i=0; i<proxy.monitors.length; i++) {
      if (proxy.monitors[i] != null) proxy.monitors[i].stop();
    }
    proxy = null;
  }

  static final String HELP = "help";
  static final String NONE = "";

  // Server's administration commands
  static final String LAUNCH_SERVER = "launch";
  static final String STOP_SERVER = "quit";
  static final String CRASH_SERVER = "crash";

    // Environment control
  static final String SET_VARIABLE = "set";
  static final String GET_VARIABLE = "get";

    // JVM's monitoring and control
  static final String GC = "gc";
  static final String THREADS = "threads";

    // Consumer's administration commands
  static final String LIST_MCONS = "consumers";
  static final String START_MCONS = "start";
  static final String STOP_MCONS = "stop";

  // Service's administration commands
  static final String LIST_SERVICE = "services";
  static final String ADD_SERVICE = "add";
  static final String REMOVE_SERVICE = "remove";

  static final String PUT_FILE = "putf";

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

  class AdminMonitor extends Daemon {
    Socket socket = null;
    BufferedReader reader = null;
    PrintWriter writer = null;

    /**
     * Constructor.
     */
    protected AdminMonitor(String name) {
      super(name);
      // Get the logging monitor from AdminProxy (overload Daemon setup)
      logmon = AdminProxy.xlogmon;
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
	running = false;
	thread = null;
	// Close any ressources no longer needed, eventually stop the
	// enclosing component.
	shutdown();
      }
    }

    public void shutdown() {
      try {
	listen.close();
      } catch (Exception exc) {}
      listen = null;
    }

    public void doRequest(String request) {
      String cmd = null;

      logmon.log(BasicLevel.DEBUG, getName() + ", request=" + request);

      try {
	// Tokenizes the request to parse it.
	StringTokenizer st = new StringTokenizer(request);

	cmd = st.nextToken();
	if (cmd.equals(STOP_SERVER)) {
	  // Stop the AgentServer
          logmon.log(BasicLevel.WARN, getName() + ", bye.");
	  AgentServer.stop();
	} else if (cmd.equals(CRASH_SERVER)) {
          // Stop the AgentServer
          logmon.log(BasicLevel.WARN, getName() + ", crash!");
	  System.exit(0);
	} else if (cmd.equals(GC)) {
	  Runtime runtime = Runtime.getRuntime();
	  writer.println("before: " +
			 runtime.freeMemory() + " octets free / " +
			 runtime.totalMemory() + " octets.");
	  runtime.gc();
	  writer.println("after: " +
			 runtime.freeMemory() + " octets free / " +
			 runtime.totalMemory() + " octets.");
	} else if (cmd.equals(SET_VARIABLE)) {
	  try {
	    if (st.countTokens() != 2)
	      throw new Exception("Usage: set property value");

	    String property = st.nextToken();
	    String value = st.nextToken();

	    // finds variable class and name
	    int pindex = property.lastIndexOf('.');
	    if (pindex == -1) {
	      // bad formed property name, ignores
	      throw new Exception("bad formed property name: " + property);
	    }
	    String varClassName = property.substring(0, pindex);
	    String varName = property.substring(pindex + 1);

	    try {
	      // finds variable
	      Class varClass = Class.forName(varClassName);
	      Field var = varClass.getDeclaredField(varName);
	      // sets variable according to its type
	      String varType = var.getType().getName();
	      if (varType.equals("boolean") ||
		  varType.equals("java.lang.Boolean")) {
		var.set(null, new Boolean(value));
	      } else if (varType.equals("int") ||
			 varType.equals("java.lang.Integer")) {
		var.set(null, new Integer(value));
	      } else if (varType.equals("java.lang.String")) {
		var.set(null, value);
	      } else {
		throw new Exception("error setting property " +
				    varClassName + "." + varName +
				    ": unexpected type " + varType);
	      }
	    } catch (Exception exc) {
	      if (debug) exc.printStackTrace(writer);
	      throw new Exception("error setting property " +
				  varClassName + "." + varName +
				  ": " + exc.getMessage());
	    }
	    writer.println("done.");
	  } catch (Exception exc) {
	    writer.println(exc.getMessage());
	  }
	} else if (cmd.equals(GET_VARIABLE)) {
	  try {
	    if (st.countTokens() != 1)
	      throw new Exception("Usage: get property");

	    String property = st.nextToken();

	    // finds variable class and name
	    int pindex = property.lastIndexOf('.');
	    if (pindex == -1) {
	      // bad formed property name, ignores
	      throw new Exception("bad formed property name: " + property);
	    }
	    String varClassName = property.substring(0, pindex);
	    String varName = property.substring(pindex + 1);

	    try {
	      // finds variable
	      Class varClass = Class.forName(varClassName);
	      Field var = varClass.getDeclaredField(varName);
	      // get the variable value
	      Object value = var.get(null);
	      writer.println(property + " = " + value);
	    } catch (Exception exc) {
	      if (debug) exc.printStackTrace(writer);
	      throw new Exception("error getting property " +
				  varClassName + "." + varName +
				  ": " + exc.getMessage());
	    }
	  } catch (Exception exc) {
	    writer.println(exc.getMessage());
	  }
	} else if (cmd.equals(THREADS)) {
	  String group = null;
	  if (st.hasMoreTokens())
	    group = st.nextToken();

	  ThreadGroup tg = Thread.currentThread().getThreadGroup();
	  while (tg.getParent() != null)
	    tg = tg.getParent();
	  int nbt = tg.activeCount();
	  Thread[] tab = new Thread[nbt];
	  tg.enumerate(tab);

	  for (int j=0; j<nbt; j++) {
	    if ((group != null) &&
		! tab[j].getThreadGroup().getName().equals(group))
	      continue;
	    writer.println("+----------------------------------------");
	    writer.println("[" +
			   ((group==null)?(tab[j].getThreadGroup().getName() + "."):"") +
			   tab[j].getName() + "]" +
			   (tab[j].isAlive()?" alive":"") +
			   (tab[j].isDaemon()?" daemon":"") + "\n " +
			   tab[j]);
	  }
	} else if (cmd.equals(LIST_MCONS)) {
	  for (int i=0; i<AgentServer.consumers.length; i++ ){
	    MessageConsumer cons = AgentServer.consumers[i];
	    writer.println("+----------------------------------------");
	    writer.println(cons);
	  }
	} else if (cmd.equals(START_MCONS)) {
	  String domain = null;
	  if (st.hasMoreTokens()) {
	    // start the identified consumer.
	    domain = st.nextToken();
	  }
	  for (int i=0; i<AgentServer.consumers.length; i++ ){
	    MessageConsumer cons = AgentServer.consumers[i];
	  
	    if (((domain == null) || domain.equals(cons.getName())) &&
		(! cons.isRunning())) {
	      try {
		cons.start();
		writer.println("start " + cons.getName() + " done.");
	      } catch (Exception exc) {
		writer.println("Can't start "+ cons.getName() + ": " +
			       exc.getMessage());
		if (debug) exc.printStackTrace(writer);
	      }
	    }
	  }
	} else if (cmd.equals(STOP_MCONS)) {
	  String domain = null;
	  if (st.hasMoreTokens()) {
	    // stop the identified consumer.
	    domain = st.nextToken();
	  }
	  for (int i=0; i<AgentServer.consumers.length; i++ ){
	    MessageConsumer cons = AgentServer.consumers[i];
	  
	    if (((domain == null) || domain.equals(cons.getName())) &&
		cons.isRunning()) {
	      cons.stop();
	      writer.println("stop " + cons.getName() + " done.");
	    }
	  }
	} else if (cmd.equals(LIST_SERVICE)) {
	  ServiceDesc services[] = ServiceManager.getServices();
	  for (int i=0; i<services.length; i++ ){
	    writer.println("+----------------------------------------");
	    writer.println(services[i].getClassName() + " (" +
			   services[i].getArguments() + ")" +
			   (services[i].isInitialized()?" initialized ":"") +
			   (services[i].isRunning()?" running":""));
	  }
	} else if (cmd.equals(ADD_SERVICE)) {
	  // Add a new Service
	  String sclass = null;
	  String args = null;
	  try {
	    sclass = st.nextToken();
	    if (st.hasMoreTokens())
	      args = st.nextToken();
	  } catch (NoSuchElementException exc) {
	    writer.println("Usage: add <sclass> [<args>]");
	  }
	  try {
	    ServiceManager.register(sclass, args);
	    writer.println("Service <" + sclass + "> registred.");
	    ServiceManager.start(sclass);
	    writer.println("Service <" + sclass + "> started.");
	  } catch (Exception exc) {
	    // Report the error
	    writer.println("Can't start service: " + exc.getMessage());
	    if (debug) exc.printStackTrace(writer);
	  }
	} else if (cmd.equals(REMOVE_SERVICE)) {
	  // Remove an existing Service
	  String sclass = null;
	  try {
	    sclass = st.nextToken();
	  } catch (NoSuchElementException exc) {
	    writer.println("Usage: " + REMOVE_SERVICE + " <sclass> [<args>]");
	  }
	  try {
	    ServiceManager.stop(sclass);
	    writer.println("Service <" + sclass + "> stopped.");
	  } catch (Exception exc) {
	    writer.println("Can't stop service: " + exc.getMessage());
	    if (debug) exc.printStackTrace(writer);
	  }
	  try {
	    ServiceManager.unregister(sclass);
	    writer.println("Service <" + sclass + "> unregistred.");
	  } catch (Exception exc) {
	    writer.println("Can't unregister service: " + exc.getMessage());
	    if (debug) exc.printStackTrace(writer);
	  }
	} else if (cmd.equals(LAUNCH_SERVER)) {
          int sid = -1;
          try {
            sid = Integer.parseInt(st.nextToken());
          } catch (Exception exc) {
            writer.println("Usage: " + LAUNCH_SERVER + " <sid>");
          }
          
          try {
            writer.println(AdminProxy.startAgentServer((short) sid));
          } catch (Exception exc) {
            writer.println("Can't launch server: " + exc.getMessage());
            if (debug) exc.printStackTrace(writer);
          }
	} else if (cmd.equals(PUT_FILE)) {
          String sourceName = null;
          long fileSize = 0;
          int blockSize = 0;
          String destinationName = null;

          try {
            if (((sourceName = st.nextToken ()) == null) || (sourceName.trim().length() == 0))
	      throw new Exception("No source file name has been given");
            fileSize = new Long (st.nextToken ()).intValue ();
            blockSize = new Integer (st.nextToken ()).intValue ();
            destinationName = st.nextToken ();
            if ((destinationName == null) ||
                (destinationName.trim().length() == 0))
              destinationName = sourceName;
            File f = new File (destinationName);
            FileOutputStream fout = null;
            DataInputStream din = null;
            fout = new FileOutputStream (f);
            din = new DataInputStream (socket.getInputStream());
            for (long i = 0; i * blockSize < fileSize; i++) {
              byte [] currentBlock = new byte [blockSize];
              din.read (currentBlock, 0, (int) Math.min (blockSize, fileSize - (i * blockSize)));
              fout.write (currentBlock, 0, (int) (Math.min (blockSize, fileSize - (i * blockSize))));
              fout.flush ();
              // send just a character to realize flow control
              writer.print (".");
              writer.flush ();
            } 
            fout.close();
	  } catch (Exception exc) {
	    // Report the error
	    writer.println("Unable to read source file name: " +
                           exc.getMessage());
	    if (debug) exc.printStackTrace(writer);
	  }
	} else if (cmd.equals(NONE)) {
	} else if (cmd.equals(HELP)) {
	  writer.println(
	    "Description of available commands:\n" +
	    "\t" + HELP +
	    "\n\t\tGives the summary of the options.\n" +
	    "\t" + STOP_SERVER +
	    "\n\t\tStops the server.\n" +
	    "\t" + SET_VARIABLE + "variable value" +
	    "\n\t\tSet the specified static variable with the given value.\n" +
	    "\t" + GET_VARIABLE +
	    "\n\t\tReturn the value of the specified static variable.\n" +
	    "\t" + GC + 
	    "\n\t\tRun the garbage collector in the specified A3 server.\n" +
	    "\t" + THREADS + " [group]" +
	    "\n\t\tList all threads in server JVM.\n" +
	    "\t" + LIST_MCONS +
	    "\n\t\tList all defined consumers.\n" +
	    "\t" + START_MCONS + " [domain]" +
	    "\n\t\tStarts the specified MessageConsumer.\n" +
	    "\t" + STOP_MCONS + " [domain]" +
	    "\n\t\tStops the specified MessageConsumer.\n" +
	    "\t" + LIST_SERVICE +
	    "\n\t\tList all registered services.\n" +
	    "\t" + ADD_SERVICE + " classname arguments" +
	    "\n\t\tRegisters and starts the specified Service.\n" +
	    "\t" + REMOVE_SERVICE + " classname" +
	    "\n\t\tStops then unregister the specified Service.\n" +
	    "\t" + LAUNCH_SERVER + " sid" +
	    "\n\t\tLaunch the specified A3 Server.\n");
	} else {
	  writer.println("unknown command:" + cmd);
	}
      } finally {
      }
    }
  }

  /**
   * Starts an agent server from its id. Be careful an AgentServer must
   * be initialized.
   *
   * @param sid		id of agent server to start
   */
  public static String startAgentServer(short sid) throws Exception {
    xlogmon.log(BasicLevel.DEBUG,
               "AdminProxy#" + AgentServer.getServerId() +
               ": start AgentServer#" + sid);

    Process p = (Process) proxy.ASP.get(new Short(sid));
    if (p != null) {
      try {
	xlogmon.log(BasicLevel.DEBUG,
		    "AdminProxy#" + AgentServer.getServerId() +
		    ": AgentServer#" + sid + " -> " + p.exitValue());
      } catch (IllegalThreadStateException exc) {
        // there is already another AS#sid running
	xlogmon.log(BasicLevel.WARN,
		    "AdminProxy#" + AgentServer.getServerId() +
		    ": AgentServer#" + sid + " already running.");
        throw new IllegalStateException("AgentServer#" + sid +
                                        " already running.");
      }
    }

    String javapath = 
      new File(new File(System.getProperty("java.home"), "bin"),
               "java").getPath();
    String classpath = System.getProperty("java.class.path");
    String userdir = System.getProperty("user.dir");

    if (! AgentServer.getHostname(sid).equals(AgentServer.getHostname(AgentServer.getServerId()))) {
     xlogmon.log(BasicLevel.WARN,
		 "AdminProxy#" + AgentServer.getServerId() +
		 ": AgentServer#" + sid + " is not local. ");
     throw new Exception("AgentServer#" + sid + " is not local.");
    }

    Vector argv = new Vector();
    argv.addElement(javapath);
    argv.addElement("-classpath");
    argv.addElement(classpath);
    if (AgentServer.isTransient(sid)) {
      argv.addElement("-Xmx64m");
    }
    argv.addElement("fr.dyade.aaa.agent.AgentServer");
    argv.addElement(Short.toString(sid));
    argv.addElement(new File(userdir, "s" + sid).getPath());
	  
    String[] command = new String[argv.size()];
    argv.copyInto(command);

    p = Runtime.getRuntime().exec(command);
    proxy.ASP.put(new Short(sid), p);

    xlogmon.log(BasicLevel.DEBUG,
               "AdminProxy#" + AgentServer.getServerId() +
               ": AgentServer#" + sid + " started");

    BufferedReader br =
      new BufferedReader(new InputStreamReader(p.getInputStream()));
    return br.readLine();
  }

  static void close(Socket socket) {
    try {
      socket.getInputStream().close();
    } catch (Exception exc) {}
    try {
      socket.getOutputStream().close();
    } catch (Exception exc) {}
    try {
      socket.close();
    } catch (Exception exc) {}
  }

  /**
   * Stops cleanly an agent server from its id. Be careful an AgentServer
   * must be initialized (for configuration).
   *
   * @param sid		id of agent server to stop
   */
  public static void stopAgentServer(short sid) throws Exception {
    Socket socket = null;
    xlogmon.log(BasicLevel.DEBUG,
               "AdminProxy#" + AgentServer.getServerId() +
               ": stop AgentServer#" + sid);
    try {
      String host = AgentServer.getHostname(sid);
      int port = Integer.parseInt(
        AgentServer.getServiceArgs(sid, "fr.dyade.aaa.agent.AdminProxy"));
      socket = new Socket(host, port);
      socket.getOutputStream().write((STOP_SERVER + "\n").getBytes());
      try {
        socket.getInputStream().read();
      } catch (SocketException exc) {
        // Nothing to do: connection reset by peer:
      }
    } catch (Throwable exc) {
      xlogmon.log(BasicLevel.ERROR,
                  "AdminProxy#" + AgentServer.getServerId() +
                  "error during stop server#" + sid, exc);
      throw new Exception("Can't stop server#" + sid +
                          ": " + exc.getMessage());
    } finally {
      close(socket);
      socket = null;
    }
    xlogmon.log(BasicLevel.DEBUG,
               "AdminProxy#" + AgentServer.getServerId() +
               ": AgentServer#" + sid + " stopped");
  }

  /**
   * Stops violently an agent server from its id. Be careful an AgentServer
   * must be initialized (for configuration).
   *
   * @param sid		id of agent server to stop
   */
  public static void killAgentServer(short sid) throws Exception {
    Process p = (Process) proxy.ASP.get(new Short(sid));

    xlogmon.log(BasicLevel.DEBUG,
                "AdminProxy#" + AgentServer.getServerId() +
                " kill AgentServer#" + sid + " [" + p + ']');

    if (p != null) p.destroy();
  }

  public static void joinAgentServer(short sid) throws Exception {
    Process p = (Process) proxy.ASP.get(new Short(sid));

    xlogmon.log(BasicLevel.DEBUG,
                "AdminProxy#" + AgentServer.getServerId() +
                " join AgentServer#" + sid + " [" + p + ']');

    // TODO: put it in previous method and set a Timer.
    if (p != null) p.waitFor();
  }

  /**
   * Stops violently an agent server from its id. Be careful an AgentServer
   * must be initialized (for configuration).
   *
   * @param sid		id of agent server to stop
   */
  public static void crashAgentServer(short sid) throws Exception {
    Socket socket = null;
    xlogmon.log(BasicLevel.DEBUG,
               "AdminProxy#" + AgentServer.getServerId() +
               ": crash AgentServer#" + sid);
    try {
      String host = AgentServer.getHostname(sid);
      int port = Integer.parseInt(
        AgentServer.getServiceArgs(sid, "fr.dyade.aaa.agent.AdminProxy"));
      socket = new Socket(host, port);
      socket.getOutputStream().write((CRASH_SERVER + "\n").getBytes());
      try {
        socket.getInputStream().read();
      } catch (SocketException exc) {
        // Nothing to do: connection reset by peer:
      }
    } catch (Throwable exc) {
      xlogmon.log(BasicLevel.ERROR,
                  "AdminProxy#" + AgentServer.getServerId() +
                  "error during crash server#" + sid, exc);
      throw new Exception("Can't crash server#" + sid +
                          ": " + exc.getMessage());
    } finally {
      close(socket);
      socket = null;
    }
    xlogmon.log(BasicLevel.DEBUG,
               "AdminProxy#" + AgentServer.getServerId() +
               ": AgentServer#" + sid + " crashed");
  }

  public static void main(String args[]) {
    try {
      if (args[0].equalsIgnoreCase("stop")) {
        String host = args[1];
        int port = Integer.parseInt(args[2]);
        Socket socket = new Socket(host, port);
        socket.getOutputStream().write((STOP_SERVER + "\n").getBytes());
        try {
          socket.getInputStream().read();
        } catch (SocketException exc) {
          // Nothing to do: connection reset by peer:
        }
      } else if (args[0].equalsIgnoreCase("crash")) {
        String host = args[1];
        int port = Integer.parseInt(args[2]);
        Socket socket = new Socket(host, port);
        socket.getOutputStream().write((CRASH_SERVER + "\n").getBytes());
        try {
          socket.getInputStream().read();
        } catch (SocketException exc) {
          // Nothing to do: connection reset by peer:
        }
      }
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }
}
