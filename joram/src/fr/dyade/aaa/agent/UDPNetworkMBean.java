package fr.dyade.aaa.agent;

import java.net.SocketException;

public interface UDPNetworkMBean extends NetworkMBean {

  public int getSocketReceiveBufferSize() throws SocketException;

  public int getSocketSendBufferSize() throws SocketException;

  public void setSocketReceiveBufferSize(int size) throws SocketException;

  public void setSocketSendBufferSize(int size) throws SocketException;

}
