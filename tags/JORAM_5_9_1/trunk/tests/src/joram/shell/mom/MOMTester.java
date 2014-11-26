/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package joram.shell.mom;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.Connection;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Subscription;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.mom.proxies.UserAgent;
import org.objectweb.joram.mom.proxies.UserAgentMBean;
import org.ow2.joram.shell.mom.commands.MOMCommands;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.EngineMBean;
import fr.dyade.aaa.common.Strings;


public class MOMTester extends Thread implements BundleActivator {

  private static final long TIMEOUT = 10000;
  private static final long TEMPORIZER = 200;
 
  private static String HOST = "localhost";
  private static int PORT = 2560;
  
  private final static short SID = 0;
  private final static String USER_NAME = "testuser";
  private final static String USER_PWD = "testpwd";
  private final static String USER2_NAME = "testusertwo";
  private final static String USER2_PWD = "testpwdtwo";
  private final static String NON_EXISTENT_USER = "nonExistentUser";
  private final static String NON_EXISTENT_QUEUE_NAME = "nonExistentQueue";
  private final static String QUEUE_NAME = "testQueue";
  private final static String TOPIC_NAME = "testTopic";
  private final static String NON_EXISTENT_TOPIC_NAME = "nonExistentTopic";
  private final static String SUB_NAME = "testSubscription";
  private final static String NON_EXISTENT_SUB_NAME = "nonExistentSubscription";
  private final static String NON_EXISTENT_MSG_ID = "AMessageIdThatCannotExist";
  private final static int NB_MSG = 10;
 
  private ConnectionFactory cf;
  
  /* STDOUT and STDIN capture */
  private ByteArrayOutputStream baos_out = new ByteArrayOutputStream();
  private ByteArrayOutputStream baos_err = new ByteArrayOutputStream();
  private PrintStream prevStdout = null, prevStderr = null;

  /* OSGI */
  private BundleContext context;
  @SuppressWarnings("rawtypes")
  private ServiceTracker cmdTracker;
  
  /* Communication file*/
  private final static String STDOUT_FILE = "joram.shell.mom.stdout";
  private final static String STDERR_FILE = "joram.shell.mom.stderr";
  private final static String LOCK_FILE = "joram.shell.mom.lock";
  private final static String TEST_FILE = "joram.shell.mom.tests";
  private static boolean LOG_STD = true;

  private BufferedWriter stdoutFile;
  private BufferedWriter stderrFile;
  private ObjectOutputStream excFile;
  private File lock;
  
  /* Failures */
  private Collection<Exception> excCollec;
  
  /* Temporary logger */
  private BufferedWriter logger;
  private final static String LOG_FILE = "joram.shell.mom.log";
  
  void assertTrue(String msg, boolean bool) {
    if(!bool) {
        Exception exc = new Exception(msg);
        excCollec.add(exc);
    }
  }
  
  void assertFalse(String msg, boolean bool) {
    assertTrue(msg, !bool);
  }

  void assertNotNull(String msg, Object obj) {
    assertTrue(msg, obj!=null);
  }
  
