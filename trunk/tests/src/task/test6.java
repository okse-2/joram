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
 * Main to test the <code>ServiceTask<code> class.
 *
 * @see		ServiceTask
 */
public class test6 extends TestCase {
  public test6() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 10000L;

    Test6Agent test = new Test6Agent();
    
    // create a BounceService agent
    BounceService bounce = new BounceService((short) 0);
    bounce.deploy();

    Sequential parent = new Sequential((short) 0, null, null);
    parent.addStatusListener(test.getId());
    test.parent = parent.getId();
    AgentId child[] = new AgentId[3];

    ServiceTask task1 = new ServiceTask((short) 0,
                                        parent.getId(), bounce.getId(),
                                        new CountCommand(null, 1));
    task1.addStatusListener(test.getId());
    task1.deploy();
    test.task1 = task1.getId();
    child[0] = task1.getId();

    ServiceTask task2 = new ServiceTask((short) 0,
                                        parent.getId(), bounce.getId(),
                                        new CountCommand(null, 2));
    task2.addStatusListener(test.getId());
    task2.deploy();
    test.task2 = task2.getId();
    child[1] = task2.getId();

    ServiceTask task3 = new ServiceTask((short) 0,
                                        parent.getId(), bounce.getId(),
                                        new CountCommand(null, 3));
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
    new test6().runTest(args);
  }
}

class Test6Agent extends Agent {
  AgentId parent, task1, task2, task3;
  int state = 0;
  boolean run[] = {false, false, false};
  boolean done[] = {false, false, false};

  public Test6Agent() {
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
    } else if (state == 5) {
      // StatusNotification,task1,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 6) {
      // StatusNotification,parent,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, parent, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              parent, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 7) {
      // StatusNotification,task1,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 8) {
      // StatusNotification,task2,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 9) {
      // StatusNotification,task2,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 10) {
      // StatusNotification,task3,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task3, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task3, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 11) {
      // StatusNotification,task3,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task3, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task3, status.getTask());
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
