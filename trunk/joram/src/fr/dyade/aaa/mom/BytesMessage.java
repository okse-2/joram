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

public class BytesMessage extends fr.dyade.aaa.mom.Message implements javax.jms.BytesMessage{ 
	
	/** the dataInputStream of the BytesMessage */
	private transient DataInputStream in;
	
	/** the dataoutputStream of the BytesMessage */
	private transient DataOutputStream out;
	
	/** the array of bytes for the dataInputStream */
	private byte[] tabArray;
	
	/** the constructor of the ouputStream */
	private transient ByteArrayOutputStream byteArrayOutStream;
	
	public BytesMessage() {
		modeReadWrite = WRITE_MODE;
		byteArrayOutStream = new ByteArrayOutputStream();
		out = new DataOutputStream(byteArrayOutStream);
	}

	/** @see JMS Specifications */
	public boolean readBoolean() throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			return in.readBoolean();
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
			return in.readByte();
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
	public int readUnsignedByte() throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			return in.readUnsignedByte();
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
			return in.readShort();
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
	public int readUnsignedShort() throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			return in.readUnsignedShort();
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
			return in.readChar();
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
			return in.readInt();
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
			return in.readLong();
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
			return in.readFloat();
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
			return in.readDouble();
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
	public int readBytes(byte[] b, int len) throws javax.jms.JMSException {
		try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			return in.read(b,0,len);
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
	public String readUTF() throws javax.jms.JMSException {
       try {
			if(!(modeReadWrite==READ_MODE))
				throw(new javax.jms.MessageNotReadableException("Message Not Readable"));
			return in.readUTF();
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
	public void writeUTF(String value) throws javax.jms.JMSException {
       try {
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			out.writeUTF(value);
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
			if(!(modeReadWrite==WRITE_MODE))
				throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
			
			if(obj instanceof Double)
				out.writeDouble(((Double) obj).doubleValue()); 
			else if(obj instanceof Float)
				out.writeFloat(((Float) obj).floatValue()); 
			else if(obj instanceof Long)
				out.writeLong(((Long) obj).longValue()); 
			else if(obj instanceof Integer)
				out.writeInt(((Integer) obj).intValue()); 
			else if(obj instanceof Short)
				out.writeShort(((Short) obj).shortValue()); 
			else if(obj instanceof Byte)
				out.writeByte(((Byte) obj).byteValue()); 
			else if(obj instanceof Boolean)
				out.writeBoolean(((Boolean) obj).booleanValue()); 
			else if(obj instanceof Character)
				out.writeChar(((Character) obj).charValue()); 
			else if(obj instanceof String)
				out.write(((String) obj).getBytes()); 
			else if(obj instanceof Byte[]) {
				Byte[] b = (Byte[]) obj;
				int i;
				for(i=0; i<b.length;i++)
					out.writeByte(b[i].byteValue()); 
			} else
				throw(new javax.jms.MessageFormatException("Incorrect Format Message "));
		} catch (javax.jms.JMSException exc) {
			throw (exc);
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
				in = new DataInputStream(byteArrayInStream);
			}
		} catch (java.io.IOException exc) {
			javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
			except.setLinkedException(exc);
			throw(except);
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
				out = new DataOutputStream(byteArrayOutStream);
			}
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
}
