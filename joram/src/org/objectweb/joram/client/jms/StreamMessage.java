/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.jms;

import java.io.*;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageEOFException;

/**
 * Implements the <code>javax.jms.StreamMessage</code> interface.
 */
public class StreamMessage extends Message implements javax.jms.StreamMessage
{
  /** The array in which the written data is buffered. */
  private ByteArrayOutputStream outputBuffer = null;
  /** The stream in which body data is written. */
  private ObjectOutputStream outputStream = null;
  /** The stream for reading the data. */
  private ObjectInputStream inputStream = null;

  /** <code>true</code> if the message body is read-only. */
  private boolean RObody = false; 
  /** <code>true</code> if the message body is write-only. */
  private boolean WObody = true;

  /** Local bytes array. */
  private byte[] bytes = null;
  /** <code>true</code> if the message has been sent since its last modif. */
  private boolean prepared = false;


  /**
   * Instanciates a bright new <code>StreamMessage</code>.
   *
   * @exception JMSException  In case of an error while creating the output
   *              stream.
   */
  StreamMessage() throws JMSException
  {
    super();
    outputBuffer = new ByteArrayOutputStream();

    try {
      outputStream = new ObjectOutputStream(outputBuffer);
    }
    catch (IOException ioE) {
      JMSException jE =
        new JMSException("Error while creating the stream facility.");
      jE.setLinkedException(ioE);
      throw jE;
    }
  }

  /**
   * Instanciates a <code>StreamMessage</code> wrapping a consumed
   * MOM message containing a stream of bytes.
   *
   * @param sess  The consuming session.
   * @param momMsg  The MOM message to wrap.
   *
   * @exception JMSException  In case of an error while creating the input
   *              stream.
   */
  StreamMessage(Session sess, org.objectweb.joram.shared.messages.Message momMsg)
  throws JMSException
  {
    super(sess, momMsg);
    bytes = momMsg.getStream();

    try {
      inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
    }
    catch (Exception exc) {
      JMSException jE =
        new JMSException("Error while creating the stream facility.");
      jE.setLinkedException(exc);
      throw jE;
    }
    RObody = true;
    WObody = false;
  }
  

