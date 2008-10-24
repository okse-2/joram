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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Basic.Deliver;
import com.rabbitmq.client.AMQP.Basic.GetOk;
import com.rabbitmq.client.AMQP.Basic.Return;
import com.rabbitmq.client.impl.AMQContentHeader;
import com.rabbitmq.client.impl.AMQImpl;
import com.rabbitmq.client.impl.Frame;
import com.rabbitmq.client.impl.LongStringHelper;

import fr.dyade.aaa.util.Daemon;
import fr.dyade.aaa.util.Debug;
import fr.dyade.aaa.util.Queue;

/**
 * Listens to the TCP connections.
 */
public class AMQPConnectionListener extends Daemon implements Consumer {

  public static Logger logger = Debug.getLogger(AMQPConnectionListener.class.getName());

  /** The server socket listening to connections from the AMQP peer. */
  private ServerSocket serverSocket;

  /** Used to interact with the MOM. */
  private MOMHandler momHandler;

  /** The socket used to listen. */
  private Socket sock;
  
  /** Timeout when listening on the socket. */
  private int timeout;

  /** The channel contexts. */
  private Map channelContexts;
  
  /** The heartbeat period in seconds. */
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
  
  private void sendMethodToPeer(Method method, int channelNumber) throws IOException {
    System.out.println("send method : " + method);
    queueOut.push(((com.rabbitmq.client.impl.Method) method).toFrame(channelNumber));
    classState = method.protocolClassId();
    methodState = method.protocolMethodId();
    System.out.println("method sent");//NTA tmp
  }

