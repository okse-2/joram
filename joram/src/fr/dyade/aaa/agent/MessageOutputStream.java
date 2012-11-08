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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * Class used to send messages through a stream.
 * <p>
 * This OutputStream is a combination between a ByteArrayOutputStream and a
 * BufferedOutputStream. It allows the replacement of the underlying stream and
 * the serialization of object through an internal ObjectOutputStream.
 * <p>
 * Be careful this OutputStream is not synchronized.
 */
public abstract class MessageOutputStream extends OutputStream {
  /**
   * The internal ObjectOutputStream needed to serialize the notification.
   */
  protected ObjectOutputStream oos;

  /**
   * The internal buffer where data is stored. 
   */
  protected byte buf[];

  /**
   * The number of valid bytes in the buffer.
   */
  protected int count;

  protected boolean compressedFlows = false;
  
  /**
   * Default logger for MessageOutputStream.
   */
  protected static Logger logmon = null;
  
  /**
   * Returns default logger for MessageOutputStream.
   * @return Default logger for MessageOutputStream.
   */
  protected static Logger getLogger() {
    if (logmon == null)
      logmon = Debug.getLogger("fr.dyade.aaa.agent.MessageOutputStream");
    return logmon;
  }
  
  // ObjectStream constants needed to reinitialize the ObjectOutputStream.
  private static final byte STREAM_MAGIC1 =
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF);
  private static final byte STREAM_MAGIC2 =
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF);
  private static final byte STREAM_VERSION1 =
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF);
  private static final byte STREAM_VERSION2 =
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF);

  /**
   *  Creates a new output stream to write data to an unspecified 
   * underlying output stream through a buffer with default size.
   */
  public MessageOutputStream() throws IOException {
    this(8192);
  }

  /**
   *  Creates a new output stream to write data to an unspecified 
   * underlying output stream through a buffer with specified size.
   *
   * @param size the buffer size.
   * @exception IllegalArgumentException if size is less than 0.
   * @exception IOException if the internal ObjectOutputStream cannot be
   *		correctly initialized.
   */
  public MessageOutputStream(int size) throws IOException {
    if (size <= 0)
      throw new IllegalArgumentException("Buffer size <= 0");
    buf = new byte[size];

    if (! compressedFlows)
      oos = new ObjectOutputStream(this);
    count = 0;
  }

  /**
   * Writes the specified byte to this output stream. 
   *
   * @param      b   the byte to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  public abstract void write(int b) throws IOException;

  /**
   * Writes <code>b.length</code> bytes to this output stream. 
   * <p>
   * This method calls its <code>write</code> method of three arguments with
   * the  arguments <code>b</code>, <code>0</code>, and <code>b.length</code>.
   *
   * @param      b   the data to be written.
   * @exception  IOException  if an I/O error occurs.
   * @see        write(byte[], int, int)
   */
  public final void write(byte b[]) throws IOException {
    write(b, 0, b.length);
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array starting
   * at offset <code>off</code> to this output stream.
   *
   * @param      b     the data.
   * @param      off   the start offset in the data.
   * @param      len   the number of bytes to write.
   * @exception  IOException  if an I/O error occurs.
   */
  public abstract void write(byte b[], int off, int len) throws IOException;

  /**
   * Writes a short directly to the buffer.
   * Be careful, the buffer must be large enough to contain the short.
   *
   * @param      s     the data.
   */
  protected final void writeShort(short s) {
    buf[count++] = (byte) (s >>>  8);
    buf[count++] = (byte) (s >>>  0);
  }

  /**
   * Writes an int directly to the buffer.
   * Be careful, the buffer must be large enough to contain the int.
   *
   * @param      i     the data.
   */
  protected final void writeInt(int i) {
    buf[count++] = (byte) (i >>>  24);
    buf[count++] = (byte) (i >>>  16);
    buf[count++] = (byte) (i >>>  8);
    buf[count++] = (byte) (i >>>  0);
  }

  /**
   * Writes the protocol header to this output stream.
   * This method must be overloaded in subclass.
   */
  abstract protected void writeHeader() throws IOException;

  /**
   * Writes the message header data to the buffer.
   *
   * @param msg The message to write out.
   */
  protected final void writeMessageHeader(Message msg) {
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "writeMessageHeader()");
    
    // Writes sender's AgentId
    writeShort(msg.from.from);
    writeShort(msg.from.to);
    writeInt(msg.from.stamp);
    // Writes adressee's AgentId
    writeShort(msg.to.from);
    writeShort(msg.to.to);
    writeInt(msg.to.stamp);
    // Writes source server id of message
    writeShort(msg.source);
    // Writes destination server id of message
    writeShort(msg.dest);
    // Writes stamp of message
    writeInt(msg.stamp);
    
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "writeMessageHeader returns");
  }

  /**
   * Writes a message to this output stream.
   * This method can be overloaded in subclass.
   *
   * @param msg  The message to write out.
   * @param time The current time in milliseconds, this parameter
   *             is used to the handling of notification expiration.
   */
  protected final void writeMessage(Message msg,
                                    long time) throws IOException {
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "writeMessage()");
    
    // Writes the protocol specific data.
    writeHeader();

    if (msg != null) {
      // Writes the message header data.
      writeMessageHeader(msg);

      if (msg.not == null) {
        if (getLogger().isLoggable(BasicLevel.DEBUG))
          getLogger().log(BasicLevel.DEBUG, "writeMessage - 1");
        
        buf[count++] = Message.NULL;
        flush();
      } else {
        if (getLogger().isLoggable(BasicLevel.DEBUG))
          getLogger().log(BasicLevel.DEBUG, "writeMessage - 2");
        
        // Writes notification attributes
        buf[count++] = msg.optToByte();

        try {
          if (msg.not.expiration > 0)
            msg.not.expiration -= time;

          // Writes a serializable object to this output stream.
          if (compressedFlows) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipos = new GZIPOutputStream(baos);
            
            oos = new ObjectOutputStream(gzipos);
            oos.writeObject(msg.not);

            // Be careful, the reset writes a TC_RESET byte
            oos.reset();
            // The OOS flush call the flush of this output stream.
            oos.flush();
            gzipos.finish();
            gzipos.flush();
            
            if (getLogger().isLoggable(BasicLevel.DEBUG))
              getLogger().log(BasicLevel.DEBUG, "writeNotification - size=" + baos.size());

            writeInt(baos.size());
            baos.writeTo(this);
            
            flush();
            
            oos = null;
          } else {
            // Write the STREAM_MAGIC constant
            buf[count++] = STREAM_MAGIC1;
            buf[count++] = STREAM_MAGIC2;
            // Write the STREAM_VERSION constant
            buf[count++] = STREAM_VERSION1;
            buf[count++] = STREAM_VERSION2;
            oos.writeObject(msg.not);

            // Be careful, the reset writes a TC_RESET byte
            oos.reset();
            // The OOS flush call the flush of this output stream.
            oos.flush();
          }
        } finally {
          if ((msg.not != null) && (msg.not.expiration > 0))
            msg.not.expiration += time;
        }
      }
    } else {
      flush();
    }
    
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "writeMessage returns");
  }
}
