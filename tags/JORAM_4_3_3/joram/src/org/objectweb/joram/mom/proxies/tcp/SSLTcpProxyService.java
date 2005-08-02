/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s): 
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

  private final static String CIPHER = "cipherList";
  private final static String KS = "keystore";
  private final static String KS_PASS = "keystore_pass";
  private final static String KS_TYPE = "keystore_type";
  private final static String SSLCONTEXT = "sslCtx";


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
    int port;
    if (args != null) {
      StringTokenizer st = new StringTokenizer(args);      
      port = Integer.parseInt(st.nextToken());
    } else {
      port = DEFAULT_PORT;
    }

    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    ServerSocket serverSocket = createServerSocket(port);

    int poolSize = Integer.getInteger(
      POOL_SIZE_PROP, DEFAULT_POOL_SIZE).intValue();

    int timeout = Integer.getInteger(
      SO_TIMEOUT_PROP, DEFAULT_SO_TIMEOUT).intValue();
    
    proxyService = new SSLTcpProxyService(
      serverSocket, poolSize, timeout);
    proxyService.start();
  }
  
  public SSLTcpProxyService(ServerSocket serverSocket,
                            int poolSize,
                            int timeout) {
    super(serverSocket,poolSize,timeout);
  }

  private static ServerSocketFactory createServerSocketFactory() 
    throws Exception {
    char[] keyStorePass =  System.getProperty(KS_PASS,"jorampass").toCharArray();
    String keystoreFile = System.getProperty(KS,"./joram_ks");
    String sslContext = System.getProperty(SSLCONTEXT,"SSL");
    String ksType = System.getProperty(KS_TYPE,"JKS");

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "SSLTcpProxyService.createServerSocketFactory : keystoreFile=" + 
        keystoreFile);

    KeyStore keystore = KeyStore.getInstance(ksType);
    keystore.load(new FileInputStream(keystoreFile),keyStorePass);
    
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

  private static ServerSocket createServerSocket(int port) throws Exception {
    ServerSocketFactory serverSocketFactory = createServerSocketFactory();
    
    int backlog = Integer.getInteger(
      BACKLOG_PROP, DEFAULT_BACKLOG).intValue();

    SSLServerSocket serverSocket = 
      (SSLServerSocket) serverSocketFactory.createServerSocket(port, backlog);
    // requie mutual authentification
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
