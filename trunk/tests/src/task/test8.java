/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

import joram.framework.TestCase;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.*;
import com.scalagent.task.Task.Status;
import com.scalagent.scheduler.monitor.*;

/**
 * Main to test the <code>IndexedCommand<code> and <code>IndexedReport<code>
 * classes. Copy of test <code>test6</code>.
 *
 * @see		IndexedCommand
 * @see		IndexedReport
 */
public class test8 extends TestCase {
  public test8() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 10000L;

    Test8Agent test = new Test8Agent();

    // create a BounceService agent
    IbounceService bounce = new IbounceService((short) 0);
    bounce.deploy();

    Test8Monitor monitor = new Test8Monitor((short) 0);
    monitor.test = test.getId();
    monitor.bounce = bounce.getId();
    monitor.deploy();

    Channel.sendTo(monitor.getId(), new ResetNotification());

    test.monitor = monitor.getId();
    test.deploy();
  }

  public static void main(String args[]) {
    new test8().runTest(args);
  }
}

class Test8Monitor extends MonitorAgent {
  AgentId test;
  AgentId bounce;
  int state = 0;
  
  public Test8Monitor(short to) {
    super(to);
  }

  /**
   * Reacts to <code>test7</code> specific notifications.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void react(AgentId from, Notification not) {
    try {
      if (not instanceof ResetNotification) {
	// send first command
	IcountCommand command = new IcountCommand(1);
	CommandMonitor monitor = new CommandMonitor(this, this.bounce, command);
	monitor.start();
	state = 1;
	// command completion is managed in childReport
      } else {
	super.react(from, not);
      }
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", exception in " +
                 toString() + ".react(" + not + ")", exc);
    }
  }

  public void childReport(Monitor child, int status) throws Exception {
    if (status == Status.DONE) {
      if (state < 3) {
	state ++;
	IcountCommand command = new IcountCommand(state);
	CommandMonitor monitor = new CommandMonitor(this, bounce, command);
	monitor.start();
      } else {
	sendTo(test, new StatusNotification(getId(), Status.DONE, null));
      }
    } else {
      sendTo(test, new StatusNotification(getId(), status, child.getErrorMessage()));
    }
  }
}

class Test8Agent extends Agent {
  AgentId monitor;
  int state = 0;

  public Test8Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    TestCase.assertEquals("step#" + state, 
                          "com.scalagent.task.StatusNotification",
                          not.getClass().getName());
    TestCase.assertEquals("step#" + state, monitor, from);
    if (state == 0) {
      // StatusNotification,monitor,status=RUN,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 1) {
      // StatusNotification,monitor,status=RUN,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 2) {
      // StatusNotification,monitor,status=RUN,message=null,result=null
      if (not instanceof StatusNotification) {
        StatusNotification status = (StatusNotification) not;
        TestCase.assertEquals("step#" + state,
                              status.getStatus(), Task.Status.RUN);
      }
    } else if (state == 3) {
      // StatusNotification,monitor,status=DONE,message=null,result=null
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
                            monitor, status.getTask());
      TestCase.assertNull("step#" + state, status.getMessage());
      TestCase.assertNull("step#" + state, status.getResult());
    }

//  System.out.println("step#" + state + "->" + not);
    state += 1;
  }
}
