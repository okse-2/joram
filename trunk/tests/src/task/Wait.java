/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

public class Wait {
  public static void main (String args[]) throws Exception {
    long timeout = 10L;
    try {
      timeout = Long.parseLong(args[0]);
    } catch (Exception exc) {}
    Thread.currentThread().sleep(timeout * 1000L);
    System.exit(0);
  }
}
