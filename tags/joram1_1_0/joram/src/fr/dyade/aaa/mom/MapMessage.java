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

import java.util.*;

/** 
 *	a MapMessage is as JMS specifications 
 * 
 *	@see         subclasses
 *	javax.jms.Message 
 */
 
public class MapMessage extends fr.dyade.aaa.mom.Message implements javax.jms.MapMessage { 
	
	/** hashtable which  contains the set of the properties */
	private Hashtable mapMessageTable ;
	
	/** constructor of the MapMesage */
	public MapMessage() {
		modeReadWrite = READ_WRITE_MODE;
		mapMessageTable = new Hashtable();
	}

	/** @see JMS Specifications */
	public boolean getBoolean(String name) throws javax.jms.JMSException {
		try {
			Object obj = mapMessageTable.get(name);
			if(obj instanceof Boolean)
				return ((Boolean) obj).booleanValue() ;
			return (new Boolean((String) obj)).booleanValue() ;
		} catch (ClassCastException exc) {
			javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}	
	}
	
	/** @see JMS Specifications */
	public byte getByte(String name) throws javax.jms.JMSException {
		try {
			Object obj = mapMessageTable.get(name);
			if(obj instanceof Byte)
				return ((Byte) obj).byteValue() ;
			return (new Byte((String) obj)).byteValue() ;
		} catch (ClassCastException exc) {
			javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public short getShort(String name) throws javax.jms.JMSException {
		try {
			Object obj = mapMessageTable.get(name);
			if(obj instanceof Short)
				return ((Short) obj).shortValue() ;
			else if(obj instanceof Byte)
				return (short)((Byte) obj).byteValue() ;
			return (new Short((String) obj)).shortValue() ;
		} catch (ClassCastException exc) {
			javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public char getChar(String name) throws javax.jms.JMSException {
		try {
			Object obj = mapMessageTable.get(name);
			if(obj instanceof Character)
				return ((Character) obj).charValue() ;
			return ((String) obj).charAt(0) ;
		} catch (ClassCastException exc) {
			javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public int getInt(String name) throws javax.jms.JMSException {
		try {
			Object obj = mapMessageTable.get(name);
			if(obj instanceof Integer)
				return (int)((Integer) obj).intValue() ;
			else if(obj instanceof Short)
				return (int)((Short) obj).shortValue() ;
			else if(obj instanceof Byte)
				return (int)((Byte) obj).byteValue() ;
			return (new Integer((String) obj)).intValue() ;
		} catch (ClassCastException exc) {
			javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public long getLong(String name) throws javax.jms.JMSException {
		try {
			Object obj = mapMessageTable.get(name);
			if(obj instanceof Long)
				return ((Long) obj).longValue() ;
			else if(obj instanceof Integer)
				return (long)((Integer) obj).intValue() ;
			else if(obj instanceof Short)
				return (long)((Short) obj).shortValue() ;
			else if(obj instanceof Byte)
				return (long)((Byte) obj).byteValue() ;
			return (new Long((String) obj)).longValue() ;
		} catch (ClassCastException exc) {
			javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public float getFloat(String name) throws javax.jms.JMSException {
		try {
			Object obj = mapMessageTable.get(name);
			if(obj instanceof Float)
				return ((Float) obj).floatValue() ;
			return (new Float((String) obj)).floatValue() ;
		} catch (ClassCastException exc) {
			javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public double getDouble(String name) throws javax.jms.JMSException {
		try {
			Object obj = mapMessageTable.get(name);
			if(obj instanceof Double)
				return ((Double) obj).doubleValue() ;
			else if(obj instanceof Float)
				return (double)((Float) obj).floatValue() ;
			return (new Double((String) obj)).doubleValue() ;
		} catch (ClassCastException exc) {
			javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public String getString(String name) throws javax.jms.JMSException {
		try {
			Object obj = mapMessageTable.get(name);
			if(obj instanceof Double)
				return String.valueOf((Double) obj) ;
			else if(obj instanceof Float)
				return String.valueOf((Float) obj) ;
			else if(obj instanceof Long)
				return String.valueOf((Long) obj) ;
			else if(obj instanceof Integer)
				return String.valueOf((Integer) obj) ;
			else if(obj instanceof Short)
				return String.valueOf((Short) obj) ;
			else if(obj instanceof Byte)
				return String.valueOf((Byte) obj) ;	
			else if(obj instanceof Boolean)
				return String.valueOf((Boolean) obj) ;
			else if(obj instanceof Character)
				return String.valueOf((Character) obj) ;
			return ((String) obj) ;
		} catch (ClassCastException exc) {
			javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public byte[] getBytes(String name) throws javax.jms.JMSException {
		try {
			Object obj = mapMessageTable.get(name);
			return ((fr.dyade.aaa.mom.MyByteArray) obj).getByteArray() ;
		} catch (ClassCastException exc) {
			javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public Object getObject(String name) throws javax.jms.JMSException {
		try {
			return mapMessageTable.get(name);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public java.util.Enumeration getMapNames() throws javax.jms.JMSException {
		try {
			return mapMessageTable.keys();
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setBoolean(String name, boolean value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			mapMessageTable.put(name, new Boolean(value));
		
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setByte(String name, byte value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			mapMessageTable.put(name, new Byte(value));
		
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setShort(String name, short value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			mapMessageTable.put(name, new Short(value));
		
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setChar(String name, char value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			mapMessageTable.put(name, new Character(value));
		
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setInt(String name, int value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			mapMessageTable.put(name, new Integer(value));
			
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setLong(String name, long value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			mapMessageTable.put(name, new Long(value));
		
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setFloat(String name, float value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			mapMessageTable.put(name, new Float(value));
		
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setDouble(String name, double value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			mapMessageTable.put(name, new Double(value));
		
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setString(String name, String value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			mapMessageTable.put(name, new String(value));
		
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setBytes(String name, byte[] value) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			mapMessageTable.put(name, new fr.dyade.aaa.mom.MyByteArray(value));
		
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setBytes(String name, byte[] value, int offset, int length) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			mapMessageTable.put(name, new fr.dyade.aaa.mom.MyByteArray((new String(value, offset, length)).getBytes()));
		
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void setObject(String name, Object obj) throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE)
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			if(obj instanceof Double)
				mapMessageTable.put(name, obj) ;
			else if(obj instanceof Float)
				mapMessageTable.put(name, obj) ;
			else if(obj instanceof Long)
				mapMessageTable.put(name, obj) ;
			else if(obj instanceof Integer)
				mapMessageTable.put(name, obj) ;
			else if(obj instanceof Short)
				mapMessageTable.put(name, obj) ;
			else if(obj instanceof Byte)
				mapMessageTable.put(name, obj) ;	
			else if(obj instanceof Byte[])
				mapMessageTable.put(name, obj) ;	
			else if(obj instanceof Boolean)
				mapMessageTable.put(name, obj) ;
			else if(obj instanceof Character)
				mapMessageTable.put(name, obj) ;
			else if(obj instanceof String)
				mapMessageTable.put(name, obj) ;
			else
				throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
		} catch (javax.jms.JMSException exc) {
			throw(exc);	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public boolean itemExists(String name) throws javax.jms.JMSException {
		try {
			return mapMessageTable.containsKey(name);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** overwrite the methode as jms specified */ 
	public void clearBody() throws javax.jms.JMSException {
		try {
			modeReadWrite = READ_WRITE_MODE;
			mapMessageTable.clear();
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** put the mode in readOnly */
	public void reset() throws javax.jms.JMSException {
		try {
			modeReadWrite = READ_MODE;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
}
