/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2015 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 *                 Benoit Pelletier (Bull SA)
 */
package org.objectweb.joram.client.jms;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.Deflater;

import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.net.SocketFactory;

/**
 * A <code>FactoryParameters</code> instance holds a
 * <code>&lt;XA&gt;ConnectionFactory</code> configuration parameters,
 * it allows to configure the related ConnectionFactory.
 */
public class FactoryParameters implements java.io.Serializable, Cloneable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  private static Logger logger = Debug.getLogger(FactoryParameters.class.getName());

  /** Name of host hosting the server to create connections with. */
  private String host;

  /**
   * Returns the name of host hosting the server to create connections with.
   *
   * @return The name of host hosting the server.
   */
  public String getHost() {
    return host;
  }

  /** Port to be used for accessing the server. */
  private int port;

  /**
   * Returns the port to be used for accessing the server.
   *
   * @return The port to be used for accessing the server.
   */
  public int getPort() {
    return port;
  }

  /**
   * url needed to connect to joram HA
   */
  private String url;

  /**
   * Returns the url to be used for accessing the server.
   *
   * @return The url to be used for accessing the server.
   */
  public String getUrl() {
    return url;
  }

  /** The clientID used by connection */
  public String clientID = null;
  
  /**
   *  Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm),
   * default value is true.
   */
  public boolean TcpNoDelay = true;
  /**
   *  Enable SO_LINGER with the specified linger time in seconds, if the
   * value is less than 0 then it disables SO_LINGER. Default value is -1.
   */
  public int SoLinger = -1;
  /**
   *  Enable/disable SO_TIMEOUT with the specified timeout in milliseconds.
   * The timeout must be > 0. A timeout of zero is interpreted as an infinite
   * timeout. Default value is 0.
   */
  public int SoTimeout = 0;
  /**
   * Duration in seconds during which connecting is attempted (connecting
   * might take time if the server is temporarily not reachable); the 0 value
   * is set for connecting only once and aborting if connecting failed.
   */
  public int connectingTimer = 30;
  /**
   * Duration in seconds during which a JMS transacted (non XA) session might
   * be pending; above that duration the session is rolled back and closed;
   * the 0 value means "no timer".
   * <p>
   * The default value is 0 (no timer).
   */
  public int txPendingTimer = 0;
  /**
   * Period in milliseconds between two ping requests sent by the client
   * connection to the server; if the server does not receive any ping
   * request during approximatively more than 3 * cnxPendingTimer, the
   * connection is considered as dead and processed as required.
   */
  public int cnxPendingTimer = 0;

  /**
   * Allows to define a specific factory for socket in order to by-pass
   * compatibility problem between JDK version.
   * Currently there is two factories, The default factory one for JDK
   * since 1.4, and SocketFactory13 for JDK prior to 1.4.
   */
  public String socketFactory = SocketFactory.DefaultFactory;
  
  /**
   *  Determines whether the messages consumed are implicitly acknowledged
   * or not. When true messages are immediately removed from queue when
   * delivered.
   */
  public boolean implicitAck;

  /**
   *  Determines whether the persistent produced messages are asynchronously
   * sent (without acknowledge) or not.
   * <p>
   * Messages sent asynchronously may be lost if a failure occurs before the
   * message is persisted on the server.
   * <p>
   * Non persistent messages are always sent without acknowledgment. 
   * <p>
   *  Default is false, persistent messages are sent with acknowledge.
   */
  public boolean asyncSend = false;

  /**
   *  The maximum number of messages that can be read at once from a queue.
   * <p>
   *  Default is 1.
   */
  public int queueMessageReadMax = 1;

  /**
   *  The maximum number of acknowledgements that can be buffered in
   * Session.DUPS_OK_ACKNOWLEDGE mode when listening to a topic.
   * <p>
   *  Default value is 10.
   */
  public int topicAckBufferMax = 10;

  /**
   * Determines whether client threads which are using the same connection
   * are synchronized in order to group together the requests they send.
   */
  public boolean multiThreadSync = false;

  /**
   * The maximum time the threads hang if 'multiThreadSync' is true.
   * <p>
   *  Either they wake up (wait time out) or they are notified (by the first
   * waken up thread).
   * <p>
   *  Default value is 1ms.
   */
  public int multiThreadSyncDelay = 1;

  /**
   * The maximum numbers of threads that hang if 'multiThreadSync' is true.
   * <p>
   * Default value is 10 waiting threads.
   */
  public int multiThreadSyncThreshold = 10;

  /**
   *  This threshold is the maximum messages number over which the
   * subscription is passivated.
   * <p>
   *  Default is Integer.MAX_VALUE.
   */
  public int topicPassivationThreshold = Integer.MAX_VALUE;

  /**
   *  This threshold is the minimum messages number below which the
   * subscription is activated.
   * <p>
   *  Default value is 0.
   */
  public int topicActivationThreshold = 0;
  
  /**
   *  This is the local IP address on which the TCP connection is activated. 
   * <p>
   *  The value can either be a machine name, such as "java.sun.com", or a
   * textual representation of its IP address.
   */
  public String outLocalAddress = null;

  /**
   * This is the local IP address port on which the TCP connection is activated
   */
  public int outLocalPort = 0;

  /**
   * List of Message interceptors while receiving a message.
   * <br>These interceptors are called when <code>Session#receive()</code> is called.
   * <br>The execution follows the order of the elements within the list.
   * <br>This property is facultative. If set, then the {@link MessageInterceptor}
   * <code>handle</code> callback method of the IN interceptors}
   * are called. 
   */
  public final List inInterceptors = new ArrayList();
  /**
   * List of Message interceptors while sending a message.
   * <br>These interceptors are called when <code>Session#send()</code> is called.
   * <br>The execution follows the order of the elements within the list.
   * <br>This property is facultative. If set, then the  {@link MessageInterceptor}
   * <code>handle</code> callback method of the OUT interceptors}
   * are called. 
   */
  public final List outInterceptors = new ArrayList();
  
  /**
   * This attribute defines the minimum size beyond which the message body is compressed.
   * The default value is 0 (no compression).
   */
  public int compressedMinSize = 0;
  
  /**
   * This attribute defines the compression level (0-9) used when the message body is compressed.
   * The default value is 1 (Deflater.BEST_SPEED).
   */
  public int compressionLevel = Deflater.BEST_SPEED;
  
  /**
   * Set this attribute to true to increase performance.
   * The default value is false (use the AckedQueue)
   */
  public boolean noAckedQueue = false;
  
  /**
   * Constructs a <code>FactoryParameters</code> instance.
   *
   * @param host  Name of host hosting the server to create connections with.
   * @param port  Port to be used for accessing the server.
   */
  public FactoryParameters(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /**
   * Constructs a <code>FactoryParameters</code> instance.
   *
   * @param url  joram ha url
   */
  public FactoryParameters(String url) {
    this.url = url;
    host = "";
    port = -1;
  }

  /**
   * Constructs an empty <code>FactoryParameters</code>.
   */
  public FactoryParameters() {}

//   public void toReference(Reference ref) {
//     toReference(ref, "cf");
//   }

  public void toReference(Reference ref, String prefix) {
//     if (prefix == null) prefix = "cf";

    ref.add(new StringRefAddr(prefix + ".host", getHost()));
    ref.add(new StringRefAddr(prefix + ".port",
                              new Integer(getPort()).toString()));

    ref.add(new StringRefAddr(prefix + ".url", getUrl()));

    ref.add(new StringRefAddr(prefix + ".clientID", clientID));
    ref.add(new StringRefAddr(prefix + ".TcpNoDelay",
                              new Boolean(TcpNoDelay).toString()));
    ref.add(new StringRefAddr(prefix + ".SoLinger",
                              new Integer(SoLinger).toString()));
    ref.add(new StringRefAddr(prefix + ".SoTimeout",
                              new Integer(SoTimeout).toString()));

    ref.add(new StringRefAddr(prefix + ".cnxT",
                              new Integer(connectingTimer).toString()));
    ref.add(new StringRefAddr(prefix + ".txT",
                              new Integer(txPendingTimer).toString()));
    ref.add(new StringRefAddr(prefix + ".cnxPT", 
                              new Integer(cnxPendingTimer).toString()));

    ref.add(new StringRefAddr(prefix + ".socketFactory", socketFactory));

    ref.add(new StringRefAddr(prefix + ".implicitAck", 
                              new Boolean(implicitAck).toString()));
    ref.add(new StringRefAddr(prefix + ".asyncSend", 
                              new Boolean(asyncSend).toString()));
    ref.add(new StringRefAddr(prefix + ".queueMessageReadMax", 
                              new Integer(queueMessageReadMax).toString()));
    ref.add(new StringRefAddr(prefix + ".topicAckBufferMax", 
                              new Integer(topicAckBufferMax).toString()));
    ref.add(new StringRefAddr(prefix + ".multiThreadSync", 
                              new Boolean(multiThreadSync).toString()));
    ref.add(new StringRefAddr(prefix + ".multiThreadSyncDelay", 
                              new Integer(multiThreadSyncDelay).toString()));
    ref.add(new StringRefAddr(prefix + ".multiThreadSyncThreshold", 
                              new Integer(multiThreadSyncThreshold).toString()));
    ref.add(new StringRefAddr(prefix + ".topicPassivationThreshold", 
                              new Integer(topicPassivationThreshold).toString()));
    ref.add(new StringRefAddr(prefix + ".topicActivationThreshold", 
                              new Integer(topicActivationThreshold).toString()));
    ref.add(new StringRefAddr(prefix + ".compressedMinSize", new Integer(compressedMinSize).toString()));
    ref.add(new StringRefAddr(prefix + ".compressionLevel", new Integer(compressionLevel).toString()));
    ref.add(new StringRefAddr(prefix + ".noAckedQueue", new Boolean(noAckedQueue).toString()));
    ref.add(new StringRefAddr(prefix + ".outLocalPort", new Integer(outLocalPort).toString()));
    if (outLocalAddress != null)
      ref.add(new StringRefAddr(prefix + ".outLocalAddress", outLocalAddress));
    if(!inInterceptors.isEmpty())
      ref.add(new StringRefAddr(prefix + ".inInterceptors", getListInInterceptorClassName()));
    if(outInterceptors!=null)
        ref.add(new StringRefAddr(prefix + ".outInterceptors", getListOutInterceptorClassName()));
  }

//   public void fromReference(Reference ref) {
//     fromReference(ref, "cf");
//   }

  public void fromReference(Reference ref, String prefix) {
//     if (prefix == null) prefix = "cf";

    host = (String) ref.get(prefix + ".host").getContent();
    port = new Integer((String) ref.get(prefix + ".port").getContent()).intValue();

    url = (String) ref.get(prefix + ".url").getContent();

    clientID = (String) ref.get(prefix + ".clientID").getContent();
    TcpNoDelay = new Boolean((String) ref.get(prefix + ".TcpNoDelay").getContent()).booleanValue();
    SoLinger = new Integer((String) ref.get(prefix + ".SoLinger").getContent()).intValue();
    SoTimeout = new Integer((String) ref.get(prefix + ".SoTimeout").getContent()).intValue();

    connectingTimer = new Integer((String) ref.get(prefix + ".cnxT").getContent()).intValue();
    txPendingTimer = new Integer((String) ref.get(prefix + ".txT").getContent()).intValue();
    cnxPendingTimer = new Integer((String) ref.get(prefix + ".cnxPT").getContent()).intValue();

    socketFactory = (String) ref.get(prefix + ".socketFactory").getContent();
    
    implicitAck = new Boolean((String) ref.get(prefix + ".implicitAck").getContent()).booleanValue();
    asyncSend = new Boolean((String) ref.get(prefix + ".asyncSend").getContent()).booleanValue();
    queueMessageReadMax = new Integer((String) ref.get(prefix + ".queueMessageReadMax").getContent()).intValue();
    topicAckBufferMax = new Integer((String) ref.get(prefix + ".topicAckBufferMax").getContent()).intValue();
    multiThreadSync = new Boolean((String) ref.get(prefix + ".multiThreadSync").getContent()).booleanValue();
    multiThreadSyncDelay = new Integer((String) ref.get(prefix + ".multiThreadSyncDelay").getContent()).intValue();
    multiThreadSyncThreshold = new Integer((String) ref.get(prefix + ".multiThreadSyncThreshold").getContent()).intValue();
    topicPassivationThreshold = new Integer((String) ref.get(prefix + ".topicPassivationThreshold").getContent()).intValue();
    topicActivationThreshold = new Integer((String) ref.get(prefix + ".topicActivationThreshold").getContent()).intValue();
    compressedMinSize = new Integer((String) ref.get(prefix + ".compressedMinSize").getContent()).intValue();
    compressionLevel = new Integer((String) ref.get(prefix + ".compressionLevel").getContent()).intValue();
    noAckedQueue = new Boolean((String) ref.get(prefix + ".noAckedQueue").getContent()).booleanValue();
    outLocalPort = new Integer((String) ref.get(prefix + ".outLocalPort").getContent()).intValue();
    RefAddr outLocalAddressRef = ref.get(prefix + ".outLocalAddress");
    if (outLocalAddressRef != null)
      outLocalAddress = (String) outLocalAddressRef.getContent();
    RefAddr interceptorRef = ref.get(prefix + ".inInterceptors");
    if(interceptorRef!=null){
    	setListInInterceptorClassName((String)interceptorRef.getContent());
    }
    interceptorRef = ref.get(prefix + ".outInterceptors");
    if(interceptorRef!=null){
    	setListOutInterceptorClassName((String)interceptorRef.getContent());
    }
  }

  public Hashtable code(Hashtable h, String prefix) {
    if (getHost() != null)
      h.put(prefix + ".host", getHost());
    h.put(prefix + ".port", new Integer(getPort()));

    if (getUrl() != null)
      h.put(prefix + ".url", getUrl());

    h.put(prefix + ".clientID", clientID);
    h.put(prefix + ".TcpNoDelay", new Boolean(TcpNoDelay));
    h.put(prefix + ".SoLinger", new Integer(SoLinger));
    h.put(prefix + ".SoTimeout", new Integer(SoTimeout));

    h.put(prefix + ".connectingTimer", new Integer(connectingTimer));
    h.put(prefix + ".txPendingTimer", new Integer(txPendingTimer));
    h.put(prefix + ".cnxPendingTimer", new Integer(cnxPendingTimer));

    h.put(prefix + ".socketFactory", socketFactory);
    
    h.put(prefix + ".implicitAck", new Boolean(implicitAck));
    h.put(prefix + ".asyncSend", new Boolean(asyncSend));
    h.put(prefix + ".queueMessageReadMax", new Integer(queueMessageReadMax));
    h.put(prefix + ".topicAckBufferMax", new Integer(topicAckBufferMax));
    h.put(prefix + ".multiThreadSync", new Boolean(multiThreadSync));
    h.put(prefix + ".multiThreadSyncDelay", new Integer(multiThreadSyncDelay));
    h.put(prefix + ".multiThreadSyncThreshold", new Integer(multiThreadSyncThreshold));
    h.put(prefix + ".topicPassivationThreshold", new Integer(topicPassivationThreshold));
    h.put(prefix + ".topicActivationThreshold", new Integer(topicActivationThreshold));
    h.put(prefix + ".compressedMinSize", new Integer(compressedMinSize));
    h.put(prefix + ".compressionLevel", new Integer(compressionLevel));
    h.put(prefix + ".noAckedQueue", new Boolean(noAckedQueue));
    h.put(prefix + ".outLocalPort", new Integer(outLocalPort));
    if (outLocalAddress != null)
      h.put(prefix + ".outLocalAddress", outLocalAddress);
    if(inInterceptors!=null)
      h.put(prefix + ".inInterceptors", getListInInterceptorClassName());
    if(outInterceptors!=null)
        h.put(prefix + ".outInterceptors", getListOutInterceptorClassName());
    return h;
  }

  public void decode(Hashtable h, String prefix) {
    host = (String) h.get(prefix + ".host");
    port = ((Integer) h.get(prefix + ".port")).intValue();

    url = (String) h.get(prefix + ".url");

    clientID = (String) h.get(prefix + ".clientID");
    TcpNoDelay = ((Boolean) h.get(prefix + ".TcpNoDelay")).booleanValue();
    SoLinger = ((Integer) h.get(prefix + ".SoLinger")).intValue();
    SoTimeout = ((Integer) h.get(prefix + ".SoTimeout")).intValue();

    connectingTimer = ((Integer) h.get(prefix + ".connectingTimer")).intValue();
    txPendingTimer = ((Integer) h.get(prefix + ".txPendingTimer")).intValue();
    cnxPendingTimer = ((Integer) h.get(prefix + ".cnxPendingTimer")).intValue();

    socketFactory = (String) h.get(prefix + ".socketFactory");
    
    implicitAck = ((Boolean) h.get(prefix + ".implicitAck")).booleanValue();
    asyncSend = ((Boolean) h.get(prefix + ".asyncSend")).booleanValue();
    queueMessageReadMax = ((Integer) h.get(prefix + ".queueMessageReadMax")).intValue();
    topicAckBufferMax = ((Integer) h.get(prefix + ".topicAckBufferMax")).intValue();
    multiThreadSync = ((Boolean) h.get(prefix + ".multiThreadSync")).booleanValue();
    multiThreadSyncDelay = ((Integer) h.get(prefix + ".multiThreadSyncDelay")).intValue();
    multiThreadSyncThreshold = ((Integer) h.get(prefix + ".multiThreadSyncThreshold")).intValue();
    topicPassivationThreshold = ((Integer) h.get(prefix + ".topicPassivationThreshold")).intValue();
    topicActivationThreshold = ((Integer) h.get(prefix + ".topicActivationThreshold")).intValue();
    compressedMinSize = ((Integer) h.get(prefix + ".compressedMinSize")).intValue();
    compressionLevel = ((Integer) h.get(prefix + ".compressionLevel")).intValue();
    noAckedQueue = ((Boolean) h.get(prefix + ".noAckedQueue")).booleanValue();
    outLocalPort = ((Integer) h.get(prefix + ".outLocalPort")).intValue();
    outLocalAddress = (String) h.get(prefix + ".outLocalAddress");
    String listInterceptorClassNames = (String) h.get(prefix + ".inInterceptors");
    if(listInterceptorClassNames!=null){
    	setListInInterceptorClassName(listInterceptorClassNames);
    }
    listInterceptorClassNames = (String) h.get(prefix + ".outInterceptors");
    if(listInterceptorClassNames!=null){
    	setListOutInterceptorClassName(listInterceptorClassNames);
    }
  }

  public Object clone() {
    Object clone = null;
    try {
      clone = super.clone();
    } catch (CloneNotSupportedException exc) {
      // Unreachable
    }
    return clone;
  }
  
  public String toString() {
	StringBuffer strbuf = new StringBuffer();
	
	strbuf.append('(').append(super.toString());
	strbuf.append(",host=").append(host);
  strbuf.append(",port=").append(port);
  strbuf.append(",url=").append(url);
  strbuf.append(",connectingTimer=").append(connectingTimer);
  strbuf.append(",txPendingTimer=").append(txPendingTimer);
  strbuf.append(",cnxPendingTimer=").append(cnxPendingTimer);
  strbuf.append(",implicitAck=").append(implicitAck);
  strbuf.append(",asyncSend=").append(asyncSend);
  strbuf.append(",queueMessageReadMax=").append(queueMessageReadMax);
  strbuf.append(",topicAckBufferMax=").append(topicAckBufferMax);
  strbuf.append(",multiThreadSync=").append(multiThreadSync);
  strbuf.append(",multiThreadSyncDelay=").append(multiThreadSyncDelay);
  strbuf.append(",multiThreadSyncThreshold=").append(multiThreadSyncThreshold);
  strbuf.append(",topicPassivationThreshold=").append(topicPassivationThreshold);
  strbuf.append(",topicActivationThreshold=").append(topicActivationThreshold);
  strbuf.append(",compressedMinSize=").append(compressedMinSize);
  strbuf.append(",compressionLevel=").append(compressionLevel);
  strbuf.append(",noAckedQueue=").append(noAckedQueue);
  strbuf.append(",outLocalAddress=").append(outLocalAddress);
  strbuf.append(",outLocalPort=").append(outLocalPort);
  
  if(inInterceptors!=null)
    strbuf.append(",inInterceptors=").append(getListInInterceptorClassName());
  if(outInterceptors!=null)
    strbuf.append(",outInterceptors=").append(getListOutInterceptorClassName());
  
  return strbuf.toString();
  }
  
  // Methods needed to manage interceptors in administration stuff.
  
  public void addInInterceptor(String pInterceptorClassName) {
	  addInterceptor(pInterceptorClassName, inInterceptors);
  }
  
  public void addOutInterceptor(String pInterceptorClassName) {
	  addInterceptor(pInterceptorClassName,outInterceptors);
  }
  
  private void addInterceptor(String pInterceptorClassName, List pInterceptors) {
    if (pInterceptorClassName != null) {
      try {
        pInterceptors.add((MessageInterceptor)Class.forName(pInterceptorClassName).newInstance());
      } catch(Throwable t) {
        t.printStackTrace();
      }
    }
  }
  
  public boolean removeInInterceptor(String pInterceptorClassName) {
	  return removeInterceptor(pInterceptorClassName, inInterceptors);
  }
  
  public boolean removeOutInterceptor(String pInterceptorClassName) {
	  return removeInterceptor(pInterceptorClassName, outInterceptors);
  }
  
  public boolean removeInterceptor(String pInterceptorClassName, List pInterceptors) {
    boolean removed = false;
    if (pInterceptorClassName != null) {
      for(Iterator it=pInterceptors.iterator();it.hasNext();) {
        if (pInterceptorClassName.equals(it.next().getClass().getName())) {
          removed=true;
          it.remove();
        }
      }
    }
    return removed;
  }
  
  // Methods needed to serialize interceptors in Reference (JNDI).
  
  private static final String INTERCEPTOR_CLASS_NAME_SEPARATOR =",";
  
  private String getListInInterceptorClassName(){
	  return getListInterceptorClassName(inInterceptors);
  }
  
  private String getListOutInterceptorClassName(){
	  return getListInterceptorClassName(outInterceptors);
  }
  
  private String getListInterceptorClassName(List pInterceptors) {
    if (!pInterceptors.isEmpty()) {
      StringBuffer cns = new StringBuffer();
      for (Iterator it= pInterceptors.iterator();it.hasNext();) {
        cns.append(it.next().getClass().getName());
        if (it.hasNext()) cns.append(INTERCEPTOR_CLASS_NAME_SEPARATOR);
      }
      return cns.toString();
    }
    return null;
  } 
  
  private void setListInInterceptorClassName(String pListInterceptorClassName){
	  setListInterceptorClassName(pListInterceptorClassName,inInterceptors);
  }
  
  private void setListOutInterceptorClassName(String pListInterceptorClassName){
	  setListInterceptorClassName(pListInterceptorClassName,outInterceptors);
  }
  
  private void setListInterceptorClassName(String pListInterceptorClassName, List pInterceptors) {
    if (pListInterceptorClassName != null) {
      StringTokenizer cns = new StringTokenizer(pListInterceptorClassName,INTERCEPTOR_CLASS_NAME_SEPARATOR);
      while (cns.hasMoreTokens()) {
        addInterceptor(cns.nextToken(), pInterceptors);
      }
    }
  }
  
  public void setParameters(Properties properties) {
    if ((properties == null) || (properties.isEmpty()))
      return;

    for (Enumeration keys = properties.keys(); keys.hasMoreElements();) {
      String name = (String) keys.nextElement();
      String value = (String) properties.getProperty(name);

      try {
        if ("cnxPendingTimer".equals(name)) {
          cnxPendingTimer = new Integer(value).intValue();
        } else if ("connectingTimer".equals(name)) {
          connectingTimer = new Integer(value).intValue();
        } else if ("asyncSend".equals(name)) {
          asyncSend = new Boolean(value).booleanValue();
        } else if ("txPendingTimer".equals(name)) {
          txPendingTimer = new Integer(value).intValue();
        } else if ("implicitAck".equals(name)) {
          implicitAck = new Boolean(value).booleanValue();
        } else if ("multiThreadSync".equals(name)) {
          multiThreadSync = new Boolean(value).booleanValue();
        } else if ("multiThreadSyncDelay".equals(name)) {
          multiThreadSyncDelay = new Integer(value).intValue();
        } else if ("multiThreadSyncThreshold".equals(name)) {
          multiThreadSyncThreshold = new Integer(value).intValue();
        } else if ("queueMessageReadMax".equals(name)) {
          queueMessageReadMax = new Integer(value).intValue();
        } else if ("topicAckBufferMax".equals(name)) {
          topicAckBufferMax = new Integer(value).intValue();
        } else if ("topicActivationThreshold".equals(name)) {
          topicActivationThreshold = new Integer(value).intValue();
        } else if ("topicPassivationThreshold".equals(name)) {
          topicPassivationThreshold = new Integer(value).intValue();
        } else if ("SoTimeout".equals(name)) {
          SoTimeout = new Integer(value).intValue();
        } else if ("TcpNoDelay".equals(name)) {
          TcpNoDelay = new Boolean(value).booleanValue();
        } else if ("SoLinger".equals(name)) {
          SoLinger = new Integer(value).intValue();
        } else if ("compressedMinSize".equals(name)) {
          compressedMinSize = new Integer(value).intValue();
        } else if ("compressionLevel".equals(name)) {
          compressionLevel = new Integer(value).intValue();
        } else if ("noAckedQueue".equals(name)) {
          noAckedQueue = new Boolean(value).booleanValue();
        } else if ("outLocalPort".equals(name)) {
          outLocalPort = new Integer(value).intValue();
        } else if ("outLocalAddress".equals(name)) {
          outLocalAddress = value;
        } else if ("clientID".equals(name)) {
          clientID = value;
        } else {
          logger.log(BasicLevel.ERROR,
                     "Could not set FactoryParameters <" + name + ", " + value + ">",
                     new Exception("Unknow parameter: " + name));
        }
      } catch (NumberFormatException exc) {
        logger.log(BasicLevel.ERROR,
                   "Could not set FactoryParameters <" + name + ", " + value + ">", exc);
      }
    }
  }

}
