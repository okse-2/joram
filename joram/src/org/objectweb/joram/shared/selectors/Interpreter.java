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
 * Contributor(s):  ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.selectors;

import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.messages.*;

import java.util.StringTokenizer;

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
    if (name.equals("JMSMessageID"))
      value = message.getIdentifier();
    else if (name.equals("JMSPriority"))
      value = new Integer(message.getPriority());
    else if (name.equals("JMSTimestamp"))
      value = new Long(message.getTimestamp());
    else if (name.equals("JMSCorrelationID"))
      value = message.getCorrelationId();
    else if (name.equals("JMSDeliveryMode")) {
      if (message.getPersistent())
          value = "PERSISTENT";
      else
          value = "NON_PERSISTENT";
    }
    else if (name.equals("JMSType"))
      value = ConversionHelper.toString(message.getOptionalHeader("JMSType"));
    // Checking JMSX header names:
    else if (name.startsWith("JMSX")) {
      if (name.equals("JMSXDeliveryCounts"))
        value = new Integer(message.deliveryCount);
      else
        value = message.getOptionalHeader(name);
    }
    // Checking JORAM specific header names:
    else if (name.equals("JMS_JORAM_DELETEDDEST"))
      value = new Boolean(message.deletedDest);
    else if (name.equals("JMS_JORAM_NOTWRITEABLE"))
      value = new Boolean(message.notWriteable);
    else if (name.equals("JMS_JORAM_EXPIRED"))
      value = new Boolean(message.expired);
    else if (name.equals("JMS_JORAM_UNDELIVERABLE"))
      value = new Boolean(message.undeliverable);
    // Checking properties:
    else
      value = message.getObjectProperty(name);

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
