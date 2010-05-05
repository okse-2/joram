/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

import fr.dyade.aaa.agent.*;
import com.scalagent.scheduler.monitor.IndexedCommand;

/**
  * Test command notification holding a count.
  */
public class IcountCommand extends IndexedCommand {
  private int count;

  /**
   * Creates a notification to be sent.
   *
   * @param name		name of target entry of the command
   */
  public IcountCommand(int count) {
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
