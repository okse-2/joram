package fr.dyade.aaa.agent;

import fr.dyade.aaa.util.*;
import fr.dyade.aaa.agent.conf.*;

public class SCServer implements SCServerMBean {
  public SCServer() {
  }

  public short getServerId() {
    return AgentServer.getServerId();
  }

  public String getName() {
    return AgentServer.getName();
  }

  public boolean isTransient() {
    return AgentServer.isTransient();
  }

  public void start() {
    try {
      AgentServer.start();
    } catch (Throwable exc) {
    }
  }

  public void stop() {
    AgentServer.stop();
  }

  public int getStatus() {
    return AgentServer.getStatus();
  }

  public String getStatusInfo() {
    return AgentServer.getStatusInfo();
  }
}
