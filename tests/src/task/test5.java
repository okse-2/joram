/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import joram.framework.TestCase;
import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;
import com.scalagent.task.*;
import com.scalagent.task.composed.*;
import com.scalagent.task.util.*;

/**
 * Main to test the <code>Parallel<code> class.
 *
 * @see		Parallel
 */
public class test5 extends TestCase {
  public test5() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 10000L;

    Test5Agent test = new Test5Agent();
    String cp = System.getProperty("java.class.path");

    Parallel parent = new Parallel((short) 0, null, null);
    parent.addStatusListener(test.getId());
    test.parent = parent.getId();
    AgentId child[] = new AgentId[3];

    JavaMain task1 = new JavaMain((short) 0,
                                  parent.getId(), "task.Hello 0", cp);
//  Program task1 = new Program((short) 0, parent.getId(), "sh ./hello.sh 0");
    task1.addStatusListener(test.getId());
    task1.deploy();
    test.task1 = task1.getId();
    child[0] = task1.getId();

    JavaMain task2 = new JavaMain((short) 0,
                                  parent.getId(), "task.Hello 1", cp);
//  Program task2 = new Program((short) 0, parent.getId(), "sh ./hello.sh 1");
    task2.addStatusListener(test.getId());
    task2.deploy();
    test.task2 = task2.getId();
    child[1] = task2.getId();

    JavaMain task3 = new JavaMain((short) 0,
                                  parent.getId(), "task.Hello 2", cp);
//  Program task3 = new Program((short) 0, parent.getId(), "sh ./hello.sh 2");
    task3.addStatusListener(test.getId());
    task3.deploy();
    test.task3 = task3.getId();
    child[2] = task3.getId();

    parent.setChild(child);
    parent.deploy();
    Channel.sendTo(parent.getId(), new ResetNotification());

    test.deploy();
  }

  public static void main(String args[]) {
    new test5().runTest(args);
  }
}

class Test5Agent extends Agent {
  AgentId parent, task1, task2, task3;
  int state = 0;
  boolean run[] = {false, false, false};
  boolean done[] = {false, false, false};

  public Test5Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    TestCase.assertEquals("step#" + state, 
                          "com.scalagent.task.StatusNotification",
                          not.getClass().getName());
    if (state == 0) {
      // StatusNotification,parent,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, parent, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 1) {
      // StatusNotification,task3,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task3, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task3, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 2) {
      // StatusNotification,task2,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 3) {
      // StatusNotification,task1,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 4) {
      // StatusNotification,parent,status=INIT,message=null,result=null
      TestCase.assertEquals("step#" + state, parent, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.INIT);
      }
    } else if ((state == 5) || (state == 6) || (state == 7)) {
      // StatusNotification,task1/2/3,status=RUN,message=null,result=null
      if (from.equals(task1)) run[0] = true;
      if (from.equals(task2)) run[1] = true;
      if (from.equals(task3)) run[2] = true;
      if (state == 7)
        TestCase.assertTrue("step#" + state, run[0] && run[1] && run[2]);

      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              from, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 8) {
      // StatusNotification,parent,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, parent, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if ((state == 9) || (state == 10) || (state == 11)) {
      // StatusNotification,task1/2/3,status=DONE,message=null,result=null
      if (from.equals(task1)) done[0] = true;
      if (from.equals(task2)) done[1] = true;
      if (from.equals(task3)) done[2] = true;
      if (state == 11)
        TestCase.assertTrue("step#" + state, done[0] && done[1] && done[2]);

      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              from, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 12) {
      // StatusNotification,parent,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, parent, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
      TestCase.endTest();
    }

    if (not instanceof StatusNotification) {
      StatusNotification status = (StatusNotification) not;
      TestCase.assertNull("step#" + state, status.getMessage());
      TestCase.assertNull("step#" + state, status.getResult());
    }

//  System.out.println("step#" + state + "->" + not);
    state += 1;
  }
}
