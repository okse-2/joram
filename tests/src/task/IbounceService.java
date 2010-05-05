/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import java.io.*;
import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.*;
import com.scalagent.scheduler.monitor.*;
import com.scalagent.task.Task;

/**
  * Similar to <code>BounceService</code> with <code>IndexedCommand</code>s
  * and <code>IndexedReport</code>s instead of <code>Command</code>s and
  * <code>Report</code>s.
  *
  * @see	BounceService
  */
public class IbounceService extends Agent {
  private int count;	/** count of received commands */

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   */
  public IbounceService(short to) {
    super(to);
    count = 0;
  }

  /**
   * Provides a string image for this object.
   */
  public String toString() {
    return "(" + super.toString() + ",count=" + count + ")";
  }

  /**
   * Reacts to notifications.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof IcountCommand) {
      checkCount(from, (IcountCommand) not);
    } else {
      super.react(from, not);
    }
  }

  /**
    * Checks command count agains private count. Sends back a report.
    */
  void checkCount(AgentId from, IcountCommand command) throws IOException {
    IndexedReport report = null;
    count ++;
    if (command.getCount() == count) {
      report = new IndexedReport(command.getId(), Task.Status.DONE, null, null);
    } else {
      report = new IndexedReport(command.getId(), Task.Status.FAIL, "bad count: " + count, null);
    }
    sendTo(from, report);
  }
}
