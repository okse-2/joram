package com.scalagent.testctrlgreen;

import com.scalagent.ctrlgreen.Inventory;

public class MyInventory extends Inventory {
  public String inventory = null;
  
  public MyInventory(String inventory) {
    this.inventory = inventory;
  }
  
  public String toString() {
    return inventory;
  }
}