  /** 
   * API method.
   *
   * @exception JMSException  In case of an error while closing the input or
   *              output streams.
   */
  public void clearBody() throws JMSException
  {
    super.clearBody();

    try {
      if (WObody) {
        outputStream.close();
        outputBuffer.close();
      }
      else
        inputStream.close();

      outputBuffer = new ByteArrayOutputStream();
      outputStream = new ObjectOutputStream(outputBuffer);
      bytes = null;
      RObody = false;
      WObody = true;
      prepared = false;
    }
    catch (IOException ioE) {
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
  public void writeBoolean(boolean value) throws JMSException
  {
    prepareWrite();

    try {
      outputStream.writeBoolean(value);
    }
    catch (IOException ioE) {
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
  public void writeByte(byte value) throws JMSException
  {
    prepareWrite();

    try {
      outputStream.writeByte(value);
    }
    catch (IOException ioE) {
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
  public void writeBytes(byte[] value) throws JMSException
  {
    prepareWrite();

    try {
      outputStream.write(value);
    }
    catch (IOException ioE) {
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
  public void writeBytes(byte[] value, int offset, int length)
              throws JMSException
  {
    prepareWrite();

    try {
      outputStream.write(value, offset, length);
    }
    catch (IOException ioE) {
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
  public void writeChar(char value) throws JMSException
  {
    prepareWrite();

    try {
      outputStream.writeChar(value);
    }
    catch (IOException ioE) {
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
  public void writeDouble(double value) throws JMSException
  {
    prepareWrite();

    try {
      outputStream.writeDouble(value);
    }
    catch (IOException ioE) {
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
  public void writeFloat(float value) throws JMSException
  {
    prepareWrite();

    try {
      outputStream.writeFloat(value);
    }
    catch (IOException ioE) {
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
  public void writeInt(int value) throws JMSException
  {
    prepareWrite();

    try {
      outputStream.writeInt(value);
    }
    catch (IOException ioE) {
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
  public void writeLong(long value) throws JMSException
  {
    prepareWrite();

    try {
      outputStream.writeLong(value);
    }
    catch (IOException ioE) {
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
  public void writeShort(short value) throws JMSException
  {
    prepareWrite();

    try {
      outputStream.writeShort(value);
    }
    catch (IOException ioE) {
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
  public void writeString(String value) throws JMSException
  {
    prepareWrite();

    if (value == null) return;

    try {
      outputStream.writeUTF(value);
    }
    catch (IOException ioE) {
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
  public void writeObject(Object value) throws JMSException
  {
    prepareWrite();

    if (! (value instanceof Boolean) && ! (value instanceof Character)
        && ! (value instanceof Number) && ! (value instanceof String)
        && ! (value instanceof byte[]))
      throw new MessageFormatException("Can't write non Java primitive type"
                                       + " as a bytes array.");
    try {
      outputStream.writeObject(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value.");
      jE.setLinkedException(ioE);
      throw jE;
    }
  }
  
  
  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   */
  public boolean readBoolean() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readBoolean();
    }
    catch (Exception e) {
      throw new MessageFormatException("Can't read the expected type: " + e);
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   *            MessageEOFException          Unexpected end of bytes array.
   */
  public byte readByte() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readByte();
    }
    catch (Exception e) {
      JMSException je = null;
      if (e instanceof EOFException)
        je = new MessageEOFException("Unexpected end of bytes array : " + e);
      else
        je = new MessageFormatException("Can't read the expected type: " + e);
      throw je;
    }
  }
 
  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   */
  public short readShort() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readShort();
    }
    catch (Exception e) {
      throw new MessageFormatException("Can't read the expected type: " + e);
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   */
  public char readChar() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readChar();
    }
    catch (Exception e) {
      throw new MessageFormatException("Can't read the expected type: " + e);
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   */
  public int readInt() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readInt();
    }
    catch (Exception e) {
      throw new MessageFormatException("Can't read the expected type: " + e);
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   */
  public long readLong() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readLong();
    }
    catch (Exception e) {
      throw new MessageFormatException("Can't read the expected type: " + e);
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   */
  public float readFloat() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readFloat();
    }
    catch (Exception e) {
      throw new MessageFormatException("Can't read the expected type: " + e);
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   */
  public double readDouble() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readDouble();
    }
    catch (Exception e) {
      throw new MessageFormatException("Can't read the expected type: " + e);
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   */
  public int readBytes(byte[] bytes) throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    if (bytes == null)
      return -1;
    if (bytes.length == 0)
      return 0;

    int counter = 0;
    try {
      for (int i = 0; i < bytes.length; i ++) {
        bytes[i] = inputStream.readByte();
        counter++;
      }
    }
    // End of array has been reached:
    catch (EOFException eofE) {}
    // An error has occured!
    catch (IOException ioE) {
      throw new MessageFormatException("Can't read the expected type: " + ioE);
    }
    if (counter == 0)
      return -1;
    return counter;
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the expected type is
   *                                         not possible.
   */
  public String readString() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readUTF();
    }
    catch (Exception e) {
      throw new MessageFormatException("Can't read the expected type: " + e);
    }
  }

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception MessageFormatException       If reading the body is
   *                                         not possible.
   */
  public Object readObject() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return inputStream.readObject();
    }
    catch (Exception e) {
      throw new MessageFormatException("Can't read the body: " + e);
    }
  }

  
  /** 
   * API method.
   *
   * @exception JMSException  If an error occurs while closing the output
   *              stream.
   */
  public void reset() throws JMSException
  {
    try {
      if (WObody) {
        outputStream.flush();
        bytes = outputBuffer.toByteArray();
      }
      else
        inputStream.close();
      
      inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));

      RObody = true;
      WObody = false;
    }
    catch (IOException iE) {
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
   * @exception Exception  If an error occurs while serializing.
   */
  protected void prepare() throws Exception
  {
    super.prepare();

    if (WObody) {
      outputStream.flush();
      bytes = outputBuffer.toByteArray();
      prepared = true;
    }

    momMsg.clearBody();
    momMsg.setStream(bytes);
  } 

  /**
   * Internal method called before each writing operation.
   *
   * @exception MessageNotWriteableException  If the message body is READ only.
   * @exception JMSException  If the stream could not be prepared for the
   *              writing operation.
   */
  private void prepareWrite() throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    if (prepared) {
      prepared = false;
      outputBuffer = new ByteArrayOutputStream();

      try {
        outputStream = new ObjectOutputStream(outputBuffer);
      }
      catch (IOException exc) {
        throw new JMSException("Can prepare stream for writing operation: "
                               + exc);
      }
    }
  }
}
