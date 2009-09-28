package com.nortel.oam.test3.common;

public class Props {
  public static final short serverId = (short) 0;
  public static final String topicFactoryName = "topicFactoryName";
  public static final String mainTopicName = "mainTopic";
  public static final String queueFactoryName = "queueFactoryName";
  public static final String requestQueueName = "requestQueue";
  public static int msgs_count_per_cycle = 4000;

  public static final int TIMEOUT = 5; // sec 

  public static final int PRODUCER_DELAY = 1; // msec
  public static final int CONSUMER_DELAY = 3; // msec
  public static final int CONNECTING_TIMER = 90; // sec
  public static final int CNX_PENDING_TIMER = 500; // msec
}
