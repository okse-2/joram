/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2011 ScalAgent Distributed Technologies
 * Copyright (C) 2008 - 2009 CNES
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
package org.ow2.joram.mom.amqp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.AMQPException;
import org.ow2.joram.mom.amqp.exceptions.ChannelErrorException;
import org.ow2.joram.mom.amqp.exceptions.CommandInvalidException;
import org.ow2.joram.mom.amqp.exceptions.ConnectionException;
import org.ow2.joram.mom.amqp.exceptions.FrameErrorException;
import org.ow2.joram.mom.amqp.exceptions.NotImplementedException;
import org.ow2.joram.mom.amqp.exceptions.SyntaxErrorException;
import org.ow2.joram.mom.amqp.exceptions.UnexpectedFrameException;
import org.ow2.joram.mom.amqp.marshalling.AMQP;
import org.ow2.joram.mom.amqp.marshalling.AbstractMarshallingMethod;
import org.ow2.joram.mom.amqp.marshalling.Frame;
import org.ow2.joram.mom.amqp.marshalling.LongStringHelper;
import org.ow2.joram.mom.amqp.marshalling.MarshallingHeader;
import org.ow2.joram.mom.amqp.messages.MessageReceived;
import org.ow2.joram.mom.amqp.structures.Deliver;
import org.ow2.joram.mom.amqp.structures.GetResponse;
import org.ow2.joram.mom.amqp.structures.Returned;

import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.Queue;
import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * Listens to the TCP connections.
 */
public class AMQPConnectionListener extends Daemon {
  
  public static Logger logger = Debug.getLogger(AMQPConnectionListener.class.getName());

  /**
   * The number of bytes that is not used for payload in an AMQP frame: all
   * frames consist of a header (7 octets), a payload of arbitrary size, and a
   * 'frame-end' octet
   */
  public static final int AMQP_FRAME_EXTRA_SIZE = 8;

  /**
   * The implementation version of the broker.
   */
  public static final String JORAM_AMQP_VERSION = MetaData.version;

  /**
   * The message locale that the server supports. The locale defines the
   * language in which the server will send reply texts.
   */
  public static final Locale JORAM_AMQP_LOCALE = Locale.ENGLISH;

  /**
   * The security mechanism that the server supports.
   */
  public static final String JORAM_AMQP_SECURITY = "PLAIN";

  /**
   * Specifies highest channel number that the server permits. Usable channel
   * numbers are in the range 1..channelmax. Zero indicates no specified limit.
   */
  public static int JORAM_AMQP_MAX_CHANNELS = 0;

  /**
   * The largest frame size that the server proposes for the connection,
   * including frame header and endbyte. The client can negotiate a lower value.
   * Zero means that the server does not impose any specific limit but may
   * reject very large frames if it cannot allocate resources for them.
   */
  public static int JORAM_AMQP_MAX_FRAME_SIZE = 0;
  
  /**
   * This table provides a set of peer properties, used for identification,
   * debugging, and general information.
   */
  public static final Map<String, Object> MOM_PROPERTIES;
  
  static {
    MOM_PROPERTIES = new HashMap<String, Object>();
    MOM_PROPERTIES.put("product", LongStringHelper.asLongString("JORAM_AMQP"));
    MOM_PROPERTIES.put("platform", LongStringHelper.asLongString("Java"));
    MOM_PROPERTIES.put("copyright", LongStringHelper.asLongString("ScalAgent"));
    MOM_PROPERTIES.put("version", LongStringHelper.asLongString(JORAM_AMQP_VERSION));
  }

  private static final int NO_CHANNEL = 0;
  
  /** Contains the opened channels. */
  private Map<Integer, PublishRequest> openChannel = new HashMap<Integer, PublishRequest>();
  
  /** The server socket listening to connections from the AMQP peer. */
  private volatile ServerSocket serverSocket;

  /** The socket used to listen. */
  private Socket sock;
  
  /** Heartbeats requested by client and server, in seconds. */
  private int sHeartbeat;
  private int cHeartbeat;

  private Queue queueIn;
  private BlockingQueue queueOut;

  private NetServerOut netServerOut;

  private int maxBodySize = 0;

  private int channelMax = 0;

  volatile boolean closing = false;

