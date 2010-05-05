/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import java.io.*;
import java.util.*;

import joram.framework.TestCase;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.*;
import com.scalagent.task.util.*;

/**
 * Main to test the <code>Program</code> constructor with string array
 * argument.
 *
 * @see		Program
 */
public class test9 extends TestCase {
  public test9() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 10000L;

    Test9Agent test = new Test9Agent();

    String[] args1 = new String[4];
    args1[0] = "java";
    args1[1] = "-cp";
    args1[2] = "../classes";
    args1[3] = "task.Hello";
    Program task1 = new Program((short) 0, null, args1);
    task1.addStatusListener(test.getId());
    task1.deploy();
    Channel.sendTo(task1.getId(), new ResetNotification());

    String[] args2 = new String[5];
    args2[0] = "java";
    args2[1] = "-cp";
    args2[2] = "../classes";
    args2[3] = "task.Hello";
    args2[4] = "Andre";
    Program task2 = new Program((short) 0, null, args2);
    task2.addStatusListener(test.getId());
    task2.deploy();
    Channel.sendTo(task2.getId(), new ResetNotification());

    String[] args3 = new String[4];
    args3[0] = "java";
    args3[1] = "-cp";
    args3[2] = "../classes";
    args3[3] = "task.Hello Andre";
    Program task3 = new Program((short) 0, null, args3);
    task3.addStatusListener(test.getId());
    task3.deploy();
    Channel.sendTo(task3.getId(), new ResetNotification());

    test.task1 = task1.getId();
    test.task2 = task2.getId();
    test.task3 = task3.getId();
    test.deploy();
  }

  public static void main(String args[]) {
    new test9().runTest(args);
  }
}

class Test9Agent extends Agent {
  AgentId task1, task2, task3;
  int state = 0;

  public Test9Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    if (state == 0) {
      // StatusNotification,task1,status=WAIT,message=null,result=null
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
      // StatusNotification,task2,status=WAIT,message=null,result=null
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
       // StatusNotification,task3,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task3, from);
      TestCase.assertEquals("step#" + state, 
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task3, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
        TestCase.assertNull("step#" + state, status.getMessage());
        TestCase.assertNull("step#" + state, status.getResult());
      }
    } else if (state == 3) {
      // StatusNotification,task1,status=RUN,message=null,result=null
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
    } else if (state == 4) {
      // StatusNotification,task2,status=RUN,message=null,result=null
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
    } else if (state == 5) {
      // StatusNotification,task3,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task3, from);
      TestCase.assertEquals("step#" + state, 
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task3, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
        TestCase.assertNull("step#" + state, status.getMessage());
        TestCase.assertNull("step#" + state, status.getResult());
      }
    } else if (state == 6) {
      // StatusNotification,task1,status=FAIL,message="...",result=null
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
    } else if (state == 7) {
      // StatusNotification,task2,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      TestCase.assertEquals("step#" + state, 
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
        TestCase.assertNull("step#" + state, status.getMessage());
        TestCase.assertNull("step#" + state, status.getResult());
      }
    } else if (state == 8) {
      // StatusNotification,task3,status=FAIL,message="...",result=null
      TestCase.assertEquals("step#" + state, task3, from);
      TestCase.assertEquals("step#" + state, 
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task3, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.FAIL);
        TestCase.assertNull("step#" + state, status.getResult());
      }
      TestCase.endTest();
    }
//  System.out.println("step#" + state + "->" + not);
    state += 1;
  }
}
