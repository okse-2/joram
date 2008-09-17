
package joram.pb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import javax.naming.Context;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class TempTopic extends joram.framework.TestCase {
    
   
    public static void main(String[] args) throws Exception {
	new TempTopic().run();
    }
    public void run() throws Exception{
	startAgentServer((short) 0);
        AdminModule.connect("root", "root", 60);
             
        Context initialContext = getInitialContext();
	{
            javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
            initialContext.rebind("cf", cf);
        }
                
       
        ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("cf");
        
        //Create Connection
        if(cf == null){
            throw new Exception("Fail to obtain ConnectionFactory from context:"+initialContext.getEnvironment());
        }
        
        try{
            
            /*
             * Test for creation of 1 Temporary Topic
             * 
             */
            {
                System.out.println("Create 1 temp topic");
                Connection connection = cf.createConnection("root", "root");
                
                Map<String, List<?>> map = createTempTopic(connection, 1);
                List<?> names = map.get("name");
                 assert names.size() == 1 : "Should have only 1 name";
	
                //check if temp topics exists
                for (Object object : names) {
                    String name = (String)object;
                    assert checkTopicExists(name) : " Temp Topics should exist";
                }

		printTempTopic("after create 1 topic");

                
                //close connection
                connection.close();
               	printTempTopic("after close cnx");
	
		
                //Print undeleted topics
                printUndeletedTopics(names);
	
            }
            
            /*
             * Test for creation of 2 Temporary Topics
             * 
             */
	     {
                System.out.println("Create 2 temp topic");
                Connection connection = cf.createConnection("root", "root");
                
                Map<String, List<?>> map = createTempTopic(connection, 2);
                List<?> names = map.get("name");
                assert names.size() == 2 : "Should have only 2 name";
               
                //check if temp topics exists
                for (Object name : names) {
                    assert checkTopicExists((String)name) : " Temp Topics should exist";
                }
		printTempTopic("after create 2");
                //close connection
                connection.close();
		printTempTopic("after close cnx");
                //Print undeleted topics
                printUndeletedTopics(names);
	
		 }
            
            /*
             * Test for creation of 3 Temporary Topics
             * 
             */
	      {
                System.out.println("Create 3 temp topic");
                Connection connection = cf.createConnection("root", "root");
                
                Map<String, List<?>> map = createTempTopic(connection, 10);
                List<?> names = map.get("name");
                assert names.size() == 10 : "Should have only 3 name";
                
                //check if temp topics exists
                for (Object name : names) {
                    assert checkTopicExists((String)name) : " Temp Topics should exist";
                }
                	printTempTopic("after create 10");
                //close connection
                connection.close();
                	printTempTopic("after close cnx");
                //Print undeleted topics
                printUndeletedTopics(names);
	
		}
            
            
        }finally
        {
	    // AdminModule.disconnect();
	    stopAgentServer((short) 0);
            
        }
    

    }

    private static void printUndeletedTopics(List<?> names) {
        //check if temp topic exists
        List<String>unDeletedTopics = new ArrayList<String>();
        for (Object object : names) {
            String name = (String)object;
            if(checkTopicExists(name)){
                unDeletedTopics.add(name);
            }
            
        }
        if(!unDeletedTopics.isEmpty()){
            System.out.println("Undeleted Temp Topics :" + Arrays.deepToString(unDeletedTopics.toArray()));
        }
    }

    private static boolean checkTopicExists(String name) {
        try {
            List<Destination> destinations = AdminModule.getDestinations();
            for (Destination destination : destinations) {
                TemporaryTopic topic = (TemporaryTopic)destination;
                if(topic.getTopicName().equalsIgnoreCase(name)){
                    return true;
                }
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }


    private static Map<String, List<?>> createTempTopic(Connection connection, int count) throws NamingException, Exception {
        List<String> tempTopicNames = new ArrayList<String>();
        List<TemporaryTopic> tempTopics = new ArrayList<TemporaryTopic>();
        Map<String, List<?>> map = new HashMap<String, List<?>>();
        
        for(int i=0; i< count; i++){
            Session tempSession1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            TemporaryTopic tempTopic = tempSession1.createTemporaryTopic();
            tempTopicNames.add(tempTopic.getTopicName());
            tempTopics.add(tempTopic);
            
        }

        map.put("name", tempTopicNames);
        map.put("topic", tempTopics);
        return map;
        
    }


    private static javax.naming.Context getInitialContext()
            throws NamingException {
        Hashtable<String, String> namingContextEnv = new Hashtable<String, String>();
        namingContextEnv.put("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
        namingContextEnv.put("java.naming.factory.host","localhost");
        namingContextEnv.put("java.naming.factory.port","16400");
        
        javax.naming.Context jndiCtx = new javax.naming.InitialContext(namingContextEnv);
        return jndiCtx;
    }

    private static void  printTempTopic(String str) throws Exception{
	System.out.println();
    	List<Destination> destinations = AdminModule.getDestinations();
	for (Destination destination : destinations) {
	    TemporaryTopic topic = (TemporaryTopic)destination;
	    System.out.println(str+topic);
	}
	System.out.println();
    }
}
