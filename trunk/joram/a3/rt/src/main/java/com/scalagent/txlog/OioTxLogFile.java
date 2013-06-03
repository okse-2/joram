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

public class OioTxLogFile extends TxLogFile {
  
  private String mode;
  
  private RandomAccessFile raf;
  
  public OioTxLogFile(File file, boolean syncOnWrite) {
    super(file);
    if (syncOnWrite) {
      mode = "rwd";
    } else {
      mode = "rw";
    }
  }

  @Override
  protected void doOpen(File file) throws IOException {
    raf = new RandomAccessFile(file, mode);
  }

  @Override
  protected long getFileSize() throws IOException {
    return raf.length();
  }

  @Override
  protected void setPosition(long filePointer) throws IOException {
    raf.seek(filePointer);
  }

  @Override
  protected int doWrite(ByteBuffer buf) throws IOException {
    int len = buf.limit();
    raf.write(buf.array(), 0, len);
    return len;
  }

  @Override
  protected int doRead(ByteBuffer buf) throws IOException {
    int len = raf.read(buf.array(), 0, buf.limit());
    buf.position(len);
    return len;
  }

  @Override
  protected long doReadLong() throws IOException {
    return raf.readLong();
  }

  @Override
  protected void doClose() throws IOException {
    raf.close();
    raf = null;
  }

}
