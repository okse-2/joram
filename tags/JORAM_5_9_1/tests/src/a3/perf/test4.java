/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001-2003 ScalAgent Distributed Technologies
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
 * Initial developer(s):ScalAgent D.T.
 * Contributor(s): 
 */


package a3.perf;

import fr.dyade.aaa.agent.*;
import framework.TestCase;

public class test4 extends TestCase {
  public static Object lock;
  public static int round = 0;
  
  public static int nbRound;
  public static int nbMsgPerRound;
  public static int ballast;
  
  public static Receiver4 receiver;
  
  public static void main(String args[]) {
    new test4().runTest(args);
  }

  public test4() {
    super();
  }

  protected void setUp() throws Exception {
    nbRound = Integer.getInteger("nbRound", 20).intValue();
    nbMsgPerRound = Integer.getInteger("nbMsgPerRound", 1000).intValue();
    ballast = Integer.getInteger("ballast", 50000).intValue();

    timeout = (long) (100 * nbRound * nbMsgPerRound);
    timeout = Long.getLong("timeout", timeout).longValue();

    lock = new Object();

    receiver = new Receiver4();
    receiver.deploy();

    new Sender().start();
  }

  class Sender extends Thread {
    long min = Long.MAX_VALUE;
    long max = 0L;
    long avg = 0L;
    
    public void run() {
      for (int i=0; i<nbRound; i++) {
        long start = System.currentTimeMillis();
        Token4 token = new Token4(ballast);
        for (int j=nbMsgPerRound; j>=0; j--) {
          token.bounce = j;
          Channel.sendTo(receiver.getId(), token);
        }

        synchronized(lock) {
          try {
            if (test4.round < i) lock.wait();
          } catch (InterruptedException exc) {
            // TODO Auto-generated catch block
            exc.printStackTrace();
          }
        }

        long end = System.currentTimeMillis();
        long current = (end-start)*1000 /nbMsgPerRound;
        if (current < min) min = current;
        if (current > max) max = current;
        avg += current;
        
        System.out.println("Round#" + i + "=" + current);
      }
      
      writeIntoFile("Test4: " + nbRound + '/' + nbMsgPerRound + '/' + ballast + " -> " + min + '/' + max + '/' + (avg/nbRound));
      writeIntoFile("Test4: " + AgentServer.getTransaction().toString());
      
      try {
        Thread.sleep(2000L);
      } catch (InterruptedException exc) {
        // TODO Auto-generated catch block
        exc.printStackTrace();
      }
      
      TestCase.endTest();
    }
  }
}

class Token4 extends Notification {
  public int bounce;
  public int[] ballast;

  public Token4(int size) {
    this.ballast = new int[size];
  }

  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",bounce=").append(bounce);
    output.append(')');

    return output;
  }
}

class Receiver4 extends Agent {
  Receiver4() {
    super();
  }

  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof Token4) {
      if (((Token4) not).bounce == 0)
        synchronized(test4.lock) {
          test4.round += 1;
          test4.lock.notifyAll();
        }
    } else {
      super.react(from, not);
    }
  }
}
