package fr.dyade.aaa.agent;

public interface CallbackNotification {

  Runnable getCallback();

  void setCallback(Runnable callback);

}