  void assertNull(String msg, Object obj) {
    assertTrue(msg, obj==null);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void start(BundleContext arg0) throws Exception {
    logger = new BufferedWriter(new FileWriter(LOG_FILE));;
    
    context = arg0;
    cmdTracker = new ServiceTracker(context,
        MOMCommands.class.getCanonicalName(), null);
    cmdTracker.open();
    
    lock = new File(LOCK_FILE);
    lock.createNewFile();
    
    stdoutFile = new BufferedWriter(new FileWriter(STDOUT_FILE));
    stderrFile = new BufferedWriter(new FileWriter(STDERR_FILE));
    excFile = new ObjectOutputStream(new FileOutputStream(TEST_FILE));
    excCollec = new ArrayList<Exception>();
    
    start();
  }
  
  @Override
  public void stop(BundleContext arg0) throws Exception {
    closeFiles();
    cmdTracker.close();
  }
  
  public void closeFiles() {
      try {
        excFile.writeObject(excCollec);
        excFile.close();
        
        stderrFile.close();
        stdoutFile.close();
        logger.close();
        
        lock.delete();
     } catch (IOException e) {
        log(e);
     }    
  }
  
  @SuppressWarnings("deprecation")
  public void run() {
    try {
      cf = TcpConnectionFactory.create(HOST, PORT);
        ((TcpConnectionFactory) cf).getParameters().connectingTimer = 5;
        ((TcpConnectionFactory) cf).getParameters().multiThreadSync = false;
      AdminModule.connect(cf, "root", "root");
        
      testCreate();
      testList();
      testLsMsg();
      testQueueLoad();
      testSubscriptionLoad();
      testInfo();
      testDeleteMsg();
      testClear();
      testDelete();
      testPing();
    } catch (Exception e) {
      excCollec.add(e);
    } finally {
      closeFiles();      
    }
  }

  private MOMCommands getMOMCommands(String testedCmd) {
    try {
      return (MOMCommands) cmdTracker.waitForService(TIMEOUT);
    } catch (InterruptedException e) {
      log(e.getLocalizedMessage());
      return null;
    }
  }

  /* create
   *  a) Pour chaque catégorie d'items, l'accés à un item suite à sa création
   *  réussit.
   */
  @SuppressWarnings("deprecation")
  private void testCreate() {
    MOMCommands cmds = getMOMCommands("[joram:mom:create]");
    if(cmds==null) { log("ERROR: Couldn't retrieve MOM Commands service.");return;}
    
    //Test for the user
    boolean found = AdminTopic.lookupUser(USER_NAME)!=null;

    if(!found) {
      InputStream stdin = System.in;
      System.setIn(new ByteArrayInputStream(USER_PWD.getBytes()));
      cmds.create(new String[]{"user",USER_NAME});
      System.setIn(stdin);
      try {
        Thread.sleep(TEMPORIZER);
      } catch (InterruptedException e) {
        log("ERROR: [joram:mom:create user] Interrupted");
      }
      found = AdminTopic.lookupUser(USER_NAME)!=null;
      assertTrue("[joram:mom:create user] Can't find the created user.", found);
    } else
      log("ERROR: [joram:mom:create user] The user \""+USER_NAME+"\" already exists.");
    
    //Test for the queue
    // Normal case
    found = AdminTopic.isDestinationTableContain(QUEUE_NAME);

    if(!found) {
      startStderrCapture();
      startStdoutCapture();
      cmds.create(new String[]{"queue",QUEUE_NAME});
      stopStderrCapture();
      stopStdoutCapture();
      try {
        Thread.sleep(TEMPORIZER);
      } catch (InterruptedException e) {
        log("ERROR: [joram:mom:create queue] Interrupted");
      }
      found = AdminTopic.isDestinationTableContain(QUEUE_NAME);
      assertTrue("[joram:mom:create queue] Can't find the created queue.", found);
    } else
      log("ERROR: [joram:mom:create queue] The queue \""+QUEUE_NAME+"\" already exists.");

    //Test for the topic
    found = AdminTopic.isDestinationTableContain(TOPIC_NAME);
    if(!found) {
      startStderrCapture();
      cmds.create(new String[]{"topic",TOPIC_NAME});
      stopStderrCapture();
      try {
        Thread.sleep(TEMPORIZER);
      } catch (InterruptedException e) {
        log("ERROR: [joram:mom:create queue] Interrupted");
      }
      found = AdminTopic.isDestinationTableContain(TOPIC_NAME);
      assertTrue("[joram:mom:create topic] Can't find the created topic.", found);
    } else
      log("ERROR: [joram:mom:create topic] The topic \""+TOPIC_NAME+"\" already exists.");
  }
  
  /* list
   *  a) Pour toute catégorie d'items (destination, topic, queue, user,
   *     subscription), chaque item apparaît une et une seule fois, et pas d'item
   *     surperflu n'apparaît.
   *  b) Dans le cas des souscriptions, si l'utilisateur n'est pas trouvé, un
   *     message le précise.
   */
  private void testList() {
    MOMCommands cmds = getMOMCommands("[joram:mom:list]");
    if(cmds==null) { log("ERROR: Couldn't retrieve MOM Commands service.");return;}

    String res, regex;
    
    //Test for the queue
    try {
      Queue.create(SID,QUEUE_NAME);
      startStdoutCapture();
      cmds.list(new String[]{"queue"});
      res = stopStdoutCapture();
      regex = ".* ! "+QUEUE_NAME+" * ! .* ! .* ! .* ! .*";
      assertTrue("[joram:mom:list queue] Created queue named \""+QUEUE_NAME+"\" not found", hasMatchingLine(res, regex));
    } catch (AdminException e) {
      log("[joram:mom:list queue] Creation request failed.");
    } catch (ConnectException e) {
      log("[joram:mom:list queue] Connection closed or broken.");
    } catch (Exception e) {
      log("[joram:mom:list queue] "+e.getClass().getCanonicalName());
    }
    
    //Test for the topic
    Topic topic = null;
    try {
      topic = Topic.create(SID,TOPIC_NAME);
      
      startStdoutCapture();
      cmds.list(new String[]{"topic"});
      res = stopStdoutCapture();
      
      regex = ".* ! "+TOPIC_NAME+" * ! .* ! .* ! .* ! .*";
      assertTrue("[joram:mom:list topic] Created topic named \""+TOPIC_NAME+"\" not found", hasMatchingLine(res, regex));
    } catch (AdminException e) {
      log("[joram:mom:list topic] Creation request failed.");
    } catch (ConnectException e) {
      log("[joram:mom:list topic] Connection closed or broken.");
    } catch (Exception e) {
      log("[joram:mom:list topic] "+e.getClass().getCanonicalName());
    }
    
    //Test for the destination
    try {
      startStdoutCapture();
      cmds.list(new String[]{"destination"});
      res = stopStdoutCapture();
      //#0.0.1028 ! testQueue       ! Queue ! Mon Sep 10 09:10:52 CEST 2012 ! -/-  
      //.* ! testQueue * ! Queue * ! .* ! [r-]/[w-] *
      regex = ".* ! "+QUEUE_NAME+" * ! Queue * ! .* ! [r-]/[w-] *";
      assertTrue("[joram:mom:list destination] Created queue named \""+QUEUE_NAME+"\" not found",
          hasMatchingLine(res, regex));
      regex = ".* ! "+TOPIC_NAME+" * ! Topic * ! .* ! [r-]/[w-] *";
      assertTrue("[joram:mom:list destination] Created topic named \""+TOPIC_NAME+"\" not found",
          hasMatchingLine(res, regex));
    } catch (Exception e) {
      log("[joram:mom:list destination] "+e.getClass().getCanonicalName());
    }
    
    //Test for the subscription
    try {
      if(topic!=null) {
        //Normal case: There is a subscription
        createSubscriber(SID, TOPIC_NAME, SUB_NAME, USER_NAME, USER_PWD);

        startStdoutCapture();
        cmds.list(new String[]{"subscription",USER_NAME});
        res = stopStdoutCapture();
        
        regex = SUB_NAME.substring(0, 9)+" * ! #\\d+.\\d+.\\d+ * ! \\d+ * ! \\d+ * ! \\d+ * ! -?\\d+ * ! \\d+ *";
        assertTrue("[joram:mom:list subscription] Created subscripion named \""+SUB_NAME+"\" not found",
            hasMatchingLine(res, regex));
        
        //Error case: The user doesn't exist
        startStderrCapture();
        cmds.list(new String[]{"subscription",NON_EXISTENT_USER});
        res = stopStderrCapture();
        
        assertTrue("[joram:mom:list subscription] No error displayed when the user doesn't exist.",
            (res != null && res.length()!=0 && hasMatchingLine(res, "Error: .*")));
      } else {
        log("ERROR: Topic not found.");
      }
    } catch(JMSSecurityException e) {
      log("[joram:mom:list subscription] Identification failed.");
    } catch (IllegalStateException e) {
      log("[joram:mom:list subscription] Server not listening.");
    } catch (JMSException e) {
      log("[joram:mom:list subscription] "+e.getClass().getCanonicalName());
    } catch (ConnectException e) {
      log("[joram:mom:list subscription] Connection closed or broken.");
    } catch (AdminException e) {
      log("[joram:mom:list subscription] Request failed.");
    }
  }
  
  /* lsMsg
   *  a) Deux appels, séparés d'un envoi de message, on vérifie l'arrivée du
   *     nouveau message.
   *  b) Dans le cas des souscriptions, si l'utilisateur n'est pas trouvé, un
   *     message d'erreur le précise.
   *  c) Si la destination n'existe pas, une erreur est affichée.
   */
  private void testLsMsg() {
    String res;
    MOMCommands cmds = getMOMCommands("[joram:mom:lsMsg]");
    if(cmds==null) { log("ERROR: Couldn't retrieve MOM Commands service.");return;}

    //Queue
    //Normal case
    try {
      sendMessagesToQueue(QUEUE_NAME, NB_MSG);
    } catch (ConnectException e) {
      log("[joram:mom:lsMsg queue] Connection closed or broken.");
    } catch (JMSException e) {
      log("[joram:mom:lsMsg queue] "
        +e.getClass().getCanonicalName()+" : "+e.getMessage());
    } catch (AdminException e) {
      log("[joram:mom:lsMsg queue] Creation request failed.");
    }
    startStdoutCapture();
    cmds.lsMsg(new String[]{"queue",QUEUE_NAME});
    res = stopStdoutCapture();
    
    int nbMatch = 0;
//    for(String line : res.split("\n")){
//      String regex = "ID:\\d+.\\d+.\\d+c\\d+m\\d+ * ! [A-Z]+ * ! .* ! .* ! .* ! \\d+ *\t";
//      if(line.matches(regex)) nbMatch++;
//    }
    //ID:0.0.1027c1m8 ! TEXT ! 10:36:40 11/9/2012 ! -         ! 4
    String regex = "ID:\\d+.\\d+.\\d+c\\d+m\\d+ +! [A-Z]+ +! .* +! .* +! \\d+";
    nbMatch = countMatchingLines(res, regex);
    assertTrue("[joram:mom:lsMsg queue] Not all messages have been displayed. Read: "+nbMatch,
        nbMatch>=NB_MSG);
    
    //Error case: the queue doesn't exist
    startStderrCapture();
    cmds.lsMsg(new String[]{"queue",NON_EXISTENT_QUEUE_NAME});
    res = stopStderrCapture();
    assertTrue("[joram:mom:lsMsg subscription] No error when giving a wrong queue name " +
        "name.", res.contains("Error: Queue not found."));
    
    // Subscription
    // Normal case
    try {
      createSubscriber(SID, TOPIC_NAME, SUB_NAME, USER_NAME, USER_PWD);
    } catch (ConnectException e) {
      log("[joram:mom:lsMsg subscription] Connection closed or broken.");
      log(e);
    } catch (JMSException e) {
      log("[joram:mom:lsMsg subscription] "
          +e.getClass().getCanonicalName()+" : "+e.getMessage());
      log(e);
    } catch (AdminException e) {
      log("[joram:mom:lsMsg subscription] Administration error.");
      log(e);
    }
    
    try {
      sendMessagesToTopic(TOPIC_NAME, NB_MSG);
    } catch (ConnectException e) {
      log("[ERROR] Identification failed.");
    } catch (JMSException e) {
      log("[joram:mom:lsMsg subscription] "
          +e.getClass().getCanonicalName()+" : "+e.getMessage());
      log(e);
    } catch (AdminException e) {
      log("[joram:mom:lsMsg subscription] Administration error.");
      log(e);
    }

    startStdoutCapture();
    cmds.lsMsg(new String[]{"subscription",USER_NAME,SUB_NAME});
    res = stopStdoutCapture();

    nbMatch = 0;
//    for(String line : res.split("\n")){
//      String regex = "ID:\\d+.\\d+.\\d+c\\d+m\\d+ * ! [A-Z]+ * ! .* ! .* ! .* ! \\d+ *\t";
//      if(line.matches(regex)) nbMatch++;
//    }
    //Same as [lsMsg queue]
    //regex = "ID:\\d+.\\d+.\\d+c\\d+m\\d+ * ! [A-Z]+ * ! .* ! .* ! .* ! \\d+ *";
    nbMatch = countMatchingLines(res, regex);
    assertTrue("[joram:mom:lsMsg subscription] Not all messages have been displayed. Read: "+nbMatch,
        nbMatch>=NB_MSG);
    
    //Error case: the user doesn't exist
    startStderrCapture();
    cmds.lsMsg(new String[]{"subscription",NON_EXISTENT_USER,SUB_NAME});
    res = stopStderrCapture();
    assertTrue("[joram:mom:lsMsg subscription] No error when giving a wrong user " +
    		"name.", res.contains("Error: The user "+NON_EXISTENT_USER+" does not exist."));
    
    //Error case: the subscription doesn't exist
    startStderrCapture();
    cmds.lsMsg(new String[]{"subscription",USER_NAME,NON_EXISTENT_SUB_NAME});
    res = stopStderrCapture();
    assertTrue("[joram:mom:lsMsg subscription] No error when giving a wrong subscription name " +
        "name.", res.contains("Error: Subscription not found."));
  }

  /* queueLoad
   *  a) On obtient un résultat de la forme attendu.
   *  b) Si la queue n'existe pas, aucune erreur n'est affichée.
   */
  private void testQueueLoad() {
    String res;
    MOMCommands cmds = getMOMCommands("[joram:mom:queueLoad]");
    if(cmds==null) { log("ERROR: Couldn't retrieve MOM Commands service.");return;}

    //Normal case
    startStdoutCapture();
    cmds.queueLoad(new String[]{QUEUE_NAME});
    res = stopStdoutCapture();

    assertTrue("[joram:mom:queueLoad] The output is not as expected.",
        res.matches("Pending count of \""+QUEUE_NAME+"\" : \\d+\n"));

    //Error case
    startStderrCapture();
    cmds.queueLoad(new String[]{NON_EXISTENT_QUEUE_NAME});
    res = stopStderrCapture();

    assertTrue("[joram:mom:queueLoad] No error when giving a wrong queue name.",
        res.contains("Error: There is no queue with the name \""+NON_EXISTENT_QUEUE_NAME+"\"."));
  }

  /* subscriptionLoad
   *  a) On obtient un résultat de la forme attendu.
   *  b) Si l'utilisateur n'existe pas, une erreur est affichée.
   *  c) Si la sousscription n'existe pas, une erreur est affichée.
   */
  private void testSubscriptionLoad() {
    String res;
    MOMCommands cmds = getMOMCommands("[joram:mom:queueLoad]");
    if(cmds==null) { log("ERROR: Couldn't retrieve MOM Commands service.");return;}

    //Normal case
    startStdoutCapture();
    cmds.subscriptionLoad(new String[]{USER_NAME,SUB_NAME});
    res = stopStdoutCapture();

    assertTrue("[joram:mom:subscriptionLoad] The output is not as expected.",
        res.matches("Pending count of \""+SUB_NAME+"\" \\("+USER_NAME+"\\) : \\d+\n"));

    //Error case: the user doesn't exist
    startStderrCapture();
    cmds.subscriptionLoad(new String[]{NON_EXISTENT_USER,SUB_NAME});
    res = stopStderrCapture();

    assertTrue("[joram:mom:subscriptionLoad] No error when giving a wrong user name.",
        res.contains("Error: The user "+NON_EXISTENT_USER+" does not exist."));

    //Error case: the subscription doesn't exist
    startStderrCapture();
    cmds.subscriptionLoad(new String[]{USER_NAME,NON_EXISTENT_SUB_NAME});
    res = stopStderrCapture();

    assertTrue("[joram:mom:subscriptionLoad] No error when giving a wrong subscription name.",
        res.contains("Error: There is no subscription of "+USER_NAME+" to "+NON_EXISTENT_SUB_NAME));
  }
  
  /* info
   *  a) On obtient un résultat de la forme attendu.
   *  b) Dans le cas des souscriptions, si l'utilisateur n'est pas trouvé, un message le précise.
   *  c) Si la destination n'existe pas, aucune erreur n'est affichée.
   */
  private void testInfo() {
    log(new Exception("testInfo : not implemented"));
  }
  /* ping
   *  a) Avec JORAM en marche, le résultat est positif.
   *  b) Avec JORAM non démarré, le résultat est négatif.
   */
  private void testPing() {
    MOMCommands cmds = getMOMCommands("[joram:mom:ping]");
    if(cmds==null) { log("ERROR: Couldn't retrieve MOM Commands service.");return;}
    
    //Normal case
    startStdoutCapture();
    cmds.ping();
    String res = stopStdoutCapture();
    assertTrue("[joram:mom:ping] The resultat should be positive but is not.",
        res.contains("OK"));
    
    //Error case
    AgentServer.stop();
    startStdoutCapture();
    cmds.ping();
    res = stopStdoutCapture();
    assertTrue("[joram:mom:ping] The resultat should be negative but is positive.",
        res.contains("KO"));
  }
  
  /* deleteMsg
   *  a) On envoie un message, on vérifie sa présence (et récupère l'id) et le supprime. Le message ne doit plus apparaître ensuite.
   *  b) Dans le cas des souscriptions, si l'utilisateur n'est pas trouvé,
   *     un message le précise.
   *  c) Si la destination n'existe pas, une erreur est affichée.
   */
    /**************************************************
     *                 WARNING:                       
     * This test must be called after testLsMsg which 
     * creates messages that can now be deleted.      
     **************************************************/
  private void testDeleteMsg() {
    testDeleteMsgQueue();
    testDeleteMsgSub();
  }
  
  private void testDeleteMsgQueue() {
    MOMCommands cmds = getMOMCommands("[joram:mom:deleteMsg queue]");
    if(cmds==null) { log("ERROR: Couldn't retrieve MOM Commands service.");return;}

    //Normal case:
    Queue queue = null;
    String[] msgIds = null;
    try {
      queue = Queue.create(SID, QUEUE_NAME);
      msgIds = queue.getMessageIds();
    } catch (Exception e) {
      log(e);
      return;
    }

    //Normal case:
    cmds.deleteMsg(new String[]{"queue",QUEUE_NAME,msgIds[0]});
    try {
      Thread.sleep(TEMPORIZER);
    } catch (InterruptedException e) {
      log("ERROR: [joram:mom:deleteMsg queue] Interrupted");
    }
    boolean found = true;
    try {
      queue.getMessage(msgIds[0]);
    } catch (ConnectException e) {
      log(e);
    } catch (AdminException e) {
      found = false;
    } catch (JMSException e) {
      log(e);
    } finally {
      assertFalse("[joram:mom:deleteMsg queue] The message has not been deleted.",
          found);
    }
    
    //Error case: The queue doesn't exist
    startStderrCapture();
    cmds.deleteMsg(new String[]{"queue",NON_EXISTENT_QUEUE_NAME,NON_EXISTENT_MSG_ID});
    String res = stopStderrCapture();
    assertTrue("[joram:mom:deleteMsg queue] No error when deleting a message from " +
    		    "a non-existent queue.",
        res.contains("Error: The queue \""+NON_EXISTENT_QUEUE_NAME+"\" does not " +
        		"exist."));
  }
  
  private void testDeleteMsgSub() {
    MOMCommands cmds = getMOMCommands("[joram:mom:deleteMsg queue]");
    if(cmds==null) { log("ERROR: Couldn't retrieve MOM Commands service.");return;}

    //Normal case:
    try {
      User user = User.create(USER_NAME, USER_PWD);
      String[] msgIds = user.getMessageIds(SUB_NAME);
      cmds.deleteMsg(new String[]{"subscription",USER_NAME,SUB_NAME,msgIds[0]});
      try {
        Thread.sleep(TEMPORIZER);
      } catch (InterruptedException e) {
        log("ERROR: [joram:mom:deleteMsg subscription] Interrupted");
      }
      boolean found = true;
      try {
        user.getMessage(SUB_NAME,msgIds[0]);
      } catch (ConnectException e) {
        log(e);
      } catch (AdminException e) {
        log(e);
        found = false;
      } catch (JMSException e) {
        log(e);
      } finally {
        assertFalse("[joram:mom:deleteMsg subscription] The message has not been " +
        		"deleted.", found);
      }
    } catch (Exception e) {
      log(e);
    }
    
    //Error case: The subscription doesn't exist
    startStderrCapture();
    cmds.deleteMsg(new String[]{"subscription",USER_NAME,NON_EXISTENT_SUB_NAME,NON_EXISTENT_MSG_ID});
    String res = stopStderrCapture();
    assertTrue("[joram:mom:deleteMsg subscription] No error when deleting a message from " +
            "a non-existent subscription.",
        res.contains("Error: The user \""+USER_NAME
            +"\" has no subscription of the name \""+NON_EXISTENT_SUB_NAME+"\""));
    
    //Error case: The user doesn't exist
    startStderrCapture();
    cmds.deleteMsg(new String[]{"subscription",NON_EXISTENT_USER,SUB_NAME,NON_EXISTENT_MSG_ID});
    res = stopStderrCapture();
    assertTrue("[joram:mom:deleteMsg subscription] No error when deleting a message from " +
            "a non-existent user's subscription.",
        res.contains("Error: The user \""+NON_EXISTENT_USER+"\" does not exist."));
  }

  /* clear
   *  a) On envoie plusieurs messages, on vérifie leur présence, on appelle « clear ».
   *     La destination doit être vide.
   *  b) Dans le cas des souscriptions, si l'utilisateur n'est pas trouvé, un
   *     message le précise.
   *  c) Si la destination n'existe pas, aucune erreur n'est affichée.
   */
  /**************************************************
   *                 WARNING:                       
   * This test must be called after testLsMsg which 
   * creates messages that can now be cleared.
   * NB: testDeleteMsg should be called before to
   *     have messages to delete.   
   **************************************************/
  private void testClear() {
    testClearQueue();
    testClearSubscription();
  }
  
  private void testClearQueue() {
    MOMCommands cmds = getMOMCommands("[joram:mom:clear queue]");
    if(cmds==null) { log("ERROR: Couldn't retrieve MOM Commands service.");return;}
   
    //Queue
    //Normal case
    Queue queue = null;
    int before,after;
    try {
      queue = Queue.create(SID, QUEUE_NAME);
      before = queue.getPendingMessages();
      if(before==0) {
        log("[joram:mom:clear queue] The queue has not pending messages, " +
            "clear can't be tested for normal case");
        return;
      }
      cmds.clear(new String[]{"queue",QUEUE_NAME});
      try {
        Thread.sleep(TEMPORIZER);
      } catch (InterruptedException e) {
        log("ERROR: [joram:mom:clear queue] Interrupted");
      }
      after = queue.getPendingMessages();
      assertTrue("[joram:mom:clear queue] The queue has not been cleared. Nb messages left: "+after
          ,after==0);
    } catch (Exception e) {
      log(e);
      return;
    }
    
    //Error case: the queue does not exist.
    startStderrCapture();
    cmds.clear(new String[]{"queue",NON_EXISTENT_QUEUE_NAME});
    String res = stopStderrCapture();
    assertTrue("[joram:mom:clear queue] No error when calling clear on a " +
    		"non-existent queue.", res.contains("Error: The queue "+
    		NON_EXISTENT_QUEUE_NAME+" does not exist."));
  }

  private void testClearSubscription() {
    MOMCommands cmds = getMOMCommands("[joram:mom:clear queue]");
    if(cmds==null) { log("ERROR: Couldn't retrieve MOM Commands service.");return;}

    //Normal case
    try {
      User user = User.create(USER_NAME, USER_PWD);
      int before = user.getSubscription(SUB_NAME).getMessageCount();
      if(before==0) {
        log("[joram:mom:clear subscription] The subscrition has not pending " +
        		"messages, clear can't be tested for normal case");
        return;
      }
//      log("Before clear, PMC = "+before);
      cmds.clear(new String[]{"subscription",USER_NAME,SUB_NAME});
      try {
        Thread.sleep(TEMPORIZER);
      } catch (InterruptedException e) {
        log("ERROR: [joram:mom:clear subscription] Interrupted");
      }
      int after = user.getSubscription(SUB_NAME).getMessageCount();
      assertTrue("[joram:mom:clear subscription] The subscription has not been " +
      		"cleared. Nb messages left: "+after, after==0);
    } catch (Exception e) {
      log(e);
      return;
    }
    
    //Error case: The user does not exist
    startStderrCapture();
    cmds.clear(new String[]{"subscription",NON_EXISTENT_USER,SUB_NAME});
    String res = stopStderrCapture();
    assertTrue("[joram:mom:clear subscription] No error when clearing a " +
    		"non-existent user's subscription.", 
    		res.contains("Error: The user \""+NON_EXISTENT_USER+"\" does not exist."));

    //Error case: The subscription does not exist
    startStderrCapture();
    cmds.clear(new String[]{"subscription",USER_NAME,NON_EXISTENT_SUB_NAME});
    res = stopStderrCapture();
    assertTrue("[joram:mom:clear subscription] No error when clearing a " +
    		"non-existent subscription.", res.contains("Error: The user \""+USER_NAME
        +"\" has no subscription of the name \""+NON_EXISTENT_SUB_NAME+"\""));
  }  
  
  
  /* delete
   *  a) Pour chaque item supprimé, l'accés à cet item échoue.
   *  b) Si l'item n'existe pas, un message d'erreur est affiché.
   */
  private void testDelete() {
    MOMCommands cmds = getMOMCommands("[joram:mom:delete]");
    if(cmds==null) { log("ERROR: Couldn't retrieve MOM Commands service.");return;}
    //System.err.println("Error: No "+category.toLowerCase()+" found.");
    
    //Queue
    //Normal case
    try {
      Queue.create(SID,QUEUE_NAME);
      cmds.delete(new String[]{"queue",QUEUE_NAME});
      try {
        Thread.sleep(TEMPORIZER);
      } catch (InterruptedException e) {
        log("ERROR: [joram:mom:delete queue] Interrupted");
      }
      assertFalse("[joram:mom:delete queue] The queue still exists.",
          AdminTopic.isDestinationTableContain(QUEUE_NAME));
    } catch (Exception e) {
      //TODO
      log(e);
      return;
    }
    
    //Error case: The queue does not exist
    startStderrCapture();
    cmds.delete(new String[]{"queue",NON_EXISTENT_QUEUE_NAME});
    String res = stopStderrCapture();
    assertTrue("[joram:mom:delete queue] No error when trying to delete a non-existent queue.",
        res.contains("Error: queue not found."));
    
    //Topic
    //Normal case
    try {
      Topic.create(SID,TOPIC_NAME);
      cmds.delete(new String[]{"topic",TOPIC_NAME});
      try {
        Thread.sleep(TEMPORIZER);
      } catch (InterruptedException e) {
        log("ERROR: [joram:mom:delete topic] Interrupted");
      }
      assertFalse("[joram:mom:delete topic] The topic still exists.",
          AdminTopic.isDestinationTableContain(TOPIC_NAME));
    } catch (Exception e) {
      //TODO
      log(e);
      return;
    }
    
    //Error case: The topic does not exist
    startStderrCapture();
    cmds.delete(new String[]{"topic",NON_EXISTENT_TOPIC_NAME});
    res = stopStderrCapture();
    assertTrue("[joram:mom:delete topic] No error when trying to delete a non-existent topic.",
        res.contains("Error: topic not found."));
    
    //User
    //Normal case
    try {
      User.create(USER2_NAME,USER2_PWD,SID);
      cmds.delete(new String[]{"user",USER2_NAME});
      try {
        Thread.sleep(TEMPORIZER);
      } catch (InterruptedException e) {
        log("ERROR: [joram:mom:delete user] Interrupted");
      }
      assertNull("[joram:mom:delete user] The user still exists.",
          AdminTopic.lookupUser(USER2_NAME));
    } catch (Exception e) {
      //TODO
      log(e);
      return;
    }
    
    //Error case: The user does not exist
    startStderrCapture();
    cmds.delete(new String[]{"user",NON_EXISTENT_USER});
    res = stopStderrCapture();
    assertTrue("[joram:mom:delete user] No error when trying to delete a non-existent user.",
        res.contains("Error: user not found."));
   }

  private void sendMessagesToQueue(String destName, int n) throws JMSException, ConnectException, AdminException {
    Connection cnx = cf.createConnection(USER_NAME,USER_PWD);
    Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    cnx.start();
    Destination dest = Queue.create(SID, destName);
    dest.setFreeWriting();
    MessageProducer producer = session.createProducer(dest);
    TextMessage message = session.createTextMessage();

    for (int i = 1; i <= n; i++) {
      message.setText("Test number " + i);
      producer.send(message);
    }
  }
  
  private void sendMessagesToTopic(String destName, int n) throws JMSException, ConnectException, AdminException {
    Connection cnx = cf.createConnection(USER_NAME,USER_PWD);
    Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    cnx.start();
    Destination dest = Topic.create(SID, destName);
    dest.setFreeWriting();
    MessageProducer producer = session.createProducer(dest);
    TextMessage message = session.createTextMessage();

    for (int i = 1; i <= n; i++) {
      message.setText("Test number " + i);
      producer.send(message);
    }
  }
  
  private void createSubscriber(Short sid, String topicName, String subName, String userName, String pwd)
      throws ConnectException, AdminException, JMSException {
 
    User user = User.create(userName, pwd);
    try {
      user.getSubscription(subName);
    } catch(AdminException e) {
      Topic topic = Topic.create(sid, topicName);
      topic.setFreeReading(); topic.setFreeWriting();
      TopicConnection cnx = cf.createTopicConnection(userName,pwd);
      cnx.setClientID("MOMTester");
      TopicSession session = cnx.createTopicSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
      session.createDurableSubscriber(topic,subName);
    }
  }
 
  private void startStdoutCapture() {
    if(prevStdout==null) {
      prevStdout = System.out;
      baos_out.reset();
      System.setOut(new PrintStream(baos_out));
    }
  }
  
  private int nbOut = 0;
  private String stopStdoutCapture() {
    System.setOut(prevStdout);
    prevStdout = null;
    if(LOG_STD)
      try {
        stdoutFile.write("Capture #"+nbOut++);
        stdoutFile.newLine();
        stdoutFile.write(baos_out.toString());
        stdoutFile.newLine();
      } catch (IOException e) {
        log(e);
      }
    return baos_out.toString();
  }
  
  private void startStderrCapture() {
    if(prevStderr==null) {
      prevStderr = System.err;
      baos_err.reset();
      System.setErr(new PrintStream(baos_err));
    }
  }
  
  private int nbErr = 0;
  private String stopStderrCapture() {
    System.setErr(prevStderr);
    prevStderr = null;
    if(LOG_STD)
      try {
        stderrFile.write("Capture #"+nbErr++);
        stderrFile.newLine();
        stderrFile.write(baos_err.toString());
        stderrFile.newLine();
      } catch (IOException e) {
        log(e);
      }
    return baos_err.toString();
  }
 
  private boolean hasMatchingLine(String input, String regex) {
//    log("hasMatchingLine("+input+','+regex+')');
    Pattern pattern = Pattern.compile(regex);
    java.util.regex.Matcher matcher = pattern.matcher(input);
    return matcher.find();
  }
  
  private int countMatchingLines(String input, String regex) {
//    log("countMatchingLine("+input+','+regex+')');
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);
    int count = 0;
    while(matcher.find())
      count++;
    return count;

  }

  private void log(String msg) {
    try {
      logger.write(msg);
      logger.newLine();
      logger.flush();
    } catch (IOException e) {
    }
  }
  
  private void log(Exception exc) {
    try {
      logger.write(exc.getClass().getCanonicalName() +": "+exc.getMessage());
      logger.newLine();
      for(StackTraceElement ste : exc.getStackTrace()) {
        logger.write("\t"+ste.toString());
        logger.newLine();
        logger.flush();
      }
    } catch (IOException e) {
    }
  }
  
}
