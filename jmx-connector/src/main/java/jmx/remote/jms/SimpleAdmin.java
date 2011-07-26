package jmx.remote.jms;
import java.net.ConnectException;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * the <b>Simple Admin </b> class  creates and registers  the connectionfactory in the Jndi.
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class SimpleAdmin {

  public static void main(String[] args) throws ConnectException, AdminException,Exception {
    System.out.println();
    System.out.println("Launch of Simple administration...");
    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);//on crée la connectionFactory
    AdminModule.connect(cf, "root", "root");//il faut etre administrateur pour crée les files d'attente et les sujets
	User.create("anonymous", "anonymous");
	//On enregistre la connextionFactory dans la JNDI
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("ConnectionFactory", cf);
    jndiCtx.close();
    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
