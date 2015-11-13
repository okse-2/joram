package com.scalagent.testctrlgreen;

import com.scalagent.ctrlgreen.Action;

public class MyAction extends Action {
  public String parameters = null;
  
  public MyAction(int type, String parameters) {
    super(type);
    this.parameters = parameters;
  }
 
  public String toString() {
    return super.toString() + '[' + parameters +']';
  }

}
