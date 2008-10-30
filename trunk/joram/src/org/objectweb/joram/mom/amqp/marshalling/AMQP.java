/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007-2008 ScalAgent Distributed Technologies
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
   * The file class provides methods that support reliable file transfer. File messages have a specific set of properties that are required for interoperability with file transfer applications. File messages and acknowledgements are subject to channel transactions. Note that the file class does not provide message browsing methods; these are not compatible with the staging model. Applications that need browsable file transfer should use Basic content and the Basic class.
   */
  public static final int CLASS_FILE = 70;

  /**
   * The stream class provides methods that support multimedia streaming. The stream class uses the following semantics: one message is one packet of data; delivery is unacknowledged and unreliable; the consumer can specify quality of service parameters that the server can try to adhere to; lower-priority messages may be discarded in favour of high priority messages.
   */
  public static final int CLASS_STREAM = 80;

  /**
   * Standard transactions provide so-called "1.5 phase commit". We can ensure that work is never lost, but there is a chance of confirmations being lost, so that messages may be resent. Applications that use standard transactions must be able to detect and ignore duplicate messages.
   */
  public static final int CLASS_TX = 90;

  /**
   * Distributed transactions provide so-called "2-phase commit". The AMQP distributed transaction model supports the X-Open XA architecture and other distributed transaction implementations. The Dtx class assumes that the server has a private communications channel (not AMQP) to a distributed transaction coordinator.
   */
  public static final int CLASS_DTX = 100;

  /**
   * The tunnel methods are used to send blocks of binary data - which can be serialised AMQP methods or other protocol frames - between AMQP peers.
   */
  public static final int CLASS_TUNNEL = 110;

  /**
   * [WORK IN PROGRESS] The message class provides methods that support an industry-standard messaging model.
   */
  public static final int CLASS_MESSAGE = 120;


  public static final int[] ids = {10,20,30,40,50,60,70,80,90,100,110,120};
  
  public static final java.lang.String[] classnames = {
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Access",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Queue",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$File",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tunnel",
    "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message"
  };
  
  public static final int FRAME_METHOD = 1;
  public static final int FRAME_HEADER = 2;
  public static final int FRAME_BODY = 3;
  public static final int FRAME_OOB_METHOD = 4;
  public static final int FRAME_OOB_HEADER = 5;
  public static final int FRAME_OOB_BODY = 6;
  public static final int FRAME_TRACE = 7;
  public static final int FRAME_HEARTBEAT = 8;
  public static final int FRAME_MIN_SIZE = 4096;
  public static final int FRAME_END = 206;
  public static final int REPLY_SUCCESS = 200;
  public static final int NOT_DELIVERED = 310;
  public static final int CONTENT_TOO_LARGE = 311;
  public static final int NO_ROUTE = 312;
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
  public static final int RESOURCE_ERROR = 506;
  public static final int NOT_ALLOWED = 530;
  public static final int NOT_IMPLEMENTED = 540;
  public static final int INTERNAL_ERROR = 541;

  public static class Connection
    extends AbstractMarshallingClass {
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
    public static final int METHOD_CONNECTION_REDIRECT = 42;
    public static final int METHOD_CONNECTION_CLOSE = 50;
    public static final int METHOD_CONNECTION_CLOSE_OK = 51;
  

    public static final int[] mids = {10,11,20,21,30,31,40,41,42,50,51};
    
    public static final java.lang.String[] methodnames = {
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Start",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$StartOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Secure",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$SecureOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Tune",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$TuneOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Open",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$OpenOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Redirect",
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
  public static class Start
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The protocol version, major component, as transmitted in the AMQP protocol header. This, combined with the protocol minor component fully describe the protocol version, which is written in the format major-minor. Hence, with major=1, minor=3, the protocol version would be "1-3".
   */
    public int versionMajor;
  /**
   * The protocol version, minor component, as transmitted in the AMQP protocol header. This, combined with the protocol major component fully describe the protocol version, which is written in the format major-minor. Hence, with major=1, minor=3, the protocol version would be "1-3".
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.versionMajor = AMQPStreamUtil.readOctet(in);
      this.versionMinor = AMQPStreamUtil.readOctet(in);
      this.serverProperties = AMQPStreamUtil.readTable(in);
      this.mechanisms = AMQPStreamUtil.readLongstr(in);
      this.locales = AMQPStreamUtil.readLongstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeOctet(this.versionMajor, out);
      AMQPStreamUtil.writeOctet(this.versionMinor, out);
      AMQPStreamUtil.writeTable(this.serverProperties, out);
      AMQPStreamUtil.writeLongstr(this.mechanisms, out);
      AMQPStreamUtil.writeLongstr(this.locales, out);
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
  public static class StartOk
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.clientProperties = AMQPStreamUtil.readTable(in);
      this.mechanism = AMQPStreamUtil.readShortstr(in);
      this.response = AMQPStreamUtil.readLongstr(in);
      this.locale = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeTable(this.clientProperties, out);
      AMQPStreamUtil.writeShortstr(this.mechanism, out);
      AMQPStreamUtil.writeLongstr(this.response, out);
      AMQPStreamUtil.writeShortstr(this.locale, out);
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
  public static class Secure
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.challenge = AMQPStreamUtil.readLongstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLongstr(this.challenge, out);
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
  public static class SecureOk
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.response = AMQPStreamUtil.readLongstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLongstr(this.response, out);
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
  public static class Tune
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The maximum total number of channels that the server allows per connection. Zero means that the server does not impose a fixed limit, but the number of allowed channels may be limited by available server resources.
   */
    public int channelMax;
  /**
   * The largest frame size that the server proposes for the connection. The client can negotiate a lower value. Zero means that the server does not impose any specific limit but may reject very large frames if it cannot allocate resources for them.
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.channelMax = AMQPStreamUtil.readShort(in);
      this.frameMax = AMQPStreamUtil.readLong(in);
      this.heartbeat = AMQPStreamUtil.readShort(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.channelMax, out);
      AMQPStreamUtil.writeLong(this.frameMax, out);
      AMQPStreamUtil.writeShort(this.heartbeat, out);
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
  public static class TuneOk
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.channelMax = AMQPStreamUtil.readShort(in);
      this.frameMax = AMQPStreamUtil.readLong(in);
      this.heartbeat = AMQPStreamUtil.readShort(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.channelMax, out);
      AMQPStreamUtil.writeLong(this.frameMax, out);
      AMQPStreamUtil.writeShort(this.heartbeat, out);
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
  public static class Open
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The name of the virtual host to work with.
   */
    public java.lang.String virtualHost;
  /**
   * The client can specify zero or more capability names, delimited by spaces. The server can use this string to how to process the client's connection request.
   */
    public java.lang.String capabilities;
  /**
   * In a configuration with multiple collaborating servers, the server may respond to a Connection.Open method with a Connection.Redirect. The insist option tells the server that the client is insisting on a connection to the specified server.
   */
    public boolean insist;
	  
	  public final static int INDEX = 40;
	  
	  
  /**
   * This method opens a connection to a virtual host, which is a collection of resources, and acts to separate multiple application domains within a server. The server may apply arbitrary limits per virtual host, such as the number of each type of entity that may be used, per connection and/or in total.
   */
    public Open(java.lang.String virtualHost,java.lang.String capabilities,boolean insist) {
      this.virtualHost = virtualHost;
      this.capabilities = capabilities;
      this.insist = insist;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.virtualHost = AMQPStreamUtil.readShortstr(in);
      this.capabilities = AMQPStreamUtil.readShortstr(in);
      this.insist = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.virtualHost, out);
      AMQPStreamUtil.writeShortstr(this.capabilities, out);
      AMQPStreamUtil.writeBit(this.insist, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Open(");
      
      buff.append("virtualHost=");
      buff.append(virtualHost);
      buff.append(',');
      buff.append("capabilities=");
      buff.append(capabilities);
      buff.append(',');
      buff.append("insist=");
      buff.append(insist);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method signals to the client that the connection is ready for use.
   */
  public static class OpenOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public java.lang.String knownHosts;
	  
	  public final static int INDEX = 41;
	  
	  
  /**
   * This method signals to the client that the connection is ready for use.
   */
    public OpenOk(java.lang.String knownHosts) {
      this.knownHosts = knownHosts;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.knownHosts = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.knownHosts, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("OpenOk(");
      
      buff.append("knownHosts=");
      buff.append(knownHosts);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method redirects the client to another server, based on the requested virtual host and/or capabilities.
   */
  public static class Redirect
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * Specifies the server to connect to. This is an IP address or a DNS name, optionally followed by a colon and a port number. If no port number is specified, the client should use the default port number for the protocol.
   */
    public java.lang.String host;
  /**
   * 
   */
    public java.lang.String knownHosts;
	  
	  public final static int INDEX = 42;
	  
	  
  /**
   * This method redirects the client to another server, based on the requested virtual host and/or capabilities.
   */
    public Redirect(java.lang.String host,java.lang.String knownHosts) {
      this.host = host;
      this.knownHosts = knownHosts;
    }
    
    public Redirect() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Redirect";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.host = AMQPStreamUtil.readShortstr(in);
      this.knownHosts = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.host, out);
      AMQPStreamUtil.writeShortstr(this.knownHosts, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Redirect(");
      
      buff.append("host=");
      buff.append(host);
      buff.append(',');
      buff.append("knownHosts=");
      buff.append(knownHosts);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method indicates that the sender wants to close the connection. This may be due to internal conditions (e.g. a forced shut-down) or due to an error handling a specific method, i.e. an exception. When a close is due to an exception, the sender provides the class and method id of the method which caused the exception.
   */
  public static class Close
    extends AbstractMarshallingMethod {
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
	  
	  public final static int INDEX = 50;
	  
	  
  /**
   * This method indicates that the sender wants to close the connection. This may be due to internal conditions (e.g. a forced shut-down) or due to an error handling a specific method, i.e. an exception. When a close is due to an exception, the sender provides the class and method id of the method which caused the exception.
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
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection$Close";
      
    }
    
    public int getClassId() { 
      return 10;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Connection";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.replyCode = AMQPStreamUtil.readShort(in);
      this.replyText = AMQPStreamUtil.readShortstr(in);
      this.classId = AMQPStreamUtil.readShort(in);
      this.methodId = AMQPStreamUtil.readShort(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.replyCode, out);
      AMQPStreamUtil.writeShortstr(this.replyText, out);
      AMQPStreamUtil.writeShort(this.classId, out);
      AMQPStreamUtil.writeShort(this.methodId, out);
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
   * This method confirms a Connection.Close method and tells the recipient that it is safe to release resources for the connection and close the socket.
   */
  public static class CloseOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 51;
	  
	  
  /**
   * This method confirms a Connection.Close method and tells the recipient that it is safe to release resources for the connection and close the socket.
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("CloseOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }



  public void readFrom(DataInputStream in)
    throws IOException {
    
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Connection(");
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Channel
    extends AbstractMarshallingClass {
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
    public static final int METHOD_CHANNEL_RESUME = 50;
    public static final int METHOD_CHANNEL_PING = 60;
    public static final int METHOD_CHANNEL_PONG = 70;
    public static final int METHOD_CHANNEL_OK = 80;
  

    public static final int[] mids = {10,11,20,21,40,41,50,60,70,80};
    
    public static final java.lang.String[] methodnames = {
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Open",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$OpenOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Flow",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$FlowOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Close",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$CloseOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Resume",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Ping",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Pong",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Ok"
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
  public static class Open
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * Configures out-of-band transfers on this channel. The syntax and meaning of this field will be formally defined at a later date.
   */
    public java.lang.String outOfBand;
	  
	  public final static int INDEX = 10;
	  
	  
  /**
   * This method opens a channel to the server.
   */
    public Open(java.lang.String outOfBand) {
      this.outOfBand = outOfBand;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.outOfBand = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.outOfBand, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Open(");
      
      buff.append("outOfBand=");
      buff.append(outOfBand);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method signals to the client that the channel is ready for use.
   */
  public static class OpenOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public LongString channelId;
	  
	  public final static int INDEX = 11;
	  
	  
  /**
   * This method signals to the client that the channel is ready for use.
   */
    public OpenOk(LongString channelId) {
      this.channelId = channelId;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.channelId = AMQPStreamUtil.readLongstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLongstr(this.channelId, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("OpenOk(");
      
      buff.append("channelId=");
      buff.append(channelId);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method asks the peer to pause or restart the flow of content data. This is a simple flow-control mechanism that a peer can use to avoid overflowing its queues or otherwise finding itself receiving more messages than it can process. Note that this method is not intended for window control. The peer that receives a disable flow method should finish sending the current content frame, if any, then pause.
   */
  public static class Flow
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * If 1, the peer starts sending content frames. If 0, the peer stops sending content frames.
   */
    public boolean active;
	  
	  public final static int INDEX = 20;
	  
	  
  /**
   * This method asks the peer to pause or restart the flow of content data. This is a simple flow-control mechanism that a peer can use to avoid overflowing its queues or otherwise finding itself receiving more messages than it can process. Note that this method is not intended for window control. The peer that receives a disable flow method should finish sending the current content frame, if any, then pause.
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.active = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeBit(this.active, out);
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
  public static class FlowOk
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.active = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeBit(this.active, out);
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
  public static class Close
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.replyCode = AMQPStreamUtil.readShort(in);
      this.replyText = AMQPStreamUtil.readShortstr(in);
      this.classId = AMQPStreamUtil.readShort(in);
      this.methodId = AMQPStreamUtil.readShort(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.replyCode, out);
      AMQPStreamUtil.writeShortstr(this.replyText, out);
      AMQPStreamUtil.writeShort(this.classId, out);
      AMQPStreamUtil.writeShort(this.methodId, out);
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
  public static class CloseOk
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("CloseOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method resume a previously interrupted channel.
   */
  public static class Resume
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public LongString channelId;
	  
	  public final static int INDEX = 50;
	  
	  
  /**
   * This method resume a previously interrupted channel.
   */
    public Resume(LongString channelId) {
      this.channelId = channelId;
    }
    
    public Resume() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Resume";
      
    }
    
    public int getClassId() { 
      return 20;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.channelId = AMQPStreamUtil.readLongstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLongstr(this.channelId, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Resume(");
      
      buff.append("channelId=");
      buff.append(channelId);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] Request that the recipient issue a pong request.
   */
  public static class Ping
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 60;
	  
	  
  /**
   * [WORK IN PROGRESS] Request that the recipient issue a pong request.
   */
    public Ping() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Ping";
      
    }
    
    public int getClassId() { 
      return 20;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Ping(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] Issued after a ping request is received. Note that this is a request issued after receiving a ping, not a response to receiving a ping.
   */
  public static class Pong
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 70;
	  
	  
  /**
   * [WORK IN PROGRESS] Issued after a ping request is received. Note that this is a request issued after receiving a ping, not a response to receiving a ping.
   */
    public Pong() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Pong";
      
    }
    
    public int getClassId() { 
      return 20;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Pong(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] Signals normal completion of a method.
   */
  public static class Ok
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 80;
	  
	  
  /**
   * [WORK IN PROGRESS] Signals normal completion of a method.
   */
    public Ok() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel$Ok";
      
    }
    
    public int getClassId() { 
      return 20;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Channel";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Ok(");
      
      buff.append(')');
      return buff.toString();
    }
  }



  public void readFrom(DataInputStream in)
    throws IOException {
    
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Channel(");
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Access
    extends AbstractMarshallingClass {
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
  

    public static final int[] mids = {10,11};
    
    public static final java.lang.String[] methodnames = {
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Access$Request",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Access$RequestOk"
    };
    
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
   * This method requests an access ticket for an access realm. The server responds by granting the access ticket. If the client does not have access rights to the requested realm this causes a connection exception. Access tickets are a per-channel resource.
   */
  public static class Request
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * Specifies the name of the realm to which the client is requesting access. The realm is a configured server-side object that collects a set of resources (exchanges, queues, etc.). If the channel has already requested an access ticket onto this realm, the previous ticket is destroyed and a new ticket is created with the requested access rights, if allowed.
   */
    public java.lang.String realm;
  /**
   * Request exclusive access to the realm, meaning that this will be the only channel that uses the realm's resources.
   */
    public boolean exclusive;
  /**
   * Request message passive access to the specified access realm. Passive access lets a client get information about resources in the realm but not to make any changes to them.
   */
    public boolean passive;
  /**
   * Request message active access to the specified access realm. Active access lets a client get create and delete resources in the realm.
   */
    public boolean active;
  /**
   * Request write access to the specified access realm. Write access lets a client publish messages to all exchanges in the realm.
   */
    public boolean write;
  /**
   * Request read access to the specified access realm. Read access lets a client consume messages from queues in the realm.
   */
    public boolean read;
	  
	  public final static int INDEX = 10;
	  
	  
  /**
   * This method requests an access ticket for an access realm. The server responds by granting the access ticket. If the client does not have access rights to the requested realm this causes a connection exception. Access tickets are a per-channel resource.
   */
    public Request(java.lang.String realm,boolean exclusive,boolean passive,boolean active,boolean write,boolean read) {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.realm = AMQPStreamUtil.readShortstr(in);
      this.exclusive = AMQPStreamUtil.readBit(in);
      this.passive = AMQPStreamUtil.readBit(in);
      this.active = AMQPStreamUtil.readBit(in);
      this.write = AMQPStreamUtil.readBit(in);
      this.read = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.realm, out);
      AMQPStreamUtil.writeBit(this.exclusive, out);
      AMQPStreamUtil.writeBit(this.passive, out);
      AMQPStreamUtil.writeBit(this.active, out);
      AMQPStreamUtil.writeBit(this.write, out);
      AMQPStreamUtil.writeBit(this.read, out);
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
   * This method provides the client with an access ticket. The access ticket is valid within the current channel and for the lifespan of the channel.
   */
  public static class RequestOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
	  
	  public final static int INDEX = 11;
	  
	  
  /**
   * This method provides the client with an access ticket. The access ticket is valid within the current channel and for the lifespan of the channel.
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
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



  public void readFrom(DataInputStream in)
    throws IOException {
    
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Access(");
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Exchange
    extends AbstractMarshallingClass {
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
  

    public static final int[] mids = {10,11,20,21};
    
    public static final java.lang.String[] methodnames = {
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$Declare",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$DeclareOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$Delete",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Exchange$DeleteOk"
    };
    
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
   * This method creates an exchange if it does not already exist, and if the exchange exists, verifies that it is of the correct and expected class.
   */
  public static class Declare
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * When a client defines a new exchange, this belongs to the access realm of the ticket used. All further work done with that exchange must be done with an access ticket for the same realm.
   */
    public int ticket;
  /**
   * 
   */
    public java.lang.String exchange;
  /**
   * Each exchange belongs to one of a set of exchange types implemented by the server. The exchange types define the functionality of the exchange - i.e. how messages are routed through it. It is not valid or meaningful to attempt to change the type of an existing exchange.
   */
    public java.lang.String type;
  /**
   * If set, the server will not create the exchange. The client can use this to check whether an exchange exists without modifying the server state.
   */
    public boolean passive;
  /**
   * If set when creating a new exchange, the exchange will be marked as durable. Durable exchanges remain active when a server restarts. Non-durable exchanges (transient exchanges) are purged if/when a server restarts.
   */
    public boolean durable;
  /**
   * If set, the exchange is deleted when all queues have finished using it.
   */
    public boolean autoDelete;
  /**
   * If set, the exchange may not be used directly by publishers, but only when bound to other exchanges. Internal exchanges are used to construct wiring that is not visible to applications.
   */
    public boolean internal;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
  /**
   * A set of arguments for the declaration. The syntax and semantics of these arguments depends on the server implementation. This field is ignored if passive is 1.
   */
    public Map arguments;
	  
	  public final static int INDEX = 10;
	  
	  
  /**
   * This method creates an exchange if it does not already exist, and if the exchange exists, verifies that it is of the correct and expected class.
   */
    public Declare(int ticket,java.lang.String exchange,java.lang.String type,boolean passive,boolean durable,boolean autoDelete,boolean internal,boolean nowait,Map arguments) {
      this.ticket = ticket;
      this.exchange = exchange;
      this.type = type;
      this.passive = passive;
      this.durable = durable;
      this.autoDelete = autoDelete;
      this.internal = internal;
      this.nowait = nowait;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.type = AMQPStreamUtil.readShortstr(in);
      this.passive = AMQPStreamUtil.readBit(in);
      this.durable = AMQPStreamUtil.readBit(in);
      this.autoDelete = AMQPStreamUtil.readBit(in);
      this.internal = AMQPStreamUtil.readBit(in);
      this.nowait = AMQPStreamUtil.readBit(in);
      this.arguments = AMQPStreamUtil.readTable(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.type, out);
      AMQPStreamUtil.writeBit(this.passive, out);
      AMQPStreamUtil.writeBit(this.durable, out);
      AMQPStreamUtil.writeBit(this.autoDelete, out);
      AMQPStreamUtil.writeBit(this.internal, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
      AMQPStreamUtil.writeTable(this.arguments, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Declare(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
      buff.append("autoDelete=");
      buff.append(autoDelete);
      buff.append(',');
      buff.append("internal=");
      buff.append(internal);
      buff.append(',');
      buff.append("nowait=");
      buff.append(nowait);
      buff.append(',');
      buff.append("arguments=");
      buff.append(arguments);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method confirms a Declare method and confirms the name of the exchange, essential for automatically-named exchanges.
   */
  public static class DeclareOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 11;
	  
	  
  /**
   * This method confirms a Declare method and confirms the name of the exchange, essential for automatically-named exchanges.
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method deletes an exchange. When an exchange is deleted all queue bindings on the exchange are cancelled.
   */
  public static class Delete
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
  /**
   * 
   */
    public java.lang.String exchange;
  /**
   * If set, the server will only delete the exchange if it has no queue bindings. If the exchange has queue bindings the server does not delete it but raises a channel exception instead.
   */
    public boolean ifUnused;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
	  
	  public final static int INDEX = 20;
	  
	  
  /**
   * This method deletes an exchange. When an exchange is deleted all queue bindings on the exchange are cancelled.
   */
    public Delete(int ticket,java.lang.String exchange,boolean ifUnused,boolean nowait) {
      this.ticket = ticket;
      this.exchange = exchange;
      this.ifUnused = ifUnused;
      this.nowait = nowait;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.ifUnused = AMQPStreamUtil.readBit(in);
      this.nowait = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeBit(this.ifUnused, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Delete(");
      
      buff.append("ticket=");
      buff.append(ticket);
      buff.append(',');
      buff.append("exchange=");
      buff.append(exchange);
      buff.append(',');
      buff.append("ifUnused=");
      buff.append(ifUnused);
      buff.append(',');
      buff.append("nowait=");
      buff.append(nowait);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method confirms the deletion of an exchange.
   */
  public static class DeleteOk
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("DeleteOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }



  public void readFrom(DataInputStream in)
    throws IOException {
    
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Exchange(");
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Queue
    extends AbstractMarshallingClass {
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
  

    public static final int[] mids = {10,11,20,21,50,51,30,31,40,41};
    
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
   * This method creates or checks a queue. When creating a new queue the client can specify various properties that control the durability of the queue and its contents, and the level of sharing for the queue.
   */
  public static class Declare
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * When a client defines a new queue, this belongs to the access realm of the ticket used. All further work done with that queue must be done with an access ticket for the same realm.
   */
    public int ticket;
  /**
   * 
   */
    public java.lang.String queue;
  /**
   * If set, the server will not create the queue. This field allows the client to assert the presence of a queue without modifying the server state.
   */
    public boolean passive;
  /**
   * If set when creating a new queue, the queue will be marked as durable. Durable queues remain active when a server restarts. Non-durable queues (transient queues) are purged if/when a server restarts. Note that durable queues do not necessarily hold persistent messages, although it does not make sense to send persistent messages to a transient queue.
   */
    public boolean durable;
  /**
   * Exclusive queues may only be consumed from by the current connection. Setting the 'exclusive' flag always implies 'auto-delete'.
   */
    public boolean exclusive;
  /**
   * If set, the queue is deleted when all consumers have finished using it. Last consumer can be cancelled either explicitly or because its channel is closed. If there was no consumer ever on the queue, it won't be deleted.
   */
    public boolean autoDelete;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
  /**
   * A set of arguments for the declaration. The syntax and semantics of these arguments depends on the server implementation. This field is ignored if passive is 1.
   */
    public Map arguments;
	  
	  public final static int INDEX = 10;
	  
	  
  /**
   * This method creates or checks a queue. When creating a new queue the client can specify various properties that control the durability of the queue and its contents, and the level of sharing for the queue.
   */
    public Declare(int ticket,java.lang.String queue,boolean passive,boolean durable,boolean exclusive,boolean autoDelete,boolean nowait,Map arguments) {
      this.ticket = ticket;
      this.queue = queue;
      this.passive = passive;
      this.durable = durable;
      this.exclusive = exclusive;
      this.autoDelete = autoDelete;
      this.nowait = nowait;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.passive = AMQPStreamUtil.readBit(in);
      this.durable = AMQPStreamUtil.readBit(in);
      this.exclusive = AMQPStreamUtil.readBit(in);
      this.autoDelete = AMQPStreamUtil.readBit(in);
      this.nowait = AMQPStreamUtil.readBit(in);
      this.arguments = AMQPStreamUtil.readTable(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeBit(this.passive, out);
      AMQPStreamUtil.writeBit(this.durable, out);
      AMQPStreamUtil.writeBit(this.exclusive, out);
      AMQPStreamUtil.writeBit(this.autoDelete, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
      AMQPStreamUtil.writeTable(this.arguments, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Declare(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
      buff.append("nowait=");
      buff.append(nowait);
      buff.append(',');
      buff.append("arguments=");
      buff.append(arguments);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method confirms a Declare method and confirms the name of the queue, essential for automatically-named queues.
   */
  public static class DeclareOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * Reports the name of the queue. If the server generated a queue name, this field contains that name.
   */
    public java.lang.String queue;
  /**
   * Reports the number of messages in the queue, which will be zero for newly-created queues.
   */
    public int messageCount;
  /**
   * Reports the number of active consumers for the queue. Note that consumers can suspend activity (Channel.Flow) in which case they do not appear in this count.
   */
    public int consumerCount;
	  
	  public final static int INDEX = 11;
	  
	  
  /**
   * This method confirms a Declare method and confirms the name of the queue, essential for automatically-named queues.
   */
    public DeclareOk(java.lang.String queue,int messageCount,int consumerCount) {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.messageCount = AMQPStreamUtil.readLong(in);
      this.consumerCount = AMQPStreamUtil.readLong(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeLong(this.messageCount, out);
      AMQPStreamUtil.writeLong(this.consumerCount, out);
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
   * This method binds a queue to an exchange. Until a queue is bound it will not receive any messages. In a classic messaging model, store-and-forward queues are bound to a direct exchange and subscription queues are bound to a topic exchange.
   */
  public static class Bind
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The client provides a valid access ticket giving "active" access rights to the queue's access realm.
   */
    public int ticket;
  /**
   * Specifies the name of the queue to bind. If the queue name is empty, refers to the current queue for the channel, which is the last declared queue.
   */
    public java.lang.String queue;
  /**
   * 
   */
    public java.lang.String exchange;
  /**
   * Specifies the routing key for the binding. The routing key is used for routing messages depending on the exchange configuration. Not all exchanges use a routing key - refer to the specific exchange documentation. If the queue name is empty, the server uses the last queue declared on the channel. If the routing key is also empty, the server uses this queue name for the routing key as well. If the queue name is provided but the routing key is empty, the server does the binding with that empty routing key. The meaning of empty routing keys depends on the exchange implementation.
   */
    public java.lang.String routingKey;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
  /**
   * A set of arguments for the binding. The syntax and semantics of these arguments depends on the exchange class.
   */
    public Map arguments;
	  
	  public final static int INDEX = 20;
	  
	  
  /**
   * This method binds a queue to an exchange. Until a queue is bound it will not receive any messages. In a classic messaging model, store-and-forward queues are bound to a direct exchange and subscription queues are bound to a topic exchange.
   */
    public Bind(int ticket,java.lang.String queue,java.lang.String exchange,java.lang.String routingKey,boolean nowait,Map arguments) {
      this.ticket = ticket;
      this.queue = queue;
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.nowait = nowait;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
      this.nowait = AMQPStreamUtil.readBit(in);
      this.arguments = AMQPStreamUtil.readTable(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
      AMQPStreamUtil.writeTable(this.arguments, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Bind(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
      buff.append("nowait=");
      buff.append(nowait);
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
  public static class BindOk
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
  public static class Unbind
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The client provides a valid access ticket giving "active" access rights to the queue's access realm.
   */
    public int ticket;
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
    public Unbind(int ticket,java.lang.String queue,java.lang.String exchange,java.lang.String routingKey,Map arguments) {
      this.ticket = ticket;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
      this.arguments = AMQPStreamUtil.readTable(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
      AMQPStreamUtil.writeTable(this.arguments, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Unbind(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
  public static class UnbindOk
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method removes all messages from a queue. It does not cancel consumers. Purged messages are deleted without any formal "undo" mechanism.
   */
  public static class Purge
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The access ticket must be for the access realm that holds the queue.
   */
    public int ticket;
  /**
   * Specifies the name of the queue to purge. If the queue name is empty, refers to the current queue for the channel, which is the last declared queue.
   */
    public java.lang.String queue;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
	  
	  public final static int INDEX = 30;
	  
	  
  /**
   * This method removes all messages from a queue. It does not cancel consumers. Purged messages are deleted without any formal "undo" mechanism.
   */
    public Purge(int ticket,java.lang.String queue,boolean nowait) {
      this.ticket = ticket;
      this.queue = queue;
      this.nowait = nowait;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.nowait = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Purge(");
      
      buff.append("ticket=");
      buff.append(ticket);
      buff.append(',');
      buff.append("queue=");
      buff.append(queue);
      buff.append(',');
      buff.append("nowait=");
      buff.append(nowait);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method confirms the purge of a queue.
   */
  public static class PurgeOk
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.messageCount = AMQPStreamUtil.readLong(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLong(this.messageCount, out);
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
   * This method deletes a queue. When a queue is deleted any pending messages are sent to a dead-letter queue if this is defined in the server configuration, and all consumers on the queue are cancelled.
   */
  public static class Delete
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The client provides a valid access ticket giving "active" access rights to the queue's access realm.
   */
    public int ticket;
  /**
   * Specifies the name of the queue to delete. If the queue name is empty, refers to the current queue for the channel, which is the last declared queue.
   */
    public java.lang.String queue;
  /**
   * If set, the server will only delete the queue if it has no consumers. If the queue has consumers the server does does not delete it but raises a channel exception instead.
   */
    public boolean ifUnused;
  /**
   * If set, the server will only delete the queue if it has no messages.
   */
    public boolean ifEmpty;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
	  
	  public final static int INDEX = 40;
	  
	  
  /**
   * This method deletes a queue. When a queue is deleted any pending messages are sent to a dead-letter queue if this is defined in the server configuration, and all consumers on the queue are cancelled.
   */
    public Delete(int ticket,java.lang.String queue,boolean ifUnused,boolean ifEmpty,boolean nowait) {
      this.ticket = ticket;
      this.queue = queue;
      this.ifUnused = ifUnused;
      this.ifEmpty = ifEmpty;
      this.nowait = nowait;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.ifUnused = AMQPStreamUtil.readBit(in);
      this.ifEmpty = AMQPStreamUtil.readBit(in);
      this.nowait = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeBit(this.ifUnused, out);
      AMQPStreamUtil.writeBit(this.ifEmpty, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Delete(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
      buff.append("nowait=");
      buff.append(nowait);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method confirms the deletion of a queue.
   */
  public static class DeleteOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * Reports the number of messages purged.
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.messageCount = AMQPStreamUtil.readLong(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLong(this.messageCount, out);
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



  public void readFrom(DataInputStream in)
    throws IOException {
    
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Queue(");
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Basic
    extends AbstractMarshallingClass {
    private static final long serialVersionUID = 1L;
	  
    public BasicProperties basicProps = null;
    
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
    public static final int METHOD_BASIC_RECOVER = 100;
  

    public static final int[] mids = {10,11,20,21,30,31,40,50,60,70,71,72,80,90,100};
    
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
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Basic$Recover"
    };
    
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
   * This method requests a specific quality of service. The QoS can be specified for the current channel or for all channels on the connection. The particular properties and semantics of a qos method always depend on the content class semantics. Though the qos method could in principle apply to both peers, it is currently meaningful only for the server.
   */
  public static class Qos
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The client can request that messages be sent in advance so that when the client finishes processing a message, the following message is already held locally, rather than needing to be sent down the channel. Prefetching gives a performance improvement. This field specifies the prefetch window size in octets. The server will send a message in advance if it is equal to or smaller in size than the available prefetch size (and also falls into other prefetch limits). May be set to zero, meaning "no specific limit", although other prefetch limits may still apply. The prefetch-size is ignored if the no-ack option is set.
   */
    public int prefetchSize;
  /**
   * Specifies a prefetch window in terms of whole messages. This field may be used in combination with the prefetch-size field; a message will only be sent in advance if both prefetch windows (and those at the channel and connection level) allow it. The prefetch-count is ignored if the no-ack option is set.
   */
    public int prefetchCount;
  /**
   * By default the QoS settings apply to the current channel only. If this field is set, they are applied to the entire connection.
   */
    public boolean global;
	  
	  public final static int INDEX = 10;
	  
	  
  /**
   * This method requests a specific quality of service. The QoS can be specified for the current channel or for all channels on the connection. The particular properties and semantics of a qos method always depend on the content class semantics. Though the qos method could in principle apply to both peers, it is currently meaningful only for the server.
   */
    public Qos(int prefetchSize,int prefetchCount,boolean global) {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.prefetchSize = AMQPStreamUtil.readLong(in);
      this.prefetchCount = AMQPStreamUtil.readShort(in);
      this.global = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLong(this.prefetchSize, out);
      AMQPStreamUtil.writeShort(this.prefetchCount, out);
      AMQPStreamUtil.writeBit(this.global, out);
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
   * This method tells the client that the requested QoS levels could be handled by the server. The requested QoS applies to all active consumers until a new QoS is defined.
   */
  public static class QosOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 11;
	  
	  
  /**
   * This method tells the client that the requested QoS levels could be handled by the server. The requested QoS applies to all active consumers until a new QoS is defined.
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method asks the server to start a "consumer", which is a transient request for messages from a specific queue. Consumers last as long as the channel they were created on, or until the client cancels them.
   */
  public static class Consume
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
  /**
   * Specifies the name of the queue to consume from. If the queue name is null, refers to the current queue for the channel, which is the last declared queue.
   */
    public java.lang.String queue;
  /**
   * Specifies the identifier for the consumer. The consumer tag is local to a connection, so two clients can use the same consumer tags. If this field is empty the server will generate a unique tag.
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
   * Request exclusive consumer access, meaning only this consumer can access the queue.
   */
    public boolean exclusive;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
  /**
   * A set of filters for the consume. The syntax and semantics of these filters depends on the providers implementation.
   */
    public Map filter;
	  
	  public final static int INDEX = 20;
	  
	  
  /**
   * This method asks the server to start a "consumer", which is a transient request for messages from a specific queue. Consumers last as long as the channel they were created on, or until the client cancels them.
   */
    public Consume(int ticket,java.lang.String queue,java.lang.String consumerTag,boolean noLocal,boolean noAck,boolean exclusive,boolean nowait,Map filter) {
      this.ticket = ticket;
      this.queue = queue;
      this.consumerTag = consumerTag;
      this.noLocal = noLocal;
      this.noAck = noAck;
      this.exclusive = exclusive;
      this.nowait = nowait;
      this.filter = filter;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
      this.noLocal = AMQPStreamUtil.readBit(in);
      this.noAck = AMQPStreamUtil.readBit(in);
      this.exclusive = AMQPStreamUtil.readBit(in);
      this.nowait = AMQPStreamUtil.readBit(in);
      this.filter = AMQPStreamUtil.readTable(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
      AMQPStreamUtil.writeBit(this.noLocal, out);
      AMQPStreamUtil.writeBit(this.noAck, out);
      AMQPStreamUtil.writeBit(this.exclusive, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
      AMQPStreamUtil.writeTable(this.filter, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Consume(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
      buff.append("nowait=");
      buff.append(nowait);
      buff.append(',');
      buff.append("filter=");
      buff.append(filter);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * The server provides the client with a consumer tag, which is used by the client for methods called on the consumer at a later stage.
   */
  public static class ConsumeOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * Holds the consumer tag specified by the client or provided by the server.
   */
    public java.lang.String consumerTag;
	  
	  public final static int INDEX = 21;
	  
	  
  /**
   * The server provides the client with a consumer tag, which is used by the client for methods called on the consumer at a later stage.
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
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
   * This method cancels a consumer. This does not affect already delivered messages, but it does mean the server will not send any more messages for that consumer. The client may receive an arbitrary number of messages in between sending the cancel method and receiving the cancel-ok reply.
   */
  public static class Cancel
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public java.lang.String consumerTag;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
	  
	  public final static int INDEX = 30;
	  
	  
  /**
   * This method cancels a consumer. This does not affect already delivered messages, but it does mean the server will not send any more messages for that consumer. The client may receive an arbitrary number of messages in between sending the cancel method and receiving the cancel-ok reply.
   */
    public Cancel(java.lang.String consumerTag,boolean nowait) {
      this.consumerTag = consumerTag;
      this.nowait = nowait;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
      this.nowait = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Cancel(");
      
      buff.append("consumerTag=");
      buff.append(consumerTag);
      buff.append(',');
      buff.append("nowait=");
      buff.append(nowait);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method confirms that the cancellation was completed.
   */
  public static class CancelOk
    extends AbstractMarshallingMethod {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
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
   * This method publishes a message to a specific exchange. The message will be routed to queues as defined by the exchange configuration and distributed to any active consumers when the transaction, if any, is committed.
   */
  public static class Publish
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
  /**
   * Specifies the name of the exchange to publish to. The exchange name can be empty, meaning the default exchange. If the exchange name is specified, and that exchange does not exist, the server will raise a channel exception.
   */
    public java.lang.String exchange;
  /**
   * Specifies the routing key for the message. The routing key is used for routing messages depending on the exchange configuration.
   */
    public java.lang.String routingKey;
  /**
   * This flag tells the server how to react if the message cannot be routed to a queue. If this flag is set, the server will return an unroutable message with a Return method. If this flag is zero, the server silently drops the message.
   */
    public boolean mandatory;
  /**
   * This flag tells the server how to react if the message cannot be routed to a queue consumer immediately. If this flag is set, the server will return an undeliverable message with a Return method. If this flag is zero, the server will queue the message, but with no guarantee that it will ever be consumed.
   */
    public boolean immediate;
	  
	  public final static int INDEX = 40;
	  
	  
  /**
   * This method publishes a message to a specific exchange. The message will be routed to queues as defined by the exchange configuration and distributed to any active consumers when the transaction, if any, is committed.
   */
    public Publish(int ticket,java.lang.String exchange,java.lang.String routingKey,boolean mandatory,boolean immediate) {
      this.ticket = ticket;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
      this.mandatory = AMQPStreamUtil.readBit(in);
      this.immediate = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
      AMQPStreamUtil.writeBit(this.mandatory, out);
      AMQPStreamUtil.writeBit(this.immediate, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Publish(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
   * This method returns an undeliverable message that was published with the "immediate" flag set, or an unroutable message published with the "mandatory" flag set. The reply code and text provide information about the reason that the message was undeliverable.
   */
  public static class Return
    extends AbstractMarshallingMethod {
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
   * Specifies the name of the exchange that the message was originally published to.
   */
    public java.lang.String exchange;
  /**
   * Specifies the routing key name specified when the message was published.
   */
    public java.lang.String routingKey;
	  
	  public final static int INDEX = 50;
	  
	  
  /**
   * This method returns an undeliverable message that was published with the "immediate" flag set, or an unroutable message published with the "mandatory" flag set. The reply code and text provide information about the reason that the message was undeliverable.
   */
    public Return(int replyCode,java.lang.String replyText,java.lang.String exchange,java.lang.String routingKey) {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.replyCode = AMQPStreamUtil.readShort(in);
      this.replyText = AMQPStreamUtil.readShortstr(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.replyCode, out);
      AMQPStreamUtil.writeShortstr(this.replyText, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
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
   * This method delivers a message to the client, via a consumer. In the asynchronous message delivery model, the client starts a consumer using the Consume method, then the server responds with Deliver methods as and when messages arrive for that consumer.
   */
  public static class Deliver
    extends AbstractMarshallingMethod {
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
   * Specifies the name of the exchange that the message was originally published to.
   */
    public java.lang.String exchange;
  /**
   * Specifies the routing key name specified when the message was published.
   */
    public java.lang.String routingKey;
	  
	  public final static int INDEX = 60;
	  
	  
  /**
   * This method delivers a message to the client, via a consumer. In the asynchronous message delivery model, the client starts a consumer using the Consume method, then the server responds with Deliver methods as and when messages arrive for that consumer.
   */
    public Deliver(java.lang.String consumerTag,long deliveryTag,boolean redelivered,java.lang.String exchange,java.lang.String routingKey) {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
      this.deliveryTag = AMQPStreamUtil.readLonglong(in);
      this.redelivered = AMQPStreamUtil.readBit(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
      AMQPStreamUtil.writeLonglong(this.deliveryTag, out);
      AMQPStreamUtil.writeBit(this.redelivered, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
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
   * This method provides a direct access to the messages in a queue using a synchronous dialogue that is designed for specific types of application where synchronous functionality is more important than performance.
   */
  public static class Get
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
  /**
   * Specifies the name of the queue to consume from. If the queue name is null, refers to the current queue for the channel, which is the last declared queue.
   */
    public java.lang.String queue;
  /**
   * 
   */
    public boolean noAck;
	  
	  public final static int INDEX = 70;
	  
	  
  /**
   * This method provides a direct access to the messages in a queue using a synchronous dialogue that is designed for specific types of application where synchronous functionality is more important than performance.
   */
    public Get(int ticket,java.lang.String queue,boolean noAck) {
      this.ticket = ticket;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.noAck = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeBit(this.noAck, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Get(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
   * This method delivers a message to the client following a get method. A message delivered by 'get-ok' must be acknowledged unless the no-ack option was set in the get method.
   */
  public static class GetOk
    extends AbstractMarshallingMethod {
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
   * Specifies the name of the exchange that the message was originally published to. If empty, the message was published to the default exchange.
   */
    public java.lang.String exchange;
  /**
   * Specifies the routing key name specified when the message was published.
   */
    public java.lang.String routingKey;
  /**
   * This field reports the number of messages pending on the queue, excluding the message being delivered. Note that this figure is indicative, not reliable, and can change arbitrarily as messages are added to the queue and removed by other clients.
   */
    public int messageCount;
	  
	  public final static int INDEX = 71;
	  
	  
  /**
   * This method delivers a message to the client following a get method. A message delivered by 'get-ok' must be acknowledged unless the no-ack option was set in the get method.
   */
    public GetOk(long deliveryTag,boolean redelivered,java.lang.String exchange,java.lang.String routingKey,int messageCount) {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.deliveryTag = AMQPStreamUtil.readLonglong(in);
      this.redelivered = AMQPStreamUtil.readBit(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
      this.messageCount = AMQPStreamUtil.readLong(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLonglong(this.deliveryTag, out);
      AMQPStreamUtil.writeBit(this.redelivered, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
      AMQPStreamUtil.writeLong(this.messageCount, out);
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
   * This method tells the client that the queue has no messages available for the client.
   */
  public static class GetEmpty
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * For use by cluster applications, should not be used by client applications.
   */
    public java.lang.String clusterId;
	  
	  public final static int INDEX = 72;
	  
	  
  /**
   * This method tells the client that the queue has no messages available for the client.
   */
    public GetEmpty(java.lang.String clusterId) {
      this.clusterId = clusterId;
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.clusterId = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.clusterId, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("GetEmpty(");
      
      buff.append("clusterId=");
      buff.append(clusterId);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method acknowledges one or more messages delivered via the Deliver or Get-Ok methods. The client can ask to confirm a single message or a set of messages up to and including a specific message.
   */
  public static class Ack
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public long deliveryTag;
  /**
   * If set to 1, the delivery tag is treated as "up to and including", so that the client can acknowledge multiple messages with a single method. If set to zero, the delivery tag refers to a single message. If the multiple field is 1, and the delivery tag is zero, tells the server to acknowledge all outstanding messages.
   */
    public boolean multiple;
	  
	  public final static int INDEX = 80;
	  
	  
  /**
   * This method acknowledges one or more messages delivered via the Deliver or Get-Ok methods. The client can ask to confirm a single message or a set of messages up to and including a specific message.
   */
    public Ack(long deliveryTag,boolean multiple) {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.deliveryTag = AMQPStreamUtil.readLonglong(in);
      this.multiple = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLonglong(this.deliveryTag, out);
      AMQPStreamUtil.writeBit(this.multiple, out);
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
   * This method allows a client to reject a message. It can be used to interrupt and cancel large incoming messages, or return untreatable messages to their original queue.
   */
  public static class Reject
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public long deliveryTag;
  /**
   * If this field is zero, the message will be discarded. If this bit is 1, the server will attempt to requeue the message.
   */
    public boolean requeue;
	  
	  public final static int INDEX = 90;
	  
	  
  /**
   * This method allows a client to reject a message. It can be used to interrupt and cancel large incoming messages, or return untreatable messages to their original queue.
   */
    public Reject(long deliveryTag,boolean requeue) {
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.deliveryTag = AMQPStreamUtil.readLonglong(in);
      this.requeue = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLonglong(this.deliveryTag, out);
      AMQPStreamUtil.writeBit(this.requeue, out);
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
   * This method asks the broker to redeliver all unacknowledged messages on a specified channel. Zero or more messages may be redelivered. This method is only allowed on non-transacted channels.
   */
  public static class Recover
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * If this field is zero, the message will be redelivered to the original recipient. If this bit is 1, the server will attempt to requeue the message, potentially then delivering it to an alternative subscriber.
   */
    public boolean requeue;
	  
	  public final static int INDEX = 100;
	  
	  
  /**
   * This method asks the broker to redeliver all unacknowledged messages on a specified channel. Zero or more messages may be redelivered. This method is only allowed on non-transacted channels.
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

    public void readFrom(DataInputStream in)
      throws IOException {
      this.requeue = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeBit(this.requeue, out);
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

  public void readFrom(DataInputStream in)
    throws IOException {
//    basicProps = new BasicProperties();
//    basicProps.readFrom(in);
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
//    basicProps.writeTo(out);
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Basic(");
    buff.append(basicProps);
    buff.append(')');
    return buff.toString();
  }
  
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
    
    private void initReadPresence(DataInputStream in) throws IOException {
      bitCount = 0;
      flag = AMQPStreamUtil.readShort(in);
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

    public void finishWritePresence(DataOutputStream out) throws IOException {
      if (bitCount == 15) 
        AMQPStreamUtil.writeShort(flag | 1, out);
      else
        AMQPStreamUtil.writeShort(flag, out);
    }

    public void readFrom(DataInputStream in)
    throws IOException {
      
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
       contentType = AMQPStreamUtil.readShortstr(in);
     if (contentEncoding_present)
      contentEncoding = AMQPStreamUtil.readShortstr(in);
     if (headers_present)
      headers = AMQPStreamUtil.readTable(in);
     if (deliveryMode_present)
      deliveryMode = AMQPStreamUtil.readOctet(in);
     if (priority_present)
      priority = AMQPStreamUtil.readOctet(in);
     if (correlationId_present)
      correlationId = AMQPStreamUtil.readShortstr(in);
     if (replyTo_present)
      replyTo = AMQPStreamUtil.readShortstr(in);
     if (expiration_present)
      expiration = AMQPStreamUtil.readShortstr(in);
     if (messageId_present)
      messageId = AMQPStreamUtil.readShortstr(in);
     if (timestamp_present)
      timestamp = AMQPStreamUtil.readTimestamp(in);
     if (type_present)
      type = AMQPStreamUtil.readShortstr(in);
     if (userId_present)
      userId = AMQPStreamUtil.readShortstr(in);
     if (appId_present)
      appId = AMQPStreamUtil.readShortstr(in);
     if (clusterId_present)
      clusterId = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
    throws IOException {
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
      
      if(this.contentType != null) AMQPStreamUtil.writeShortstr(contentType, out);
      if(this.contentEncoding != null) AMQPStreamUtil.writeShortstr(contentEncoding, out);
      if(this.headers != null) AMQPStreamUtil.writeTable(headers, out);
      if(this.deliveryMode > -1) AMQPStreamUtil.writeOctet(deliveryMode, out);
      if(this.priority > -1) AMQPStreamUtil.writeOctet(priority, out);
      if(this.correlationId != null) AMQPStreamUtil.writeShortstr(correlationId, out);
      if(this.replyTo != null) AMQPStreamUtil.writeShortstr(replyTo, out);
      if(this.expiration != null) AMQPStreamUtil.writeShortstr(expiration, out);
      if(this.messageId != null) AMQPStreamUtil.writeShortstr(messageId, out);
      if(this.timestamp != null) AMQPStreamUtil.writeTimestamp(timestamp, out);
      if(this.type != null) AMQPStreamUtil.writeShortstr(type, out);
      if(this.userId != null) AMQPStreamUtil.writeShortstr(userId, out);
      if(this.appId != null) AMQPStreamUtil.writeShortstr(appId, out);
      if(this.clusterId != null) AMQPStreamUtil.writeShortstr(clusterId, out);
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
 }

  public static class File
    extends AbstractMarshallingClass {
    private static final long serialVersionUID = 1L;

  
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
    public int priority;
  /**
   * 
   */
    public java.lang.String replyTo;
  /**
   * 
   */
    public java.lang.String messageId;
  /**
   * 
   */
    public java.lang.String filename;
  /**
   * 
   */
    public Date timestamp;
  /**
   * 
   */
    public java.lang.String clusterId;
	  
	  public final static int INDEX = 70;
	  
    public int getClassId() { 
      return INDEX;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }
    
  
    public static final int METHOD_FILE_QOS = 10;
    public static final int METHOD_FILE_QOS_OK = 11;
    public static final int METHOD_FILE_CONSUME = 20;
    public static final int METHOD_FILE_CONSUME_OK = 21;
    public static final int METHOD_FILE_CANCEL = 30;
    public static final int METHOD_FILE_CANCEL_OK = 31;
    public static final int METHOD_FILE_OPEN = 40;
    public static final int METHOD_FILE_OPEN_OK = 41;
    public static final int METHOD_FILE_STAGE = 50;
    public static final int METHOD_FILE_PUBLISH = 60;
    public static final int METHOD_FILE_RETURN = 70;
    public static final int METHOD_FILE_DELIVER = 80;
    public static final int METHOD_FILE_ACK = 90;
    public static final int METHOD_FILE_REJECT = 100;
  

    public static final int[] mids = {10,11,20,21,30,31,40,41,50,60,70,80,90,100};
    
    public static final java.lang.String[] methodnames = {
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Qos",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$QosOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Consume",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$ConsumeOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Cancel",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$CancelOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Open",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$OpenOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Stage",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Publish",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Return",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Deliver",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Ack",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Reject"
    };
    
    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.File.mids.length; i++) {
        if (AMQP.File.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.File.methodnames[pos];  
      return "";
    }
  


  /**
   * This method requests a specific quality of service. The QoS can be specified for the current channel or for all channels on the connection. The particular properties and semantics of a qos method always depend on the content class semantics. Though the qos method could in principle apply to both peers, it is currently meaningful only for the server.
   */
  public static class Qos
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The client can request that messages be sent in advance so that when the client finishes processing a message, the following message is already held locally, rather than needing to be sent down the channel. Prefetching gives a performance improvement. This field specifies the prefetch window size in octets. May be set to zero, meaning "no specific limit". Note that other prefetch limits may still apply. The prefetch-size is ignored if the no-ack option is set.
   */
    public int prefetchSize;
  /**
   * Specifies a prefetch window in terms of whole messages. This is compatible with some file API implementations. This field may be used in combination with the prefetch-size field; a message will only be sent in advance if both prefetch windows (and those at the channel and connection level) allow it. The prefetch-count is ignored if the no-ack option is set.
   */
    public int prefetchCount;
  /**
   * By default the QoS settings apply to the current channel only. If this field is set, they are applied to the entire connection.
   */
    public boolean global;
	  
	  public final static int INDEX = 10;
	  
	  
  /**
   * This method requests a specific quality of service. The QoS can be specified for the current channel or for all channels on the connection. The particular properties and semantics of a qos method always depend on the content class semantics. Though the qos method could in principle apply to both peers, it is currently meaningful only for the server.
   */
    public Qos(int prefetchSize,int prefetchCount,boolean global) {
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
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Qos";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.prefetchSize = AMQPStreamUtil.readLong(in);
      this.prefetchCount = AMQPStreamUtil.readShort(in);
      this.global = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLong(this.prefetchSize, out);
      AMQPStreamUtil.writeShort(this.prefetchCount, out);
      AMQPStreamUtil.writeBit(this.global, out);
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
   * This method tells the client that the requested QoS levels could be handled by the server. The requested QoS applies to all active consumers until a new QoS is defined.
   */
  public static class QosOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 11;
	  
	  
  /**
   * This method tells the client that the requested QoS levels could be handled by the server. The requested QoS applies to all active consumers until a new QoS is defined.
   */
    public QosOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$QosOk";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method asks the server to start a "consumer", which is a transient request for messages from a specific queue. Consumers last as long as the channel they were created on, or until the client cancels them.
   */
  public static class Consume
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
  /**
   * Specifies the name of the queue to consume from. If the queue name is null, refers to the current queue for the channel, which is the last declared queue.
   */
    public java.lang.String queue;
  /**
   * Specifies the identifier for the consumer. The consumer tag is local to a connection, so two clients can use the same consumer tags. If this field is empty the server will generate a unique tag.
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
   * Request exclusive consumer access, meaning only this consumer can access the queue.
   */
    public boolean exclusive;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
  /**
   * A set of filters for the consume. The syntax and semantics of these filters depends on the providers implementation.
   */
    public Map filter;
	  
	  public final static int INDEX = 20;
	  
	  
  /**
   * This method asks the server to start a "consumer", which is a transient request for messages from a specific queue. Consumers last as long as the channel they were created on, or until the client cancels them.
   */
    public Consume(int ticket,java.lang.String queue,java.lang.String consumerTag,boolean noLocal,boolean noAck,boolean exclusive,boolean nowait,Map filter) {
      this.ticket = ticket;
      this.queue = queue;
      this.consumerTag = consumerTag;
      this.noLocal = noLocal;
      this.noAck = noAck;
      this.exclusive = exclusive;
      this.nowait = nowait;
      this.filter = filter;
    }
    
    public Consume() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Consume";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
      this.noLocal = AMQPStreamUtil.readBit(in);
      this.noAck = AMQPStreamUtil.readBit(in);
      this.exclusive = AMQPStreamUtil.readBit(in);
      this.nowait = AMQPStreamUtil.readBit(in);
      this.filter = AMQPStreamUtil.readTable(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
      AMQPStreamUtil.writeBit(this.noLocal, out);
      AMQPStreamUtil.writeBit(this.noAck, out);
      AMQPStreamUtil.writeBit(this.exclusive, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
      AMQPStreamUtil.writeTable(this.filter, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Consume(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
      buff.append("nowait=");
      buff.append(nowait);
      buff.append(',');
      buff.append("filter=");
      buff.append(filter);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method provides the client with a consumer tag which it MUST use in methods that work with the consumer.
   */
  public static class ConsumeOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * Holds the consumer tag specified by the client or provided by the server.
   */
    public java.lang.String consumerTag;
	  
	  public final static int INDEX = 21;
	  
	  
  /**
   * This method provides the client with a consumer tag which it MUST use in methods that work with the consumer.
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
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$ConsumeOk";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
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
   * This method cancels a consumer. This does not affect already delivered messages, but it does mean the server will not send any more messages for that consumer.
   */
  public static class Cancel
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public java.lang.String consumerTag;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
	  
	  public final static int INDEX = 30;
	  
	  
  /**
   * This method cancels a consumer. This does not affect already delivered messages, but it does mean the server will not send any more messages for that consumer.
   */
    public Cancel(java.lang.String consumerTag,boolean nowait) {
      this.consumerTag = consumerTag;
      this.nowait = nowait;
    }
    
    public Cancel() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Cancel";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
      this.nowait = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Cancel(");
      
      buff.append("consumerTag=");
      buff.append(consumerTag);
      buff.append(',');
      buff.append("nowait=");
      buff.append(nowait);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method confirms that the cancellation was completed.
   */
  public static class CancelOk
    extends AbstractMarshallingMethod {
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
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$CancelOk";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
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
   * This method requests permission to start staging a message. Staging means sending the message into a temporary area at the recipient end and then delivering the message by referring to this temporary area. Staging is how the protocol handles partial file transfers - if a message is partially staged and the connection breaks, the next time the sender starts to stage it, it can restart from where it left off.
   */
  public static class Open
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * This is the staging identifier. This is an arbitrary string chosen by the sender. For staging to work correctly the sender must use the same staging identifier when staging the same message a second time after recovery from a failure. A good choice for the staging identifier would be the SHA1 hash of the message properties data (including the original filename, revised time, etc.).
   */
    public java.lang.String identifier;
  /**
   * The size of the content in octets. The recipient may use this information to allocate or check available space in advance, to avoid "disk full" errors during staging of very large messages.
   */
    public long contentSize;
	  
	  public final static int INDEX = 40;
	  
	  
  /**
   * This method requests permission to start staging a message. Staging means sending the message into a temporary area at the recipient end and then delivering the message by referring to this temporary area. Staging is how the protocol handles partial file transfers - if a message is partially staged and the connection breaks, the next time the sender starts to stage it, it can restart from where it left off.
   */
    public Open(java.lang.String identifier,long contentSize) {
      this.identifier = identifier;
      this.contentSize = contentSize;
    }
    
    public Open() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Open";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.identifier = AMQPStreamUtil.readShortstr(in);
      this.contentSize = AMQPStreamUtil.readLonglong(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.identifier, out);
      AMQPStreamUtil.writeLonglong(this.contentSize, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Open(");
      
      buff.append("identifier=");
      buff.append(identifier);
      buff.append(',');
      buff.append("contentSize=");
      buff.append(contentSize);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method confirms that the recipient is ready to accept staged data. If the message was already partially-staged at a previous time the recipient will report the number of octets already staged.
   */
  public static class OpenOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The amount of previously-staged content in octets. For a new message this will be zero.
   */
    public long stagedSize;
	  
	  public final static int INDEX = 41;
	  
	  
  /**
   * This method confirms that the recipient is ready to accept staged data. If the message was already partially-staged at a previous time the recipient will report the number of octets already staged.
   */
    public OpenOk(long stagedSize) {
      this.stagedSize = stagedSize;
    }
    
    public OpenOk() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$OpenOk";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.stagedSize = AMQPStreamUtil.readLonglong(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLonglong(this.stagedSize, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("OpenOk(");
      
      buff.append("stagedSize=");
      buff.append(stagedSize);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method stages the message, sending the message content to the recipient from the octet offset specified in the Open-Ok method.
   */
  public static class Stage
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 50;
	  
	  
  /**
   * This method stages the message, sending the message content to the recipient from the octet offset specified in the Open-Ok method.
   */
    public Stage() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Stage";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Stage(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method publishes a staged file message to a specific exchange. The file message will be routed to queues as defined by the exchange configuration and distributed to any active consumers when the transaction, if any, is committed.
   */
  public static class Publish
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
  /**
   * Specifies the name of the exchange to publish to. The exchange name can be empty, meaning the default exchange. If the exchange name is specified, and that exchange does not exist, the server will raise a channel exception.
   */
    public java.lang.String exchange;
  /**
   * Specifies the routing key for the message. The routing key is used for routing messages depending on the exchange configuration.
   */
    public java.lang.String routingKey;
  /**
   * This flag tells the server how to react if the message cannot be routed to a queue. If this flag is set, the server will return an unroutable message with a Return method. If this flag is zero, the server silently drops the message.
   */
    public boolean mandatory;
  /**
   * This flag tells the server how to react if the message cannot be routed to a queue consumer immediately. If this flag is set, the server will return an undeliverable message with a Return method. If this flag is zero, the server will queue the message, but with no guarantee that it will ever be consumed.
   */
    public boolean immediate;
  /**
   * This is the staging identifier of the message to publish. The message must have been staged. Note that a client can send the Publish method asynchronously without waiting for staging to finish.
   */
    public java.lang.String identifier;
	  
	  public final static int INDEX = 60;
	  
	  
  /**
   * This method publishes a staged file message to a specific exchange. The file message will be routed to queues as defined by the exchange configuration and distributed to any active consumers when the transaction, if any, is committed.
   */
    public Publish(int ticket,java.lang.String exchange,java.lang.String routingKey,boolean mandatory,boolean immediate,java.lang.String identifier) {
      this.ticket = ticket;
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.mandatory = mandatory;
      this.immediate = immediate;
      this.identifier = identifier;
    }
    
    public Publish() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Publish";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
      this.mandatory = AMQPStreamUtil.readBit(in);
      this.immediate = AMQPStreamUtil.readBit(in);
      this.identifier = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
      AMQPStreamUtil.writeBit(this.mandatory, out);
      AMQPStreamUtil.writeBit(this.immediate, out);
      AMQPStreamUtil.writeShortstr(this.identifier, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Publish(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
      buff.append(',');
      buff.append("identifier=");
      buff.append(identifier);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method returns an undeliverable message that was published with the "immediate" flag set, or an unroutable message published with the "mandatory" flag set. The reply code and text provide information about the reason that the message was undeliverable.
   */
  public static class Return
    extends AbstractMarshallingMethod {
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
   * Specifies the name of the exchange that the message was originally published to.
   */
    public java.lang.String exchange;
  /**
   * Specifies the routing key name specified when the message was published.
   */
    public java.lang.String routingKey;
	  
	  public final static int INDEX = 70;
	  
	  
  /**
   * This method returns an undeliverable message that was published with the "immediate" flag set, or an unroutable message published with the "mandatory" flag set. The reply code and text provide information about the reason that the message was undeliverable.
   */
    public Return(int replyCode,java.lang.String replyText,java.lang.String exchange,java.lang.String routingKey) {
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
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Return";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.replyCode = AMQPStreamUtil.readShort(in);
      this.replyText = AMQPStreamUtil.readShortstr(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.replyCode, out);
      AMQPStreamUtil.writeShortstr(this.replyText, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
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
   * This method delivers a staged file message to the client, via a consumer. In the asynchronous message delivery model, the client starts a consumer using the Consume method, then the server responds with Deliver methods as and when messages arrive for that consumer.
   */
  public static class Deliver
    extends AbstractMarshallingMethod {
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
   * Specifies the name of the exchange that the message was originally published to.
   */
    public java.lang.String exchange;
  /**
   * Specifies the routing key name specified when the message was published.
   */
    public java.lang.String routingKey;
  /**
   * This is the staging identifier of the message to deliver. The message must have been staged. Note that a server can send the Deliver method asynchronously without waiting for staging to finish.
   */
    public java.lang.String identifier;
	  
	  public final static int INDEX = 80;
	  
	  
  /**
   * This method delivers a staged file message to the client, via a consumer. In the asynchronous message delivery model, the client starts a consumer using the Consume method, then the server responds with Deliver methods as and when messages arrive for that consumer.
   */
    public Deliver(java.lang.String consumerTag,long deliveryTag,boolean redelivered,java.lang.String exchange,java.lang.String routingKey,java.lang.String identifier) {
      this.consumerTag = consumerTag;
      this.deliveryTag = deliveryTag;
      this.redelivered = redelivered;
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.identifier = identifier;
    }
    
    public Deliver() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Deliver";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
      this.deliveryTag = AMQPStreamUtil.readLonglong(in);
      this.redelivered = AMQPStreamUtil.readBit(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
      this.identifier = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
      AMQPStreamUtil.writeLonglong(this.deliveryTag, out);
      AMQPStreamUtil.writeBit(this.redelivered, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
      AMQPStreamUtil.writeShortstr(this.identifier, out);
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
      buff.append(',');
      buff.append("identifier=");
      buff.append(identifier);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method acknowledges one or more messages delivered via the Deliver method. The client can ask to confirm a single message or a set of messages up to and including a specific message.
   */
  public static class Ack
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public long deliveryTag;
  /**
   * If set to 1, the delivery tag is treated as "up to and including", so that the client can acknowledge multiple messages with a single method. If set to zero, the delivery tag refers to a single message. If the multiple field is 1, and the delivery tag is zero, tells the server to acknowledge all outstanding messages.
   */
    public boolean multiple;
	  
	  public final static int INDEX = 90;
	  
	  
  /**
   * This method acknowledges one or more messages delivered via the Deliver method. The client can ask to confirm a single message or a set of messages up to and including a specific message.
   */
    public Ack(long deliveryTag,boolean multiple) {
      this.deliveryTag = deliveryTag;
      this.multiple = multiple;
    }
    
    public Ack() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Ack";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.deliveryTag = AMQPStreamUtil.readLonglong(in);
      this.multiple = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLonglong(this.deliveryTag, out);
      AMQPStreamUtil.writeBit(this.multiple, out);
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
   * This method allows a client to reject a message. It can be used to return untreatable messages to their original queue. Note that file content is staged before delivery, so the client will not use this method to interrupt delivery of a large message.
   */
  public static class Reject
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public long deliveryTag;
  /**
   * If this field is zero, the message will be discarded. If this bit is 1, the server will attempt to requeue the message.
   */
    public boolean requeue;
	  
	  public final static int INDEX = 100;
	  
	  
  /**
   * This method allows a client to reject a message. It can be used to return untreatable messages to their original queue. Note that file content is staged before delivery, so the client will not use this method to interrupt delivery of a large message.
   */
    public Reject(long deliveryTag,boolean requeue) {
      this.deliveryTag = deliveryTag;
      this.requeue = requeue;
    }
    
    public Reject() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File$Reject";
      
    }
    
    public int getClassId() { 
      return 70;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$File";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.deliveryTag = AMQPStreamUtil.readLonglong(in);
      this.requeue = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLonglong(this.deliveryTag, out);
      AMQPStreamUtil.writeBit(this.requeue, out);
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



  public void readFrom(DataInputStream in)
    throws IOException {
    
    this.contentType = AMQPStreamUtil.readShortstr(in);
    this.contentEncoding = AMQPStreamUtil.readShortstr(in);
    this.headers = AMQPStreamUtil.readTable(in);
    this.priority = AMQPStreamUtil.readOctet(in);
    this.replyTo = AMQPStreamUtil.readShortstr(in);
    this.messageId = AMQPStreamUtil.readShortstr(in);
    this.filename = AMQPStreamUtil.readShortstr(in);
    this.timestamp = AMQPStreamUtil.readTimestamp(in);
    this.clusterId = AMQPStreamUtil.readShortstr(in);
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
    AMQPStreamUtil.writeShortstr(this.contentType, out);
    AMQPStreamUtil.writeShortstr(this.contentEncoding, out);
    AMQPStreamUtil.writeTable(this.headers, out);
    AMQPStreamUtil.writeOctet(this.priority, out);
    AMQPStreamUtil.writeShortstr(this.replyTo, out);
    AMQPStreamUtil.writeShortstr(this.messageId, out);
    AMQPStreamUtil.writeShortstr(this.filename, out);
    AMQPStreamUtil.writeTimestamp(this.timestamp, out);
    AMQPStreamUtil.writeShortstr(this.clusterId, out);
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("File(");
    
    buff.append("contentType=");
    buff.append(contentType);
    buff.append(',');
    buff.append("contentEncoding=");
    buff.append(contentEncoding);
    buff.append(',');
    buff.append("headers=");
    buff.append(headers);
    buff.append(',');
    buff.append("priority=");
    buff.append(priority);
    buff.append(',');
    buff.append("replyTo=");
    buff.append(replyTo);
    buff.append(',');
    buff.append("messageId=");
    buff.append(messageId);
    buff.append(',');
    buff.append("filename=");
    buff.append(filename);
    buff.append(',');
    buff.append("timestamp=");
    buff.append(timestamp);
    buff.append(',');
    buff.append("clusterId=");
    buff.append(clusterId);
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Stream
    extends AbstractMarshallingClass {
    private static final long serialVersionUID = 1L;

  
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
    public int priority;
  /**
   * 
   */
    public Date timestamp;
	  
	  public final static int INDEX = 80;
	  
    public int getClassId() { 
      return INDEX;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream";
    }
    
  
    public static final int METHOD_STREAM_QOS = 10;
    public static final int METHOD_STREAM_QOS_OK = 11;
    public static final int METHOD_STREAM_CONSUME = 20;
    public static final int METHOD_STREAM_CONSUME_OK = 21;
    public static final int METHOD_STREAM_CANCEL = 30;
    public static final int METHOD_STREAM_CANCEL_OK = 31;
    public static final int METHOD_STREAM_PUBLISH = 40;
    public static final int METHOD_STREAM_RETURN = 50;
    public static final int METHOD_STREAM_DELIVER = 60;
  

    public static final int[] mids = {10,11,20,21,30,31,40,50,60};
    
    public static final java.lang.String[] methodnames = {
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Qos",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$QosOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Consume",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$ConsumeOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Cancel",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$CancelOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Publish",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Return",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Deliver"
    };
    
    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.Stream.mids.length; i++) {
        if (AMQP.Stream.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.Stream.methodnames[pos];  
      return "";
    }
  


  /**
   * This method requests a specific quality of service. The QoS can be specified for the current channel or for all channels on the connection. The particular properties and semantics of a qos method always depend on the content class semantics. Though the qos method could in principle apply to both peers, it is currently meaningful only for the server.
   */
  public static class Qos
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The client can request that messages be sent in advance so that when the client finishes processing a message, the following message is already held locally, rather than needing to be sent down the channel. Prefetching gives a performance improvement. This field specifies the prefetch window size in octets. May be set to zero, meaning "no specific limit". Note that other prefetch limits may still apply.
   */
    public int prefetchSize;
  /**
   * Specifies a prefetch window in terms of whole messages. This field may be used in combination with the prefetch-size field; a message will only be sent in advance if both prefetch windows (and those at the channel and connection level) allow it.
   */
    public int prefetchCount;
  /**
   * Specifies a desired transfer rate in octets per second. This is usually determined by the application that uses the streaming data. A value of zero means "no limit", i.e. as rapidly as possible.
   */
    public int consumeRate;
  /**
   * By default the QoS settings apply to the current channel only. If this field is set, they are applied to the entire connection.
   */
    public boolean global;
	  
	  public final static int INDEX = 10;
	  
	  
  /**
   * This method requests a specific quality of service. The QoS can be specified for the current channel or for all channels on the connection. The particular properties and semantics of a qos method always depend on the content class semantics. Though the qos method could in principle apply to both peers, it is currently meaningful only for the server.
   */
    public Qos(int prefetchSize,int prefetchCount,int consumeRate,boolean global) {
      this.prefetchSize = prefetchSize;
      this.prefetchCount = prefetchCount;
      this.consumeRate = consumeRate;
      this.global = global;
    }
    
    public Qos() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Qos";
      
    }
    
    public int getClassId() { 
      return 80;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.prefetchSize = AMQPStreamUtil.readLong(in);
      this.prefetchCount = AMQPStreamUtil.readShort(in);
      this.consumeRate = AMQPStreamUtil.readLong(in);
      this.global = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLong(this.prefetchSize, out);
      AMQPStreamUtil.writeShort(this.prefetchCount, out);
      AMQPStreamUtil.writeLong(this.consumeRate, out);
      AMQPStreamUtil.writeBit(this.global, out);
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
      buff.append("consumeRate=");
      buff.append(consumeRate);
      buff.append(',');
      buff.append("global=");
      buff.append(global);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method tells the client that the requested QoS levels could be handled by the server. The requested QoS applies to all active consumers until a new QoS is defined.
   */
  public static class QosOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 11;
	  
	  
  /**
   * This method tells the client that the requested QoS levels could be handled by the server. The requested QoS applies to all active consumers until a new QoS is defined.
   */
    public QosOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$QosOk";
      
    }
    
    public int getClassId() { 
      return 80;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method asks the server to start a "consumer", which is a transient request for messages from a specific queue. Consumers last as long as the channel they were created on, or until the client cancels them.
   */
  public static class Consume
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
  /**
   * Specifies the name of the queue to consume from. If the queue name is null, refers to the current queue for the channel, which is the last declared queue.
   */
    public java.lang.String queue;
  /**
   * Specifies the identifier for the consumer. The consumer tag is local to a connection, so two clients can use the same consumer tags. If this field is empty the server will generate a unique tag.
   */
    public java.lang.String consumerTag;
  /**
   * 
   */
    public boolean noLocal;
  /**
   * Request exclusive consumer access, meaning only this consumer can access the queue.
   */
    public boolean exclusive;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
  /**
   * A set of filters for the consume. The syntax and semantics of these filters depends on the providers implementation.
   */
    public Map filter;
	  
	  public final static int INDEX = 20;
	  
	  
  /**
   * This method asks the server to start a "consumer", which is a transient request for messages from a specific queue. Consumers last as long as the channel they were created on, or until the client cancels them.
   */
    public Consume(int ticket,java.lang.String queue,java.lang.String consumerTag,boolean noLocal,boolean exclusive,boolean nowait,Map filter) {
      this.ticket = ticket;
      this.queue = queue;
      this.consumerTag = consumerTag;
      this.noLocal = noLocal;
      this.exclusive = exclusive;
      this.nowait = nowait;
      this.filter = filter;
    }
    
    public Consume() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Consume";
      
    }
    
    public int getClassId() { 
      return 80;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
      this.noLocal = AMQPStreamUtil.readBit(in);
      this.exclusive = AMQPStreamUtil.readBit(in);
      this.nowait = AMQPStreamUtil.readBit(in);
      this.filter = AMQPStreamUtil.readTable(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
      AMQPStreamUtil.writeBit(this.noLocal, out);
      AMQPStreamUtil.writeBit(this.exclusive, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
      AMQPStreamUtil.writeTable(this.filter, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Consume(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
      buff.append("exclusive=");
      buff.append(exclusive);
      buff.append(',');
      buff.append("nowait=");
      buff.append(nowait);
      buff.append(',');
      buff.append("filter=");
      buff.append(filter);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method provides the client with a consumer tag which it may use in methods that work with the consumer.
   */
  public static class ConsumeOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * Holds the consumer tag specified by the client or provided by the server.
   */
    public java.lang.String consumerTag;
	  
	  public final static int INDEX = 21;
	  
	  
  /**
   * This method provides the client with a consumer tag which it may use in methods that work with the consumer.
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
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$ConsumeOk";
      
    }
    
    public int getClassId() { 
      return 80;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
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
   * This method cancels a consumer. Since message delivery is asynchronous the client may continue to receive messages for a short while after cancelling a consumer. It may process or discard these as appropriate.
   */
  public static class Cancel
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public java.lang.String consumerTag;
  /**
   * If set, the server will not respond to the method. The client should not wait for a reply method. If the server could not complete the method it will raise a channel or connection exception.
   */
    public boolean nowait;
	  
	  public final static int INDEX = 30;
	  
	  
  /**
   * This method cancels a consumer. Since message delivery is asynchronous the client may continue to receive messages for a short while after cancelling a consumer. It may process or discard these as appropriate.
   */
    public Cancel(java.lang.String consumerTag,boolean nowait) {
      this.consumerTag = consumerTag;
      this.nowait = nowait;
    }
    
    public Cancel() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Cancel";
      
    }
    
    public int getClassId() { 
      return 80;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
      this.nowait = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
      AMQPStreamUtil.writeBit(this.nowait, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Cancel(");
      
      buff.append("consumerTag=");
      buff.append(consumerTag);
      buff.append(',');
      buff.append("nowait=");
      buff.append(nowait);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method confirms that the cancellation was completed.
   */
  public static class CancelOk
    extends AbstractMarshallingMethod {
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
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$CancelOk";
      
    }
    
    public int getClassId() { 
      return 80;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
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
   * This method publishes a message to a specific exchange. The message will be routed to queues as defined by the exchange configuration and distributed to any active consumers as appropriate.
   */
  public static class Publish
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
  /**
   * Specifies the name of the exchange to publish to. The exchange name can be empty, meaning the default exchange. If the exchange name is specified, and that exchange does not exist, the server will raise a channel exception.
   */
    public java.lang.String exchange;
  /**
   * Specifies the routing key for the message. The routing key is used for routing messages depending on the exchange configuration.
   */
    public java.lang.String routingKey;
  /**
   * This flag tells the server how to react if the message cannot be routed to a queue. If this flag is set, the server will return an unroutable message with a Return method. If this flag is zero, the server silently drops the message.
   */
    public boolean mandatory;
  /**
   * This flag tells the server how to react if the message cannot be routed to a queue consumer immediately. If this flag is set, the server will return an undeliverable message with a Return method. If this flag is zero, the server will queue the message, but with no guarantee that it will ever be consumed.
   */
    public boolean immediate;
	  
	  public final static int INDEX = 40;
	  
	  
  /**
   * This method publishes a message to a specific exchange. The message will be routed to queues as defined by the exchange configuration and distributed to any active consumers as appropriate.
   */
    public Publish(int ticket,java.lang.String exchange,java.lang.String routingKey,boolean mandatory,boolean immediate) {
      this.ticket = ticket;
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
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Publish";
      
    }
    
    public int getClassId() { 
      return 80;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
      this.mandatory = AMQPStreamUtil.readBit(in);
      this.immediate = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
      AMQPStreamUtil.writeBit(this.mandatory, out);
      AMQPStreamUtil.writeBit(this.immediate, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Publish(");
      
      buff.append("ticket=");
      buff.append(ticket);
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
   * This method returns an undeliverable message that was published with the "immediate" flag set, or an unroutable message published with the "mandatory" flag set. The reply code and text provide information about the reason that the message was undeliverable.
   */
  public static class Return
    extends AbstractMarshallingMethod {
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
   * Specifies the name of the exchange that the message was originally published to.
   */
    public java.lang.String exchange;
  /**
   * Specifies the routing key name specified when the message was published.
   */
    public java.lang.String routingKey;
	  
	  public final static int INDEX = 50;
	  
	  
  /**
   * This method returns an undeliverable message that was published with the "immediate" flag set, or an unroutable message published with the "mandatory" flag set. The reply code and text provide information about the reason that the message was undeliverable.
   */
    public Return(int replyCode,java.lang.String replyText,java.lang.String exchange,java.lang.String routingKey) {
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
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Return";
      
    }
    
    public int getClassId() { 
      return 80;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.replyCode = AMQPStreamUtil.readShort(in);
      this.replyText = AMQPStreamUtil.readShortstr(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.replyCode, out);
      AMQPStreamUtil.writeShortstr(this.replyText, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
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
   * This method delivers a message to the client, via a consumer. In the asynchronous message delivery model, the client starts a consumer using the Consume method, then the server responds with Deliver methods as and when messages arrive for that consumer.
   */
  public static class Deliver
    extends AbstractMarshallingMethod {
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
   * Specifies the name of the exchange that the message was originally published to.
   */
    public java.lang.String exchange;
  /**
   * Specifies the name of the queue that the message came from. Note that a single channel can start many consumers on different queues.
   */
    public java.lang.String queue;
	  
	  public final static int INDEX = 60;
	  
	  
  /**
   * This method delivers a message to the client, via a consumer. In the asynchronous message delivery model, the client starts a consumer using the Consume method, then the server responds with Deliver methods as and when messages arrive for that consumer.
   */
    public Deliver(java.lang.String consumerTag,long deliveryTag,java.lang.String exchange,java.lang.String queue) {
      this.consumerTag = consumerTag;
      this.deliveryTag = deliveryTag;
      this.exchange = exchange;
      this.queue = queue;
    }
    
    public Deliver() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream$Deliver";
      
    }
    
    public int getClassId() { 
      return 80;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Stream";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.consumerTag = AMQPStreamUtil.readShortstr(in);
      this.deliveryTag = AMQPStreamUtil.readLonglong(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.consumerTag, out);
      AMQPStreamUtil.writeLonglong(this.deliveryTag, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
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
      buff.append("exchange=");
      buff.append(exchange);
      buff.append(',');
      buff.append("queue=");
      buff.append(queue);
      
      buff.append(')');
      return buff.toString();
    }
  }



  public void readFrom(DataInputStream in)
    throws IOException {
    
    this.contentType = AMQPStreamUtil.readShortstr(in);
    this.contentEncoding = AMQPStreamUtil.readShortstr(in);
    this.headers = AMQPStreamUtil.readTable(in);
    this.priority = AMQPStreamUtil.readOctet(in);
    this.timestamp = AMQPStreamUtil.readTimestamp(in);
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
    AMQPStreamUtil.writeShortstr(this.contentType, out);
    AMQPStreamUtil.writeShortstr(this.contentEncoding, out);
    AMQPStreamUtil.writeTable(this.headers, out);
    AMQPStreamUtil.writeOctet(this.priority, out);
    AMQPStreamUtil.writeTimestamp(this.timestamp, out);
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Stream(");
    
    buff.append("contentType=");
    buff.append(contentType);
    buff.append(',');
    buff.append("contentEncoding=");
    buff.append(contentEncoding);
    buff.append(',');
    buff.append("headers=");
    buff.append(headers);
    buff.append(',');
    buff.append("priority=");
    buff.append(priority);
    buff.append(',');
    buff.append("timestamp=");
    buff.append(timestamp);
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Tx
    extends AbstractMarshallingClass {
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
  

    public static final int[] mids = {10,11,20,21,30,31};
    
    public static final java.lang.String[] methodnames = {
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$Select",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$SelectOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$Commit",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$CommitOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$Rollback",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tx$RollbackOk"
    };
    
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
   * This method sets the channel to use standard transactions. The client must use this method at least once on a channel before using the Commit or Rollback methods.
   */
  public static class Select
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 10;
	  
	  
  /**
   * This method sets the channel to use standard transactions. The client must use this method at least once on a channel before using the Commit or Rollback methods.
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method confirms to the client that the channel was successfully set to use standard transactions.
   */
  public static class SelectOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 11;
	  
	  
  /**
   * This method confirms to the client that the channel was successfully set to use standard transactions.
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method commits all messages published and acknowledged in the current transaction. A new transaction starts immediately after a commit.
   */
  public static class Commit
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 20;
	  
	  
  /**
   * This method commits all messages published and acknowledged in the current transaction. A new transaction starts immediately after a commit.
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method confirms to the client that the commit succeeded. Note that if a commit fails, the server raises a channel exception.
   */
  public static class CommitOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 21;
	  
	  
  /**
   * This method confirms to the client that the commit succeeded. Note that if a commit fails, the server raises a channel exception.
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method abandons all messages published and acknowledged in the current transaction. A new transaction starts immediately after a rollback.
   */
  public static class Rollback
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 30;
	  
	  
  /**
   * This method abandons all messages published and acknowledged in the current transaction. A new transaction starts immediately after a rollback.
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method confirms to the client that the rollback succeeded. Note that if an rollback fails, the server raises a channel exception.
   */
  public static class RollbackOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 31;
	  
	  
  /**
   * This method confirms to the client that the rollback succeeded. Note that if an rollback fails, the server raises a channel exception.
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

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("RollbackOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }



  public void readFrom(DataInputStream in)
    throws IOException {
    
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Tx(");
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Dtx
    extends AbstractMarshallingClass {
    private static final long serialVersionUID = 1L;

  
	  
	  public final static int INDEX = 100;
	  
    public int getClassId() { 
      return INDEX;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx";
    }
    
  
    public static final int METHOD_DTX_SELECT = 10;
    public static final int METHOD_DTX_SELECT_OK = 11;
    public static final int METHOD_DTX_START = 20;
    public static final int METHOD_DTX_START_OK = 21;
  

    public static final int[] mids = {10,11,20,21};
    
    public static final java.lang.String[] methodnames = {
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx$Select",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx$SelectOk",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx$Start",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx$StartOk"
    };
    
    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.Dtx.mids.length; i++) {
        if (AMQP.Dtx.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.Dtx.methodnames[pos];  
      return "";
    }
  


  /**
   * This method sets the channel to use distributed transactions. The client must use this method at least once on a channel before using the Start method.
   */
  public static class Select
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 10;
	  
	  
  /**
   * This method sets the channel to use distributed transactions. The client must use this method at least once on a channel before using the Start method.
   */
    public Select() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx$Select";
      
    }
    
    public int getClassId() { 
      return 100;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method confirms to the client that the channel was successfully set to use distributed transactions.
   */
  public static class SelectOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 11;
	  
	  
  /**
   * This method confirms to the client that the channel was successfully set to use distributed transactions.
   */
    public SelectOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx$SelectOk";
      
    }
    
    public int getClassId() { 
      return 100;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
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
   * This method starts a new distributed transaction. This must be the first method on a new channel that uses the distributed transaction mode, before any methods that publish or consume messages.
   */
  public static class Start
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The distributed transaction key. This identifies the transaction so that the AMQP server can coordinate with the distributed transaction coordinator.
   */
    public java.lang.String dtxIdentifier;
	  
	  public final static int INDEX = 20;
	  
	  
  /**
   * This method starts a new distributed transaction. This must be the first method on a new channel that uses the distributed transaction mode, before any methods that publish or consume messages.
   */
    public Start(java.lang.String dtxIdentifier) {
      this.dtxIdentifier = dtxIdentifier;
    }
    
    public Start() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx$Start";
      
    }
    
    public int getClassId() { 
      return 100;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.dtxIdentifier = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.dtxIdentifier, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Start(");
      
      buff.append("dtxIdentifier=");
      buff.append(dtxIdentifier);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * This method confirms to the client that the transaction started. Note that if a start fails, the server raises a channel exception.
   */
  public static class StartOk
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 21;
	  
	  
  /**
   * This method confirms to the client that the transaction started. Note that if a start fails, the server raises a channel exception.
   */
    public StartOk() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx$StartOk";
      
    }
    
    public int getClassId() { 
      return 100;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Dtx";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("StartOk(");
      
      buff.append(')');
      return buff.toString();
    }
  }



  public void readFrom(DataInputStream in)
    throws IOException {
    
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Dtx(");
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Tunnel
    extends AbstractMarshallingClass {
    private static final long serialVersionUID = 1L;

  
  /**
   * 
   */
    public Map headers;
  /**
   * 
   */
    public java.lang.String proxyName;
  /**
   * 
   */
    public java.lang.String dataName;
  /**
   * 
   */
    public int durable;
  /**
   * 
   */
    public int broadcast;
	  
	  public final static int INDEX = 110;
	  
    public int getClassId() { 
      return INDEX;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tunnel";
    }
    
  
    public static final int METHOD_TUNNEL_REQUEST = 10;
  

    public static final int[] mids = {10};
    
    public static final java.lang.String[] methodnames = {
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tunnel$Request"
    };
    
    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.Tunnel.mids.length; i++) {
        if (AMQP.Tunnel.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.Tunnel.methodnames[pos];  
      return "";
    }
  


  /**
   * This method tunnels a block of binary data, which can be an encoded AMQP method or other data. The binary data is sent as the content for the Tunnel.Request method.
   */
  public static class Request
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * This field table holds arbitrary meta-data that the sender needs to pass to the recipient.
   */
    public Map metaData;
	  
	  public final static int INDEX = 10;
	  
	  
  /**
   * This method tunnels a block of binary data, which can be an encoded AMQP method or other data. The binary data is sent as the content for the Tunnel.Request method.
   */
    public Request(Map metaData) {
      this.metaData = metaData;
    }
    
    public Request() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tunnel$Request";
      
    }
    
    public int getClassId() { 
      return 110;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Tunnel";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.metaData = AMQPStreamUtil.readTable(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeTable(this.metaData, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Request(");
      
      buff.append("metaData=");
      buff.append(metaData);
      
      buff.append(')');
      return buff.toString();
    }
  }



  public void readFrom(DataInputStream in)
    throws IOException {
    
    this.headers = AMQPStreamUtil.readTable(in);
    this.proxyName = AMQPStreamUtil.readShortstr(in);
    this.dataName = AMQPStreamUtil.readShortstr(in);
    this.durable = AMQPStreamUtil.readOctet(in);
    this.broadcast = AMQPStreamUtil.readOctet(in);
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
    AMQPStreamUtil.writeTable(this.headers, out);
    AMQPStreamUtil.writeShortstr(this.proxyName, out);
    AMQPStreamUtil.writeShortstr(this.dataName, out);
    AMQPStreamUtil.writeOctet(this.durable, out);
    AMQPStreamUtil.writeOctet(this.broadcast, out);
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Tunnel(");
    
    buff.append("headers=");
    buff.append(headers);
    buff.append(',');
    buff.append("proxyName=");
    buff.append(proxyName);
    buff.append(',');
    buff.append("dataName=");
    buff.append(dataName);
    buff.append(',');
    buff.append("durable=");
    buff.append(durable);
    buff.append(',');
    buff.append("broadcast=");
    buff.append(broadcast);
    
    buff.append(')');
    return buff.toString();
  }
 }

  public static class Message
    extends AbstractMarshallingClass {
    private static final long serialVersionUID = 1L;

  
	  
	  public final static int INDEX = 120;
	  
    public int getClassId() { 
      return INDEX;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }
    
  
    public static final int METHOD_MESSAGE_TRANSFER = 10;
    public static final int METHOD_MESSAGE_CONSUME = 20;
    public static final int METHOD_MESSAGE_CANCEL = 30;
    public static final int METHOD_MESSAGE_GET = 40;
    public static final int METHOD_MESSAGE_RECOVER = 50;
    public static final int METHOD_MESSAGE_OPEN = 60;
    public static final int METHOD_MESSAGE_CLOSE = 70;
    public static final int METHOD_MESSAGE_APPEND = 80;
    public static final int METHOD_MESSAGE_CHECKPOINT = 90;
    public static final int METHOD_MESSAGE_RESUME = 100;
    public static final int METHOD_MESSAGE_QOS = 110;
    public static final int METHOD_MESSAGE_OK = 500;
    public static final int METHOD_MESSAGE_EMPTY = 510;
    public static final int METHOD_MESSAGE_REJECT = 520;
    public static final int METHOD_MESSAGE_OFFSET = 530;
  

    public static final int[] mids = {10,20,30,40,50,60,70,80,90,100,110,500,510,520,530};
    
    public static final java.lang.String[] methodnames = {
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Transfer",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Consume",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Cancel",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Get",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Recover",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Open",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Close",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Append",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Checkpoint",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Resume",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Qos",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Ok",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Empty",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Reject",
  	  "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Offset"
    };
    
    private static int getPosition(int id) {
      for (int i = 0; i < AMQP.Message.mids.length; i++) {
        if (AMQP.Message.mids[i] == id)
          return i;
      }
      return -1;
    }

    public java.lang.String getMethodName(int id) {
      int pos = getPosition(id);
      if (pos > -1)
        return AMQP.Message.methodnames[pos];  
      return "";
    }
  


  /**
   * [WORK IN PROGRESS] This method transfers a message between two peers. When a client uses this method to publish a message to a broker, the destination identifies a specific exchange. The message will then be routed to queues as defined by the exchange configuration and distributed to any active consumers when the transaction, if any, is committed. In the asynchronous message delivery model, the client starts a consumer using the Consume method and passing in a destination, then the broker responds with transfer methods to the specified destination as and when messages arrive for that consumer. If synchronous message delivery is required, the client may issue a get request which on success causes a single message to be transferred to the specified destination. Message acknowledgement is signalled by the return result of this method.
   */
  public static class Transfer
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
  /**
   * Specifies the destination to which the message is to be transferred. The destination can be empty, meaning the default exchange or consumer. If the destination is specified, and that exchange or consumer does not exist, the peer must raise a channel exception.
   */
    public java.lang.String destination;
  /**
   * 
   */
    public boolean redelivered;
  /**
   * This flag tells the server how to react if the message cannot be routed to a queue consumer immediately. If this flag is set, the server will reject the message. If this flag is zero, the server will queue the message, but with no guarantee that it will ever be consumed.
   */
    public boolean immediate;
  /**
   * If this is set to a non zero value then a message expiration time will be computed based on the current time plus this value. Messages that live longer than their expiration time will be discarded (or dead lettered).
   */
    public long ttl;
  /**
   * 
   */
    public int priority;
  /**
   * Set on arrival by the broker.
   */
    public Date timestamp;
  /**
   * 
   */
    public int deliveryMode;
  /**
   * The expiration header assigned by the broker. After receiving the message the broker sets expiration to the sum of the ttl specified in the publish method and the current time. (ttl = expiration - timestamp)
   */
    public Date expiration;
  /**
   * 
   */
    public java.lang.String exchange;
  /**
   * 
   */
    public java.lang.String routingKey;
  /**
   * 
   */
    public java.lang.String messageId;
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
    public java.lang.String contentType;
  /**
   * 
   */
    public java.lang.String contentEncoding;
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
    public java.lang.String transactionId;
  /**
   * 
   */
    public LongString securityToken;
  /**
   * 
   */
    public Map applicationHeaders;
	  
	  public final static int INDEX = 10;
	  
	  
  /**
   * [WORK IN PROGRESS] This method transfers a message between two peers. When a client uses this method to publish a message to a broker, the destination identifies a specific exchange. The message will then be routed to queues as defined by the exchange configuration and distributed to any active consumers when the transaction, if any, is committed. In the asynchronous message delivery model, the client starts a consumer using the Consume method and passing in a destination, then the broker responds with transfer methods to the specified destination as and when messages arrive for that consumer. If synchronous message delivery is required, the client may issue a get request which on success causes a single message to be transferred to the specified destination. Message acknowledgement is signalled by the return result of this method.
   */
    public Transfer(int ticket,java.lang.String destination,boolean redelivered,boolean immediate,long ttl,int priority,Date timestamp,int deliveryMode,Date expiration,java.lang.String exchange,java.lang.String routingKey,java.lang.String messageId,java.lang.String correlationId,java.lang.String replyTo,java.lang.String contentType,java.lang.String contentEncoding,java.lang.String userId,java.lang.String appId,java.lang.String transactionId,LongString securityToken,Map applicationHeaders) {
      this.ticket = ticket;
      this.destination = destination;
      this.redelivered = redelivered;
      this.immediate = immediate;
      this.ttl = ttl;
      this.priority = priority;
      this.timestamp = timestamp;
      this.deliveryMode = deliveryMode;
      this.expiration = expiration;
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.messageId = messageId;
      this.correlationId = correlationId;
      this.replyTo = replyTo;
      this.contentType = contentType;
      this.contentEncoding = contentEncoding;
      this.userId = userId;
      this.appId = appId;
      this.transactionId = transactionId;
      this.securityToken = securityToken;
      this.applicationHeaders = applicationHeaders;
    }
    
    public Transfer() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Transfer";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.destination = AMQPStreamUtil.readShortstr(in);
      this.redelivered = AMQPStreamUtil.readBit(in);
      this.immediate = AMQPStreamUtil.readBit(in);
      this.ttl = AMQPStreamUtil.readLonglong(in);
      this.priority = AMQPStreamUtil.readOctet(in);
      this.timestamp = AMQPStreamUtil.readTimestamp(in);
      this.deliveryMode = AMQPStreamUtil.readOctet(in);
      this.expiration = AMQPStreamUtil.readTimestamp(in);
      this.exchange = AMQPStreamUtil.readShortstr(in);
      this.routingKey = AMQPStreamUtil.readShortstr(in);
      this.messageId = AMQPStreamUtil.readShortstr(in);
      this.correlationId = AMQPStreamUtil.readShortstr(in);
      this.replyTo = AMQPStreamUtil.readShortstr(in);
      this.contentType = AMQPStreamUtil.readShortstr(in);
      this.contentEncoding = AMQPStreamUtil.readShortstr(in);
      this.userId = AMQPStreamUtil.readShortstr(in);
      this.appId = AMQPStreamUtil.readShortstr(in);
      this.transactionId = AMQPStreamUtil.readShortstr(in);
      this.securityToken = AMQPStreamUtil.readLongstr(in);
      this.applicationHeaders = AMQPStreamUtil.readTable(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.destination, out);
      AMQPStreamUtil.writeBit(this.redelivered, out);
      AMQPStreamUtil.writeBit(this.immediate, out);
      AMQPStreamUtil.writeLonglong(this.ttl, out);
      AMQPStreamUtil.writeOctet(this.priority, out);
      AMQPStreamUtil.writeTimestamp(this.timestamp, out);
      AMQPStreamUtil.writeOctet(this.deliveryMode, out);
      AMQPStreamUtil.writeTimestamp(this.expiration, out);
      AMQPStreamUtil.writeShortstr(this.exchange, out);
      AMQPStreamUtil.writeShortstr(this.routingKey, out);
      AMQPStreamUtil.writeShortstr(this.messageId, out);
      AMQPStreamUtil.writeShortstr(this.correlationId, out);
      AMQPStreamUtil.writeShortstr(this.replyTo, out);
      AMQPStreamUtil.writeShortstr(this.contentType, out);
      AMQPStreamUtil.writeShortstr(this.contentEncoding, out);
      AMQPStreamUtil.writeShortstr(this.userId, out);
      AMQPStreamUtil.writeShortstr(this.appId, out);
      AMQPStreamUtil.writeShortstr(this.transactionId, out);
      AMQPStreamUtil.writeLongstr(this.securityToken, out);
      AMQPStreamUtil.writeTable(this.applicationHeaders, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Transfer(");
      
      buff.append("ticket=");
      buff.append(ticket);
      buff.append(',');
      buff.append("destination=");
      buff.append(destination);
      buff.append(',');
      buff.append("redelivered=");
      buff.append(redelivered);
      buff.append(',');
      buff.append("immediate=");
      buff.append(immediate);
      buff.append(',');
      buff.append("ttl=");
      buff.append(ttl);
      buff.append(',');
      buff.append("priority=");
      buff.append(priority);
      buff.append(',');
      buff.append("timestamp=");
      buff.append(timestamp);
      buff.append(',');
      buff.append("deliveryMode=");
      buff.append(deliveryMode);
      buff.append(',');
      buff.append("expiration=");
      buff.append(expiration);
      buff.append(',');
      buff.append("exchange=");
      buff.append(exchange);
      buff.append(',');
      buff.append("routingKey=");
      buff.append(routingKey);
      buff.append(',');
      buff.append("messageId=");
      buff.append(messageId);
      buff.append(',');
      buff.append("correlationId=");
      buff.append(correlationId);
      buff.append(',');
      buff.append("replyTo=");
      buff.append(replyTo);
      buff.append(',');
      buff.append("contentType=");
      buff.append(contentType);
      buff.append(',');
      buff.append("contentEncoding=");
      buff.append(contentEncoding);
      buff.append(',');
      buff.append("userId=");
      buff.append(userId);
      buff.append(',');
      buff.append("appId=");
      buff.append(appId);
      buff.append(',');
      buff.append("transactionId=");
      buff.append(transactionId);
      buff.append(',');
      buff.append("securityToken=");
      buff.append(securityToken);
      buff.append(',');
      buff.append("applicationHeaders=");
      buff.append(applicationHeaders);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] This method asks the server to start a "consumer", which is a transient request for messages from a specific queue. Consumers last as long as the channel they were created on, or until the client cancels them.
   */
  public static class Consume
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
  /**
   * Specifies the name of the queue to consume from. If the queue name is null, refers to the current queue for the channel, which is the last declared queue.
   */
    public java.lang.String queue;
  /**
   * Specifies the destination for the consumer. The destination is local to a connection, so two clients can use the same destination.
   */
    public java.lang.String destination;
  /**
   * 
   */
    public boolean noLocal;
  /**
   * 
   */
    public boolean noAck;
  /**
   * Request exclusive consumer access, meaning only this consumer can access the queue.
   */
    public boolean exclusive;
  /**
   * A set of filters for the consume. The syntax and semantics of these filters depends on the providers implementation.
   */
    public Map filter;
	  
	  public final static int INDEX = 20;
	  
	  
  /**
   * [WORK IN PROGRESS] This method asks the server to start a "consumer", which is a transient request for messages from a specific queue. Consumers last as long as the channel they were created on, or until the client cancels them.
   */
    public Consume(int ticket,java.lang.String queue,java.lang.String destination,boolean noLocal,boolean noAck,boolean exclusive,Map filter) {
      this.ticket = ticket;
      this.queue = queue;
      this.destination = destination;
      this.noLocal = noLocal;
      this.noAck = noAck;
      this.exclusive = exclusive;
      this.filter = filter;
    }
    
    public Consume() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Consume";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.destination = AMQPStreamUtil.readShortstr(in);
      this.noLocal = AMQPStreamUtil.readBit(in);
      this.noAck = AMQPStreamUtil.readBit(in);
      this.exclusive = AMQPStreamUtil.readBit(in);
      this.filter = AMQPStreamUtil.readTable(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeShortstr(this.destination, out);
      AMQPStreamUtil.writeBit(this.noLocal, out);
      AMQPStreamUtil.writeBit(this.noAck, out);
      AMQPStreamUtil.writeBit(this.exclusive, out);
      AMQPStreamUtil.writeTable(this.filter, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Consume(");
      
      buff.append("ticket=");
      buff.append(ticket);
      buff.append(',');
      buff.append("queue=");
      buff.append(queue);
      buff.append(',');
      buff.append("destination=");
      buff.append(destination);
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
      buff.append("filter=");
      buff.append(filter);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] This method cancels a consumer. This does not affect already delivered messages, but it does mean the server will not send any more messages for that consumer. The client may receive an arbitrary number of messages in between sending the cancel method and receiving the cancel-ok reply.
   */
  public static class Cancel
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public java.lang.String destination;
	  
	  public final static int INDEX = 30;
	  
	  
  /**
   * [WORK IN PROGRESS] This method cancels a consumer. This does not affect already delivered messages, but it does mean the server will not send any more messages for that consumer. The client may receive an arbitrary number of messages in between sending the cancel method and receiving the cancel-ok reply.
   */
    public Cancel(java.lang.String destination) {
      this.destination = destination;
    }
    
    public Cancel() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Cancel";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.destination = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShortstr(this.destination, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Cancel(");
      
      buff.append("destination=");
      buff.append(destination);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] This method provides a direct access to the messages in a queue using a synchronous dialogue that is designed for specific types of application where synchronous functionality is more important than performance.
   */
  public static class Get
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int ticket;
  /**
   * Specifies the name of the queue to consume from. If the queue name is null, refers to the current queue for the channel, which is the last declared queue.
   */
    public java.lang.String queue;
  /**
   * On normal completion of the get request (i.e. a response of ok). A message will be transferred to the supplied destination.
   */
    public java.lang.String destination;
  /**
   * 
   */
    public boolean noAck;
	  
	  public final static int INDEX = 40;
	  
	  
  /**
   * [WORK IN PROGRESS] This method provides a direct access to the messages in a queue using a synchronous dialogue that is designed for specific types of application where synchronous functionality is more important than performance.
   */
    public Get(int ticket,java.lang.String queue,java.lang.String destination,boolean noAck) {
      this.ticket = ticket;
      this.queue = queue;
      this.destination = destination;
      this.noAck = noAck;
    }
    
    public Get() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Get";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.ticket = AMQPStreamUtil.readShort(in);
      this.queue = AMQPStreamUtil.readShortstr(in);
      this.destination = AMQPStreamUtil.readShortstr(in);
      this.noAck = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.ticket, out);
      AMQPStreamUtil.writeShortstr(this.queue, out);
      AMQPStreamUtil.writeShortstr(this.destination, out);
      AMQPStreamUtil.writeBit(this.noAck, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Get(");
      
      buff.append("ticket=");
      buff.append(ticket);
      buff.append(',');
      buff.append("queue=");
      buff.append(queue);
      buff.append(',');
      buff.append("destination=");
      buff.append(destination);
      buff.append(',');
      buff.append("noAck=");
      buff.append(noAck);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] This method asks the broker to redeliver all unacknowledged messages on a specified channel. Zero or more messages may be redelivered. This method is only allowed on non-transacted channels.
   */
  public static class Recover
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * If this field is zero, the message will be redelivered to the original recipient. If this bit is 1, the server will attempt to requeue the message, potentially then delivering it to an alternative subscriber.
   */
    public boolean requeue;
	  
	  public final static int INDEX = 50;
	  
	  
  /**
   * [WORK IN PROGRESS] This method asks the broker to redeliver all unacknowledged messages on a specified channel. Zero or more messages may be redelivered. This method is only allowed on non-transacted channels.
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
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Recover";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.requeue = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeBit(this.requeue, out);
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
   * [WORK IN PROGRESS] This method creates a reference. A references provides a means to send a message body into a temporary area at the recipient end and then deliver the message by referring to this temporary area. This is how the protocol handles large message transfers. The scope of a ref is defined to be between calls to open (or resume) and close. Between these points it is valid for a ref to be used from any content data type, and so the receiver must hold onto its contents. Should the channel be closed when a ref is still in scope, the receiver may discard its contents (unless it is checkpointed). A ref that is in scope is considered open.
   */
  public static class Open
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public LongString reference;
	  
	  public final static int INDEX = 60;
	  
	  
  /**
   * [WORK IN PROGRESS] This method creates a reference. A references provides a means to send a message body into a temporary area at the recipient end and then deliver the message by referring to this temporary area. This is how the protocol handles large message transfers. The scope of a ref is defined to be between calls to open (or resume) and close. Between these points it is valid for a ref to be used from any content data type, and so the receiver must hold onto its contents. Should the channel be closed when a ref is still in scope, the receiver may discard its contents (unless it is checkpointed). A ref that is in scope is considered open.
   */
    public Open(LongString reference) {
      this.reference = reference;
    }
    
    public Open() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Open";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.reference = AMQPStreamUtil.readLongstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLongstr(this.reference, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Open(");
      
      buff.append("reference=");
      buff.append(reference);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] This method signals the recipient that no more data will be appended to the reference.
   */
  public static class Close
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public LongString reference;
	  
	  public final static int INDEX = 70;
	  
	  
  /**
   * [WORK IN PROGRESS] This method signals the recipient that no more data will be appended to the reference.
   */
    public Close(LongString reference) {
      this.reference = reference;
    }
    
    public Close() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Close";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.reference = AMQPStreamUtil.readLongstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLongstr(this.reference, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Close(");
      
      buff.append("reference=");
      buff.append(reference);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] This method appends data to a reference.
   */
  public static class Append
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public LongString reference;
  /**
   * 
   */
    public LongString bytes;
	  
	  public final static int INDEX = 80;
	  
	  
  /**
   * [WORK IN PROGRESS] This method appends data to a reference.
   */
    public Append(LongString reference,LongString bytes) {
      this.reference = reference;
      this.bytes = bytes;
    }
    
    public Append() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Append";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.reference = AMQPStreamUtil.readLongstr(in);
      this.bytes = AMQPStreamUtil.readLongstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLongstr(this.reference, out);
      AMQPStreamUtil.writeLongstr(this.bytes, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Append(");
      
      buff.append("reference=");
      buff.append(reference);
      buff.append(',');
      buff.append("bytes=");
      buff.append(bytes);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] This method provides a means to checkpoint large message transfer. The sender may ask the recipient to checkpoint the contents of a reference using the supplied identifier. The sender may then resume the transfer at a later point. It is at the discretion of the recipient how much data to save with the checkpoint, and the sender MUST honour the offset returned by the resume method.
   */
  public static class Checkpoint
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public LongString reference;
  /**
   * This is the checkpoint identifier. This is an arbitrary string chosen by the sender. For checkpointing to work correctly the sender must use the same checkpoint identifier when resuming the message. A good choice for the checkpoint identifier would be the SHA1 hash of the message properties data (including the original filename, revised time, etc.).
   */
    public java.lang.String identifier;
	  
	  public final static int INDEX = 90;
	  
	  
  /**
   * [WORK IN PROGRESS] This method provides a means to checkpoint large message transfer. The sender may ask the recipient to checkpoint the contents of a reference using the supplied identifier. The sender may then resume the transfer at a later point. It is at the discretion of the recipient how much data to save with the checkpoint, and the sender MUST honour the offset returned by the resume method.
   */
    public Checkpoint(LongString reference,java.lang.String identifier) {
      this.reference = reference;
      this.identifier = identifier;
    }
    
    public Checkpoint() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Checkpoint";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.reference = AMQPStreamUtil.readLongstr(in);
      this.identifier = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLongstr(this.reference, out);
      AMQPStreamUtil.writeShortstr(this.identifier, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Checkpoint(");
      
      buff.append("reference=");
      buff.append(reference);
      buff.append(',');
      buff.append("identifier=");
      buff.append(identifier);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] This method resumes a reference from the last checkpoint. A reference is considered to be open (in scope) after a resume even though it will not have been opened via the open method during this session.
   */
  public static class Resume
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public LongString reference;
  /**
   * 
   */
    public java.lang.String identifier;
	  
	  public final static int INDEX = 100;
	  
	  
  /**
   * [WORK IN PROGRESS] This method resumes a reference from the last checkpoint. A reference is considered to be open (in scope) after a resume even though it will not have been opened via the open method during this session.
   */
    public Resume(LongString reference,java.lang.String identifier) {
      this.reference = reference;
      this.identifier = identifier;
    }
    
    public Resume() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Resume";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.reference = AMQPStreamUtil.readLongstr(in);
      this.identifier = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLongstr(this.reference, out);
      AMQPStreamUtil.writeShortstr(this.identifier, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Resume(");
      
      buff.append("reference=");
      buff.append(reference);
      buff.append(',');
      buff.append("identifier=");
      buff.append(identifier);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] This method requests a specific quality of service. The QoS can be specified for the current channel or for all channels on the connection. The particular properties and semantics of a qos method always depend on the content class semantics. Though the qos method could in principle apply to both peers, it is currently meaningful only for the server.
   */
  public static class Qos
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * The client can request that messages be sent in advance so that when the client finishes processing a message, the following message is already held locally, rather than needing to be sent down the channel. Prefetching gives a performance improvement. This field specifies the prefetch window size in octets. The server will send a message in advance if it is equal to or smaller in size than the available prefetch size (and also falls into other prefetch limits). May be set to zero, meaning "no specific limit", although other prefetch limits may still apply. The prefetch-size is ignored if the no-ack option is set.
   */
    public int prefetchSize;
  /**
   * Specifies a prefetch window in terms of whole messages. This field may be used in combination with the prefetch-size field; a message will only be sent in advance if both prefetch windows (and those at the channel and connection level) allow it. The prefetch-count is ignored if the no-ack option is set.
   */
    public int prefetchCount;
  /**
   * By default the QoS settings apply to the current channel only. If this field is set, they are applied to the entire connection.
   */
    public boolean global;
	  
	  public final static int INDEX = 110;
	  
	  
  /**
   * [WORK IN PROGRESS] This method requests a specific quality of service. The QoS can be specified for the current channel or for all channels on the connection. The particular properties and semantics of a qos method always depend on the content class semantics. Though the qos method could in principle apply to both peers, it is currently meaningful only for the server.
   */
    public Qos(int prefetchSize,int prefetchCount,boolean global) {
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
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Qos";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.prefetchSize = AMQPStreamUtil.readLong(in);
      this.prefetchCount = AMQPStreamUtil.readShort(in);
      this.global = AMQPStreamUtil.readBit(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLong(this.prefetchSize, out);
      AMQPStreamUtil.writeShort(this.prefetchCount, out);
      AMQPStreamUtil.writeBit(this.global, out);
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
   * [WORK IN PROGRESS] Signals the normal completion of a method.
   */
  public static class Ok
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 500;
	  
	  
  /**
   * [WORK IN PROGRESS] Signals the normal completion of a method.
   */
    public Ok() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Ok";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Ok(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] Signals that a queue does not contain any messages.
   */
  public static class Empty
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
	  
	  public final static int INDEX = 510;
	  
	  
  /**
   * [WORK IN PROGRESS] Signals that a queue does not contain any messages.
   */
    public Empty() {
    }
    
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Empty";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Empty(");
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] This response rejects a message. A message may be rejected for a number of reasons.
   */
  public static class Reject
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public int code;
  /**
   * 
   */
    public java.lang.String text;
	  
	  public final static int INDEX = 520;
	  
	  
  /**
   * [WORK IN PROGRESS] This response rejects a message. A message may be rejected for a number of reasons.
   */
    public Reject(int code,java.lang.String text) {
      this.code = code;
      this.text = text;
    }
    
    public Reject() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Reject";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.code = AMQPStreamUtil.readShort(in);
      this.text = AMQPStreamUtil.readShortstr(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeShort(this.code, out);
      AMQPStreamUtil.writeShortstr(this.text, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Reject(");
      
      buff.append("code=");
      buff.append(code);
      buff.append(',');
      buff.append("text=");
      buff.append(text);
      
      buff.append(')');
      return buff.toString();
    }
  }

  /**
   * [WORK IN PROGRESS] Returns the data offset into a reference body.
   */
  public static class Offset
    extends AbstractMarshallingMethod {
    private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
    public long value;
	  
	  public final static int INDEX = 530;
	  
	  
  /**
   * [WORK IN PROGRESS] Returns the data offset into a reference body.
   */
    public Offset(long value) {
      this.value = value;
    }
    
    public Offset() {
    }
	  
    public int getMethodId() { 
      return INDEX;
    }

    public java.lang.String getMethodName() {
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message$Offset";
      
    }
    
    public int getClassId() { 
      return 120;
    }

    public java.lang.String getClassName() { 
      return "org.objectweb.joram.mom.amqp.marshalling.AMQP$Message";
    }

    public void readFrom(DataInputStream in)
      throws IOException {
      this.value = AMQPStreamUtil.readLonglong(in);
    }

    public void writeTo(DataOutputStream out)
      throws IOException {
      AMQPStreamUtil.writeLonglong(this.value, out);
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("Offset(");
      
      buff.append("value=");
      buff.append(value);
      
      buff.append(')');
      return buff.toString();
    }
  }



  public void readFrom(DataInputStream in)
    throws IOException {
    
  }

  public void writeTo(DataOutputStream out)
    throws IOException {
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Message(");
    
    buff.append(')');
    return buff.toString();
  }
 }

}
