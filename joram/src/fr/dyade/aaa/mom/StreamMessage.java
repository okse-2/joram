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

import java.io.*;
import java.lang.*;

/** 
 *	a StreamMessage is as JMS specifications 
 * 
 *	@see         subclasses
 *	javax.jms.Message 
 */
 
public class StreamMessage extends fr.dyade.aaa.mom.Message implements javax.jms.StreamMessage{ 
	
	/** the ObjectInputStream of the BytesMessage */
	private transient ObjectInputStream in;
	
	/** the ObjectOutputStream of the BytesMessage */
	private transient ObjectOutputStream out;
	
	/** the array of bytes for the dataInputStream */
	private byte[] tabArray;
	
	/** the constructor of the ouputStream */
	private transient ByteArrayOutputStream byteArrayOutStream;
	
	public StreamMessage() throws javax.jms.JMSException {
		try {
			modeReadWrite = WRITE_MODE;
			byteArrayOutStream = new ByteArrayOutputStream();
			out = new ObjectOutputStream(byteArrayOutStream);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** @see JMS Specifications */
	public boolean readBoolean() throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			try {
				return in.readBoolean();
			} catch (java.io.IOException exc) {}
			
			return (new Boolean((String) in.readObject())).booleanValue() ;
			
		} catch (javax.jms.MessageNotReadableException exc) {
			throw (exc);
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}		

	/** @see JMS Specifications */
	public byte readByte() throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			try {
				return in.readByte();
			} catch (java.io.IOException exc) {}
			
			return (new Byte((String) in.readObject())).byteValue() ;
			
		} catch (javax.jms.MessageNotReadableException exc) {
			throw (exc);
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public int readBytes(byte[] b) throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			return in.read(b);
		} catch (javax.jms.MessageNotReadableException exc) {
			throw (exc);
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public char readChar() throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			try {
				return in.readChar();
			} catch (java.io.IOException exc) {}
			
			return ((String) in.readObject()).charAt(0) ;
			
		} catch (javax.jms.MessageNotReadableException exc) {
			throw (exc);
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** @see JMS Specifications */
	public int readInt() throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			try {
				return in.readInt();
			} catch (java.io.IOException exc1) {}
			
			try {
				return (int)in.readShort();
			} catch (java.io.IOException exc2) {}
			
			try {
				return (int)in.readByte();
			} catch (java.io.IOException exc3) {}
			
			return (new Integer((String) in.readObject())).intValue() ;
			
		} catch (javax.jms.MessageNotReadableException exc) {
			throw (exc);
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** @see JMS Specifications */
	public long readLong() throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			try {
				return in.readLong();
			} catch (java.io.IOException exc1) {}
			
			try {
				return (long)in.readInt();
			} catch (java.io.IOException exc2) {}
			
			try {
				return (long)in.readShort();
			} catch (java.io.IOException exc3) {}
			
			try {
				return (long)in.readByte();
			} catch (java.io.IOException exc4) {}
			
			return (new Long((String) in.readObject())).longValue() ;
			
		} catch (javax.jms.MessageNotReadableException exc) {
			throw (exc);
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** @see JMS Specifications */
	public float readFloat() throws javax.jms.JMSException {
   		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			try {
				return in.readFloat();
			} catch (java.io.IOException exc1) {}
			
			return (new Float((String) in.readObject())).floatValue() ;
			
		} catch (javax.jms.MessageNotReadableException exc) {
			throw (exc);
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** @see JMS Specifications */
	public double readDouble() throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			try {
				return in.readDouble();
			} catch (java.io.IOException exc1) {}
			
			try {
				return in.readFloat();
			} catch (java.io.IOException exc2) {}
			
			return (new Double((String) in.readObject())).doubleValue() ;
			
		} catch (javax.jms.MessageNotReadableException exc) {
			throw (exc);
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}	
	
	/** @see JMS Specifications */
	public short readShort() throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			try {
				return in.readShort();
			} catch (java.io.IOException exc1) {}
		
			try {
				return in.readByte();
			} catch (java.io.IOException exc2) {}
			
			return (new Short((String) in.readObject())).shortValue() ;
			
		} catch (javax.jms.MessageNotReadableException exc) {
			throw (exc);
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	
    /** @see JMS Specifications */
	public String readString() throws javax.jms.JMSException {
       try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			return (String)in.readObject();
		} catch (javax.jms.MessageNotReadableException exc) {
			throw (exc);
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
    }
	
	/** @see JMS Specifications */
	public Object readObject() throws javax.jms.JMSException {
    	try {
			try {
				return in.readObject();
			} catch (java.io.IOException exc1) {}
			
			try {
				return (new Long(in.readLong()));
			} catch (java.io.IOException exc2) {}
			
			try {
				return (new Integer(in.readInt()));
			} catch (java.io.IOException exc3) {}
			
			try {
				return (new Short(in.readShort()));
			} catch (java.io.IOException exc4) {}
			
			try {
				return (new Byte(in.readByte()));
			} catch (java.io.IOException exc5) {}
			
			try {
				return (new Boolean(in.readBoolean())) ;
			} catch (java.io.IOException exc6) {}
			
			try {
				return (new Float(in.readFloat())) ;
			} catch (java.io.IOException exc7) {}
			
			try {
				return (new Double(in.readDouble())) ;
			} catch (java.io.IOException exc8) {}
			
			return (new Character(in.readChar())) ;
			
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void writeBoolean(boolean value) throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.writeBoolean(value);
		} catch (javax.jms.MessageNotWriteableException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void writeByte(byte value) throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.writeByte((int) value);
		} catch (javax.jms.MessageNotWriteableException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void writeBytes(byte[] b, int offset, int len) throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.write(b, offset, len);
		} catch (javax.jms.MessageNotWriteableException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** @see JMS Specifications */
	public void writeBytes(byte[] b) throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.write(b);
		} catch (javax.jms.MessageNotWriteableException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void writeShort(short value) throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.writeShort((int)value);
		} catch (javax.jms.MessageNotWriteableException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** @see JMS Specifications */
	public void writeChar(char value) throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.writeChar((int)value);
		} catch (javax.jms.MessageNotWriteableException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** @see JMS Specifications */
	public void writeInt(int value) throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.writeInt(value);
		} catch (javax.jms.MessageNotWriteableException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** @see JMS Specifications */
	public void writeLong(long value) throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.writeLong(value);
		} catch (javax.jms.MessageNotWriteableException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** @see JMS Specifications */
	public void writeFloat(float value) throws javax.jms.JMSException {
   		try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.writeFloat(value);
		} catch (javax.jms.MessageNotWriteableException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

	/** @see JMS Specifications */
	public void writeDouble(double value) throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.writeDouble(value);
		} catch (javax.jms.MessageNotWriteableException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}

    /** @see JMS Specifications */
	public void writeString(String value) throws javax.jms.JMSException {
       try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.writeObject(value);
		} catch (javax.jms.MessageNotWriteableException exc) {
			throw (exc);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
    }
	
	/** @see JMS Specifications */
	public void writeObject(Object obj) throws javax.jms.JMSException {
		try {
			if(obj instanceof Double)
				out.writeObject(obj) ;
			else if(obj instanceof Float)
				out.writeObject(obj) ;
			else if(obj instanceof Long)
				out.writeObject(obj) ;
			else if(obj instanceof Integer)
				out.writeObject(obj) ;
			else if(obj instanceof Short)
				out.writeObject(obj) ;
			else if(obj instanceof Byte)
				out.writeObject(obj) ;
			else if(obj instanceof Byte[])
				out.writeObject(obj) ;	
			else if(obj instanceof Boolean)
				out.writeObject(obj) ;
			else if(obj instanceof Character)
				out.writeObject(obj) ;
			else if(obj instanceof String)
				out.writeObject(obj) ;
			else 
				throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
		} catch (javax.jms.MessageFormatException exc) {
			throw (exc);
		} catch (java.io.EOFException exc) {
			javax.jms.JMSException except = new javax.jms.MessageEOFException("End of the Message");
			except.setLinkedException(exc);
			throw(except);
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see JMS Specifications */
	public void reset() throws javax.jms.JMSException {
		try {
			if(modeReadWrite==WRITE_MODE) {
				/* first reset done by the sender */
				out.flush();
				modeReadWrite = READ_MODE;
				
				/* creation of the inputStream */
				tabArray = byteArrayOutStream.toByteArray();
			} else {
				/* second reset done by the receiver */

				if(Debug.debug)
					if(Debug.message) {
					 	int i;
						System.out.println("tabArray "+tabArray.length);
						for(i=0; i<tabArray.length;i++) 
							System.out.println(tabArray[i]);
					}
			
				ByteArrayInputStream byteArrayInStream = new ByteArrayInputStream(tabArray);
				in = new ObjectInputStream(byteArrayInStream);
			}
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** overwrite the methode as jms specified */ 
	public void clearBody() throws javax.jms.JMSException {
		try {
			if(modeReadWrite==READ_MODE) {
				modeReadWrite = WRITE_MODE;
				byteArrayOutStream.close();
				byteArrayOutStream = new ByteArrayOutputStream();
				out = new ObjectOutputStream(byteArrayOutStream);
			}
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
}

