/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2009 ScalAgent Distributed Technologies
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
 * Contributor(s): Alex Porras (MediaOcean)
 */
package org.objectweb.joram.mom.proxies.tcp;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.StringTokenizer;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.Debug;

/**
 * Starts a SSLTCP entry point for MOM clients.
 */
public class SSLTcpProxyService extends TcpProxyService {
  /** logger */
  public static Logger logger = Debug.getLogger(SSLTcpProxyService.class.getName());

  private final static String CIPHER = "org.objectweb.joram.cipherList";
  private final static String KS = "org.objectweb.joram.keystore";
  private final static String KS_PASS = "org.objectweb.joram.keystorepass";
  private final static String KS_TYPE = "org.objectweb.joram.keystoretype";
  private final static String SSLCONTEXT = "org.objectweb.joram.sslCtx";

  private static final String MBEAN_NAME = "type=Connection,mode=tcp-ssl";

  /**
   * The proxy service reference (used to stop it).
   */
  private static SSLTcpProxyService proxyService;

  /**
   * Initializes the SSLTCP entry point by creating a
   * ssl server socket listening to the specified port.
   * 
   * @param args stringified listening port
   * @param firstTime <code>true</code> 
   * when the agent server starts.   
   */
  public static void init(String args, boolean firstTime) 
  throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "SSLTcpProxyService.init(" + args + ',' + firstTime + ')');

    int port = DEFAULT_PORT;
    String address = DEFAULT_BINDADDRESS;
    if (args != null) {
      StringTokenizer st = new StringTokenizer(args);      
      port = Integer.parseInt(st.nextToken());
      if (st.hasMoreTokens()) {
        address = st.nextToken();
      }
    }

    int backlog = AgentServer.getInteger(BACKLOG_PROP, DEFAULT_BACKLOG).intValue();

    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "SSLTcpProxyService.init() - binding to address " + address + ", port " + port);

    proxyService = new SSLTcpProxyService(port, backlog, address);
    proxyService.start();

  }

  public String getMBeanName() {
    return MBEAN_NAME;
  }

  public SSLTcpProxyService(int port, int backlog, String address) throws Exception {
    super(port, backlog, address);
  }

  private static ServerSocketFactory createServerSocketFactory() throws Exception {
    char[] keyStorePass =  System.getProperty(KS_PASS, "jorampass").toCharArray();
    String keystoreFile = System.getProperty(KS, "./joram_ks");
    String sslContext = System.getProperty(SSLCONTEXT, "SSL");
    String ksType = System.getProperty(KS_TYPE, "JKS");

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "SSLTcpProxyService.createServerSocketFactory:" + keystoreFile + ':' + new String(keyStorePass));

    KeyStore keystore = KeyStore.getInstance(ksType);
    keystore.load(new FileInputStream(keystoreFile), keyStorePass);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(keystore,keyStorePass);

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(keystore);       
    TrustManager[] trustManagers = tmf.getTrustManagers();

    SSLContext ctx = SSLContext.getInstance(sslContext);
    SecureRandom securerandom = SecureRandom.getInstance("SHA1PRNG");
    //    SecureRandom securerandom = null;
    ctx.init(kmf.getKeyManagers(),trustManagers,securerandom);

    return ctx.getServerSocketFactory();
  }

  protected ServerSocket createServerSocket(int port, int backlog, String address) throws Exception {
    ServerSocketFactory serverSocketFactory = createServerSocketFactory();

    SSLServerSocket serverSocket = null;
    if (address.equals("0.0.0.0")) {
      serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port, backlog);
    } else {
      serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port, backlog, InetAddress.getByName(address));
    }

    // require mutual authentication
    serverSocket.setNeedClientAuth(true);
    // request mutual authentication
    //serverSocket.setWantClientAuth(true);
    String[] cipherTable = getCipherList();
    if (cipherTable != null && cipherTable.length > 0)
      serverSocket.setEnabledCipherSuites(cipherTable);

    return serverSocket;
  }

  private static String [] getCipherList() throws Exception {
    String cipherList = System.getProperty(CIPHER,null);
    String[] cipherTable = null;
    if ( cipherList != null ) {
      StringTokenizer tokenizer = new StringTokenizer( cipherList,",");
      int tokens = tokenizer.countTokens();
      if (tokens > 0) {
        cipherTable = new String[tokens];
        while(tokenizer.hasMoreElements())
          cipherTable[--tokens] = tokenizer.nextToken();
      }
    }
    return cipherTable;
  }
}
