/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2002 INRIA
 * Contact: joram-team@objectweb.org
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
 * USA
 * 
 * Initial developer(s): Jeff Mesnil (jmesnil@inrialpes.fr)
 * Contributor(s): ______________________________________.
 */

package org.objectweb.jtests.jms.admin;

import javax.naming.*;

public interface Admin {
  
  public String getName();

  public InitialContext createInitialContext() 
    throws NamingException;
  
  public void createQueueConnectionFactory(String name);
  public void createTopicConnectionFactory(String name);
 
  public void createQueue(String name);
  public void createTopic(String name);

  public void deleteQueue(String name);
  public void deleteTopic(String name);

  public void deleteQueueConnectionFactory (String name);
  public void deleteTopicConnectionFactory (String name);
}
