package com.nortel.oam.test2.common;

public class Props {
  public static final short serverId = (short) 0;
  public static final String topicFactoryName = "mainTopicFactory";
  public static final String mainTopicName = "mainTopic";
  public static final String queueFactoryName = "queueTopicFactory";
  public static final String requestQueueName = "requestQueue";
  public static int msgs_count_per_cycle = 100;

  public static final int TIMEOUT = 5; // sec 

  public static final int PRODUCER_DELAY = 10; // msec
  public static final int CONSUMER_DELAY = 50; // msec
  public static final int CONNECTING_TIMER = 90; // sec
  public static final int CNX_PENDING_TIMER = 20; // msec

}
