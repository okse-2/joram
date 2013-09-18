/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package joram.shell.mom;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Collection;

import framework.TestCase;

public class MOMCommandsTests extends TestCase {

  private static final long TIMEOUT = 10000;
  
  private final static short SID = 0;
  
  /* Communication file*/
  private final static String FILENAME = "joram.shell.mom.tests";
  private final static String LOCK_FILE = "joram.shell.mom.lock";
   
  private File testFile, lock;

  
  public MOMCommandsTests() {
    super();
    testFile = new File(FILENAME);
    if(testFile.exists()) testFile.delete();
    lock = new File(LOCK_FILE);
    if(lock.exists()) lock.delete();
  }
  
  public void run() {
    try {
      startAgentServer(SID);
      System.out.println("Waiting for the test to complete.");
      while(lock.exists() || !testFile.exists())
        Thread.sleep(100);
      System.out.println("Test completed. Parsing results...");

      ObjectInputStream oif = new ObjectInputStream(new FileInputStream(testFile));

      Collection<Exception> collec = (Collection<Exception>) oif.readObject();
      for(Exception exc : collec)
        addFailure(exc);
      
      oif.close();

      System.out.println("Stop.");
   } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      stopAgentServer(SID);
      System.out.println("Server stopped.");
      endTest();
      System.out.println("Finished.");
    }
  }
  
  public static void main(String[] args) {
    new MOMCommandsTests().run();
  }
}
