/*
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.mom; 

import java.lang.*; 
import java.util.*;  
 
/** 
 *	a Message is as JMS specifications
 *	All the methods for creationof new properties are not implemented
 *	at this time.
 * 
 *	@see         subclasses
 *	javax.jms.Message 
 */ 
 
public class Message implements javax.jms.Message, java.io.Serializable { 
 
	/** the persistent delivery mode */
	public final static int PERSISTENT = 0;
	
	/** the non persistent delivery mode */
	public final static int NON_PERSISTENT = 1;
	
	/** the different mode of read/write of the ObjectMessage */
	public static final int READ_MODE = 0;
	public static final int WRITE_MODE = 1;
	public static final int READ_WRITE_MODE = 2;
	
	/** the mode of read/write */
	protected int modeReadWrite;
	
	/** this variable is the identifier of the message */ 
	private String JMSMessageID;
	
	/** the TimeStamp of the Message */
	private long JMSTimestamp;
	
	/** the String JMSCorrelationID as byte[] */
	private byte[] JMSCorrelationIDByte ;
	
	/** the String JMSCorrelationID*/
	private String JMSCorrelationID ;
	
	/** the replyto destination */
	private javax.jms.Destination JMSReplyTo;
	
	/** the Destination of the Message */
	private javax.jms.Destination dest;
	
	/** the deliverymode */
	private int deliveryMode = PERSISTENT;
	
	/** boolean if a message was already delivered */
	private boolean JMSRedelivered = false;
	
	/** the type of the message */
	private String JMSType = "0";
	
	/** the time of expiration of the message in millisecondes */
	private long JMSExpiration = 0;
	
	/** priority of the message */
	private int JMSPriority = javax.jms.Message.DEFAULT_PRIORITY;
	
	/** reference to the SesionItf so as to acknowledge handly */
	private fr.dyade.aaa.mom.SessionItf session = null;
	
	/** hashtable used to add properties to the message */
	private Hashtable propertiesTable = null;
	
	/** Constructor of a JMS Message */
	public Message() {
		this.modeReadWrite = READ_WRITE_MODE;
	}
	
