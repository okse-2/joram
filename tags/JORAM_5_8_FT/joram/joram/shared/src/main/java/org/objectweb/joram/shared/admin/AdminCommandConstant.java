/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.joram.shared.admin;

public class AdminCommandConstant {

  public static final int CMD_NO = 0;
  public static final int CMD_ADD_INTERCEPTORS = 1;
  public static final int CMD_REMOVE_INTERCEPTORS = 2;
  public static final int CMD_GET_INTERCEPTORS = 3;
  public static final int CMD_REPLACE_INTERCEPTORS = 4;
  public static final int CMD_SET_PROPERTIES = 5;
  public static final int CMD_START_HANDLER = 6;
  public static final int CMD_STOP_HANDLER = 7;
  public static final int CMD_INVOKE_STATIC = 8;

  public static final String[] commandNames = { "CMD_NO", "CMD_ADD_INTERCEPTORS", "CMD_REMOVE_INTERCEPTORS",
      "CMD_GET_INTERCEPTORS", "CMD_REPLACE_INTERCEPTORS", "CMD_SET_PROPERTIES", "CMD_START_HANDLER",
      "CMD_STOP_HANDLER", "CMD_INVOKE_STATIC" };

  /** use by destination */
  public static final String INTERCEPTORS = "jms_joram_interceptors";

  /** use by UserAgent */
  public static final String INTERCEPTORS_IN = "jms_joram_interceptors_in";

  /** use by UserAgent */
  public static final String INTERCEPTORS_OUT = "jms_joram_interceptors_out";

  /** use by destination to replace interceptor */
  public static final String INTERCEPTORS_NEW = "jms_joram_interceptors_new";

  /** use by destination to replace interceptor */
  public static final String INTERCEPTORS_OLD = "jms_joram_interceptors_old";

  /** use by UserAgent to replace interceptor IN */
  public static final String INTERCEPTORS_IN_NEW = "jms_joram_interceptors_in_new";

  /** use by UserAgent to replace interceptor IN */
  public static final String INTERCEPTORS_IN_OLD = "jms_joram_interceptors_in_old";

  /** use by UserAgent to replace interceptor OUT */
  public static final String INTERCEPTORS_OUT_NEW = "jms_joram_interceptors_out_new";

  /** use by UserAgent to replace interceptor OUT */
  public static final String INTERCEPTORS_OUT_OLD = "jms_joram_interceptors_out_old";

  /** Used by AdminTopic to invoke a static method */
  public static final String INVOKE_CLASS_NAME = "jms_joram_invoke_class";

  /** Used by AdminTopic to invoke a static method */
  public static final String INVOKE_METHOD_NAME = "jms_joram_invoke_method";

  /** Used by AdminTopic to invoke a static method */
  public static final String INVOKE_METHOD_ARG = "jms_joram_invoke_arg";

  /** Used by AdminTopic to invoke a static method */
  public static final String INVOKE_METHOD_ARG_VALUE = "jms_joram_invoke_arg_value";

  /** Used by AdminTopic to invoke a static method */
  public static final String INVOKE_METHOD_RESULT = "jms_joram_invoke_result";
}
