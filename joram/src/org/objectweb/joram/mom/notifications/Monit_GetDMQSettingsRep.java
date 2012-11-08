/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.notifications;


/**
 * A <code>Monit_GetDMQSettingsRep</code> instance is used by a destination
 * or a proxy for sending to an administrator client its DMQ settings.
 */
public class Monit_GetDMQSettingsRep extends AdminReply
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** DMQ identifier. */
  private String dmqId;
  /** Threshold. */
  private Integer threshold;


  /**
   * Constructs a <code>Monit_GetDMQSettingsRep</code> instance.
   *
   * @param request  The request this reply replies to.
   * @param dmqId  DMQ identifier.
   * @param threshold  Threshold.
   */
  public Monit_GetDMQSettingsRep(AdminRequest request,
                                 String dmqId,
                                 Integer threshold)
  {
    super(request, true, null);
    this.dmqId = dmqId;
    this.threshold = threshold;
  }


  /** Returns the DMQ identifier. */
  public String getDMQId()
  {
    return dmqId;
  }

  /** Returns the threshold. */
  public Integer getThreshold()
  {
    return threshold;
  }
}
