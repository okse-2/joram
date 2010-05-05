/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
 */
package task;

public class Hello {
  public static void main (String args[]) throws Exception {
    if (args.length == 0) {
      System.err.println("usage: Hello <name>");
      System.exit(1);
    }
    System.out.println("hello " + args[0] + " !");
    System.exit(0);
  }
}
