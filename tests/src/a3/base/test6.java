/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001 ScalAgent Distributed Technologies
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


package a3.base;

import fr.dyade.aaa.agent.*;
import framework.TestCase;

public class test6 extends TestCase {
  public test6() {
    super();
  }

  public void runTest(String args[]) {
    String excType = null;
    String excMsg = null;

    try {
      excType = System.getProperty("ExcType");
      excMsg = System.getProperty("ExcMsg");
      AgentServer.init(args);
      if ((excMsg != null) && (excMsg.length() != 0) &&
          (excType != null) && (excType.length() != 0))
        addFailure(new Exception("Exception expecting: " +
                                 excType + '(' + excMsg + ')'));
    } catch (Throwable exc) {
      assertEquals(excType, exc.getClass().getName());
      assertEquals(excMsg, exc.getMessage());
    }
    endTest();
  }

  public static void main(String args[]) {
    new test6().runTest(args);
  }
}
