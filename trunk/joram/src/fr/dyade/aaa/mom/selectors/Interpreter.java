/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.selectors;

import fr.dyade.aaa.mom.excepts.MessageValueException;
import fr.dyade.aaa.mom.messages.*;

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
   * Method called by the <code>fr.dyade.aaa.mom.selectors.Filter</code>
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
      try {
        value = message.getOptionalHeader("JMSDeliveryMode");
        int deliveryMode = ConversionHelper.toInt(value);

        if (deliveryMode == 1)
          value = "NON_PERSISTENT";
        else
          value = "PERSISTENT";
      }
      // In that case the sender might not be a JMS client... doing as if
      // the default value was set.
      catch (MessageValueException mE) {
        value = "PERSISTENT";
      }
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
    else if (name.equals("JMS_JORAM_NOTWRITABLE"))
      value = new Boolean(message.notWritable);
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
