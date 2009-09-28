package joram.carrieriq;

import java.lang.reflect.Method;

import javax.jms.*;

import fr.dyade.aaa.agent.AgentServer;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

class Broker {
  static String host = "localhost";
  static int port = 16010;

  static String destc = null;

  static String destn = null;

  static Destination createDestination(String classname,
                                       String name,
                                       int sid) throws Exception {
    Class c = Class.forName(classname);
    Method m = c.getMethod("create",
                           new Class[]{int.class,
                                       java.lang.String.class});
    return (Destination) m.invoke(null, new Object[]{new Integer(sid), name});
  }

  public static void main(String[] args) throws Exception {
    AgentServer.init((short) 0, "./s0", null);
    AgentServer.start();

    Thread.sleep(1000L);

    destc = System.getProperty("DestinationClass",
                               "org.objectweb.joram.client.jms.Topic");
    destn = System.getProperty("DestinationName", "dest");

    AdminModule.connect(host, port, "root", "root", 60);

    Destination dest = createDestination(destc, destn, 0);
    User user = User.create("anonymous", "anonymous", 0);

    dest.setFreeReading();
    dest.setFreeWriting();

    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
  }
}
