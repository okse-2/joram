/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
import java.util.*;
import java.lang.reflect.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Daemon;
import fr.dyade.aaa.agent.conf.A3CML;
import fr.dyade.aaa.agent.conf.A3CMLConfig;

/**
 * A <code>AdminProxy</code> service provides a TCP service allowing remote
 * administration of agent servers.
 * <p>
 * The <code>AdminProxy</code> service can be configured by the way of
 * service argument:
 * <ul>
 * <li>the TCP port number, by default this port is 8091.
 * <li>the number of monitor needed to handled requests.
 * </ul>
 */
public class AdminProxy {

  static AdminProxy proxy = null;

  public static boolean debug = true;

  /** Property that define the TCP listen port */
  public final static String LISTENPORT = "fr.dyade.aaa.agent.AdminProxy.port";
  /** The TCP listen port, by default 8091 */
  private static int port = 8091;
  /** The number of monitors.*/
  private static int nbm;
  /** Property that define the number of monitor */
  public final static String NBMONITOR = "fr.dyade.aaa.agent.AdminProxy.nbm";
  /** Hashtable that contain all <code>Process</code> of running AgentServer */
  //   Hashtable ASP = null;

  AdminMonitor monitors[] = null;
  ServerSocket listen = null;

  static Logger xlogmon = null;

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
    xlogmon = Debug.getLogger(Debug.A3Service + ".AdminProxy" +
                              ".#" + AgentServer.getServerId());

    if (proxy != null) {
      xlogmon.log(BasicLevel.ERROR,
                  "AdminProxy#" + AgentServer.getServerId() +
      ": already initialized.");
      throw new Exception("AdminProxy" + ".#" + AgentServer.getServerId() +
      ": already initialized.");
    }

