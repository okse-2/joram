/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - ScalAgent Distributed Technologies
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
package org.ow2.joram.shell.mom.commands;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.objectweb.joram.mom.dest.AdminTopicMBean;
import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.dest.Topic;
import org.objectweb.joram.mom.dest.TopicMBean;
import org.objectweb.joram.mom.dest.QueueMBean;
import org.objectweb.joram.mom.dest.DestinationMBean;
import org.objectweb.joram.mom.messages.MessageView;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.UserAgentMBean;
import org.objectweb.joram.mom.util.JoramHelper;
import org.objectweb.joram.mom.util.SynchronousAgent;
import org.objectweb.joram.shared.DestinationConstants;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.ow2.joram.shell.ShellDisplay;

import fr.dyade.aaa.agent.AgentMBean;
import fr.dyade.aaa.agent.AgentServer;

public class MOMCommandsImpl implements MOMCommands {
  
  static private MOMCommandsImpl INSTANCE = null;
  
  static public void init(BundleContext context) {
    System.out.println("Init...");
    bundleContext = context;
    destinationTracker = new ServiceTracker
                (bundleContext, DestinationMBean.class.getCanonicalName(), null);
    queueTracker = new ServiceTracker
                (bundleContext, QueueMBean.class.getCanonicalName(),       null);
    topicTracker = new ServiceTracker
                (bundleContext, TopicMBean.class.getCanonicalName(),       null);
    userTracker = new ServiceTracker
                (bundleContext, UserAgentMBean.class.getCanonicalName(),   null);
    adminTracker = new ServiceTracker
                (bundleContext, AdminTopicMBean.class.getCanonicalName(),  null);
    clientSubTracker = new ServiceTracker
                (bundleContext, ClientSubscriptionMBean.class.getCanonicalName(),  null);

    destinationTracker.open();
    queueTracker.open();
    topicTracker.open();
    userTracker.open();
    adminTracker.open();
    clientSubTracker.open();
  }
  
  static public MOMCommandsImpl getInstance() throws Exception{
    if(INSTANCE == null && bundleContext != null)
      INSTANCE = new MOMCommandsImpl();
    if(INSTANCE == null)
      throw new Exception("MOM commands have not been initialized.");
    return INSTANCE;
  }
  
  public static final String NAMESPACE = "joram:mom";
  private static final int TIMEOUT = 1000;
 
  static private BundleContext bundleContext;
  static private ServiceTracker destinationTracker;
  static private ServiceTracker queueTracker;
  static private ServiceTracker topicTracker;
  static private ServiceTracker userTracker;
  static private ServiceTracker adminTracker;
  static private ServiceTracker clientSubTracker;
  
  /**
   * Commands list
   */
  public static String[] COMMANDS =
      new String[] {"list",       "create",
                    "delete",
                    "queueLoad",  "subscriptionLoad",
                    "info",       "lsMsg",
                    "ping",       "deleteMsg",
                    "sendMsg",    "receiveMsg",
                    "help",       "clear"};
  
  //TODO: retrieve help from file?
  /**
   * Prints all MOM commands
   */
  public static void help() {
    System.out.println("Usage: help <cmd>");
    Arrays.sort(COMMANDS);
    System.out.println("Commands: "+COMMANDS[0]);
    for(int i =1; i<COMMANDS.length; i++)
      System.out.println("          "+COMMANDS[i]);
 }  

