/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2002 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2002 INRIA
 * Contact: joram-team@objectweb.org
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
 * Initial developer(s): Jeff Mesnil (Inria)
 * Contributor(s): Nicolas Tachker (ScalAgent D.T.)
 */

package jms.conform.message;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageNotWriteableException;
import javax.jms.TextMessage;
import javax.jms.ObjectMessage;
import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import jms.conform.connection.ConnectionTest;
import jms.framework.PTPTestCase;
import jms.framework.TestConfig;


/**
 * Tests on message body.
 */
public class MessageBodyTest extends PTPTestCase {

  /**
   * Test that the <code>TextMessage.clearBody()</code> method does nto clear
   * the message properties.
   */
  public void testClearBody_2() {
    try {
      TextMessage message = senderSession.createTextMessage();
      message.setStringProperty("prop", "foo");
      message.clearBody();
      assertEquals(
          "3.11.1 Clearing a message's body does not clear its property entries.\n",
          "foo", message.getStringProperty("prop"));
    } catch (JMSException e) {
      fail(e);
    }
  }

  /**
   * Test that the <code>TextMessage.clearBody()</code> effectively clear the
   * body of the message
   */
  public void testClearBody_1() {
    try {
      TextMessage message = senderSession.createTextMessage();
      message.setText("bar");
      message.clearBody();
      assertEquals(
          "3 .11.1 the clearBody method of Message resets the value of the message body "
              + "to the 'empty' initial message value as set by the message type's create "
              + "method provided by Session.\n", null, message.getText());
    } catch (JMSException e) {
      fail(e);
    }
  }

  /**
   * Test that a call to the <code>TextMessage.setText()</code> method on a
   * received message raises a
   * <code>javax.jms.MessageNotWriteableException</code>.
   */
  public void testWriteOnReceivedBody() {
    try {
      TextMessage message = senderSession.createTextMessage();
      message.setText("foo");
      sender.send(message);

      Message m = receiver.receive(TestConfig.TIMEOUT);
      assertTrue("The message should be an instance of TextMessage.\n",
          m instanceof TextMessage);
      TextMessage msg = (TextMessage) m;
      msg.setText("bar");
      fail("should raise a MessageNotWriteableException (3.11.2)");
    } catch (MessageNotWriteableException e) {
    } catch (JMSException e) {
      fail(e);
    }
  }
  
  /**
   * Test that a call to the <code>TextMessage.getBody()</code> method on a
   * returns same value as previously setted
   * <code>javax.jms.jmsException</code>.
   */
  public void testGetTextMessageBody(){
	  try {
		  String content="abc";
		  TextMessage message= senderSession.createTextMessage();
		  message.setText(content);
		  String result= message.getBody(String.class);
		  assertTrue (" Text Message body is the same ", content.equals(result));
	  } catch (JMSException e){
		  fail (e);
	  }
  }
  
  
  /**
   * Test that a call to the <code>ObjectMessage.getBody()</code> method on a
   * returns same value as previously setted
   * <code>javax.jms.jmsException</code>.
   */
  public void testGetObjectMessageBody(){
	  try {
		  String content="checking getBody()";
		  Exception excep=new Exception(content);
		  ObjectMessage message= senderSession.createObjectMessage();
		  message.setObject(excep);
		  java.io.Serializable tmp=  message.getBody(java.io.Serializable.class);
		  assertTrue (" Instance retrieved is the same as expected ",tmp instanceof Exception);
		  Exception result= (Exception ) tmp;
		  assertTrue (" Object Message body  content is the same as expected ",result.getMessage().equals(content));
	  } catch (JMSException e){
		  fail (e);
	  }
  }
  
  /**
   * Test that a call to the <code>MapMessage.getBody()</code> method on a
   * returns same value as previously setted
   * <code>javax.jms.jmsException</code>.
   */
  public void testGetMapMessageBody(){
	  try {
		  String content="checking getBody()";
		  MapMessage message= senderSession.createMapMessage();
		  message.setObject("except", new Integer(12));
		  Map tmp=message.getBody(java.util.Map.class);
		  assertTrue (" Instance retrieved is the same as expected ",tmp.get("except") instanceof Integer);
		  Integer result= (Integer ) tmp.get("except");
		  assertTrue (" Map Message body  content is the same as expected ",result.intValue()==12);	  
	  } catch (JMSException e){
		  fail (e);
	  }
  }
  /**
   * Test that a call to the <code>BytesMessage.getBody()</code> method on a
   * returns same value as previously setted
   * <code>javax.jms.jmsException</code>.
   */
  public void testGetBytesMessageBody(){
	  try {
		  byte [] content= new byte [20];
		  content [3]=12;
		  BytesMessage message= senderSession.createBytesMessage();
		  message.writeBytes(content);
		  message.reset();
		  byte [] result=message.getBody(byte[].class);
		  assertTrue (" get body result is not null ",result!=null);	 
		  assertTrue (" Bytes Message body  content is the same as expected ",result[3]==12);	  
	  } catch (JMSException e){
		  fail (e);
	  }
  }
  public static void main(String[] args) {
    run(new MessageBodyTest());
  }
}
