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

import java.awt.DisplayMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.objectweb.joram.mom.dest.AdminTopicMBean;
import org.objectweb.joram.mom.dest.TopicMBean;
import org.objectweb.joram.mom.dest.QueueMBean;
import org.objectweb.joram.mom.dest.DestinationMBean;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.UserAgentMBean;
import org.objectweb.joram.shared.DestinationConstants;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.ow2.joram.shell.ShellDisplay;

import fr.dyade.aaa.agent.AgentMBean;
import fr.dyade.aaa.agent.AgentServer;

public class MOMCommandsImpl implements MOMCommands {
  
  public static final String NAMESPACE = "joram:mom";
  private static final int TIMEOUT = 1000;
//  private static final int DEFAULT_SID = 0;
 
  private BundleContext bundleContext;
  private ServiceTracker destinationTracker;
  private ServiceTracker queueTracker;
  private ServiceTracker topicTracker;
  private ServiceTracker userTracker;
  private ServiceTracker adminTracker;
  private ServiceTracker clientSubTracker;

  public MOMCommandsImpl(BundleContext context) {
    this.bundleContext = context;
    // TODO
//    this.tracker = new ServiceTracker(bundleContext, AdminItf.class, null);
//    this.tracker.open();
    this.destinationTracker = new ServiceTracker
                (bundleContext, DestinationMBean.class, null);
    this.queueTracker = new ServiceTracker
                (bundleContext, QueueMBean.class,       null);
    this.topicTracker = new ServiceTracker
                (bundleContext, TopicMBean.class,       null);
    this.userTracker = new ServiceTracker
                (bundleContext, UserAgentMBean.class,   null);
    this.adminTracker = new ServiceTracker
                (bundleContext, AdminTopicMBean.class,  null);
    this.clientSubTracker = new ServiceTracker
                (bundleContext, ClientSubscriptionMBean.class,  null);

    destinationTracker.open();
    queueTracker.open();
    topicTracker.open();
    userTracker.open();
    adminTracker.open();
    clientSubTracker.open();
  }
  
  /**
   * Print the descrption of the given command
   * @param command Name of the command to describe
   */
  private static void help(String command) {
    StringBuffer buf = new StringBuffer();
    String fullCommand = "["+NAMESPACE+":]"+command;
    buf.append("Usage: ").append(fullCommand).append(" ");
    if(command.equals("list")) {
      buf.append("<category> [username]");
      buf.append("\n\tPossible categories: destination, topic, queue, user, subscription");
      buf.append("\n\tNB: For the subscription category, you must provide the user name.");     
    } else if(command.equals("create")) {
      buf.append("<topic|queue> <name> [option]...\n");
      buf.append("   or: ").append(fullCommand).append(" help\n");
      buf.append("Options: -sid <server id>\tSpecifies on which server the destination is to be created\n");
      buf.append("                         \tDefault: This server\n");
      buf.append("         -ext <extension>\tSpecifies which extension class to instanciate\n");
      buf.append("                         \tDefault: None");
   
    } else if(command.equals("queueLoad")) {
      buf.append("<queueName>");
    } else if(command.equals("subscriptionLoad")) {
      buf.append("<userName> <subscriptionName>");
    } else if(command.equals("")) {
      buf.append("");
    } else if(command.equals("")) {
      buf.append("");
    } else {
      System.err.println("Unknown command: "+command);
      return;
    }
    System.out.println(buf.toString());
  }

  public void stop() {
    // TODO Auto-generated method stub
    System.err.println("Not yet implemented.");
//    try {
//      JoramAdmin service =
//          (JoramAdmin) tracker.waitForService(MAX_TIMEOUT);
//      if(service != null)
//        service.stopServer();
//      else //TODO use logger
//        System.err.println("No service found.");
//    } catch (InterruptedException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (Exception e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
  }

  public void start() {
    // TODO Auto-generated method stub
    System.err.println("Not yet implemented.");
  }


  public void list() {
    help("list");
  }
  
  public void list(String[] args) {
    String category = args[0];
    if(category.equals("destination")
    || category.equals("queue")
    || category.equals("topic")) {
      listDestination(category);
    } else if(category.equals("user")) {
      listUser();
    } else if (category.equals("subscription") && args.length==2) {
      listSubscription(args[1]);
    } else {
      help("list");
    }
  }
  
