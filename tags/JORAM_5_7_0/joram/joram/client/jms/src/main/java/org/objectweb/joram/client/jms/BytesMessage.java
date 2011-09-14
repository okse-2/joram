/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
 * Contributor(s):ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;

/**
 * Implements the <code>javax.jms.BytesMessage</code> interface.
 */
public final class BytesMessage extends Message implements javax.jms.BytesMessage {
  /** The array in which the written data is buffered. */
  private transient ByteArrayOutputStream outputBuffer = null;
  /** The stream in which body data is written. */
  private transient DataOutputStream outputStream = null;
  /** The stream for reading the written data. */
  private transient DataInputStream inputStream = null;
  /** <code>true</code> if the message has been sent since its last modif. */
  private transient boolean prepared = false;

  /**
   * Instantiates a bright new <code>BytesMessage</code>.
   */
  BytesMessage() {
    super();
    momMsg.type = org.objectweb.joram.shared.messages.Message.BYTES;

    outputBuffer = new ByteArrayOutputStream();
    outputStream = new DataOutputStream(outputBuffer);
  }

  /**
   * Instantiates a <code>BytesMessage</code> wrapping a consumed
   * MOM message containing a bytes array.
   *
   * @param sess  The consuming session.
   * @param momMsg  The MOM message to wrap.
   */
  BytesMessage(Session sess, org.objectweb.joram.shared.messages.Message momMsg) {
    super(sess, momMsg);
    inputStream = new DataInputStream(new ByteArrayInputStream(momMsg.body));
  } 

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message is WRITE-ONLY.
   */
  public long getBodyLength() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't get not readable message's"
                                            + " size.");
    return momMsg.body.length;
  } 

  /** 
   * API method.
   *
   * @exception JMSException  In case of an error while closing the output or
   *              input streams.
   */
  public void clearBody() throws JMSException {
    try {
      if (! RObody) {
        outputStream.close();
        outputBuffer.close();
      } else {
        inputStream.close();
      }

      outputBuffer = new ByteArrayOutputStream();
      outputStream = new DataOutputStream(outputBuffer);

      super.clearBody();

      prepared = false;
    } catch (IOException ioE) {
      JMSException jE = new JMSException("Error while closing the stream"
                                         + " facilities.");
      jE.setLinkedException(ioE);
      throw jE;
    }
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */
  public void writeBoolean(boolean value) throws JMSException {
    writeObject(new Boolean(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */ 
  public void writeByte(byte value) throws JMSException {
    writeObject(new Byte(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */   
  public void writeBytes(byte[] value) throws JMSException {
    writeObject(value);
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */   
  public void writeBytes(byte[] value, int offset, int length) throws JMSException {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");

    if (prepared) {
      prepared = false;
      outputBuffer = new ByteArrayOutputStream();
      outputStream = new DataOutputStream(outputBuffer);
    }

    try {
      outputStream.write(value, offset, length);
    } catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value.");
      jE.setLinkedException(ioE);
      throw jE;
    }
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */ 
  public void writeChar(char value) throws JMSException {
    writeObject(new Character(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */ 
  public void writeDouble(double value) throws JMSException {
    writeObject(new Double(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */   
  public void writeFloat(float value) throws JMSException {
    writeObject(new Float(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */  
  public void writeInt(int value) throws JMSException {
    writeObject(new Integer(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */ 
  public void writeLong(long value) throws JMSException {
    writeObject(new Long(value));
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */  
  public void writeShort(short value) throws JMSException {
    writeObject(new Short(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */   
  public void writeUTF(String value) throws JMSException {
    writeObject(value);
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception MessageFormatException  If the value type is invalid.
   * @exception JMSException  If the value could not be written on the stream.
   */ 
  public void writeObject(Object value) throws JMSException {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");

    if (value == null)
      throw new NullPointerException("Forbidden null value.");

    if (prepared) {
      prepared = false;
      outputBuffer = new ByteArrayOutputStream();
      outputStream = new DataOutputStream(outputBuffer);
    }

    try {
      if (value instanceof Boolean)
        outputStream.writeBoolean(((Boolean) value).booleanValue());
      else if (value instanceof Character)
        outputStream.writeChar(((Character) value).charValue());
      else if (value instanceof Integer)
        outputStream.writeInt(((Integer) value).intValue());
      else if (value instanceof Short)
        outputStream.writeShort(((Short) value).shortValue());
      else if (value instanceof Long)
        outputStream.writeLong(((Long) value).longValue());
      else if (value instanceof Float)
        outputStream.writeFloat(((Float) value).floatValue());
      else if (value instanceof Double)
        outputStream.writeDouble(((Double) value).doubleValue());
      else if (value instanceof String)
        outputStream.writeUTF((String) value);
      else if (value instanceof Byte)
	  outputStream.writeByte(((Byte) value).intValue());
      else if (value instanceof byte[])
        outputStream.write((byte[]) value);
      else
        throw new MessageFormatException("Can't write non Java primitive type"
                                         + " as a bytes array.");
    } catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value.");
      jE.setLinkedException(ioE);
      throw jE;
    }
  }
  
  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public boolean readBoolean() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readBoolean();
    } catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(e);
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public byte readByte() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readByte();
    } catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(e);
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public int readUnsignedByte() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readUnsignedByte();
    } catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(e);
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public short readShort() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readShort();
    } catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(e);
      throw jE;
    }
  }
  
  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public int readUnsignedShort() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readUnsignedShort();
    } catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(e);
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public char readChar() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readChar();
    } catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(e);
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public int readInt() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readInt();
    } catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(e);
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public long readLong() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readLong();
    } catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(e);
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public float readFloat() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readFloat();
    } catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(e);
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public double readDouble() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readDouble();
    } catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(e);
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public int readBytes(byte[] value) throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    int counter = 0;

    try {
      for (int i = 0; i < value.length; i ++) {
        value[i] = inputStream.readByte();
        counter++;
      }
    } catch (EOFException eofE) {
      // End of array has been reached:
    } catch (IOException ioE) {
      // An error has occurred!
      JMSException jE = null;
      jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(ioE);
      throw jE;
    }
    if (counter == 0)
      return -1;
    return counter;
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public int readBytes(byte[] value, int length) throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    if (length > value.length || length < 0)
      throw new IndexOutOfBoundsException("Invalid length parameter: "
                                          + length);
    int counter = 0;

    try {
      for (int i = 0; i < length; i ++) {
        value[i] = inputStream.readByte();
        counter++;
      }
    } catch (EOFException eofE) {
      // End of array has been reached:
    } catch (IOException ioE) {
      // An error has occured!
      JMSException jE = null;
      jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(ioE);
      throw jE;
    }
    if (counter == 0) return -1;

    return counter;
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public String readUTF() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readUTF();
    } catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array.");
      jE.setLinkedException(e);
      throw jE;
    }
  }

  /** 
   * API method.
   *
   * @exception JMSException  If an error occurs while closing the output
   *              stream.
   */
  public void reset() throws JMSException {
    try {
      if (! RObody) {
        outputStream.flush();
        momMsg.body = outputBuffer.toByteArray();
      } else {
        inputStream.close();
      }
      inputStream = new DataInputStream(new ByteArrayInputStream(momMsg.body));

      RObody = true;
    } catch (IOException iE) {
      JMSException jE =
        new JMSException("Error while manipulating the stream facilities.");
      jE.setLinkedException(iE);
      throw jE;
    }
  }

  /**
   * Method actually preparing the message for sending by transfering the
   * local body into the wrapped MOM message.
   *
   * @exception MessageFormatException  If an error occurs while serializing.
   */
  protected void prepare() throws JMSException {
    super.prepare();

    try {
      if (! RObody) {
        outputStream.flush();
        momMsg.body = outputBuffer.toByteArray();
        prepared = true;
      }
    } catch (IOException exc) {
      MessageFormatException jExc =
        new MessageFormatException("The message body could not be serialized.");
      jExc.setLinkedException(exc);
      throw jExc;
    } 
  } 
}
