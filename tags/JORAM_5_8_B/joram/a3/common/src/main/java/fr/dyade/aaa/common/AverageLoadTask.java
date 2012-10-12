/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package fr.dyade.aaa.common;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This class computes the load average of the server using Unix algorithm.
 * This task needs to be scheduled every 5 seconds.
 */
public abstract class AverageLoadTask extends TimerTask {
  /** number of bits of precision */
  private final static long FSHIFT = 11;   
  /** 1.0 as fixed-point */
  private final static long FIXED_1 =  (1<<FSHIFT);  
  /** 1/exp(5sec/1min) as fixed-point */
  private final static long EXP_1 =  1884;   
  /** 1/exp(5sec/5min) */
  private final static long EXP_5 =  2014;   
  /** 1/exp(5sec/15min) */
  private final static long EXP_15 = 2037;   

  /** load averages for the last minute. */
  long averageLoad1 = 0;
  
  /**
   * Returns the load averages for the last minute.
   * @return the load averages for the last minute.
   */
  public float getAverageLoad1() {
    return convert(averageLoad1);
  }

  /** load averages for the past 5 minutes. */
  long averageLoad5 = 0;
  
  /**
   * Returns the load averages for the past 5 minutes.
   * @return the load averages for the past 5 minutes.
   */
  public float getAverageLoad5() {
    return convert(averageLoad5);
  }
  
  /** load averages for the past 15 minutes. */
  long averageLoad15 = 0;
  
  /**
   * Returns the load averages for the past 15 minutes.
   * @return the load averages for the past 15 minutes.
   */
  public float getAverageLoad15() {
    return convert(averageLoad15);
  }
  
  float convert(long average) {
    return ((float) average) / ((float) FIXED_1);
  }
  
  long computeLoad(long load, long exp, long n) {
    load *= exp;
    load += n*(FIXED_1-exp);
    load >>= FSHIFT;

    return load;
  }

  protected abstract long countActiveTasks();
  
  /**
   * @see java.util.TimerTask#run()
   */
  @Override
  public void run() {
    long active_tasks = countActiveTasks() * FIXED_1;
    
    averageLoad1 = computeLoad(averageLoad1, EXP_1, active_tasks);
    averageLoad5 = computeLoad(averageLoad5, EXP_5, active_tasks);
    averageLoad15 = computeLoad(averageLoad15, EXP_15, active_tasks);
  }
  
  public void reset() {
    averageLoad1 = 0L;
    averageLoad5 = 0L;
    averageLoad15 = 0L;
  }
  
  /**
   * Starts the resulting task.
   * 
   * @param timer Timer to use to schedule the resulting task.
   */
  protected final void start(Timer timer) {
    timer.scheduleAtFixedRate(this, 5000L, 5000L);
  }
}

