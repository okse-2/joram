/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 20010 ScalAgent Distributed Technologies
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
 * Initial developer(s): (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */

package framework;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import org.objectweb.joram.client.jms.ConnectionMetaData;

/**
 * Utility functions for all test cases.
 */
public class BaseTestCase {
  private static BaseTestCase current = null;

  protected String name;
  protected boolean summary = true;
  protected List failures;
  protected List errors;
  protected List exceptions;
  protected PrintWriter writer = null;
  protected boolean saveErrors = true;
  
  public BaseTestCase() {
    String id = System.getProperty("framework.TestCase.TestId");
    summary = new Boolean(System.getProperty("framework.TestCase.Summary", "true")).booleanValue();
    String outfile = System.getProperty("framework.TestCase.OutFile");
    saveErrors = new Boolean(System.getProperty("framework.TestCase.SaveFailedTests", "true")).booleanValue();

    try {
      writer = new PrintWriter(new FileWriter(outfile, true));
    } catch (IOException exc) {
      throw new Error("Can't create test: " + getClass().getName());
    }

    if (id == null)
      this.name = getClass().getName();
    else
      this.name = getClass().getName() + '-' + id;
    setCurrent(this);

    // Changes the class path with absolute paths
    try {
      Properties sysprops = System.getProperties();
      String classpath = sysprops.getProperty("java.class.path");
      classpath = getAbsolutePath(classpath);
      sysprops.setProperty("java.class.path", classpath);
    } catch (Exception exc) {
      throw new Error("cannot set absolute classpath");
    }
  }

  public final synchronized void addException(Throwable t) {
    if (exceptions == null) exceptions = new Vector();
    exceptions.add(t);
  }

  /**
   * Adds a failure to the list of failures. The passed in exception
   * caused the failure.
   * The test framework distinguishes between <i>failures</i> and
   * <i>errors</i>. A failure is anticipated and checked for with
   * assertions. Errors are unanticipated problems like an
   * <code>ArrayIndexOutOfBoundsException</code>.
   */
  public final synchronized void addFailure(Throwable t) {
    if (failures == null) failures = new Vector();
    failures.add(t);
  }

  /**
   * Gets the number of detected failures.
   */
  public final synchronized int failureCount() {
    if (failures == null) return 0;
    return failures.size();
  }

  /**
   * Adds an error to the list of errors. The passed in exception
   * caused the error.
   * The test framework distinguishes between <i>failures</i> and
   * <i>errors</i>. A failure is anticipated and checked for with
   * assertions. Errors are unanticipated problems like an
   * <code>ArrayIndexOutOfBoundsException</code>.
   */
  public final synchronized void addError(Throwable t) {
    if (errors == null) errors = new Vector();
    errors.add(t);
  }

  /**
   * Gets the number of detected errors.
   */
  public final synchronized int errorCount() {
    if (errors == null) return 0;
    return errors.size();
  }

  static int asserts = 0;
  
  /**
   * Asserts that a condition is true.
   */
  static public void assertTrue(String message, boolean condition) {
    asserts++;
    if (!condition) fail(message);
  }

  /**
   * Asserts that a condition is true.
   */
  static public void assertTrue(boolean condition) {
    assertTrue(null, condition);
  }

  /**
   * Asserts that a condition is false.
   */
  static public void assertFalse(String message, boolean condition) {
    asserts++;
    if (condition) fail(message);
  }

  /**
   * Asserts that a condition is false.
   */
  static public void assertFalse(boolean condition) {
    assertFalse(null, condition);
  }

  /**
   * Asserts that two objects are equal.
   */
  static public void assertEquals(String message,
                                  Object expected, Object actual) {
    asserts++;
    if (expected == null && actual == null)
      return;
    if (expected != null && expected.equals(actual))
      return;
    failNotEquals(message, expected, actual);
  }

  /**
   * Asserts that two objects are equal.
   */
  static public void assertEquals(Object expected, Object actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two doubles are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(String message,
                                  double expected, double actual,
                                  double delta) {
    asserts++;
    if (Double.isInfinite(expected)) {
      if (!(expected == actual))
        failNotEquals(message, new Double(expected), new Double(actual));
    } else if (!(Math.abs(expected-actual) <= delta))
      // Because comparison with NaN always returns false
      failNotEquals(message, new Double(expected), new Double(actual));
  }

  /**
   * Asserts that two doubles are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(double expected, double actual,
                                  double delta) {
    assertEquals(null, expected, actual, delta);
  }

  /**
   * Asserts that two floats are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(String message,
                                  float expected, float actual,
                                  float delta) {
    asserts++;
    if (Float.isInfinite(expected)) {
      if (!(expected == actual))
        failNotEquals(message, new Float(expected), new Float(actual));
    } else if (!(Math.abs(expected-actual) <= delta))
      failNotEquals(message, new Float(expected), new Float(actual));
  }

  /**
   * Asserts that two floats are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(float expected, float actual,
                                  float delta) {
    assertEquals(null, expected, actual, delta);
  }

  /**
   * Asserts that two longs are equal.
   */
  static public void assertEquals(String message,
                                  long expected, long actual) {
    assertEquals(message, new Long(expected), new Long(actual));
  }

  /**
   * Asserts that two longs are equal.
   */
  static public void assertEquals(long expected, long actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two booleans are equal.
   */
  static public void assertEquals(String message,
                                  boolean expected, boolean actual) {
    assertEquals(message, new Boolean(expected), new Boolean(actual));
  }

  /**
   * Asserts that two booleans are equal.
   */
  static public void assertEquals(boolean expected, boolean actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two bytes are equal.
   */
  static public void assertEquals(String message,
                                  byte expected, byte actual) {
    assertEquals(message, new Byte(expected), new Byte(actual));
  }

  /**
   * Asserts that two bytes are equal.
   */
  static public void assertEquals(byte expected, byte actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two chars are equal.
   */
  static public void assertEquals(String message,
                                  char expected, char actual) {
    assertEquals(message, new Character(expected), new Character(actual));
  }

  /**
   * Asserts that two chars are equal.
   */
  static public void assertEquals(char expected, char actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two shorts are equal.
   */
  static public void assertEquals(String message,
                                  short expected, short actual) {
    assertEquals(message, new Short(expected), new Short(actual));
  }

  /**
   * Asserts that two shorts are equal.
   */
  static public void assertEquals(short expected, short actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two ints are equal.
   */
  static public void assertEquals(String message,
                                  int expected, int actual) {
    assertEquals(message, new Integer(expected), new Integer(actual));
  }

  /**
   * Asserts that two ints are equal.
   */
  static public void assertEquals(int expected, int actual) {
    assertEquals(null, expected, actual);
  }
    
  /**
   * Asserts that two byte[] are equal.
   */
  static public void assertEquals(byte[] tab1, byte[] tab2, int size) {
    asserts++;
    boolean ok=true;
    for(int j=0; j< size && ok==true;j++)
      if(tab1[j]!=tab2[j]){
        failNotEquals(null, tab1[j], tab2[j]);
        ok=false;
      }
  } 

  /**
   * Asserts that an object isn't null.
   */
  static public void assertNotNull(String message, Object object) {
    assertTrue(message, object != null); 
  }

  /**
   * Asserts that an object isn't null.
   */
  static public void assertNotNull(Object object) {
    assertNotNull(null, object);
  }

  /**
   * Asserts that an object is null.
   */
  static public void assertNull(String message, Object object) {
    assertTrue(message, object == null); 
  }

  /**
   * Asserts that an object is null.
   */
  static public void assertNull(Object object) {
    assertNull(null, object);
  }

  /**
   * Asserts that two objects refer to the same object.
   */
  static public void assertSame(String message,
                                Object expected, Object actual) {
    asserts++;
    if (expected == actual)
      return;
    failNotSame(message, expected, actual);
  }

  static boolean isGzip(File file) {
    asserts++;
    String name = file.getName();
    int idx = name.lastIndexOf('.');
    if (idx == -1) return false;
    return (name.substring(idx).equals(".gz"));
  }

  /**
   * Checks that two files are identical, ignoring address mismatch.
   *
   * @param file1	first file
   * @param file2	second file
   * @return		<code>true</code> if files are identical
   */
  public static boolean check(File file1, File file2) {
    InputStream f1 = null;
    InputStream f2 = null;

    try {
      if (isGzip(file1))
        f1 = new BufferedInputStream(
          new GZIPInputStream(
            new FileInputStream(file1)));
      else
        f1 = new BufferedInputStream(new FileInputStream(file1));
      if (isGzip(file2))
        f2 = new BufferedInputStream(
          new GZIPInputStream(
            new FileInputStream(file2)));
      else
        f2 = new BufferedInputStream(new FileInputStream(file2));

      while (true) {
        int c = f1.read();
        int c2 = f2.read();
        if (c2 != c) {
          // checks for a \r\n \n equivalence
          if ((c == '\r') && (c2 == '\n')) {
            c = f1.read();
            if (c == c2)
              continue;
          } else if ((c2 == '\r') && (c == '\n')) {
            c2 = f2.read();
            if (c == c2)
              continue;
          }
          return false;
        }
        if (c == -1) break;
      }
      return true;
    } catch (IOException exc) {
      return false;
    } finally {
      try {
        f1.close();
      } catch (Exception e2) {}
      try {
        f2.close();
      } catch (Exception e2) {}
    }
  }

  /**
   * Asserts that two files are same content.
   */
  static public void assertFileSameContent(String expected, String actual) {
    assertFileSameContent(null,expected,actual);
  }
  /**
   * Asserts that two files are same content.
   */
  static public void assertFileSameContent(String message, String expected, String actual) {
    asserts++;
    boolean ok = true;
    File file1 =null;
    File file2 =null;

    String formatted = "";
    if (message != null) formatted = message + ", ";
    try {
      file1 = new File(expected);
      if (! file1.canRead()) throw new IOException();
      expected = file1.getCanonicalPath();
    } catch (IOException exc) {
      fail(formatted + "cannot access file <" + expected + ">");
      ok = false;
    }
    try {
      file2 = new File(actual);
      if (! file2.canRead()) throw new IOException();
      actual = file2.getCanonicalPath();
    } catch (IOException exc) {
      fail(formatted + "cannot access file <" + actual + ">");
      ok = false;
    }
    if ((! ok) || isSameContent(file1, file2)) return;

    fail(formatted +
         "files <" + expected + "> and <" + actual + "> differs");
  }

  public static boolean isSameContent(File file1, File file2) {
    asserts++;
    BufferedReader f1 = null;
    RandomAccessFile f2 = null;
    long l2= 0;
    Long pfile;
    Hashtable h = new Hashtable();
    try {
      if ( file1.length()!= file2.length())
        return false;

      f1 = new BufferedReader(new FileReader(file1));
      f2 = new RandomAccessFile(file2,"r");
      while (true) {
        String line1 = f1.readLine();
        l2 = 0;
        if (line1 == null) {
          break;
        }
        while (true) {
          f2.seek(l2);
          if (h.containsKey(new Long(l2))) {
            l2 = ((Long) h.get(new Long(l2))).longValue();
            continue;
          }
          String line2 = f2.readLine();
          pfile = new Long(f2.getFilePointer());
          if (line2 == null)
            return false;
          if (line1.equals(line2)) {
            h.put(new Long(l2), pfile);
            break;
          }
          l2 = pfile.longValue();
          continue;
        }
      }
      return true;
    } catch (IOException exc) {
      return false;
    } finally {
      try {
        f1.close();
      } catch (Exception e2) {}
      try {
        f2.close();
      } catch (Exception e2) {}
    }
  }

  /**
   * Asserts that two files are identical.
   */
  static public void assertFileIdentical(String expected, String actual) {
    assertFileIdentical(null, expected, actual);
  }

  /**
   * Asserts that two files are identical.
   */
  static public void assertFileIdentical(String message,
                                         String expected, String actual) {
    asserts++;
    boolean ok = true;
    File file1 =null;
    File file2 =null;

    String formatted = "";
    if (message != null) formatted = message + ", ";
    try {
      file1 = new File(expected);
      if (! file1.canRead()) throw new IOException();
      expected = file1.getCanonicalPath();
    } catch (IOException exc) {
      fail(formatted + "cannot access file <" + expected + ">");
      ok = false;
    }
    try {
      file2 = new File(actual);
      if (! file2.canRead()) throw new IOException();
      actual = file2.getCanonicalPath();
    } catch (IOException exc) {
      fail(formatted + "cannot access file <" + actual + ">");
      ok = false;
    }
    if ((! ok) || check(file1, file2)) return;

    fail(formatted +
         "files <" + expected + "> and <" + actual + "> differs");
  }

  /**
   * Asserts that a file exists.
   */
  static public void assertFileExist(String expected) {
    assertFileExist(null, expected);
  }

  /**
   * Asserts that a file exists.
   */
  static public void assertFileExist(String message, String expected) {
    asserts++;
    File file =null;

    String formatted = "";
    if (message != null) formatted = message + ", ";
    try {
      file = new File(expected);
      if (! file.exists()) throw new IOException();
      expected = file.getCanonicalPath();
    } catch (IOException exc) {
      fail(formatted + "cannot access file <" + expected + ">");
    }
  }

  /**
   * Asserts that two objects refer to the same object.
   */
  static public void assertSame(Object expected, Object actual) {
    assertSame(null, expected, actual);
  }

  static public void exception(Throwable t) {
    current.addException(t);
  }

  /**
   * Fails a test with the given message. 
   */
  static public void fail(String message) {
    if (message == null) message = "";
      current.addFailure(new AssertionFailedError(message));
  }

  static public void error(Throwable t) {
    current.addError(t);
  }

  static private void failNotEquals(String message,
                                    Object expected, Object actual) {
    String formatted = "";
    if (message != null)
      formatted = message + ", ";
    fail(formatted + "expected:<" + expected + "> but was:<" + actual + ">");
  }

  static private void failNotSame(String message,
                                  Object expected, Object actual) {
    String formatted = "";
    if (message != null)
      formatted = message + ", ";
    fail(formatted + "expected same");
  }

  /**
   * Change the elements of a path as absolute names.
   * Use the property path.separator as separator.
   *
   * @param path	path to transform
   *
   * @return	transformed path
   */
  public static String getAbsolutePath(String path) throws Exception {
    String ps = System.getProperty("path.separator");
    StringTokenizer st = new StringTokenizer(path, ps);
    if (! st.hasMoreTokens())
      return path;

    StringBuffer buf = new StringBuffer();
    token_loop:
    while (true) {
      String tok = st.nextToken();
      buf.append(new File(tok).getAbsolutePath());
      if (! st.hasMoreTokens())
	break token_loop;
      buf.append(ps);
    }

    return buf.toString();
  }

  protected long timeout = 0L;
  protected long startDate = System.currentTimeMillis();
  protected long endDate = 0L;

  /**
   * Runs a TestCase.
   */
  public void runTest(String args[]) {
    Thread t = null;
    try {
      setUpEnv(args);   
      setUp();
      // Creates a thread to execute the test in order to
      // control the test duration.
      t = new Thread() {
        public void run() {
          try {
            startTest();
          } catch (Exception exc) {
            addError(exc);
            endTest();
          }
        }
      };
      t.setDaemon(true);
      t.start();
      if (timeout != 0L) {
        Thread.sleep(timeout);
        throw new Exception("timeout expired");
      }
    } catch (Throwable exc) {
      // TODO:
      addError(exc);
      endTest();
    }
  }

  /**
   * Sets up the generic environment for a class of tests.
   */
  protected void setUpEnv(String args[]) throws Exception {}

  /**
   * Sets up the test specific environment.
   */
  protected void setUp() throws Exception {
  }

  /**
   * Actually starts the test.
   * Should call setStartDate to define the actual beginning of the test,
   * and ensure that endTest is eventually called.
   */
  protected void startTest() throws Exception {}

  /**
   * Informs the framework that a test begins.
   */
  public static final void setStartDate() {
    current.startDate = System.currentTimeMillis();
  }

  /**
   * Informs the framework that a test was completed.
   */
  public static final void endTest() {
    endTest(null);
  }

  public static final void endTest(boolean exit) {
    endTest(null, exit);
  }

  public static final void endTest(String msg) {
    endTest(msg, true);
  }

  public static final void endTest(String msg, boolean exit) {
    current.endDate = System.currentTimeMillis();
    int status = 0;

    current.endEnv();
    current.tearDown();

    // computer information
    writeSysInfo();
    
    // write Joram version
    if (current.summary)
      System.err.println(ConnectionMetaData.providerName + " " + ConnectionMetaData.providerVersion);
    if (current.writer != null)
      current.writer.println("| " + ConnectionMetaData.providerName + " " + ConnectionMetaData.providerVersion);

    // TODO:
    if ((current.failures != null) || (current.errors != null)) {
      if (current.summary)
        System.err.println("TEST \"" + current.name + "\" FAILED" +
                           ", asserts: " + asserts +
                           ", failures: " + current.failureCount() +
                           ", errors: " + current.errorCount() + ", [" +
                           (current.endDate - current.startDate) + "].");
      if (current.writer != null)
        current.writer.println("TEST \"" + current.name + "\" FAILED" +
                               ", asserts: " + asserts +
                               ", failures: " + current.failureCount() +
                               ", errors: " + current.errorCount() + ", [" +
                               (current.endDate - current.startDate) + "].");
    } else {
      if (current.summary)
        System.err.println("TEST \"" + current.name + "\" OK [" + asserts + ", " +
                           (current.endDate - current.startDate) + "].");
      if (current.writer != null)
        current.writer.println("TEST \"" + current.name + "\" OK [" + asserts + ", " +
                               (current.endDate - current.startDate) + "].");
    }
    if (msg != null) {
      if (current.summary) System.err.println(msg);
      if (current.writer != null) current.writer.println(msg);
    }

    if (current.failures != null) {
      status += current.failures.size();
      if (current.writer != null) {
        for (int i=0; i<current.failures.size(); i++) {
          current.writer.print("+" + i + ") ");
          ((Throwable) current.failures.get(i)).printStackTrace(current.writer);
        }
      }
    }

    if (current.errors != null) {
      status += current.errors.size();
      if (current.writer != null) {
        for (int i=0; i<current.errors.size(); i++) {
          current.writer.print("+" + i + ") ");
          ((Throwable) current.errors.get(i)).printStackTrace(current.writer);
        }
      }
    }

    if (current.exceptions != null) {
      if (current.writer != null) {
        for (int i=0; i<current.exceptions.size(); i++) {
          current.writer.print("+" + i + ") ");
          ((Throwable) current.exceptions.get(i)).printStackTrace(current.writer);
        }
      }
    }

    if (current.saveErrors
        && (current.failures != null || current.errors != null || current.exceptions != null)) {
      DateFormat df = new SimpleDateFormat("yy-MM-dd [HH.mm.ss] ");
      File currentDir = new File(".");
      File destDir = new File("../" + df.format(new Date()) + current.name);
      try {
        copyDirectory(currentDir, destDir);
      } catch (IOException exc) {
        if (current.writer != null) {
          current.writer.print("Error while saving the run directory: " + exc.getMessage());
        }
      }
    }

    if (current.writer != null) {
      current.writer.flush();
      current.writer.close();
    }

    if (exit) System.exit(status);
  }

  public static void copyDirectory(File srcPath, File dstPath) throws IOException {
    if (srcPath.isDirectory()) {
      if (!dstPath.exists()) {
        dstPath.mkdir();
      }
      String files[] = srcPath.list();
      for (int i = 0; i < files.length; i++) {
        copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
      }
    } else {
      if (!srcPath.exists())
        throw new IOException("Source path doesn't exists.");

      if (srcPath.getName().endsWith(".lck")) return;

      try {
        InputStream in = new FileInputStream(srcPath);
        OutputStream out = new FileOutputStream(dstPath);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
        in.close();
        out.close();
      } catch (IOException exc) {
        throw new IOException(srcPath.toString() + ": " + exc.getMessage());
      }
    }
  }

  public static void writeSysInfo() {
    InetAddress addr = null;
    String hostname = null;
    try {
      addr = InetAddress.getLocalHost();
      hostname = addr.getHostName();
    } catch (UnknownHostException e) { }

    OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
    if (current.summary) {
      if (hostname != null)
        System.err.println("hostname: " + hostname);
      if (bean != null)
        System.err.println(
            "System: " + bean.getArch() +  
            ", OS: " + bean.getName() +
            ", Nb processor(s): " + bean.getAvailableProcessors());
    }
    if (current.writer != null) {
      current.writer.println("----------------------------------------------------");
      if (hostname != null)
        current.writer.println("| hostname: " + hostname);
      if (bean != null)
        current.writer.println(
            "| System: " + bean.getArch() +  
            ", OS: " + bean.getName() +
            ", Nb processor(s): " + bean.getAvailableProcessors());
      current.writer.println("| Date: " + new Date(System.currentTimeMillis()));
    }
  }
 
  public void writeIntoFile(String str){
    if (current.writer != null) {
      current.writer.println(str);
      current.writer.flush();
    }
  }

  /**
   * Finalizes the generic environment for a class of tests.
   */
  protected void endEnv() {}

  /**
   * Destroys everything and make sure all parts of the test
   * are stopped.
   */
  protected void tearDown() {
  }

  public static synchronized BaseTestCase getCurrent() {
    return current;
  }

  public static synchronized void setCurrent(BaseTestCase current) {
   BaseTestCase.current = current;
  }

  public static void main(String args[]) throws Exception {
    assertFileIdentical(args[0], args[1]);
    endTest();
  }
}
