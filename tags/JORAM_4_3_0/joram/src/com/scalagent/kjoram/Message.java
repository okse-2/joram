/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram;

import com.scalagent.kjoram.excepts.IllegalStateException;
import com.scalagent.kjoram.excepts.*;
import com.scalagent.kjoram.messages.*;

import java.util.*;

/**
 * A Joram message wraps a proprietary MOM message which is actually the
 * effective MOM transport facility for the JMS operations.
 */
public class Message
{
  /** The wrapped MOM message. */
  protected com.scalagent.kjoram.messages.Message momMsg;
  /**
   * If the message is actually consumed, the session that consumes it, 
   * <code>null</code> otherwise.
   */
  protected Session sess = null;


  /**
   * Constructs a bright new <code>Message</code>.
   */
  Message()
  {
    momMsg = new com.scalagent.kjoram.messages.Message();
  }

  /**
   * Constructs a <code>Message</code> wrapping a MOM message consumed by a
   * session.
   *
   * @param sess  The consuming session.
   * @param momMsg  The MOM message to wrap.
   */
  Message(Session sess, com.scalagent.kjoram.messages.Message momMsg)
  {
    this.sess = sess;
    this.momMsg = momMsg;
  }

  
  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   * @exception JMSException  If the acknowledgement fails for any other
   *              reason.
   */
  public void acknowledge() throws JMSException
  {
    if (sess == null
        || sess.transacted
        || sess.acknowledgeMode != Session.CLIENT_ACKNOWLEDGE)
      return;

    if (sess.closed)
      throw new IllegalStateException("Forbidden call on a closed session.");

    sess.acknowledge();
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void clearBody() throws JMSException
  {
    momMsg.clearBody();
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void clearProperties() throws JMSException
  {
    momMsg.clearProperties();
  } 

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public boolean propertyExists(String name) throws JMSException
  {
    return momMsg.propertyExists(name);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public Enumeration getPropertyNames() throws JMSException
  {
    return momMsg.getPropertyNames();
  }

 
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void setJMSMessageID(String id) throws JMSException
  {
    momMsg.setIdentifier(id);
  }

   
  /**
   * API method.
   *
   * @exception JMSException  If the priority value is incorrect.
   */
  public void setJMSPriority(int priority) throws JMSException
  {
    if (0 <= priority && priority <= 9)
      momMsg.setPriority(priority);
    else
      throw new JMSException("Priority of "+ priority +" is not valid"
                             + " (should be an integer between 0 and 9).");
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void setJMSDestination(Destination dest) throws JMSException
  {
    if (dest == null)
      momMsg.setDestination(null, true);

    if (dest instanceof Queue)
      momMsg.setDestination(((Queue) dest).getQueueName(), true);
    else
      momMsg.setDestination(((Topic) dest).getTopicName(), false);

    if (dest instanceof TemporaryQueue || dest instanceof TemporaryTopic)
      momMsg.setOptionalHeader("JMSTempDestination", new Boolean(true));
    else
      momMsg.setOptionalHeader("JMSTempDestination", new Boolean(false));
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void setJMSExpiration(long expiration) throws JMSException
  {
    momMsg.setExpiration(expiration);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void setJMSRedelivered(boolean redelivered) throws JMSException
  {
    momMsg.denied = redelivered;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void setJMSReplyTo(Destination replyTo) throws JMSException
  {
    if (replyTo == null)
      momMsg.setReplyTo(null, true);

    if (replyTo instanceof Queue)
      momMsg.setReplyTo(((Queue) replyTo).getQueueName(), true);
    else
      momMsg.setReplyTo(((Topic) replyTo).getTopicName(), false);

    if (replyTo instanceof TemporaryQueue || replyTo instanceof TemporaryTopic)
      momMsg.setOptionalHeader("JMSTempReplyTo", new Boolean(true));
    else
      momMsg.setOptionalHeader("JMSTempReplyTo", new Boolean(false));
  }
  
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void setJMSTimestamp(long timestamp) throws JMSException
  {
    momMsg.setTimestamp(timestamp);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */ 
  public void setJMSCorrelationID(String correlationID) throws JMSException
  {
    momMsg.setCorrelationId(correlationID);
  }
  
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void setJMSCorrelationIDAsBytes(byte[] correlationID)
            throws JMSException
  {
    momMsg.setCorrelationId(ConversionHelper.toString(correlationID));
  }
  
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void setJMSType(String type) throws JMSException
  {
    momMsg.setOptionalHeader("JMSType", type);
  }

  /**
   * API method.
   *
   * @exception JMSException  If the delivery mode is incorrect.
   */
  public void setJMSDeliveryMode(int deliveryMode) throws JMSException
  {
    if (deliveryMode != DeliveryMode.PERSISTENT 
	&& deliveryMode != DeliveryMode.NON_PERSISTENT)
      throw new JMSException("Invalid delivery mode.");

    momMsg.setPersistent(deliveryMode == DeliveryMode.PERSISTENT);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getJMSMessageID() throws JMSException
  {
    return momMsg.getIdentifier();
  }
  
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getJMSPriority() throws JMSException
  {
    return momMsg.getPriority();
  }
 
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public int getJMSDeliveryMode() throws JMSException
  {
    if (momMsg.getPersistent()) 
      return DeliveryMode.PERSISTENT;
    else
      return DeliveryMode.NON_PERSISTENT;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public Destination getJMSDestination() throws JMSException
  {
    String id = momMsg.getDestinationId();
    boolean queue = momMsg.toQueue();
    Object temporaryValue = null;
    boolean temporary = false;

    try {
      temporaryValue = momMsg.getOptionalHeader("JMSTempDestination");
      temporary = ConversionHelper.toBoolean(temporaryValue);
    }
    // If the value can't be retrieved, it might be because the sender is
    // not a JMS client and did not know about temporary destinations...
    catch (Exception exc) {}

    if (queue) {
      if (temporary)
        return new TemporaryQueue(id, null);
      else
        return new Queue(id);
    }
    else {
      if (temporary)
        return new TemporaryTopic(id, null);
      else
        return new Topic(id);
    }
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public long getJMSExpiration() throws JMSException
  {
    return momMsg.getExpiration();
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public boolean getJMSRedelivered() throws JMSException
  {
    return momMsg.denied;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public Destination getJMSReplyTo() throws JMSException
  {
    String id = momMsg.getReplyToId();
    boolean queue = momMsg.replyToQueue();
    Object temporaryValue = null;
    boolean temporary = false;

    if (id == null)
      return null;

    try {
      temporaryValue = momMsg.getOptionalHeader("JMSTempReplyTo");
      temporary = ConversionHelper.toBoolean(temporaryValue);
    }
    // If the value can't be retrieved, it might be because the sender is not
    // a JMS client...
    catch (Exception exc) {}
  
    if (queue) {
      if (temporary)
        return new TemporaryQueue(id, null);
      else
        return new Queue(id);
    }
    else {
      if (temporary)
        return new TemporaryTopic(id, null);
      else
        return new Topic(id);
    } 
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public long getJMSTimestamp() throws JMSException
  {
    return momMsg.getTimestamp();
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getJMSType() throws JMSException
  {
    Object value = momMsg.getOptionalHeader("JMSType");
    return ConversionHelper.toString(value);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getJMSCorrelationID() throws JMSException
  {
    return momMsg.getCorrelationId();
  }
  
  /**
   * API method.
   *
   * @exception MessageFormatException  In case of a problem while retrieving
   *              the field. 
   */
  public byte[] getJMSCorrelationIDAsBytes() throws JMSException
  {
    try {
      return ConversionHelper.toBytes(momMsg.getCorrelationId());
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setBooleanProperty(String name, boolean value)
            throws JMSException
  {
    doSetProperty(name, new Boolean(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setByteProperty(String name, byte value) throws JMSException
  {
    doSetProperty(name, new Byte(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
//    public void setDoubleProperty(String name, double value) throws JMSException
//    {
//      doSetProperty(name, new Double(value));
//    }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
//    public void setFloatProperty(String name, float value) throws JMSException
//    {
//      doSetProperty(name, new Float(value));
//    }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setIntProperty(String name, int value) throws JMSException
  {
    doSetProperty(name, new Integer(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setLongProperty(String name, long value) throws JMSException
  {
    doSetProperty(name, new Long(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid, or if the
   *              object is invalid.
   */
  public void setObjectProperty(String name, Object value) throws JMSException
  {
    doSetProperty(name, value);
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setShortProperty(String name, short value) throws JMSException
  {
    doSetProperty(name, new Short(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setStringProperty(String name, String value) throws JMSException
  {
    doSetProperty(name, value);
  }


  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public boolean getBooleanProperty(String name) throws JMSException 
  {
    try {
      return ConversionHelper.toBoolean(doGetProperty(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }
  
  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public byte getByteProperty(String name) throws JMSException 
  {
    try {
      return ConversionHelper.toByte(doGetProperty(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
//    public double getDoubleProperty(String name) throws JMSException
//    {
//      try {
//        return ConversionHelper.toDouble(doGetProperty(name));
//      }
//      catch (MessageValueException mE) {
//        throw new MessageFormatException(mE.getMessage());
//      }
//    }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
//    public float getFloatProperty(String name) throws JMSException
//    {
//      try {
//        return ConversionHelper.toFloat(doGetProperty(name));
//      }
//      catch (MessageValueException mE) {
//        throw new MessageFormatException(mE.getMessage());
//      }
//    }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public int getIntProperty(String name) throws JMSException
  {
    try {
      return ConversionHelper.toInt(doGetProperty(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public long getLongProperty(String name) throws JMSException
  {
    try {
      return ConversionHelper.toLong(doGetProperty(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception JMSException  If the name is invalid.
   */
  public Object getObjectProperty(String name) throws JMSException
  {
    return doGetProperty(name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public short getShortProperty(String name) throws JMSException
  {
    try {
      return ConversionHelper.toShort(doGetProperty(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception JMSException  If the name is invalid.
   */
  public String getStringProperty(String name) throws JMSException
  {
    return ConversionHelper.toString(doGetProperty(name));
  }

  /**
   * Method actually setting a new property.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the name is invalid.
   * @exception IllegalArgumentException  If the name string is null or empty.
   */
  private void doSetProperty(String name, Object value) throws JMSException
  {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    String upName = name.toUpperCase();

    try {
      if (name.startsWith("JMSX")) {
        if (name.equals("JMSXGroupID"))
          momMsg.setOptionalHeader(name, ConversionHelper.toString(value));
        else if (name.equals("JMSXGroupSeq"))
          momMsg.setOptionalHeader(name,
                                   new Integer(ConversionHelper.toInt(value)));
        else
          throw new JMSException("Property names with prefix 'JMSX' are"
                                 + " reserved.");
      }
      else if (name.startsWith("JMS_"))
        throw new JMSException("Property names with prefix 'JMS_' are"
                               + " reserved.");
      else if (name.startsWith("JMS"))
        throw new JMSException("Property names with prefix 'JMS' are"
                               + " reserved.");
      else if (upName.equals("NULL")
               || upName.equals("TRUE")
               || upName.equals("FALSE")
               || upName.equals("NOT")
               || upName.equals("AND")
               || upName.equals("OR")
               || upName.equals("BETWEEN")
               || upName.equals("LIKE")
               || upName.equals("IN")
               || upName.equals("IS")
               || upName.equals("ESCAPE"))
        throw new JMSException("Invalid property name: " + name + " is a"
                               + " SQL terminal.");
      else
        momMsg.setObjectProperty(name, value);
    }
    catch (MessageException mE) {
      if (mE instanceof MessageValueException)
        throw new MessageFormatException(mE.getMessage());
      if (mE instanceof MessageROException)
        throw new MessageNotWriteableException(mE.getMessage());
    }
  }

  /**
   * Method actually getting a property.
   *
   * @param name  The property name.
   */
  private Object doGetProperty(String name)
  {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    Object value = null;

    if (name.startsWith("JMSX")) {
      if (name.equals("JMSXDeliveryCount"))
        value = new Integer(momMsg.deliveryCount);
      else
        value = momMsg.getOptionalHeader(name);
    }
    else if (name.startsWith("JMS_JORAM")) {
      if (name.equals("JMS_JORAM_DELETEDDEST"))
        value = new Boolean(momMsg.deletedDest);
      else if (name.equals("JMS_JORAM_NOTWRITABLE"))
        value = new Boolean(momMsg.notWriteable);
      else if (name.equals("JMS_JORAM_EXPIRED"))
        value = new Boolean(momMsg.expired);
      else if (name.equals("JMS_JORAM_UNDELIVERABLE"))
        value = new Boolean(momMsg.undeliverable);
    }
    else
      value = momMsg.getObjectProperty(name);

    return value;
  }


  /**
   * Method called by message producers for getting the wrapped MOM message
   * they actually send.
   *
   * @exception MessageFormatException  If the data could not be serialized.
   */
  com.scalagent.kjoram.messages.Message getMomMessage()
    throws MessageFormatException
  {
    try {
      prepare();
      return momMsg;
    }
    catch (Exception e) {
      MessageFormatException jE =
        new MessageFormatException("The message body could not be"
                                   + " serialized.");
      jE.setLinkedException(e);
      throw jE;
    } 
  }
 
  /**
   * Wraps a given MOM message in the appropriate Joram message.
   * <p>
   * This method is actually called by a session consuming a MOM message
   * for wrapping it in a Joram message before handing it to the consumer.
   *
   * @exception JMSException  If an error occurs while building the message.
   */
  static Message
         wrapMomMessage(Session sess, com.scalagent.kjoram.messages.Message momMsg)
         throws JMSException
  {
    Message msg = null;

    if (momMsg.getType() == MessageType.SIMPLE)
      msg = new Message(sess, momMsg);
    else if (momMsg.getType() == MessageType.TEXT)
      msg = new TextMessage(sess, momMsg);
    else if (momMsg.getType() == MessageType.MAP)
      msg = new MapMessage(sess, momMsg);
    else if (momMsg.getType() == MessageType.BYTES)
      msg = new BytesMessage(sess, momMsg);

    return msg;
  }

  /**
   * Converts a non-Joram JMS message into a Joram message.
   *
   * @exception JMSException  If an error occurs while building the message.
   */
  static Message convertJMSMessage(Message jmsMsg)
         throws JMSException
  {
    Message msg = null;
    if (jmsMsg instanceof TextMessage) {
      msg = new TextMessage();
      ((TextMessage) msg).setText(((TextMessage)jmsMsg).getText());
    }
    else if (jmsMsg instanceof BytesMessage) {
      msg = new BytesMessage();
      try {
        while (true)
          ((BytesMessage) msg).writeByte(((BytesMessage)jmsMsg).readByte());
      }
      catch (MessageEOFException mE) {}
    }
    else if (jmsMsg instanceof MapMessage) {
      msg = new MapMessage();
      Enumeration mapNames = ((MapMessage) jmsMsg).getMapNames();
    }
    else
      msg = new Message();

    msg.setJMSCorrelationID(jmsMsg.getJMSCorrelationID());
    msg.setJMSReplyTo(jmsMsg.getJMSReplyTo());
    msg.setJMSType(jmsMsg.getJMSType());

    return msg;
  }

  /**
   * Method preparing the message for sending; resets header values, and
   * serializes the body (done in subclasses).
   *
   * @exception Exception  If an error occurs while serializing.
   */
  protected void prepare() throws Exception
  {
    momMsg.denied = false;
    momMsg.deletedDest = false;
    momMsg.expired = false;
    momMsg.notWriteable = false;
    momMsg.undeliverable = false;
  }
}  