  /**
   * Prints the description of the given command<br/>
   * Usage: [joram:mom:]help <command><br/>
   * @param command Name of the command to describe
   */
  public static void help(String command) {
    //Checks whether the command name contains the namespace
    if(command.length()>NAMESPACE.length()
        && command.substring(0, NAMESPACE.length()).equalsIgnoreCase(NAMESPACE)) {
      //If so, remove the namespace part
      command = command.substring(NAMESPACE.length()+1, command.length());
    }
    String fullCommand = "["+NAMESPACE+":]"+command;
    StringBuffer buf = new StringBuffer();
    buf.append("Usage: ").append(fullCommand).append(" ");
    if(command.equalsIgnoreCase("list")) {
      buf.append("<category> [username]");
      buf.append("\n\tPossible categories: destination, topic, queue, user, subscription");
      buf.append("\n\tNB: For the subscription category, you must provide the user name.");     
    } else if(command.equalsIgnoreCase("help")) {
      buf.append("<command>");     
      buf.append("\nShows this help.");
    } else if(command.equalsIgnoreCase("create")) {
      buf.append("<topic|queue> <name> [option...]");
      buf.append("\n       ").append(fullCommand).append(" ");
      buf.append("user [<name>]\n");
      buf.append("Options: -sid <server id>\tSpecifies on which server the destination is to be created\n");
      buf.append("                         \tDefault: This server\n");
      buf.append("         -ext <extension>\tSpecifies which extension class to instanciate\n");
      buf.append("                         \tDefault: None");
    } else if(command.equalsIgnoreCase("queueLoad")) {
      buf.append("<queueName>");
    } else if(command.equalsIgnoreCase("subscriptionLoad")) {
      buf.append("<userName> <subscriptionName>");
    } else if(command.equalsIgnoreCase("delete")) {
      buf.append("(topic|queue|user) <name>");
    } else if(command.equalsIgnoreCase("info")) {
      buf.append("(queue|topic) <name>");
      buf.append("\n       ").append(fullCommand).append(" ");
      buf.append("subscription <user name> <subscription name>");
    } else if(command.equalsIgnoreCase("lsMsg")) {
      buf.append("queue <queue name> [[first msg idx]:[last msg idx]]");
      buf.append("\n       ").append(fullCommand).append(" ");
      buf.append("subscription <username> <subscription name> [[first msg idx]:[last msg idx]]");
    } else if(command.equalsIgnoreCase("deleteMsg")) {
      buf.append("queue <queue name> <msg id>");
      buf.append("\n       ").append(fullCommand).append(" ");
      buf.append("subscription <username> <subscription name> <msg id>");
    } else if(command.equalsIgnoreCase("receiveMsg")) {
      buf.append("<queue name> [options]");
      buf.append("Options: -n <x>\tReceive only <x> messages");
      buf.append("         -t <x>\tTimes out after <x> seconds");
    } else if(command.equalsIgnoreCase("sendMsg")) {
      buf.append("(queue|topic) <destination name> <text>");
    } else if(command.equalsIgnoreCase("ping")) {
      buf.append("\nChecks whether a JoramAdminTopic exists.");
    } else if(command.equalsIgnoreCase("clear")) {
      buf.append("queue <name>");
      buf.append("\n       ").append(fullCommand).append(" ");
      buf.append("subscription <username> <subscription name>");
      buf.append("\nDeletes all pending messages.");
    } else {
      System.err.println("Unknown command: "+command);
      return;
    }
    System.out.println(buf.toString());
  }

  /**
   * Lists all queues, topics, both or users with useful informations<br/>
   * Usage: [joram:mom:]list <category> [username]<br/>
   *   Possible categories: destination, topic, queue, user, subscription<br/>
   *   NB: For the subscription category, you must provide the user name.<br/>
   */
  public void list(String[] args) {
    if(args.length==0) {
      help("list");
      return;
    }
    String category = args[0].toLowerCase();
    if(category.equals("destination")) {
      listDestination();
    } else if(category.equals("topic")) {
      listTopic();
    } else if(category.equals("queue")) {
      listQueue();
    } else if(category.equals("user")) {
      listUser();
    } else if (category.equals("subscription") && args.length==2) {
      listSubscription(args[1]);
    } else {
      help("list");
    }
  }
  
  private void listDestination() {
    Object[] obj = destinationTracker.getServices();
    
    if(obj == null || obj.length ==0) {
      System.out.println("There is no destination.");
      return;
    }
    
    HashMap<String, DestinationMBean> dests = new HashMap<String, DestinationMBean>();
    for(Object o : obj) {
      DestinationMBean d = (DestinationMBean) o;
      dests.put(d.getDestinationId(), d);
    }

    String[][] table = new String[dests.size()+1][];
    table[0] = new String[] { "Id",
                              "Name",
                              "Type",
                              "Creation Date",
                              "Perm."};
    int i = 1;
    for(DestinationMBean d : dests.values()) {
      String type = "NA";
      if(d instanceof TopicMBean)         type="Topic";
        else if(d instanceof QueueMBean)  type="Queue";
        else                              type="(Unknown)";
      table[i++] = new String[] { d.getDestinationId(),
                                d.getName(),
                                type,
                                d.getCreationDate(),
                                (d.isFreeReading()?"r":"-")+'/'
                                +(d.isFreeWriting()?"w":"-")};
    }
    int n = dests.size();
    if(n < 2)
      System.out.println("There is " + dests.size() + " destination.");
    else
      System.out.println("There are " + dests.size() + " destinations.");
    ShellDisplay.displayTable(table, true);
  }
  
