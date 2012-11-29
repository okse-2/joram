/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2009 ScalAgent Distributed Technologies
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
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.perfs;

import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.common.Configuration;

/**
 * See build for more detail : Class use with more option
 *
 */
public class Test6 extends BaseTest {
  static int NbSender = 4;
  static int NbReceiver = 4;
  static int NbDestination = 2;

  static int NbRound = 100;
  static int NbMsgPerRound = 100;
  static int MsgSize = 100;

  static Destination dest[] = null;
  static ConnectionFactory cf = null;

  static boolean MsgTransient = false;

  static boolean SubDurable = false;

  static boolean multiThreadSync = false;

  static int multiThreadSyncDelay = 1;

  static boolean multiCnx = false;

  static int queueMessageReadMax;

  static int topicAckBufferMax;

  boolean implicitAck;
  
  static boolean dupsOk;

  public static void main (String args[]) throws Exception {
    new Test6().run();
  }

  public void run(){
    try{
      System.out.println("server start");
      if (! Boolean.getBoolean("ServerOutside"))
        startServer();
      writeIntoFile("=================== start new test =================================");
      String baseclass = "joram.perfs.ColocatedBaseTest";
      baseclass = System.getProperty("BaseClass", baseclass);

      NbSender = Integer.getInteger("NbSender", NbSender).intValue();
      NbReceiver = Integer.getInteger("NbReceiver", NbReceiver).intValue();
      NbDestination = Integer.getInteger("NbDestination", NbDestination).intValue();
      NbRound = Integer.getInteger("NbRound", NbRound).intValue();
      NbMsgPerRound = Integer.getInteger("NbMsgPerRound", NbMsgPerRound).intValue();
      MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();
      MsgTransient = Boolean.getBoolean("MsgTransient");
      SubDurable = Boolean.getBoolean("SubDurable");
      multiThreadSync = Boolean.getBoolean("multiThreadSync");
      multiThreadSyncDelay = Integer.getInteger("multiThreadSyncDelay", 1).intValue();
      multiCnx = Boolean.getBoolean("multiCnx");
      dupsOk = Boolean.getBoolean("dupsOk");
      queueMessageReadMax = Integer.getInteger("queueMessageReadMax", 1).intValue();
      topicAckBufferMax = Integer.getInteger("topicAckBufferMax", 0).intValue();
      implicitAck = Boolean.getBoolean("implicitAck");

      if (multiCnx && multiThreadSync) 
        throw new Exception("Can't test both multiCnx and multiThreadSync");

      AdminConnect(baseclass);

      String destclass = System.getProperty("Destination", "org.objectweb.joram.client.jms.Queue");
      dest = new Destination[NbDestination];
      for (int i=0; i<NbDestination; i++) {
        dest[i] = createDestination(destclass);
        dest[i].setFreeReading();
        dest[i].setFreeWriting();
      }

      User user = User.create("anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

      writeIntoFile("----------------------------------------------------");
      writeIntoFile("Transaction: " + Configuration.getProperty("Transaction"));
      writeIntoFile("Engine: " + Configuration.getProperty("Engine"));
      writeIntoFile("baseclass: " + baseclass +
                    ", Transacted=" + Configuration.getBoolean("Transacted"));
      writeIntoFile("Destination: " + destclass);
      writeIntoFile("Message: transient=" + MsgTransient);
      writeIntoFile("Subscriber: durable=" + SubDurable + 
                    ", dupsOk=" + dupsOk);
      writeIntoFile("            queueMessageReadMax=" + queueMessageReadMax +
                    ", topicAckBufferMax=" + topicAckBufferMax);
      writeIntoFile("Subscriber:       implicitAck=" + implicitAck);
      writeIntoFile("NbSender=" + NbSender +
                    ", NbReceiver=" + NbReceiver +
                    ", NbDestination=" + NbDestination);
      writeIntoFile("NbRound=" + NbRound + 
                    ", NbMsgPerRound=" + NbMsgPerRound +
                    ", MsgSize=" + MsgSize);
      writeIntoFile("multiThreadSync=" + multiThreadSync + 
                    ", multiThreadSyncDelay=" + multiThreadSyncDelay);
      writeIntoFile("multiCnx=" + multiCnx);
      writeIntoFile("----------------------------------------------------");


      ConnectionFactory cf =  createConnectionFactory(baseclass);
      Connection cnx1 = cf.createConnection();

      ConnectionFactory cf2 = createConnectionFactory(baseclass);
      ((org.objectweb.joram.client.jms.ConnectionFactory)cf2).getParameters().multiThreadSync = multiThreadSync;
      ((org.objectweb.joram.client.jms.ConnectionFactory)cf2).getParameters().multiThreadSyncDelay = multiThreadSyncDelay;
      ((org.objectweb.joram.client.jms.ConnectionFactory)cf2).getParameters().multiThreadSyncThreshold = NbSender/2;

      Connection cnx2 = null;
      if (! multiCnx) {
        cnx2 = cf2.createConnection();
        cnx2.start();
      }
      
      Receiver receiver[] = new Receiver[NbReceiver];
      for (int i=0; i<NbReceiver; i++) {
        receiver[i] = new Receiver(cnx1, dest[i%NbDestination]);
      }

      Lock lock = new Lock(NbSender);
      Sender sender[] = new Sender[NbSender];
      for (int i=0; i<NbSender; i++) {
        if (multiCnx) {
          cnx2 = cf2.createConnection();
          cnx2.start();
        }
        sender[i] = new Sender(cnx2, dest[i%NbDestination],
                               NbRound, NbMsgPerRound, MsgSize,
                               lock, MsgTransient);
      }

      cnx1.start();

      long t1 = System.currentTimeMillis();
      for (int i=0; i<NbSender; i++) {
        new Thread(sender[i]).start();
      }

      lock.ended();
      long t2 = System.currentTimeMillis();

      long dt1 = 0L;
      for (int i=0; i<NbSender; i++) {
        dt1 += sender[i].dt1;
      }

      long dt3 = 0L; long dt4 = 0L;
      for (int i=0; i<NbReceiver; i++) {
        dt3 += (receiver[i].last - receiver[i].start);
        dt4 += receiver[i].travel;
      }

      long NbMsg = NbMsgPerRound * NbRound * NbSender;

      writeIntoFile("----------------------------------------------------");
      writeIntoFile("| sender dt(us)=" +  ((dt1 *1000L)/(NbMsg)));
      writeIntoFile("| receiver dt(us)=" + ((dt3 *1000L)/(NbMsg)));
      writeIntoFile("| Mean travel time (ms)=" + (dt4/(NbMsg)));
      writeIntoFile("| Mean time = " +
                    ((t2-t1)*1000L) / (NbMsg) + "us per msg, " +
                    ((1000L * (NbMsg)) / (t2-t1)) + "msg/s");     
      int median = 0;
      for (int s = 0; s < sender.length; s++)
        median += getMedian(sender[s].arrayList.toArray());
      writeIntoFile("| Median = " + median/sender.length + "msg/s");
      writeIntoFile("----------------------------------------------------");
    } catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      fr.dyade.aaa.agent.AgentServer.stop();
      endTest(); 
    }
  }

  public static int getMedian(Object[] array) {
    final int EVEN_NUMBER_OF_ELEMENTS = 0;
    Arrays.sort(array);
    int result = array.length % 2;
    int median = 0;
    // Definition: Median is the Middle number in an odd number of entries array
    // Median is the avg of the two number in the center of an even number array
    if (result == EVEN_NUMBER_OF_ELEMENTS) {
      int rightNumber = array.length / 2;
      int leftNumber = rightNumber - 1;
      median = ((Integer)array[rightNumber] + (Integer)array[leftNumber]) / 2;
    } else { // Odd number of items, choose the center one
      int rightNumber = array.length / 2;
      median = (Integer) array[rightNumber];
    }
    return median;
  }

}
class Lock {
  int count;

  Lock(int count) {
    this.count = count;
  }

  synchronized void exit() {
    count -= 1;
    notify();
  }

  synchronized void ended() {
      while (count != 0) {
	  try {
	      wait();
	  } catch (InterruptedException exc) {
	  }
    }
  }
}
