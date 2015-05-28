/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2002 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2002 INRIA
 * Contact: joram-team@objectweb.org
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
 * Initial developer(s): Jeff Mesnil (Inria)
 * Contributor(s): Nicolas Tachker (ScalAgent D.T.)
 */

package jms.framework;

import java.lang.reflect.Method;

import javax.jms.JMSException;

import framework.TestCase;


/**
 * Class extending <code>framework.TestCase</code> to provide a new
 * <code>fail()</code> method with an <code>Exception</code> as parameter.
 * <br />
 * Every Test Case for JMS should extend this class instead of
 * <code>framework.TestCase</code>
 * 
 */
public abstract class JMSTestCase extends TestCase {

  abstract protected void setUp();
  abstract protected void clear();
  
  /**
   * Fails a test with an exception which will be used for a message.
   * 
   * If the exception is an instance of <code>javax.jms.JMSException</code>,
   * the message of the failure will contained both the JMSException and its
   * linked exception (provided there's one).
   */
  public void fail(Exception e) {
    if (e instanceof javax.jms.JMSException) {
      JMSException exception = (JMSException) e;
      String message = e.toString();
      Exception linkedException = exception.getLinkedException();
      if (linkedException != null) {
        message += " [linked exception: " + linkedException + "]";
      }
      super.fail(message);
    } else {
      super.fail(e.getMessage());
    }
  }

  public static void run(JMSTestCase testObj) {
    try {
      startAgentServer((short) 0);

      Thread.sleep(1000);

      testObj.clear();
      
      if (testObj != null) {
        Method[] methods = testObj.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
          if (methods[i].getName().startsWith("test")) {
            testObj.setUp();
            System.out.println(testObj.getClass().getName() + "." + methods[i].getName() + "()");
            methods[i].invoke(testObj, new Object[0]);
            
            testObj.clear();
          }
        }
      }

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      //System.out.println("Server stop ");
      stopAgentServer((short) 0);
      endTest(null, false);
    }
  }

  public JMSTestCase() {
    super();
  }
  
  
}