  private void listDestination(String category) {
    Object[] obj;
    
    if(category.equals("destination")) {
      obj = destinationTracker.getServices();
    } else if(category.equals("queue")) {
      obj = queueTracker.getServices();
    } else {
      obj = topicTracker.getServices();
    }
    
    if(obj == null || obj.length ==0) {
      System.err.println("Error: There is no "+category);
      return;
    }
    
    HashMap<String, DestinationMBean> dests = new HashMap<String, DestinationMBean>();
    for(Object o : obj) {
      DestinationMBean d = (DestinationMBean) o;
      dests.put(d.getDestinationId(), d);
    }

    String[][] table = new String[dests.size()+1][];
    table[0] = new String[] {"Id","Name","Type","Creation Date","Nb Rec Msg","Nb Del Msg","Read","Write"};
    int i = 1;
    for(DestinationMBean d : dests.values()) {
      String type = "NA";
      if(d instanceof TopicMBean)
        type="Topic";
      else if(d instanceof QueueMBean)
        type="Queue";
      table[i++] = new String[] { d.getDestinationId(),
                                d.getName(),
                                type,
                                d.getCreationDate(),
                                Long.toString(d.getNbMsgsReceiveSinceCreation()),
                                Long.toString(d.getNbMsgsDeliverSinceCreation()),
                                (d.isFreeReading()?"Yes":"No"),
                                (d.isFreeWriting()?"Yes":"No")};
    }
    int n = dests.size();
    if(n < 2)
      System.out.println("There is " + dests.size() + " "+category+".");
    else
      System.out.println("There are " + dests.size() + " "+category+"s.");
    System.out.println();
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
    ServiceTracker<UserAgentMBean, UserAgentMBean> userTracker =
        new ServiceTracker<UserAgentMBean, UserAgentMBean>(bundleContext, UserAgentMBean.class, null);
    userTracker.open();
    UserAgentMBean user = null;
    for(Object o : userTracker.getServices()) {
      UserAgentMBean u = (UserAgentMBean) o;
      if(u.getName().equals(userName)) {
        user = u;
        break;
      }
    }
    if(user == null) {
      System.out.println("The user "+userName+" does not exist.");
      return;
    }
    //Step 2: retrieve the user's subscriptions
    String[] names = user.getSubscriptionNames();
    HashSet<ClientSubscriptionMBean> subs =
        new HashSet<ClientSubscriptionMBean>();
    for(Object o : clientSubTracker.getServices()) {
      ClientSubscriptionMBean s = (ClientSubscriptionMBean) o;
      for(String n : names)
        if(n.equals(s.getName())) {
          subs.add(s);
          break;
        }
    }
    if(subs.size()==0) {
      System.out.println("The user "+userName+" has no subscription.");
      return;
    }
    
    //Step 3: Display
    String[][] table = new String[subs.size()+1][8];
    table[0] = new String[]{"Name",
                            "TopicIdAsString",
                            "PendingMessageCount",
                            "Delivered Message Count",
                            "Selector",
                            "NbMsgMax",
                            "NbMsgDeliveredSinceCreation",
                            "NbMsgSendToDMQSinceCreation"};
    int i=1;
    for(ClientSubscriptionMBean sub : subs) {
      table[i] = new String[] {
        sub.getName(),
        sub.getTopicIdAsString(),
        String.valueOf(sub.getPendingMessageCount()),
        String.valueOf(sub.getDeliveredMessageCount()),
        sub.getSelector()!=null?"Oui":"Non",
        String.valueOf(sub.getNbMaxMsg()),
        String.valueOf(sub.getNbMsgsDeliveredSinceCreation()),
        String.valueOf(sub.getNbMsgsSentToDMQSinceCreation())
      };
    }
    ShellDisplay.displayTable(table, true);
  }
  

  public void create(String[] args) {
    if(args == null || args.length<2)  {
      System.err.println("[1]Error: Bad arguments.");
      help("create");
      return;
    }
    byte type = 0;
    String name= null;
    int sid = AgentServer.getServerId();
    String ext = null;
    
    if(args[0].equals("help")) {
      help("create");
      return;
    } else if(args[0].equals("topic")) {
      type=DestinationConstants.TOPIC_TYPE;
    }  else if(args[0].equals("queue")) {
      type=DestinationConstants.QUEUE_TYPE;
    } else {
      System.err.println("[2]Error: Bad arguments.");
      help("create");
      return;
    }
    
    name=args[1];
   
    for(int i = 2; i < args.length; i++) {
      if(args[i].equals("-sid") && args.length>i+1) {
        sid=Integer.parseInt(args[++i]);
      } else if(args[i].equals("ext") && args.length>i+1) {
        ext = args[++i];
      } else {
        System.err.println("[3]Error: Bad arguments.");
        help("create");
        return;
     }
    }
    
    AdminTopicMBean admin;
    try {
      admin = (AdminTopicMBean) adminTracker.waitForService(TIMEOUT);
    } catch (InterruptedException e) {
      System.err.println("[4]Error: Interrupted.");
      return;
    }
    // TODO: checks whether the destination has been properly created
    switch(type) {
      case DestinationConstants.QUEUE_TYPE:
        if(ext==null)
          admin.createQueue(name,sid);
        else
          admin.createQueue(name,ext,sid);
        System.out.println(
            "Queue "+name+" created on server "+sid+
            (ext==null?".":" with the class "+ext+"."));
        break;
      case DestinationConstants.TOPIC_TYPE:
        if(ext==null)
          admin.createTopic(name,sid);
        else
          admin.createTopic(name,ext,sid);
        System.out.println(
            "Topic "+name+" created on server "+sid+
            (ext==null?".":" with the class "+ext+"."));
        break;
    }
  }
  
