/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.joram.mom.amqp;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;
import org.objectweb.joram.mom.amqp.marshalling.AMQPHelper;
import org.objectweb.joram.mom.amqp.marshalling.AbstractMarshallingMethod;
import org.objectweb.joram.mom.amqp.marshalling.Frame;
import org.objectweb.joram.mom.amqp.marshalling.LongString;
import org.objectweb.joram.mom.amqp.marshalling.LongStringHelper;
import org.objectweb.joram.mom.amqp.marshalling.MarshallingBody;
import org.objectweb.joram.mom.amqp.marshalling.MarshallingHeader;
import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Daemon;
import fr.dyade.aaa.util.Debug;
import fr.dyade.aaa.util.Queue;

/**
 * Listens to the TCP connections.
 */
public class AMQPConnectionListener extends Daemon implements Consumer {

  public static Logger logger = Debug.getLogger(AMQPConnectionListener.class.getName());

  /** The server socket listening to connections from the AMQP peer. */
  private volatile ServerSocket serverSocket;

  /** Used to interact with the MOM. */
  private MOMHandler momHandler;

  /** The socket used to listen. */
  private Socket sock;
  
  /** Timeout when listening on the socket. */
  private int timeout;

  /** The channel contexts. */
  private Map channelContexts;
  
  /** The heart beat period in seconds. */
  private int heartBeat = 300;

  private static final int NO_STATE = -1;
  private int classState = NO_STATE;
  private int methodState = NO_STATE;
  
  private Queue queueOut;
  private NetServerOut netServerOut;

  /**
   * Creates a new connection listener.
   * 
   * @param serverSocket
   *          the server socket to listen to
   * @param timeout
   *          the socket timeout delay.
   * @param amqpService
   *          service AMQP of this connection listener
   */
  public AMQPConnectionListener(ServerSocket serverSocket, int timeout, MOMHandler momHandler) {
    super("AMQPConnectionListener");
    this.serverSocket = serverSocket;
    this.timeout = timeout;
    this.momHandler = momHandler;
    channelContexts = new HashMap();
    queueOut = new Queue();
  }

  public void run() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPConnectionListener.run()");

