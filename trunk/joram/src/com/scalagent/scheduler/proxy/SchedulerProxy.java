/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package com.scalagent.scheduler.proxy;

/**
 * A <code>SchedulerProxy</code> agent provides a command line interface to 
 * <code>Scheduler</code> agent through a TCP connection and a
 * <code>SchedulerStream</code>.
 * <p>
 * The <code>SchedulerProxy</code> agent has a name so that it may be found by
 * the client from that name. An example client code is:
 * <p><blockquote><pre>
 * SchedulerProxy proxy = new SchedulerProxy("scheduler name", schedulerId,
 *					     SchedulerProxy.STREAM_ASCII);
 * proxy.deploy();
 * Socket socket = SchedulerProxy.connect("scheduler name");
 * Writer scheduler = new OutputStreamWriter(socket.getOutputStream());
 * scheduler.write("schedule \"event name\" start=\"21 sep 98 14:29:18\"");
 * scheduler.flush();
 * </pre></blockquote><p>
 * Dates are interpreted using the default locale.
 *
 * @see		Scheduler
 * @see		SchedulerStream
 * @see		Locale
 */
public class SchedulerProxy {
//   /**
//    * Gets a socket to a proxy agent from its name.
//    *
//    * @param name	file name provided at agent creation
//    * @return		a socket connected to the proxy agent
//    */
//   public static Socket connect(String name) throws IOException {
//     File file = new File(name);
//     BufferedReader fr = new BufferedReader(new FileReader(file));
//     String line = fr.readLine();
//     fr.close();

//     if (line == null)
//       throw new EOFException();

//     int colon = line.indexOf(':');
//     InetAddress addr = InetAddress.getByName(line.substring(0, colon));
//     int port = Integer.parseInt(line.substring(colon+1));
//     return new Socket(addr, port);
//   }

  public static final int STREAM_OBJECT = 1;
  public static final int STREAM_ASCII = 2;


//   public String schedulerName;	/** symbolic name of the scheduler */
//   public AgentId scheduler;	/** id of associated Scheduler agent */
//   public int streamType;	/** STREAM_OBJECT, STREAM_ASCII */

//   /**
//    * Creates a local agent with unknown port.
//    *
//    * @param name	symbolic name of this agent
//    * @param scheduler	id of associated Scheduler agent
//    */
//   public SchedulerProxy(String schedulerName, AgentId scheduler, int streamType) {
//     super();
//     localPort = 0;
//     this.schedulerName = schedulerName;
//     this.scheduler = scheduler;
//     this.streamType = streamType;
//   }

//   /**
//    * Creates an agent to be configured.
//    *
//    * @param to		target agent server
//    * @param name	symbolic name of this agent
//    */
//   public SchedulerProxy(short to, String name) {
//     super(to, name);
//     localPort = 0;
//     schedulerName = null;
//     scheduler = null;
//     streamType = 2;
//   }


//   /**
//    * Provides a string image for this object.
//    *
//    * @return	a string image for this object
//    */
//   public String toString() {
//     return "(" + super.toString() +
//       ",schedulerName=" + schedulerName +
//       ",scheduler=" + scheduler +
//       ",streamType=" + streamType + ")";
//   }

//   /**
//    * Initializes the transient members of this agent.
//    * This function is first called by the factory agent,
//    * then by the system each time the agent server is restarted.
//    *
//    * @param firstTime		true when first called by the factory
//    */
//   protected void agentInitialize(boolean firstTime) throws Exception {
//     // initializes the command line socket
//     super.agentInitialize(firstTime);

//     // registers this agent so that the client may connect
//     File file = new File(schedulerName);
//     if (file.exists()) {
//       // remove previous registration
//       if (! file.delete())
// 	throw new IllegalArgumentException("cannot delete " + schedulerName);
//     }
//     FileWriter fw = new FileWriter(file);
//     fw.write(InetAddress.getLocalHost().getHostAddress());
//     fw.write(':');
//     fw.write(String.valueOf(listenPort));
//     fw.close();
//   }

//   /**
//    * Reacts to <code>SchedulerProxy</code> specific notifications.
//    * Forwards notifications from the input stream to the scheduler.
//    * Analyzes the notification type, then calls the appropriate
//    * <code>doReact</code> function. By default calls <code>react</code>
//    * from base class.
//    * Handled notification types are :
//    *	<code>AgentDeleteRequest</code>.
//    *
//    * @param from	agent sending notification
//    * @param not		notification to react to
//    */
//   public void react(AgentId from, Notification not) throws Exception {
//     if (from.equals(getId()) &&
// 	not instanceof ScheduleEvent) {
//       // forwards notifications from the input stream to the scheduler
//       sendTo(scheduler, not);
//     } else if (not instanceof DeleteNot) {
//       doReact(from, (DeleteNot) not);
//     } else {
//       super.react(from, not);
//     }
//   }

//   /**
//    * Reacts to <code>AgentDeleteRequest</code> notifications.
//    * Calls <code>delete</code>.
//    *
//    * @param from	agent sending notification
//    * @param not		notification to react to
//    */
//   protected void doReact(AgentId from, DeleteNot not) throws Exception {
//     delete(not.reply);
//   }

//   /**
//    * Creates a (chain of) filter(s) for transforming the specified
//    * <code>InputStream</code> into a <code>NotificationInputStream</code>.
//    *
//    * @param in	the underlying input stream
//    * @return	the <code>NotificationInputStream</code> object
//    */
//   protected NotificationInputStream setInputFilters(InputStream in) throws StreamCorruptedException, IOException {
//     switch (streamType) {
//     case STREAM_OBJECT:
//       return new SerialInputStream(in);
//     case STREAM_ASCII:
//       return new SchedulerInputStream(in);
//     }
//     return null;
//   }

//   /**
//    * Creates a (chain of) filter(s) for transforming the specified
//    * <code>OutputStream</code> into a <code>NotificationOutputStream</code>.
//    *
//    * @param out	the underlying output stream
//    * @return	the <code>NotificationOutputStream</code> object
//    */
//   protected NotificationOutputStream setOutputFilters(OutputStream out) throws IOException {
//     switch (streamType) {
//     case STREAM_OBJECT:
//       return new SerialOutputStream(out);
//     case STREAM_ASCII:
//       return new StringOutputStream(out);
//     }
//     return null;
//   }
}