  private void listQueue() {
    Object[] obj = queueTracker.getServices();
    
    if(obj == null || obj.length ==0) {
      System.out.println("There is no queue.");
      return;
    }
    
    HashMap<String, QueueMBean> dests = new HashMap<String, QueueMBean>();
    for(Object o : obj) {
      QueueMBean d = (QueueMBean) o;
      dests.put(d.getDestinationId(), d);
    }

    String[][] table = new String[dests.size()+1][];
    table[0] = new String[] { "Id",
                              "Name",
                              "Pending msg",
                              "Rcvd Msg",
                              "Dlvd Msg",
                              "Perm."};
    int i = 1;
    for(QueueMBean d : dests.values()) {
      table[i++] = new String[] { d.getDestinationId(),
                                d.getName(),
                                Integer.toString(d.getPendingMessageCount()),
                                Long.toString(d.getNbMsgsReceiveSinceCreation()),
                                Long.toString(d.getNbMsgsDeliverSinceCreation()),
                                (d.isFreeReading()?"r":"-")+'/'
                                +(d.isFreeWriting()?"w":"-")};
    }
    int n = dests.size();
    if(n < 2)
      System.out.println("There is " + dests.size() + " queue.");
    else
      System.out.println("There are " + dests.size() + " queues.");
    ShellDisplay.displayTable(table, true);    
  }
  
  private void listTopic() {
    Object[] obj = topicTracker.getServices();
    
    if(obj == null || obj.length ==0) {
      System.out.println("There is no topic.");
      return;
    }
    
    HashMap<String, TopicMBean> dests = new HashMap<String, TopicMBean>();
    for(Object o : obj) {
      TopicMBean d = (TopicMBean) o;
      dests.put(d.getDestinationId(), d);
    }

    String[][] table = new String[dests.size()+1][];
    table[0] = new String[] { "Id",
                              "Name",
                              "Subscriber",
                              "Rcvd Msg",
                              "Dlvd Msg",
                              "Perm."};
    int i = 1;
    for(TopicMBean d : dests.values()) {
      table[i++] = new String[] { d.getDestinationId(),
                                d.getName(),
                                Integer.toString(d.getNumberOfSubscribers()),
                                Long.toString(d.getNbMsgsReceiveSinceCreation()),
                                Long.toString(d.getNbMsgsDeliverSinceCreation()),
                                (d.isFreeReading()?"r":"-")+'/'
                                +(d.isFreeWriting()?"w":"-")};
    }
    int n = dests.size();
    if(n < 2)
      System.out.println("There is " + dests.size() + " topic.");
    else
      System.out.println("There are " + dests.size() + " topics.");
    ShellDisplay.displayTable(table, true);    
  }
  
  private void listUser() {
    Object[] objs = userTracker.getServices();
    if(objs == null || objs.length ==0) {
      System.err.println("Error: There is no user.");
      return;
    }
    HashMap<String,UserAgentMBean> users = new HashMap<String, UserAgentMBean>();
    for(int i = 0; i < objs.length; i++) {
      UserAgentMBean u = (UserAgentMBean) objs[i];
      users.put(u.getAgentId(), u);
    }
    
    String[][] table = new String[users.size()+1][];
    table[0] = new String[]{"User Id","Name"};
    int i = 1;
    for(UserAgentMBean u : users.values()) {
      table[i++] = new String[] { u.getAgentId(),
                                  u.getName()};
    }
    ShellDisplay.displayTable(table, true);
  }