  /**
   * Creates a new connection listener.
   * 
   * @param serverSocket
   *          the server socket to listen to
   * @param heartbeat
   *          the socket timeout delay.
   * @throws IOException 
   * @throws Exception 
   */
  public AMQPConnectionListener(ServerSocket serverSocket, int heartbeat) throws IOException {
    super("AMQPConnectionListener");
    this.serverSocket = serverSocket;
    this.sHeartbeat = heartbeat;
    queueIn = new Queue();
    queueOut = new LinkedBlockingDeque();
    
    // create Proxy
    Proxy proxy = new Proxy(queueIn, queueOut);
    proxy.start();
  }

  public void run() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPConnectionListener.run()");

    try {
      acceptConnection();
    } catch (ConnectionException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      connectionException(exc.getCode(), exc.getMessage(), 0, 0);
    } catch (IOException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      connectionException(AMQP.FRAME_ERROR, exc.getMessage(), 0, 0);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION::: AMQPConnectionListener.run()", e);
      connectionException(AMQP.INTERNAL_ERROR, e.getMessage(), 0, 0);
    } finally {
      finish();
    }
  }

  /**
   * Proceed this frame.
   * 
   * @param frame
   * @throws IOException
   */
  private void process(Frame frame) throws IOException, ConnectionException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "proceed frame = " + frame);

    int channelNumber = frame.getChannel();
    if (channelMax != 0 && channelNumber > channelMax) {
      throw new ChannelErrorException("Non permitted channel number: " + channelNumber);
    }
    
    switch (frame.getType()) {
    case AMQP.FRAME_METHOD:
      if (isChannelOpen(channelNumber) && openChannel.get(Integer.valueOf(channelNumber)) != null) {
        throw new UnexpectedFrameException("Method frame was not expected.");
      }
      doProcessMethod(AbstractMarshallingMethod.read(frame.getPayload()), channelNumber);
      break;
    case AMQP.FRAME_HEADER:
      PublishRequest publishRequest = openChannel.get(Integer.valueOf(channelNumber));
      if (publishRequest == null || publishRequest.getHeader() != null) {
        throw new UnexpectedFrameException("Header frame was unexpected.");
      }
      if (channelNumber == NO_CHANNEL) {
        throw new ChannelErrorException("Content header channel can't be zero.");
      }
      doProcessHeader(MarshallingHeader.read(frame.getPayload()), publishRequest, channelNumber);
      break;
    case AMQP.FRAME_BODY:
      publishRequest = openChannel.get(Integer.valueOf(channelNumber));
      if (publishRequest == null || publishRequest.getHeader() == null) {
        throw new UnexpectedFrameException("Body frame was unexpected.");
      }
      doProcessBody(frame.getPayload(), publishRequest, channelNumber);
      break;
    case AMQP.FRAME_HEARTBEAT:
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "client FRAME_HEARTBEAT.");
      }
      if (channelNumber != NO_CHANNEL) {
        throw new CommandInvalidException("Non-zero channel number for heartbeat frame.");
      }
      break;

    default:
      if (logger.isLoggable(BasicLevel.WARN)) {
        logger.log(BasicLevel.WARN, "AMQPConnectionListener.process:: bad type " + frame);
      }
      throw new FrameErrorException("Bad frame type received: " + frame.getType());
    } 
  }

  /**
   * Release channel resources and close it by sending a notification to the
   * client.
   */
  private void connectionException(int errorNumber, String message, int classId, int methodId) {
    AMQP.Connection.Close close = new AMQP.Connection.Close(errorNumber, message, classId, methodId);
    sendMethodToPeer(close, NO_CHANNEL);
  }
  
  private void closeProxy() {
    openChannel.clear();
    sendToProxy(new AMQP.Connection.Close());
  }
  
  private void sendMethodToPeer(AbstractMarshallingMethod method, int channelNumber) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "send method : " + method);
    method.channelNumber = channelNumber;
    queueOut.add(method);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "method sent");
  }
  
  private void doProcessMethod(AbstractMarshallingMethod method, int channelNumber)
      throws ConnectionException {
    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "+ doProcess marshallingMethod = " + method);

      method.channelNumber = channelNumber;
      
      switch (method.getClassId()) {

      /******************************************************
       * Class Connection
       ******************************************************/
      case AMQP.Connection.INDEX:
        if (channelNumber != NO_CHANNEL) {
          throw new CommandInvalidException("Non-zero channel number used for connection class.");
        }

        switch (method.getMethodId()) {
        case AMQP.Connection.StartOk.INDEX:
          AMQP.Connection.StartOk startOk = (AMQP.Connection.StartOk) method;
          if (logger.isLoggable(BasicLevel.INFO)) {
            logger.log(BasicLevel.INFO, "Locale selected: " + startOk.locale);
            logger.log(BasicLevel.INFO, "Security mechanism selected: " + startOk.mechanism);
            logger.log(BasicLevel.INFO, "Client properties: " + startOk.clientProperties);
          }
          String[] localeValues = startOk.locale.split("_");
          Locale selectedLocale;
          if (localeValues.length == 0) {
            throw new SyntaxErrorException("Error parsing locale: " + startOk.locale);
          } else if (localeValues.length == 1) {
            selectedLocale = new Locale(localeValues[0]);
          } else if (localeValues.length == 2) {
            selectedLocale = new Locale(localeValues[0], localeValues[1]);
          } else {
            selectedLocale = new Locale(localeValues[0], localeValues[1], localeValues[2]);
          }
          if (!selectedLocale.getLanguage().equals(JORAM_AMQP_LOCALE.getLanguage())) {
            throw new SyntaxErrorException("Unsupported locale: " + selectedLocale);
          }
          // TODO secure the connection if necessary
          sendMethodToPeer(new AMQP.Connection.Tune(JORAM_AMQP_MAX_CHANNELS, JORAM_AMQP_MAX_FRAME_SIZE,
              sHeartbeat), channelNumber);
          break;

        case AMQP.Connection.SecureOk.INDEX:
          sendMethodToPeer(new AMQP.Connection.Tune(JORAM_AMQP_MAX_CHANNELS, JORAM_AMQP_MAX_FRAME_SIZE,
              sHeartbeat), channelNumber);
          break;

        case AMQP.Connection.TuneOk.INDEX:
          AMQP.Connection.TuneOk tuneOk = (AMQP.Connection.TuneOk) method;
          if (logger.isLoggable(BasicLevel.INFO)) {
            logger.log(BasicLevel.INFO, "Max channel negotiated: " + tuneOk.channelMax);
            logger.log(BasicLevel.INFO, "Max frame size negotiated: " + tuneOk.frameMax);
            logger.log(BasicLevel.INFO, "Heartbeat period desired: " + tuneOk.heartbeat);
          }
          tuneConnectionParameters(tuneOk);
          break;

        case AMQP.Connection.Open.INDEX:
          AMQP.Connection.Open open = (AMQP.Connection.Open) method;
          sendMethodToPeer(new AMQP.Connection.OpenOk(open.virtualHost), channelNumber);
          break;

        default:
          if (method.getMethodId() == AMQP.Connection.Close.INDEX) {
            closeProxy();
          } else if (method.getMethodId()== AMQP.Connection.CloseOk.INDEX) {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "CLOSE_OK");
            // stop the listener 
            stop();
          }
        }
        break;

      /******************************************************
       * Class Channel
       ******************************************************/
      case AMQP.Channel.INDEX:
        if (channelNumber == NO_CHANNEL) {
          throw new CommandInvalidException("No channel defined.");
        }
        if (method.getMethodId() != AMQP.Channel.Open.INDEX
            && method.getMethodId() != AMQP.Channel.CloseOk.INDEX
            && !isChannelOpen(channelNumber)) {
          throw new ChannelErrorException("Channel not opened.");
        }

        switch (method.getMethodId()) {
        case AMQP.Channel.Open.INDEX:
          if (isChannelOpen(channelNumber)) {
            throw new ChannelErrorException("Channel " + channelNumber + " already opened.");
          }
          openChannel(channelNumber);
          sendMethodToPeer(new AMQP.Channel.OpenOk(LongStringHelper.asLongString("" + channelNumber)), channelNumber);
          break;

        case AMQP.Channel.Flow.INDEX:
          // TODO
          throw new NotImplementedException("Flow method currently not implemented.");

        case AMQP.Channel.FlowOk.INDEX:
          // Server doesn't send flow requests for now.
          break;

        case AMQP.Channel.Close.INDEX:
          AMQP.Channel.Close close = (AMQP.Channel.Close) method;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Channel close : replyCode=" + close.replyCode + ", replyText="
              + close.replyText + ", classId=" + close.classId + ", methodId=" + close.methodId);
          sendToProxy(close);
          break;

        case AMQP.Channel.CloseOk.INDEX:
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Channel CLOSE_OK");
          break;

        default:
          break;
        }
        break;

      /******************************************************
       * Class Queue
       ******************************************************/
      case AMQP.Queue.INDEX:
        if (!isChannelOpen(channelNumber)) {
          throw new ChannelErrorException("Channel not opened.");
        }
        sendToProxy(method);
        break;

      /******************************************************
       * Class BASIC
       ******************************************************/
      case AMQP.Basic.INDEX:
        if (!isChannelOpen(channelNumber)) {
          throw new ChannelErrorException("Channel not opened.");
        }

        switch (method.getMethodId()) {
        case AMQP.Basic.Publish.INDEX:
          PublishRequest publishRequest = createPublishRequest(channelNumber);
          publishRequest.setPublish((AMQP.Basic.Publish) method);
          break;

        case AMQP.Basic.Get.INDEX:
        case AMQP.Basic.Ack.INDEX:
        case AMQP.Basic.Consume.INDEX:
        case AMQP.Basic.Cancel.INDEX:
        case AMQP.Basic.RecoverAsync.INDEX:
        case AMQP.Basic.Recover.INDEX:
        case AMQP.Basic.Qos.INDEX:
        case AMQP.Basic.Reject.INDEX:
          sendToProxy(method);
          break;

        default:
          break;
        }
        break;

      /******************************************************
       * Class Exchange
       ******************************************************/
      case AMQP.Exchange.INDEX:
        if (!isChannelOpen(channelNumber)) {
          throw new ChannelErrorException("Channel not opened.");
        }
        sendToProxy(method);
        break;

      /******************************************************
       * Class Tx
       ******************************************************/
      case AMQP.Tx.INDEX:
        if (!isChannelOpen(channelNumber)) {
          throw new ChannelErrorException("Channel not opened.");
        }
        sendToProxy(method);
        break;
        
      default:
        // nothing
        break;
      }
    } catch (ConnectionException exc) {
      connectionException(exc.getCode(), exc.getMessage(), method.getClassId(), method.getMethodId());
    }
  }

  private void tuneConnectionParameters(AMQP.Connection.TuneOk tuneOk) throws SyntaxErrorException {
    if (tuneOk.frameMax < 0) {
      throw new SyntaxErrorException("Negative maximum frame size.");
    }
    if (JORAM_AMQP_MAX_FRAME_SIZE == 0) {
      if (tuneOk.frameMax != 0) {
        maxBodySize = tuneOk.frameMax - AMQP_FRAME_EXTRA_SIZE;
      }
    } else if (tuneOk.frameMax != 0 && tuneOk.frameMax <= JORAM_AMQP_MAX_FRAME_SIZE) {
      maxBodySize = tuneOk.frameMax - AMQP_FRAME_EXTRA_SIZE;
    } else {
      throw new SyntaxErrorException("Error negotiating max frame size.");
    }
    if (maxBodySize != 0 && maxBodySize + AMQP_FRAME_EXTRA_SIZE < AMQP.FRAME_MIN_SIZE) {
      throw new SyntaxErrorException("Requested frame size is too low: " + maxBodySize);
    }

    if (tuneOk.channelMax < 0) {
      throw new SyntaxErrorException("Negative maximum channel number.");
    }
    if (JORAM_AMQP_MAX_CHANNELS == 0) {
      if (tuneOk.channelMax != 0) {
        channelMax = tuneOk.channelMax;
      }
    } else if (tuneOk.channelMax != 0 && tuneOk.channelMax <= JORAM_AMQP_MAX_CHANNELS) {
      channelMax = tuneOk.channelMax;
    } else {
      throw new SyntaxErrorException("Error negotiating max channel number.");
    }

    cHeartbeat = tuneOk.heartbeat;

  }

  /**
   * Process the content header.
   * 
   * @param header
   * @param channelNumber
   */
  private void doProcessHeader(MarshallingHeader header, PublishRequest publishRequest, int channelNumber) {

    publishRequest.setHeader(header.getBasicProperties(), header.getBodySize());
    publishRequest.channel = channelNumber;

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "=== Header = " + header.getBasicProperties());

    if (header.getBodySize() == 0) {
      sendToProxy(publishRequest);
      removePublishRequest(channelNumber);
    }
  }

  /**
   * Process the content body.
   * 
   * @param body
   * @param channelNumber
   */
  private void doProcessBody(byte[] body, PublishRequest publishRequest, int channelNumber)
      throws FrameErrorException {
    if (body != null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "== body = " + new String(body));
      if (maxBodySize != 0 && body.length > maxBodySize) {
        throw new FrameErrorException("Frame is bigger than maximum negociated size: " + body.length);
      }
      boolean finished = publishRequest.appendBody(body);
      if (finished) {
        MessageReceived messageReceived = new MessageReceived(
                publishRequest.getBody(), publishRequest.getPublish().routingKey,
                sock.getInetAddress().getHostAddress(), sock.getPort()
        );
        AMQPService.notifyMessageReceived(messageReceived);
        sendToProxy(publishRequest);
        removePublishRequest(channelNumber);
      }
    }
  }

  private void sendToProxy(PublishRequest publishRequest) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPConnectionListener.sendToProxy(" + publishRequest + ')');
    if (!closing) {
      queueIn.push(publishRequest);
    }
  }

  private void sendToProxy(AbstractMarshallingMethod method) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPConnectionListener.sendToProxy(" + method + ')');
    if (closing && !(method instanceof AMQP.Connection.Close)) {
      return;
    }
    queueIn.push(method);
  }

  private void acceptConnection() throws AMQPException, IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPConnectionListener.acceptConnection()");

    sock = serverSocket.accept();

    // create new connection listener for parallelism 
    AMQPService.createConnectionListener();

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, " -> accept connection: " + sock.getInetAddress().getHostAddress());
    }

    sock.setTcpNoDelay(true);
    //If a peer detects no incoming traffic (i.e. received octets) for two heartbeat
    // intervals or longer, it should close the connection
    sock.setSoTimeout(sHeartbeat * 2000);

    try {
      readProtocolHeader(sock.getInputStream());
    } catch (FrameErrorException e) {
      if (logger.isLoggable(BasicLevel.WARN)) {
        logger.log(BasicLevel.WARN, "EXCEPTION :: ", e);
      }
      // If the server cannot support the protocol specified in the
      // protocol header, it MUST respond with a valid protocol header and
      // then close the socket connection.
      OutputStream bos = new BufferedOutputStream(sock.getOutputStream());
      bos.write('A');
      bos.write('M');
      bos.write('Q');
      bos.write('P');
      bos.write(0);
      bos.write(AMQP.PROTOCOL.MAJOR);
      bos.write(AMQP.PROTOCOL.MINOR);
      bos.write(AMQP.PROTOCOL.REVISION);
      bos.flush();
      closeSocket();
      return;
    }

    //queueOut.removeAllElements();
    netServerOut = new NetServerOut(this.getClass().getName());
    netServerOut.start();

    AMQP.Connection.Start startMethod = getConnectionStartMethod();
    queueOut.add(startMethod);

    InputStream cnxInputStream = sock.getInputStream();
    while (true) {
      process(Frame.readFrom(cnxInputStream));
    }

  }

  private static void readProtocolHeader(InputStream in) throws IOException, FrameErrorException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPConnectionListener.readProtocolHeader(" + in + ')');

    StringBuffer buff = new StringBuffer();
    char c = (char) StreamUtil.readUnsignedByteFrom(in);
    buff.append(c);
    if (c != 'A') {
      throw new FrameErrorException("Invalid header: " + buff);
    }

    c = (char) StreamUtil.readUnsignedByteFrom(in);
    buff.append(c);
    if (c != 'M') {
      throw new FrameErrorException("Invalid header: " + buff);
    }

    c = (char) StreamUtil.readUnsignedByteFrom(in);
    buff.append(c);
    if (c != 'Q') {
      throw new FrameErrorException("Invalid header: " + buff);
    }

    c = (char) StreamUtil.readUnsignedByteFrom(in);
    buff.append(c);
    if (c != 'P') {
      throw new FrameErrorException("Invalid header: " + buff);
    }

    // Header style has changed between 0.9 and 0.9.1: try to accept 0.9 style
    boolean style09 = false;
    int i = StreamUtil.readUnsignedByteFrom(in);
    buff.append(i);
    if (i != 0) {
      if (i == 1) {
        style09 = true;
      } else {
        throw new FrameErrorException("Invalid header: " + buff);
      }
    }

    i = StreamUtil.readUnsignedByteFrom(in);
    buff.append(i);
    if (i != AMQP.PROTOCOL.MAJOR) {
      if (!style09 || i != 1) {
        throw new FrameErrorException("Incorrect major version: " + i);
      }
    }

    i = StreamUtil.readUnsignedByteFrom(in);
    buff.append(i);
    if (i != AMQP.PROTOCOL.MINOR) {
      if (!style09 || i != AMQP.PROTOCOL.MAJOR) {
        throw new FrameErrorException("Incorrect minor version: " + i);
      }
    }

    i = StreamUtil.readUnsignedByteFrom(in);
    buff.append(i);
    if (i != AMQP.PROTOCOL.REVISION) {
      if (!style09 || i != AMQP.PROTOCOL.MINOR) {
        throw new FrameErrorException("Incorrect revision version: " + i);
      }
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPConnectionListener.readProtocolHeader: client protocol = "
          + buff.toString());
  }
  
  /**
   * Creates a {@link AMQP.Connection.Start} method object.
   */
  private static AMQP.Connection.Start getConnectionStartMethod() throws IOException {
    AMQP.Connection.Start startMethod = 
      new AMQP.Connection.Start(
          AMQP.PROTOCOL.MAJOR,
          AMQP.PROTOCOL.MINOR,
          MOM_PROPERTIES,
          LongStringHelper.asLongString(JORAM_AMQP_SECURITY),
          LongStringHelper.asLongString(JORAM_AMQP_LOCALE.toString()));
    return startMethod;
  }

  private boolean isChannelOpen(int channel) {
    if (channel == 0) {
      return true;
    }
    return openChannel.containsKey(Integer.valueOf(channel));
  }
  
  private void openChannel(int channel) {
    openChannel.put(Integer.valueOf(channel), null);
  }
  
  private void closeChannel(int channel) {
    openChannel.remove(Integer.valueOf(channel));
  }
  
  private PublishRequest createPublishRequest(int channel) {
    PublishRequest request = new PublishRequest();
    request.channel = channel;
    openChannel.put(Integer.valueOf(channel), request);
    return request;
  }
  
  private void removePublishRequest(int channel) {
    openChannel.put(Integer.valueOf(channel), null);
  }
  
  private void closeSocket() {
    try {
      if (sock != null) {
        sock.close();
      }
    } catch (IOException e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "", e);
    }
  }

  protected void shutdown() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPConnectionListener.shutdown()");
    closeSocket();
  }

  protected void close() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPConnectionListener.close()");
    closeProxy();
    AMQPService.removeConnectionListener(this);
  }
  
  final class NetServerOut extends Daemon {

    private OutputStream os = null;

    NetServerOut(String name) {
      super(name + ".NetServerOut", logger);
    }

    protected void close() {
      try {
        os.close();
        sock.close();
      } catch (IOException exc) {
        if (this.logmon.isLoggable(BasicLevel.DEBUG))
          this.logmon.log(BasicLevel.DEBUG, exc);
      }
    }

    protected void shutdown() {
//      queueOut.close();
    }

    private void writeToPeer(Frame frame) throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "writeToPeer frame : " + frame);
      Frame.writeTo(frame, os, maxBodySize);
      os.flush();
    }
    
    public void run() {
      try {
        try {
          sock.setTcpNoDelay(false);
          os = new BufferedOutputStream(sock.getOutputStream());
        } catch (IOException exc) {
          logmon.log(BasicLevel.FATAL, "cannot start.");
        }

        while (running) {
          Object obj = null;

          canStop = true;
          try {
            if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
              this.logmon.log(BasicLevel.DEBUG, "waiting message");
            }
            if (cHeartbeat <= 0) {
              obj = queueOut.take();
            } else {
              obj = queueOut.poll(cHeartbeat, TimeUnit.SECONDS);
            }
          } catch (InterruptedException exc) {
            if (this.logmon.isLoggable(BasicLevel.DEBUG))
              this.logmon.log(BasicLevel.DEBUG, this.getName() + ", interrupted");
          }
          canStop = false;
          if (!running)
            break;
            
          if (this.logmon.isLoggable(BasicLevel.DEBUG))
            this.logmon.log(BasicLevel.DEBUG, "getAndPop = " + obj + " closing=" + closing);

          if (closing) {
            if (!(obj instanceof AMQP.Connection.Close) && !(obj instanceof AMQP.Connection.CloseOk)) {
              if (this.logmon.isLoggable(BasicLevel.DEBUG))
                this.logmon.log(BasicLevel.DEBUG, "Method not sent: closing.");
              continue;
            }
          }

          if (obj == null) {
            if (this.logmon.isLoggable(BasicLevel.DEBUG)) {
              this.logmon.log(BasicLevel.DEBUG, "Send heartbeat to client.");
            }
            writeToPeer(new Frame(AMQP.FRAME_HEARTBEAT, NO_CHANNEL));
            continue;
          }

          if (obj instanceof AbstractMarshallingMethod) {
            AbstractMarshallingMethod method = (AbstractMarshallingMethod) obj;
            if (!isChannelOpen(method.channelNumber))
              continue;
            // is a Channel.Close method ?
            if (method instanceof AMQP.Channel.Close || method instanceof AMQP.Channel.CloseOk) {
              closeChannel(method.channelNumber);
            }
            writeToPeer(method.toFrame());
            if (method instanceof AMQP.Connection.CloseOk) {
              stop();
            }
            if (method instanceof AMQP.Connection.Close) {
              stop();
              closing = true;
            }
          } else if (obj instanceof Deliver) {
            Deliver deliver = (Deliver) obj;
            writeToPeer(deliver.deliver.toFrame());
            int channelNumber = deliver.deliver.channelNumber;
            if (deliver.body == null) {
              writeToPeer(MarshallingHeader.toFrame(0, deliver.properties, channelNumber));
            } else {
              writeToPeer(MarshallingHeader.toFrame(deliver.body.length, deliver.properties, channelNumber));
              writeToPeer(new Frame(AMQP.FRAME_BODY, channelNumber, deliver.body));
            }

          } else if (obj instanceof GetResponse) {
            GetResponse resp = (GetResponse) obj;
            writeToPeer(resp.getOk.toFrame());
            int channelNumber = resp.getOk.channelNumber;
            if (resp.body == null) {
              writeToPeer(MarshallingHeader.toFrame(0, resp.properties, channelNumber));
            } else {
              writeToPeer(MarshallingHeader.toFrame(resp.body.length, resp.properties, channelNumber));
              writeToPeer(new Frame(AMQP.FRAME_BODY, channelNumber, resp.body));
            }
          } else if (obj instanceof Returned) {
            Returned returned = (Returned) obj;
            writeToPeer(returned.returned.toFrame());
            int channelNumber = returned.returned.channelNumber;
            if (returned.body == null) {
              writeToPeer(MarshallingHeader.toFrame(0, returned.properties, channelNumber));
            } else {
              writeToPeer(MarshallingHeader.toFrame(returned.body.length, returned.properties, channelNumber));
              writeToPeer(new Frame(AMQP.FRAME_BODY, channelNumber, returned.body));
            }

          } else if (obj instanceof Frame) {
            writeToPeer((Frame) obj);
          } else {
            if (logger.isLoggable(BasicLevel.ERROR))
              logger.log(BasicLevel.ERROR, this.getName() + ": UNEXPECTED OBJECT CLASS: "
                  + obj.getClass().getName());
          }
        }
      } catch (SocketException exc) {
        this.logmon.log(BasicLevel.DEBUG, this.getName() + ", socket error", exc);
      } catch (Exception exc) {
        this.logmon.log(BasicLevel.FATAL, this.getName() + ", unrecoverable exception", exc);
      } finally {
        finish();
      }
    }
  }
}
