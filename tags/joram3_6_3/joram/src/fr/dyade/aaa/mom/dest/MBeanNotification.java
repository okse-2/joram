/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.dest;

import fr.dyade.aaa.mom.admin.AdminRequest;


/**
 * A <code>MBeanNotification</code> is a notification sent by a
 * MBean to the administration topic and wrapping an administration request.
 */
public class MBeanNotification extends fr.dyade.aaa.agent.Notification
{
  /** The wrapped administration request. */
  AdminRequest request;

  
  /**
   * Constructs a <code>MBeanNotification</code> wrapping a given
   * administration request.
   */
  protected MBeanNotification(AdminRequest request)
  {
    this.request = request;
  }
}
