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
public class Converter {
  String incomingDestClass = null;

  public void setIncomingDestClass(String incomingDestClass) {
    this.incomingDestClass = incomingDestClass;
  }

  String incomingDestName = null;

  public void setIncomingDestName(String incomingDestName) {
    this.incomingDestName = incomingDestName;
  }

  String outgoingDestClass = null;

  public void setOutgoingDestClass(String outgoingDestClass) {
    this.outgoingDestClass = outgoingDestClass;
  }

  String outgoingDestName = null;

  public void setOutgoingDestName(String outgoingDestName) {
    this.outgoingDestName = outgoingDestName;
  }

  ConverterMessageListenerImpl listener = null;

  public void setConverterMessageListenerImpl(ConverterMessageListenerImpl listener) {
    this.listener = listener;
  }

  public void init() throws Exception {
    Producer prod = new Producer();
    prod.setDestinationClass(outgoingDestClass);
    prod.setDestinationName(outgoingDestName);
    MessageConverterImpl mc = new MessageConverterImpl();
    prod.setMessageConverter(mc);
    prod.init();

    Consumer cons = new Consumer();
    cons.setDestinationClass(incomingDestClass);
    cons.setDestinationName(incomingDestName);
    ConverterMessageListenerImpl cml = new ConverterMessageListenerImpl();
    cml.setObjectListener(new ExamplePojo());
    cml.setMessageProducer(prod);
    cons.setObjectMessageListener(cml);
    cons.init();
  }

  public void cleanup() throws JMSException {
  }

  public static void main(String[] args) throws Exception {
    Converter conv = new Converter();
    String prop = System.getProperty("IncomingDestClass",
                                     "org.objectweb.joram.client.jms.Queue");
    conv.setIncomingDestClass(prop);
    prop = System.getProperty("IncomingDestName", "queue");
    conv.setIncomingDestName(prop);
    prop = System.getProperty("OutgoingDestClass",
                                     "org.objectweb.joram.client.jms.Topic");
    conv.setOutgoingDestClass(prop);
    prop = System.getProperty("OutgoingDestName", "topic");
    conv.setOutgoingDestName(prop);

    conv.init();
  }
}
