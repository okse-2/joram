package joram.carrieriq;

import java.lang.reflect.Method;

import javax.jms.*;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * This class wrap a JMS MessageConsumer.
 */
public class Consumer {
  String host = "localhost";
  int port = 16010;

  public void setHostname(String host) {
    this.host = host;
  }

  public void setPort(int port) {
    this.port = port;
  }

  String destc = null;

  public void setDestinationClass(String destc) {
    this.destc = destc;
  }

  String destn = null;

  public void setDestinationName(String destn) {
    this.destn = destn;
  }

  ObjectMessageListener listener = null;

  public void setObjectMessageListener(ObjectMessageListener listener) {
    this.listener = listener;
  }

  static Destination createDestination(String classname,
                                       String name,
                                       int sid) throws Exception {
    Class c = Class.forName(classname);
    Method m = c.getMethod("create",
                           new Class[]{int.class,
                                       java.lang.String.class});
    return (Destination) m.invoke(null, new Object[]{new Integer(sid), name});
  }

  ConnectionFactory cf = null;

  Connection cnx = null;
  Session sess = null;
  Destination dest = null;
  MessageConsumer cons = null;

  public void init() throws Exception {
    AdminModule.connect(host, port, "root", "root", 60);
    dest = createDestination(destc, destn, 0);
    cf = TcpConnectionFactory.create(host, port);
    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

    cnx = cf.createConnection();
    sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    cons = sess.createConsumer(dest);
    cons.setMessageListener(listener);
    cnx.start();
  }

  public void cleanup() throws JMSException {
    cnx.close();
  }

  public static void main(String[] args) throws Exception {
    Consumer c = new Consumer();

    String prop = System.getProperty("DestinationClass",
                                     "org.objectweb.joram.client.jms.Topic");
    c.setDestinationClass(prop);

    prop = System.getProperty("DestinationName", "dest");
    c.setDestinationName(prop);

    ObjectMessageListenerImpl oml = new ObjectMessageListenerImpl();
    oml.setObjectListener(new ExamplePojo());
    c.setObjectMessageListener(oml);

    c.init();
  }
}
