/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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
 * Implements the <code>javax.jms.StreamMessage</code> interface.
 */
public final class StreamMessage extends Message implements javax.jms.StreamMessage {
  /** The array in which the written data is buffered. */
  private transient ByteArrayOutputStream outputBuffer = null;
  /** The stream in which body data is written. */
  private transient DataOutputStream outputStream = null;
  /** The stream for reading the data. */
  private transient DataInputStream inputStream = null;

  /** <code>true</code> if the message has been sent since its last modification. */
  private transient boolean prepared = false;

  private transient int available = 0;
  private transient boolean firstTimeBytesRead = true;

  private static final int SHORT = 1;
  private static final int CHAR = 2;
  private static final int INT = 3;
  private static final int LONG = 4;
  private static final int FLOAT = 5;
  private static final int DOUBLE = 6;
  private static final int BOOLEAN = 7;
  private static final int STRING = 8;
  private static final int BYTE = 9;
  private static final int BYTES = 10;
  private static final int NULL = 11;

  /**
   * Instantiates a bright new <code>StreamMessage</code>.
   *
   * @exception JMSException  In case of an error while creating the output
   *              stream.
   */
  StreamMessage() throws JMSException {
    super();
    momMsg.type = org.objectweb.joram.shared.messages.Message.STREAM;

    outputBuffer = new ByteArrayOutputStream();
    outputStream = new DataOutputStream(outputBuffer);
    available = 0;
    firstTimeBytesRead = true;
  }

  /**
   * Instantiates a <code>StreamMessage</code> wrapping a consumed
   * MOM message containing a stream of bytes.
   *
   * @param session  The consuming session.
   * @param momMsg  The MOM message to wrap.
   *
   * @exception JMSException  In case of an error while creating the input
   *              stream.
   */
  StreamMessage(Session session,
                org.objectweb.joram.shared.messages.Message momMsg)
    throws JMSException {
    super(session, momMsg);
    
    try {
      inputStream = new DataInputStream(new ByteArrayInputStream(momMsg.body));
    } catch (Exception exc) {
      JMSException jE =
        new JMSException("Error while creating the stream facility.");
      jE.setLinkedException(exc);
      throw jE;
    }
    available = 0;
    firstTimeBytesRead = true;
  }
  
