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
 * Implements the <code>javax.jms.StreamMessage</code> interface.
 */
public class StreamMessage extends Message implements javax.jms.StreamMessage
{
  /** The stream containing the written data. */
  private ObjectOutputStream oos;
  /** The temporary array holding the written data. */
  private ByteArrayOutputStream bos;
  /** The stream for reading the data. */
  private ObjectInputStream ois;

  /** <code>true</code> if the message body is write-only. */
  private boolean WObody = true;


  /**
   * Instanciates a <code>StreamMessage</code>.
   *
   * @exception JMSException  In case of an error while creating the stream.
   */
  StreamMessage(Session sess) throws JMSException
  {
    super(sess);
    bos = new ByteArrayOutputStream();

    try {
      oos = new ObjectOutputStream(bos);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while creating the stream: "
                                         + ioE);
      jE.setLinkedException(ioE);
      throw jE;
    }
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
      if (bos != null)
        bos.close();
      if (oos != null)
        oos.close();
      if (ois != null)
        ois.close();

      bos = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(bos);
      WObody = true;
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
      oos.writeBoolean(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
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
      oos.write(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
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
      oos.write(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
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
      oos.write(value, offset, length);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
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
      oos.writeChar(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
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
      oos.writeDouble(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
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
      oos.writeFloat(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
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
      oos.writeInt(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
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
      oos.writeLong(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
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

    if (! (value instanceof Boolean) && ! (value instanceof Character)
        && ! (value instanceof Number) && ! (value instanceof String)
        && ! (value instanceof byte[]))
      throw new MessageFormatException("Can't write non Java primitive type"
                                       + " as a bytes array.");
    try {
      oos.writeObject(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
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
      oos.writeShort(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void writeString(String value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't write a value as the"
                                             + " message body is read-only.");
    try {
      oos.writeUTF(value);
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while writing the value: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
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
      return ois.readBoolean();
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
      return ois.readByte();
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
      return ois.readShort();
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
      return ois.readChar();
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
      return ois.readInt();
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
      return ois.readLong();
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
      return ois.readFloat();
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
      return ois.readDouble();
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
  public int readBytes(byte[] bytes) throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    int counter = 0;

    try {
      for (int i = 0; i < bytes.length; i ++) {
        bytes[i] = ois.readByte();
        counter++;
      }
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof IOException) {
        jE = new JMSException("Could not read the bytes array: " + e);
        jE.setLinkedException(e);
        throw jE;
      }
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
  public String readString() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return ois.readUTF();
    }
    catch (Exception e) {
      JMSException jE = null;
      if (e instanceof EOFException)
        jE = new MessageEOFException("Unexpected end of bytes array: " + e);
      else if (e instanceof IOException)
        jE = new JMSException("Could not read the bytes array: " + e);
      else
        jE = new JMSException("Exc: " + e);
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
  public Object readObject() throws JMSException
  {
    if (WObody)
      throw new MessageNotReadableException("Can't read the message body as"
                                            + " it is write-only.");
    try {
      return ois.readObject();
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
   * @exception JMSException  In case of an error while closing the streams.
   */
  public void reset() throws JMSException
  {
    try {
      oos.flush();
      ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())); 
  
      RObody = true;
      WObody = false;
    }
    catch (IOException ioE) {
      JMSException jE = new JMSException("Error while resetting the streams: "
                                         + ioE);
      jE.setLinkedException(ioE);
    }
  }

  /**
   * Method actually serializing the wrapped map into the MOM message.
   *
   * @exception Exception  If an error occurs while serializing.
   */
  protected void prepare() throws Exception
  {
    oos.flush();
    momMsg.setStream(bos);
  }

  /** 
   * Method actually deserializing the MOM body as the wrapped map.
   *
   * @exception Exception  If an error occurs while deserializing.
   */
  protected void restore() throws Exception
  {
    ois = new ObjectInputStream(momMsg.getStream());
    RObody = true;
    WObody = false;
  }
}

