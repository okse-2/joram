package com.scalagent.testctrlgreen;

import java.util.Properties;

import com.scalagent.ctrlgreen.Parameters;

public class MyParameters extends Parameters {
  public Properties parameters = null;
  
  public MyParameters(Properties parameters) {
    this.parameters = parameters;
  }
  
  public String toString() {
    return parameters.toString();
  }
}
