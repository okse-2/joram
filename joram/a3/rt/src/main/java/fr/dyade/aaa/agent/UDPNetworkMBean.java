package fr.dyade.aaa.agent;

import java.net.SocketException;

public interface UDPNetworkMBean extends NetworkMBean {

  public int getSocketReceiveBufferSize() throws SocketException;

  public int getSocketSendBufferSize() throws SocketException;

}
