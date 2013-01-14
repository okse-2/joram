/*
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

public interface NetworkMBean {
  /**
   * Returns this <code>Engine</code>'s name.
   *
   * @return this <code>Engine</code>'s name.
   */
  public String getName();

  /**
   * Tests if the engine is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning();

  /** Causes this engine to begin execution */
  public void start() throws Exception;

  /** Forces the engine to stop executing */
  public void stop();

  /**
   * Returns a string representation of this consumer.
   *
   * @return	A string representation of this consumer. 
   */
  public String toString();

  /**
   * Gets the WDActivationPeriod value.
   *
   * @return the WDActivationPeriod value
   */
  public long getWDActivationPeriod();

  /**
   * Sets the WDActivationPeriod value.
   *
   * @param WDActivationPeriod	the WDActivationPeriod value
   */
  public void setWDActivationPeriod(long WDActivationPeriod);

  /**
   * Gets the WDNbRetryLevel1 value.
   *
   * @return the WDNbRetryLevel1 value
   */
  public int getWDNbRetryLevel1();

  /**
   * Sets the WDNbRetryLevel1 value.
   *
   * @param WDNbRetryLevel1	the WDNbRetryLevel1 value
   */
  public void setWDNbRetryLevel1(int WDNbRetryLevel1);

  /**
   * Gets the WDRetryPeriod1 value.
   *
   * @return the WDRetryPeriod1 value
   */
  public long getWDRetryPeriod1();

  /**
   * Sets the WDRetryPeriod1 value.
   *
   * @param WDRetryPeriod1	the WDRetryPeriod1 value
   */
  public void setWDRetryPeriod1(long WDRetryPeriod1);

  /**
   * Gets the WDNbRetryLevel2 value.
   *
   * @return the WDNbRetryLevel2 value
   */
  public int getWDNbRetryLevel2();

  /**
   * Sets the WDNbRetryLevel2 value.
   *
   * @param WDNbRetryLevel2	the WDNbRetryLevel2 value
   */
  public void setWDNbRetryLevel2(int WDNbRetryLevel2);

  /**
   * Gets the WDRetryPeriod2 value.
   *
   * @return the WDRetryPeriod2 value
   */
  public long getWDRetryPeriod2();

  /**
   * Sets the WDRetryPeriod2 value.
   *
   * @param WDRetryPeriod2	the WDRetryPeriod2 value
   */
  public void setWDRetryPeriod2(long WDRetryPeriod2);

  /**
   * Gets the WDRetryPeriod3 value.
   *
   * @return the WDRetryPeriod3 value
   */
  public long getWDRetryPeriod3();

  /**
   * Sets the WDRetryPeriod3 value.
   *
   * @param WDRetryPeriod3	the WDRetryPeriod3 value
   */
  public void setWDRetryPeriod3(long WDRetryPeriod3);

  /**
   * Gets the number of waiting messages in this network.
   *
   * @return	the number of waiting messages.
   */
  public int getNbWaitingMessages();

  /**
   * Returns the number of messages sent since last reboot.
   * 
   * @return  the number of messages sent since last reboot.
   */
  public int getNbMessageSent();

  /**
   * Returns the number of messages received since last reboot.
   * 
   * @return  the number of messages received since last reboot.
   */
  public int getNbMessageReceived();

  /**
   * Returns the load averages for the last minute.
   * @return the load averages for the last minute.
   */
  public float getAverageLoad1();

  /**
   * Returns the load averages for the past 5 minutes.
   * @return the load averages for the past 5 minutes.
   */
  public float getAverageLoad5();
  
  /**
   * Returns the load averages for the past 15 minutes.
   * @return the load averages for the past 15 minutes.
   */
  public float getAverageLoad15();
}