  private void listSubscription(String userName) {
    //Step 1: retrieve the user
    UserAgentMBean user = null;
    Object[] objs = userTracker.getServices();
    if(objs==null) {
      System.err.println("Error: No user found.");
      return;
    }
    for(Object o : objs) {
      UserAgentMBean u = (UserAgentMBean) o;
      if(u.getName().equals(userName)) {
        user = u;
        break;
      }
    }
    if(user == null) {
      System.err.println("Error: The user "+userName+" does not exist.");
      return;
    }
    //Step 2: retrieve the user's subscriptions
    String[] names = user.getSubscriptionNames();
    HashSet<ClientSubscriptionMBean> subs =
        new HashSet<ClientSubscriptionMBean>();
    Object[] clients = clientSubTracker.getServices();
    if(clients==null) {
      System.err.println("Error: No subscription found.");
      return;
    }
    for(Object o : clients) {
      ClientSubscriptionMBean s = (ClientSubscriptionMBean) o;
      for(String n : names)
        if(n.equals(s.getName())) {
          subs.add(s);
          break;
        }
    }
    if(subs.size()==0) {
      System.err.println("Error: The user "+userName+" has no subscription.");
      return;
    }
    
    //Step 3: Display
    String[][] table = new String[subs.size()+1][8];
    table[0] = new String[]{"Name",
                            "Topic Id",
                            "Pndng msgs",
                            "Wtng for ack.",
                            "Dlvd msg",
                            "Nb msg max",
                            "Sent to DMQ"};
    int i=1;
    for(ClientSubscriptionMBean sub : subs) {
      table[i] = new String[] {
        sub.getName().subSequence(0, 9).toString(), //Name
        sub.getTopicIdAsString(), //Topic Id
        String.valueOf(sub.getPendingMessageCount()), //Pending messages
        String.valueOf(sub.getDeliveredMessageCount()), //Waiting for ack.
        String.valueOf(sub.getNbMsgsDeliveredSinceCreation()), //Nb msg delivered
        String.valueOf(sub.getNbMaxMsg()), //Nb msg max
        String.valueOf(sub.getNbMsgsSentToDMQSinceCreation()) //Nb msg send to DMQ
      };
    }
    ShellDisplay.displayTable(table, true);
  }
  
  /**
   * Creates a destination or a user<br/>
   * Usage: [joram:mom:]create <topic|queue> <name> [option...]<br/>
   *        [joram:mom:]create user [<name>]<br/>
   * Options: -sid <server id> Specifies on which server the destination is to be created<br/>
   *                           Default: This server<br/>
   *          -ext <extension> Specifies which extension class to instanciate<br/>
   *                           Default: None<br/>
   */
  public void create(String[] args) {
    if(args == null || args.length<2)  {
      help("create");
      return;
    }
    
    if(args[0].equals("topic") || args[0].equals("queue")) {
      createDestination(args);
    }  else if(args[0].equals("user")) {
      String[] newArgs = new String[args.length-1];
      for (int i = 1; i < args.length; i++) {
        newArgs[i-1] = args[i];
      }
      createUser(newArgs);
      return;
    } else {
      help("create");
      return;
    }
  }
  
  private void createDestination(String[] args) {
    byte type = args[0].equalsIgnoreCase("queue")?
        DestinationConstants.QUEUE_TYPE:
        DestinationConstants.TOPIC_TYPE;
    String name= null;
    short sid = AgentServer.getServerId();
    String ext = null;
    //TODO: Handle properties
//    Properties props = new Properties();
    name=args[1];
    
    for(int i = 2; i < args.length; i++) {
      if(args[i].equals("-sid") && args.length>i+1) {
        sid=Short.parseShort(args[++i]);
      } else if(args[i].equals("-ext") && args.length>i+1) {
        ext = args[++i];
      } else {
        help("create");
        return;
     }
    }
    
    try {
      SynchronousAgent syncAgent = SynchronousAgent.getSynchronousAgent();
      if(ext==null)
        ext = type==DestinationConstants.QUEUE_TYPE?
            Queue.class.getName():Topic.class.getName();
      if(syncAgent.createDestination(sid, name, ext, null, type))
        if(type==DestinationConstants.QUEUE_TYPE)
          System.out.println("Queue "+name+" created on server "+sid+
              (ext==null?".":" with the class "+ext+"."));
        else
          System.out.println("Topic "+name+" created on server "+sid+
              (ext==null?".":" with the class "+ext+"."));
      else
        System.err.println("Error: The creation request failed.");
    } catch (IOException e) {
      System.err.println("Error: Couldn't retrieve the synchronous agent.");
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted while waiting for the reply.");
    }
  }
  
  /**
   * Add a new user to the servers
   * @param args parameters of the command
   */
  private void createUser(String[] args) {
    String userName = null;
    Scanner s = new Scanner(System.in);
    if(args.length==0) {
      System.out.print("User name: "); System.out.flush();
      userName = s.nextLine();
    } else if(args.length==1) {
      userName = args[0];
    } else {
      help("addUser");
      return;
    }
    if(!userName.matches("[A-Za-z][A-Za-z0-9]{2,}?")) {
      System.out.println("The user name must begin with a letter and contain at least 3 alhpa-numeric caracters.");
      return;
    }
    
    System.out.print("Password: "); System.out.flush();
    String pwd = s.nextLine();
    if(userName.length()<5) {
      System.out.println("The password must be at least 6 caracters long.");
      return;
    }
    
    try {
      SynchronousAgent syncAgent = SynchronousAgent.getSynchronousAgent();
      boolean res =syncAgent.createUser(AgentServer.getServerId(),
          userName, pwd, null, null);
      if(res)
        System.out.println("User "+userName+" succesfully created.");
      else
        System.out.println("User creation failed.");
    } catch (ClassNotFoundException e) {
      System.err.println("Error: Identity class not found.");
    } catch (IOException e) {
      System.err.println("Error: Couldn't retrieve the synchronous agent.");
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted while waiting for the reply.");
    } catch (Exception e) {
      System.err.println("Error: "+e.getClass().getName()+" received.");
      e.printStackTrace(System.err);
    }
  }
  
