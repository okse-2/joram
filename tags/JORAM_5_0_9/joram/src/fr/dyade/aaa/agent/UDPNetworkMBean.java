package fr.dyade.aaa.agent;

public interface UDPNetworkMBean extends NetworkMBean {

  public int getSocketReceiveBufferSize();

  public int getSocketSendBufferSize();

  public void setSocketReceiveBufferSize(int size);

  public void setSocketSendBufferSize(int size);

}
