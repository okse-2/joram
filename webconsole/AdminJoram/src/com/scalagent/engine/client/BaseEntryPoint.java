package com.scalagent.engine.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

public interface BaseEntryPoint extends EntryPoint {

  public static final SharedMessages baseMessages = (SharedMessages) GWT.create(SharedMessages.class);
  
}
