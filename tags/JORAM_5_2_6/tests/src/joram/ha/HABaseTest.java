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
package joram.ha;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import framework.TestCase;

/**
 * Test HA servers with external client either with a queue and a topic. The test
 * starts 3 replicas then successively kills and restarts each  during sending and
 * receiving messages. The test verifies that none message is lost. 
 */
public class HABaseTest extends TestCase {
  public static PrintWriter pw = null;
  
  public HABaseTest() {
    super();
    File file;
    try {
      file = File.createTempFile("test", ".txt", new File("."));
      pw = new PrintWriter(new FileOutputStream(file), true);
    } catch (IOException exc) {
      exc.printStackTrace();
      System.exit(0);
    }
  }

  public static Process startHAServer(short sid,
                                      File dir,
                                      String rid) throws Exception {
    String[] jvmargs = new String[] {
      "-DnbClusterExpected=2",
      "-DTransaction=fr.dyade.aaa.util.NullTransaction"};

    String[] args = new String[] { rid };

    Process p =  getAdmin().execAgentServer(sid, dir,
                                            jvmargs,
                                            "fr.dyade.aaa.agent.AgentServer",
                                            args);
    getAdmin().closeServerStream(p);

    return p;
  }

  public static class Killer extends Thread {
    private Process process;
    private int index;
    private long pause;

    Killer(Process process, int index, long pause) {
      this.process = process;
      this.index = index;
      this.pause = pause;
    }

    public void run() {
      try {
        Thread.sleep(pause);
      } catch (InterruptedException exc) {
      }
      pw.println("Kill replica " + index);
      process.destroy();
    }
  }
}
