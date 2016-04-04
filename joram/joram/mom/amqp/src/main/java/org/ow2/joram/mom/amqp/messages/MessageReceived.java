package org.ow2.joram.mom.amqp.messages;

public class MessageReceived {

    private final byte[] body;
    private final String host;
    private final int port;
    private final String routingKey;
    private String exchange;

    public MessageReceived(String exchange, String routingKey, byte[] body, String host, int port) {
        this.body = body;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.host = host;
        this.port = port;
    }

    public byte[] getBody() {
        return body;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
