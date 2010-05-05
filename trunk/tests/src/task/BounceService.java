/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import java.io.*;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.Task;
import com.scalagent.task.util.Report;

/**
  * Test Agent whose purpose is to answers <code>CountCommand</code>s
  * with <code>Report</code>s. The <code>CountCommand</code> holds an
  * ordering number which this object checks to equal the internal count
  * of commands.
  *
  * @see	CountCommand
  */
public class BounceService extends Agent {
  private int count;	/** count of received commands */

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   */
  public BounceService(short to) {
    super(to);
    count = 0;
  }

  /**
   * Provides a string image for this object.
   */
  public String toString() {
    return "(" + super.toString() +  ",count=" + count + ")";
  }

  /**
   * Reacts to notifications.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof CountCommand) {
      checkCount((CountCommand) not);
    } else {
      super.react(from, not);
    }
  }

  /**
    * Checks command count agains private count. Sends back a report.
    */
  void checkCount(CountCommand command) throws IOException {
    Report report = null;
    count ++;
    if (command.getCount() == count) {
      report = new Report(command, Task.Status.DONE, null);
    } else {
      report = new Report(command, Task.Status.FAIL, "bad count: " + count);
    }
    sendTo(command.getReport(), report);
  }
}
