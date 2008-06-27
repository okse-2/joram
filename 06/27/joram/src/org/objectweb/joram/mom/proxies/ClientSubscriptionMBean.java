package org.objectweb.joram.mom.proxies;

import javax.management.openmbean.TabularData;

import fr.dyade.aaa.agent.AgentId;

public interface ClientSubscriptionMBean {

  /**
   * Deletes a particular pending message in the subscription. The message is
   * pointed out through its unique identifier.
   * 
   * @param msgId
   *            The unique message's identifier.
   */
  void deleteMessage(String msgId);

  /**
   * Sets the maximum number of message for the subscription.
   * 
   * @param nbMaxMsg
   *            the maximum number of message for subscription (-1 set no
   *            limit).
   */
  void setNbMaxMsg(int nbMaxMsg);

  /**
   * Deletes all messages
   */
  public void clear();

  /**
   * Returns the subscription's context identifier.
   */
  public int getContextId();

  /**
   * Returns the identifier of the subscribing request.
   */
  public int getSubRequestId();

  /**
   * Returns the name of the subscription.
   */
  public String getName();

  /**
   * Returns the identifier of the subscription topic.
   */
  public AgentId getTopicId();

  /**
   * Returns the selector.
   */
  public String getSelector();

  /**
   * Returns <code>true</code> if the subscription is durable.
   */
  public boolean getDurable();

  /**
   * Returns <code>true</code> if the subscription is active.
   */
  public boolean getActive();

  /**
   * Returns the maximum number of message for the subscription. If the limit is
   * unset the method returns -1.
   * 
   * @return the maximum number of message for subscription if set; -1
   *         otherwise.
   */
  public int getNbMaxMsg();

  /**
   * Returns the number of pending messages for the subscription.
   * 
   * @return The number of pending message for the subscription.
   */
  public int getPendingMessageCount();

  /**
   * Returns the list of message's identifiers for the subscription.
   * 
   * @return the list of message's identifiers for the subscription.
   */
  public String[] getMessageIds();
  
  /**
   * Returns the number of erroneous messages forwarded to the DMQ since
   * creation time of this subscription.
   * 
   * @return the number of erroneous messages forwarded to the DMQ.
   */
  public long getNbMsgsSentToDMQSinceCreation();
  
  /**
   * Returns the description of a particular pending message. The message is
   * pointed out through its unique identifier. The description includes the
   * type and priority of the message.
   * 
   * @param msgId
   *            The unique message's identifier.
   * @return the description of the message.
   */
  public TabularData getMessagesTabularData() throws Exception;

}