  /**
   * Delete a destination or a user<br/>
   * Usage: [joram:mom:]delete (topic|queue|user) <name><br/>
   */
  public void delete(String[] args) {
    if(args.length != 2) {
      help("delete");
      return;
    }
    String category = args[0];
    ServiceTracker tracker;
    if(category.equalsIgnoreCase("queue")) {
      tracker = queueTracker;
    } else if(category.equalsIgnoreCase("topic")) {
      tracker = topicTracker;
    } else if(category.equalsIgnoreCase("user")) {
      tracker = userTracker;
    } else {
      System.err.println("Error: Unknwon category.");
      return;
    }
    Object[] objs = tracker.getServices();
    if(objs==null) {
      System.err.println("Error: "+category.toLowerCase()+" not found.");
      return;
    }
    AgentMBean a;
    for(int i = 0; i < objs.length; i++) {
      a = (AgentMBean) objs[i];
      if(a.getName().equals(args[1])) {
        if(category.equalsIgnoreCase("user"))
          try {
            if(SynchronousAgent.getSynchronousAgent().deleteUser(a.getName(), a.getAgentId()))
              System.out.println("User successfully deleted.");
            else
              System.err.println("User suppression failed.");            
          } catch (InterruptedException e) {
            System.err.println("Error: Interrupted");
          } catch (Exception e) {
            System.err.println("Error: Exception raised");
            e.printStackTrace();
          }
        else
          try {
            if(SynchronousAgent.getSynchronousAgent().deleteDest(a.getAgentId()))
              System.out.println("Destination successfully deleted.");
            else
              System.err.println("Destination suppression failed.");            
          } catch (InterruptedException e) {
            System.err.println("Error: Interrupted");
          } catch (Exception e) {
            System.err.println("Error: Exception raised");
            e.printStackTrace();
          }
        return;
      }
    }
    System.err.println("Error: "+category.toLowerCase()+" not found.");
  }

  /**
   * Shows the pending message count<br/>
   * Usage: [joram:mom:]queueLoad <queueName><br/>
   */
  public void queueLoad(String[] args) {
    if(args.length!=1) {
      help("queueLoad");
      return;
    }
    
    String name = args[0];
    QueueMBean queue = findQueue(name);

    if(queue!=null) {
      int c = queue.getPendingMessageCount();
      System.out.println("Pending count of \""+name+"\" : "+c);
    } else {
      System.err.println("Error: There is no queue with the name \""+name+"\".");
    }
  }
  
  //TODO recode this when service registration fixed (object name's properties registered)
  private QueueMBean findQueue(String name) {
    Object[] objs = queueTracker.getServices();
    if(objs==null)
      return null;
    for(Object o : objs) {
      QueueMBean q = (QueueMBean) o;
      if(q.getName().equals(name)) {
        return q;
      }
    }
    return null;
  }

  private TopicMBean findTopic(String name) {
    Object[] objs = topicTracker.getServices();
    if(objs==null)
      return null;
    for(Object o : objs) {
      TopicMBean t = (TopicMBean) o;
      if(t.getName().equals(name)) {
        return t;
      }
    }
    return null;
  }
  
  private AdminTopicMBean findAdminTopic() {
    try {
      return (AdminTopicMBean) adminTracker.waitForService(TIMEOUT);
    } catch (InterruptedException e) {
      return null;
    }
  }
  
  /**
   * Shows the pending message count of the subscription<br/>
   * Usage: [joram:mom:]subscriptionLoad <userName> <subscriptionName><br/>
   */
  public void subscriptionLoad(String[] args) {
    if(args.length != 2) {
      help("subscriptionLoad");
      return;
    }
    String userName = args[0];
    String subName = args[1];
    ClientSubscriptionMBean sub = null;
    try {
      sub = findClientSubscription(userName, subName);
    } catch (UserNotFoundException e) {
      System.err.println("Error: The user "+userName+" does not exist.");
      return;
    }
    if(sub == null) {
      System.err.println("Error: There is no subscription of "+userName+" to "+subName);
    } else {
      System.out.println("Pending count of \""+subName+"\" ("+userName+") : "+sub.getPendingMessageCount());
    }
  }
  
