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
/ * Administers an agent server for the classic samples.
 */
 /** Ne pas oublier de lancer le serveur joram avec la commande ant single_server pour que le SimpleAdmin puisse se connecter*/

public class SimpleAdmin {

  public static void main(String[] args) throws ConnectException, AdminException,Exception {
    
    System.out.println();
    System.out.println("Launch of Simple administration...");

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);//on crée la connectionFactory
    AdminModule.connect(cf, "root", "root");//il faut etre administrateur pour crée les files d'attente et les sujets
    //Queue queueRequete = Queue.create("queueReq");
    //Queue queueReponse = Queue.create("queueRep");
    //Topic topic = Topic.create("topic");
    Queue qToto = Queue.create("Qtoto");
	
    
    User.create("anonymous", "anonymous");
    // On peut accèder aux deux queues et au topic en lecture et en ecriture
   /* queueRequete.setFreeReading();
    queueReponse.setFreeReading();
    topic.setFreeReading();
    queueRequete.setFreeWriting();
    queueReponse.setFreeWriting();
    topic.setFreeWriting();*/
    qToto.setFreeReading();
    qToto.setFreeWriting();

    //On enregistre les 2 queues et le topic ainsi que les connextionFactory dans la JNDI
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("ConnectionFactory", cf);
    //jndiCtx.bind("Qtoto",qToto);
   // jndiCtx.bind("QRequete", queueRequete);
   //jndiCtx.bind("QReponse", queueReponse);
   // jndiCtx.bind("Topic", topic);
    
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
