/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France-Telecom R&D
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
 */
package org.objectweb.joram.mom.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.CallbackNotification;
import fr.dyade.aaa.common.Debug;

public class MemoryController {
  
  public static Logger logger = Debug.getLogger(MemoryController.class.getName());
  
  private static MemoryController singleton = new MemoryController();
  
  public static MemoryController getMemoryController() {
    return singleton;
  }
  
  private int memorySizeMax;
  
  private AtomicLong memorySize;
  
  private List<Runnable> memoryCallbacks;
  
  private MemoryController() {
    memorySize = new AtomicLong();
    memoryCallbacks = new ArrayList<Runnable>();
  }
  
  public int getMemorySizeMax() {
    return memorySizeMax;
  }

  public void setMemorySizeMax(int memorySizeMax) {
    this.memorySizeMax = memorySizeMax;
  }

  public void add(int delta) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MemoryController.add(" + delta + ')');
    long currentSize = memorySize.addAndGet(delta);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "memorySize=" + memorySize.get());
    if (delta < 0) {
      checkMemory(currentSize);
    }
  }
  
  private void checkMemory(long currentSize) {
    if (currentSize <= memorySizeMax) {
      synchronized (memoryCallbacks) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Run memoryCallbacks: " + memoryCallbacks.size());
        if (memoryCallbacks.size() > 0) {
          for (Runnable callback : memoryCallbacks) {
            callback.run();
          }
          memoryCallbacks.clear();
        }
      }
    }
  }
  
  public void checkMemory(CallbackNotification cn) {
    Runnable callback = cn.getCallback();
    if (callback == null) return;
    
    if (memorySize.get() > memorySizeMax) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Add memory callback");
      Runnable memoryCallback = new MemoryControlCallback(callback);
      synchronized (memoryCallbacks) {
        memoryCallbacks.add(memoryCallback);
        checkMemory(memorySize.get());
      }
      cn.setCallback(memoryCallback);
    }
  }
  
  public long getUsedMemorySize() {
    return memorySize.get();
  }
  
  static class MemoryControlCallback implements Runnable {
    
    private Runnable callback;
    
    private int latch;
    
    public MemoryControlCallback(Runnable callback) {
      this.callback = callback;
      latch = 2;
    }
    
    public synchronized void run() {
      latch--;
      if (latch == 0) {
        callback.run();
      }
    }
    
  }

}
