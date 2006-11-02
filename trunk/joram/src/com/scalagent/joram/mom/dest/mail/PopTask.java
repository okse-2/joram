/*
 * Copyright (C) 2005 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.mail;

import fr.dyade.aaa.util.TimerTask;
import fr.dyade.aaa.util.Timer;
import org.objectweb.joram.client.jms.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Timer task responsible for doing a pop.
 */
public class PopTask extends TimerTask {    
  
  private Timer timer;
  private long popPeriod;
  private JavaMailDest dest;
  
  public PopTask(JavaMailDest dest, long popPeriod) {
    timer = new Timer();
    this.popPeriod = popPeriod;
    this.dest = dest;
  }
  
  public void run() {
    if (System.currentTimeMillis() > popPeriod) {
      dest.doPop();
    }
    try {
      timer.schedule(this, popPeriod);
    } catch (Exception exc) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgClient.log(BasicLevel.ERROR, "", exc);
    }
  }
}
