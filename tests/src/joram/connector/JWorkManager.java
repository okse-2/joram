/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): BADOLLE Fabien ( ScalAgent Distributed Technologies )
 * Contributor(s):
 */
package joram.connector;

import java.util.LinkedList;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;

public class JWorkManager implements WorkManager {

  protected LinkedList workList = new LinkedList();

  protected static int poolnumber = 0;
  protected static int threadnumber = 0;

  protected int maxThread;
  protected int minThread;
  protected int nbRunThread; 
  protected int freeThreads;  
  protected long waitingTime; 

  protected boolean valid = true; 

  public JWorkManager(int minsz, int maxsz, long threadwait) {
    minThread = minsz;
    maxThread = maxsz;
    waitingTime = threadwait * 1000L;
    poolnumber++;
    for (nbRunThread = 0; nbRunThread < minsz; nbRunThread++) {
      PoolThread st = new PoolThread(this, threadnumber++, poolnumber);
      st.start();
    }
  }

  public void doWork(Work work) throws WorkException {}

  public void doWork(Work work, long timeout, ExecutionContext ectx, WorkListener listener) throws WorkException {
    work.run();
  }
  public long startWork(Work work) throws WorkException {
    return 0;
  }

  public long startWork(Work work, long timeout, ExecutionContext ectx, WorkListener listener) throws WorkException {
    return 0;
  }

  public void scheduleWork(Work work) throws WorkException {
    scheduleWork(work, INDEFINITE, null, null);
  }

  public void scheduleWork(final Work work, long timeout, ExecutionContext ectx, WorkListener listener) throws WorkException {
    synchronized (workList) {
      workList.add(work);
      if (nbRunThread < maxThread && workList.size() > freeThreads) {
        nbRunThread++;
        PoolThread st = new PoolThread(this, threadnumber++, poolnumber);
        st.start();
      } else {
        workList.notify();
      }
    }
  }

  class PoolThread extends Thread {

    private JWorkManager mgr;
    private int number;

    PoolThread(JWorkManager m, int num, int wm) {
      mgr = m;
      number = num;
      setName("PoolThread-" + wm + "/" + num);
    }

    public void run() {
      while (true) {
        try {
          Work run = null;
          boolean haswait = false;
          synchronized (workList) {
            while (workList.isEmpty()) {
              if ((haswait && freeThreads > minThread) || !valid) {
                nbRunThread--;
                throw new InterruptedException("Thread ending");
              }
              try {
                freeThreads++;
                workList.wait(waitingTime);
                freeThreads--;
                haswait = true;
              } catch (InterruptedException e) {
                freeThreads--;
                nbRunThread--;
                throw e;
              }
            }
            run = (Work) workList.removeFirst();
          }
          doWork(run,INDEFINITE , null,null);
        } catch (InterruptedException e) {
          // exit from thread
          return;
        } catch (WorkException e) {
          ConnectorTest1.error(e);
        }
      }
    }
  }
}
    
