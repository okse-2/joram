package com.nortel.oam.test1.common;

import java.util.List;

public class SpeedMeter {
  private List msgList;
  private MessagesRateSetter rateSetter;

  public SpeedMeter(List msgList, MessagesRateSetter rateSetter) {
    this.msgList = msgList;
    this.rateSetter = rateSetter;
  }

  public void start() {
    Thread thread = new Thread("SpeedMeter") {
      public void run() {
        try {
          long startT = System.currentTimeMillis();
          float startS = msgList.size();
          long endT = 0;
          float endS = 0;
          float rate = 0;
          while (true) {
            synchronized (msgList) {
              endS = msgList.size();
              endT = System.currentTimeMillis();
            }
            if (endT > startT) {
              rate = ((endS - startS) / (endT - startT)) * 1000;
              rateSetter.setMessagesRate(rate);
            }
            startT = endT;
            startS = endS;
            Thread.sleep(500);
          }
        } catch (Throwable e) {
          System.err.println("SpeedMeter failure:");
          e.printStackTrace();
        }
      }
    };
    thread.setDaemon(true);
    thread.start();
  }
}
