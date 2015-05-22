/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2013 ScalAgent Distributed Technologies
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
 * <p>
 * A BytesMessage object is used to send a message containing a stream of uninterpreted bytes.
 * It inherits from the Message interface and adds a bytes message body. The BytesMessage methods
 * are based largely on those found in java.io.DataInputStream and java.io.DataOutputStream.
 * <p>
 * The primitive types can be written explicitly using methods for each type. They may also be
 * written generically as objects. For instance, a call to BytesMessage.writeInt(6) is equivalent
 * to BytesMessage.writeObject(new Integer(6)).
 * <p>
 * When the message is first created, and when clearBody is called, the body of the message is in
 * write-only mode. After the first call to reset has been made, the message body is in read-only
 * mode. After a message has been sent, the client that sent it can retain and modify it without
 * affecting the message that has been sent. The same message object can be sent multiple times.
 * When a message has been received, the provider has called reset so that the message body is in
 * read-only mode for the client.
 * <p>
 * If clearBody is called on a message in read-only mode, the message body is cleared and the message
 * is in write-only mode.
 * <p>
 * If a client attempts to read a message in write-only mode, a MessageNotReadableException is thrown.
 * <p>
 * If a client attempts to write a message in read-only mode, a MessageNotWriteableException is thrown.
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
   * @throws IOException if an I/O error has occurred
   */
  BytesMessage(Session sess, org.objectweb.joram.shared.messages.Message momMsg) throws JMSException {
    super(sess, momMsg);
    try {
      inputStream = new DataInputStream(new ByteArrayInputStream(momMsg.getBody()));
    } catch (IOException exc) {
      MessageFormatException jExc =
        new MessageFormatException("The message body could not be uncompressed.");
      jExc.setLinkedException(exc);
      throw jExc;
    } 
  } 

  /**
   * API method.
   * Gets the number of bytes of the message body when the message is in read-only mode.
   * The value returned can be used to allocate a byte array. The value returned is the entire
   * length of the message body, regardless of where the pointer for reading the message is
   * currently located.
   * 
   * @return the number of bytes in the message's body.
   *
   * @exception MessageNotReadableException  If the message is WRITE-ONLY.
   */
  public long getBodyLength() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't get not readable message's  size.");
    return momMsg.getBodyLength();
  } 

  /** 
   * API method.
   * Clears out the message body.
   * <p>
   * Calling this method leaves the message body in the same state as an empty body in
   * a newly created message.
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
   * Writes a boolean to the bytes message stream as a 1-byte value. The value true is written
   * as the value (byte)1; the value false is written as the value (byte)0.
   * 
   * @param value the value to be written.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */
  public void writeBoolean(boolean value) throws JMSException {
    writeObject(new Boolean(value));
  }
 
  /** 
   * API method.
   * Writes a byte to the bytes message stream.
   * 
   * @param value the value to be written.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */ 
  public void writeByte(byte value) throws JMSException {
    writeObject(new Byte(value));
  }
 
  /** 
   * API method.
   * Writes a byte array to the bytes message stream.
   * 
   * @param value the byte array to be written.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */   
  public void writeBytes(byte[] value) throws JMSException {
    writeObject(value);
  }

  /** 
   * API method.
   * Writes a portion of a byte array to the bytes message stream.
   * 
   * @param value the byte array to be written.
   * @param offset the initial offset within the byte array
   * @param length the number of bytes to use
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
   * Writes a char to the bytes message stream.
   * 
   * @param value the value to be written.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */ 
  public void writeChar(char value) throws JMSException {
    writeObject(new Character(value));
  }
 
  /** 
   * API method.
   * Writes a double to the bytes message stream.
   * 
   * @param value the value to be written.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */ 
  public void writeDouble(double value) throws JMSException {
    writeObject(new Double(value));
  }
 
  /** 
   * API method.
   * Writes a float to the bytes message stream.
   * 
   * @param value the value to be written.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */   
  public void writeFloat(float value) throws JMSException {
    writeObject(new Float(value));
  }
 
  /** 
   * API method.
   * Writes an int to the bytes message stream.
   * 
   * @param value the value to be written.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */  
  public void writeInt(int value) throws JMSException {
    writeObject(new Integer(value));
  }
 
  /** 
   * API method.
   * Writes a long to the bytes message stream.
   * 
   * @param value the value to be written.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */ 
  public void writeLong(long value) throws JMSException {
    writeObject(new Long(value));
  }

  /** 
   * API method.
   * Writes a short to the bytes message stream.
   * 
   * @param value the value to be written.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */  
  public void writeShort(short value) throws JMSException {
    writeObject(new Short(value));
  }
 
  /** 
   * API method.
   * Writes a string to the bytes message stream using UTF-8 encoding in a machine-independent manner.
   * 
   * @param value the String value to be written.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */   
  public void writeUTF(String value) throws JMSException {
    writeObject(value);
  }

  /** 
   * API method.
   * Writes an object to the bytes message stream.
   * <p>
   * This method works only for the objectified primitive object types (Integer, Double,
   * Long ...), String objects, and byte arrays.
   * 
   * @param value the primitive Java object to be written; it must not be null.
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
   * Reads a boolean from the bytes message stream.
   * 
   * @return the value read
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
   * Reads a byte from the bytes message stream.
   * 
   * @return the value read
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
   * Reads an unsigned byte from the bytes message stream.
   * 
   * @return the next byte from the bytes message stream, interpreted as an unsigned 8-bit number.
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
   * Reads a short from the bytes message stream.
   * 
   * @return the value read
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
   * Reads an unsigned short from the bytes message stream.
   * 
   * @return the next two bytes from the bytes message stream, interpreted as an unsigned 16-bit integer.
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
   * Reads a char from the bytes message stream.
   * 
   * @return the value read
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
   * Reads an int from the bytes message stream.
   * 
   * @return the value read
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
   * Reads a long from the bytes message stream.
   * 
   * @return the value read
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
   * Reads a float from the bytes message stream.
   * 
   * @return the value read
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
   * Reads a double from the bytes message stream.
   * 
   * @return the value read
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
   * Reads up to value.length bytes from the bytes message stream. A subsequent call
   * reads the next increment, and so on.
   * <p>
   * A return value of the total number of bytes read less than the length of the array
   * indicates that there are no more bytes left to be read from the stream. The next read
   * of the stream returns -1.
   * 
   * @param value the buffer into which the data is read.
   * @return the total number of bytes read into the buffer, or -1 if there is no more data
   *         because the end of the stream has been reached.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public int readBytes(byte[] value) throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as it is write-only.");
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
   * Reads up to length bytes of the bytes message stream. A subsequent call reads the
   * next increment, and so on.
   * <p>
   * A return value of the total number of bytes read less than the length parameter indicates
   * that there are no more bytes left to be read from the stream. The next read of the stream
   * returns -1.
   * <p>
   * If length is negative, or length is greater than the length of the array value, then an
   * IndexOutOfBoundsException is thrown. No bytes will be read from the stream for this exception
   * case.
   * 
   * @param value   the buffer into which the data is read.
   * @param length  the number of bytes to read; must be less than or equal to value.length.
   * @return the total number of bytes read into the buffer, or -1 if there is no more data
   *         because the end of the stream has been reached.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public int readBytes(byte[] value, int length) throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as it is write-only.");
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
   * Reads a string that has been encoded using a modified UTF-8 format from the bytes
   * message stream.
   * 
   * @return a Unicode string from the bytes message stream.
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
   * Puts the message body in read-only mode and repositions the stream of bytes to the beginning.
   *
   * @exception JMSException  If an error occurs while closing the output
   *              stream.
   */
  public void reset() throws JMSException {
    try {
      if (! RObody) {
        outputStream.flush();
        momMsg.setBody(outputBuffer.toByteArray());
      } else {
        inputStream.close();
      }
      inputStream = new DataInputStream(new ByteArrayInputStream(momMsg.getBody()));

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
        momMsg.setBody(outputBuffer.toByteArray());
        prepared = true;
      }
    } catch (IOException exc) {
      MessageFormatException jExc =
        new MessageFormatException("The message body could not be serialized.");
      jExc.setLinkedException(exc);
      throw jExc;
    }
  } 

  /**
   * Get message content as byte array
   * 
   * @return byte[] the message content as byte array
   * @throws JMSException If an error occurs while closing the output stream.
   */
  byte[] getBytes() throws JMSException {
    byte[] result;
    try {
      reset();
      result = momMsg.getBody();
      reset();
      return result;
    } catch (IOException e) {
      throw new JMSException ("Unable to get byte message body ");
    }
  }
  
  @Override
  public <T> T getBody(Class<T> c) throws JMSException {
    if (! RObody)
      throw new MessageFormatException("Message is not readable");
    
    return super.getBody(c);
  }

  @Override
  protected <T> T getEffectiveBody(Class<T> c) throws JMSException {
    return ((T) getBytes());
  }
}


