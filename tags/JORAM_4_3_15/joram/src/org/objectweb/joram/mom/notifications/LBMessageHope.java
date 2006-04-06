/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - France Telecom R&D
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.mom.notifications;


public class LBMessageHope extends LBLoadingFactor {

  private int nbMsg = -1;

  public LBMessageHope(long validityPeriode,
                       float rateOfFlow) {
    super(validityPeriode,rateOfFlow);
  }
  
  public void setNbMsg(int nbMsg) {
    this.nbMsg = nbMsg;
  }
  
  public int getNbMsg() {
    return nbMsg;
  }

  public String toString() {
    StringBuffer str = new StringBuffer();
    str.append("LBMessageHope (");
    str.append(super.toString());
    str.append(", nbMsg=");
    str.append(nbMsg);
    str.append(")");
    return str.toString();
  }
}
