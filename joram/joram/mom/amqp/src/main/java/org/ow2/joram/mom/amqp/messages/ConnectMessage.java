package org.ow2.joram.mom.amqp.messages;

public class ConnectMessage extends AbstractConnectMessage {

    public ConnectMessage(String host, int port, ClientType clientType) {
        super(host, port, clientType);
    }
}
