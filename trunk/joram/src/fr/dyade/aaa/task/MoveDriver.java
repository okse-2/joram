/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */
package fr.dyade.aaa.task;

import java.io.*;
import fr.dyade.aaa.agent.*;

/**
 * A <code>MoveDriver</code> object is used by a <code>MoveFile</code>
 * task agent to rename or copy a file.
 * <p>
 * A <code>MoveDriver</code> may be safely executed more than once, after
 * either a partial or a total execution.
 *
 * @see		MoveFile
 */
public class MoveDriver extends ThreadTaskDriver {
  public static final String RCS_VERSION="@(#)$Id: MoveDriver.java,v 1.1 2002-03-06 16:52:20 joram Exp $";

  /** original file */
  protected File input;

  /** target file */
  protected File output;

  /** if true duplicates the file */
  public boolean duplicate = false;

  /**
   * If an actual copy of the file is needed, the <code>stop</code> variable
   * is periodically checked every time this number of bytes have been written.
   * Current value is 1Mo.
   */
  public int checkForStop = 0x100000;

  /**
   * Constructor.
   *
   * @param task	id of associated <code>ThreadTask</code> agent
   * @param input	original file
   * @param output	target file
   * @param duplicate	if true makes a copy
   */
  public MoveDriver(AgentId task,
                    File input, File output,
                    boolean duplicate) {
    super(task);
    this.input = input;
    this.output = output;
    this.duplicate = duplicate;
  }


  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",input=" + input +
      ",output=" + output + ")";
  }

  /**
   * Actually executes the driver code.
   * To be defined in derived classes.
   * <p>
   * Beware: this code is executed in a separate thread, outside from any
   * transaction. Notifications may be sent using function <code>sendTo</code>,
   * and they will actually be sent as soon as the function is called; there is
   * no atomic treatment as there is in an agent reaction.
   * <p>
   * May update <code>errorMessage</code>, a not null value meaning failure.
   * <p>
   * Should regularly check for variable <code>stop</code>, and exit the thread
   * when it is <code>true</code>.
   */
  public void run() throws Exception {
    if (! input.exists()) {
      // assumes a previous execution already moved the file
      return;
    }

    if (output.equals(input)) {
      // nothing to do
      return;
    }

    if (!duplicate && input.renameTo(output))
      return;

    // renaming may have failed because the two files do not belong to
    // the same file system, tries to copy the file
    BufferedReader in = null;
    BufferedWriter out = null;
    int bytes = 0;
    try {
      in = new BufferedReader(new FileReader(input));
      out = new BufferedWriter(new FileWriter(output));
      copy_loop:
      while (true) {
	int c = in.read();
	if (c == -1)
	  break copy_loop;
	out.write(c);
	// checks for a stop request
	bytes ++;
	if (bytes >= checkForStop) {
	  if (stop) {
	    in.close();
	    in = null;
	    out.close();
	    out = null;
	    output.delete();
	    return;
	  }
	  bytes = 0;
	}
      }
    } finally {
      if (in != null)
	in.close();
      if (out != null)
	out.close();
    }
    if (!duplicate) input.delete();
  }

  public void close() {}
}
