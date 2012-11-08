/**
 *
 * 
 *
 */
package joram.pb;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

public class HaTestPb  {

  public static void main(String args[]) {
    try{
      /* Admin.main(new String[]{""});

	    Hashtable env = new Hashtable();
	    env.put("scn.naming.provider.url","hascn://localhost:16400,localhost:16401");
	    env.put("java.naming.factory.initial","fr.dyade.aaa.jndi2.haclient.HANamingContextFactory");	
	    Context jndiCtx = new InitialContext(env);

	    TopicConnectionFactory tcf;
	    tcf = (TopicConnectionFactory)jndiCtx.lookup("TopicConnectionFactory");
       */

      String adminLogin = "root";
      String adminPassword = "root";
      String userLogin = "zz";
      String userPassword = "zz";
      int nb =5;

      ConnectionFactory cf = (ConnectionFactory) HATcpConnectionFactory.create("hajoram://localhost:16010,localhost:16011,localhost:16012");
      cf.getParameters().connectingTimer = 30;

      AdminModule.connect(cf, adminLogin, adminPassword);

      User.create(userLogin, userPassword);

      // create topic
      Topic topic[] = new Topic[nb];
      for (int i=0; i<nb; i++) {
        topic[i] =  Topic.create(0);
        topic[i].setFreeReading();
        topic[i].setFreeWriting();
      }

      AdminModule.disconnect();

      //  jndiCtx.close();

      TopicConnection connection = cf.createTopicConnection("root","root");

      connection.start();
      TopicSession session = connection.createTopicSession(false, 
                                                           Session.AUTO_ACKNOWLEDGE);


      TopicPublisher publisher = session.createPublisher((Topic)null);
      TopicSubscriber subscriber[] =new TopicSubscriber[nb];

      TopicConnection cnx[] = new TopicConnection[nb];
      TopicSession session1[]= new TopicSession[nb];
      // create connection, session of subscriber
      for (int i=0; i<nb; i++) {
        cnx[i] = cf.createTopicConnection("root","root");
        session1[i] = cnx[i].createTopicSession(false,
                                                Session.AUTO_ACKNOWLEDGE);
        subscriber[i] = session1[i].createSubscriber(topic[i]);
        System.out.println(subscriber[i]);
        subscriber[i].setMessageListener(new MsgListener("sub"+i));
        cnx[i].start();
      }

      // publisher send message to topic
      System.out.println("Publishes messages on topic...");
      TextMessage message =null; 
      for(int j=0;j<20;j++){
        for(int i=0;i<nb;i++){
          message = session.createTextMessage();
          message.setText("message"+i+"#"+j);
          publisher.send(topic[i],message);
        }
        System.out.println("message "+j+" sent");
        Thread.sleep(5000);
      }

    } catch (Exception exc) {
      exc.printStackTrace();
      // error(exc);
    }	
  }
}

//       echo "kill serv 1" >> log.txt

//       echo "kill serv 2" >> log.txt

//       echo "kill serv 3" >> log.txt


/*
 class  Admin{
    public static void main(String[] args) throws Exception {
	String adminLogin = "root";
	String adminPassword = "root";
	String userLogin = "zz";
	String userPassword = "zz";
	
	javax.jms.TopicConnectionFactory tcf = TopicHATcpConnectionFactory.create("hajoram://localhost:16010,localhost:16011");
       	((ConnectionFactory) tcf).getParameters().connectingTimer = 30;
	
	AdminModule.connect(tcf, adminLogin, adminPassword);
	
	Hashtable env = new Hashtable();
	env.put("scn.naming.provider.url","hascn://localhost:16400,localhost:16401");
	env.put("java.naming.factory.initial","fr.dyade.aaa.jndi2.haclient.HANamingContextFactory");	
	Context jndiCtx = new InitialContext(env);

	jndiCtx.bind("TopicConnectionFactory", tcf);
	
	User.create(userLogin, userPassword);
	
	Topic connTest = null;
	for(int i=0;i<5;i++){
	    connTest = Topic.create(0);
	    connTest.setFreeReading();
	    connTest.setFreeWriting();
	    jndiCtx.bind("Topic"+i, connTest);
	} 
	jndiCtx.close();
	AdminModule.disconnect();
    }
    
    
}
*/



class Server3 {
    
    public static void main(String[] args) {
	try {
	    AgentServer.main(new String[]{"0","./s0","2"});
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}




class Server2 {
    
    public static void main(String[] args) {
	try {
	    AgentServer.main(new String[]{"0","./s0","1"});
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

class Server1 {
    
    public static void main(String[] args) {
	
	try {
	    AgentServer.main(new String[]{"0","./s0","0"});
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}




 class MsgListener implements MessageListener
{
  String ident = null;

  public MsgListener()
  {}

  public MsgListener(String ident)
  {
    this.ident = ident;
    System.out.println(ident);
  }

  public void onMessage(Message msg)
  {
    try {
      if (msg instanceof TextMessage) {
        if (ident == null) 
          System.out.println(((TextMessage) msg).getText());
        else
          System.out.println(ident + ": " + ((TextMessage) msg).getText()
			     +"\t source : " + ((TextMessage) msg).getJMSDestination()
			     +"\t redelivred ? : " + ((TextMessage) msg).getJMSRedelivered());
      }
     
    }catch (JMSException jE) {
      System.err.println("Exception in listener: " + jE);
    }
  }
}
