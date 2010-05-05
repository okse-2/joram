/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import java.io.*;

import joram.framework.TestCase;

import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;
import com.scalagent.task.*;
import com.scalagent.task.util.*;

/**
 * Main to test killing and restarting a task.
 *
 * @see		Task
 * @see		RestartNotification
 * @see		EventKill
 */
public class test11 extends TestCase {
  public test11() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 30000L;

    Test11Agent test = new Test11Agent();
    test.deploy();
  }

  public static void main(String args[]) {
    new test11().runTest(args);
  }
}

class Test11Agent extends Agent {
  int state = 0;
  AgentId task1;

  public Test11Agent() {
    super();
  }

  public void agentInitialize(boolean firstime) throws IOException {
    String cp = System.getProperty("java.class.path");

    Program task = new JavaMain((short) 0, getId(), "task.Wait 5", cp);
    task.deploy();

    task1 = task.getId();
    sendTo(task1, new ResetNotification());
    sendTo(task1, new Condition());
  }

  public void react(AgentId from, Notification not) throws Exception {
    TestCase.assertEquals("step#" + state, 
                          "com.scalagent.task.StatusNotification",
                          not.getClass().getName());
    TestCase.assertEquals("step#" + state, task1, from);

    if (state == 0) {
      // StatusNotification,task1,status=WAIT,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Program.Status.WAIT);
      }
    } else if (state == 1) {
      // StatusNotification,task1,status=RUN,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Program.Status.RUN);
      }
      // kills process
      sendTo(task1, new EventKill());
    } else if (state == 2) {
      // StatusNotification,task1,status=KILL,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Program.Status.KILL);
      }
    } else if (state == 3) {
      // StatusNotification,task1,status=STOP,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Program.Status.STOP);
      }
      // restarts process
      sendTo(task1, new RestartNotification());
    } else if (state == 4) {
      // StatusNotification,task1,status=WAIT,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Program.Status.WAIT);
      }
    } else if (state == 5) {
      // StatusNotification,task1,status=RUN,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Program.Status.RUN);
      }
    } else if (state == 6) {
      // StatusNotification,task1,status=DONE,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Program.Status.DONE);
      }
      TestCase.endTest();
    }

    if (not instanceof StatusNotification) {
      StatusNotification status = (StatusNotification) not;

      TestCase.assertEquals("step#" + state,
                            task1, status.getTask());
      if (state != 3)
        // AF: on STOP message = "" !
        TestCase.assertNull("step#" + state, status.getMessage());
      TestCase.assertNull("step#" + state, status.getResult());
    }

//  System.out.println("step#" + state + "->" + not);
    state += 1;

  }
}
