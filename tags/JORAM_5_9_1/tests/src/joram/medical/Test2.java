package joram.medical;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.ConnectException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Session;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;

public class Test2 extends TestCase {
  public static void main(String[] args) throws FileNotFoundException {
    new Test2().run();
  }

  public void run()  {
    try {
      short sid = 0;
      String host = "localhost";
      String adminuid = "root";
      String adminpwd = "root";
      int joram = 16010;
      int jndi = 16400;

      createAndStartJoramServer(sid, host, adminuid, adminpwd, joram, jndi);
      JMSContext jctx = createContext(adminuid, adminpwd, host, joram, Session.AUTO_ACKNOWLEDGE);
      jctx.setAutoStart(false);
      Destination queue = createDestination(jctx, "queue:myQueue");
      
      JMSProducer producer = jctx.createProducer();
      JMSConsumer consumer = jctx.createConsumer(queue);
      jctx.start();

      producer.send(queue, "coucou");
      String msg = consumer.receiveBody(String.class);
      System.out.println("receives: " + msg);
      
      stopJoramServer(host, joram, adminuid, adminpwd);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      endTest();     
    }
  }
  
  public final static String CFG_ADMINUID_PROPERTY = "fr.dyade.aaa.agent.A3CONF_ADMINUID";
  public final static String CFG_ADMINPWD_PROPERTY = "fr.dyade.aaa.agent.A3CONF_ADMINPWD";
  public final static String CFG_JMS_PORT_PROPERTY = "fr.dyade.aaa.agent.A3CONF_JMS_PORT";
  public final static String CFG_JNDI_PORT_PROPERTY = "fr.dyade.aaa.agent.A3CONF_JNDI_PORT";

  public void createAndStartJoramServer(short sid,
                                        String host,
                                        String adminuid, String adminpwd,
                                        int joram, int jndi) throws Exception {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append(
                  "<property name=\"Transaction\" value=\"fr.dyade.aaa.ext.NGTransaction\"/>\n" +
                  "<server id=\"").append(sid).append("\" name=\"S").append(sid).append("\" hostname=\"").append(host).append("\">\n" +
                  "<service class=\"org.objectweb.joram.mom.proxies.ConnectionManager\" args=\"").append(adminuid).append(' ').append(adminpwd).append("\"/>\n" +
                  "<service class=\"org.objectweb.joram.mom.proxies.tcp.TcpProxyService\" args=\"").append(joram).append("\"/>\n" +
                  "<service class=\"fr.dyade.aaa.jndi2.server.JndiServer\" args=\"").append(jndi).append("\"/>\n" +
                  "</server>\n" +
                  "</config>\n");
    PrintWriter pw = new PrintWriter(new File("a3servers.xml"));
    pw.println(strbuf.toString());
    pw.flush();
    pw.close();
    
//    pw = new PrintWriter(new File("a3debug.cfg"));
//    pw.println("log.config.classname org.objectweb.util.monolog.wrapper.javaLog.LoggerFactory");
//    pw.println("handler.logf.type RollingFile");
//    pw.println("handler.logf.output server.log");
//    pw.println("handler.logf.pattern  %l %h %d, %m%n");
//    pw.println("handler.logf.fileNumber 2");
//    pw.println("handler.logf.maxSize 1000000");
//    pw.println("logger.root.handler.0 logf");
//    pw.println("logger.root.level ERROR");
//    pw.flush();
//    pw.close();
    
    AgentServer.init((short) sid, 
                     new File("s" + sid).getPath(), 
                     null);
    AgentServer.start();
  }
  
  public void stopJoramServer(String host, int port,
                              String adminuid, String adminpwd) throws ConnectException, AdminException {
    ConnectionFactory cf = TcpConnectionFactory.create(host, port);
    AdminModule.connect(cf, adminuid, adminpwd);
    AdminModule.stopServer();
  }

  public static JMSContext createContext(String user, String pass,
                                         String hostname, int port,
                                         int mode) throws JMSException, JMSRuntimeException {
    ConnectionFactory joramCF = null;
    joramCF = TcpConnectionFactory.create(hostname,port); 
    
    // resolving classloader issues
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    try {
      // returns the a new JMS context
      return joramCF.createContext(user, pass, mode);
    } finally {
      Thread.currentThread().setContextClassLoader(classloader);
    }
  }
  
  protected static final String QUEUE = "queue";
  protected static final String TOPIC = "topic";
  
  public static Destination createDestination(JMSContext jctx, String dname) {
    Destination dest = null;
    
    String type = TOPIC;
    int idx = dname.indexOf(':');
    if (idx >= 0) {
      type = dname.substring(0, idx);
      dname = dname.substring(idx+1);
    }
    
    if (QUEUE.equals(type)) {
      dest = jctx.createQueue(dname);
    } else {
      dest = jctx.createTopic(dname);
    }
    
    return dest;
  }
}
