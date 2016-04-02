package org.ow2.joram.mom.amqp;

import org.ow2.joram.mom.amqp.messages.*;

public interface AMQPMessageListener {
    void onConnect(ConnectMessage message);
    void onDisconnect(DisconnectMessage message);
    void onMessageReceived(MessageReceived message);
    void onSubscribe(SubscribeMessage message);
    void onUnsubscribe(UnsubscribeMessage message);
}