  private UserAgentMBean findUser(String userName) {
    Object[] objs = userTracker.getServices();
    for(Object o: objs) {
      UserAgentMBean u = (UserAgentMBean) o;
      if(u.getName().equals(userName))
        return u;
    }
    return null;
  }
  
  private ClientSubscriptionMBean findClientSubscription(String userName, String subName) throws UserNotFoundException {
    UserAgentMBean user = findUser(userName);
    if(user==null) {
      throw new UserNotFoundException(userName);
    }
    user.getSubscriptionNames();
    
    Object[] objs = clientSubTracker.getServices();
    if(objs==null)
      return null;
    for(Object o : objs) {
      ClientSubscriptionMBean c = (ClientSubscriptionMBean)o;
      
      if(c.getName().equals(subName))
        return c;
    }
    return null;
  }
  
  /**
   * Show information about a destination<br/>
   * Usage: [joram:mom:]info (queue|topic) <name><br/>
   *        [joram:mom:]info subscription <user name> <subscription name><br/>
   */
  public void info(String[] args) {
    if(args.length<2) {
      help("info");
      return;
    }
    String category = args[0];
    String destName = args[1];
    if(category.equals("topic")) {
      infoTopic(destName);
    } else if(category.equals("queue")) {
      infoQueue(destName);
    } else if(category.equals("subscription")) {
      infoSubscription(args[1], args[2]);
    } else {
      System.err.println("Error: Unknown category.");
      help("info");
      return;     
    }
  }

  private void infoTopic(String name) {
    TopicMBean dest = findTopic(name);
    if(dest==null) {
      System.err.println("Error: Topic \""+name+"\" not found.");
      return;
    }
    System.out.println("Topic name        : "+dest.getName());
    System.out.println("Destination ID    : "+dest.getDestinationId());
    System.out.println("Creation date     : "+dest.getCreationDate());
    System.out.println("Free reading      : "+(dest.isFreeReading()?"Yes":"No"));
    System.out.println("Free writing      : "+(dest.isFreeWriting()?"Yes":"No"));
    System.out.println("Message sent      : "+dest.getNbMsgsDeliverSinceCreation());
    System.out.println("Message received  : "+dest.getNbMsgsReceiveSinceCreation());
    System.out.println("Nb of subscribers : "+dest.getNumberOfSubscribers());
    System.out.println("Nb of DMQ messages: "+dest.getNbMsgsSentToDMQSinceCreation());
  }

  private void infoQueue(String name) {
    QueueMBean dest = findQueue(name);
    if(dest==null) {
      System.err.println("Error: Queue \""+name+"\" not found.");
      return;
    }
    System.out.println("Topic name              : "+dest.getName());
    System.out.println("Destination ID          : "+dest.getDestinationId());
    System.out.println("Creation date           : "+dest.getCreationDate());
    System.out.println("Free reading            : "+(dest.isFreeReading()?"Yes":"No"));
    System.out.println("Free writing            : "+(dest.isFreeWriting()?"Yes":"No"));
    System.out.println("Nb of pending messages  : "+dest.getPendingMessageCount());
    System.out.println("Messages sent           : "+dest.getNbMsgsDeliverSinceCreation());
    System.out.println("Messages received       : "+dest.getNbMsgsReceiveSinceCreation());
    System.out.println("Nb of DMQ messages      : "+dest.getNbMsgsSentToDMQSinceCreation());
  }

  private void infoSubscription(String userName, String subName) {
    ClientSubscriptionMBean sub = null;
    try {
      sub = findClientSubscription(userName, subName);
    } catch (UserNotFoundException e) {
      System.err.println("Error: The user "+userName+" does not exist.");
      return;
    }
    if(sub == null) {
      System.err.println("Error: Subscription not found.");
      return;
    }
    System.out.println("Subscription name        : "+sub.getName());
    System.out.println("Nb of pending messages   : "+sub.getPendingMessageCount());
    System.out.println("Nb of delivered messages : "+sub.getNbMsgsDeliveredSinceCreation());
    System.out.println("Nb of DMQ messages       : "+sub.getNbMsgsSentToDMQSinceCreation());
  }
  
