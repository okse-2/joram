/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.mom; 

import java.lang.*; 
import java.util.*;  
 
/** 
 * A <code>MessageMOMExtern</code> is used for the client - 
 * MOM communications.
 * <br>
 * Attributes are the request ID and a key identifying 
 * either the <code>DriverIn</code> through which it reached
 * the MOM, or the <code>DriverOut</code> it must go through
 * to reach the external client.
 *
 * @author  Frederic Maistre
 */ 
public class MessageMOMExtern implements java.io.Serializable
{ 
  private long requestID;
  private int driverKey;

  /**
   * Constructor.
   *
   * @param requestID Request identifier.
   */
  protected MessageMOMExtern(long requestID) {
    this.requestID = requestID;
  }

  /**
   * Constructor.
   *
   * @param requestID Request identifier.
   * @param driversKey Connection set identifier.
   */
  protected MessageMOMExtern(long requestID, int driverKey)
  {
    this.requestID = requestID;
    this.driverKey = driverKey;
  }

  /** Method setting the driversKey attribute. */
  public void setDriverKey(int driverKey)
  {
    this.driverKey = driverKey;
  }

  /** Method returning the request ID. */
  public long getMessageMOMExternID()
  {
    return requestID;
  }

  /** Method returning the driversKey attribute. */
  public int getDriverKey()
  {
    return driverKey;
  }

}
