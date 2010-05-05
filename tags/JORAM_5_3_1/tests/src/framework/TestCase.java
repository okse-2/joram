/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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

package framework;

import java.io.File;

import fr.dyade.aaa.agent.AgentServer;

/**
 * Framework for tests using A3 agent servers.
 */
public class TestCase extends BaseTestCase {

  static SCAdminItf admin = null;

  protected boolean running = false;

  public TestCase() {
    super();
  }

  /**
   * Sets up the generic environment for a class of tests.
   * Starts the agent server.
   */
  protected void setUpEnv(String args[]) throws Exception {
    AgentServer.init(args);
    running = true;
  }

  /**
   * Actually starts the test.
   * Calls setStartDate.
   * <p>
   * Derived class may not call this base class implementation
   * in overloaded functions, however setStartDate should be called.
   */
  protected void startTest() throws Exception {
    AgentServer.start();
    setStartDate();
  }

  /**
   * Finalizes the generic environment for a class of tests.
   * Stops the agent server.
   */
  protected void endEnv() {
    if (running) {
      // Stop the AgentServer
      // Creates a thread to execute AgentServer.stop in order to
      // avoid deadlock if called from an agent reaction.
      Thread t = new Thread() {
        public void run() {
          AgentServer.stop();
        }
      };
      t.setDaemon(true);
      t.start();
      running = false;
    }
  }

  public static void startAgentServer(short sid) throws Exception {
    try {
      getAdmin().startAgentServer(sid);
    } catch (IllegalStateException exc) {
      exception(exc);
      // The process is still alive, kill it!
      getAdmin().killAgentServer(sid);
      getAdmin().startAgentServer(sid);
    }
  }

  public static void startAgentServer(short sid, String[] jvmargs) throws Exception {
    try {
      getAdmin().startAgentServer(sid, jvmargs);
    } catch (IllegalStateException exc) {
      exception(exc);
      // The process is still alive, kill it!
      getAdmin().killAgentServer(sid);
      getAdmin().startAgentServer(sid, jvmargs);
    }
  }

  public static void startAgentServer(short sid, short cid, String[] jvmargs) throws Exception {
    try {
      getAdmin().startAgentServer(sid, cid, jvmargs);
    } catch (IllegalStateException exc) {
      exception(exc);
      // The process is still alive, kill it!
      getAdmin().killAgentServer(sid);
      getAdmin().startAgentServer(sid, cid, jvmargs);
    }
  }

  public static void stopAgentServer(short sid) {
    try {
      getAdmin().stopAgentServer(sid);
    } catch (Exception exc) {
      exception(exc);
    }
  }

  public static void stopAgentServerExt(int telnetPort) {
    try {
      getAdmin().stopAgentServerExt(telnetPort);
    } catch (Exception exc) {
      exception(exc);
    }
  }
  public static void killAgentServerExt(short sid) {
    try {
      getAdmin().killAgentServer(sid);
    } catch (Exception exc) {
      exception(exc);
    }
  }

  public static void crashAgentServer(short sid) {
    killAgentServer(sid);
  }

  public static void killAgentServer(short sid) {
    try {
      getAdmin().killAgentServer(sid);
    } catch (Exception exc) {
      exception(exc);
    }
  }

  public static SCAdminItf getAdmin() throws Exception {
    if (admin == null) {
      String scAdminClass = System.getProperty("SCAdminClass", SCAdminClassic.class.getName());
      admin = (SCAdminItf) Class.forName(scAdminClass).newInstance();
    }
    return admin;
  }

  public static void main(String args[]) throws Exception {
    assertFileIdentical(args[0], args[1]);
    endTest();
  }

  public static void deleteDirectory(File dir) {
    String[] files = dir.list();
    for (int i = 0; i < files.length; i++) {
      File f =  new File(dir, files[i]);
      if (f.isDirectory()) {
        deleteDirectory(f);
      } else {
        f.delete();
      }
    }
    dir.delete();
  }
}
