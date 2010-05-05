/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.kjoram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * 
 */
public final class BytesMessage extends Message {
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
   type = BYTES;

    outputBuffer = new ByteArrayOutputStream();
    outputStream = new DataOutputStream(outputBuffer);
  }


  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message is WRITE-ONLY.
   */
  public long getBodyLength() throws JoramException {
    return body.length;
  } 

  /** 
   * API method.
   *
   * @exception JoramException  In case of an error while closing the output or
   *              input streams.
   */
  public void clearBody() throws JoramException {
    try {
      if (outputStream != null) outputStream.close();
      if (outputBuffer != null) outputBuffer.close();
      if (inputStream != null) inputStream.close();

      outputBuffer = new ByteArrayOutputStream();
      outputStream = new DataOutputStream(outputBuffer);

      body = null;

      prepared = false;
    } catch (IOException ioE) {
      JoramException jE = new JoramException("Error while closing the stream"
                                         + " facilities.");
      throw jE;
    }
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JoramException  If the value could not be written on the stream.
   */
  public void writeBoolean(boolean value) throws JoramException {
    writeObject(new Boolean(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JoramException  If the value could not be written on the stream.
   */ 
  public void writeByte(byte value) throws JoramException {
    writeObject(new Byte(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JoramException  If the value could not be written on the stream.
   */   
  public void writeBytes(byte[] value) throws JoramException {
    writeObject(value);
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JoramException  If the value could not be written on the stream.
   */   
  public void writeBytes(byte[] value, int offset, int length) throws JoramException {
    if (prepared) {
      prepared = false;
      outputBuffer = new ByteArrayOutputStream();
      outputStream = new DataOutputStream(outputBuffer);
    }

    try {
      outputStream.write(value, offset, length);
    } catch (IOException ioE) {
      JoramException jE = new JoramException("Error while writing the value.");
      throw jE;
    }
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JoramException  If the value could not be written on the stream.
   */ 
  public void writeChar(char value) throws JoramException {
    writeObject(new Character(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JoramException  If the value could not be written on the stream.
   */ 
  public void writeDouble(double value) throws JoramException {
    writeObject(new Double(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JoramException  If the value could not be written on the stream.
   */   
  public void writeFloat(float value) throws JoramException {
    writeObject(new Float(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JoramException  If the value could not be written on the stream.
   */  
  public void writeInt(int value) throws JoramException {
    writeObject(new Integer(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JoramException  If the value could not be written on the stream.
   */ 
  public void writeLong(long value) throws JoramException {
    writeObject(new Long(value));
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JoramException  If the value could not be written on the stream.
   */  
  public void writeShort(short value) throws JoramException {
    writeObject(new Short(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JoramException  If the value could not be written on the stream.
   */   
  public void writeUTF(String value) throws JoramException {
    writeObject(value);
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception MessageFormatException  If the value type is invalid.
   * @exception JoramException  If the value could not be written on the stream.
   */ 
  public void writeObject(Object value) throws JoramException {

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
	  outputStream.writeByte(((Byte) value).byteValue());
      else if (value instanceof byte[])
        outputStream.write((byte[]) value);
      else
        throw new MessageFormatException("Can't write non Java primitive type"
                                         + " as a bytes array.");
    } catch (IOException ioE) {
      throw new JoramException(ioE.getMessage());
    }
  }
  
  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public boolean readBoolean() throws JoramException {
    try {
      return inputStream.readBoolean();
    } catch (Exception e) {
      JoramException jE = null;
      if (e instanceof EOFException)
        jE = new JoramException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public byte readByte() throws JoramException {
    try {
      return inputStream.readByte();
    } catch (Exception e) {
      JoramException jE = null;
      if (e instanceof EOFException)
        jE = new JoramException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public int readUnsignedByte() throws JoramException {
    try {
      return inputStream.readUnsignedByte();
    } catch (Exception e) {
      JoramException jE = null;
      if (e instanceof EOFException)
        jE = new JoramException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public short readShort() throws JoramException {
    try {
      return inputStream.readShort();
    } catch (Exception e) {
      JoramException jE = null;
      if (e instanceof EOFException)
        jE = new JoramException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
  }
  
  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public int readUnsignedShort() throws JoramException {
    try {
      return inputStream.readUnsignedShort();
    } catch (Exception e) {
      JoramException jE = null;
      if (e instanceof EOFException)
        jE = new JoramException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public char readChar() throws JoramException {
    try {
      return inputStream.readChar();
    } catch (Exception e) {
      JoramException jE = null;
      if (e instanceof EOFException)
        jE = new JoramException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public int readInt() throws JoramException {
    try {
      return inputStream.readInt();
    } catch (Exception e) {
      JoramException jE = null;
      if (e instanceof EOFException)
        jE = new JoramException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public long readLong() throws JoramException {
    try {
      return inputStream.readLong();
    } catch (Exception e) {
      JoramException jE = null;
      if (e instanceof EOFException)
        jE = new JoramException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public float readFloat() throws JoramException {
    try {
      return inputStream.readFloat();
    } catch (Exception e) {
      JoramException jE = null;
      if (e instanceof EOFException)
        jE = new JoramException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public double readDouble() throws JoramException {
    try {
      return inputStream.readDouble();
    } catch (Exception e) {
      JoramException jE = null;
      if (e instanceof EOFException)
        jE = new JoramException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public int readBytes(byte[] value) throws JoramException {
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
      JoramException jE = null;
      jE = new JoramException("Could not read the bytes array.");
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
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public int readBytes(byte[] value, int length) throws JoramException {
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
      // An error has occurred!
      JoramException jE = null;
      jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
    if (counter == 0) return -1;

    return counter;
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JoramException  If an exception occurs while reading the bytes.
   */
  public String readUTF() throws JoramException {
    try {
      return inputStream.readUTF();
    } catch (Exception e) {
      JoramException jE = null;
      if (e instanceof EOFException)
        jE = new JoramException("Unexpected end of bytes array.");
      else if (e instanceof IOException)
        jE = new JoramException("Could not read the bytes array.");
      throw jE;
    }
  }

  /** 
   * API method.
   *
   * @exception JoramException  If an error occurs while closing the output
   *              stream.
   */
  public void reset() throws JoramException {
    try {
      if (outputStream != null) outputStream.flush();
      if (outputBuffer != null) body = outputBuffer.toByteArray();
      if (inputStream != null)inputStream.close();

      inputStream = new DataInputStream(new ByteArrayInputStream(body));
    } catch (IOException iE) {
      JoramException jE =
        new JoramException("Error while manipulating the stream facilities.");
      throw jE;
    }
  }

  /**
   * Method actually preparing the message for sending by transferring the
   * local body into the wrapped MOM message.
   *
   * @exception MessageFormatException  If an error occurs while serializing.
   */
  protected void prepare() throws JoramException {
    redelivered = false;

    try {
      if (outputStream != null) outputStream.flush();
      if (outputBuffer != null) {
        body = outputBuffer.toByteArray();
        prepared = true;
      }
    } catch (IOException exc) {
      MessageFormatException jExc =
        new MessageFormatException("The message body could not be serialized.");
      throw jExc;
    } 
  } 
}