    try {
      proxy = new AdminProxy();
    } catch (IOException exc) {
      xlogmon.log(BasicLevel.ERROR,
                  "AdminProxy#" + AgentServer.getServerId() +
                  ", can't get listen port", exc);
      throw exc;
    }
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
          Thread.sleep(i * 500);
        } catch (InterruptedException e) {}
      }
    }

    //     ASP = new Hashtable();

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

  public static void stopService() {
    for (int i=0; i<proxy.monitors.length; i++) {
      if (proxy.monitors[i] != null) proxy.monitors[i].stop();
      proxy.monitors[i] = null;
    }
    proxy = null;
  }

  static final String HELP = "help";
  static final String NONE = "";

  // Server's administration commands
  public static final String STOP_SERVER = "halt";
  public static final String CRASH_SERVER = "crash";
  public static final String PING = "ping";
  public static final String CONFIG = "config";

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

  // Debug's tool
  static final String DUMP = "dump";

  // update traces configuration
  public static final String LOG = "log";

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
      // Get the logging monitor from AdminProxy (overload Daemon setup)
      super(name, AdminProxy.xlogmon);
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
            logmon.log(BasicLevel.DEBUG, getName() + ", waiting: " + listen);
            socket = listen.accept();
            logmon.log(BasicLevel.DEBUG, getName() + ", receiving.");
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
        logmon.log(BasicLevel.DEBUG, getName() + ", finishing.");
        finish();
      }
    }

    protected void close() {
      try {
        logmon.log(BasicLevel.DEBUG, getName() + ", closing: " + listen);
        listen.close();
      } catch (Exception exc) {}
      listen = null;
    }

    protected void shutdown() {
      logmon.log(BasicLevel.DEBUG, getName() + ", close(): ");
      close();
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
          AgentServer.stop(false);
          logmon.log(BasicLevel.WARN, getName() + ", bye.");
        } else if (cmd.equals(CRASH_SERVER)) {
          // Kill the AgentServer
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
          for (Enumeration c=AgentServer.getConsumers();
          c.hasMoreElements(); ) {
            MessageConsumer cons = (MessageConsumer) c.nextElement();
            writer.println("+----------------------------------------");
            writer.println(cons);
          }
        } else if (cmd.equals(START_MCONS)) {
          String domain = null;
          if (st.hasMoreTokens()) {
            // start the identified consumer.
            domain = st.nextToken();
          }
          for (Enumeration c=AgentServer.getConsumers();
          c.hasMoreElements(); ) {
            MessageConsumer cons = (MessageConsumer) c.nextElement();

            if (((domain == null) || domain.equals(cons.getName()))) {
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
          for (Enumeration c=AgentServer.getConsumers();
          c.hasMoreElements(); ) {
            MessageConsumer cons = (MessageConsumer) c.nextElement();

            if (((domain == null) || domain.equals(cons.getName()))) {
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
          try {
            // Add a new Service
            String sclass = null;
            String args = null;
            try {
              sclass = st.nextToken();
              if (st.hasMoreTokens())
                args = st.nextToken();
            } catch (NoSuchElementException exc) {
              throw new Exception("Usage: add <sclass> [<args>]");
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
          } catch (Exception exc) {
            writer.println(exc.getMessage());
          } 
        } else if (cmd.equals(REMOVE_SERVICE)) {
          // Remove an existing Service
          String sclass = null;
          try {
            sclass = st.nextToken();
          } catch (NoSuchElementException exc) {
            writer.println("Usage: " + REMOVE_SERVICE + " <sclass> [<args>]");
            return;
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
        } else if (cmd.equals(DUMP)) {
          AgentId id = null;
          try {
            id = AgentId.fromString(st.nextToken());
          } catch (IllegalArgumentException exc) {
            writer.println("Usage: " + DUMP + " #x.y.z");
            return;
          }
          try {
            writer.println(AgentServer.getEngine().dumpAgent(id));
          } catch (Exception exc) {
            writer.println("Can't launch server: " + exc.getMessage());
            if (debug) exc.printStackTrace(writer);
          }
        } else if (cmd.equals(NONE)) {
        } else if (cmd.equals(PING)) {
          writer.println(AgentServer.getServerId());
        } else if (cmd.equals(CONFIG)) {
          try {
            A3CMLConfig a3CMLConfig = AgentServer.getConfig();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter out = new PrintWriter(baos);
            A3CML.toXML(a3CMLConfig, out);
            out.flush();
            baos.flush();
            baos.close();
            byte[] bytes = baos.toByteArray();
            writer.println(new String(bytes));
          } catch (Exception exc) {
            writer.println("Can't load configuration: " + exc.getMessage());
            if (debug) exc.printStackTrace(writer);
          }
        } else if (cmd.equals(LOG)){
          PrintStream oldErr = System.err;
          try {
            System.setErr(new PrintStream(socket.getOutputStream()));
            String topic = st.nextToken();
            String level = st.nextToken();
            int intLevel;
            if (level.equals("DEBUG")) {
              intLevel = org.objectweb.util.monolog.api.BasicLevel.DEBUG;
            } else if (level.equals("ERROR")) {
              intLevel = org.objectweb.util.monolog.api.BasicLevel.ERROR;
            } else if (level.equals("FATAL")) {
              intLevel = org.objectweb.util.monolog.api.BasicLevel.FATAL;
            } else if (level.equals("INFO")) {
              intLevel = org.objectweb.util.monolog.api.BasicLevel.INFO;
            } else if (level.equals("INHERIT")) {
              intLevel = org.objectweb.util.monolog.api.BasicLevel.INHERIT;
            } else if (level.equals("WARN")) {
              intLevel = org.objectweb.util.monolog.api.BasicLevel.WARN;
            } else {
              writer.println("Unknown level: " + level);
              return;
            }
            Debug.setLoggerLevel(topic, intLevel);
          } catch(Exception exc){
            writer.println(exc.getMessage());
          } finally{
            System.setErr(oldErr);
            writer.println("OK");
          }
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
                         "\t" + CONFIG + 
          "\n\t\tReturns the configuration of the server in XML format.\n");
        } else {
          writer.println("unknown command:" + cmd);
        }
      } finally {
      }
    }
  }
}
