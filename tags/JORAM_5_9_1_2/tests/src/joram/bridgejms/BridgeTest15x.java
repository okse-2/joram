package joram.bridgejms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 *  Test the JMS bridge with a specific architecture using 2 separate flows with distribution
 * and acquisition destinations (see the architecture description in Test15x-Archi.jpg).
 */
public class BridgeTest15x extends TestCase {
  public static void main(String[] args) {
    new BridgeTest15x().run();
  }

  static final int ROUND = 10000;
  static final int bigsize = 1000000;
  
  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)2, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(5000);

      AdminTest15x admin = new AdminTest15x();

      javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
      admin.centralAdmin(cf, "A:B");

      javax.jms.ConnectionFactory cfA = TcpConnectionFactory.create("localhost", 16011);
      admin.localAdmin(cfA, "A");

      javax.jms.ConnectionFactory cfB = TcpConnectionFactory.create("localhost", 16012);
      admin.localAdmin(cfB, "B");

      Connection cnx = cf.createConnection();
      Forward fwdA = new Forward(cnx, "A");
      Forward fwdB = new Forward(cnx, "B");
      cnx.start();
      
      Connection cnxA = cfA.createConnection();
      Receiver recvA = new Receiver("A", cnxA);
      Sender sndA = new Sender("A", cnxA);
      cnxA.start();
      
      Connection cnxB = cfB.createConnection();
      Receiver recvB = new Receiver("B", cnxB);
      Sender sndB = new Sender("B", cnxB);
      cnxB.start();
      
      for (int i=0; i<ROUND; i++) {
      	if ((i%100) == 99) {
      		sndA.sendbig();
      		sndB.sendbig();
      	} else {
      		sndA.send("A#" + i);
      		sndB.send("B#" + i);
      	}
        
        Thread.sleep(5, 0);
      }
      
      Thread.sleep(10000L);
      
      fwdA.close(); fwdB.close();
      recvA.close(); sndA.close();
      recvB.close(); sndB.close();
      
      cnx.close();
      cnxA.close();
      cnxB.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      killAgentServer((short)0);
      killAgentServer((short)1);
      killAgentServer((short)2);
      endTest(); 
    }
  }
}

class Forward implements MessageListener {
  Connection cnx = null;
  Session session = null;
  MessageConsumer cons = null;
  MessageProducer prod = null;
  Queue queue1, queue2;

  Forward(Connection cnx, String client) throws JMSException {
    this.cnx = cnx;
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

    queue1 = session.createQueue("queue1" + client);
    queue2 = session.createQueue("queue2" + client);

    cons = session.createConsumer(queue1);
    prod = session.createProducer(queue2);
    cons.setMessageListener(this);
  }

  @Override
  public void onMessage(Message msg) {
    try {
//      System.out.println("Central receives: " + msg);
      prod.send(msg);
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
  
  public void close() throws JMSException {
    session.close();
  }
}

class Sender {
  String name = null;
  Connection cnx = null;
  Session session = null;
  MessageProducer prod = null;
  Queue queue1;
  
  Sender(String name, Connection cnx) throws JMSException {
    this.name = name;
    this.cnx = cnx;
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

    queue1 = session.createQueue("queue1");
    prod = session.createProducer(queue1);
  }
  
  void send(String text) throws JMSException {
    TextMessage msg = session.createTextMessage(text);
    prod.send(msg);
  }
  
  void sendbig() throws JMSException {
	  byte[] payload = new byte[BridgeTest15x.bigsize];
	  ObjectMessage msg = session.createObjectMessage();
	  msg.setObject(payload);
	  prod.send(msg);
  }
  
  public void close() throws JMSException {
    session.close();
  }
}

class Receiver implements MessageListener {
  String name = null;
  Connection cnx = null;
  Session session = null;
  MessageConsumer cons = null;
  Queue queue2;

  int cpt = 0;
  
  Receiver(String name, Connection cnx) throws JMSException {
    this.name = name + '#';
    this.cnx = cnx;
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

    queue2 = session.createQueue("queue2");

    cons = session.createConsumer(queue2);
    cons.setMessageListener(this);
  }

  @Override
  public void onMessage(Message m) {
  	try {
  		if (m instanceof TextMessage) {
  			TextMessage msg = (TextMessage) m;
  			String str = msg.getText();
  			TestCase.assertTrue("Should receive #" + cpt, str.equals(name + cpt));
  			if (! str.equals(name + cpt)) {
  				System.out.println(name + cpt + ") receives " + str);
  				cpt = Integer.parseInt(str.substring(name.length()));
  			}
  		} else {
  			ObjectMessage msg = (ObjectMessage) m;
  			byte[] payload = (byte[]) msg.getObject();
  			if (payload.length != BridgeTest15x.bigsize)
  				System.out.println(name + cpt + " receives BigObject: " + payload.length);
  			TestCase.assertTrue("Should receive - " + payload.length, (payload.length == BridgeTest15x.bigsize));
  		}
  		cpt++;
  		if ((cpt%1000)==0) System.out.println(name + cpt);
  	} catch (Exception e) {
  		e.printStackTrace();
  	}
  }
  
  public void close() throws JMSException {
    TestCase.assertTrue("Should receive " + BridgeTest15x.ROUND, cpt == BridgeTest15x.ROUND);
    session.close();
  }
}

