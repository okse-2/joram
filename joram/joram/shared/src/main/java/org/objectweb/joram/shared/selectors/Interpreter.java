/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
 * Contributor(s):  ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.selectors;

import java.util.StringTokenizer;

import org.objectweb.joram.shared.messages.Message;

/** 
 * The <code>Interpreter</code> class is used for interpreting selector
 * queries.
 */
class Interpreter
{
  /**
   * Calls the appropriate method for interpreting a field name according to
   * the syntax type.
   * <p>
   * Method called by the <code>org.objectweb.joram.shared.selectors.Filter</code>
   * class.
   *
   * @param name  Name of a field to retrieve.
   * @param message  Message in which retrieving the field.
   * @param syntaxType  Type of the syntax; ex: "JMS".
   */
  static Object interpret(String name, Message message, String syntaxType)
  {
    if (syntaxType.equals("JMS"))
      return jmsInterpret(name, message);
    else
      return null;
  }

  /** Gets the String value of the given object. */
  public static String wrapToString(Object value)
  {
    if (value == null)
      return null;

    if (value instanceof byte[])
      return new String((byte[]) value);
    else
      return value.toString();
  }

  /**
   * Retrieves the value of a field following the JMS syntax rules.
   *
   * @param name  Name of a field to retrieve.
   * @param message  Message in which retrieving the field.
   */
  private static Object jmsInterpret(String name, Message message)
  {
    Object value = null;

    // Checking JMS header fields names:
    if (name.equals("JMSMessageID")) {
      value = message.id;
    } else if (name.equals("JMSPriority")) {
      value = new Integer(message.priority);
    } else if (name.equals("JMSTimestamp")) {
      value = new Long(message.timestamp);
    } else if (name.equals("JMSCorrelationID")) {
      value = message.correlationId;
    } else if (name.equals("JMSDeliveryMode")) {
      if (message.persistent)
          value = "PERSISTENT";
      else
          value = "NON_PERSISTENT";
    } else if (name.equals("JMSType")) {
      value = wrapToString(message.getOptionalHeader("JMSType"));
    } else if (name.startsWith("JMSX")) {
      if (name.equals("JMSXDeliveryCounts"))
        // Checking JMSX header names:
        value = new Integer(message.deliveryCount);
      else
        value = message.getOptionalHeader(name);
    } else {
      // Checking properties:
      value = message.getProperty(name);
    }

    // If the value is a String, replacing its simple quote <'> 
    // by a double one <''> (see JMS 1.1 3.8.1.1):
    if (value instanceof String) {
      StringTokenizer tokenizer = new StringTokenizer((String)value, "'");
      StringBuffer buff = new StringBuffer();               
      while (tokenizer.hasMoreTokens()) {
        buff.append(tokenizer.nextToken());
        buff.append("''");
      }
      String s = buff.toString();
      value = s.substring(0, s.length() - 2);
    }
    else if (value instanceof Number)
      value = new Double(((Number) value).doubleValue());
  
    return value;
  }
}
