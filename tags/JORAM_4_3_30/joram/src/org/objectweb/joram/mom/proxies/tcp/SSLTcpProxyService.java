/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2006 ScalAgent Distributed Technologies
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

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.util.*;

import java.net.*;
import java.util.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLContext;

import org.objectweb.joram.mom.MomTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Starts a SSLTCP entry point for MOM clients.
 */
public class SSLTcpProxyService extends TcpProxyService {

  private final static String CIPHER = "org.objectweb.joram.cipherList";
  private final static String KS = "org.objectweb.joram.keystore";
  private final static String KS_PASS = "org.objectweb.joram.keystorepass";
  private final static String KS_TYPE = "org.objectweb.joram.keystoretype";
  private final static String SSLCONTEXT = "org.objectweb.joram.sslCtx";


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
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "SSLTcpProxyService.init(" + 
        args + ',' + firstTime + ')');

    int port =  DEFAULT_PORT;;
    String address = DEFAULT_BINDADDRESS;
    if (args != null) {
      StringTokenizer st = new StringTokenizer(args);      
      port = Integer.parseInt(st.nextToken());
      if (st.hasMoreTokens()) {
        address = st.nextToken();
      }
    }
    
    int backlog = Integer.getInteger(BACKLOG_PROP, DEFAULT_BACKLOG).intValue();

    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    ServerSocket serverSocket;

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "SSLTcpProxyService.init() - binding to address " + address + ", port " + port);

    serverSocket = createServerSocket(port, backlog, address);
    int poolSize = Integer.getInteger(POOL_SIZE_PROP, DEFAULT_POOL_SIZE).intValue();

    int timeout = Integer.getInteger(SO_TIMEOUT_PROP, DEFAULT_SO_TIMEOUT).intValue();
    
    proxyService = new SSLTcpProxyService(serverSocket, poolSize, timeout);
    proxyService.start();
  }
  
  public SSLTcpProxyService(ServerSocket serverSocket,
                            int poolSize,
                            int timeout) {
    super(serverSocket,poolSize,timeout);
  }

  private static ServerSocketFactory createServerSocketFactory() 
    throws Exception {
    char[] keyStorePass =  System.getProperty(KS_PASS, "jorampass").toCharArray();
    String keystoreFile = System.getProperty(KS, "./joram_ks");
    String sslContext = System.getProperty(SSLCONTEXT, "SSL");
    String ksType = System.getProperty(KS_TYPE, "JKS");

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              "SSLTcpProxyService.createServerSocketFactory:" +
                              keystoreFile + ':' + keyStorePass);

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
    
    return (ServerSocketFactory) ctx.getServerSocketFactory();
  }

  private static ServerSocket createServerSocket(int port, int backlog, String address) throws Exception {
    ServerSocketFactory serverSocketFactory = createServerSocketFactory();

    SSLServerSocket serverSocket = null;
    if (address.equals("0.0.0.0")) {
      serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port, backlog);
    } else {
      serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port, backlog, InetAddress.getByName(address));
    }

    // require mutual authentification
    serverSocket.setNeedClientAuth(true);
    // request mutual authentification
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
