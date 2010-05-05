/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import joram.framework.TestCase;
import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;
import com.scalagent.task.*;
import com.scalagent.task.util.*;

public class test3 extends TestCase {
  public test3() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 10000L;

    Test3Agent test = new Test3Agent();

//     Program task1 = new Program((short) 0, null, "sh ./hello.sh");
    Program task1 = new Program((short) 0, null,
                                "java -cp ../classes task.Hello");
    task1.addStatusListener(test.getId());
    task1.deploy();
    Channel.sendTo(task1.getId(), new ResetNotification());
    test.task1 = task1.getId();

//     Program task2 = new Program((short) 0, null, "sh ./hello.sh Andre");
    Program task2 = new Program((short) 0, null,
                                "java -cp ../classes task.Hello Andre");
    task2.addStatusListener(test.getId());
    task2.deploy();
    Channel.sendTo(task2.getId(), new ResetNotification());
    test.task2 = task2.getId();

    test.deploy();
  }

  public static void main(String args[]) {
    new test3().runTest(args);
  }
}

class Test3Agent extends Agent {
  AgentId task1, task2;
  int state = 0;

  public Test3Agent() {
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
    } else if (state == 4 || state == 5) {
      TestCase.assertEquals("step#" + state, 
                            "com.scalagent.task.StatusNotification",
                            not.getClass().getName());
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        if (task1 == status.getTask()) {
          // StatusNotification,date,task1,status=FAIL,message="...",result=null
          TestCase.assertEquals("step#" + state,
                                task1, status.getTask());
          TestCase.assertEquals("step#" + state,
                                status.getStatus(), Task.Status.FAIL);
          TestCase.assertEquals("step#" + state,
                                status.getMessage().trim(),
                                "usage ./hello.sh <name>");
          TestCase.assertNull("step#" + state, status.getResult());
        } else if (task2 == status.getTask()) {
          // StatusNotification,date,task2,status=DONE,message=null,result=null
          TestCase.assertEquals("step#" + state,
                                task2, status.getTask());
          TestCase.assertEquals("step#" + state,
                                status.getStatus(), Task.Status.DONE);
          TestCase.assertNull("step#" + state, status.getMessage());
          TestCase.assertNull("step#" + state, status.getResult());
        }
      }
    }
    if (state == 5)
      TestCase.endTest();
//  System.out.println("step#" + state + "->" + not);
    state += 1;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + super.toString() + ",step#=" + state + ")";
  }
}
