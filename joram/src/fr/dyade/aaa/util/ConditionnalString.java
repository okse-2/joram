/*
 * Copyright (C) 2001 SCALAGENT
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 */
package fr.dyade.aaa.util;

import java.util.*;

public class ConditionnalString implements java.io.Serializable {


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
