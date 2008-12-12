/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2008 CNES
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
package org.objectweb.joram.mom.amqp.marshalling;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class AMQP {

  public static class PROTOCOL {
    public static final int MAJOR = 0;
    public static final int MINOR = 9;
    public static final int PORT = 5672;
  }

    

  /**
   * The connection class provides methods for a client to establish a network connection to a server, and for both peers to operate the connection thereafter.
   */
  public static final int CLASS_CONNECTION = 10;

  /**
   * The channel class provides methods for a client to establish a channel to a server and for both peers to operate the channel thereafter.
   */
  public static final int CLASS_CHANNEL = 20;

  /**
   * The protocol control access to server resources using access tickets. A client must explicitly request access tickets before doing work. An access ticket grants a client the right to use a specific set of resources - called a "realm" - in specific ways.
   */
  public static final int CLASS_ACCESS = 30;

  /**
   * Exchanges match and distribute messages across queues. Exchanges can be configured in the server or created at runtime.
   */
  public static final int CLASS_EXCHANGE = 40;

  /**
   * Queues store and forward messages. Queues can be configured in the server or created at runtime. Queues must be attached to at least one exchange in order to receive messages from publishers.
   */
  public static final int CLASS_QUEUE = 50;

  /**
   * The Basic class provides methods that support an industry-standard messaging model.
   */
  public static final int CLASS_BASIC = 60;

  /**
   * The Tx class allows publish and ack operations to be batched into atomic
   * units of work. The intention is that all publish and ack requests issued
   * within a transaction will complete successfully or none of them will.
   * Servers SHOULD implement atomic transactions at least where all publish or
   * ack requests affect a single queue. Transactions that cover multiple queues
   * may be non-atomic, given that queues can be created and destroyed
   * asynchronously, and such events do not form part of any transaction.
   * Further, the behaviour of transactions with respect to the immediate and
   * mandatory flags on Basic.Publish methods is not defined.
   */
  public static final int CLASS_TX = 90;


  public static final int[] ids = { 10, 20, 30, 40, 50, 60, 90 };
  
  public static final java.lang.String[] classnames = {
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Access",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx"
  };
  
    public static final int FRAME_METHOD = 1;
  public static final int FRAME_HEADER = 2;
  public static final int FRAME_BODY = 3;
  public static final int FRAME_HEARTBEAT = 8;
  public static final int FRAME_MIN_SIZE = 4096;
  public static final int FRAME_END = 206;
  public static final int REPLY_SUCCESS = 200;
  public static final int CONTENT_TOO_LARGE = 311;
  public static final int NO_CONSUMERS = 313;
  public static final int CONNECTION_FORCED = 320;
  public static final int INVALID_PATH = 402;
  public static final int ACCESS_REFUSED = 403;
  public static final int NOT_FOUND = 404;
  public static final int RESOURCE_LOCKED = 405;
  public static final int PRECONDITION_FAILED = 406;
  public static final int FRAME_ERROR = 501;
  public static final int SYNTAX_ERROR = 502;
  public static final int COMMAND_INVALID = 503;
  public static final int CHANNEL_ERROR = 504;
  public static final int UNEXPECTED_FRAME = 505;
  public static final int RESOURCE_ERROR = 506;
  public static final int NOT_ALLOWED = 530;
  public static final int NOT_IMPLEMENTED = 540;
  public static final int INTERNAL_ERROR = 541;

  public static class Connection extends AbstractMarshallingClass {
  
    private static final long serialVersionUID = 1L;

  
      
    public final static int INDEX = 10;
      
    public int getClassId() { 
      return INDEX;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }
    
  
    public static final int METHOD_CONNECTION_START = 10;
    public static final int METHOD_CONNECTION_START_OK = 11;
    public static final int METHOD_CONNECTION_SECURE = 20;
    public static final int METHOD_CONNECTION_SECURE_OK = 21;
    public static final int METHOD_CONNECTION_TUNE = 30;
    public static final int METHOD_CONNECTION_TUNE_OK = 31;
    public static final int METHOD_CONNECTION_OPEN = 40;
    public static final int METHOD_CONNECTION_OPEN_OK = 41;
    public static final int METHOD_CONNECTION_CLOSE = 50;
    public static final int METHOD_CONNECTION_CLOSE_OK = 51;
  

    public static final int[] mids = { 10, 11, 20, 21, 30, 31, 40, 41, 50, 51 };
    
    public static final java.lang.String[] methodnames = {
      "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Start",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$StartOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Secure",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$SecureOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Tune",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$TuneOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Open",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$OpenOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Close",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$CloseOk"
    };
    
    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.Connection.mids.length; i++) {
        if (AMQP.Connection.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.Connection.methodnames[pos];  
      return "";
    }
  


  /**
   * This method starts the connection negotiation process by telling the client the protocol version that the server proposes, along with a list of security mechanisms which the client can use for authentication.
   */
  public static class Start extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
       * The major version number can take any value from 0 to 99 as defined in
       * the AMQP specification.
       */
    public int versionMajor;
      /**
       * The minor version number can take any value from 0 to 99 as defined in
       * the AMQP specification.
       */
    public int versionMinor;
  /**
   * 
   */
    public Map serverProperties;
  /**
   * A list of the security mechanisms that the server supports, delimited by spaces.
   */
    public LongString mechanisms;
  /**
   * A list of the message locales that the server supports, delimited by spaces. The locale defines the language in which the server will send reply texts.
   */
    public LongString locales;
      
    public final static int INDEX = 10;
      
      
  /**
   * This method starts the connection negotiation process by telling the client the protocol version that the server proposes, along with a list of security mechanisms which the client can use for authentication.
   */
    public Start(int versionMajor,int versionMinor,Map serverProperties,LongString mechanisms,LongString locales) {
      this.versionMajor = versionMajor;
      this.versionMinor = versionMinor;
      this.serverProperties = serverProperties;
      this.mechanisms = mechanisms;
      this.locales = locales;
    }
    
    public Start() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Start";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.versionMajor = in.readOctet();
        this.versionMinor = in.readOctet();
        this.serverProperties = in.readTable();
        this.mechanisms = in.readLongstr();
        this.locales = in.readLongstr();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeOctet(this.versionMajor);
        out.writeOctet(this.versionMinor);
        out.writeTable(this.serverProperties);
        out.writeLongstr(this.mechanisms);
        out.writeLongstr(this.locales);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Start(");
      
      buff.append("versionMajor=");
      buff.append(versionMajor);
      buff.append(',');
      buff.append("versionMinor=");
      buff.append(versionMinor);
      buff.append(',');
      buff.append("serverProperties=");
      buff.append(serverProperties);
      buff.append(',');
      buff.append("mechanisms=");
      buff.append(mechanisms);
      buff.append(',');
      buff.append("locales=");
      buff.append(locales);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method selects a SASL security mechanism.
   */
  public static class StartOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public Map clientProperties;
  /**
   * A single security mechanisms selected by the client, which must be one of those specified by the server.
   */
    public java.lang.String mechanism;
  /**
   * A block of opaque data passed to the security mechanism. The contents of this data are defined by the SASL security mechanism.
   */
    public LongString response;
  /**
   * A single message locale selected by the client, which must be one of those specified by the server.
   */
    public java.lang.String locale;
      
    public final static int INDEX = 11;
      
      
  /**
   * This method selects a SASL security mechanism.
   */
    public StartOk(Map clientProperties,java.lang.String mechanism,LongString response,java.lang.String locale) {
      this.clientProperties = clientProperties;
      this.mechanism = mechanism;
      this.response = response;
      this.locale = locale;
    }
    
    public StartOk() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$StartOk";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.clientProperties = in.readTable();
        this.mechanism = in.readShortstr();
        this.response = in.readLongstr();
        this.locale = in.readShortstr();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeTable(this.clientProperties);
        out.writeShortstr(this.mechanism);
        out.writeLongstr(this.response);
        out.writeShortstr(this.locale);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("StartOk(");
      
      buff.append("clientProperties=");
      buff.append(clientProperties);
      buff.append(',');
      buff.append("mechanism=");
      buff.append(mechanism);
      buff.append(',');
      buff.append("response=");
      buff.append(response);
      buff.append(',');
      buff.append("locale=");
      buff.append(locale);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * The SASL protocol works by exchanging challenges and responses until both peers have received sufficient information to authenticate each other. This method challenges the client to provide more information.
   */
  public static class Secure extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * Challenge information, a block of opaque binary data passed to the security mechanism.
   */
    public LongString challenge;
      
    public final static int INDEX = 20;
      
      
  /**
   * The SASL protocol works by exchanging challenges and responses until both peers have received sufficient information to authenticate each other. This method challenges the client to provide more information.
   */
    public Secure(LongString challenge) {
      this.challenge = challenge;
    }
    
    public Secure() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Secure";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.challenge = in.readLongstr();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeLongstr(this.challenge);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Secure(");
      
      buff.append("challenge=");
      buff.append(challenge);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method attempts to authenticate, passing a block of SASL data for the security mechanism at the server side.
   */
  public static class SecureOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * A block of opaque data passed to the security mechanism. The contents of this data are defined by the SASL security mechanism.
   */
    public LongString response;
      
    public final static int INDEX = 21;
      
      
  /**
   * This method attempts to authenticate, passing a block of SASL data for the security mechanism at the server side.
   */
    public SecureOk(LongString response) {
      this.response = response;
    }
    
    public SecureOk() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$SecureOk";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.response = in.readLongstr();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeLongstr(this.response);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("SecureOk(");
      
      buff.append("response=");
      buff.append(response);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method proposes a set of connection configuration values to the client. The client can accept and/or adjust these.
   */
  public static class Tune extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
       * Specifies highest channel number that the server permits. Usable
       * channel numbers are in the range 1..channel-max. Zero indicates no
       * specified limit.
       */
    public int channelMax;
      /**
       * The largest frame size that the server proposes for the connection,
       * including frame header and end-byte. The client can negotiate a lower
       * value. Zero means that the server does not impose any specific limit
       * but may reject very large frames if it cannot allocate resources for
       * them.
       */
    public int frameMax;
  /**
   * The delay, in seconds, of the connection heartbeat that the server wants. Zero means the server does not want a heartbeat.
   */
    public int heartbeat;
      
    public final static int INDEX = 30;
      
      
  /**
   * This method proposes a set of connection configuration values to the client. The client can accept and/or adjust these.
   */
    public Tune(int channelMax,int frameMax,int heartbeat) {
      this.channelMax = channelMax;
      this.frameMax = frameMax;
      this.heartbeat = heartbeat;
    }
    
    public Tune() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Tune";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.channelMax = in.readShort();
        this.frameMax = in.readLong();
        this.heartbeat = in.readShort();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.channelMax);
        out.writeLong(this.frameMax);
        out.writeShort(this.heartbeat);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Tune(");
      
      buff.append("channelMax=");
      buff.append(channelMax);
      buff.append(',');
      buff.append("frameMax=");
      buff.append(frameMax);
      buff.append(',');
      buff.append("heartbeat=");
      buff.append(heartbeat);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method sends the client's connection tuning parameters to the server. Certain fields are negotiated, others provide capability information.
   */
  public static class TuneOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * The maximum total number of channels that the client will use per connection.
   */
    public int channelMax;
  /**
   * The largest frame size that the client and server will use for the connection. Zero means that the client does not impose any specific limit but may reject very large frames if it cannot allocate resources for them. Note that the frame-max limit applies principally to content frames, where large contents can be broken into frames of arbitrary size.
   */
    public int frameMax;
  /**
   * The delay, in seconds, of the connection heartbeat that the client wants. Zero means the client does not want a heartbeat.
   */
    public int heartbeat;
      
    public final static int INDEX = 31;
      
      
  /**
   * This method sends the client's connection tuning parameters to the server. Certain fields are negotiated, others provide capability information.
   */
    public TuneOk(int channelMax,int frameMax,int heartbeat) {
      this.channelMax = channelMax;
      this.frameMax = frameMax;
      this.heartbeat = heartbeat;
    }
    
    public TuneOk() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$TuneOk";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.channelMax = in.readShort();
        this.frameMax = in.readLong();
        this.heartbeat = in.readShort();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.channelMax);
        out.writeLong(this.frameMax);
        out.writeShort(this.heartbeat);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("TuneOk(");
      
      buff.append("channelMax=");
      buff.append(channelMax);
      buff.append(',');
      buff.append("frameMax=");
      buff.append(frameMax);
      buff.append(',');
      buff.append("heartbeat=");
      buff.append(heartbeat);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method opens a connection to a virtual host, which is a collection of resources, and acts to separate multiple application domains within a server. The server may apply arbitrary limits per virtual host, such as the number of each type of entity that may be used, per connection and/or in total.
   */
  public static class Open extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * The name of the virtual host to work with.
   */
    public java.lang.String virtualHost;
      /**
   * 
   */
    public java.lang.String reserved1;
      /**
   * 
   */
    public boolean reserved2;

      public final static int INDEX = 40;
      
      
  /**
   * This method opens a connection to a virtual host, which is a collection of resources, and acts to separate multiple application domains within a server. The server may apply arbitrary limits per virtual host, such as the number of each type of entity that may be used, per connection and/or in total.
   */
    public Open(java.lang.String virtualHost, java.lang.String reserved1, boolean reserved2) {
      this.virtualHost = virtualHost;
      this.reserved1 = reserved1;
        this.reserved2 = reserved2;
    }
    
    public Open() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Open";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.virtualHost = in.readShortstr();
        this.reserved1 = in.readShortstr();
        this.reserved2 = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShortstr(this.virtualHost);
        out.writeShortstr(this.reserved1);
        out.writeBit(this.reserved2);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Open(");
      
      buff.append("virtualHost=");
      buff.append(virtualHost);
      buff.append(',');
      buff.append("reserved1=");
        buff.append(reserved1);
      buff.append(',');
      buff.append("reserved2=");
        buff.append(reserved2);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method signals to the client that the connection is ready for use.
   */
  public static class OpenOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public java.lang.String reserved1;

      public final static int INDEX = 41;
      
      
  /**
   * This method signals to the client that the connection is ready for use.
   */
    public OpenOk(java.lang.String reserved1) {
        this.reserved1 = reserved1;
    }
    
    public OpenOk() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$OpenOk";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.reserved1 = in.readShortstr();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShortstr(this.reserved1);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("OpenOk(");
      
      buff.append("reserved1=");
        buff.append(reserved1);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method indicates that the sender wants to close the connection. This
     * may be due to internal conditions (e.g. a forced shut-down) or due to an
     * error handling a specific method, i.e. an exception. When a close is due
     * to an exception, the sender provides the class and method id of the
     * method which caused the exception.
     */
  public static class Close extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
   * 
   */
    public int replyCode;
  /**
   * 
   */
    public java.lang.String replyText;
      /**
       * When the close is provoked by a method exception, this is the class of
       * the method.
       */
      public int classId;
      /**
       * When the close is provoked by a method exception, this is the ID of the
       * method.
       */
      public int methodId;

      public final static int INDEX = 50;

      /**
       * This method indicates that the sender wants to close the connection.
       * This may be due to internal conditions (e.g. a forced shut-down) or due
       * to an error handling a specific method, i.e. an exception. When a close
       * is due to an exception, the sender provides the class and method id of
       * the method which caused the exception.
       */
    public Close(int replyCode, java.lang.String replyText, int classId, int methodId) {
        this.replyCode = replyCode;
        this.replyText = replyText;
        this.classId = classId;
        this.methodId = methodId;
    }
    
    public Close() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Close";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.replyCode = in.readShort();
        this.replyText = in.readShortstr();
        this.classId = in.readShort();
        this.methodId = in.readShort();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.replyCode);
        out.writeShortstr(this.replyText);
        out.writeShort(this.classId);
        out.writeShort(this.methodId);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Close(");
      
      buff.append("replyCode=");
        buff.append(replyCode);
        buff.append(',');
        buff.append("replyText=");
        buff.append(replyText);
        buff.append(',');
        buff.append("classId=");
        buff.append(classId);
      buff.append(',');
      buff.append("methodId=");
        buff.append(methodId);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method confirms a Connection.Close method and tells the recipient
     * that it is safe to release resources for the connection and close the
     * socket.
     */
  public static class CloseOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 51;

      /**
       * This method confirms a Connection.Close method and tells the recipient
       * that it is safe to release resources for the connection and close the
       * socket.
       */
    public CloseOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$CloseOk";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("CloseOk(");
      
      buff.append(')');
        return buff.toString();
      }
    }

  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Connection(");
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Channel extends AbstractMarshallingClass {
  
    private static final long serialVersionUID = 1L;

  
      
    public final static int INDEX = 20;
      
    public int getClassId() { 
      return INDEX;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel";
    }
    
  
    public static final int METHOD_CHANNEL_OPEN = 10;
    public static final int METHOD_CHANNEL_OPEN_OK = 11;
    public static final int METHOD_CHANNEL_FLOW = 20;
    public static final int METHOD_CHANNEL_FLOW_OK = 21;
    public static final int METHOD_CHANNEL_CLOSE = 40;
    public static final int METHOD_CHANNEL_CLOSE_OK = 41;
  

    public static final int[] mids = { 10, 11, 20, 21, 40, 41 };
    
    public static final java.lang.String[] methodnames = {
      "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Open",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$OpenOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Flow",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$FlowOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Close",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$CloseOk"
    };
    
    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.Channel.mids.length; i++) {
        if (AMQP.Channel.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.Channel.methodnames[pos];  
      return "";
    }
  


  /**
   * This method opens a channel to the server.
   */
  public static class Open extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
   * 
   */
    public java.lang.String reserved1;

      public final static int INDEX = 10;
      
      
  /**
   * This method opens a channel to the server.
   */
    public Open(java.lang.String reserved1) {
        this.reserved1 = reserved1;
    }
    
    public Open() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Open";
      
    }
    
    public int getClassId() { 
      return 20;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.reserved1 = in.readShortstr();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShortstr(this.reserved1);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Open(");
      
      buff.append("reserved1=");
        buff.append(reserved1);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method signals to the client that the channel is ready for use.
   */
  public static class OpenOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public LongString reserved1;

      public final static int INDEX = 11;
      
      
  /**
   * This method signals to the client that the channel is ready for use.
   */
    public OpenOk(LongString reserved1) {
        this.reserved1 = reserved1;
    }
    
    public OpenOk() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$OpenOk";
      
    }
    
    public int getClassId() { 
      return 20;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.reserved1 = in.readLongstr();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeLongstr(this.reserved1);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("OpenOk(");
      
      buff.append("reserved1=");
        buff.append(reserved1);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method asks the peer to pause or restart the flow of content data
     * sent by a consumer. This is a simple flow-control mechanism that a peer
     * can use to avoid overflowing its queues or otherwise finding itself
     * receiving more messages than it can process. Note that this method is not
     * intended for window control. It does not affect contents returned by
     * Basic.Get-Ok methods.
     */
  public static class Flow extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * If 1, the peer starts sending content frames. If 0, the peer stops sending content frames.
   */
    public boolean active;
      
    public final static int INDEX = 20;

      /**
       * This method asks the peer to pause or restart the flow of content data
       * sent by a consumer. This is a simple flow-control mechanism that a peer
       * can use to avoid overflowing its queues or otherwise finding itself
       * receiving more messages than it can process. Note that this method is
       * not intended for window control. It does not affect contents returned
       * by Basic.Get-Ok methods.
       */
    public Flow(boolean active) {
      this.active = active;
    }
    
    public Flow() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Flow";
      
    }
    
    public int getClassId() { 
      return 20;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.active = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeBit(this.active);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Flow(");
      
      buff.append("active=");
      buff.append(active);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * Confirms to the peer that a flow command was received and processed.
   */
  public static class FlowOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * Confirms the setting of the processed flow method: 1 means the peer will start sending or continue to send content frames; 0 means it will not.
   */
    public boolean active;
      
    public final static int INDEX = 21;
      
      
  /**
   * Confirms to the peer that a flow command was received and processed.
   */
    public FlowOk(boolean active) {
      this.active = active;
    }
    
    public FlowOk() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$FlowOk";
      
    }
    
    public int getClassId() { 
      return 20;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.active = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeBit(this.active);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("FlowOk(");
      
      buff.append("active=");
      buff.append(active);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method indicates that the sender wants to close the channel. This may be due to internal conditions (e.g. a forced shut-down) or due to an error handling a specific method, i.e. an exception. When a close is due to an exception, the sender provides the class and method id of the method which caused the exception.
   */
  public static class Close extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int replyCode;
  /**
   * 
   */
    public java.lang.String replyText;
  /**
   * When the close is provoked by a method exception, this is the class of the method.
   */
    public int classId;
  /**
   * When the close is provoked by a method exception, this is the ID of the method.
   */
    public int methodId;
      
    public final static int INDEX = 40;
      
      
  /**
   * This method indicates that the sender wants to close the channel. This may be due to internal conditions (e.g. a forced shut-down) or due to an error handling a specific method, i.e. an exception. When a close is due to an exception, the sender provides the class and method id of the method which caused the exception.
   */
    public Close(int replyCode,java.lang.String replyText,int classId,int methodId) {
      this.replyCode = replyCode;
      this.replyText = replyText;
      this.classId = classId;
      this.methodId = methodId;
    }
    
    public Close() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Close";
      
    }
    
    public int getClassId() { 
      return 20;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.replyCode = in.readShort();
        this.replyText = in.readShortstr();
        this.classId = in.readShort();
        this.methodId = in.readShort();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.replyCode);
        out.writeShortstr(this.replyText);
        out.writeShort(this.classId);
        out.writeShort(this.methodId);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Close(");
      
      buff.append("replyCode=");
      buff.append(replyCode);
      buff.append(',');
      buff.append("replyText=");
      buff.append(replyText);
      buff.append(',');
      buff.append("classId=");
      buff.append(classId);
      buff.append(',');
      buff.append("methodId=");
      buff.append(methodId);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method confirms a Channel.Close method and tells the recipient that it is safe to release resources for the channel.
   */
  public static class CloseOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 41;
      
      
  /**
   * This method confirms a Channel.Close method and tells the recipient that it is safe to release resources for the channel.
   */
    public CloseOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$CloseOk";
      
    }
    
    public int getClassId() { 
      return 20;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("CloseOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  
  public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Channel(");
    
    buff.append(')');
      return buff.toString();
    }
  }

  public static class Exchange extends AbstractMarshallingClass {

    private static final long serialVersionUID = 1L;

  
      
    public final static int INDEX = 40;
      
    public int getClassId() { 
      return INDEX;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange";
    }
    
  
    public static final int METHOD_EXCHANGE_DECLARE = 10;
    public static final int METHOD_EXCHANGE_DECLARE_OK = 11;
    public static final int METHOD_EXCHANGE_DELETE = 20;
    public static final int METHOD_EXCHANGE_DELETE_OK = 21;
  

    public static final int[] mids = { 10, 11, 20, 21 };
    
    public static final java.lang.String[] methodnames = {
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$Declare",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$DeclareOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$Delete",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$DeleteOk" };
    
    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.Exchange.mids.length; i++) {
        if (AMQP.Exchange.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.Exchange.methodnames[pos];
      return "";
    }
  


  /**
     * This method creates an exchange if it does not already exist, and if the
     * exchange exists, verifies that it is of the correct and expected class.
     */
  public static class Declare extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
   * 
   */
      public int reserved1;
      /**
   * 
   */
      public java.lang.String exchange;
      /**
       * Each exchange belongs to one of a set of exchange types implemented by
       * the server. The exchange types define the functionality of the exchange
       * - i.e. how messages are routed through it. It is not valid or
       * meaningful to attempt to change the type of an existing exchange.
       */
      public java.lang.String type;
      /**
       * If set, the server will reply with Declare-Ok if the exchange already
       * exists with the same name, and raise an error if not. The client can
       * use this to check whether an exchange exists without modifying the
       * server state. When set, all other method fields except name and no-wait
       * are ignored. A declare with both passive and no-wait has no effect.
       * Arguments are compared for semantic equivalence.
       */
      public boolean passive;
      /**
       * If set when creating a new exchange, the exchange will be marked as
       * durable. Durable exchanges remain active when a server restarts.
       * Non-durable exchanges (transient exchanges) are purged if/when a server
       * restarts.
       */
      public boolean durable;
      /**
   * 
   */
      public boolean reserved2;
      /**
   * 
   */
      public boolean reserved3;
      /**
   * 
   */
      public boolean noWait;
      /**
       * A set of arguments for the declaration. The syntax and semantics of
       * these arguments depends on the server implementation.
       */
      public Map arguments;

      public final static int INDEX = 10;

      /**
       * This method creates an exchange if it does not already exist, and if
       * the exchange exists, verifies that it is of the correct and expected
       * class.
       */
    public Declare(int reserved1, java.lang.String exchange, java.lang.String type, boolean passive,
          boolean durable, boolean reserved2, boolean reserved3, boolean noWait, Map arguments) {
        this.reserved1 = reserved1;
        this.exchange = exchange;
        this.type = type;
        this.passive = passive;
        this.durable = durable;
        this.reserved2 = reserved2;
        this.reserved3 = reserved3;
        this.noWait = noWait;
        this.arguments = arguments;
    }
    
    public Declare() {
      }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$Declare";
      
    }
    
    public int getClassId() { 
      return 40;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.reserved1 = in.readShort();
        this.exchange = in.readShortstr();
        this.type = in.readShortstr();
        this.passive = in.readBit();
        this.durable = in.readBit();
        this.reserved2 = in.readBit();
        this.reserved3 = in.readBit();
        this.noWait = in.readBit();
        this.arguments = in.readTable();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.reserved1);
        out.writeShortstr(this.exchange);
        out.writeShortstr(this.type);
        out.writeBit(this.passive);
        out.writeBit(this.durable);
        out.writeBit(this.reserved2);
        out.writeBit(this.reserved3);
        out.writeBit(this.noWait);
        out.writeTable(this.arguments);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Declare(");

        buff.append("reserved1=");
        buff.append(reserved1);
        buff.append(',');
        buff.append("exchange=");
        buff.append(exchange);
        buff.append(',');
        buff.append("type=");
        buff.append(type);
        buff.append(',');
        buff.append("passive=");
        buff.append(passive);
        buff.append(',');
        buff.append("durable=");
        buff.append(durable);
        buff.append(',');
        buff.append("reserved2=");
        buff.append(reserved2);
        buff.append(',');
        buff.append("reserved3=");
        buff.append(reserved3);
        buff.append(',');
        buff.append("noWait=");
        buff.append(noWait);
        buff.append(',');
        buff.append("arguments=");
        buff.append(arguments);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method confirms a Declare method and confirms the name of the
     * exchange, essential for automatically-named exchanges.
     */
  public static class DeclareOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 11;

      /**
       * This method confirms a Declare method and confirms the name of the
       * exchange, essential for automatically-named exchanges.
       */
    public DeclareOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$DeclareOk";
      
    }
    
    public int getClassId() { 
      return 40;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("DeclareOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
     * This method deletes an exchange. When an exchange is deleted all queue
     * bindings on the exchange are cancelled.
     */
    public static class Delete extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
   * 
   */
    public int reserved1;
      /**
   * 
   */
    public java.lang.String exchange;
      /**
       * If set, the server will only delete the exchange if it has no queue
       * bindings. If the exchange has queue bindings the server does not delete
       * it but raises a channel exception instead.
       */
    public boolean ifUnused;
      /**
   * 
   */
    public boolean noWait;

      public final static int INDEX = 20;

      /**
       * This method deletes an exchange. When an exchange is deleted all queue
       * bindings on the exchange are cancelled.
       */
    public Delete(int reserved1, java.lang.String exchange, boolean ifUnused, boolean noWait) {
        this.reserved1 = reserved1;
        this.exchange = exchange;
        this.ifUnused = ifUnused;
        this.noWait = noWait;
      }

      public Delete() {
      }

      public int getMethodId() {
        return INDEX;
      }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$Delete";
      
    }
    
    public int getClassId() { 
      return 40;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.reserved1 = in.readShort();
        this.exchange = in.readShortstr();
        this.ifUnused = in.readBit();
        this.noWait = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.reserved1);
        out.writeShortstr(this.exchange);
        out.writeBit(this.ifUnused);
        out.writeBit(this.noWait);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Delete(");
      
      buff.append("reserved1=");
        buff.append(reserved1);
      buff.append(',');
      buff.append("exchange=");
        buff.append(exchange);
      buff.append(',');
      buff.append("ifUnused=");
        buff.append(ifUnused);
      buff.append(',');
      buff.append("noWait=");
        buff.append(noWait);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method confirms the deletion of an exchange.
     */
  public static class DeleteOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 21;

      /**
       * This method confirms the deletion of an exchange.
       */
    public DeleteOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$DeleteOk";
      
    }
    
    public int getClassId() { 
      return 40;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("DeleteOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Exchange(");
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Queue extends AbstractMarshallingClass {
  
    private static final long serialVersionUID = 1L;

  
      
    public final static int INDEX = 50;
      
    public int getClassId() { 
      return INDEX;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue";
    }
    
  
    public static final int METHOD_QUEUE_DECLARE = 10;
    public static final int METHOD_QUEUE_DECLARE_OK = 11;
    public static final int METHOD_QUEUE_BIND = 20;
    public static final int METHOD_QUEUE_BIND_OK = 21;
    public static final int METHOD_QUEUE_UNBIND = 50;
    public static final int METHOD_QUEUE_UNBIND_OK = 51;
    public static final int METHOD_QUEUE_PURGE = 30;
    public static final int METHOD_QUEUE_PURGE_OK = 31;
    public static final int METHOD_QUEUE_DELETE = 40;
    public static final int METHOD_QUEUE_DELETE_OK = 41;
  

    public static final int[] mids = { 10, 11, 20, 21, 50, 51, 30, 31, 40, 41 };
    
    public static final java.lang.String[] methodnames = {
      "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$Declare",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$DeclareOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$Bind",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$BindOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$Unbind",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$UnbindOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$Purge",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$PurgeOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$Delete",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$DeleteOk"
    };
    
    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.Queue.mids.length; i++) {
        if (AMQP.Queue.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.Queue.methodnames[pos];  
      return "";
    }

    /**
     * This method creates or checks a queue. When creating a new queue the
     * client can specify various properties that control the durability of the
     * queue and its contents, and the level of sharing for the queue.
     */
  public static class Declare extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
   * 
   */
    public int reserved1;
  /**
   * 
   */
    public java.lang.String queue;
      /**
       * If set, the server will reply with Declare-Ok if the queue already
       * exists with the same name, and raise an error if not. The client can
       * use this to check whether a queue exists without modifying the server
       * state. When set, all other method fields except name and no-wait are
       * ignored. A declare with both passive and no-wait has no effect.
       * Arguments are compared for semantic equivalence.
       */
    public boolean passive;
      /**
       * If set when creating a new queue, the queue will be marked as durable.
       * Durable queues remain active when a server restarts. Non-durable queues
       * (transient queues) are purged if/when a server restarts. Note that
       * durable queues do not necessarily hold persistent messages, although it
       * does not make sense to send persistent messages to a transient queue.
       */
    public boolean durable;
      /**
       * Exclusive queues may only be accessed by the current connection, and
       * are deleted when that connection closes. Passive declaration of an
       * exclusive queue by other connections are not allowed.
       */
    public boolean exclusive;
      /**
       * If set, the queue is deleted when all consumers have finished using it.
       * The last consumer can be cancelled either explicitly or because its
       * channel is closed. If there was no consumer ever on the queue, it won't
       * be deleted. Applications can explicitly delete auto-delete queues using
       * the Delete method as normal.
       */
    public boolean autoDelete;
      /**
   * 
   */
    public boolean noWait;
      /**
       * A set of arguments for the declaration. The syntax and semantics of
       * these arguments depends on the server implementation.
       */
    public Map arguments;
      
    public final static int INDEX = 10;

      /**
       * This method creates or checks a queue. When creating a new queue the
       * client can specify various properties that control the durability of
       * the queue and its contents, and the level of sharing for the queue.
       */
    public Declare(int reserved1, java.lang.String queue, boolean passive, boolean durable,
          boolean exclusive, boolean autoDelete, boolean noWait, Map arguments) {
        this.reserved1 = reserved1;
        this.queue = queue;
      this.passive = passive;
      this.durable = durable;
      this.exclusive = exclusive;
      this.autoDelete = autoDelete;
      this.noWait = noWait;
      this.arguments = arguments;
    }
    
    public Declare() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$Declare";
      
    }
    
    public int getClassId() { 
      return 50;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.reserved1 = in.readShort();
        this.queue = in.readShortstr();
        this.passive = in.readBit();
        this.durable = in.readBit();
        this.exclusive = in.readBit();
        this.autoDelete = in.readBit();
        this.noWait = in.readBit();
        this.arguments = in.readTable();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.reserved1);
        out.writeShortstr(this.queue);
        out.writeBit(this.passive);
        out.writeBit(this.durable);
        out.writeBit(this.exclusive);
        out.writeBit(this.autoDelete);
        out.writeBit(this.noWait);
        out.writeTable(this.arguments);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Declare(");
      
      buff.append("reserved1=");
        buff.append(reserved1);
      buff.append(',');
      buff.append("queue=");
        buff.append(queue);
      buff.append(',');
      buff.append("passive=");
      buff.append(passive);
      buff.append(',');
      buff.append("durable=");
      buff.append(durable);
      buff.append(',');
      buff.append("exclusive=");
        buff.append(exclusive);
        buff.append(',');
      buff.append("autoDelete=");
      buff.append(autoDelete);
      buff.append(',');
      buff.append("noWait=");
        buff.append(noWait);
      buff.append(',');
      buff.append("arguments=");
      buff.append(arguments);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method confirms a Declare method and confirms the name of the queue,
     * essential for automatically-named queues.
     */
  public static class DeclareOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
       * Reports the name of the queue. If the server generated a queue name,
       * this field contains that name.
       */
      public java.lang.String queue;
      /**
   * 
   */
      public int messageCount;
      /**
       * Reports the number of active consumers for the queue. Note that
       * consumers can suspend activity (Channel.Flow) in which case they do not
       * appear in this count.
       */
    public int consumerCount;
      
    public final static int INDEX = 11;

      /**
       * This method confirms a Declare method and confirms the name of the
       * queue, essential for automatically-named queues.
       */
      public DeclareOk(java.lang.String queue, int messageCount, int consumerCount) {
        this.queue = queue;
        this.messageCount = messageCount;
        this.consumerCount = consumerCount;
      }

      public DeclareOk() {
      }

      public int getMethodId() {
        return INDEX;
      }

      public java.lang.String getMethodName() {
        return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$DeclareOk";
      
    }
    
    public int getClassId() { 
      return 50;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.queue = in.readShortstr();
        this.messageCount = in.readLong();
        this.consumerCount = in.readLong();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShortstr(this.queue);
        out.writeLong(this.messageCount);
        out.writeLong(this.consumerCount);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("DeclareOk(");
      
      buff.append("queue=");
        buff.append(queue);
        buff.append(',');
        buff.append("messageCount=");
        buff.append(messageCount);
        buff.append(',');
        buff.append("consumerCount=");
        buff.append(consumerCount);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method binds a queue to an exchange. Until a queue is bound it will
     * not receive any messages. In a classic messaging model, store-and-forward
     * queues are bound to a direct exchange and subscription queues are bound
     * to a topic exchange.
     */
  public static class Bind extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int reserved1;
      /**
       * Specifies the name of the queue to bind.
       */
      public java.lang.String queue;
  /**
   * 
   */
    public java.lang.String exchange;
      /**
       * Specifies the routing key for the binding. The routing key is used for
       * routing messages depending on the exchange configuration. Not all
       * exchanges use a routing key - refer to the specific exchange
       * documentation. If the queue name is empty, the server uses the last
       * queue declared on the channel. If the routing key is also empty, the
       * server uses this queue name for the routing key as well. If the queue
       * name is provided but the routing key is empty, the server does the
       * binding with that empty routing key. The meaning of empty routing keys
       * depends on the exchange implementation.
       */
      public java.lang.String routingKey;
      /**
   * 
   */
    public boolean noWait;
      /**
       * A set of arguments for the binding. The syntax and semantics of these
       * arguments depends on the exchange class.
       */
    public Map arguments;

      public final static int INDEX = 20;

      /**
       * This method binds a queue to an exchange. Until a queue is bound it
       * will not receive any messages. In a classic messaging model,
       * store-and-forward queues are bound to a direct exchange and
       * subscription queues are bound to a topic exchange.
       */
    public Bind(int reserved1, java.lang.String queue, java.lang.String exchange,
          java.lang.String routingKey, boolean noWait, Map arguments) {
        this.reserved1 = reserved1;
        this.queue = queue;
      this.exchange = exchange;
      this.routingKey = routingKey;
        this.noWait = noWait;
        this.arguments = arguments;
    }
    
    public Bind() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$Bind";
      
    }
    
    public int getClassId() { 
      return 50;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.reserved1 = in.readShort();
        this.queue = in.readShortstr();
        this.exchange = in.readShortstr();
        this.routingKey = in.readShortstr();
        this.noWait = in.readBit();
        this.arguments = in.readTable();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.reserved1);
        out.writeShortstr(this.queue);
        out.writeShortstr(this.exchange);
        out.writeShortstr(this.routingKey);
        out.writeBit(this.noWait);
        out.writeTable(this.arguments);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Bind(");
      
      buff.append("reserved1=");
        buff.append(reserved1);
        buff.append(',');
        buff.append("queue=");
        buff.append(queue);
      buff.append(',');
      buff.append("exchange=");
      buff.append(exchange);
      buff.append(',');
      buff.append("routingKey=");
        buff.append(routingKey);
        buff.append(',');
        buff.append("noWait=");
        buff.append(noWait);
      buff.append(',');
      buff.append("arguments=");
        buff.append(arguments);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method confirms that the bind was successful.
     */
  public static class BindOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 21;

      /**
       * This method confirms that the bind was successful.
       */
    public BindOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$BindOk";
      
    }
    
    public int getClassId() { 
      return 50;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("BindOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
     * This method unbinds a queue from an exchange.
     */
    public static class Unbind extends AbstractMarshallingMethod {

      private static final long serialVersionUID = 1L;

      /**
   * 
   */
      public int reserved1;
      /**
       * Specifies the name of the queue to unbind.
       */
      public java.lang.String queue;
      /**
       * The name of the exchange to unbind from.
       */
      public java.lang.String exchange;
      /**
       * Specifies the routing key of the binding to unbind.
       */
      public java.lang.String routingKey;
      /**
       * Specifies the arguments of the binding to unbind.
       */
      public Map arguments;

      public final static int INDEX = 50;

      /**
       * This method unbinds a queue from an exchange.
       */
      public Unbind(int reserved1, java.lang.String queue, java.lang.String exchange,
          java.lang.String routingKey, Map arguments) {
        this.reserved1 = reserved1;
        this.queue = queue;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.arguments = arguments;
      }
    
    public Unbind() {
      }

      public int getMethodId() {
        return INDEX;
      }

    public java.lang.String getMethodName() {
        return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$Unbind";

      }
    
    public int getClassId() { 
      return 50;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue";
    }

    public void readFrom(AMQPInputStream in) throws IOException {
        this.reserved1 = in.readShort();
        this.queue = in.readShortstr();
        this.exchange = in.readShortstr();
        this.routingKey = in.readShortstr();
        this.arguments = in.readTable();
      }

      public void writeTo(AMQPOutputStream out) throws IOException {
        out.writeShort(this.reserved1);
        out.writeShortstr(this.queue);
        out.writeShortstr(this.exchange);
        out.writeShortstr(this.routingKey);
        out.writeTable(this.arguments);
      }
    
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Unbind(");

        buff.append("reserved1=");
        buff.append(reserved1);
        buff.append(',');
        buff.append("queue=");
        buff.append(queue);
        buff.append(',');
        buff.append("exchange=");
        buff.append(exchange);
        buff.append(',');
        buff.append("routingKey=");
        buff.append(routingKey);
        buff.append(',');
        buff.append("arguments=");
        buff.append(arguments);

        buff.append(')');
        return buff.toString();
    }
  }

    /**
     * This method confirms that the unbind was successful.
     */
  public static class UnbindOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 51;

      /**
       * This method confirms that the unbind was successful.
       */
    public UnbindOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$UnbindOk";
      
    }
    
    public int getClassId() { 
      return 50;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("UnbindOk(");
      
      buff.append(')');
        return buff.toString();
    }
  }

    /**
     * This method removes all messages from a queue which are not awaiting
     * acknowledgment.
     */
  public static class Purge extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
   * 
   */
    public int reserved1;
      /**
       * Specifies the name of the queue to purge.
       */
    public java.lang.String queue;
      /**
   * 
   */
    public boolean noWait;

      public final static int INDEX = 30;

      /**
       * This method removes all messages from a queue which are not awaiting
       * acknowledgment.
       */
    public Purge(int reserved1, java.lang.String queue, boolean noWait) {
        this.reserved1 = reserved1;
      this.queue = queue;
      this.noWait = noWait;
    }
    
    public Purge() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$Purge";
      
    }
    
    public int getClassId() { 
      return 50;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.reserved1 = in.readShort();
        this.queue = in.readShortstr();
        this.noWait = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.reserved1);
        out.writeShortstr(this.queue);
        out.writeBit(this.noWait);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Purge(");
      
      buff.append("reserved1=");
        buff.append(reserved1);
        buff.append(',');
        buff.append("queue=");
      buff.append(queue);
      buff.append(',');
      buff.append("noWait=");
        buff.append(noWait);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method confirms the purge of a queue.
     */
  public static class PurgeOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
       * Reports the number of messages purged.
       */
    public int messageCount;

      public final static int INDEX = 31;

      /**
       * This method confirms the purge of a queue.
       */
    public PurgeOk(int messageCount) {
        this.messageCount = messageCount;
    }
    
    public PurgeOk() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$PurgeOk";
      
    }
    
    public int getClassId() { 
      return 50;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.messageCount = in.readLong();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeLong(this.messageCount);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("PurgeOk(");
      
      buff.append("messageCount=");
        buff.append(messageCount);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method deletes a queue. When a queue is deleted any pending messages
     * are sent to a dead-letter queue if this is defined in the server
     * configuration, and all consumers on the queue are cancelled.
     */
  public static class Delete extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
   * 
   */
      public int reserved1;
      /**
       * Specifies the name of the queue to delete.
       */
      public java.lang.String queue;
      /**
       * If set, the server will only delete the queue if it has no consumers.
       * If the queue has consumers the server does does not delete it but
       * raises a channel exception instead.
       */
      public boolean ifUnused;
      /**
       * If set, the server will only delete the queue if it has no messages.
       */
      public boolean ifEmpty;
      /**
   * 
   */
      public boolean noWait;

      public final static int INDEX = 40;

      /**
       * This method deletes a queue. When a queue is deleted any pending
       * messages are sent to a dead-letter queue if this is defined in the
       * server configuration, and all consumers on the queue are cancelled.
       */
    public Delete(int reserved1, java.lang.String queue, boolean ifUnused, boolean ifEmpty, boolean noWait) {
        this.reserved1 = reserved1;
        this.queue = queue;
        this.ifUnused = ifUnused;
        this.ifEmpty = ifEmpty;
        this.noWait = noWait;
    }
    
    public Delete() {
      }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$Delete";
      
    }
    
    public int getClassId() { 
      return 50;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.reserved1 = in.readShort();
        this.queue = in.readShortstr();
        this.ifUnused = in.readBit();
        this.ifEmpty = in.readBit();
        this.noWait = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.reserved1);
        out.writeShortstr(this.queue);
        out.writeBit(this.ifUnused);
        out.writeBit(this.ifEmpty);
        out.writeBit(this.noWait);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Delete(");
      
      buff.append("reserved1=");
        buff.append(reserved1);
        buff.append(',');
        buff.append("queue=");
        buff.append(queue);
        buff.append(',');
        buff.append("ifUnused=");
        buff.append(ifUnused);
        buff.append(',');
        buff.append("ifEmpty=");
        buff.append(ifEmpty);
        buff.append(',');
        buff.append("noWait=");
        buff.append(noWait);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method confirms the deletion of a queue.
     */
  public static class DeleteOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
       * Reports the number of messages deleted.
       */
    public int messageCount;

      public final static int INDEX = 41;

      /**
       * This method confirms the deletion of a queue.
       */
    public DeleteOk(int messageCount) {
        this.messageCount = messageCount;
    }
    
    public DeleteOk() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue$DeleteOk";
      
    }
    
    public int getClassId() { 
      return 50;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.messageCount = in.readLong();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeLong(this.messageCount);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("DeleteOk(");
      
      buff.append("messageCount=");
        buff.append(messageCount);
      
      buff.append(')');
      return buff.toString();
    }
  }

  
  public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Queue(");

      buff.append(')');
      return buff.toString();
    }
  }

  public static class Access extends AbstractMarshallingClass {
    
    private static final long serialVersionUID = 1L;

    public final static int INDEX = 30;

    public int getClassId() {
      return INDEX;
    }

    public java.lang.String getClassName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Access";
    }

    public static final int METHOD_ACCESS_REQUEST = 10;
    public static final int METHOD_ACCESS_REQUEST_OK = 11;

    public static final int[] mids = { 10, 11 };

    public static final java.lang.String[] methodnames = {
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Access$Request",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Access$RequestOk" };

    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.Access.mids.length; i++) {
        if (AMQP.Access.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.Access.methodnames[pos];
      return "";
    }

    /**
     * This method requests an access ticket for an access realm. The server
     * responds by granting the access ticket. If the client does not have
     * access rights to the requested realm this causes a connection exception.
     * Access tickets are a per-channel resource.
     */
    public static class Request extends AbstractMarshallingMethod {
      private static final long serialVersionUID = 1L;

      /**
       * Specifies the name of the realm to which the client is requesting
       * access. The realm is a configured server-side object that collects a
       * set of resources (exchanges, queues, etc.). If the channel has already
       * requested an access ticket onto this realm, the previous ticket is
       * destroyed and a new ticket is created with the requested access rights,
       * if allowed.
       */
      public java.lang.String realm;
      /**
       * Request exclusive access to the realm, meaning that this will be the
       * only channel that uses the realm's resources.
       */
      public boolean exclusive;
      /**
       * Request message passive access to the specified access realm. Passive
       * access lets a client get information about resources in the realm but
       * not to make any changes to them.
       */
      public boolean passive;
      /**
       * Request message active access to the specified access realm. Active
       * access lets a client get create and delete resources in the realm.
       */
      public boolean active;
      /**
       * Request write access to the specified access realm. Write access lets a
       * client publish messages to all exchanges in the realm.
       */
      public boolean write;
      /**
       * Request read access to the specified access realm. Read access lets a
       * client consume messages from queues in the realm.
       */
      public boolean read;

      public final static int INDEX = 10;

      /**
       * This method requests an access ticket for an access realm. The server
       * responds by granting the access ticket. If the client does not have
       * access rights to the requested realm this causes a connection
       * exception. Access tickets are a per-channel resource.
       */
      public Request(java.lang.String realm, boolean exclusive, boolean passive, boolean active,
          boolean write, boolean read) {
        this.realm = realm;
        this.exclusive = exclusive;
        this.passive = passive;
        this.active = active;
        this.write = write;
        this.read = read;
      }

      public Request() {
      }

      public int getMethodId() {
        return INDEX;
      }

      public java.lang.String getMethodName() {
        return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Access$Request";

      }

      public int getClassId() {
        return 30;
      }

      public java.lang.String getClassName() {
        return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Access";
      }

      public void readFrom(AMQPInputStream in) throws IOException {
        this.realm = in.readShortstr();
        this.exclusive = in.readBit();
        this.passive = in.readBit();
        this.active = in.readBit();
        this.write = in.readBit();
        this.read = in.readBit();
      }

      public void writeTo(AMQPOutputStream out) throws IOException {
        out.writeShortstr(this.realm);
        out.writeBit(this.exclusive);
        out.writeBit(this.passive);
        out.writeBit(this.active);
        out.writeBit(this.write);
        out.writeBit(this.read);
      }

      public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Request(");

        buff.append("realm=");
        buff.append(realm);
        buff.append(',');
        buff.append("exclusive=");
        buff.append(exclusive);
        buff.append(',');
        buff.append("passive=");
        buff.append(passive);
        buff.append(',');
        buff.append("active=");
        buff.append(active);
        buff.append(',');
        buff.append("write=");
        buff.append(write);
        buff.append(',');
        buff.append("read=");
        buff.append(read);

        buff.append(')');
        return buff.toString();
      }
    }

    /**
     * This method provides the client with an access ticket. The access ticket
     * is valid within the current channel and for the lifespan of the channel.
     */
    public static class RequestOk extends AbstractMarshallingMethod {
      private static final long serialVersionUID = 1L;

      /**
 * 
 */
      public int ticket;

      public final static int INDEX = 11;

      /**
       * This method provides the client with an access ticket. The access
       * ticket is valid within the current channel and for the lifespan of the
       * channel.
       */
      public RequestOk(int ticket) {
        this.ticket = ticket;
      }

      public RequestOk() {
      }

      public int getMethodId() {
        return INDEX;
      }

      public java.lang.String getMethodName() {
        return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Access$RequestOk";

      }

      public int getClassId() {
        return 30;
      }

      public java.lang.String getClassName() {
        return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Access";
      }

      public void readFrom(AMQPInputStream in) throws IOException {
        this.ticket = in.readShort();
      }

      public void writeTo(AMQPOutputStream out) throws IOException {
        out.writeShort(this.ticket);
      }

      public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("RequestOk(");

        buff.append("ticket=");
        buff.append(ticket);

        buff.append(')');
        return buff.toString();
      }
    }

    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Access(");

      buff.append(')');
      return buff.toString();
    }
  }

  public static class Basic extends AbstractMarshallingClass {
  
    private static final long serialVersionUID = 1L;

    public static class BasicProperties {
      public java.lang.String contentType;
      public java.lang.String contentEncoding;
      public Map headers;
      public int deliveryMode;
      public int priority;
      public java.lang.String correlationId;
      public java.lang.String replyTo;
      public java.lang.String expiration;
      public java.lang.String messageId;
      public Date timestamp;
      public java.lang.String type;
      public java.lang.String userId;
      public java.lang.String appId;
      public java.lang.String clusterId;

      private int bitCount = 0;
      private int flag = 1;

      private void initReadPresence(AMQPInputStream in) throws IOException {
        bitCount = 0;
        flag = in.readShort();
      }

      private boolean readPresence() throws IOException {
        int bit = 15 - bitCount;
        bitCount++;
        return (flag & (1 << bit)) != 0;
      }

      public void finishReadPresence() throws IOException {
        if ((flag & 1) != 0)
          throw new IOException("Unexpected continuation.");
      }

      public void initWritePresence() throws IOException {
        flag = 0;
        bitCount = 0;
      }

      public void writePresence(boolean present) throws IOException {
        if (present) {
          int bit = 15 - bitCount;
          flag = flag | (1 << bit);
        }
        bitCount++;
      }

      public void finishWritePresence(AMQPOutputStream out) throws IOException {
        if (bitCount == 15)
          out.writeShort(flag | 1);
        else
          out.writeShort(flag);
      }

      public void readFrom(AMQPInputStream in) throws IOException {

        initReadPresence(in);
        boolean contentType_present = readPresence();
        boolean contentEncoding_present = readPresence();
        boolean headers_present = readPresence();
        boolean deliveryMode_present = readPresence();
        boolean priority_present = readPresence();
        boolean correlationId_present = readPresence();
        boolean replyTo_present = readPresence();
        boolean expiration_present = readPresence();
        boolean messageId_present = readPresence();
        boolean timestamp_present = readPresence();
        boolean type_present = readPresence();
        boolean userId_present = readPresence();
        boolean appId_present = readPresence();
        boolean clusterId_present = readPresence();
        finishReadPresence();

        if (contentType_present)
          contentType = in.readShortstr();
        if (contentEncoding_present)
          contentEncoding = in.readShortstr();
        if (headers_present)
          headers = in.readTable();
        if (deliveryMode_present)
          deliveryMode = in.readOctet();
        if (priority_present)
          priority = in.readOctet();
        if (correlationId_present)
          correlationId = in.readShortstr();
        if (replyTo_present)
          replyTo = in.readShortstr();
        if (expiration_present)
          expiration = in.readShortstr();
        if (messageId_present)
          messageId = in.readShortstr();
        if (timestamp_present)
          timestamp = in.readTimestamp();
        if (type_present)
          type = in.readShortstr();
        if (userId_present)
          userId = in.readShortstr();
        if (appId_present)
          appId = in.readShortstr();
        if (clusterId_present)
          clusterId = in.readShortstr();
      }

      public void writeTo(AMQPOutputStream out) throws IOException {
        initWritePresence();
        writePresence(this.contentType != null);
        writePresence(this.contentEncoding != null);
        writePresence(this.headers != null);
        writePresence(this.deliveryMode > -1);
        writePresence(this.priority > -1);
        writePresence(this.correlationId != null);
        writePresence(this.replyTo != null);
        writePresence(this.expiration != null);
        writePresence(this.messageId != null);
        writePresence(this.timestamp != null);
        writePresence(this.type != null);
        writePresence(this.userId != null);
        writePresence(this.appId != null);
        writePresence(this.clusterId != null);
        finishWritePresence(out);

        if (this.contentType != null)
          out.writeShortstr(contentType);
        if (this.contentEncoding != null)
          out.writeShortstr(contentEncoding);
        if (this.headers != null)
          out.writeTable(headers);
        if (this.deliveryMode > -1)
          out.writeOctet(deliveryMode);
        if (this.priority > -1)
          out.writeOctet(priority);
        if (this.correlationId != null)
          out.writeShortstr(correlationId);
        if (this.replyTo != null)
          out.writeShortstr(replyTo);
        if (this.expiration != null)
          out.writeShortstr(expiration);
        if (this.messageId != null)
          out.writeShortstr(messageId);
        if (this.timestamp != null)
          out.writeTimestamp(timestamp);
        if (this.type != null)
          out.writeShortstr(type);
        if (this.userId != null)
          out.writeShortstr(userId);
        if (this.appId != null)
          out.writeShortstr(appId);
        if (this.clusterId != null)
          out.writeShortstr(clusterId);
      }

      public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("BasicProperties(");
        buff.append("contentType=");
        buff.append(contentType);
        buff.append(',');
        buff.append("contentEncoding=");
        buff.append(contentEncoding);
        buff.append(',');
        buff.append("headers=");
        buff.append(headers);
        buff.append(',');
        buff.append("deliveryMode=");
        buff.append(deliveryMode);
        buff.append(',');
        buff.append("priority=");
        buff.append(priority);
        buff.append(',');
        buff.append("correlationId=");
        buff.append(correlationId);
        buff.append(',');
        buff.append("replyTo=");
        buff.append(replyTo);
        buff.append(',');
        buff.append("expiration=");
        buff.append(expiration);
        buff.append(',');
        buff.append("messageId=");
        buff.append(messageId);
        buff.append(',');
        buff.append("timestamp=");
        buff.append(timestamp);
        buff.append(',');
        buff.append("type=");
        buff.append(type);
        buff.append(',');
        buff.append("userId=");
        buff.append(userId);
        buff.append(',');
        buff.append("appId=");
        buff.append(appId);
        buff.append(',');
        buff.append("clusterId=");
        buff.append(clusterId);
        buff.append(')');
        return buff.toString();
      }
    }
  
  /**
   * 
   */
    public java.lang.String contentType;
    /**
   * 
   */
    public java.lang.String contentEncoding;
    /**
   * 
   */
    public Map headers;
    /**
   * 
   */
    public int deliveryMode;
    /**
   * 
   */
    public int priority;
    /**
   * 
   */
    public java.lang.String correlationId;
    /**
   * 
   */
    public java.lang.String replyTo;
    /**
   * 
   */
    public java.lang.String expiration;
    /**
   * 
   */
    public java.lang.String messageId;
    /**
   * 
   */
    public Date timestamp;
    /**
   * 
   */
    public java.lang.String type;
    /**
   * 
   */
    public java.lang.String userId;
    /**
   * 
   */
    public java.lang.String appId;
    /**
   * 
   */
    public java.lang.String reserved;

    public final static int INDEX = 60;
      
    public int getClassId() { 
      return INDEX;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }
    
  
    public static final int METHOD_BASIC_QOS = 10;
    public static final int METHOD_BASIC_QOS_OK = 11;
    public static final int METHOD_BASIC_CONSUME = 20;
    public static final int METHOD_BASIC_CONSUME_OK = 21;
    public static final int METHOD_BASIC_CANCEL = 30;
    public static final int METHOD_BASIC_CANCEL_OK = 31;
    public static final int METHOD_BASIC_PUBLISH = 40;
    public static final int METHOD_BASIC_RETURN = 50;
    public static final int METHOD_BASIC_DELIVER = 60;
    public static final int METHOD_BASIC_GET = 70;
    public static final int METHOD_BASIC_GET_OK = 71;
    public static final int METHOD_BASIC_GET_EMPTY = 72;
    public static final int METHOD_BASIC_ACK = 80;
    public static final int METHOD_BASIC_REJECT = 90;
    public static final int METHOD_BASIC_RECOVER_ASYNC = 100;
    public static final int METHOD_BASIC_RECOVER = 110;
    public static final int METHOD_BASIC_RECOVER_OK = 111;
  

    public static final int[] mids = { 10, 11, 20, 21, 30, 31, 40, 50, 60, 70, 71, 72, 80, 90, 100, 110, 111 };
    
    public static final java.lang.String[] methodnames = {
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Qos",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$QosOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Consume",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$ConsumeOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Cancel",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$CancelOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Publish",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Return",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Deliver",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Get",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$GetOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$GetEmpty",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Ack",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Reject",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$RecoverAsync",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Recover",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$RecoverOk" };
    
    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.Basic.mids.length; i++) {
        if (AMQP.Basic.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.Basic.methodnames[pos];
      return "";
    }

    /**
     * This method requests a specific quality of service. The QoS can be
     * specified for the current channel or for all channels on the connection.
     * The particular properties and semantics of a qos method always depend on
     * the content class semantics. Though the qos method could in principle
     * apply to both peers, it is currently meaningful only for the server.
     */
  public static class Qos extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
       * The client can request that messages be sent in advance so that when
       * the client finishes processing a message, the following message is
       * already held locally, rather than needing to be sent down the channel.
       * Prefetching gives a performance improvement. This field specifies the
       * prefetch window size in octets. The server will send a message in
       * advance if it is equal to or smaller in size than the available
       * prefetch size (and also falls into other prefetch limits). May be set
       * to zero, meaning "no specific limit", although other prefetch limits
       * may still apply. The prefetch-size is ignored if the no-ack option is
       * set.
       */
      public int prefetchSize;
      /**
       * Specifies a prefetch window in terms of whole messages. This field may
       * be used in combination with the prefetch-size field; a message will
       * only be sent in advance if both prefetch windows (and those at the
       * channel and connection level) allow it. The prefetch-count is ignored
       * if the no-ack option is set.
       */
      public int prefetchCount;
      /**
       * By default the QoS settings apply to the current channel only. If this
       * field is set, they are applied to the entire connection.
       */
      public boolean global;

      public final static int INDEX = 10;

      /**
       * This method requests a specific quality of service. The QoS can be
       * specified for the current channel or for all channels on the
       * connection. The particular properties and semantics of a qos method
       * always depend on the content class semantics. Though the qos method
       * could in principle apply to both peers, it is currently meaningful only
       * for the server.
       */
    public Qos(int prefetchSize, int prefetchCount, boolean global) {
        this.prefetchSize = prefetchSize;
        this.prefetchCount = prefetchCount;
        this.global = global;
    }
    
    public Qos() {
      }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Qos";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.prefetchSize = in.readLong();
        this.prefetchCount = in.readShort();
        this.global = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeLong(this.prefetchSize);
        out.writeShort(this.prefetchCount);
        out.writeBit(this.global);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Qos(");

        buff.append("prefetchSize=");
        buff.append(prefetchSize);
        buff.append(',');
        buff.append("prefetchCount=");
        buff.append(prefetchCount);
        buff.append(',');
        buff.append("global=");
        buff.append(global);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method tells the client that the requested QoS levels could be
     * handled by the server. The requested QoS applies to all active consumers
     * until a new QoS is defined.
     */
  public static class QosOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 11;

      /**
       * This method tells the client that the requested QoS levels could be
       * handled by the server. The requested QoS applies to all active
       * consumers until a new QoS is defined.
       */
    public QosOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$QosOk";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("QosOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method asks the server to start a "consumer", which is a transient
     * request for messages from a specific queue. Consumers last as long as the
     * channel they were declared on, or until the client cancels them.
     */
    public static class Consume extends AbstractMarshallingMethod {

      private static final long serialVersionUID = 1L;

      /**
   * 
   */
      public int reserved1;
      /**
       * Specifies the name of the queue to consume from.
       */
      public java.lang.String queue;
      /**
       * Specifies the identifier for the consumer. The consumer tag is local to
       * a channel, so two clients can use the same consumer tags. If this field
       * is empty the server will generate a unique tag.
       */
      public java.lang.String consumerTag;
      /**
   * 
   */
      public boolean noLocal;
      /**
   * 
   */
      public boolean noAck;
      /**
       * Request exclusive consumer access, meaning only this consumer can
       * access the queue.
       */
      public boolean exclusive;
      /**
   * 
   */
    public boolean noWait;
      /**
       * A set of arguments for the consume. The syntax and semantics of these
       * arguments depends on the server implementation.
       */
      public Map arguments;

      public final static int INDEX = 20;

      /**
       * This method asks the server to start a "consumer", which is a transient
       * request for messages from a specific queue. Consumers last as long as
       * the channel they were declared on, or until the client cancels them.
       */
    public Consume(int reserved1, java.lang.String queue, java.lang.String consumerTag, boolean noLocal,
          boolean noAck, boolean exclusive, boolean noWait, Map arguments) {
        this.reserved1 = reserved1;
        this.queue = queue;
        this.consumerTag = consumerTag;
        this.noLocal = noLocal;
        this.noAck = noAck;
        this.exclusive = exclusive;
        this.noWait = noWait;
        this.arguments = arguments;
    }
    
    public Consume() {
      }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Consume";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in) throws IOException {
        this.reserved1 = in.readShort();
        this.queue = in.readShortstr();
        this.consumerTag = in.readShortstr();
        this.noLocal = in.readBit();
        this.noAck = in.readBit();
        this.exclusive = in.readBit();
        this.noWait = in.readBit();
        this.arguments = in.readTable();
    }

    public void writeTo(AMQPOutputStream out) throws IOException {
        out.writeShort(this.reserved1);
        out.writeShortstr(this.queue);
        out.writeShortstr(this.consumerTag);
        out.writeBit(this.noLocal);
        out.writeBit(this.noAck);
        out.writeBit(this.exclusive);
        out.writeBit(this.noWait);
        out.writeTable(this.arguments);
      }

      public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Consume(");

        buff.append("reserved1=");
        buff.append(reserved1);
        buff.append(',');
        buff.append("queue=");
        buff.append(queue);
        buff.append(',');
        buff.append("consumerTag=");
        buff.append(consumerTag);
        buff.append(',');
        buff.append("noLocal=");
        buff.append(noLocal);
        buff.append(',');
        buff.append("noAck=");
        buff.append(noAck);
        buff.append(',');
        buff.append("exclusive=");
        buff.append(exclusive);
        buff.append(',');
        buff.append("noWait=");
        buff.append(noWait);
        buff.append(',');
        buff.append("arguments=");
        buff.append(arguments);

        buff.append(')');
        return buff.toString();
    }
  }

    /**
     * The server provides the client with a consumer tag, which is used by the
     * client for methods called on the consumer at a later stage.
     */
  public static class ConsumeOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
       * Holds the consumer tag specified by the client or provided by the
       * server.
       */
      public java.lang.String consumerTag;

      public final static int INDEX = 21;

      /**
       * The server provides the client with a consumer tag, which is used by
       * the client for methods called on the consumer at a later stage.
       */
    public ConsumeOk(java.lang.String consumerTag) {
        this.consumerTag = consumerTag;
    }
    
    public ConsumeOk() {
      }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$ConsumeOk";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.consumerTag = in.readShortstr();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShortstr(this.consumerTag);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("ConsumeOk(");

        buff.append("consumerTag=");
        buff.append(consumerTag);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method cancels a consumer. This does not affect already delivered
     * messages, but it does mean the server will not send any more messages for
     * that consumer. The client may receive an arbitrary number of messages in
     * between sending the cancel method and receiving the cancel-ok reply.
     */
  public static class Cancel extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
   * 
   */
      public java.lang.String consumerTag;
      /**
   * 
   */
      public boolean noWait;

      public final static int INDEX = 30;

      /**
       * This method cancels a consumer. This does not affect already delivered
       * messages, but it does mean the server will not send any more messages
       * for that consumer. The client may receive an arbitrary number of
       * messages in between sending the cancel method and receiving the
       * cancel-ok reply.
       */
    public Cancel(java.lang.String consumerTag, boolean noWait) {
        this.consumerTag = consumerTag;
        this.noWait = noWait;
    }
    
    public Cancel() {
      }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Cancel";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.consumerTag = in.readShortstr();
        this.noWait = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShortstr(this.consumerTag);
        out.writeBit(this.noWait);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Cancel(");

        buff.append("consumerTag=");
        buff.append(consumerTag);
        buff.append(',');
        buff.append("noWait=");
        buff.append(noWait);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method confirms that the cancellation was completed.
     */
  public static class CancelOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
   * 
   */
    public java.lang.String consumerTag;

      public final static int INDEX = 31;

      /**
       * This method confirms that the cancellation was completed.
       */
    public CancelOk(java.lang.String consumerTag) {
        this.consumerTag = consumerTag;
    }
    
    public CancelOk() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$CancelOk";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.consumerTag = in.readShortstr();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShortstr(this.consumerTag);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("CancelOk(");
      
      buff.append("consumerTag=");
        buff.append(consumerTag);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
     * This method publishes a message to a specific exchange. The message will
     * be routed to queues as defined by the exchange configuration and
     * distributed to any active consumers when the transaction, if any, is
     * committed.
     */
    public static class Publish extends AbstractMarshallingMethod {

      private static final long serialVersionUID = 1L;

      /**
   * 
   */
      public int reserved1;
      /**
       * Specifies the name of the exchange to publish to. The exchange name can
       * be empty, meaning the default exchange. If the exchange name is
       * specified, and that exchange does not exist, the server will raise a
       * channel exception.
       */
      public java.lang.String exchange;
      /**
       * Specifies the routing key for the message. The routing key is used for
       * routing messages depending on the exchange configuration.
       */
      public java.lang.String routingKey;
      /**
       * This flag tells the server how to react if the message cannot be routed
       * to a queue. If this flag is set, the server will return an unroutable
       * message with a Return method. If this flag is zero, the server silently
       * drops the message.
       */
      public boolean mandatory;
      /**
       * This flag tells the server how to react if the message cannot be routed
       * to a queue consumer immediately. If this flag is set, the server will
       * return an undeliverable message with a Return method. If this flag is
       * zero, the server will queue the message, but with no guarantee that it
       * will ever be consumed.
       */
    public boolean immediate;

      public final static int INDEX = 40;

      /**
       * This method publishes a message to a specific exchange. The message
       * will be routed to queues as defined by the exchange configuration and
       * distributed to any active consumers when the transaction, if any, is
       * committed.
       */
    public Publish(int reserved1, java.lang.String exchange, java.lang.String routingKey,
          boolean mandatory, boolean immediate) {
        this.reserved1 = reserved1;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.mandatory = mandatory;
        this.immediate = immediate;
    }
    
    public Publish() {
      }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Publish";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.reserved1 = in.readShort();
        this.exchange = in.readShortstr();
        this.routingKey = in.readShortstr();
        this.mandatory = in.readBit();
        this.immediate = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.reserved1);
        out.writeShortstr(this.exchange);
        out.writeShortstr(this.routingKey);
        out.writeBit(this.mandatory);
        out.writeBit(this.immediate);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Publish(");

        buff.append("reserved1=");
        buff.append(reserved1);
        buff.append(',');
        buff.append("exchange=");
        buff.append(exchange);
        buff.append(',');
        buff.append("routingKey=");
        buff.append(routingKey);
        buff.append(',');
        buff.append("mandatory=");
        buff.append(mandatory);
        buff.append(',');
        buff.append("immediate=");
        buff.append(immediate);
      
      buff.append(')');
      return buff.toString();
      }
  }

  /**
     * This method returns an undeliverable message that was published with the
     * "immediate" flag set, or an unroutable message published with the
     * "mandatory" flag set. The reply code and text provide information about
     * the reason that the message was undeliverable.
     */
    public static class Return extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int replyCode;
  /**
   * 
   */
    public java.lang.String replyText;
      /**
       * Specifies the name of the exchange that the message was originally
       * published to. May be empty, meaning the default exchange.
       */
    public java.lang.String exchange;
      /**
       * Specifies the routing key name specified when the message was
       * published.
       */
    public java.lang.String routingKey;

      public final static int INDEX = 50;

      /**
       * This method returns an undeliverable message that was published with
       * the "immediate" flag set, or an unroutable message published with the
       * "mandatory" flag set. The reply code and text provide information about
       * the reason that the message was undeliverable.
       */
    public Return(int replyCode, java.lang.String replyText, java.lang.String exchange,
          java.lang.String routingKey) {
        this.replyCode = replyCode;
        this.replyText = replyText;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }
    
    public Return() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Return";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.replyCode = in.readShort();
        this.replyText = in.readShortstr();
        this.exchange = in.readShortstr();
        this.routingKey = in.readShortstr();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShort(this.replyCode);
        out.writeShortstr(this.replyText);
        out.writeShortstr(this.exchange);
        out.writeShortstr(this.routingKey);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Return(");
      
      buff.append("replyCode=");
        buff.append(replyCode);
        buff.append(',');
        buff.append("replyText=");
        buff.append(replyText);
        buff.append(',');
        buff.append("exchange=");
        buff.append(exchange);
        buff.append(',');
        buff.append("routingKey=");
        buff.append(routingKey);

        buff.append(')');
        return buff.toString();
      }
  }

  /**
     * This method delivers a message to the client, via a consumer. In the
     * asynchronous message delivery model, the client starts a consumer using
     * the Consume method, then the server responds with Deliver methods as and
     * when messages arrive for that consumer.
     */
    public static class Deliver extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
      public java.lang.String consumerTag;
      /**
   * 
   */
      public long deliveryTag;
      /**
   * 
   */
      public boolean redelivered;
      /**
       * Specifies the name of the exchange that the message was originally
       * published to. May be empty, indicating the default exchange.
       */
      public java.lang.String exchange;
      /**
       * Specifies the routing key name specified when the message was
       * published.
       */
      public java.lang.String routingKey;

      public final static int INDEX = 60;

      /**
       * This method delivers a message to the client, via a consumer. In the
       * asynchronous message delivery model, the client starts a consumer using
       * the Consume method, then the server responds with Deliver methods as
       * and when messages arrive for that consumer.
       */
      public Deliver(java.lang.String consumerTag, long deliveryTag, boolean redelivered,
          java.lang.String exchange, java.lang.String routingKey) {
        this.consumerTag = consumerTag;
        this.deliveryTag = deliveryTag;
        this.redelivered = redelivered;
        this.exchange = exchange;
        this.routingKey = routingKey;
      }

      public Deliver() {
      }

      public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
        return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Deliver";

      }
    
    public int getClassId() {
        return 60;
      }

      public java.lang.String getClassName() {
        return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
      }

      public void readFrom(AMQPInputStream in) throws IOException {
        this.consumerTag = in.readShortstr();
        this.deliveryTag = in.readLonglong();
        this.redelivered = in.readBit();
        this.exchange = in.readShortstr();
        this.routingKey = in.readShortstr();
    }

    public void writeTo(AMQPOutputStream out) throws IOException {
        out.writeShortstr(this.consumerTag);
        out.writeLonglong(this.deliveryTag);
        out.writeBit(this.redelivered);
        out.writeShortstr(this.exchange);
        out.writeShortstr(this.routingKey);
      }

      public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Deliver(");

        buff.append("consumerTag=");
        buff.append(consumerTag);
        buff.append(',');
        buff.append("deliveryTag=");
        buff.append(deliveryTag);
        buff.append(',');
        buff.append("redelivered=");
        buff.append(redelivered);
        buff.append(',');
        buff.append("exchange=");
        buff.append(exchange);
        buff.append(',');
        buff.append("routingKey=");
        buff.append(routingKey);

        buff.append(')');
        return buff.toString();
    }
  }

    /**
     * This method provides a direct access to the messages in a queue using a
     * synchronous dialogue that is designed for specific types of application
     * where synchronous functionality is more important than performance.
     */
  public static class Get extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int reserved1;
      /**
       * Specifies the name of the queue to get a message from.
       */
    public java.lang.String queue;
  /**
   * 
   */
    public boolean noAck;

      public final static int INDEX = 70;

      /**
       * This method provides a direct access to the messages in a queue using a
       * synchronous dialogue that is designed for specific types of application
       * where synchronous functionality is more important than performance.
       */
      public Get(int reserved1, java.lang.String queue, boolean noAck) {
        this.reserved1 = reserved1;
        this.queue = queue;
        this.noAck = noAck;
      }

      public Get() {
      }

      public int getMethodId() {
        return INDEX;
      }

      public java.lang.String getMethodName() {
        return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Get";

      }

      public int getClassId() {
        return 60;
      }

      public java.lang.String getClassName() {
        return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
      }

      public void readFrom(AMQPInputStream in) throws IOException {
        this.reserved1 = in.readShort();
        this.queue = in.readShortstr();
        this.noAck = in.readBit();
      }

      public void writeTo(AMQPOutputStream out) throws IOException {
        out.writeShort(this.reserved1);
        out.writeShortstr(this.queue);
        out.writeBit(this.noAck);
      }

      public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Get(");

        buff.append("reserved1=");
        buff.append(reserved1);
        buff.append(',');
        buff.append("queue=");
        buff.append(queue);
        buff.append(',');
        buff.append("noAck=");
        buff.append(noAck);

        buff.append(')');
        return buff.toString();
      }
    }

    /**
     * This method delivers a message to the client following a get method. A
     * message delivered by 'get-ok' must be acknowledged unless the no-ack
     * option was set in the get method.
     */
  public static class GetOk extends AbstractMarshallingMethod {

      private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public long deliveryTag;
  /**
   * 
   */
    public boolean redelivered;
      /**
       * Specifies the name of the exchange that the message was originally
       * published to. If empty, the message was published to the default
       * exchange.
       */
    public java.lang.String exchange;
      /**
       * Specifies the routing key name specified when the message was
       * published.
       */
    public java.lang.String routingKey;
  /**
   * 
   */
    public int messageCount;

      public final static int INDEX = 71;

      /**
       * This method delivers a message to the client following a get method. A
       * message delivered by 'get-ok' must be acknowledged unless the no-ack
       * option was set in the get method.
       */
    public GetOk(long deliveryTag, boolean redelivered, java.lang.String exchange,
          java.lang.String routingKey, int messageCount) {
        this.deliveryTag = deliveryTag;
      this.redelivered = redelivered;
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.messageCount = messageCount;
    }
    
    public GetOk() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$GetOk";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.deliveryTag = in.readLonglong();
        this.redelivered = in.readBit();
        this.exchange = in.readShortstr();
        this.routingKey = in.readShortstr();
        this.messageCount = in.readLong();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeLonglong(this.deliveryTag);
        out.writeBit(this.redelivered);
        out.writeShortstr(this.exchange);
        out.writeShortstr(this.routingKey);
        out.writeLong(this.messageCount);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("GetOk(");
      
      buff.append("deliveryTag=");
        buff.append(deliveryTag);
      buff.append(',');
      buff.append("redelivered=");
      buff.append(redelivered);
      buff.append(',');
      buff.append("exchange=");
      buff.append(exchange);
      buff.append(',');
      buff.append("routingKey=");
      buff.append(routingKey);
      buff.append(',');
      buff.append("messageCount=");
        buff.append(messageCount);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method tells the client that the queue has no messages available for
     * the client.
     */
  public static class GetEmpty extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public java.lang.String reserved1;

      public final static int INDEX = 72;

      /**
       * This method tells the client that the queue has no messages available
       * for the client.
       */
    public GetEmpty(java.lang.String reserved1) {
        this.reserved1 = reserved1;
    }
    
    public GetEmpty() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$GetEmpty";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.reserved1 = in.readShortstr();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeShortstr(this.reserved1);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("GetEmpty(");
      
      buff.append("reserved1=");
        buff.append(reserved1);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method acknowledges one or more messages delivered via the Deliver
     * or Get-Ok methods. The client can ask to confirm a single message or a
     * set of messages up to and including a specific message.
     */
  public static class Ack extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public long deliveryTag;
      /**
       * If set to 1, the delivery tag is treated as "up to and including", so
       * that the client can acknowledge multiple messages with a single method.
       * If set to zero, the delivery tag refers to a single message. If the
       * multiple field is 1, and the delivery tag is zero, tells the server to
       * acknowledge all outstanding messages.
       */
      public boolean multiple;

      public final static int INDEX = 80;

      /**
       * This method acknowledges one or more messages delivered via the Deliver
       * or Get-Ok methods. The client can ask to confirm a single message or a
       * set of messages up to and including a specific message.
       */
    public Ack(long deliveryTag, boolean multiple) {
        this.deliveryTag = deliveryTag;
        this.multiple = multiple;
    }
    
    public Ack() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Ack";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.deliveryTag = in.readLonglong();
        this.multiple = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeLonglong(this.deliveryTag);
        out.writeBit(this.multiple);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Ack(");
      
      buff.append("deliveryTag=");
        buff.append(deliveryTag);
        buff.append(',');
        buff.append("multiple=");
        buff.append(multiple);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method allows a client to reject a message. It can be used to
     * interrupt and cancel large incoming messages, or return untreatable
     * messages to their original queue.
     */
  public static class Reject extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public long deliveryTag;
      /**
       * If requeue is true, the server will attempt to requeue the message. If
       * requeue is false or the requeue attempt fails the messages are
       * discarded or dead-lettered.
       */
    public boolean requeue;

      public final static int INDEX = 90;

      /**
       * This method allows a client to reject a message. It can be used to
       * interrupt and cancel large incoming messages, or return untreatable
       * messages to their original queue.
       */
    public Reject(long deliveryTag, boolean requeue) {
        this.deliveryTag = deliveryTag;
        this.requeue = requeue;
    }
    
    public Reject() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Reject";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.deliveryTag = in.readLonglong();
        this.requeue = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeLonglong(this.deliveryTag);
        out.writeBit(this.requeue);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Reject(");
      
      buff.append("deliveryTag=");
        buff.append(deliveryTag);
      buff.append(',');
      buff.append("requeue=");
        buff.append(requeue);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method asks the server to redeliver all unacknowledged messages on a
     * specified channel. Zero or more messages may be redelivered. This method
     * is deprecated in favour of the synchronous Recover/Recover-Ok.
     */
  public static class RecoverAsync extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
  /**
   * If this field is zero, the message will be redelivered to the original recipient. If this bit is 1, the server will attempt to requeue the message, potentially then delivering it to an alternative subscriber.
   */
    public boolean requeue;
      
    public final static int INDEX = 100;

      /**
       * This method asks the server to redeliver all unacknowledged messages on
       * a specified channel. Zero or more messages may be redelivered. This
       * method is deprecated in favour of the synchronous Recover/Recover-Ok.
       */
    public RecoverAsync(boolean requeue) {
      this.requeue = requeue;
    }
    
    public RecoverAsync() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$RecoverAsync";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.requeue = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeBit(this.requeue);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("RecoverAsync(");
      
      buff.append("requeue=");
      buff.append(requeue);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method asks the server to redeliver all unacknowledged messages on a
     * specified channel. Zero or more messages may be redelivered. This method
     * replaces the asynchronous Recover.
     */
  public static class Recover extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;

      /**
       * If this field is zero, the message will be redelivered to the original
       * recipient. If this bit is 1, the server will attempt to requeue the
       * message, potentially then delivering it to an alternative subscriber.
       */
    public boolean requeue;

      public final static int INDEX = 110;

      /**
       * This method asks the server to redeliver all unacknowledged messages on
       * a specified channel. Zero or more messages may be redelivered. This
       * method replaces the asynchronous Recover.
       */
    public Recover(boolean requeue) {
        this.requeue = requeue;
    }
    
    public Recover() {
    }
      
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Recover";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
      this.requeue = in.readBit();
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
      out.writeBit(this.requeue);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Recover(");
      
      buff.append("requeue=");
        buff.append(requeue);
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method acknowledges a Basic.Recover method.
     */
  public static class RecoverOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 111;

      /**
       * This method acknowledges a Basic.Recover method.
       */
    public RecoverOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$RecoverOk";
      
    }
    
    public int getClassId() { 
      return 60;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("RecoverOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  
  public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Basic(");

      buff.append("contentType=");
      buff.append(contentType);
      buff.append(',');
      buff.append("contentEncoding=");
      buff.append(contentEncoding);
      buff.append(',');
      buff.append("headers=");
      buff.append(headers);
      buff.append(',');
      buff.append("deliveryMode=");
      buff.append(deliveryMode);
      buff.append(',');
      buff.append("priority=");
      buff.append(priority);
      buff.append(',');
      buff.append("correlationId=");
      buff.append(correlationId);
      buff.append(',');
      buff.append("replyTo=");
      buff.append(replyTo);
      buff.append(',');
      buff.append("expiration=");
      buff.append(expiration);
      buff.append(',');
      buff.append("messageId=");
      buff.append(messageId);
      buff.append(',');
      buff.append("timestamp=");
      buff.append(timestamp);
      buff.append(',');
      buff.append("type=");
      buff.append(type);
      buff.append(',');
      buff.append("userId=");
      buff.append(userId);
      buff.append(',');
      buff.append("appId=");
      buff.append(appId);
      buff.append(',');
      buff.append("reserved=");
      buff.append(reserved);
    
    buff.append(')');
      return buff.toString();
    }
  }

  public static class Tx extends AbstractMarshallingClass {

    private static final long serialVersionUID = 1L;

  
      
    public final static int INDEX = 90;
      
    public int getClassId() { 
      return INDEX;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx";
    }
    
  
    public static final int METHOD_TX_SELECT = 10;
    public static final int METHOD_TX_SELECT_OK = 11;
    public static final int METHOD_TX_COMMIT = 20;
    public static final int METHOD_TX_COMMIT_OK = 21;
    public static final int METHOD_TX_ROLLBACK = 30;
    public static final int METHOD_TX_ROLLBACK_OK = 31;
  

    public static final int[] mids = { 10, 11, 20, 21, 30, 31 };
    
    public static final java.lang.String[] methodnames = {
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$Select",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$SelectOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$Commit",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$CommitOk",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$Rollback",
        "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$RollbackOk" };
    
    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.Tx.mids.length; i++) {
        if (AMQP.Tx.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.Tx.methodnames[pos];
      return "";
    }

    /**
     * This method sets the channel to use standard transactions. The client
     * must use this method at least once on a channel before using the Commit
     * or Rollback methods.
     */
  public static class Select extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 10;

      /**
       * This method sets the channel to use standard transactions. The client
       * must use this method at least once on a channel before using the Commit
       * or Rollback methods.
       */
    public Select() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$Select";
      
    }
    
    public int getClassId() { 
      return 90;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Select(");
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method confirms to the client that the channel was successfully set
     * to use standard transactions.
     */
  public static class SelectOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 11;

      /**
       * This method confirms to the client that the channel was successfully
       * set to use standard transactions.
       */
    public SelectOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$SelectOk";
      
    }
    
    public int getClassId() { 
      return 90;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("SelectOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method commits all message publications and acknowledgments
     * performed in the current transaction. A new transaction starts
     * immediately after a commit.
     */
  public static class Commit extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 20;

      /**
       * This method commits all message publications and acknowledgments
       * performed in the current transaction. A new transaction starts
       * immediately after a commit.
       */
    public Commit() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$Commit";
      
    }
    
    public int getClassId() { 
      return 90;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Commit(");
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method confirms to the client that the commit succeeded. Note that
     * if a commit fails, the server raises a channel exception.
     */
  public static class CommitOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 21;

      /**
       * This method confirms to the client that the commit succeeded. Note that
       * if a commit fails, the server raises a channel exception.
       */
    public CommitOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$CommitOk";
      
    }
    
    public int getClassId() { 
      return 90;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("CommitOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method abandons all message publications and acknowledgments
     * performed in the current transaction. A new transaction starts
     * immediately after a rollback. Note that unacked messages will not be
     * automatically redelivered by rollback; if that is required an explicit
     * recover call should be issued.
     */
  public static class Rollback extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 30;

      /**
       * This method abandons all message publications and acknowledgments
       * performed in the current transaction. A new transaction starts
       * immediately after a rollback. Note that unacked messages will not be
       * automatically redelivered by rollback; if that is required an explicit
       * recover call should be issued.
       */
    public Rollback() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$Rollback";
      
    }
    
    public int getClassId() { 
      return 90;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Rollback(");
      
      buff.append(')');
      return buff.toString();
    }
  }

    /**
     * This method confirms to the client that the rollback succeeded. Note that
     * if an rollback fails, the server raises a channel exception.
     */
  public static class RollbackOk extends AbstractMarshallingMethod {
  
    private static final long serialVersionUID = 1L;
  
      
    public final static int INDEX = 31;

      /**
       * This method confirms to the client that the rollback succeeded. Note
       * that if an rollback fails, the server raises a channel exception.
       */
    public RollbackOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$RollbackOk";
      
    }
    
    public int getClassId() { 
      return 90;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx";
    }

    public void readFrom(AMQPInputStream in)
      throws IOException {
    }

    public void writeTo(AMQPOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("RollbackOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Tx(");
    
    buff.append(')');
    return buff.toString();
  }
 }

}
