/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.joram;

import java.util.*;

import java.io.*;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageEOFException;

/**
 * Implements the <code>javax.jms.BytesMessage</code> interface.
 */
public class BytesMessage extends Message implements javax.jms.BytesMessage
{
  /** <code>true</code> if body data has been written. */
  private boolean written = false;
  /** The buffer containing the written bytes. */
  private ByteArrayOutputStream writtenBos;
  /** The stream containing the written bytes. */
  private DataOutputStream writtenDos;
  /** The bytes array keeping the sent data. */
  private byte[] bytes = null;

  /** The stream for reading the carried bytes. */
  private DataInputStream dis = null;

  /** The bytes body size. */
  private long size;

  /** <code>true</code> if the message body is write-only. */
  protected boolean WObody = true;


  /**
   * Instanciates a <code>BytesMessage</code>.
   */
  BytesMessage(Session sess)
  {
    super(sess);
    writtenBos = new ByteArrayOutputStream();
    writtenDos = new DataOutputStream(writtenBos);
  }
 

  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message is WRITE-ONLY.
   */
  public long getBodyLength() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't get not readable message's"
                                            + " size.");
    return size;
  } 

  /** 
   * API method.
   *
   * @exception JMSException  In case of an error while closing the message
   *              stream facilities.
   */
  public void clearBody() throws JMSException
  {
    super.clearBody();

    try {
      if (writtenBos != null)
        writtenBos.close();
      if (writtenDos != null)
        writtenDos.close();
      if (dis != null)
        dis.close();

      writtenBos = new ByteArrayOutputStream();
      writtenDos = new DataOutputStream(writtenBos);
      bytes = null;
      WObody = true;
      written = false;
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while closing the streams: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
  }


  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */
  public void writeBoolean(boolean value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try { 
      writtenDos.writeBoolean(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void writeByte(byte value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try {
      writtenDos.write(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void writeBytes(byte[] value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try {
      writtenDos.write(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void writeBytes(byte[] value, int offset, int length)
              throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try {
      writtenDos.write(value, offset, length);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void writeChar(char value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try {
      writtenDos.writeChar(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void writeDouble(double value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " messaeg body is read-only.");
    try {
      writtenDos.writeDouble(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void writeFloat(float value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try {
      writtenDos.writeFloat(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */  
  public void writeInt(int value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try {
      writtenDos.writeInt(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void writeLong(long value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try {
      writtenDos.writeLong(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception MessageFormatException  If the value type is invalid.
   */ 
  public void writeObject(Object value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try {
      if (value instanceof Boolean)
        writtenDos.writeBoolean(((Boolean) value).booleanValue());
      else if (value instanceof Character)
        writtenDos.writeChar(((Character) value).charValue());
      else if (value instanceof Integer)
        writtenDos.writeInt(((Integer) value).intValue());
      else if (value instanceof Long)
        writtenDos.writeLong(((Long) value).longValue());
      else if (value instanceof Float)
        writtenDos.writeFloat(((Float) value).floatValue());
      else if (value instanceof Double)
        writtenDos.writeDouble(((Double) value).doubleValue());
      else if (value instanceof String)
        writtenDos.writeUTF((String) value);
      else if (value instanceof byte[])
        writtenDos.write((byte[]) value);
      else
        throw new MessageFormatException("Can't write non Java primitive type"
                                         + " as a bytes array.");
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }
  
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */  
  public void writeShort(short value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try {
      writtenDos.writeShort(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void writeUTF(String value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try {
      writtenDos.writeUTF(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
    written = true;
  }


  /**
   * API method.
   *
   * @exception MessageNotReadableException  If the message body is write-only.
   * @exception JMSException  If an exception occurs while reading the bytes.
   */
  public boolean readBoolean() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return dis.readBoolean();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
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
  public byte readByte() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return dis.readByte();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
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
  public int readUnsignedByte() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return dis.readUnsignedByte();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
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
  public short readShort() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return dis.readShort();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
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
  public int readUnsignedShort() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return dis.readUnsignedShort();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
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
  public char readChar() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return dis.readChar();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
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
  public int readInt() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return dis.readInt();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
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
  public long readLong() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return dis.readLong();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
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
  public float readFloat() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return dis.readFloat();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
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
  public double readDouble() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return dis.readDouble();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
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
  public int readBytes(byte[] value) throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    int counter = 0;

    try {
      for (int i = 0; i < value.length; i ++) {
        value[i] = dis.readByte();
        counter++;
      }
    }
    // End of array has been reached:
    catch (EOFException eofE) {}
    // An error has occured!
    catch (IOException ioE) {
      JMSException jE = null;
      jE = new JMSException("Could not read the bytes array: " + ioE);
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
  public int readBytes(byte[] value, int length) throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    if (length > value.length || length < 0)
      throw new IndexOutOfBoundsException("Invalid length parameter: "
                                          + length);
    int counter = 0;

    try {
      for (int i = 0; i < length; i ++) {
        value[i] = dis.readByte();
        counter++;
      }
    }
    // End of array has been reached:
    catch (EOFException eofE) {}
    // An error has occured!
    catch (IOException ioE) {
      JMSException jE = null;
      jE = new JMSException("Could not read the bytes array: " + ioE);
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
  public String readUTF() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return dis.readUTF();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
      jE.setLinkedException(e);
      throw jE;
    }
  }

  
  /** 
   * API method.
   *
   * @exception JMSException  If the message could not be reseted.
   */
  public void reset() throws JMSException
  {
    try {
      writtenDos.close();
   
      if (bytes == null || written)
        bytes = writtenBos.toByteArray();

      writtenBos.close();
      writtenBos = new ByteArrayOutputStream();
      writtenDos = new DataOutputStream(writtenBos);

      dis = new DataInputStream(new ByteArrayInputStream(bytes));
      size = bytes.length;
  
      RObody = true;
      WObody = false;
    }
    catch (IOException iE) {
      JMSException jE = new JMSException("Can't reset message: " + iE);
      jE.setLinkedException(iE);
      throw jE;
    }
  }

  /**
   * Method actually serializing the wrapped map into the MOM message.
   *
   * @exception Exception  If an error occurs while serializing.
   */
  protected void prepare() throws Exception
  {
    super.prepare();

    writtenDos.close();

    if (bytes == null || written)
      bytes = writtenBos.toByteArray();

    momMsg.setBytes(bytes);

    writtenBos.close();
    writtenBos = new ByteArrayOutputStream();
    writtenDos = new DataOutputStream(writtenBos);
  
    written = false;
  }

  /** 
   * Method actually deserializing the MOM body as the wrapped map.
   *
   * @exception Exception  If an error occurs while deserializing.
   */
  protected void restore() throws Exception
  {
    dis = new DataInputStream(new ByteArrayInputStream(momMsg.getBytes()));

    size = momMsg.getBytes().length;

    RObody = true;
    WObody = false;
  }
}
