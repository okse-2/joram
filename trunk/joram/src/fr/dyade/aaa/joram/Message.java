/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.joram;

import fr.dyade.aaa.mom.messages.MessageType;

import java.util.*;

import javax.jms.JMSException;
import javax.jms.IllegalStateException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

/**
 * Implements the <code>javax.jms.Message</code> interface.
 * <p>
 * A Joram message wraps a proprietary MOM message which is actually the
 * effective MOM transport facility for the JMS operations.
 */
public class Message implements javax.jms.Message
{
  /** The wrapped MOM message. */
  protected fr.dyade.aaa.mom.messages.Message momMsg;

  /** <code>true</code> if the message properties are read-only. */
  protected boolean ROproperties = false;
  /** <code>true</code> if the message body is read-only. */
  protected boolean RObody = false;
  /** The session that consumes the message, if any. */
  protected Session sess = null;


  /**
   * Constructs a <code>Message</code>.
   */
  Message()
  {
    momMsg = new fr.dyade.aaa.mom.messages.Message();
  }


  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   * @exception JMSException  If the acknowledgement fails for an other
   *              reason.
   */
  public void acknowledge() throws JMSException
  {
    if (sess == null
        || sess.transacted
        || sess.acknowledgeMode != javax.jms.Session.CLIENT_ACKNOWLEDGE)
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
    RObody = false;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void clearProperties() throws JMSException
  {
    if (momMsg.properties != null) {
      momMsg.properties.clear();
      momMsg.properties = null;
    }
    ROproperties = false;
  } 

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public boolean propertyExists(String name) throws JMSException
  {
    if (momMsg.properties == null)
      return false;

    return (momMsg.properties.get(name) != null);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public Enumeration getPropertyNames() throws JMSException
  {
    if (momMsg.properties == null)
      return (new Hashtable()).keys();

    return momMsg.properties.keys();
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
    if (0 <= priority && priority <=9)
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
  public void setJMSDestination(javax.jms.Destination destination)
            throws JMSException
  {
    if (destination instanceof Queue)
      momMsg.setDestination(((Queue) destination).getQueueName(), true);
    else if (destination instanceof Topic)
      momMsg.setDestination(((Topic) destination).getTopicName(), false);
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
  public void setJMSReplyTo(javax.jms.Destination replyTo) throws JMSException
  {
    if (replyTo instanceof Queue)
      momMsg.setReplyTo(((Queue) replyTo).getQueueName(), true);
    else if (replyTo instanceof Topic)
      momMsg.setReplyTo(((Topic) replyTo).getTopicName(), false);
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
    momMsg.userBytesHeader = correlationID;
  }
  
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void setJMSType(String type) throws JMSException
  {
    momMsg.userStringHeader = type;
  }

  /**
   * API method.
   *
   * @exception JMSException  If the delivery mode is incorrect.
   */
  public void setJMSDeliveryMode(int deliveryMode) throws JMSException
  {
    if (deliveryMode != javax.jms.DeliveryMode.PERSISTENT 
	&& deliveryMode != javax.jms.DeliveryMode.NON_PERSISTENT)
      throw new JMSException("Invalid delivery mode.");

    momMsg.userIntHeader = deliveryMode;
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
    return momMsg.userIntHeader;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public javax.jms.Destination getJMSDestination() throws JMSException
  {
    if (momMsg.getPTP())
      return new Queue(momMsg.getDestination());
    else
      return new Topic(momMsg.getDestination());
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
  public javax.jms.Destination getJMSReplyTo() throws JMSException
  {
    if (momMsg.replyToQueue())
      return new Queue(momMsg.getReplyTo());
    else
      return new Topic(momMsg.getReplyTo());
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
    return momMsg.userStringHeader;
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
   * @exception JMSException  Actually never thrown.
   */
  public byte[] getJMSCorrelationIDAsBytes() throws JMSException
  {
    return momMsg.userBytesHeader;
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
    checkPropSet(name);
    momMsg.properties.put(name, new Boolean(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setByteProperty(String name, byte value) throws JMSException
  {
    checkPropSet(name);
    momMsg.properties.put(name, new Byte(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setDoubleProperty(String name, double value) throws JMSException
  {
    checkPropSet(name);
    momMsg.properties.put(name, new Double(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setFloatProperty(String name, float value) throws JMSException
  {
    checkPropSet(name);
    momMsg.properties.put(name, new Float(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setIntProperty(String name, int value) throws JMSException
  {
    checkPropSet(name);
    momMsg.properties.put(name, new Integer(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setLongProperty(String name, long value) throws JMSException
  {
    checkPropSet(name);
    momMsg.properties.put(name, new Long(value));
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
    checkPropSet(name);

    if (value instanceof Boolean || value instanceof Number
        || value instanceof String)
      momMsg.properties.put(name, value);

    else
      throw new MessageFormatException("Can't set non primitive Java object"
                                       + " as a property value.");
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setShortProperty(String name, short value) throws JMSException
  {
    checkPropSet(name);
    momMsg.properties.put(name, new Short(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setStringProperty(String name, String value) throws JMSException
  {
    checkPropSet(name);
    momMsg.properties.put(name, new String(value));
  }


  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   */
  public boolean getBooleanProperty(String name) throws JMSException 
  {
    return ConversionHelper.getBoolean(momMsg.properties, name);
  }
  
  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   */
  public byte getByteProperty(String name) throws JMSException 
  {
    return ConversionHelper.getByte(momMsg.properties, name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   */
  public double getDoubleProperty(String name) throws JMSException
  {
    return ConversionHelper.getDouble(momMsg.properties, name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   */
  public float getFloatProperty(String name) throws JMSException
  {
    return ConversionHelper.getFloat(momMsg.properties, name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   */
  public int getIntProperty(String name) throws JMSException
  {
    return ConversionHelper.getInt(momMsg.properties, name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   */
  public long getLongProperty(String name) throws JMSException
  {
    return ConversionHelper.getLong(momMsg.properties, name);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public Object getObjectProperty(String name) throws JMSException
  {
    if (momMsg.properties == null)
      return null;

    return momMsg.properties.get(name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the property type is invalid.
   */
  public short getShortProperty(String name) throws JMSException
  {
    return ConversionHelper.getShort(momMsg.properties, name);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getStringProperty(String name) throws JMSException
  {
    return ConversionHelper.getString(momMsg.properties, name);
  }


  /**
   * Method called by message producers for getting the wrapped MOM message
   * they actually send.
   *
   * @exception JMSException  If the data could not be serialized.
   */
  fr.dyade.aaa.mom.messages.Message getMomMessage() throws JMSException
  {
    try {
      prepare();
      return momMsg;
    }
    catch (Exception e) {
      JMSException jE = new JMSException("The message body could not be"
                                         + " serialized: " + e);
      jE.setLinkedException(e);
      throw jE;
    } 
  }
 
  /**
   * Wraps a given MOM message in the appropriate Joram message.
   * <p>
   * This method is actually called by a session consuming a MOM message
   * for wrapping it in a Joram message before handing to the consumer.
   *
   * @exception JMSException  If the data could not be deserialized.
   */
  static Message wrapMomMessage(Session sess,
                                fr.dyade.aaa.mom.messages.Message momMsg)
       throws JMSException
  {
    Message msg = null;

    try {
      if (momMsg.type == MessageType.SIMPLE)
        msg = new Message();
      else if (momMsg.type == MessageType.TEXT)
        msg = new TextMessage();
      else if (momMsg.type == MessageType.MAP)
        msg = new MapMessage();
      else if (momMsg.type == MessageType.OBJECT)
        msg = new ObjectMessage();
      else if (momMsg.type == MessageType.STREAM)
        msg = new StreamMessage();
      else if (momMsg.type == MessageType.BYTES)
        msg = new BytesMessage();

      msg.sess = sess;
      msg.momMsg = momMsg;
      msg.ROproperties = true;
      msg.RObody = true;
      msg.restore();
      return msg;
    }
    catch (Exception e) {
      JMSException jE = new JMSException("The message body could not be"
                                         + " deserialized: " + e);
      jE.setLinkedException(e);
      throw jE;
    }
  }

  /**
   * Converts a non Joram JMS message into a Joram message.
   *
   * @exception JMSException  If the Joram message creation fails.
   */
  static Message convertJMSMessage(javax.jms.Message jmsMsg)
       throws JMSException
  {
    Message msg = null;
    if (jmsMsg instanceof javax.jms.TextMessage)
      msg = new TextMessage(((javax.jms.TextMessage) jmsMsg).getText());
    else if (jmsMsg instanceof javax.jms.ObjectMessage)
      msg = new ObjectMessage(((javax.jms.ObjectMessage) jmsMsg).getObject());
    else if (jmsMsg instanceof javax.jms.StreamMessage) {
      msg = new StreamMessage();
      ((StreamMessage) msg).writeObject(((javax.jms.StreamMessage)
                                         jmsMsg).readObject());
    }
    else if (jmsMsg instanceof javax.jms.BytesMessage) {
      msg = new BytesMessage();
      try {
        while (true)
          ((BytesMessage) msg).writeByte(((javax.jms.BytesMessage)
                                          jmsMsg).readByte());
      }
      catch (MessageEOFException mE) {}
    }
    else
      msg = new Message();

    msg.setJMSCorrelationID(jmsMsg.getJMSCorrelationID());
    msg.setJMSReplyTo(jmsMsg.getJMSReplyTo());
    msg.setJMSType(jmsMsg.getJMSType());

    Enumeration names = jmsMsg.getPropertyNames();
    String name;
    while (names.hasMoreElements()) {
      name = (String) names.nextElement();
      msg.setObjectProperty(name, jmsMsg.getObjectProperty(name));
    }
    return msg;
  }

  /**
   * Method actually serializing the wrapped data into the MOM message.
   *
   * @exception Exception  If an error occurs while serializing.
   */
  protected void prepare() throws Exception
  {}

  /** 
   * Method actually deserializing the MOM body as the wrapped data.
   *
   * @exception Exception  If an error occurs while deserializing.
   */
  protected void restore() throws Exception
  {}

  /**
   * Actually checks that a property with a given name might be set.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If its name starts with JMS but not JMS_.
   */
  private boolean checkPropSet(String name) throws JMSException
  {
    if (ROproperties)
      throw new MessageNotWriteableException("Can't set properties on a"
                                             + " read-only message.");

    if (name.startsWith("JMS") && ! name.startsWith("JMS_"))
      throw new JMSException("Property name can't start with JMS.");
 
    if (momMsg.properties == null)
      momMsg.properties = new Hashtable();
 
    return true;
  }
}  
