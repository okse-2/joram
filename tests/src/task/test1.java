/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import joram.framework.TestCase;

import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;
import com.scalagent.task.*;
import com.scalagent.task.util.*;

public class test1 extends TestCase {
  public test1() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 10000L;

    Test1Agent test = new Test1Agent();
    Task task2 = new NotifyTask((short) 0, null, test.getId(),
				 new Notification());
    Task task1 = new NotifyTask((short) 0, null, task2.getId(),
				 new Condition("task1"));

    test.task1 = task1.getId();
    test.task2 = task2.getId();
    test.deploy();

    ConditionHandle[] cond1, cond2;

    cond2 = new ConditionHandle[1];
    cond2[0] = new ConditionHandle("task1", task1.getId());
    task2.setConditions(cond2);
    task2.addStatusListener(test.getId());
    task2.deploy();
    Channel.sendTo(task2.getId(), new ResetNotification());

    cond1 = new ConditionHandle[1];
    cond1[0] = new ConditionHandle("start", null);
    task1.setConditions(cond1);
    task1.addStatusListener(test.getId());
    task1.deploy();
    Channel.sendTo(task1.getId(), new ResetNotification());

    Channel.sendTo(task1.getId(), new Condition("start"));
  }

  public static void main(String args[]) {
    new test1().runTest(args);
  }
}

class Test1Agent extends Agent {
  AgentId task1, task2;
  int state = 0;

  public Test1Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    if (state == 0) {
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
                              status.getStatus(), Program.Status.WAIT);
      }
    } else if (state == 1) {
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
                              status.getStatus(), Program.Status.WAIT);
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
                              status.getStatus(), Program.Status.RUN);
      }
    } else if (state == 3) {
      // StatusNotification,date,task1,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      TestCase.assertEquals("step#" + state,
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Program.Status.DONE);
      }
    } else if (state == 4) {
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
                              status.getStatus(), Program.Status.RUN);
      }
    } else if (state == 5) {
      TestCase.assertEquals("step#" + state, task2, from);
      TestCase.assertEquals("step#" + state,
                            "fr.dyade.aaa.agent.Notification",
                            not.getClass().getName());
    } else if (state == 6) {
      // StatusNotification,date,task1,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      TestCase.assertEquals("step#" + state,
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Program.Status.DONE);
      }
      TestCase.endTest();
    }
    state += 1;
  }
}