  /** 
   * API method.
   *
   * @exception JMSException  In case of an error while closing the input or
   *              output streams.
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
   * Internal method called before each writing operation.
   *
   * @exception MessageNotWriteableException  If the message body is READ only.
   * @exception JMSException  If the stream could not be prepared for the
   *              writing operation.
   */
  private void prepareWrite() throws JMSException {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the message body is read-only.");
    if (prepared) {
      prepared = false;
      outputBuffer = new ByteArrayOutputStream();
      outputStream = new DataOutputStream(outputBuffer);
    }
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */
  public void writeBoolean(boolean value) throws JMSException {
    prepareWrite();

    try {
      outputStream.writeByte(BOOLEAN);
      outputStream.writeBoolean(value);
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
  public void writeByte(byte value) throws JMSException {
    prepareWrite();

    try {
      outputStream.writeByte(BYTE);
      outputStream.writeByte((int) value);
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
  public void writeBytes(byte[] value) throws JMSException {
    writeBytes(value, 0, value.length);
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception JMSException  If the value could not be written on the stream.
   */   
  public void writeBytes(byte[] value, int offset, int length) throws JMSException {
    prepareWrite();
    
    try {
      outputStream.writeByte(BYTES);
      if (value == null) {
        outputStream.writeInt(-1);
        return;
      }
      
      outputStream.writeInt(length);
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
    prepareWrite();

    try {
      outputStream.writeByte(CHAR);
      outputStream.writeChar((int) value);
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
  public void writeDouble(double value) throws JMSException {
    prepareWrite();

    try {
      outputStream.writeByte(DOUBLE);
      outputStream.writeDouble(value);
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
  public void writeFloat(float value) throws JMSException {
    prepareWrite();

    try {
      outputStream.writeByte(FLOAT);
      outputStream.writeFloat(value);
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
  public void writeInt(int value) throws JMSException {
    prepareWrite();

    try {
      outputStream.writeByte(INT);
      outputStream.writeInt(value);
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
  public void writeLong(long value) throws JMSException {
    prepareWrite();

    try {
      outputStream.writeByte(LONG);
      outputStream.writeLong(value);
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
  public void writeShort(short value) throws JMSException {
    prepareWrite();

    try {
      outputStream.writeByte(SHORT);
      outputStream.writeShort(value);
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
  public void writeString(String value) throws JMSException {
    prepareWrite();
    
    try {
      if (value == null)
        outputStream.writeByte(NULL);
      else {
        outputStream.writeByte(STRING);
        outputStream.writeUTF(value);
      }
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
   * @exception MessageFormatException  If the value type is invalid.
   * @exception JMSException  If the value could not be written on the stream.
   */ 
  public void writeObject(Object value) throws JMSException {
    prepareWrite();

    if (value == null) {
      try {
        outputStream.writeByte(NULL);
      } catch (IOException ioE) {
        JMSException jE = new JMSException("Error while writing the value.");
        jE.setLinkedException(ioE);
        throw jE;
      }
    } else if (value instanceof Boolean) {
      writeBoolean(((Boolean) value).booleanValue());
    } else if (value instanceof Character) {
      writeChar(((Character) value).charValue());
    } else if (value instanceof Byte) {
      writeByte(((Byte) value).byteValue());
    } else if (value instanceof Short) { 
      writeShort(((Short) value).shortValue());
    } else if (value instanceof Integer) { 
      writeInt(((Integer) value).intValue());
    } else if (value instanceof Long) { 
      writeLong(((Long) value).longValue());
    } else if (value instanceof Float) { 
      writeFloat(((Float) value).floatValue());
    } else if (value instanceof Double) { 
      writeDouble(((Double) value).doubleValue());
    } else if (value instanceof String) {
      writeString((String) value);
    } else if (value instanceof byte[]) {
      writeBytes((byte[]) value);
    } else
      throw new MessageFormatException("Can't write non Java primitive type"
                                       + " as a bytes array.");
  }
  
  
  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   * @exception MessageEOFException          Unexpected end of bytes array.
   * @exception JMSException                 internal error
   */
  public boolean readBoolean() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      byte type = inputStream.readByte();
      if (type == BOOLEAN)
        return inputStream.readBoolean();
      else if (type == STRING)
        return Boolean.valueOf(inputStream.readUTF()).booleanValue();
      else
        throw new MessageFormatException("type read: " + type + " is not a boolean or a String.");
    } catch (EOFException e1) {
      MessageEOFException exc = new MessageEOFException("end of message " + e1);
      exc.setLinkedException(e1);
      throw exc;
    } catch (IOException e2) {
      JMSException exc = new JMSException("IOException");
      exc.setLinkedException(e2);
      throw exc;
    }                                                             
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   * @exception MessageEOFException          Unexpected end of bytes array.
   * @exception JMSException                 internal error
   */
  public byte readByte() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      inputStream.mark(inputStream.available());
      byte type = inputStream.readByte();
      if (type == BYTE)
        return inputStream.readByte();
      else if (type == STRING)
        return Byte.valueOf(inputStream.readUTF()).byteValue();
      else
        throw new MessageFormatException("type read: " + type + " is not a byte or a String.");
    } catch (EOFException e1) {
      MessageEOFException exc = new MessageEOFException("end of message " + e1);
      exc.setLinkedException(e1);
      throw exc;
    } catch (IOException e2) {
      JMSException exc = new JMSException("IOException");
      exc.setLinkedException(e2);
      throw exc;
    } catch (NumberFormatException e3) {
      try {
        inputStream.reset();
      } catch (Exception e) {}
      throw e3;
    }           
  }
 
  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   * @exception MessageEOFException          Unexpected end of bytes array.
   * @exception JMSException                 internal error
   */
  public short readShort() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      byte type = inputStream.readByte();
      if (type == SHORT)
        return inputStream.readShort();
      else if (type == BYTE)
        return inputStream.readByte();
      else if (type == STRING)
        return Short.valueOf(inputStream.readUTF()).shortValue();
      else
        throw new MessageFormatException("type read: " + type + " is not a short, a byte or a String.");
    } catch (EOFException e1) {
      MessageEOFException exc = new MessageEOFException("end of message " + e1);
      exc.setLinkedException(e1);
      throw exc;
    } catch (IOException e2) {
      JMSException exc = new JMSException("IOException");
      exc.setLinkedException(e2);
      throw exc;
    } 
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   * @exception MessageEOFException          Unexpected end of bytes array.
   * @exception JMSException                 internal error
   */
  public char readChar() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      byte type = inputStream.readByte();
      if (type == CHAR)
        return inputStream.readChar();
//        else if (type == STRING)
//          return inputStream.readUTF().charAt(0);
      else if (type == NULL)
        throw new NullPointerException("null is not a char.");
      else
        throw new MessageFormatException("type read: " + type + " is not a char.");
    } catch (EOFException e1) {
      MessageEOFException exc = new MessageEOFException("end of message " + e1);
      exc.setLinkedException(e1);
      throw exc;
    } catch (IOException e2) {
      JMSException exc = new JMSException("IOException");
      exc.setLinkedException(e2);
      throw exc;
    } 
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   * @exception MessageEOFException          Unexpected end of bytes array.
   * @exception JMSException                 internal error
   */
  public int readInt() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      byte type = inputStream.readByte();
      if (type == INT)
        return inputStream.readInt();
      else if (type == SHORT)
        return inputStream.readShort();
      else if (type == BYTE)
        return inputStream.readByte();
      else if (type == STRING)
        return Integer.valueOf(inputStream.readUTF()).intValue();
      else
        throw new MessageFormatException("type read: " + type + 
                                         " is not a int, a short, a byte or a String.");
    } catch (EOFException e1) {
      MessageEOFException exc = new MessageEOFException("end of message " + e1);
      exc.setLinkedException(e1);
      throw exc;
    } catch (IOException e2) {
      JMSException exc = new JMSException("IOException");
      exc.setLinkedException(e2);
      throw exc;
    } 
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   * @exception MessageEOFException          Unexpected end of bytes array.
   * @exception JMSException                 internal error
   */
  public long readLong() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      byte type = inputStream.readByte();
      if (type == LONG)
        return inputStream.readLong();
      else if (type == INT)
        return inputStream.readInt();
      else if (type == SHORT)
        return inputStream.readShort();
      else if (type == BYTE)
        return inputStream.readByte();
      else if (type == STRING)
        return Long.valueOf(inputStream.readUTF()).longValue();
      else
        throw new MessageFormatException("type read: " + type + 
                                         " is not a int, a short, a byte or a String.");
    } catch (EOFException e1) {
      MessageEOFException exc = new MessageEOFException("end of message " + e1);
      exc.setLinkedException(e1);
      throw exc;
    } catch (IOException e2) {
      JMSException exc = new JMSException("IOException");
      exc.setLinkedException(e2);
      throw exc;
    } 
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   * @exception MessageEOFException          Unexpected end of bytes array.
   * @exception JMSException                 internal error
   */
  public float readFloat() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      byte type = inputStream.readByte();
      if (type == FLOAT)
        return inputStream.readFloat();
      else if (type == STRING)
        return Float.valueOf(inputStream.readUTF()).floatValue();
      else
        throw new MessageFormatException("type read: " + type + " is not float or String.");
    } catch (EOFException e1) {
      MessageEOFException exc = new MessageEOFException("end of message " + e1);
      exc.setLinkedException(e1);
      throw exc;
    } catch (IOException e2) {
      JMSException exc = new JMSException("IOException");
      exc.setLinkedException(e2);
      throw exc;
    } 
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   * @exception MessageEOFException          Unexpected end of bytes array.
   * @exception JMSException                 internal error
   */
  public double readDouble() throws JMSException
  {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      byte type = inputStream.readByte();
      if (type == DOUBLE)
        return inputStream.readDouble();
      else if (type == FLOAT)
        return inputStream.readFloat();
      else if (type == STRING)
        return Double.valueOf(inputStream.readUTF()).doubleValue();
      else
        throw new MessageFormatException("type read: " + type + " is not a double, a float or a String.");
    } catch (EOFException e1) {
      MessageEOFException exc = new MessageEOFException("end of message " + e1);
      exc.setLinkedException(e1);
      throw exc;
    } catch (IOException e2) {
      JMSException exc = new JMSException("IOException");
      exc.setLinkedException(e2);
      throw exc;
    } 
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   * @exception MessageEOFException          Unexpected end of bytes array.
   * @exception JMSException                 internal error
   */
  public int readBytes(byte[] bytes) throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    if (bytes == null)  return -1;

    if (bytes.length == 0) return 0;
    
    int ret = 0; 
    try {
      byte type;
      int counter = 0; 
      if (firstTimeBytesRead) {
        type = inputStream.readByte();

        if (type == BYTES) {
          available = inputStream.readInt();
          
          if (available == 0 || available == -1)
            return available;
          
          int toread = 0;
          if (bytes.length >= available)
            toread = available;
          else
            toread = bytes.length;
          for (int i = 0; i < toread; i ++) {
            bytes[i] = inputStream.readByte();
            counter++;
          }
          ret = counter;
          available = toread - counter;
          firstTimeBytesRead = false;
          counter = 0;
        } else
          throw new MessageFormatException("type read: " + type + " is not a byte[].");
      } else {
        if (available > 0)
          type = BYTES;
        else {
          inputStream.mark(inputStream.available());
          type = inputStream.readByte();
        }
        
        if (type == BYTES) {
          if (available >= 0)
            available = inputStream.readInt();

          if (available == 0 || available == -1)
            return available;

          int toread = 0;          
          if (bytes.length >= available)
            toread = available;
          else
            toread = bytes.length;
          for (int i = 0; i < toread; i ++) {
            bytes[i] = inputStream.readByte();
            counter++;
          }
          ret = counter;
          available = toread - counter;
          firstTimeBytesRead = false;
          counter = 0;
        } else {
          inputStream.reset();
          firstTimeBytesRead = true;
          return -1;
        }
      }
    } catch (EOFException e1) {
      MessageEOFException exc = new MessageEOFException("end of message " + e1);
      exc.setLinkedException(e1);
      throw exc;
    } catch (IOException e2) {
      JMSException exc = new JMSException("IOException");
      exc.setLinkedException(e2);
      throw exc;
    } 

    if (ret == 0)
      return -1;
    return ret;
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   * @exception MessageEOFException          Unexpected end of bytes array.
   * @exception JMSException                 internal error
   */
  public String readString() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      byte type = inputStream.readByte();
      if (type == STRING)
        return inputStream.readUTF();
      else if (type == INT)
        return String.valueOf(inputStream.readInt());
      else if (type == SHORT)
        return String.valueOf(inputStream.readShort());
      else if (type == BYTE)
        return String.valueOf(inputStream.readByte());
      else if (type == FLOAT)
        return String.valueOf(inputStream.readFloat());
      else if (type == LONG)
        return String.valueOf(inputStream.readLong());
      else if (type == DOUBLE)
        return String.valueOf(inputStream.readDouble());
      else if (type == BOOLEAN)
        return String.valueOf(inputStream.readBoolean());
      else if (type == CHAR)
        return String.valueOf(inputStream.readChar());
      else if (type == NULL)
        return null;
      else
        throw new MessageFormatException("type read: " + type + 
                                         " is not a int, a short, a byte,... or a String.");
    } catch (EOFException e1) {
      MessageEOFException exc = new MessageEOFException("end of message " + e1);
      exc.setLinkedException(e1);
      throw exc;
    } catch (IOException e2) {
      JMSException exc = new JMSException("IOException");
      exc.setLinkedException(e2);
      throw exc;
    } 
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the body is
   *                                         not possible.
   * @exception MessageEOFException          Unexpected end of bytes array.
   * @exception JMSException                 internal error
   */
  public Object readObject() throws JMSException {
    if (! RObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      byte type = inputStream.readByte();

      if (type == BOOLEAN) {
        return new Boolean(inputStream.readBoolean());
      } else if (type == CHAR) {
        return new Character(inputStream.readChar());
      } else if (type == BYTE) {
        return new Byte(inputStream.readByte());
      } else if (type == SHORT) { 
        return new Short(inputStream.readShort());
      } else if (type == INT) { 
        return new Integer(inputStream.readInt());
      } else if (type == LONG) { 
        return new Long(inputStream.readLong());
      } else if (type == FLOAT) { 
        return new Float(inputStream.readFloat());
      } else if (type == DOUBLE) { 
        return new Double(inputStream.readDouble());
      } else if (type == STRING) {
        return inputStream.readUTF();
      } else if (type == NULL) {
        return null;
      } else if (type == BYTES) {
        int available = inputStream.readInt();

        if (available == -1) return null;
        if (available == 0) return new byte[0];

        byte[] b = new byte[available];
        inputStream.read(b);
        return b;
      } else
        throw new MessageFormatException("not a primitive object.");
    } catch (EOFException e1) {
      MessageEOFException exc = new MessageEOFException("end of message " + e1);
      exc.setLinkedException(e1);
      throw exc;
    } catch (IOException e2) {
      JMSException exc = new JMSException("IOException");
      exc.setLinkedException(e2);
      throw exc;
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
      if (inputStream != null) inputStream.reset();

      RObody = true;
      firstTimeBytesRead = true;
    } catch (IOException iE) {
      JMSException jE =
        new JMSException("Error while manipulating the stream facilities.");
      jE.setLinkedException(iE);
      throw jE;
    }
  }

  /**
   * Method actually preparing the message for sending by transferring the
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
