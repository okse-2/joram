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
package connector;



import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
         
import java.util.LinkedList;



public class JWorkManager implements WorkManager {

    protected LinkedList workList = new LinkedList();

    protected static int poolnumber = 0;
    protected static int threadnumber = 0;

    protected int maxpoolsz;
    protected int minpoolsz;
    protected int poolsz; // current size of thread pool
    protected int freeThreads;  // free threads ready to work
    protected long waitingTime; // in millisec

    protected boolean valid = true; // set to false when WorkManager is removed.

    protected static final long FEW_MORE_SECONDS = 3000;


   
    public JWorkManager(int minsz, int maxsz, long threadwait) {
        minpoolsz = minsz;
        maxpoolsz = maxsz;
        waitingTime = threadwait * 1000L;
	poolnumber++;
	for (poolsz = 0; poolsz < minsz; poolsz++) {
            PoolThread pt = new PoolThread(this, threadnumber++, poolnumber);
            pt.start();
        }
    }
    
    public void doWork(Work work) throws WorkException {
  
    }

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
            if (poolsz < maxpoolsz && workList.size() > freeThreads) {
		poolsz++;
                PoolThread pt = new PoolThread(this, threadnumber++, poolnumber);
                pt.start();
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
			 if ((haswait && freeThreads > minpoolsz) || !valid) {
			     poolsz--;
			     throw new InterruptedException("Thread ending");
			 }
			 try {
			     freeThreads++;
			     workList.wait(waitingTime);
			     freeThreads--;
			     haswait = true;
			 } catch (InterruptedException e) {
			     freeThreads--;
			     poolsz--;
			     throw e;
			 }
		     }
		     run = (Work) workList.removeFirst();
		     workList.notify();
		     doWork(run,INDEFINITE , null,null);
		 }
	     } catch (InterruptedException e) {
		 System.out.println("InterruptedException");
		 return;
	     } catch (WorkException e) {
		    System.out.println("WorkException");
	     }
	 }
     }
     
 }




}
    
