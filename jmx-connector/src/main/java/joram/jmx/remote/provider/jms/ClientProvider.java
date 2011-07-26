package joram.jmx.remote.provider.jms;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXServiceURL;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.Map;
import jmx.remote.jms.JmsJmxConnector;

/***
 * Construct a new JMXConnector
 * 
 * @param url
 * @param environment
 * @return the new Connector
 * @throws IOException
 * 
 * @author Djamel-Eddine Boumchedda
 */
public class ClientProvider implements JMXConnectorProvider {

  public JMXConnector newJMXConnector(JMXServiceURL url, Map environment) throws IOException {
    String protocol = url.getProtocol();
    if (!"jms".equals(protocol))
      throw new MalformedURLException("Wrong protocol " + protocol + " for provider " + this);
    System.out.println("Je suis la -> Dans le CLient Provider!!!!!!!!!!!!!!!!!!");
    File f = new File("trace-Client.txt");
    PrintStream pS = new PrintStream(f);
    Exception e = new Exception();
    e.printStackTrace(pS);

    JMXConnector result = new JmsJmxConnector(environment, url);
    return result;
  }
}