  /**
   * Displays pending messages from a destination<br/>
   * Usage: [joram:mom:]lsMsg queue <queue name> [[first msg idx]:[last msg idx]]<br/>
   *        [joram:mom:]lsMsg subscription <username> <subscription name> [[first msg idx]:[last msg idx]]<br/>
   */
  public void lsMsg(String[] args) {
    //Argument parsing (includes messages retrieval)
    if(args.length < 2) {
      help("lsMsg");
      return;
    }
    
    String category = args[0].toLowerCase();
    String range = null;
    List<MessageView> msgs = null;
    if(category.equals("queue")) {
      if(args.length > 3) {
        help("lsMsg");
        return;
      }
      String queueName = args[1];
      if(args.length==3)
        range = args[2];
      //messages retrieval
      msgs = getQueueMessages(queueName);
      if(msgs == null) {
        System.err.println("Error: Queue not found.");
        return;
      }
    } else if(category.equals("subscription")) {
      if(args.length > 4 || args.length < 3) {
        help("lsMsg");
        return;
      }
      String userName = args[1];
      String subName = args[2];
      if(args.length==4)
        range = args[3];
      //messages retrieval
      try {
        msgs = getSubscriptionMessages(userName, subName);
        if(msgs == null) {
          System.err.println("Error: Subscription not found.");
          return;
        }
      } catch (UserNotFoundException e) {
        System.err.println("Error: The user "+userName+" does not exist.");
        return;
      }
    } else {
      System.err.println("Error: Unknown category: "+category);
      return;
    }
    
    if(msgs.isEmpty()) {
      System.out.println("There is no pending message in this "+category+".");
      return;     
    }
    
    //Range parsing
    if(range!=null) {
      if(range.matches("\\d*:\\d*")) {
        String[] parts = range.split(":",2);
        int start = parts[0].length()!=0?Integer.parseInt(parts[0]):0;
        int end = parts[1].length()!=0?Integer.parseInt(parts[1]):msgs.size()-1;
        msgs=getMessageRange(msgs, start, end);
      } else {
        System.err.println("Error: Incorrect range format. Must be [start]:[end].");
        return;
      }
    }
    
    //Data formatting
    String[][] table = new String[msgs.size()+1][];
    table[0] =
        new String[]{"Msg ID","Type","Creation date","Text","Expiration Date",
        "Priority"};
    //TODO: The order attribute (in joram.mom.messages.Message, visible via JMXMessageWrapper) unretrievable
    int i = 1;
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss dd-MMM-yyyy");
    for(MessageView msg : msgs) {
      String type = "UNKNOWN";
      switch (msg.getType()) {
        case org.objectweb.joram.shared.messages.Message.SIMPLE:
          type="SIMPLE"; break;
        case org.objectweb.joram.shared.messages.Message.TEXT:
          type="TEXT"; break;
        case org.objectweb.joram.shared.messages.Message.OBJECT:
          type="OBJECT"; break;
        case org.objectweb.joram.shared.messages.Message.MAP:
          type="MAP"; break;
        case org.objectweb.joram.shared.messages.Message.STREAM:
          type="STREAM"; break;
        case org.objectweb.joram.shared.messages.Message.BYTES:
          type="BYTES"; break;
        case org.objectweb.joram.shared.messages.Message.ADMIN:
          type="ADMIN"; break;
        default:
          break;
      }
      String date = msg.getExpiration()!=0?
          sdf.format(new Date(msg.getExpiration())):
          "-";
      table[i++] = new String[] {
          msg.getId(),
          type,
          sdf.format(new Date(msg.getTimestamp())),
          type=="TEXT"?msg.getText():"N/A",
          date,
          Integer.toString(msg.getPriority())
      };
    }
    ShellDisplay.displayTable(table, true);
  }
  
  /**
   * Checks whether Joram works<br/>
   * Usage: [joram:mom:]ping <br/>
   */
  public void ping() {
    AdminTopicMBean adminTopic = findAdminTopic();
    System.out.println(adminTopic==null?"KO":"OK");
  }  

  /**
   * Delete a pending message from a queue or subscription<br/>
   * Usage: [joram:mom:]deleteMsg queue <queue name> <msg id><br/>
   *        [joram:mom:]deleteMsg subscription <username> <subscription name> <msg id><br/>
   */
  public void deleteMsg(String[] args) {
    if(args.length == 3
        && args[0].equalsIgnoreCase("queue")) {
      deleteMsgQueue(args[1], args[2]);
    } else if (args.length == 4
        && args[0].equalsIgnoreCase("subscription")) {
      deleteMsgSub(args[1], args[2], args[3]);
    } else {
      help("deleteMsg");
    }
  }
 