    while (running) {
      canStop = true;
      if (serverSocket != null) {
        try {
          acceptConnection();
        } catch (Exception exc) {
          if (running) {
            continue;
          } else {
            break;
          }
        }
      } else {
        return;
      }
    }
  }
  
  private void sendMethodToPeer(AbstractMarshallingMethod method, int channelNumber) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "send method : " + method);
    queueOut.push(AMQPHelper.writeMethod(method, channelNumber));
    classState = method.getClassId();
    methodState = method.getMethodId();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "method sent");
  }

  /**
   * send content header to peer.
   * 
   * @param message
   * @param channelNumber
   * @throws IOException
   */
  private void sendHeaderToPeer(BasicProperties header, long bodySize, int channelNumber)
      throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "sendHeaderToPeer = " + header);
    MarshallingHeader mrashallingHeader = new MarshallingHeader();
    mrashallingHeader.setClassId(AMQP.Basic.INDEX);
    mrashallingHeader.setBodySize(bodySize);
    mrashallingHeader.setBasicProperties(header);
    queueOut.push(AMQPHelper.writeContentHeader(mrashallingHeader, channelNumber));
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "sendHeaderToPeer done.  header = " + header);
  }

  /**
   * Send content body to peer.
   * 
   * @param message
   * @param channelNumber
   * @throws IOException
   */
  private void sendBodyToPeer(byte[] body, int channelNumber) throws IOException {
    if (body != null && body.length != 0) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "sendBodyToPeer = " + new String(body));
      MarshallingBody marshallingBody = new MarshallingBody();
      marshallingBody.setBinPayload(body);
      queueOut.push(AMQPHelper.writeContentBody(marshallingBody, channelNumber));
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "sendBodyToPeer done.");
  }

  /**
   * Proceed this frame, switch by type.
   * 
   * @param frame
   * @throws IOException
   */
  private void process(Frame frame) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "\nproceed frame = " + frame);
    int channelNumber = frame.getChannel();
    switch (frame.getType()) {
    case AMQP.FRAME_METHOD:
      doProcessMethod(AbstractMarshallingMethod.read(frame.getInputStream()), channelNumber);
      break;
    case AMQP.FRAME_HEADER:
      doProcessHeader(frame, channelNumber);
      break;
    case AMQP.FRAME_BODY:
      doProcessBody(frame.getPayload(), channelNumber);
      break;
    case AMQP.FRAME_HEARTBEAT:
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "client FRAME_HEARTBEAT.");
      }
      momHandler.heartbeat(channelNumber);
      queueOut.push(new Frame(AMQP.FRAME_HEARTBEAT, channelNumber));
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "client FRAME_HEARTBEAT.");
      break;

    default:
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "+++++ process frame type nok : " + frame.getType());
      if (logger.isLoggable(BasicLevel.WARN)) {
        logger.log(BasicLevel.WARN, "AMQPConnectionListener.process:: bad type " + frame);
      }
      break;
    }
  }

  private void invalidState(AbstractMarshallingMethod marshallingMethod) throws IOException {
    if (logger.isLoggable(BasicLevel.ERROR))
      logger.log(BasicLevel.ERROR, "BAD STATE (current classState = " + classState + ", methodState = "
          + methodState
        + ") cState = " + marshallingMethod.getMethodId() + ", mState = "
        + marshallingMethod.getMethodId() + " (" + marshallingMethod.getMethodName() + ')');

    throw new IOException("BAD STATE (current classState = " + classState + ", methodState = " + methodState
        + ") cState = " + marshallingMethod.getMethodId() + ", mState = "
        + marshallingMethod.getMethodId() + " (" + marshallingMethod.getMethodName() + ')');
  }

  private void doProcessMethod(AbstractMarshallingMethod method, int channelNumber) throws Exception {
    if (method != null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "+ doProcess marshallingMethod = " + method);
      switch (method.getClassId()) {
      /******************************************************
       * Class Connection
       ******************************************************/
      case AMQP.Connection.INDEX:
        switch (method.getMethodId()) {
        case AMQP.Connection.StartOk.INDEX:
          if (classState == AMQP.Connection.INDEX && methodState == AMQP.Connection.Start.INDEX) {
            // sendConnectionSecureMethod(channelNumber); //TODO
            // remove when sendSecureMethod implemented.
            sendMethodToPeer(new AMQP.Connection.Tune(channelNumber, 10 * 1024, heartBeat), channelNumber);
          } else
            invalidState(method);
          break;

        case AMQP.Connection.SecureOk.INDEX:
          if (classState == AMQP.Connection.INDEX && methodState == AMQP.Connection.Secure.INDEX) {
            //TODO NTA : heartbeat, frameMax
            sendMethodToPeer(new AMQP.Connection.Tune(channelNumber, 10 * 1024, heartBeat), channelNumber);
          } else
            invalidState(method);
          break;

        case AMQP.Connection.TuneOk.INDEX:
          if (classState == AMQP.Connection.INDEX && methodState == AMQP.Connection.Tune.INDEX) {
            methodState = AMQP.Connection.TuneOk.INDEX;
          } else
            invalidState(method);
          break;

        case AMQP.Connection.Open.INDEX:
          if (classState == AMQP.Connection.INDEX && methodState == AMQP.Connection.TuneOk.INDEX) {
            sendMethodToPeer(new AMQP.Connection.OpenOk(sock.getLocalAddress().getHostAddress()),
                channelNumber);
          } else
            invalidState(method);
          break;

        case AMQP.Connection.Close.INDEX:
          sendMethodToPeer(new AMQP.Connection.CloseOk(), channelNumber);
          break;

        case AMQP.Connection.CloseOk.INDEX:
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "CLOSE_OK");
          methodState = NO_STATE;
          classState = NO_STATE;
          break;

        default:
          invalidState(method);
          break;
        }
        break;

      /******************************************************
       * Class Channel
       ******************************************************/
      case AMQP.Channel.INDEX:
        switch (method.getMethodId()) {
        case AMQP.Channel.Open.INDEX:
          channelContexts.put(new Integer(channelNumber), new PublishRequest());
          sendMethodToPeer(new AMQP.Channel.OpenOk(LongStringHelper.asLongString(""+channelNumber)), channelNumber);
          break;

        case AMQP.Channel.Flow.INDEX:
          if (classState == AMQP.Channel.INDEX && methodState == AMQP.Channel.OpenOk.INDEX) {
            // TODO change flow state active/inactive
            sendMethodToPeer(new AMQP.Channel.FlowOk(((AMQP.Channel.Flow) method).active),
                channelNumber);
          } else
            invalidState(method);
          break;

        case AMQP.Channel.FlowOk.INDEX:
          if (classState == AMQP.Channel.INDEX && methodState == AMQP.Channel.Flow.INDEX) {
            //TODO change flow state active/inactive
          } else
            invalidState(method);
          break;

        case AMQP.Channel.Close.INDEX:
          AMQP.Channel.Close close = (AMQP.Channel.Close) method;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Channel close : replyCode=" + close.replyCode + ", replyText="
              + close.replyText + ", classId=" + close.classId + ", methodId=" + close.methodId);
          channelContexts.remove(new Integer(channelNumber));
          sendMethodToPeer(new AMQP.Channel.CloseOk(), channelNumber);
          break;

        case AMQP.Channel.CloseOk.INDEX:
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Channel CLOSE_OK");
          methodState = NO_STATE;
          classState = NO_STATE;
          break;

        default:
          invalidState(method);
          break;
        }
        break;

      /******************************************************
       * Class Access
       ******************************************************/
      case AMQP.Access.INDEX:
        switch (method.getMethodId()) {
        case AMQP.Access.Request.INDEX:
          if (classState == AMQP.Channel.INDEX && methodState == AMQP.Channel.OpenOk.INDEX) {
            AMQP.Access.Request request = (AMQP.Access.Request) method;
            AMQP.Access.RequestOk requestOk = momHandler.accessRequest(
                request.realm,
                request.exclusive,
                request.passive,
                request.active,
                request.write,
                request.read,
                channelNumber);
            sendMethodToPeer(requestOk, channelNumber);
          } else
            invalidState(method);
          break;

        default:
          invalidState(method);
          break;
        }
        break;

      /******************************************************
       * Class Queue
       ******************************************************/
      case AMQP.Queue.INDEX:
        switch (method.getMethodId()) {
        case AMQP.Queue.Declare.INDEX:
          AMQP.Queue.Declare declare = (AMQP.Queue.Declare) method;
          AMQP.Queue.DeclareOk declareOk = momHandler.queueDeclare(
              declare.queue,
              declare.passive,
              declare.durable,
              declare.exclusive,
              declare.autoDelete,
              declare.arguments,
              declare.ticket,
              channelNumber);
          if (declare.nowait == false) {
            sendMethodToPeer(declareOk, channelNumber);
          }
          break;

        case AMQP.Queue.Delete.INDEX:
          AMQP.Queue.Delete delete = (AMQP.Queue.Delete) method;
          AMQP.Queue.DeleteOk deleteOk = momHandler.queueDelete(
              delete.queue,
              delete.ifUnused,
              delete.ifEmpty,
              delete.nowait,
              delete.ticket,
              channelNumber);
          if (delete.nowait == false) {
            sendMethodToPeer(deleteOk, channelNumber);
          }
          break;

        case AMQP.Queue.Bind.INDEX:
          AMQP.Queue.Bind bind = (AMQP.Queue.Bind) method;
          momHandler.queueBind(
              bind.queue,
              bind.exchange,
              bind.nowait,
              bind.routingKey,
              bind.arguments,
              bind.ticket,
              channelNumber);
          if (bind.nowait == false)
            sendMethodToPeer(new AMQP.Queue.BindOk(), channelNumber);
          break;
          
        case AMQP.Queue.Purge.INDEX:
          AMQP.Queue.Purge purge = (AMQP.Queue.Purge) method;
          momHandler.queuePurge(
              purge.queue,
              purge.nowait,
              purge.ticket,
              channelNumber);
          if (purge.nowait == false)
            sendMethodToPeer(new AMQP.Queue.PurgeOk(), channelNumber);
          break;

        default:
          invalidState(method);
          break;
        }
        break;

      /******************************************************
       * Class BASIC
       ******************************************************/
      case AMQP.Basic.INDEX:
        switch (method.getMethodId()) {
        case AMQP.Basic.Publish.INDEX:
          AMQP.Basic.Publish publish = (AMQP.Basic.Publish) method;
          ((PublishRequest) channelContexts.get(new Integer(channelNumber))).setPublish(publish);
          break;

        case AMQP.Basic.Get.INDEX:
          AMQP.Basic.Get get = (AMQP.Basic.Get) method;
          momHandler.basicGet(get.queue, get.noAck, get.ticket, channelNumber);
//          if (getResponse != null) {
//            sendMethodToPeer(new AMQP.Basic.GetOk(
//                getResponse.getEnvelope().getDeliveryTag(),
//                getResponse.getEnvelope()!=null,
//                getResponse.getEnvelope().getExchange(),
//                getResponse.getEnvelope().getRoutingKey(),
//                getResponse.getMessageCount()), channelNumber);
//            sendHeaderToPeer(getResponse.getProps(), getResponse.getBody().length, channelNumber);
//            sendBodyToPeer(getResponse.getBody(), channelNumber);
//          } else {
//            sendMethodToPeer(new AMQP.Basic.GetEmpty(), channelNumber);
//          }
          break;

        case AMQP.Basic.Ack.INDEX:
          AMQP.Basic.Ack ack = (AMQP.Basic.Ack) method;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "ACK = " + ack);
          momHandler.basicAck(ack.deliveryTag, ack.multiple, channelNumber);
          break;

        case AMQP.Basic.Consume.INDEX:
          AMQP.Basic.Consume consume = (AMQP.Basic.Consume) method;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "consume = " + consume);
          AMQP.Basic.ConsumeOk consumerOk = momHandler.basicConsume(
              consume.queue,
              consume.noAck,
              consume.consumerTag,
              consume.noLocal,
              consume.exclusive,
              consume.ticket,
              channelNumber);
          if (consume.nowait == false) {
            sendMethodToPeer(consumerOk, channelNumber);
          }
          break;

        case AMQP.Basic.Cancel.INDEX:
          AMQP.Basic.Cancel cancel = (AMQP.Basic.Cancel) method;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "cancel consumerTag = " + cancel.consumerTag + " nowait = "
              + cancel.nowait);
          momHandler.basicCancel(cancel.consumerTag, channelNumber);
          if (cancel.nowait == false) {
            sendMethodToPeer(new AMQP.Basic.CancelOk(cancel.consumerTag), channelNumber);
          }
          break;

        default:
          invalidState(method);
          break;
        }
        break;

      /******************************************************
       * Class Exchange
       ******************************************************/
      case AMQP.Exchange.INDEX:
        switch (method.getMethodId()) {
        case AMQP.Exchange.Declare.INDEX:
          AMQP.Exchange.Declare declare = (AMQP.Exchange.Declare) method;
          momHandler.exchangeDeclare(
              declare.exchange,
              declare.type,
              declare.passive,
              declare.durable,
              declare.autoDelete,
              declare.arguments,
              declare.ticket,
              channelNumber);
          if (declare.nowait == false) {
            sendMethodToPeer(new AMQP.Exchange.DeclareOk(), channelNumber);
          }
          break;
          
        case AMQP.Exchange.Delete.INDEX:
          AMQP.Exchange.Delete delete = (AMQP.Exchange.Delete) method;
          momHandler.exchangeDelete(
              delete.exchange,
              delete.ifUnused,
              delete.nowait,
              delete.ticket,
              channelNumber);
          if (delete.nowait == false) {
            sendMethodToPeer(new AMQP.Exchange.DeleteOk(), channelNumber);
          }
          break;

        default:
          invalidState(method);
          break;
        }
        break;

      /******************************************************
       * Class Tx
       ******************************************************/
      case AMQP.Tx.INDEX:
        switch (method.getMethodId()) {
        case AMQP.Tx.Select.INDEX:
          AMQP.Tx.Select select = (AMQP.Tx.Select) method;
          //TODO
          sendMethodToPeer(new AMQP.Tx.SelectOk(), channelNumber);
          break;

        case AMQP.Tx.Rollback.INDEX:
          AMQP.Tx.Rollback rollback = (AMQP.Tx.Rollback) method;
          //TODO
          sendMethodToPeer(new AMQP.Tx.RollbackOk(), channelNumber);
          break;

        case AMQP.Tx.Commit.INDEX:
          AMQP.Tx.Commit commit = (AMQP.Tx.Commit) method;
          //TODO
          sendMethodToPeer(new AMQP.Tx.CommitOk(), channelNumber);
          break;

        default:
          invalidState(method);
          break;
        }
        break;

      default:
        invalidState(method);
        break;
      }
    }
  }

  /**
   * Process the content header.
   * 
   * @param frame
   * @param channelNumber
   */
  private void doProcessHeader(Frame frame, int channelNumber) throws IOException {
    MarshallingHeader marshallingHeader = null;
    marshallingHeader = AMQPHelper.readContentHeader(frame);

    PublishRequest publishRequest = (PublishRequest) channelContexts.get(new Integer(channelNumber));
    publishRequest.bodySize = marshallingHeader.getBodySize();
    publishRequest.setHeader(marshallingHeader.getBasicProperties());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "=== Header = " + marshallingHeader.getBasicProperties());
    if (publishRequest.bodySize == 0)
      try {
        momHandler.basicPublish(publishRequest, channelNumber);
        publishRequest.bodyRead = 0;
      } catch (Exception e) {
        //TODO notifier AMQP Client
        e.printStackTrace();
      }
  }

  /**
   * Process the content body.
   * 
   * @param body
   * @param channelNumber
   */
  private void doProcessBody(byte[] body, int channelNumber) {
    if (body != null) {
      PublishRequest publishRequest = (PublishRequest) channelContexts.get(new Integer(channelNumber));
      publishRequest.bodyRead += body.length;
      
      // TODO guestion des fragments (faire des sends pour chaque body)
      publishRequest.body = body;
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "== body = " + new String(body));
      if (publishRequest.bodyRead == publishRequest.bodySize) {
        try {
          momHandler.basicPublish(publishRequest, channelNumber);
          publishRequest.bodyRead = 0;
        } catch (Exception e) {
          //TODO notifier AMQP Client
          e.printStackTrace();
        }
      }
    }
  }

  private void acceptConnection() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPConnectionListener.acceptConnection()");

    sock = serverSocket.accept();

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, " -> accept connection: " + sock.getInetAddress().getHostAddress());
    }

    try {
      sock.setTcpNoDelay(true);
      // Fix bug when the client doesn't
      // use the right protocol (e.g. Telnet)
      // and blocks this listener.
      sock.setSoTimeout(timeout);

      try {
        AMQPHelper.readProtocolHeader(new DataInputStream(sock.getInputStream()));
      } catch (IOException e) {
        // bad header.
        if (logger.isLoggable(BasicLevel.WARN)) {
          logger.log(BasicLevel.WARN, "EXCEPTION :: ", e);
        }
        sendConnectionStartMethod(0);
        sock.close();
        return;
      }
      sendConnectionStartMethod(0);
      
      netServerOut = new NetServerOut(this.getClass().getName());
      netServerOut.start();
      while (true) {
        Frame frame = Frame.readFrom(new DataInputStream(sock.getInputStream()));
        process(frame);
      }

    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "", exc);
      netServerOut.stop();
      sock.close();
      throw exc;
    }
  }

  /**
   * send Connection Start method and update state.
   * 
   * @param channelNumber
   */
  private void sendConnectionStartMethod(int channelNumber) throws IOException {
    AMQP.Connection.Start startMethod = 
      new AMQP.Connection.Start(
          AMQP.PROTOCOL.MAJOR,
          AMQP.PROTOCOL.MINOR,
          momHandler.getMOMProperties(),
          LongStringHelper.asLongString("mechanism"),
          LongStringHelper.asLongString("locales"));
    Frame frame = AMQPHelper.writeMethod(startMethod, channelNumber);  
    classState = startMethod.getClassId();
    methodState = startMethod.getMethodId();
    DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
    Frame.writeTo(frame, dos);
    dos.flush();
  }
  
  public void handleDelivery(int channelNumber, AMQP.Basic.Deliver deliver, BasicProperties header, byte[] body) {
    try {
      sendMethodToPeer(deliver, channelNumber);
      sendHeaderToPeer(header, body.length, channelNumber);
      sendBodyToPeer(body, channelNumber);
    } catch (IOException exc) {
      // TODO Auto-generated catch block
      exc.printStackTrace();
    }
  }

  public void handleBasicReturn(int channelNumber, AMQP.Basic.Return basicReturn, BasicProperties header, byte[] body) {
    try {
      sendMethodToPeer(basicReturn, channelNumber);
      sendHeaderToPeer(header, body.length, channelNumber);
      sendBodyToPeer(body, channelNumber);
    } catch (IOException exc) {
      // TODO Auto-generated catch block
      exc.printStackTrace();
    }
  }


  public void handleGet(int channelNumber, AMQP.Basic.GetOk getOk, BasicProperties header, byte[] body) {
    try {
      if (getOk.deliveryTag == -1) {
        sendMethodToPeer(new AMQP.Basic.GetEmpty(""), channelNumber);
      } else {
        sendMethodToPeer(getOk, channelNumber);
        sendHeaderToPeer(header, body.length, channelNumber);
        sendBodyToPeer(body, channelNumber);
      }
    } catch (IOException exc) {
      // TODO Auto-generated catch block
      exc.printStackTrace();
    }
  }

  protected void shutdown() {
    try {
      if (sock != null) {
        sock.close();
      }
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "", e);
    }
  }

  protected void close() {
    momHandler.close();
  }
  
  final class NetServerOut extends Daemon {

    private DataOutputStream os = null;

    NetServerOut(String name) {
      super(name + ".NetServerOut");
    }

    protected void close() {
      try {
        os.close();
      } catch (IOException exc) {
        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG, exc);
      }
    }

    protected void shutdown() {
    }

    public void run() {
      try {
        try {
          sock.setTcpNoDelay(false);
          os = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
        } catch (IOException exc) {
          logmon.log(BasicLevel.FATAL, getName() + ", cannot start.");
        }

        Frame frame = null;
        while (running) {
          canStop = true;
          try {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", waiting message");
            frame = (Frame) queueOut.get();
            queueOut.pop();
          } catch (InterruptedException exc) {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", interrupted");
            continue;
          }
          canStop = false;
          if (!running)
            break;

          Frame.writeTo(frame, os);
          os.flush();
        }
      } catch (Exception exc) {
        this.logmon.log(BasicLevel.FATAL, this.getName() + ", unrecoverable exception", exc);
      } finally {
        finish();
      }
    }
  }
}
