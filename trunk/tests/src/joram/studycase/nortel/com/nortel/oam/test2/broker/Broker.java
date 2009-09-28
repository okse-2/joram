package com.nortel.oam.test2.broker;

import com.nortel.oam.test2.common.Props;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.conf.A3CML;
import fr.dyade.aaa.agent.conf.A3CMLServer;
import fr.dyade.aaa.agent.conf.A3CMLService;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;

public class Broker {
  private String adminLogin;
  private String adminPwd;
  private int proxyServicePort;
  private String localIpAdress;


  public static void main(String[] args) {
    new Broker().start();
  }

  private void start() {
    try {
      URL resource = getClass().getResource("/" + "a3servers.xml");
      InputStreamReader serverConfigReader = new InputStreamReader(resource.openStream());
      AgentServer.setConfig(A3CML.getConfig(serverConfigReader));
      serverConfigReader.close();
      AgentServer.init(Props.serverId, "", null);
      AgentServer.start();
      System.out.println("Broker started: OK");
      initAdminProperties();
      createResources();
      waitStopSignal();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void waitStopSignal() throws IOException {
    System.out.println("Press any key to stop.");
    System.in.read();
    AgentServer.stop();
  }

  private void initAdminProperties() throws Exception {
    A3CMLServer server = AgentServer.getConfig().getServer(Props.serverId);
    A3CMLService connectionManagerService = server.getService("org.objectweb.joram.mom.proxies.ConnectionManager");
    String[] loginInfos = connectionManagerService.args.split("\\s");
    adminLogin = loginInfos[0];
    adminPwd = loginInfos[1];
    A3CMLService tcpProxyService = server.getService("org.objectweb.joram.mom.proxies.tcp.TcpProxyService");
    proxyServicePort = Integer.parseInt(tcpProxyService.args);
    localIpAdress = InetAddress.getLocalHost().getHostAddress();
    AdminModule.connect(localIpAdress, proxyServicePort, adminLogin, adminPwd, Props.TIMEOUT);
    User.create("anonymous", "anonymous", Props.serverId);
    AdminModule.disconnect();
    System.out.println("Admin properties retrieved: OK");
  }

  private void createResources() throws Exception {
    Context jndiCtx = null;
    try {
      System.out.println("connectingTimer=" + Props.CONNECTING_TIMER + ", cnxPendingTimer=" + Props.CNX_PENDING_TIMER);
      AdminModule.connect(localIpAdress, proxyServicePort, adminLogin, adminPwd, Props.TIMEOUT);
      jndiCtx = new InitialContext();

      ConnectionFactory topicFactory =
              TopicTcpConnectionFactory.create(localIpAdress, proxyServicePort);
      FactoryParameters parameters = ((org.objectweb.joram.client.jms.ConnectionFactory) topicFactory).getParameters();
      parameters.connectingTimer = Props.CONNECTING_TIMER; // secs
      parameters.cnxPendingTimer = Props.CNX_PENDING_TIMER; //msecs
      parameters.txPendingTimer = 0; // secs

      try {
        jndiCtx.bind(Props.topicFactoryName, topicFactory);
      } catch (NameAlreadyBoundException e) {
      }
      System.out.println(Props.topicFactoryName + " created and bound in JNDI: OK");

      Topic mainTopic = Topic.create(Props.serverId, Props.mainTopicName);
      mainTopic.setFreeReading();
      mainTopic.setFreeWriting();
      try {
        jndiCtx.bind(Props.mainTopicName, mainTopic);
      } catch (NameAlreadyBoundException e) {
      }
      System.out.println(Props.mainTopicName + " created and bound in JNDI: OK");

      ConnectionFactory queueFactory = QueueTcpConnectionFactory.create(localIpAdress, proxyServicePort);
      parameters = ((org.objectweb.joram.client.jms.ConnectionFactory) queueFactory).getParameters();
      parameters.connectingTimer = Props.CONNECTING_TIMER; // secs
      parameters.cnxPendingTimer = Props.CNX_PENDING_TIMER; //msecs
      parameters.txPendingTimer = 0; // secs

      try {
        jndiCtx.bind(Props.queueFactoryName, queueFactory);
      } catch (NameAlreadyBoundException e) {
      }
      System.out.println(Props.queueFactoryName + " created and bound in JNDI: OK");

      Queue requestQueue = Queue.create(Props.serverId, Props.requestQueueName);
      requestQueue.setFreeReading();
      requestQueue.setFreeWriting();
      try {
        jndiCtx.bind(Props.requestQueueName, requestQueue);
      } catch (NameAlreadyBoundException e) {
      }
      System.out.println(Props.requestQueueName + " created and bound in JNDI: OK");
    } finally {
      if (jndiCtx != null) {
        jndiCtx.close();
      }
      AdminModule.disconnect();
    }
  }
}
