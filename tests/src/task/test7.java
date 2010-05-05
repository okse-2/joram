/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import joram.framework.TestCase;
import fr.dyade.aaa.agent.*;
import com.scalagent.task.*;
import com.scalagent.task.composed.*;
import com.scalagent.task.util.*;

/**
 * Main to test the <code>Delegating</code> class.
 *
 * @see		JavaMain
 */
public class test7 extends TestCase {
  public test7() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 10000L;

    Test7Agent test = new Test7Agent();

    Test7DelegatingAgent delegating =
      new Test7DelegatingAgent((short) 0, null);
    delegating.addStatusListener(test.getId());
    delegating.cp = System.getProperty("java.class.path");
    delegating.deploy();
    test.delegating = delegating.getId();

    Channel.sendTo(delegating.getId(), new ResetNotification());
    test.deploy();
  }

  public static void main(String args[]) {
    new test7().runTest(args);
  }
}

class Test7DelegatingAgent extends Delegating {
  static final int TASK_0 = 0;
  static final int TASK_1 = 1;
  static final int TASK_2 = 2;

  String cp;

  public Test7DelegatingAgent(short to, AgentId parent) {
    super(to, parent);
  }

  protected void delegatingStart() throws Exception {
    Task task = new JavaMain((short) 0,
                             null, "task.Hello 0", cp);
    startChild(task, TASK_0);
  }

  /**
   * Continues this task execution when a sub-task completes.
   *
   * @param handle	child handle
   * @param not		notification from child
   */
  protected void childDone(KTaskHandle child) throws Exception {
    Task task;
    switch (child.key) {
    case TASK_0:
      task = new JavaMain((short) 0,
                          null, "task.Hello 1", cp);
      startChild(task, TASK_1);
      break;
    case TASK_1:
      task = new JavaMain((short) 0,
                          null, "task.Hello 2", cp);
      break;
    case TASK_2:
      // end of this task
      break;
    }
  }
}

class Test7Agent extends Agent {
  AgentId delegating;
  int state = 0;

  public Test7Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    TestCase.assertEquals("step#" + state, 
                          "com.scalagent.task.StatusNotification",
                          not.getClass().getName());
    TestCase.assertEquals("step#" + state, delegating, from);
    if (state == 0) {
      // StatusNotification,delegating,status=WAIT,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 1) {
      // StatusNotification,delegating,status=INIT,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.INIT);
      }
    } else if (state == 2) {
      // StatusNotification,delegating,status=RUN,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 3) {
      // StatusNotification,delegating,status=DONE,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
      TestCase.endTest();
    }

    if (not instanceof StatusNotification) {
      StatusNotification status = (StatusNotification) not;

      TestCase.assertEquals("step#" + state,
                            delegating, status.getTask());
      TestCase.assertNull("step#" + state, status.getMessage());
      TestCase.assertNull("step#" + state, status.getResult());
    }

//  System.out.println("step#" + state + "->" + not);
    state += 1;
  }
}
