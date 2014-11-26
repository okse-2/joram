/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): 
 */
package joram.jms2;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.MessageNotWriteableRuntimeException;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Set properties on JMSProducer.
 */
public class Test3 extends TestCase {
  public static void main(String[] args) {
    new Test3().run();
  }

  public void run()  {
    boolean     bool = true;
    byte        bValue = 127;
    short       nShort = 10;
    int         nInt = 5;
    long        nLong = 333;
    float       nFloat = 1;
    double      nDouble = 100;
    String      testString = "test";

    try {
      startAgentServer((short) 0);
      Thread.sleep(1000);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      AdminModule.connect(cf, "root", "root");   
      User.create("anonymous", "anonymous", 0);
      Queue queue = Queue.create("queue");
      queue.setFreeReading();
      queue.setFreeWriting();
      AdminModule.disconnect();

      JMSContext context = cf.createContext();
      JMSProducer producer = context.createProducer();
      JMSConsumer consumer = context.createConsumer(queue);
      context.start();

      producer.setProperty("TESTBOOLEAN", bool);
      producer.setProperty("TESTBYTE", bValue);
      producer.setProperty("TESTDOUBLE", nDouble);
      producer.setProperty("TESTFLOAT", nFloat);
      producer.setProperty("TESTINT", nInt);
      producer.setProperty("TESTLONG", nLong);
      producer.setProperty("TESTSHORT", nShort);
      producer.setProperty("TESTSTRING", "test");

      producer.setProperty("OBJTESTBOOLEAN", Boolean.valueOf(bool) );
      producer.setProperty("OBJTESTBYTE", Byte.valueOf(bValue));
      producer.setProperty("OBJTESTDOUBLE", Double.valueOf(nDouble));
      producer.setProperty("OBJTESTFLOAT", Float.valueOf(nFloat));
      producer.setProperty("OBJTESTINT", Integer.valueOf(nInt));
      producer.setProperty("OBJTESTLONG", Long.valueOf(nLong));
      producer.setProperty("OBJTESTSHORT", Short.valueOf(nShort));
      producer.setProperty("OBJTESTSTRING", "test");

      try {
        assertTrue("should return false for unknown getBooleanProperty",
                   producer.getBooleanProperty("TESTDUMMY") == false);
      } catch (Exception e) {
        assertTrue("Caught unexpected exception: " + e.getMessage(), false);
      }
      try {
        producer.getByteProperty("TESTDUMMY");
        assertTrue("NumberFormatException should have occurred for getByteProperty", false);
      } catch (java.lang.NumberFormatException np) {
        assertTrue("NumberFormatException as expected", true);
      }
      try {
        producer.getShortProperty("TESTDUMMY");
        assertTrue("NumberFormatException should have occurred for getShortProperty", false);
      } catch (java.lang.NumberFormatException np) {
        assertTrue("NumberFormatException as expected", true);
      }
      try {
        producer.getIntProperty("TESTDUMMY");
        assertTrue("NumberFormatException should have occurred for getIntProperty", false);
      } catch (java.lang.NumberFormatException np) {
        assertTrue("NumberFormatException as expected", true);
      }
      try {
        producer.getLongProperty("TESTDUMMY");
        assertTrue("NumberFormatException should have occurred for getLongProperty", false);
      } catch (java.lang.NumberFormatException np) {
        assertTrue("NumberFormatException as expected", true);
      }
      try {
        producer.getFloatProperty("TESTDUMMY");
        assertTrue("NullPointerException should have occurred for getFloatProperty", false);
      } catch (java.lang.NullPointerException np) {
        assertTrue("NullPointerException as expected", true);
      }
      try {
        producer.getDoubleProperty("TESTDUMMY");
        assertTrue("NullPointerException should have occurred for getDoubleProperty", false);
      } catch (java.lang.NullPointerException np) {
        assertTrue("NullPointerException as expected", true);
      }

      TextMessage msg = context.createTextMessage("message");
      producer.send(queue, msg);
      TextMessage recv = (TextMessage) consumer.receive();

      assertTrue("incorrect value returned from getBooleanProperty",
                 recv.getBooleanProperty("TESTBOOLEAN") == bool);
      assertTrue("incorrect value returned from getByteProperty",
                 recv.getByteProperty("TESTBYTE") == bValue);
      assertTrue("incorrect value returned from getLongProperty",
                 recv.getLongProperty("TESTLONG") == nLong);
      assertTrue("incorrect value returned from getStringProperty",
                 recv.getStringProperty("TESTSTRING").equals(testString));
      assertTrue("incorrect value returned from getDoubleProperty",
                 recv.getDoubleProperty("TESTDOUBLE") == nDouble);
      assertTrue("incorrect value returned from getFloatProperty",
                 recv.getFloatProperty("TESTFLOAT") == nFloat);
      assertTrue("incorrect value returned from getIntProperty",
                 recv.getIntProperty("TESTINT") == nInt);
      assertTrue("incorrect value returned from getShortProperty",
                 recv.getShortProperty("TESTSHORT") == nShort);
      assertTrue("incorrect Boolean value returned from getObjectProperty",
                 (((Boolean) recv.getObjectProperty("OBJTESTBOOLEAN")).booleanValue() == bool));
      assertTrue("incorrect Byte value returned from getObjectProperty",
                 (((Byte) recv.getObjectProperty("OBJTESTBYTE")).byteValue() == bValue));
      assertTrue("incorrect Long value returned from getObjectProperty",
                 (((Long) recv.getObjectProperty("OBJTESTLONG")).longValue() == nLong));
      assertTrue("incorrect String value returned from getObjectProperty",
                 (((String) recv.getObjectProperty("OBJTESTSTRING")).equals(testString)));
      assertTrue("incorrect Double value returned from getObjectProperty",
                 (((Double) recv.getObjectProperty("OBJTESTDOUBLE")).doubleValue() == nDouble));
      assertTrue("incorrect Float value returned from getObjectProperty",
                 (((Float) recv.getObjectProperty("OBJTESTFLOAT")).floatValue() == nFloat));
      assertTrue("incorrect Integer value returned from getObjectProperty",
                 (((Integer) recv.getObjectProperty("OBJTESTINT")).intValue() == nInt));
      assertTrue("incorrect Short value returned from getObjectProperty",
                 (((Short) recv.getObjectProperty("OBJTESTSHORT")).shortValue() == nShort));
      
      producer.clearProperties();

      // All JMSProducer properties are deleted by the clearProperties method.
      // This leaves the message with an empty set of properties.
      
      assertTrue("NullPointerException should have occurred for getDoubleProperty",
                 producer.getObjectProperty("OBJTESTLONG") == null);
      try {
        producer.getShortProperty("TESTSHORT");
        assertTrue("NumberFormatException should have occurred for getShortProperty", false);
      } catch (java.lang.NumberFormatException np) {
        assertTrue("NumberFormatException as expected", true);
      }
      
      
      // Set/Get JMSProducer message header JMSCorrelationID as bytes.
      {
        byte[] cid = "TestCorrelationID".getBytes();
        producer.setJMSCorrelationIDAsBytes(cid);
        cid = producer.getJMSCorrelationIDAsBytes();
        String cidString = new String(cid);
        assertTrue("getJMSCorrelationID returned incorrect value: " + cidString,
                   ("TestCorrelationID".equals(cidString)));
      }
      
      // Set JMSProducer message headers (Set JMSCorrelationID, JMSType, JMSReplyTo)

      String cid = "TestCorrelationID";
      producer.setJMSCorrelationID(cid);
      assertTrue("getJMSCorrelationID returned null", cid.equals(producer.getJMSCorrelationID()));
      String mtype = "TestMessage";
      producer.setJMSType(mtype);
      assertTrue("getJMSType returned null", mtype.equals(producer.getJMSType()));
      producer.setJMSReplyTo(queue);
      assertTrue("getJMSReplyTo returned null", queue.equals(producer.getJMSReplyTo()));

      msg = context.createTextMessage("message");
      producer.send(queue, msg);
      recv = (TextMessage) consumer.receive();

      assertTrue("getJMSCorrelationID returned null", cid.equals(recv.getJMSCorrelationID()));
      assertTrue("getJMSType returned null", mtype.equals(recv.getJMSType()));
      assertTrue("getJMSReplyTo returned null", queue.equals(recv.getJMSReplyTo()));
      
      // Using received TextMessage try and send it.
      // Expect MessageNotWriteableRuntimeException.
      
      producer.setProperty("OBJTESTLONG", Long.valueOf(nLong));
      try {
        producer.send(queue, recv);
        assertTrue("Expected MessageNotWriteableRuntimeException to be thrown", false);
      } catch (MessageNotWriteableRuntimeException e) {
      } catch (Exception e) {
        assertTrue("Caught unexpected exception: " + e.getMessage(), false);
      }

      context.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();     
    }
  }
}
