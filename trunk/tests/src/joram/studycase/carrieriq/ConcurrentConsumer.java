package joram.carrieriq;

import java.lang.reflect.Method;
import java.util.Vector;

import javax.jms.*;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * This class wrap a JMS MessageConsumer.
 */
public class ConcurrentConsumer {
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

  String durableSubName = null;

  public void setDurableSubName(String durableSubName) {
    this.durableSubName = durableSubName;
  }

  String selector = null;

  public void setSelector(String selector) {
    this.selector = selector;
  }

  ObjectMessageListener listener = null;

  public void setObjectMessageListener(ObjectMessageListener listener) {
    this.listener = listener;
  }

  int poolSize = 1;

  public void setPoolSize(int poolSize) {
    this.poolSize = poolSize;
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

  /** List of messages in processing */
  Vector msgs = null;
  /** Reference to the last messages when needed to be ack'ed */
  Message lasttoack = null;

  final static int CREATED = 0;
  final static int INITIALIZED = 1;
  final static int CONNECTED = 2;
  final static int RUNNING = 3;
  final static int CLOSED = 4;
  final static int STOPPED = 5;

  int status = CREATED;

  public synchronized void init() throws Exception {
    if (status != CREATED) return;

    AdminModule.connect(host, port, "root", "root", 60);
    dest = createDestination(destc, destn, 0);
    cf = TcpConnectionFactory.create(host, port);
    org.objectweb.joram.client.jms.FactoryParameters fp = ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters();
    fp.cnxPendingTimer = 250;
    fp.connectingTimer = 2;
    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

    msgs = new Vector();
    lasttoack = null;

    status = INITIALIZED;

    connect();

    for (int i=0; i<poolSize; i++) {
      new Thread(new HandleMessageTask(this, listener)).start();
    }

    status = RUNNING;
  }

  synchronized void connect() throws JMSException {
    cnx = cf.createConnection();
    sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    if (durableSubName == null) {
      cons = sess.createConsumer(dest, selector);
    } else if (dest instanceof javax.jms.Topic) {
      cons = sess.createDurableSubscriber((javax.jms.Topic) dest,
                                          durableSubName, selector, false);
    } else {
      throw new javax.jms.IllegalStateException("Durable subscription implies the use of a Topic");
    }
    cnx.start();

    status = CONNECTED;
  }

  long timeout = 500L;

  synchronized Message receive() throws JMSException {
    // Waits only for the specified timeout to allow the acknowledge if
    // there is no more messages.
    Message msg = cons.receive(timeout);
    if (msg != null) {
      // adds the message to the list
      msgs.add(msg);
      // If any this message is no longer the last
      lasttoack = null;
    }
    return msg;
  }

  synchronized void acknowledge(Message msg) throws JMSException {
    if (msg == msgs.firstElement()) {
      if ((msgs.size() == 1) && (lasttoack != null)) {
        //  There is no more message in processing, but the last one is
        // not acknowledged. Acknowledge it!
        lasttoack.acknowledge();
      } else {
        msg.acknowledge();
      }
      msgs.removeElementAt(0);
    } else {
      if (msg == msgs.lastElement()) {
        lasttoack = msg;
      }
      msgs.remove(msg);
    }
  }

  public void cleanup() throws JMSException {
    cnx.close();
  }

  public static void main(String[] args) throws Exception {
    ConcurrentConsumer cons = new ConcurrentConsumer();

    String prop = System.getProperty("DestinationClass",
                                     "org.objectweb.joram.client.jms.Topic");
    cons.setDestinationClass(prop);

    prop = System.getProperty("DestinationName", "dest");
    cons.setDestinationName(prop);
    cons.setDurableSubName("sub1");
    cons.setPoolSize(4);

    ObjectMessageListenerImpl oml = new ObjectMessageListenerImpl();
    oml.setObjectListener(new ExamplePojo());
    cons.setObjectMessageListener(oml);

    cons.init();
  }
}

class HandleMessageTask implements Runnable {
  ObjectMessageListener oml;
  ConcurrentConsumer cons = null;

  HandleMessageTask(ConcurrentConsumer cons, ObjectMessageListener oml) {
    this.cons = cons;
    this.oml = oml;
  }

  public void run() {
    java.util.Random r = new java.util.Random();
    try {
      while (true) {
        Message msg = cons.receive();
        if (msg == null) continue;

        System.out.println(this.toString() + " handles " + msg);
        oml.onMessage(msg);
        try {
          Thread.sleep(r.nextInt(10));
        } catch (InterruptedException e) {}
        cons.acknowledge(msg);
      }
    } catch (JMSException exc) {
      System.out.println("Error handling message " + exc);
    }
  }
}
