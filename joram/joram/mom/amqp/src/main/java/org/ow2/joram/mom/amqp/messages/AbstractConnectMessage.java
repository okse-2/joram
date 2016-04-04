package org.ow2.joram.mom.amqp.messages;

abstract class AbstractConnectMessage {

    public enum ClientType {
        Subscriber,
        Publisher
    }

    private String host;
    private int port;
    private ClientType clientType;

    public AbstractConnectMessage(String host, int port, ClientType clientType) {
        this.host = host;
        this.port = port;
        this.clientType = clientType;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return  port;
    }

    public boolean isPublisher() {
        return clientType == ClientType.Publisher;
    }

    public boolean isSubscriber() {
        return clientType == ClientType.Subscriber;
    }
}
