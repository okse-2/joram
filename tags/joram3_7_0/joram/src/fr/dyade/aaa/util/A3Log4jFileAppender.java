/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.util;

import java.io.*;
import java.util.*;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import fr.dyade.aaa.agent.AgentServer;

/**
 * A3Log4jFileAppender appends log events to a file depending of the
 * agent server id.
 */
public class A3Log4jFileAppender extends FileAppender {
  /** RCS version number of this file: $Revision: 1.5 $ */
  public static final String RCS_VERSION="@(#)$Id: A3Log4jFileAppender.java,v 1.5 2003-09-11 09:54:24 fmaistre Exp $";

  /**
   * The default constructor does not do anything. 
   */
  public A3Log4jFileAppender() {
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