  /**
   * send content header to peer.
   * 
   * @param message
   * @param channelNumber
   * @throws IOException
   */
  private void sendHeaderToPeer(AMQP.BasicProperties header, long bodySize, int channelNumber)
      throws IOException {
    System.out.println("sendHeaderToPeer = " + header);
    queueOut.push(header.toFrame(channelNumber, bodySize));
    System.out.println("sendHeaderToPeer done.  header = " + header);
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
      System.out.println("sendBodyToPeer = " + new String(body));
      queueOut.push(new Frame(AMQP.FRAME_BODY, channelNumber, body));
    }
    System.out.println("sendBodyToPeer done.");
  }

  /**
   * Proceed this frame, switch by type.
   * 
   * @param frame
   * @throws IOException
   */
  private void process(Frame frame) throws Exception {
    System.out.println("\nproceed frame = " + frame);//NTA tmp
    int channelNumber = frame.channel;
    switch (frame.type) {
    case AMQP.FRAME_METHOD:
      doProcessMethod(AMQImpl.readMethodFrom(frame.getInputStream()), channelNumber);
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
      System.out.println("client FRAME_HEARTBEAT.");//NTA tmp
      break;

    default:
      System.out.println("+++++ process frame type nok : " + frame.type);//NTA tmp
      if (logger.isLoggable(BasicLevel.WARN)) {
        logger.log(BasicLevel.WARN, "AMQPConnectionListener.process:: bad type " + frame);
      }
      break;
    }
  }

  private void invalidState(Method marshallingMethod) throws IOException {
    System.out.println("BAD STATE (current classState = " + classState + ", methodState = " + methodState
        + ") cState = " + marshallingMethod.protocolMethodId() + ", mState = "
        + marshallingMethod.protocolMethodId() + " (" + marshallingMethod.protocolMethodName() + ')');//NTA tmp

    throw new IOException("BAD STATE (current classState = " + classState + ", methodState = " + methodState
        + ") cState = " + marshallingMethod.protocolMethodId() + ", mState = "
        + marshallingMethod.protocolMethodId() + " (" + marshallingMethod.protocolMethodName() + ')');
  }

  private void doProcessMethod(Method method, int channelNumber) throws Exception {
    if (method != null) {
      //System.out.println("+ doProcess marshallingMethod = " + marshallingMethod);
      switch (method.protocolClassId()) {
      /******************************************************
       * Class Connection
       ******************************************************/
      case AMQImpl.Connection.INDEX:
        switch (method.protocolMethodId()) {
        case AMQImpl.Connection.StartOk.INDEX:
          if (classState == AMQImpl.Connection.INDEX && methodState == AMQImpl.Connection.Start.INDEX) {
            // sendConnectionSecureMethod(channelNumber); //TODO
            // remove when sendSecureMethod implemented.
            sendMethodToPeer(new AMQImpl.Connection.Tune(channelNumber, 10 * 1024, heartBeat), channelNumber);
          } else
            invalidState(method);
          break;

        case AMQImpl.Connection.SecureOk.INDEX:
          if (classState == AMQImpl.Connection.INDEX && methodState == AMQImpl.Connection.Secure.INDEX) {
            //TODO NTA : heartbeat, frameMax
            sendMethodToPeer(new AMQImpl.Connection.Tune(channelNumber, 10 * 1024, heartBeat), channelNumber);
          } else
            invalidState(method);
          break;

        case AMQImpl.Connection.TuneOk.INDEX:
          if (classState == AMQImpl.Connection.INDEX && methodState == AMQImpl.Connection.Tune.INDEX) {
            methodState = AMQImpl.Connection.TuneOk.INDEX;
          } else
            invalidState(method);
          break;

        case AMQImpl.Connection.Open.INDEX:
          if (classState == AMQImpl.Connection.INDEX && methodState == AMQImpl.Connection.TuneOk.INDEX) {
            sendMethodToPeer(new AMQImpl.Connection.OpenOk(sock.getLocalAddress().getHostAddress()),
                channelNumber);
          } else
            invalidState(method);
          break;

        case AMQImpl.Connection.Close.INDEX:
          sendMethodToPeer(new AMQImpl.Connection.CloseOk(), channelNumber);
          break;

        case AMQImpl.Connection.CloseOk.INDEX:
          System.out.println("CLOSE_OK");//NTA tmp
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
      case AMQImpl.Channel.INDEX:
        switch (method.protocolMethodId()) {
        case AMQImpl.Channel.Open.INDEX:
          channelContexts.put(new Integer(channelNumber), new PublishRequest());
          sendMethodToPeer(new AMQImpl.Channel.OpenOk(), channelNumber);
          break;

        case AMQImpl.Channel.Flow.INDEX:
          if (classState == AMQImpl.Channel.INDEX && methodState == AMQImpl.Channel.OpenOk.INDEX) {
            // TODO change flow state active/inactive
            sendMethodToPeer(new AMQImpl.Channel.FlowOk(((AMQImpl.Channel.Flow) method).active),
                channelNumber);
          } else
            invalidState(method);
          break;

        case AMQImpl.Channel.FlowOk.INDEX:
          if (classState == AMQImpl.Channel.INDEX && methodState == AMQImpl.Channel.Flow.INDEX) {
            //TODO change flow state active/inactive
          } else
            invalidState(method);
          break;

        case AMQImpl.Channel.Close.INDEX:
          AMQImpl.Channel.Close close = (AMQImpl.Channel.Close) method;
          System.out.println("Channel close : replyCode=" + close.replyCode + ", replyText="
              + close.replyText + ", classId=" + close.classId + ", methodId=" + close.methodId);
          channelContexts.remove(new Integer(channelNumber));
          sendMethodToPeer(new AMQImpl.Channel.CloseOk(), channelNumber);
          break;

        case AMQImpl.Channel.CloseOk.INDEX:
          System.out.println("Channel CLOSE_OK");//NTA tmp
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
      case AMQImpl.Access.INDEX:
        switch (method.protocolMethodId()) {
        case AMQImpl.Access.Request.INDEX:
          if (classState == AMQImpl.Channel.INDEX && methodState == AMQImpl.Channel.OpenOk.INDEX) {
            AMQP.Access.Request request = (AMQP.Access.Request) method;
            AMQP.Access.RequestOk requestOk = momHandler.accessRequest(
                request.getRealm(),
                request.getExclusive(),
                request.getPassive(),
                request.getActive(),
                request.getWrite(),
                request.getRead(),
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
      case AMQImpl.Queue.INDEX:
        switch (method.protocolMethodId()) {
        case AMQImpl.Queue.Declare.INDEX:
          AMQP.Queue.Declare declare = (AMQP.Queue.Declare) method;
          AMQP.Queue.DeclareOk declareOk = momHandler.queueDeclare(
              declare.getQueue(),
              declare.getPassive(),
              declare.getDurable(),
              declare.getExclusive(),
              declare.getAutoDelete(),
              declare.getArguments(),
              declare.getTicket(),
              channelNumber);
          sendMethodToPeer(declareOk, channelNumber);
          break;

        case AMQImpl.Queue.Delete.INDEX:
          AMQP.Queue.Delete delete = (AMQP.Queue.Delete) method;
          AMQP.Queue.DeleteOk deleteOk = momHandler.queueDelete(
              delete.getQueue(),
              delete.getIfUnused(),
              delete.getIfEmpty(),
              delete.getNowait(),
              delete.getTicket(),
              channelNumber);
          sendMethodToPeer(deleteOk, channelNumber);
          break;

        case AMQImpl.Queue.Bind.INDEX:
          AMQP.Queue.Bind bind = (AMQP.Queue.Bind) method;
          momHandler.queueBind(
              bind.getQueue(),
              bind.getExchange(),
              bind.getNowait(),
              bind.getRoutingKey(),
              bind.getArguments(),
              bind.getTicket(),
              channelNumber);
          if (bind.getNowait() == false)
            sendMethodToPeer(new AMQImpl.Queue.BindOk(), channelNumber);
          break;
          
        case AMQImpl.Queue.Purge.INDEX:
          AMQP.Queue.Purge purge = (AMQP.Queue.Purge) method;
          momHandler.queuePurge(
              purge.getQueue(),
              purge.getNowait(),
              purge.getTicket(),
              channelNumber);
          if (purge.getNowait() == false)
            sendMethodToPeer(new AMQImpl.Queue.PurgeOk(), channelNumber);
          break;

        default:
          invalidState(method);
          break;
        }
        break;

      /******************************************************
       * Class BASIC
       ******************************************************/
      case AMQImpl.Basic.INDEX:
        switch (method.protocolMethodId()) {
        case AMQImpl.Basic.Publish.INDEX:
          AMQP.Basic.Publish publish = (AMQP.Basic.Publish) method;
          ((PublishRequest) channelContexts.get(new Integer(channelNumber))).setPublish(publish);
          break;

        case AMQImpl.Basic.Get.INDEX:
          AMQP.Basic.Get get = (AMQP.Basic.Get) method;
          GetResponse getResponse = momHandler.basicGet(get.getQueue(), get.getNoAck(), get.getTicket(),
              channelNumber);
          if (getResponse != null) {
            sendMethodToPeer(new AMQImpl.Basic.GetOk(
                getResponse.getEnvelope().getDeliveryTag(),
                getResponse.getEnvelope()!=null,
                getResponse.getEnvelope().getExchange(),
                getResponse.getEnvelope().getRoutingKey(),
                getResponse.getMessageCount()), channelNumber);
            sendHeaderToPeer(getResponse.getProps(), getResponse.getBody().length, channelNumber);
            sendBodyToPeer(getResponse.getBody(), channelNumber);
          } else {
            //            sendMethodToPeer(new AMQImpl.Basic.GetEmpty(), channelNumber);
          }
          break;

        case AMQImpl.Basic.Ack.INDEX:
          AMQP.Basic.Ack ack = (AMQP.Basic.Ack) method;
          System.out.println("ACK = " + ack);
          momHandler.basicAck(ack.getDeliveryTag(), ack.getMultiple(), channelNumber);
          break;

        case AMQImpl.Basic.Consume.INDEX:
          AMQP.Basic.Consume consume = (AMQP.Basic.Consume) method;
          System.out.println("consume = " + consume);
          AMQP.Basic.ConsumeOk consumerOk = momHandler.basicConsume(
              consume.getQueue(),
              consume.getNoAck(),
              consume.getConsumerTag(),
              consume.getNoLocal(),
              consume.getExclusive(),
              consume.getTicket(),
              channelNumber);
          sendMethodToPeer(consumerOk, channelNumber);
          break;

        case AMQImpl.Basic.Cancel.INDEX:
          AMQP.Basic.Cancel cancel = (AMQP.Basic.Cancel) method;
          System.out.println("cancel consumerTag = " + cancel.getConsumerTag() + " nowait = "
              + cancel.getNowait());
          momHandler.basicCancel(cancel.getConsumerTag(), channelNumber);
          sendMethodToPeer(new AMQImpl.Basic.CancelOk(cancel.getConsumerTag()), channelNumber);
          break;

        default:
          invalidState(method);
          break;
        }
        break;

      /******************************************************
       * Class Exchange
       ******************************************************/
      case AMQImpl.Exchange.INDEX:
        switch (method.protocolMethodId()) {
        case AMQImpl.Exchange.Declare.INDEX:
          AMQP.Exchange.Declare declare = (AMQP.Exchange.Declare) method;
          momHandler.exchangeDeclare(
              declare.getExchange(),
              declare.getType(),
              declare.getPassive(),
              declare.getDurable(),
              declare.getAutoDelete(),
              declare.getArguments(),
              declare.getTicket(),
              channelNumber);
          sendMethodToPeer(new AMQImpl.Exchange.DeclareOk(), channelNumber);
          break;
          
        case AMQImpl.Exchange.Delete.INDEX:
          AMQP.Exchange.Delete delete = (AMQP.Exchange.Delete) method;
          momHandler.exchangeDelete(
              delete.getExchange(),
              delete.getIfUnused(),
              delete.getNowait(),
              delete.getTicket(),
              channelNumber);
          sendMethodToPeer(new AMQImpl.Exchange.DeleteOk(), channelNumber);
          break;

        default:
          invalidState(method);
          break;
        }
        break;

      /******************************************************
       * Class Tx
       ******************************************************/
      case AMQImpl.Tx.INDEX:
        switch (method.protocolMethodId()) {
        case AMQImpl.Tx.Select.INDEX:
          AMQP.Tx.Select select = (AMQP.Tx.Select) method;
          //TODO
          sendMethodToPeer(new AMQImpl.Tx.SelectOk(), channelNumber);
          break;

        case AMQImpl.Tx.Rollback.INDEX:
          AMQP.Tx.Rollback rollback = (AMQP.Tx.Rollback) method;
          //TODO
          sendMethodToPeer(new AMQImpl.Tx.RollbackOk(), channelNumber);
          break;

        case AMQImpl.Tx.Commit.INDEX:
          AMQP.Tx.Commit commit = (AMQP.Tx.Commit) method;
          //TODO
          sendMethodToPeer(new AMQImpl.Tx.CommitOk(), channelNumber);
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
    AMQContentHeader header = null;
    DataInputStream in = frame.getInputStream();
    header = AMQImpl.readContentHeaderFrom(in);

    PublishRequest publishRequest = (PublishRequest) channelContexts.get(new Integer(channelNumber));
    publishRequest.bodySize = header.readFrom(in);
    publishRequest.setHeader((BasicProperties) header);

    System.out.println("=== MarshallingHeader = " + header);
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
      
      // TODO gestion des fragments (faire des sends pour chaque body)
      publishRequest.body = body;
      System.out.println("== body = " + new String(body));
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
        readProtocolHeader(new DataInputStream(sock.getInputStream()));
      } catch (IOException e) {
        // bad header.
        if (logger.isLoggable(BasicLevel.WARN)) {
          logger.log(BasicLevel.WARN, "EXCEPTION :: ", e);
        }
        sendConnectionStartMethod(0);
        sock.close();
        return;
      }

      System.out.println("channelNumber = " + 0);
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

  private static void readProtocolHeader(DataInputStream in) throws IOException {
    StringBuffer buff = new StringBuffer();
    char c = (char) in.readByte();
    buff.append(c);
    if (c != 'A')
      throw new IOException("bad header : " + buff);
    c = (char) in.readByte();
    buff.append(c);
    if (c != 'M')
      throw new IOException("bad header : " + buff);
    c = (char) in.readByte();
    buff.append(c);
    if (c != 'Q')
      throw new IOException("bad header : " + buff);
    c = (char) in.readByte();
    buff.append(c);
    if (c != 'P')
      throw new IOException("bad header : " + buff);
    int i = in.readByte();
    buff.append(i);
    //    if (i != 1)
    //      badProtocolHeader(buff.toString());
    i = in.readByte();
    buff.append(i);
    //    if (i != 1)
    //      badProtocolHeader(buff.toString());
    i = in.readByte();
    buff.append(i);
    //    if (i != AMQP.PROTOCOL.MAJOR)
    //      badProtocolHeader(buff.toString());
    i = in.readByte();
    buff.append(i);
    //    if (i != AMQP.PROTOCOL.MINOR)
    //      badProtocolHeader(buff.toString());
    System.out.println("client protocol = " + buff.toString());
  }

  /**
   * send Connection Start method and update state.
   * 
   * @param channelNumber
   */
  private void sendConnectionStartMethod(int channelNumber) throws IOException {
    AMQImpl.Connection.Start startMethod = new AMQImpl.Connection.Start(
        AMQP.PROTOCOL.MAJOR,
        AMQP.PROTOCOL.MINOR,
        momHandler.getMOMProperties(),
        LongStringHelper.asLongString("mechanism"),
        LongStringHelper.asLongString("locales"));
    classState = startMethod.protocolClassId();
    methodState = startMethod.protocolMethodId();
    Frame frame = ((com.rabbitmq.client.impl.Method) startMethod).toFrame(channelNumber);
    frame.writeTo(new DataOutputStream(sock.getOutputStream()));
    sock.getOutputStream().flush();
  }
  
  public void handleDelivery(int channelNumber, Deliver deliver, BasicProperties header, byte[] body) {
    try {
      sendMethodToPeer(deliver, channelNumber);
      sendHeaderToPeer(header, body.length, channelNumber);
      sendBodyToPeer(body, channelNumber);
    } catch (IOException exc) {
      // TODO Auto-generated catch block
      exc.printStackTrace();
    }
  }

  public void handleBasicReturn(int channelNumber, Return basicReturn, BasicProperties header, byte[] body) {
    try {
      sendMethodToPeer(basicReturn, channelNumber);
      sendHeaderToPeer(header, body.length, channelNumber);
      sendBodyToPeer(body, channelNumber);
    } catch (IOException exc) {
      // TODO Auto-generated catch block
      exc.printStackTrace();
    }
  }


  public void handleGet(int channelNumber, GetOk getOk, BasicProperties header, byte[] body) {
    try {
      if (getOk.getDeliveryTag() == -1) {
        sendMethodToPeer(new AMQImpl.Basic.GetEmpty(""), channelNumber);
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
          os = new DataOutputStream(sock.getOutputStream());
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

          frame.writeTo(os);
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
