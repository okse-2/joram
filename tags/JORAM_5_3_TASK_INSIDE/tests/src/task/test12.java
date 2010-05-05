/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import joram.framework.TestCase;

import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;
import com.scalagent.scheduler.event.*;
import com.scalagent.scheduler.proxy.*;
import com.scalagent.task.*;

/**
 * Main to test scheduling a task.
 *
 * @see		Scheduler
 */
public class test12 extends TestCase {
  static AgentId schedulerId;

  public test12() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 300000L;

    Test12Agent test = new Test12Agent();

    Locale.setDefault(Locale.FRANCE);

    Scheduler scheduler = new Scheduler("Test12");
    scheduler.deploy();
    schedulerId = scheduler.getId();

    SchedulerProxy proxy =
      new SchedulerProxy("Test12",
                         schedulerId, SchedulerProxy.STREAM_ASCII);
    proxy.deploy();

    Test12Task task2 = new Test12Task("task2");
    task2.addStatusListener(test.getId());
    ConditionHandle[] conditions = new ConditionHandle[1];
    conditions[0] = new ConditionHandle("task2", scheduler.getId());
    task2.setConditions(conditions);
    task2.deploy();
    test.task2 = task2.getId();
    Channel.sendTo(task2.getId(), new ResetNotification());

    Test12Task task1 = new Test12Task("task1");
    task1.addStatusListener(test.getId());
    conditions = new ConditionHandle[1];
    conditions[0] = new ConditionHandle("task1", schedulerId);
    task1.setConditions(conditions);
    task1.deploy();
    test.task1 = task1.getId();
    Channel.sendTo(task1.getId(), new ResetNotification());

    Date now = new Date();
    // creates the first event at 5s from now, for a 5s duration
    Channel.sendTo(schedulerId,
                   new ScheduleEvent("task1",
                                     new Date(now.getTime() + 5000), 5));

    test.deploy();
  }

  public static void main(String args[]) {
    new test12().runTest(args);
  }
}

class Test12Task extends Task {
  static boolean init2 = false;

  public Test12Task(String name) {
    super((short) 0, name);
  }

  /**
   * Resets a <code>Task</code> agent ready for execution.
   */
  public void reset() throws Exception {
    if (getStatus() == Status.NONE) {
      sendTo(test12.schedulerId, new AddConditionListener(getName()));
    }
    super.reset();
  }

  /**
   * Starts task execution, must be defined in derived classes. This function
   * must start calling <code>setStatus(Status.INIT&RUN)</code>, and ensure
   * that <code>setStatus(Status.DONE/FAIL)</code> is eventually called.
   * This function is also called when the execution conditions of a restarted
   * task come true.
   */
  protected void start() throws Exception {
    setStatus(Status.RUN);
//  System.out.println("executing " + getName());

    if (!init2 && getName().equals("task1")) {
      // creates the second event, repeat every minute
      sendTo(test12.schedulerId,
             new CronEvent("task2", "* * * * *"));
      init2 = true;
    } else if (getName().equals("task2")) {
      // reschedules first event via the scheduler proxy
      rescheduleFirst();
    }

    setStatus(Status.DONE);
    reset();
  }

  /**
   * Stops task execution, must be defined in derived classes.
   * This function must ensure that <code>setStatus(Status.DONE/FAIL/STOP)</code>
   * is eventually called.
   */
  protected void taskStop() throws Exception {
    // should never be called
    setStatus(Status.STOP);
  }

  void rescheduleFirst() throws Exception {
    // reschedules the first event at 5s from now
    Date date = new Date(new Date().getTime() + 5000);
    StringBuffer command = new StringBuffer();
    command.append("schedule \"task1\" start=\"");
    command.append(DateFormat.getDateTimeInstance().format(date));
    command.append("\";");
//  System.out.println(command.toString());
    Socket socket = SchedulerProxy.connect("Test12");
    Writer scheduler = new OutputStreamWriter(socket.getOutputStream());
    scheduler.write(command.toString());
    scheduler.flush();
    scheduler.close();
  }
}

class Test12Agent extends Agent {
  int state = 0;
  AgentId task1, task2;

  public Test12Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    TestCase.assertEquals("step#" + state, 
                          "fr.dyade.aaa.task.StatusNotification",
                          not.getClass().getName());
    if (state == 0) {
      // StatusNotification,task2,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 1) {
      // StatusNotification,task1,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 2) {
      // StatusNotification,task1,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 3) {
      // StatusNotification,task1,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 4) {
      // StatusNotification,task1,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 5) {
      // StatusNotification,task2,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 6) {
      // StatusNotification,task2,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 7) {
      // StatusNotification,task2,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 8) {
      // StatusNotification,task1,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 9) {
      // StatusNotification,task1,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 10) {
      // StatusNotification,task1,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 11) {
      // StatusNotification,task2,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 12) {
      // StatusNotification,task2,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 13) {
      // StatusNotification,task2,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 14) {
      // StatusNotification,task1,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 15) {
      // StatusNotification,task1,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 16) {
      // StatusNotification,task1,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 17) {
      // StatusNotification,task2,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 18) {
      // StatusNotification,task2,status=DONE,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 19) {
      // StatusNotification,task2,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task2, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task2, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
    } else if (state == 20) {
      // StatusNotification,task1,status=RUN,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 21) {
      // StatusNotification,task1,status=DONE,message=null,result=nul
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.DONE);
      }
    } else if (state == 22) {
      // StatusNotification,task1,status=WAIT,message=null,result=null
      TestCase.assertEquals("step#" + state, task1, from);
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              task1, status.getTask());
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.WAIT);
      }
      TestCase.endTest();
    }

    if (not instanceof StatusNotification) {
      StatusNotification status = (StatusNotification) not;

      if (state != 3)
        // AF: on STOP message = "" !
        TestCase.assertNull("step#" + state, status.getMessage());
      TestCase.assertNull("step#" + state, status.getResult());
    }

//  System.out.println("step#" + state + "->" + not);
    state += 1;
  }
}
