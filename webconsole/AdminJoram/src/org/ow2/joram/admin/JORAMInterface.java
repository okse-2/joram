package org.ow2.joram.admin;



public class JORAMInterface {

	SysoListener listener;
	
	public SysoListener getListener() { return listener; }
	
	public JORAMInterface(String login, String password) throws Exception {
		
		listener = new SysoListener();
		boolean connected = false;

		JoramAdmin admin = new JoramAdminImpl();
	    connected = admin.connect(login, password);
	    
	    if(!connected) throw new Exception("Erreur de login/password");
	    
	    admin.start(listener);
	}
	
	/**
	 * Create a message on a queue in JORAM
	 * 
	 * @param queueName Name of the queue containing the message
	 * @param id Id of the message
	 * @param expiration Expiration date of the message
	 * @param timestamp Timestamp of the message
	 * @param priority Priority of the message
	 * @param text Text of the message
	 * @param type Type of the message
	 * @return Create successful
	 */
	public boolean createNewMessage(String queueName, String id, long expiration, long timestamp, int priority, String text, int type) {
		// TODO : Create Message
		return true;
	}
	
	/**
	 * Edit a message on a queue in JORAM
	 * 
	 * @param queueName Name of the queue containing the message
	 * @param id Id of the message
	 * @param expiration Expiration date of the message
	 * @param timestamp Timestamp of the message
	 * @param priority Priority of the message
	 * @param text Text of the message
	 * @param type Type of the message
	 * @return Edit successful
	 */
	public boolean editMessage(String queueName, String id, long expiration, long timestamp, int priority, String text, int type) {
		// TODO : Edit Message
		return true;
	}
	
	/**
	 * Delete a message in JORAM
	 * 
	 * @param messageName Name of the message to delete
	 * @param queueName Name of the queue containing the message
	 * @return suppression Delete Successful
	 */
	public boolean deleteMessage(String messageName, String queueName) {
		// TODO : Delete Message
		return true;
	}
	
	
	/**
	 * Create a topic on JORAM
	 * 
	 * @param name Name of the topic
	 * @param DMQ DMQ Id of the topic
	 * @param destination Destination of the topic
	 * @param period Period of the topic
	 * @param freeReading FreeReading of the topic
	 * @param freeWriting FreeWriting of the topic
	 * @return Create successful
	 */
	public boolean createNewTopic(String name, String DMQ, String destination, long period, boolean freeReading, boolean freeWriting) {
		// TODO : Create Topic
		return true;
	}
	
	/**
	 * Edit a topic on JORAM
	 * 
	 * @param name Name of the topic
	 * @param DMQ DMQ Id of the topic
	 * @param destination Destination of the topic
	 * @param period Period of the topic
	 * @param freeReading FreeReading of the topic
	 * @param freeWriting FreeWriting of the topic
	 * @return Create successful
	 */
	public boolean editTopic(String name, String DMQ, String destination, long period, boolean freeReading, boolean freeWriting) {
		// TODO : Edit Topic
		return true;
	}
	
	/**
	 * Delete a topic in JORAM
	 * 
	 * @param topicName Name of the topic to delete
	 * @return Delete successful
	 */
	public boolean deleteTopic(String topicName) {
		// TODO : Delete Topic
		return true;
	}
	

	/**
	 * Create a user on JORAM
	 * 
	 * @param name Name of the user
	 * @param period Period of the user
	 * @return Create successful
	 */
	public boolean createNewUser(String name, long period) {
		// TODO : Create User
		return true;
	}
	
	/**
	 * Edit a user on JORAM
	 * 
	 * @param name Name of the user
	 * @param period Period of the user
	 * @return Edit successful
	 */
	public boolean editUser(String name, long period) {
		// TODO : Edit User
		return true;
	}
	
	/**
	 * Delete a user on JORAM
	 * 
	 * @param userName Name of the user to delete
	 * @return Delete successful
	 */
	public boolean deleteUser(String userName) {
		// TODO : Delete User
		return true;
	}
	
	/**
	 * Create a queue on JORAM
	 * 
	 * @param name Name of the queue
	 * @param DMQ DMQ Id of the queue
	 * @param destination Destination Id of the queue
	 * @param period Period of the queue
	 * @param threshold Threshold of the queue
	 * @param nbMaxMsg Maximum messages of the queue
	 * @param freeReading Is the queue FreeReading
	 * @param freeWriting Is the queue FreeWriting
	 * @return Create successful
	 */
	public boolean createNewQueue(String name, String DMQ, String destination, long period, int threshold, int nbMaxMsg, boolean freeReading, boolean freeWriting) {
		// TODO : Create Queue
		return true;
	}
	
	/**
	 * Edit a queue on JORAM
	 * 
	 * @param name Name of the queue
	 * @param DMQ DMQ Id of the queue
	 * @param destination Destination Id of the queue
	 * @param period Period of the queue
	 * @param threshold Threshold of the queue
	 * @param nbMaxMsg Maximum messages of the queue
	 * @param freeReading Is the queue FreeReading
	 * @param freeWriting Is the queue FreeWriting
	 * @return Edit successful
	 */
	public boolean editQueue(String name, String DMQ, String destination, long period, int threshold, int nbMaxMsg, boolean freeReading, boolean freeWriting) {
		// TODO : Edit Queue
		return true;
	}
	
	/**
	 * Delete a Queue on JORAM
	 * 
	 * @param queueName Name of the topic to delete
	 * @return Delete successful
	 */
	public boolean deleteQueue(String queueName) {
		// TODO : Delete Queue
		return true;
	}
	
	/**
	 * Clear the waiting requests for a queue on JORAM
	 * 
	 * @param queueName Name of the queue
	 * @return Clear successful
	 */
	public boolean cleanWaitingRequest(String queueName) {
		// TODO : Clear Waiting Request
		return true;
	}

	/**
	 * Clear the pending messages for a queue on JORAM
	 * 
	 * @param queueName Name of the queue
	 * @return Clear successful
	 */
	public boolean cleanPendingMessage(String queueName) {
		// TODO : Clear Pending Message
		return true;
	}
	
	
	/**
	 * Create a subscription on JORAM
	 * 
	 * @param name Name of the subscription
	 * @param nbMaxMsg Maximum messages on the subscription
	 * @param context Context Id of the subscription
	 * @param selector Selector of the subscription
	 * @param subRequest SubRequest of the subscription
	 * @param active Is the subscription active
	 * @param durable Is the subscription durable
	 * @return Create successful
	 */
	public boolean createNewSubscription(String name, int nbMaxMsg, int context, String selector, int subRequest, boolean active, boolean durable) {
		// TODO : Create Subscription
		return true;
	}
	
	/**
	 * Edit a subscription on JORAM
	 * 
	 * @param name Name of the subscription
	 * @param nbMaxMsg Maximum messages on the subscription
	 * @param context Context Id of the subscription
	 * @param selector Selector of the subscription
	 * @param subRequest SubRequest of the subscription
	 * @param active Is the subscription active
	 * @param durable Is the subscription durable
	 * @return Edit successful
	 */
	public boolean editSubscription(String name, int nbMaxMsg, int context, String selector, int subRequest, boolean active, boolean durable) {
		// TODO : Edit Subscription
		return true;
	}
	
	/**
	 * Delete a Queue in JORAM
	 * 
	 * @param subscriptionName Name of the subscription to delete
	 * @return Delete successful
	 */
	public boolean deleteSubscription(String subscriptionName) {
		// TODO : Delete Subscription
		return true;
	}
	
}
