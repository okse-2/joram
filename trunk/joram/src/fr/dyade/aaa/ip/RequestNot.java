package fr.dyade.aaa.ip;

import fr.dyade.aaa.agent.Notification;

public class RequestNot extends Notification  {
  int id;

  public RequestNot(Request request) {
    this.id = request.id;
  }
}
