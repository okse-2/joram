/*
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
 */
package fr.dyade.aaa.agent;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.BinaryDump;

/**
 * Class used to receive messages through a stream.
 * <p>
 * Be careful this InputStream is not synchronized.
 */
public abstract class MessageInputStream extends InputStream {
  /**
   * The internal buffer where data is stored. 
   */
  protected byte buf[];

  /**
   * The number of valid bytes in the buffer.
   * <p>
   * The index one greater than the index of the last valid byte in 
   * the buffer. 
   * <p>
   * This value is always in the range <tt>0</tt> through <tt>buf.length</tt>;
   * elements <tt>buf[0]</tt> through <tt>buf[count-1]</tt> contain valid byte
   * data.
   */
  protected int count;

  /**
   * The current position in the buffer. This is the index of the next 
   * character to be read from the <code>buf</code> array. 
   * <p>
   * This value is always in the range <code>0</code> through
   * <code>count</code>. If it is less than <code>count</code>, then
   * <code>buf[pos]</code> is the next byte to be supplied as input;
   * if it is equal to <code>count</code>, then the  next <code>read</code>
   * or <code>skip</code> operation will require more bytes to be read from
   * the contained  input stream.
   */
  protected int pos;

  protected boolean compressedFlows = false;
  
  /**
   * Default logger for MessageInputStream.
   */
  protected static Logger logmon = null;
  
  /**
   * Returns default logger for MessageInputStream.
   * @return default logger for MessageInputStream.
   */
  protected static Logger getLogger() {
    if (logmon == null)
      logmon = Debug.getLogger("fr.dyade.aaa.agent.MessageInputStream");
    return logmon;
  }

  /**
   * Creates a <code>MessageInputStream</code>.
   */
  MessageInputStream() {}

  /**
   * Reads the next byte of data from the input stream. The value byte is
   * returned as an <code>int</code> in the range <code>0</code> to
   * <code>255</code>. If no byte is available because the end of the stream
   * has been reached, the value <code>-1</code> is returned. This method
   * blocks until input data is available, the end of the stream is detected,
   * or an exception is thrown.
   * <p>
   * Subclass must provide an implementation of this method.
   *
   * @return     the next byte of data, or <code>-1</code> if the end of the
   *             stream is reached.
   * @exception  IOException  if an I/O error occurs.
   */
  public abstract int read() throws IOException;

  /**
   * Reads some number of bytes from the input stream and stores them into
   * the buffer array <code>b</code>. The number of bytes actually read is
   * returned as an integer.  This method blocks until input data is
   * available, end of file is detected, or an exception is thrown.
   * <p>
   * The <code>read(b)</code> method for class <code>InputStream</code>
   * has the same effect as: <code>read(b, 0, b.length)</code>
   *
   * @param      b   the buffer into which the data is read.
   * @return     the total number of bytes read into the buffer, or
   *             <code>-1</code> is there is no more data because the end of
   *             the stream has been reached.
   * @exception  IOException  If the first byte cannot be read for any reason
   * other than the end of the file, if the input stream has been closed, or
   * if some other I/O error occurs.
   * @exception  NullPointerException  if <code>b</code> is <code>null</code>.
   */
  public final int read(byte b[]) throws IOException {
    return read(b, 0, b.length);
  }

  /**
   * Reads up to <code>len</code> bytes of data from the input stream into
   * an array of bytes.  An attempt is made to read as many as
   * <code>len</code> bytes, but a smaller number may be read.
   * The number of bytes actually read is returned as an integer.
   * <p>
   * This method blocks until input data is available, end of file is
   * detected, or an exception is thrown.
   *
   * @param      b     the buffer into which the data is read.
   * @param      off   the start offset in array <code>b</code>
   *                   at which the data is written.
   * @param      len   the maximum number of bytes to read.
   * @return     the total number of bytes read into the buffer, or
   *             <code>-1</code> if there is no more data because the end of
   *             the stream has been reached.
   * @exception  IOException If the first byte cannot be read for any reason
   * other than end of file, or if the input stream has been closed, or if
   * some other I/O error occurs.
   * @exception  NullPointerException If <code>b</code> is <code>null</code>.
   * @exception  IndexOutOfBoundsException If <code>off</code> is negative, 
   * <code>len</code> is negative, or <code>len</code> is greater than 
   * <code>b.length - off</code>
   */
  public abstract int read(byte b[], int off, int len) throws IOException;

