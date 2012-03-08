/*
 * Copyright (C) 2001 - 2011 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.util.management;

import java.util.List;
import java.util.Set;

public interface MXServer {
  public void registerMBean(Object bean, String name) throws Exception;

  public void unregisterMBean(String name) throws Exception;
  
  public Object getAttribute(String objectName, String attribute) throws Exception;
    
  public List<String> getAttributeNames(String objectName) throws Exception;
  
  public Set<String> queryNames(String objectName) throws Exception;
}
