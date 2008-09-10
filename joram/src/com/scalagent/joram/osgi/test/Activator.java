package com.scalagent.joram.osgi.test;

import java.io.*;

import org.osgi.framework.*;

import javax.jms.*;
import javax.naming.*;

import com.scalagent.joram.osgi.client.service.JoramClient;

public class Activator implements BundleActivator {

  private static BundleContext bcontext;

  public static BundleContext getBundleContext() {
    return bcontext;
  }

  ServiceReference ref = null;
  JoramClient jclient = null;

  /**
   * Implements BundleActivator.start().
   *
   * @param context the framework context for the bundle.
   */
  public void start(BundleContext context) throws Exception {
    bcontext = context;

    ref = context.getServiceReference(JoramClient.class.getName());
    jclient = (JoramClient) context.getService(ref);

    Context ictx = jclient.getInitialContext();
    System.err.println("" + ictx.getClass());

    // Admin phase
    ClassLoader cl = getClass().getClassLoader();
//    Thread ct = Thread.currentThread();
//    ClassLoader cl = ct.getContextClassLoader();
    InputStream is = cl.getResourceAsStream("joramAdmin.xml");
    jclient.executeAdminXML(new InputStreamReader(is));

    Reference refQ = (Reference) ictx.lookup("queue");

    System.err.println("" + refQ.getClass());
    System.err.println("" + refQ.getFactoryClassName());
    System.err.println(refQ.toString());

//     Thread ct = Thread.currentThread();
//     ClassLoader cl = ct.getContextClassLoader();
//     ct.setContextClassLoader(ictx.getClass().getClassLoader());
//     Queue queue = (Queue) ictx.lookup("queue");
//     Topic topic = (Topic) ictx.lookup("topic");
//     ct.setContextClassLoader(cl);
//     ictx.close();

    ConnectionFactory cf = jclient.getTcpConnectionFactory("localhost", 16010);
    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
//     Queue queue = sess.createQueue("#0.0.1026");
    Queue queue = sess.createQueue((String) refQ.get("dest.name").getContent());

    TextMessage msg = sess.createTextMessage();
    MessageProducer producer = sess.createProducer(queue);
    msg.setText("Hello world");
    producer.send(msg);
    sess.close();
    cnx.close();
  }

  /**
   * Implements BundleActivator.stop().
   *
   * @param context the framework context for the bundle.
   */
  public void stop(BundleContext context) {
    context.ungetService(ref);
  }
}
