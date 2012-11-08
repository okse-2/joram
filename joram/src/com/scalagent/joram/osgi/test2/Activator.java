package com.scalagent.joram.osgi.test2;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

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

    jclient.connect("localhost", 16010, "root", "root", 10);
    ConnectionFactory cf = jclient.getTcpConnectionFactory("localhost", 16010);
    jclient.createUser("anonymous", "anonymous");
    Queue queue = jclient.createQueue("queue");
    jclient.disconnect();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
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
