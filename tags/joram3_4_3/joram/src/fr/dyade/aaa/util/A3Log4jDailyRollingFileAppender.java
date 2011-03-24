/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.util;

import java.io.*;
import java.util.*;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import fr.dyade.aaa.agent.AgentServer;

/**
 * A3Log4jFileAppender appends log events to a file depending of the
 * agent server id.
 */
public class A3Log4jDailyRollingFileAppender extends DailyRollingFileAppender {
  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: A3Log4jDailyRollingFileAppender.java,v 1.3 2003-03-19 15:19:04 fmaistre Exp $";

  /**
   * The default constructor does not do anything. 
   */
  public A3Log4jDailyRollingFileAppender() {
    super();
    short id = AgentServer.getServerId();
    if (id != -1) {
      fileName = "server#" + id + ".audit";
    } else if (fileName == null) {
      try {
        fileName = File.createTempFile("client", ".audit", new File(".")).getPath();
      } catch (Exception exc) {
        fileName = "client.audit";
      }
    }
    activateOptions();
  }
}