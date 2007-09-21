/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package ha;

import framework.TestCase;

import java.io.*;
import java.util.*;
import javax.jms.*;

import org.objectweb.joram.client.jms.admin.AdminModule;

public class HATest extends TestCase {

    public static final int MESSAGE_NUMBER = 20;

    public HATest() {
	super();
    }

    public void run() {
	Process p0 = null;
	Process p1 = null;
	Process p2 = null;
	try {
	    File r0 = new File("r0");
	    r0.mkdir();
      
	    File r1 = new File("r1");
	    r1.mkdir();
      
	    File r2 = new File("r2");
	    r2.mkdir();

	    try {
		Thread.sleep(2000);
	    } catch (InterruptedException exc) {}

	    System.out.println("Start the replica 0");
	    p0 = startAgentServer(
				  (short)0, r0, 
				  new String[]{"-DnbClusterExpected=2", 
					       "-DTransaction=fr.dyade.aaa.util.NullTransaction",
					       "-D" + fr.dyade.aaa.util.Debug.DEBUG_DIR_PROPERTY + "=.."},
				  new String[]{"0"});
      
	    try {
  	  	Thread.sleep(2000);
	    } catch (InterruptedException exc) {}

	    System.out.println("Start the replica 1");
	    p1 = startAgentServer(
				  (short)0, r1, 
				  new String[]{"-DnbClusterExpected=2", 
					       "-DTransaction=fr.dyade.aaa.util.NullTransaction",
					       /* "-Dcom.sun.management.jmxremote",*/
					       /* "-DMXServer=com.scalagent.jmx.JMXServer",*/
					       "-D" + fr.dyade.aaa.util.Debug.DEBUG_DIR_PROPERTY + "=.."},
				  new String[]{"1"});
     

	    System.out.println("Start the replica 2");
	    p2 = startAgentServer(
				  (short)0, r2, 
				  new String[]{"-DnbClusterExpected=2", 
					       "-DTransaction=fr.dyade.aaa.util.NullTransaction",
					       "-D" + fr.dyade.aaa.util.Debug.DEBUG_DIR_PROPERTY + "=.."},
				  new String[]{"2"});

	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException exc) {}

	     
	    AdminModule.connect("localhost", 2560,
				"root", "root", 60);

	    org.objectweb.joram.client.jms.admin.User user = 
		org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 0);

	    ConnectionFactory cf = 
		org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory.create("hajoram://localhost:2560,localhost:2561,localhost:2562");
	    ((org.objectweb.joram.client.jms.ConnectionFactory)cf).getParameters().cnxPendingTimer = 500;
	    ((org.objectweb.joram.client.jms.ConnectionFactory)cf).getParameters().connectingTimer = 30;

	    org.objectweb.joram.client.jms.Queue queue = 
		org.objectweb.joram.client.jms.Queue.create(0);
	    queue.setFreeReading();
	    queue.setFreeWriting();

	    AdminModule.disconnect();
     

	    Connection cnx = cf.createConnection("anonymous", "anonymous");
	    Session session = cnx.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = session.createProducer(queue);
	    MessageConsumer consumer = session.createConsumer(queue);
	    cnx.start();


	    new Killer(p0, 0, 500 * (MESSAGE_NUMBER / 2)).start();