  public void delete(String[] args) {
    AgentMBean agent;
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
    
    AgentMBean[] agents = new AgentMBean[objs.length];
    for(int i = 0; i < agents.length; i++)
      agents[i] = (AgentMBean) objs[i];
    for(AgentMBean a : agents)
      if(a.getName().equals(args[1]))
        a.delete();
  }
  
  public void addUser(String[] args) {
    String userName = null;
    Scanner s= new Scanner(System.in);
    if(args.length==0) {
      System.out.print("User name: "); System.out.flush();
      userName = s.nextLine();
    } else if(args.length==1) {
      userName = args[0];
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
   
    AdminTopicMBean admin;
    try {
      admin = (AdminTopicMBean) adminTracker.waitForService(TIMEOUT);
      //TODO: No info about the result of the creation... Now assuming it works.
      admin.createUser(userName, pwd);
      System.out.println("User succesfully created.");
    } catch (InterruptedException e) {
      System.err.println("[4]Error: Interrupted.");
      return;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public void testDoublon() {
    System.out.println("MOMCommandsImpl.testDoublon");
  }

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
      System.err.println("There is no queue with the name \""+name+"\".");
    }
  }
  
  //TODO recode this when service registration fixed (object name's properties registered)
  private QueueMBean findQueue(String name) {
    Object[] objs = queueTracker.getServices();
    for(Object o : objs) {
      QueueMBean q = (QueueMBean) o;
      if(q.getName().equals(name)) {
        return q;
      }
    }
    return null;
  }

  public void subscriptionLoad(String[] args) {
    if(args.length != 2) {
      help("subscriptionLoad");
      return;
    }
    //TODO not used yet
    String userName = args[0];
    String subName = args[1];
    ClientSubscriptionMBean sub =
        findClientSubscription(userName, subName);
    if(sub == null) {
      System.err.println("Error: There is no subscription of "+userName+" to "+subName);
    } else {
      System.out.println("Pending count of \""+subName+"\" ("+userName+") : "+sub.getPendingMessageCount());
    }
  }
  
  private ClientSubscriptionMBean findClientSubscription(String userName, String subName) {
    Object[] objs = clientSubTracker.getServices();
    for(Object o : objs) {
      ClientSubscriptionMBean c = (ClientSubscriptionMBean)o;
      if(c.getName().equals(subName))
        return c;
    }
    return null;
  }
  
  public void info(String[] args) {
    if(args.length!=1) {
      help("info");
      return;
    }
    String destName = args[0];
    Object[] dests = destinationTracker.getServices();
    DestinationMBean dest = null;
    for(Object o : dests) {
      DestinationMBean d = (DestinationMBean) o;
      if(d.getName().equals(destName)) {
        dest = d;
        break;
      }
    }
    if(dest==null) {
      System.out.println("Error: There is no destination with the name \""+destName+"\".");
    } else if(dest instanceof TopicMBean) {
      infoTopic((TopicMBean)dest);
    } else if (dest instanceof QueueMBean) {
      infoQueue((QueueMBean)dest);
    } else {
      System.out.println("Unknown destination type.");
    }
  }

  private void infoTopic(TopicMBean dest) {
    System.out.println("Topic name       : "+dest.getName());
    System.out.println("Destination ID   : "+dest.getDestinationId());
    System.out.println("Creation date    : "+dest.getCreationDate());
    System.out.println("Free reading     : "+(dest.isFreeReading()?"Yes":"No"));
    System.out.println("Free writing     : "+(dest.isFreeWriting()?"Yes":"No"));
    System.out.println("Message sent     : "+dest.getNbMsgsDeliverSinceCreation());
    System.out.println("Message received : "+dest.getNbMsgsReceiveSinceCreation());
    System.out.println("Nb of subscribers: "+dest.getNumberOfSubscribers());
}

  private void infoQueue(QueueMBean dest) {
    System.out.println("Topic name        : "+dest.getName());
    System.out.println("Destination ID    : "+dest.getDestinationId());
    System.out.println("Creation date     : "+dest.getCreationDate());
    System.out.println("Free reading      : "+(dest.isFreeReading()?"Yes":"No"));
    System.out.println("Free writing      : "+(dest.isFreeWriting()?"Yes":"No"));
    System.out.println("Pending messages  : "+dest.getPendingMessageCount());
    System.out.println("Messages sent     : "+dest.getNbMsgsDeliverSinceCreation());
    System.out.println("Messages received : "+dest.getNbMsgsReceiveSinceCreation());
  }

  public static void main(String[] args) {
    help("list");
    help("create");
  }
}
