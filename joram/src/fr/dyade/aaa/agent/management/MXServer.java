/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.agent.management;

import java.io.*;
import java.util.*;

public interface  MXServer {
  public void registerMBean(Object bean,
                            String domain,
                            String name,
                            String type,
                            String desc) throws Exception;

  public void registerMBean(Object bean,
                            String name,
                            String type,
                            String desc) throws Exception;

  public void unregisterMBean(String domain,
                              String name,
                              String type,
                              String desc) throws Exception;

  public void unregisterMBean(String name,
                              String type,
                              String desc) throws Exception;
}
