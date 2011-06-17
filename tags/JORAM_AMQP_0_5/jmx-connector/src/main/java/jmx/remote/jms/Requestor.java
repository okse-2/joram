package jmx.remote.jms;

import java.awt.List;
import java.io.IOException;
import java.net.ConnectException;
import java.util.LinkedList;
import javax.jms.*;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.TemporaryQueue;
import org.objectweb.joram.client.jms.admin.AdminException;


public class Requestor {
ClientJMS clientJms;

public Requestor(){
	try {
		clientJms = new ClientJMS();
	} catch (ConnectException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NamingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (JMSException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (AdminException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
	

}
