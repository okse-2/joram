package joram.shell.mom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