	    for (int i = 0; i < MESSAGE_NUMBER; i++) {	
		TextMessage msg = session.createTextMessage();
		msg.setText("Test number1 " + i);
		producer.send(msg);

		try {
		    Thread.sleep(500);
		} catch (InterruptedException exc) {}
	    }

	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException exc) {}
	    
	    new Killer(p1, 1, 500 * (MESSAGE_NUMBER / 2)).start();
	    
	    System.out.println("Restart the replica 0");
	    p0 = startAgentServer(
				  (short)0, r0, 
				  new String[]{"-DnbClusterExpected=2", 
					       "-DTransaction=fr.dyade.aaa.util.NullTransaction",
					       "-D" + fr.dyade.aaa.util.Debug.DEBUG_DIR_PROPERTY + "=.."},
				  new String[]{"0"});

      
	    for (int i = 0; i < MESSAGE_NUMBER; i++) {
		TextMessage msg = (TextMessage)consumer.receive();
		assertTrue(msg.getText().startsWith("Test number1"));
		try {
		    Thread.sleep(500);
		} catch (InterruptedException exc) {}
	    }

	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException exc) {}

	    for (int i = 0; i < MESSAGE_NUMBER; i++) {	
		TextMessage msg = session.createTextMessage();
		msg.setText("Test number2 " + i);
		producer.send(msg);
	    }

	    new Killer(p2, 2, 500 * (MESSAGE_NUMBER / 2)).start();

     
	    System.out.println("Start the replica 1");
	    p1 = startAgentServer(
				  (short)0, r1, 
				  new String[]{"-DnbClusterExpected=2", 
					       "-DTransaction=fr.dyade.aaa.util.NullTransaction",
					       /* "-Dcom.sun.management.jmxremote",*/
					       /* "-DMXServer=com.scalagent.jmx.JMXServer",*/
					       "-D" + fr.dyade.aaa.util.Debug.DEBUG_DIR_PROPERTY + "=.."},
				  new String[]{"1"});


	    for (int i = 0; i < MESSAGE_NUMBER; i++) {
		TextMessage msg = (TextMessage)consumer.receive();
		assertTrue(msg.getText().startsWith("Test number2"));
		try {
		    Thread.sleep(500);
		} catch (InterruptedException exc) {}
	    }
	} catch (Exception exc) {
	    exc.printStackTrace();
	    error(exc);
	} finally {
	    p0.destroy();
	    p1.destroy();
	    p2.destroy();
	    endTest();     
	}
    }

    public static Process startAgentServer(short sid,
					   File dir,
					   String[] jvmarg,
					   String[] servarg) throws Exception {
	return startAgentServer(sid, dir, jvmarg, servarg, "fr.dyade.aaa.agent.AgentServer");
    }

    public static Process startAgentServer(short sid,
					   File dir,
					   String[] jvmarg,
					   String[] servarg,
					   String serverClassName) throws Exception {
	String javapath = 
	    new File(new File(System.getProperty("java.home"), "bin"),
		     "java").getPath();
	String classpath = System.getProperty("java.class.path");

	Vector argv = new Vector();
	argv.addElement(javapath);
	argv.addElement("-classpath");
	argv.addElement(classpath);
	if (jvmarg != null) {
	    for (int i=0; i<jvmarg.length; i++)
		argv.addElement(jvmarg[i]);
	}
	argv.addElement(serverClassName);
	argv.addElement(Short.toString(sid));
	argv.addElement("s" + sid);
	if (servarg != null) {
	    for (int i=0; i<servarg.length; i++)
		argv.addElement(servarg[i]);
	}

	String[] command = new String[argv.size()];
	argv.copyInto(command);

	Process p;
	if (dir == null) {
	    p = Runtime.getRuntime().exec(command);
	} else {
	    p = Runtime.getRuntime().exec(command, null, dir);
	}
    
	// Close all streams of subprocess in order to avoid deadlock due
	// to limited buffer size.
	try {
	    p.getInputStream().close();
	} catch (Exception exc) {}
	try {
	    p.getOutputStream().close();
	} catch (Exception exc) {}
	try {
	    p.getErrorStream().close();
	} catch (Exception exc) {}
	return p;
    }

    public static void main(String args[]) {
	new HATest().run();
    }

    public static class Killer extends Thread {    
	private Process process;
	private int index;
	private long pause;

	Killer(Process process,
	       int index,
	       long pause) {      
	    this.process = process;
	    this.index = index;
	    this.pause = pause;
	}

	public void run() {
	    try {
		Thread.sleep(pause);
	    } catch (InterruptedException exc) {}
	    System.out.println("Kill replica " + index);
	    process.destroy();
	}
    }
}
