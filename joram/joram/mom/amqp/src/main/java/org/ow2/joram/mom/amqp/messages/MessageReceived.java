package org.ow2.joram.mom.amqp.messages;

public class MessageReceived {

    private final byte[] body;
    private final String host;
    private final int port;
    private String topic;

    public MessageReceived(byte[] body, String topic, String host, int port) {
        this.body = body;
        this.topic = topic;
        this.host = host;
        this.port = port;
    }

    public byte[] getBody() {
        return body;
    }

    public String getTopic() {
        return topic;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
