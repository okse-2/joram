/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import joram.framework.TestCase;
import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;
import com.scalagent.task.*;
import com.scalagent.task.util.*;

public class test2 extends TestCase {
  public test2() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 160000L;

    Test2Agent test = new Test2Agent();
    String cp = System.getProperty("java.class.path");

    JavaMain task1 = new JavaMain((short) 0, null, "task.Hello", cp);
    task1.addStatusListener(test.getId());
    task1.deploy();
    Channel.sendTo(task1.getId(), new ResetNotification());
    test.task1 = task1.getId();

    JavaMain task2 = new JavaMain((short) 0, null, "task.Hello Serge", cp);
    task2.addStatusListener(test.getId());
    task2.deploy();
    Channel.sendTo(task2.getId(), new ResetNotification());
    test.task2 = task2.getId();

    test.deploy();
  }

  public static void main(String args[]) {
    new test2().runTest(args);
  }
}

class Test2Agent extends Agent {
  AgentId task1, task2;
  int state = 0;

  public Test2Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    if (state == 0) {
      // StatusNotification,date,task1,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      TestCase.assertEquals("step#" + state, 
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
        TestCase.assertNull("step#" + state, status.getMessage());
        TestCase.assertNull("step#" + state, status.getResult());
      }
    } else if (state == 1) {
      // StatusNotification,date,task2,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      TestCase.assertEquals("step#" + state, 
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
        TestCase.assertNull("step#" + state, status.getMessage());
        TestCase.assertNull("step#" + state, status.getResult());
     }
    } else if (state == 2) {
      // StatusNotification,date,task1,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      TestCase.assertEquals("step#" + state, 
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
        TestCase.assertNull("step#" + state, status.getMessage());
        TestCase.assertNull("step#" + state, status.getResult());
      }
    } else if (state == 3) {
      // StatusNotification,date,task2,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      TestCase.assertEquals("step#" + state, 
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
        TestCase.assertNull("step#" + state, status.getMessage());
        TestCase.assertNull("step#" + state, status.getResult());
      }
    } else if (state == 4) {
      // StatusNotification,date,task1,status=FAIL,message="...",result=null
      TestCase.assertEquals("step#" + state, task1, from);
      TestCase.assertEquals("step#" + state, 
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.FAIL);
        TestCase.assertEquals("step#" + state,
                              status.getMessage().trim(),
                              "usage: Hello <name>");
        TestCase.assertNull("step#" + state, status.getResult());
      }
    } else if (state == 5) {
      // StatusNotification,date,task2,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state + "<from>", task2, from);
      TestCase.assertEquals("step#" + state + "<not>", 
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state + "<task>",
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state + "<status>",
                              status.getStatus(), Task.Status.DONE);
        TestCase.assertNull("step#" + state + "<msg>", status.getMessage());
        TestCase.assertNull("step#" + state + "<result>", status.getResult());
      }
      TestCase.endTest();
    }
    state += 1;
  }
}
