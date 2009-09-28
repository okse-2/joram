package com.nortel.oam.test1.broker;

import com.nortel.oam.test1.common.Props;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.conf.A3CML;
import fr.dyade.aaa.agent.conf.A3CMLServer;
import fr.dyade.aaa.agent.conf.A3CMLService;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
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
  private static final int TIMEOUT = 5;

  private String adminLogin;
  private String adminPwd;
  private int proxyServicePort;
  private String localIpAdress;
  private static final int CONNECTING_TIMER = 90; // sec
  private static final int CNX_PENDING_TIMER = 500; // msec

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
    AdminModule.connect(localIpAdress, proxyServicePort, adminLogin, adminPwd, TIMEOUT);
    User.create("anonymous", "anonymous", Props.serverId);
    AdminModule.disconnect();
    System.out.println("Admin properties retrieved: OK");
  }

  private void createResources() throws Exception {
    Context jndiCtx = null;
    try {
      AdminModule.connect(localIpAdress, proxyServicePort, adminLogin, adminPwd, TIMEOUT);
      jndiCtx = new InitialContext();

      ConnectionFactory mainFactory = TopicTcpConnectionFactory.create(localIpAdress, proxyServicePort);
      FactoryParameters topic_parameters = ((org.objectweb.joram.client.jms.ConnectionFactory) mainFactory).getParameters();
      topic_parameters.connectingTimer = CONNECTING_TIMER;
      topic_parameters.cnxPendingTimer = CNX_PENDING_TIMER;

      try {
        jndiCtx.bind(Props.mainTopicFactoryName, mainFactory);
      } catch (NameAlreadyBoundException e) {
      }
      System.out.println(Props.mainTopicFactoryName + " created and bound in JNDI: OK");
      Topic mainTopic = Topic.create(Props.serverId, Props.mainTopicName);
      mainTopic.setFreeReading();
      mainTopic.setFreeWriting();
      try {
        jndiCtx.bind(Props.mainTopicName, mainTopic);
      } catch (NameAlreadyBoundException e) {
      }
      System.out.println(Props.mainTopicName + " created and bound in JNDI: OK");
    } finally {
      if (jndiCtx != null) {
        jndiCtx.close();
      }
      AdminModule.disconnect();
    }
  }
}
