/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package com.scalagent.txlog;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NioTxLogFile extends TxLogFile {
  
  private boolean syncOnWrite;
  
  private RandomAccessFile raf;
  
  private FileChannel channel;

  public NioTxLogFile(File file, boolean syncOnWrite) {
    super(file);
    this.syncOnWrite = syncOnWrite;
  }
  
  @Override
  protected void doOpen(File file) throws IOException {
    raf = new RandomAccessFile(file, "rw");
    channel = raf.getChannel();
  }
  
  @Override
  protected long getFileSize() throws IOException {
    return channel.size();
  }
  
  @Override
  protected void setPosition(long position) throws IOException {
    channel.position(position);
  }
  
  @Override
  protected int doWrite(ByteBuffer buf) throws IOException {
    int n = channel.write(buf);
    if (syncOnWrite) {
      channel.force(false);
    }
    return n;
  }
  
  @Override
  protected int doRead(ByteBuffer buf) throws IOException {
    return channel.read(buf);
  }
  
  @Override
  protected long doReadLong() throws IOException {
    return raf.readLong();
  }
  
  @Override
  protected void doClose() throws IOException {
    channel.close();
    raf.close();
    raf = null;
    channel = null;
  }
  
}
