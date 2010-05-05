/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 */
package task;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.util.Command;

/**
  * Test command notification holding a count.
  */
public class CountCommand extends Command {
  private int count;

  /**
   * Creates a notification to be sent.
   *
   * @param report		agent to report status to
   * @param name		name of target entry of the command
   */
  public CountCommand(AgentId report, int count) {
    super(report);
    this.count = count;
  }

  /**
    * Accesses read only property.
    */
  public int getCount() { return count; }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output buffer to fill in
   * @return resulting buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",count=").append(count);
    output.append(')');

    return output;
  }
}
