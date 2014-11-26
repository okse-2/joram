/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package cron;

import java.util.Date;
import java.util.Timer;

import com.scalagent.scheduler.CronEvent;
import com.scalagent.scheduler.Scheduler;

public class CronTest {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    System.out.println(new Date(System.currentTimeMillis()) + " : Cron test wait (1 minute) ...");
    Timer timer = new Timer();
   Scheduler scheduler = new Scheduler(timer);
   //<minutes> <hours> <days of month> <months> <days of week>
   String cronDate = "* * * * *"; // all minutes.
   scheduler.scheduleEvent(new CronEvent("cron_test", cronDate), new CronTestTask());
   System.in.read();
  }

}