	/** cf JMS Specifications */
	public java.lang.String getJMSMessageID() throws javax.jms.JMSException {
		try {
			return JMSMessageID;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** cf JMS Specifications */
	public void setJMSMessageID(java.lang.String JMSMessageID) throws javax.jms.JMSException {
		try {
			this.JMSMessageID = JMSMessageID;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** cf JMS Specifications */
	public long getJMSTimestamp() throws javax.jms.JMSException {
		try {
			return JMSTimestamp;	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
 
	/** cf JMS Specifications */
	public void setJMSTimestamp(long JMSTimestamp) throws javax.jms.JMSException {
		try {
			this.JMSTimestamp = JMSTimestamp;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public byte[] getJMSCorrelationIDAsBytes() throws javax.jms.JMSException {
		try {
			return JMSCorrelationIDByte;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public void setJMSCorrelationIDAsBytes(byte[] JMSCorrelationIDByte) throws javax.jms.JMSException {
		try {
			this.JMSCorrelationIDByte = JMSCorrelationIDByte;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public void setJMSCorrelationID(java.lang.String JMSCorrelationID) throws javax.jms.JMSException {
		try {
			this.JMSCorrelationID = JMSCorrelationID;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public java.lang.String getJMSCorrelationID() throws javax.jms.JMSException {
		try {
			return JMSCorrelationID;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public javax.jms.Destination getJMSReplyTo() throws javax.jms.JMSException {
		try {
			return JMSReplyTo;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public void setJMSReplyTo(javax.jms.Destination JMSReplyTo) throws javax.jms.JMSException {
		try {
			this.JMSReplyTo = JMSReplyTo;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public javax.jms.Destination getJMSDestination() throws javax.jms.JMSException {
		try {
			return dest;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public void setJMSDestination(javax.jms.Destination dest) throws javax.jms.JMSException {
		try {
			this.dest = dest;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public int getJMSDeliveryMode() throws javax.jms.JMSException {
		try {
			return deliveryMode;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public void setJMSDeliveryMode(int deliveryMode) throws javax.jms.JMSException {
		try {
			this.deliveryMode = deliveryMode;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public boolean getJMSRedelivered() throws javax.jms.JMSException {
		try {
			return JMSRedelivered;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public void setJMSRedelivered(boolean JMSRedelivered) throws javax.jms.JMSException {
		try {
			this.JMSRedelivered = JMSRedelivered;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public java.lang.String getJMSType() throws javax.jms.JMSException {
		try {
			return JMSType;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public void setJMSType(java.lang.String JMSType) throws javax.jms.JMSException {
		try {
			this.JMSType = JMSType;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public long getJMSExpiration() throws javax.jms.JMSException {
		try {
			return JMSExpiration;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public void setJMSExpiration(long JMSExpiration) throws javax.jms.JMSException {
		try {
			this.JMSExpiration = JMSExpiration;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public int getJMSPriority() throws javax.jms.JMSException {
		try {
			return JMSPriority;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public void setJMSPriority(int JMSPriority) throws javax.jms.JMSException {
		try {
			this.JMSPriority = JMSPriority;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** cf JMS Specifications */
	public void clearProperties() throws javax.jms.JMSException {
		propertiesTable = null;
	}

	/** cf JMS Specifications 
	 */
	public boolean propertyExists(java.lang.String name) {
	  if (propertiesTable != null && propertiesTable.get(name) != null)
	    return true;

	  return false;
	}

	/** cf JMS Specifications */
	public boolean getBooleanProperty(java.lang.String name) throws javax.jms.JMSException {
		try {
			if(propertiesTable!=null) {
				Object obj = propertiesTable.get(name);
				if(obj instanceof Boolean)
					return ((Boolean) obj).booleanValue();
				else if(obj instanceof String)
					return (new Boolean((String) obj)).booleanValue();
				else
					throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
			} else
				throw(new java.lang.NullPointerException());
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public byte getByteProperty(java.lang.String name) throws javax.jms.JMSException {
		try {
			if(propertiesTable!=null) {
				Object obj = propertiesTable.get(name);
				if(obj instanceof Byte)
					return ((Byte) obj).byteValue();
				else if(obj instanceof String)
					return (new Byte((String) obj)).byteValue();
				else
					throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
			} else
				throw(new java.lang.NullPointerException());
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public short getShortProperty(java.lang.String name) throws javax.jms.JMSException {
		try {
			if(propertiesTable!=null) {
				Object obj = propertiesTable.get(name);
				if(obj instanceof Short)
					return ((Short) obj).shortValue();
				else if(obj instanceof Byte)
					return (short) ((Byte) obj).byteValue();
				else if(obj instanceof String)
					return (new Short((String) obj)).shortValue();
				else
					throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
			} else
				throw(new java.lang.NullPointerException());
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public int getIntProperty(java.lang.String name) throws javax.jms.JMSException {
		try {
			if(propertiesTable!=null) {
				Object obj = propertiesTable.get(name);
				if(obj instanceof Integer)
					return ((Integer) obj).intValue();
				else if(obj instanceof Short)
					return (int) ((Short) obj).shortValue();
				else if(obj instanceof Byte)
					return (int) ((Byte) obj).byteValue();
				else if(obj instanceof String)
					return (new Integer((String) obj)).intValue();
				else
					throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
			} else
				throw(new java.lang.NullPointerException());
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public long getLongProperty(java.lang.String name) throws javax.jms.JMSException {
		try {
			if(propertiesTable!=null) {
				Object obj = propertiesTable.get(name);
				if(obj instanceof Long)
					return ((Long) obj).longValue();
				else if(obj instanceof Integer)
					return (long) ((Integer) obj).intValue();
				else if(obj instanceof Short)
					return (long) ((Short) obj).shortValue();
				else if(obj instanceof Byte)
					return (long) ((Byte) obj).byteValue();
				else if(obj instanceof String)
					return (new Long((String) obj)).longValue();
				else
					throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
			} else
				throw(new java.lang.NullPointerException());
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public float getFloatProperty(java.lang.String name) throws javax.jms.JMSException {
		try {
			if(propertiesTable!=null) {
				Object obj = propertiesTable.get(name);
				if(obj instanceof Float)
					return ((Float) obj).floatValue();
				else if(obj instanceof String)
					return (new Float((String) obj)).floatValue();
				else
					throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
			} else
				throw(new java.lang.NullPointerException());
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public double getDoubleProperty(java.lang.String name) throws javax.jms.JMSException {
		try {
			if(propertiesTable!=null) {
				Object obj = propertiesTable.get(name);
				if(obj instanceof Double)
					return ((Double) obj).doubleValue();
				else if(obj instanceof Float)
					return (double) ((Float) obj).floatValue();
				else if(obj instanceof String)
					return (new Double((String) obj)).doubleValue();
				else
					throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
			} else
				throw(new java.lang.NullPointerException());
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public java.lang.String getStringProperty(java.lang.String name) throws javax.jms.JMSException {
		try {
			if(propertiesTable!=null) {
				Object obj = propertiesTable.get(name);
				if(obj instanceof String)
					return ((String) obj);
				else if(obj instanceof Long)
					return ((Long) obj).toString();
				else if(obj instanceof Integer)
					return ((Integer) obj).toString();
				else if(obj instanceof Short)
					return ((Short) obj).toString();
				else if(obj instanceof Byte)
					return ((Byte) obj).toString();
				else if(obj instanceof Double)
					return ((Double) obj).toString();
				else if(obj instanceof Float)
					return ((Float) obj).toString();
				else if(obj instanceof Boolean)
					return ((Boolean) obj).toString();
				else
					throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
			} else
				return null;
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public java.lang.Object getObjectProperty(java.lang.String name) throws javax.jms.JMSException {
		try {
			if(propertiesTable!=null) {
				Object obj = propertiesTable.get(name);
				if(obj instanceof String)
					return ((String) obj);
				else if(obj instanceof Long)
					return ((Long) obj);
				else if(obj instanceof Integer)
					return ((Integer) obj);
				else if(obj instanceof Short)
					return ((Short) obj);
				else if(obj instanceof Byte)
					return ((Byte) obj);
				else if(obj instanceof Double)
					return ((Double) obj);
				else if(obj instanceof Float)
					return ((Float) obj);
				else if(obj instanceof Boolean)
					return ((Boolean) obj);
				else 
					throw(new javax.jms.JMSException("Not a correct Object read"));
			} else
				return null;
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public java.util.Enumeration getPropertyNames() throws javax.jms.JMSException {
		try {
			this.createPropertiesTable();
			return propertiesTable.keys();
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public void setBooleanProperty(java.lang.String name, boolean value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			this.createPropertiesTable();
			propertiesTable.put(name, new Boolean(value));
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public void setByteProperty(java.lang.String name, byte value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			this.createPropertiesTable();
			propertiesTable.put(name, new Byte(value));
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public void setShortProperty(java.lang.String name, short value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			this.createPropertiesTable();
			propertiesTable.put(name, new Short(value));
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public void setIntProperty(java.lang.String name, int value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			this.createPropertiesTable();
			propertiesTable.put(name, new Integer(value));
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	public void setLongProperty(java.lang.String name, long value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			this.createPropertiesTable();
			propertiesTable.put(name, new Long(value));
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public void setFloatProperty(java.lang.String name, float value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			this.createPropertiesTable();
			propertiesTable.put(name, new Float(value));
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public void setDoubleProperty(java.lang.String name, double value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			this.createPropertiesTable();
			propertiesTable.put(name, new Double(value));
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public void setStringProperty(java.lang.String name, java.lang.String value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			this.createPropertiesTable();
			propertiesTable.put(name, value);
		} catch (javax.jms.JMSException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public void setObjectProperty(java.lang.String name, java.lang.Object value) throws javax.jms.JMSException {
		try {
			if(value instanceof Double)
				propertiesTable.put(name, value);
			else if(value instanceof Float)
				propertiesTable.put(name, value);
			else if(value instanceof Long)
				propertiesTable.put(name, value);
			else if(value instanceof Integer)
				propertiesTable.put(name, value);
			else if(value instanceof Short)
				propertiesTable.put(name, value);
			else if(value instanceof Byte)
				propertiesTable.put(name, value);
			else if(value instanceof Boolean)
				propertiesTable.put(name, value);
			else if(value instanceof String)
				propertiesTable.put(name, value);
			else 
				throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
		} catch (javax.jms.MessageFormatException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	public void acknowledge() throws javax.jms.JMSException {
		session.acknowledgeMessage(JMSMessageID);
	}

	public void clearBody() throws javax.jms.JMSException {
		if(modeReadWrite==READ_MODE) 
			modeReadWrite = READ_WRITE_MODE;	
	}
	
	/** set the reference to the session so as to acknowledge handly */
	public void setRefSessionItf(fr.dyade.aaa.mom.SessionItf session) {
		this.session = session;
	}
	
	/** creates the properties table if null */
	private void createPropertiesTable() {
		if(propertiesTable==null)
			propertiesTable = new Hashtable();
	}


    public Object getProp(String name)
    {
      if (name.startsWith("JMS")) {
        if (name.equals("JMSDeliveryMode"))
          return new Double((new Integer(deliveryMode)).doubleValue());
        else if (name.equals("JMSPriority"))
          return new Double((new Integer(JMSPriority)).doubleValue());
        else if (name.equals("JMSMessageID"))
          return JMSMessageID;
        else if (name.equals("JMSTimestamp"))
          return new Double((new Long(JMSTimestamp)).doubleValue());
        else if (name.equals("JMSCorrelationID"))
          return JMSCorrelationID;
        else if (name.equals("JMSType"))
          return JMSType;
      }
      else if (propertyExists(name)) {
        Object prop = propertiesTable.get(name);  
        if (prop instanceof Short)
          return new Double(((Short) prop).doubleValue());
        else if (prop instanceof Integer)
          return new Double(((Integer) prop).doubleValue());
        else if (prop instanceof Long)
          return new Double(((Long) prop).doubleValue());
        else if (prop instanceof Float)
          return new Double(((Float) prop).doubleValue());
        else if (prop instanceof Double)
          return (Double) prop;
        else if (prop instanceof String)
          return (String) prop;
        else if (prop instanceof Boolean)
          return (Boolean) prop;
      }
      return null;
    }
}	
