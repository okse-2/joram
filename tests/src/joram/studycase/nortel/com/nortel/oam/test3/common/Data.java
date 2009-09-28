package com.nortel.oam.test3.common;

import java.io.Serializable;

public class Data implements Serializable {
  private int id;
  private String payload;

  public Data(int id, String payload) {
    this.id = id;
    this.payload = payload;
  }

  public int getId() {
    return id;
  }

  public String getPayload() {
    return payload;
  }

  public String toString() {
    return id + " : " + payload;
  }
}
