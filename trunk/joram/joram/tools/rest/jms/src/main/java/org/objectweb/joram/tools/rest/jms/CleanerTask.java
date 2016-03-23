/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2016 ScalAgent Distributed Technologies
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
package org.objectweb.joram.tools.rest.jms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

public class CleanerTask implements Callable<Boolean> {

  public static Logger logger = Debug.getLogger(CleanerTask.class.getName());

  private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
  private int period = 60;//in s
  private int timeOut = 3600;//in s
  private ScheduledFuture<?> callableHandle;
  private HashMap<String, RestClientContext> restClientCtxs = Helper.getInstance().getRestClientCtxs();
  private Helper helper = Helper.getInstance();

  /**
   * @return the period
   */
  public int getPeriod() {
    return period;
  }

  /**
   * @param period the period to set
   */
  public void setPeriod(int period) {
    this.period = period;
  }

  /**
   * @return the timeOut
   */
  public int getTimeOut() {
    return timeOut;
  }

  /**
   * @param timeOut the timeOut to set
   */
  public void setTimeOut(int timeOut) {
    this.timeOut = timeOut;
  }

  public void start() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "cleanerTask.start period = " + period);
    if (scheduler != null && scheduler.isTerminated())
      scheduler = Executors.newScheduledThreadPool(2);

    if (callableHandle != null && !callableHandle.isCancelled()) {
      callableHandle.cancel(true);
    }
    callableHandle = scheduler.scheduleAtFixedRate(new Runnable() {
      public void run() {
        submitCallTask();
      }
    },  0, period, TimeUnit.SECONDS);
  }

  public void stop() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "cleanerTask.stop");
    if (callableHandle != null)
      callableHandle.cancel(true);
    scheduler.shutdown();
  }

  public void submitCallTask() {
    Boolean res = false;
    Future<Boolean> future = scheduler.submit(this);
    try {
      res = future.get(timeOut, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "==== cleanerTask.submitCallTask InterruptedException");
    } catch (ExecutionException e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "==== cleanerTask.submitCallTask ExecutionException");
    } catch (TimeoutException e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "==== cleanerTask.submitCallTask TimeoutException");
      future.cancel(true);
    }
  }

  @Override
  public Boolean call() throws Exception {
    ArrayList<String> toClose = new ArrayList<String>();
    for (RestClientContext restClientCtx : restClientCtxs.values()) {
      if (restClientCtx.getIdleTimeout() < 1) {
        // never close
        continue;
      }
//      if (logger.isLoggable(BasicLevel.DEBUG))
//        logger.log(BasicLevel.DEBUG, "cleanerTask.call : " + restClientCtx.getClientId() + ", idleTimeout = " + restClientCtx.getIdleTimeout());
      if (restClientCtx.getLastActivity() + restClientCtx.getIdleTimeout() < System.currentTimeMillis()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "cleanerTask.call close : " + restClientCtx.getClientId());
        if (!toClose.contains(restClientCtx.getClientId()))
          toClose.add(restClientCtx.getClientId());
      }
    }

    for (String clientId : toClose) {
      helper.close(clientId);
    }
    return true;
  }

}
