/*
 * Copyright (C) 2001 SCALAGENT
 */
package fr.dyade.aaa.util;

import java.util.*;

public class ConditionnalString implements java.io.Serializable {

  public static final String RCS_VERSION="@(#)$Id: ConditionnalString.java,v 1.3 2002-03-06 16:58:48 joram Exp $";

  public static ConditionnalString valueOf(String s) throws Exception {
    StringTokenizer tokenizer = new StringTokenizer(s, "(),");
    int tokenNb = tokenizer.countTokens();
    String value = "";
    if (tokenNb == 2) {
      value = tokenizer.nextToken();
    }
    String c = tokenizer.nextToken();
    boolean condition = Boolean.valueOf(c).booleanValue();
    ConditionnalString cs = new ConditionnalString(value, condition);
    return cs;
  }

  public String value = null;
  public boolean condition = false;

  public ConditionnalString(String value, boolean condition) {
    this.value = value;
    this.condition = condition;
  }

  public String toString() {
    return '(' + value + ',' + condition + ')';
  }
}
