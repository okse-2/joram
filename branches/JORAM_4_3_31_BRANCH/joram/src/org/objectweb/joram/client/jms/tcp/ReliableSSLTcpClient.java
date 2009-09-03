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
package org.objectweb.joram.client.jms.tcp;

import fr.dyade.aaa.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;
import javax.jms.*;

import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

public class ReliableSSLTcpClient extends ReliableTcpClient {

  private final static String CIPHER = "org.objectweb.joram.cipherList";
  private final static String KS = "org.objectweb.joram.keystore";
  private final static String KS_PASS = "org.objectweb.joram.keystorepass";
  private final static String KS_TYPE = "org.objectweb.joram.keystoretype";
  private final static String SSLCONTEXT = "org.objectweb.joram.sslCtx";

  public ReliableSSLTcpClient() {
    super();
  }

  protected Socket createSocket(String hostName, int port) 
    throws Exception {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "ReliableSSLTcpClient.createSocket(" + 
        hostName+"," + port + ")");

    SocketFactory socketFactory = createSocketFactory();
    return socketFactory.createSocket(hostName, port);
  }

  private static SocketFactory createSocketFactory() 
    throws Exception {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "ReliableSSLTcpClient.createSocketFactory()");

    char[] keyStorePass =  System.getProperty(KS_PASS,"jorampass").toCharArray();
    String keystoreFile = System.getProperty(KS,"./joram_ks");
    String sslContext = System.getProperty(SSLCONTEXT,"SSL");
    String ksType = System.getProperty(KS_TYPE,"JKS");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "SSLTcpProxyService.createSocketFactory : keystoreFile=" + 
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
    
    return (SocketFactory) ctx.getSocketFactory();
  }
}