  private void deleteMsgQueue(String queueName,String msgId) {
    try {
      if(!SynchronousAgent.getSynchronousAgent()
          .deleteQueueMessage(queueName, msgId))
        System.err.println("Error: The queue \""+queueName+"\" does not exist.");
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
      return;
    } catch (Exception e) {
      System.err.println("Error: Exception raised");
      e.printStackTrace();
      return;
    }
  }
  
  private void deleteMsgSub(String userName, String subName, String msgId) {
    //Check user & subscription
    try {
      ClientSubscriptionMBean sub = findClientSubscription(userName, subName);
      if(sub==null) {
        System.err.println("Error: The user \""+userName
            +"\" has no subscription of the name \""+subName+"\"");          
        return;
      }
    } catch(UserNotFoundException e) {
      System.err.println("Error: The user \""+userName+"\" does not exist.");          
      return;        
    }
    try {
      if(!SynchronousAgent.getSynchronousAgent().deleteSubMessage(userName, subName, msgId))
        System.err.println("Error: Couldn't delete message from "+userName
            +"'s subscription "+subName);
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
      return;
    } catch (Exception e) {
      System.err.println("Error: Exception raised");
      e.printStackTrace();
      return;
    }
  }
  
  /**
   * Deletes all pending messages from a subscription or a queue.<br/>
   * Usage: [joram:mom:]clear queue <name><br/>
   *        [joram:mom:]clear subscription <username> <subscription name><br/>
   */
  public void clear(String[] args) {
    if(args.length == 2 && args[0].equalsIgnoreCase("queue")) {
      String queueName = args[1];
      if(!JoramHelper.clearQueue(queueName))
        System.err.println("Error: The queue "+queueName+" does not exist.");
    } else if (args.length == 3 && args[0].equalsIgnoreCase("subscription")) {
      String userName = args[1];
      String subName = args[2];
      try {
        ClientSubscriptionMBean sub = findClientSubscription(userName, subName);
        if(sub==null) {
          System.err.println("Error: The user \""+userName
              +"\" has no subscription of the name \""+subName+"\"");          
          return;
        }
      } catch(UserNotFoundException e) {
        System.err.println("Error: The user \""+userName+"\" does not exist.");          
        return;        
      }
      if(!JoramHelper.clearSubscription(userName, subName))
        System.err.println("Error: Couldn't clear "+userName+"'s subscription "+subName);      
    } else {
      help("clear");
    }
  }
  
  /**
   * @deprecated
   */
  public void sendMsg(String[] args) {
    System.err.println("Error: Not yet implemented.");  
    help("sendMsg");
  }
  
  /**
   * @deprecated
   */
  public void receiveMsg(String[] args) {
    System.err.println("Error: Not yet implemented.");
    help("receiveMsg");
  }

  private List<MessageView> getQueueMessages(String queueName) {
    QueueMBean queue = findQueue(queueName);
    if(queue == null)
      return null;
    @SuppressWarnings("unchecked")
    List<MessageView> msgs = queue.getMessagesView();
    return msgs==null?new ArrayList<MessageView>():msgs;
  }
  
  private List<MessageView> getSubscriptionMessages(String userName, String subscriptionName) throws UserNotFoundException {
    ClientSubscriptionMBean sub = findClientSubscription(userName, subscriptionName);
    if(sub==null)
      return null;
    @SuppressWarnings("unchecked")
    List<MessageView> msgs = sub.getMessagesView();
    return msgs==null?new ArrayList<MessageView>():msgs;
  }
  
  private List<MessageView> getMessageRange(List<MessageView> msgs, int start, int end) {
    if(start<0)
      start=0;
    if(end >= msgs.size())
      end = msgs.size()-1;
    if(start > end)
      return msgs;
    List<MessageView> res = new ArrayList<MessageView>();
    for(int i = start; i<=end; i++)
      res.add(msgs.get(i));
    return res;
  }
  
  public static void main(String[] args) {
    for(String cmd : COMMANDS) {
      System.out.println("======== "+cmd+" ========");
      help(cmd);
      System.out.println();
    }
  }
  
  private class UserNotFoundException extends Exception{
    /**
     * 
     */
    private static final long serialVersionUID = -8029807791362829155L;

    public UserNotFoundException(String userName) {
      super("The user ["+userName+"] was not found.");
    }
  }
}

