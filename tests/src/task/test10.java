/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import java.io.*;
import java.util.*;

import joram.framework.TestCase;

import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;
import com.scalagent.task.*;
import com.scalagent.task.composed.*;
import com.scalagent.task.util.*;

/**
 * Main to test a restart of a failed task.
 *
 * @see		Task
 * @see		RestartNotification
 */
public class test10 extends TestCase {
  public test10() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 10000L;

    Test10Agent test = new Test10Agent();

    Sequential parent = new Sequential((short) 0, test.getId(), null);
    AgentId child[] = new AgentId[3];

    Task task1 = new Program((short) 0,
                             parent.getId(), "rm -f test9.sh test9.th");
    task1.deploy();
    child[0] = task1.getId();

    Task task2 = new Program((short) 0,
                             parent.getId(), "cp hello.sh test9.th");
    task2.deploy();
    child[1] = task2.getId();

    Task task3 = new Program((short) 0,
                             parent.getId(), "sh ./test9.sh ok");
    task3.deploy();
    child[2] = task3.getId();

    parent.setChild(child);
    parent.deploy();

    test.parent1 = parent.getId();
    test.deploy();
  }

  public static void main(String args[]) {
    new test10().runTest(args);
  }
}

class Test10Agent extends Agent {
  int state = 0;
  AgentId parent1, parent2;

  public Test10Agent() {
    super();
  }

  public void agentInitialize(boolean firstime) {
    sendTo(parent1, new ResetNotification());
    sendTo(parent1, new Condition());
  }

  public void react(AgentId from, Notification not) throws Exception {
    TestCase.assertEquals("step#" + state, 
                          "com.scalagent.task.StatusNotification",
                          not.getClass().getName());

    if (state == 0) {
      // StatusNotification,parent1,status=WAIT,message=null,result=nul
      TestCase.assertEquals("step#" + state, parent1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
        TestCase.assertNull("step#" + state, status.getMessage());
      }
    } else if (state == 1) {
      // StatusNotification,parent1,status=INIT,message=null,result=null
      TestCase.assertEquals("step#" + state, parent1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.INIT);
        TestCase.assertNull("step#" + state, status.getMessage());
      }
    } else if (state == 2) {
      // StatusNotification,parent1,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, parent1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
        TestCase.assertNull("step#" + state, status.getMessage());
      }
    } else if (state == 3) {
      // StatusNotification,parent1,status=FAIL,message="...",result=null
      TestCase.assertEquals("step#" + state, parent1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.FAIL);
      }

      Sequential parent = new Sequential((short) 0, getId(), null);
      AgentId child[] = new AgentId[2];

      Task task1 = new Program((short) 0,
                               parent.getId(), "mv test9.th test9.sh");
      task1.deploy();
      child[0] = task1.getId();

      Task task2 = new NotifyTask((short) 0,
                                  parent.getId(), parent1,
                                  new RestartNotification());
      task2.deploy();
      child[1] = task2.getId();

      parent.setChild(child);
      parent.deploy();

      parent2 = parent.getId();

      sendTo(parent2, new ResetNotification());
      sendTo(parent2, new Condition());
    } else if (state == 4) {
      // StatusNotification,parent2,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, parent2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
        TestCase.assertNull("step#" + state, status.getMessage());
      }
    } else if (state == 5) {
      // StatusNotification,parent2,status=INIT,message=null,result=null
    } else if (state == 6) {
      // StatusNotification,parent2,status=RUN,message=null,result=null
    } else if (state == 7) {
      // StatusNotification,parent1,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, parent1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
        TestCase.assertNull("step#" + state, status.getMessage());
      }
    } else if (state == 8) {
      // StatusNotification,parent2,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, parent2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
        TestCase.assertNull("step#" + state, status.getMessage());
      }
    } else if (state == 9) {
      // StatusNotification,parent1,status=INIT,message=null,result=null
      TestCase.assertEquals("step#" + state, parent1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.INIT);
        TestCase.assertNull("step#" + state, status.getMessage());
      }
    } else if (state == 10) {
      // StatusNotification,parent1,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, parent1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
        TestCase.assertNull("step#" + state, status.getMessage());
      }
    } else if (state == 11) {
      // StatusNotification,parent1,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, parent1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
        TestCase.assertNull("step#" + state, status.getMessage());
      }
      TestCase.endTest();
    }

    if (not instanceof StatusNotification) {
      StatusNotification status = (StatusNotification) not;

      TestCase.assertNull("step#" + state, status.getResult());
    }

//  System.out.println("step#" + state + "->" + not);
    state += 1;
  }
}
