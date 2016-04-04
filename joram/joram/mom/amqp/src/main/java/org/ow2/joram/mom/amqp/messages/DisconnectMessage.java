package org.ow2.joram.mom.amqp.messages;

public class DisconnectMessage extends AbstractConnectMessage {

    public DisconnectMessage(String host, int port, ClientType clientType) {
        super(host, port, clientType);
    }
}
