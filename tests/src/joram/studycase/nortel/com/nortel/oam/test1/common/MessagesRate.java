package com.nortel.oam.test1.common;

public class MessagesRate implements MessagesRateGetter, MessagesRateSetter {
  private float msgRate;

  public MessagesRate(float msgRate) {
    this.msgRate = msgRate;
  }

  public float getMessagesRate() {
    return msgRate;
  }

  synchronized public void setMessagesRate(float msgRate) {
    if (msgRate >= 0 && msgRate < 1001) {
       this.msgRate = msgRate;
      System.out.println("\nNew messages rate taken into account: " + msgRate + " msgs/sec");
    }
  }
}
