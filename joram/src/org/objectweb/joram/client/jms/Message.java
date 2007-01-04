/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.shared.messages.*;

import java.util.*;

import javax.jms.JMSException;
import javax.jms.IllegalStateException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.Message</code> interface.
 * <p>
 * A Joram message wraps a proprietary MOM message which is actually the
 * effective MOM transport facility for the JMS operations.
 */
public class Message implements javax.jms.Message {
  /** The wrapped MOM message. */
  protected org.objectweb.joram.shared.messages.Message momMsg;
  /**
   * If the message is actually consumed, the session that consumes it, 
   * <code>null</code> otherwise.
   */
  protected Session sess = null;
  /**
   *  The JMSDestination field. This field is only use with non Joram
   * destination.
   */
  protected javax.jms.Destination jmsDest = null;

  /**
   * Constructs a bright new <code>Message</code>.
   */
  Message() {
    momMsg = org.objectweb.joram.shared.messages.Message.create();
  }

  /**
   * Constructs a <code>Message</code> wrapping a MOM message consumed by a
   * session.
   *
   * @param sess  The consuming session.
   * @param momMsg  The MOM message to wrap.
   */
  Message(Session sess, org.objectweb.joram.shared.messages.Message momMsg)
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
        || sess.getTransacted()
        || sess.getAcknowledgeMode() != javax.jms.Session.CLIENT_ACKNOWLEDGE)
      return;
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
   * Resets the read-only flag, in order to allow the modification
   * of message properties.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void resetPropertiesRO() throws JMSException {
    momMsg.resetPropertiesRO();
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
  public void setJMSDestination(javax.jms.Destination dest) 
    throws JMSException {
    jmsDest = dest;
    if (dest == null) {
      momMsg.setDestination(null, null);
    } else if (dest instanceof org.objectweb.joram.client.jms.Destination) {
      momMsg.setDestination(
        ((org.objectweb.joram.client.jms.Destination) dest).getName(), 
        ((org.objectweb.joram.client.jms.Destination) dest).getType());
    }
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
    if (replyTo == null) {
      momMsg.setDestination(null, null);
    } else {
      momMsg.setReplyTo(
        ((org.objectweb.joram.client.jms.Destination) replyTo).getName(), 
        ((org.objectweb.joram.client.jms.Destination) replyTo).getType());
    }
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
    if (deliveryMode != javax.jms.DeliveryMode.PERSISTENT
        && deliveryMode != javax.jms.DeliveryMode.NON_PERSISTENT)
      throw new JMSException("Invalid delivery mode.");

    momMsg.setPersistent(deliveryMode == javax.jms.DeliveryMode.PERSISTENT);
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
      return javax.jms.DeliveryMode.PERSISTENT;
    else
      return javax.jms.DeliveryMode.NON_PERSISTENT;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public javax.jms.Destination getJMSDestination() throws JMSException {
    if (jmsDest != null) return jmsDest;

    String id = momMsg.getDestinationId();
    String type = momMsg.toType();
    if (id != null) {
      // The destination name is unknown
      try {
        return Destination.newInstance(id, null, type);
      } catch (Exception exc) {
        throw new JMSException(exc.getMessage());
      }
    }

    return null;
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
  public javax.jms.Destination getJMSReplyTo() 
    throws JMSException {
    String id = momMsg.getReplyToId();
    String type = momMsg.replyToType();
    if (id != null) {
      // The destination name is unknown
      try {
        return Destination.newInstance(id, null, type);
      } catch (Exception exc) {
        throw new JMSException(exc.getMessage());
      }
    } else return null;
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
  public void setDoubleProperty(String name, double value) throws JMSException
  {
    doSetProperty(name, new Double(value));
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public void setFloatProperty(String name, float value) throws JMSException
  {
    doSetProperty(name, new Float(value));
  }

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
  public double getDoubleProperty(String name) throws JMSException
  {
    try {
      return ConversionHelper.toDouble(doGetProperty(name));
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
  public float getFloatProperty(String name) throws JMSException
  {
    try {
      return ConversionHelper.toFloat(doGetProperty(name));
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
      else if (name.equalsIgnoreCase("NULL")
               || name.equalsIgnoreCase("TRUE")
               || name.equalsIgnoreCase("FALSE")
               || name.equalsIgnoreCase("NOT")
               || name.equalsIgnoreCase("AND")
               || name.equalsIgnoreCase("OR")
               || name.equalsIgnoreCase("BETWEEN")
               || name.equalsIgnoreCase("LIKE")
               || name.equalsIgnoreCase("IN")
               || name.equalsIgnoreCase("IS")
               || name.equalsIgnoreCase("ESCAPE"))
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
  org.objectweb.joram.shared.messages.Message getMomMessage()
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
  public static Message
         wrapMomMessage(Session sess, org.objectweb.joram.shared.messages.Message momMsg)
         throws JMSException
  {
    Message msg = null;

    if (momMsg.getType() == MessageType.SIMPLE)
      msg = new Message(sess, momMsg);
    else if (momMsg.getType() == MessageType.TEXT)
      msg = new TextMessage(sess, momMsg);
    else if (momMsg.getType() == MessageType.MAP)
      msg = new MapMessage(sess, momMsg);
    else if (momMsg.getType() == MessageType.OBJECT)
      msg = new ObjectMessage(sess, momMsg);
    else if (momMsg.getType() == MessageType.STREAM)
      msg = new StreamMessage(sess, momMsg);
    else if (momMsg.getType() == MessageType.BYTES)
      msg = new BytesMessage(sess, momMsg);

    return msg;
  }

  /**
   * Converts a non-Joram JMS message into a Joram message.
   *
   * @exception JMSException  If an error occurs while building the message.
   */
  static Message convertJMSMessage(javax.jms.Message jmsMsg)
         throws JMSException {
    Message msg = null;
    if (jmsMsg instanceof javax.jms.TextMessage) {
      msg = new TextMessage();
      ((javax.jms.TextMessage) msg).setText(((javax.jms.TextMessage) jmsMsg).getText());
    } else if (jmsMsg instanceof javax.jms.ObjectMessage) {
      msg = new ObjectMessage();
      ((javax.jms.ObjectMessage) msg).setObject(((javax.jms.ObjectMessage) jmsMsg).getObject());
    } else if (jmsMsg instanceof javax.jms.StreamMessage) {
      msg = new StreamMessage();
      try {
        ((javax.jms.StreamMessage) jmsMsg).reset();
        while (true)
          ((StreamMessage) msg).writeObject(((javax.jms.StreamMessage) jmsMsg).readObject());
      } catch (Exception mE) {}
    } else if (jmsMsg instanceof javax.jms.BytesMessage) {
      msg = new BytesMessage();
      try {
        ((javax.jms.BytesMessage) jmsMsg).reset();
        while (true)
          ((BytesMessage) msg).writeByte(((javax.jms.BytesMessage) jmsMsg).readByte());
      } catch (Exception mE) {}
    } else if (jmsMsg instanceof javax.jms.MapMessage) {
      msg = new MapMessage();
      Enumeration mapNames = ((javax.jms.MapMessage) jmsMsg).getMapNames();
      String mapName;
      while (mapNames.hasMoreElements()) {
        mapName = (String) mapNames.nextElement();
        ((javax.jms.MapMessage) msg).setObject(mapName, 
                                               ((javax.jms.MapMessage)
                                                jmsMsg).getObject(mapName));
      }
    } else {
      msg = new Message();
    }

    msg.setJMSDestination(jmsMsg.getJMSDestination());
    msg.setJMSCorrelationID(jmsMsg.getJMSCorrelationID());
    msg.setJMSReplyTo(jmsMsg.getJMSReplyTo());
    msg.setJMSType(jmsMsg.getJMSType());
    msg.setJMSMessageID(jmsMsg.getJMSMessageID());

    Enumeration names = jmsMsg.getPropertyNames();
    if (names != null) {
      String name;
      while (names.hasMoreElements()) {
        name = (String) names.nextElement();
        try {
          msg.setObjectProperty(name, jmsMsg.getObjectProperty(name));
        } catch (JMSException e) {
          // Joram not support other Optional JMSX, just ignore.
          if (! name.startsWith("JMSX") && ! name.startsWith("JMS_"))
            throw e;
        }
      }
    }

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

  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    try {
      strbuf.append('(').append(super.toString());
      strbuf.append(",messageID=").append(getJMSMessageID());
      strbuf.append(",destination=").append(getJMSDestination());
      strbuf.append(",correlationId=").append(getJMSCorrelationID());
      strbuf.append(",deliveryMode=").append(getJMSDeliveryMode());
      strbuf.append(",expiration=").append(getJMSExpiration());
      strbuf.append(",priority=").append(getJMSPriority());
      strbuf.append(",redelivered=").append(getJMSRedelivered());
      strbuf.append(",replyTo=").append(getJMSReplyTo());
      strbuf.append(",timestamp=").append(getJMSTimestamp());
      strbuf.append(",type=").append(getJMSType());
      strbuf.append(')');
    } catch (JMSException exc) {
      JoramTracing.dbgClient.log(BasicLevel.ERROR,
                                 "Message.toString()", exc);
      return super.toString();
    }
 
    return strbuf.toString();
  }
}  
