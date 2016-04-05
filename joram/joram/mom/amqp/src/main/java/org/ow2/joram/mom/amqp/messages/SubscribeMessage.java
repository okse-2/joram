package org.ow2.joram.mom.amqp.messages;

public class SubscribeMessage {
    private String host;
    private int port;
    private String exchange;
    private String routingKey;

    public SubscribeMessage(String exchange, String routingKey, String host, int port) {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }
}
