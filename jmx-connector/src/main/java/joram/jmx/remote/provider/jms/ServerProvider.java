package joram.jmx.remote.provider.jms;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import jmx.remote.jms.JmsJmxConnectorServer;
import jmx.remote.jms.tests.*;

public class ServerProvider implements JMXConnectorServerProvider {
  /***
   * Create a JmsJmxConnectorServer
   * 
   * @param url
   * @param environment
   * @param server
   * @return the JmsJmxConnectorServer
   * @throws IOException
   * 
   * @author Djamel-Eddine Boumchedda
   */

  public JMXConnectorServer newJMXConnectorServer(JMXServiceURL url, Map environment, MBeanServer server)
      throws IOException {
    String protocol = url.getProtocol();
    if (!"jms".equals(protocol))
      throw new MalformedURLException("Wrong protocol " + protocol + " for provider" + this);
    return new JmsJmxConnectorServer(url, environment, server);
  }
}
