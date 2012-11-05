/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
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

import java.io.File;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.SCAdminBase;

/**
 * Framework for tests using A3 agent servers.
 */
public class TestCase extends BaseTestCase {
  
  static SCAdminBase admin = null;

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
    startAgentServer(sid, null);
  }

  public static void startAgentServer(short sid,
				      File dir) throws Exception {
    try {
      getAdmin().startAgentServer(sid, dir);
    } catch (IllegalStateException exc) {
      exception(exc);
      // The process is still alive, kill it!
      getAdmin().killAgentServer(sid);
      getAdmin().joinAgentServer(sid);
      getAdmin().startAgentServer(sid, dir);
    }
  }

  public static void startAgentServer(short sid,
				      File dir,
                                      String[] jvmargs) throws Exception {
    try {
      getAdmin().startAgentServer(sid, dir, jvmargs);
    } catch (IllegalStateException exc) {
      exception(exc);
      // The process is still alive, kill it!
      getAdmin().killAgentServer(sid);
      getAdmin().joinAgentServer(sid);
      getAdmin().startAgentServer(sid, dir, jvmargs);
    }
  }

  public static void stopAgentServer(short sid) {
    try {
      getAdmin().stopAgentServer(sid);
      getAdmin().joinAgentServer(sid);
    } catch (Exception exc) {
      exception(exc);
    }
  }
 public static void stopAgentServerExt(short sid) {
    try {
      getAdmin().stopAgentServer(sid);
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
    try {
      getAdmin().crashAgentServer(sid);
      return;
    } catch (Exception exc) {
      exception(exc);
    }
    try {
      getAdmin().killAgentServer(sid);
    } catch (Exception exc) {
      exception(exc);
    }
  }

  public static void joinAgentServer(short sid) {
    try {
      getAdmin().joinAgentServer(sid);
    } catch (Exception exc) {
      exception(exc);
    }
  }

  public static void killAgentServer(short sid) {
    try {
      getAdmin().killAgentServer(sid);
    } catch (Exception exc) {
      exception(exc);
    }
  }

  public static SCAdminBase getAdmin() throws Exception {
    if (admin == null) {
      String cfgFile = System.getProperty(
        AgentServer.CFG_FILE_PROPERTY,
        AgentServer.DEFAULT_CFG_FILE);
      // Initializes the admin proxy.
      admin = new SCAdminBase(cfgFile);
    }
    return admin;
  }

  public static void main(String args[]) throws Exception {
    TestCase test = new TestCase();
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