  /**
   * Reads a short directly from the buffer.
   * Be careful, the buffer must contain enough data to read the short.
   *
   * @return      the short.
   */
  protected final short readShort() {
    return (short) (((buf[pos++] & 0xFF) <<  8) + (buf[pos++] & 0xFF));
  }

  /**
   * Reads an int directly from the buffer.
   * Be careful, the buffer must contain enough data to read the int.
   *
   * @return      the int.
   */
  protected final int readInt() {
    return ((buf[pos++] & 0xFF) << 24) + ((buf[pos++] & 0xFF) << 16) +
      ((buf[pos++] & 0xFF) <<  8) + ((buf[pos++] & 0xFF) <<  0);
  }

  /**
   * Reads the protocol header from this output stream.
   * Be careful, the buffer must contain enough data to read the short.
   * This method must be overloaded in subclass.
   */
  abstract protected void readHeader() throws IOException;

  /**
   * Reads the message header data from the buffer.
   *
   * @param msg The message to complete.
   */
  protected final void readMessageHeader(Message msg) throws IOException {
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "readMessageHeader()");
    
    readFully(Message.LENGTH);
    
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "readMessageHeader-1 : " + BinaryDump.toHex(buf, pos, Message.LENGTH));
    
    // Reads sender's AgentId
    msg.from = new AgentId(readShort(), readShort(), readInt());
    // Reads adressee's AgentId
    msg.to = new AgentId(readShort(), readShort(), readInt());
    // Reads source server id of message
    msg.source = readShort();
    // Reads destination server id of message
    msg.dest = readShort();
    // Reads stamp of message
    msg.stamp = readInt();
    
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "readMessageHeader returns");
  }

  /**
   * Reads the message from this input stream.
   *
   * @return the incoming message.
   */
  protected final Message readMessage() throws Exception {
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "readMessage()");
    
    readHeader();

    Message msg = Message.alloc();
    readMessageHeader(msg);

    byte opt = buf[pos++];

    if (opt != Message.NULL) {
      // Reads notification object
      ObjectInputStream ois = null;
      if (compressedFlows) {
        readFully(4);
        int length = readInt();
        
        if (getLogger().isLoggable(BasicLevel.DEBUG))
          getLogger().log(BasicLevel.DEBUG, "readMessage - length=" + length);
        
        byte[] buf = new byte[length];
        int n = 0;
        do {
          int count = read(buf, n, length - n);
          if (count < 0) throw new EOFException();
          n += count;
        } while (n < length);
        ois = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(buf)));
      } else {
        ois = new ObjectInputStream(this);
      }
      
      if (getLogger().isLoggable(BasicLevel.DEBUG))
        getLogger().log(BasicLevel.DEBUG, "readMessage - 2");

      msg.not = (Notification) ois.readObject();
      
      if (getLogger().isLoggable(BasicLevel.DEBUG))
        getLogger().log(BasicLevel.DEBUG, "readMessage - 3");
      
      if (msg.not.expiration > 0)
        msg.not.expiration += System.currentTimeMillis();
      msg.optFromByte(opt);
      msg.not.detached = false;
      if (!compressedFlows)
        // Skips the remaining TC_RESET byte
        read();
      
      if (getLogger().isLoggable(BasicLevel.DEBUG))
        getLogger().log(BasicLevel.DEBUG, "readMessage - 4");
    } else {
      msg.not = null;
    }

    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "readMessage returns");
    
    return msg;
  }

  /**
   * Reads length bytes of data from the input stream. This method returns
   * when length bytes are available or if end of stream is reached.
   */
  protected abstract void readFully(int length) throws IOException;
}
