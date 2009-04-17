/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.tcp;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

public class ReliableSSLTcpClient extends ReliableTcpClient {

  private static Logger logger = Debug.getLogger(ReliableSSLTcpClient.class.getName());

  private final static String KS = "org.objectweb.joram.keystore";
  private final static String KS_PASS = "org.objectweb.joram.keystorepass";
  private final static String KS_TYPE = "org.objectweb.joram.keystoretype";
  private final static String SSLCONTEXT = "org.objectweb.joram.sslCtx";

  public ReliableSSLTcpClient() {
    super();
  }

  protected Socket createSocket(String hostname, int port) throws Exception {
    InetAddress outLocalAddr = null;
    String outLocalAddrStr = params.outLocalAddress;
    if (outLocalAddrStr != null)
      outLocalAddr = InetAddress.getByName(outLocalAddrStr);

    int outLocalPort = params.outLocalPort;
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ReliableSSLTcpClient[" + identity + ',' + key + "].createSocket(" +
                 hostname + "," + port + ") on interface " + outLocalAddrStr + ":" + outLocalPort);

    SocketFactory socketFactory = createSocketFactory();
    // AF: Be careful SSLSocketFactory don't allow to use ConnectTimeout
    Socket socket =  socketFactory.createSocket(hostname, port, outLocalAddr, outLocalPort);

    return socket;
  }

  private static SocketFactory createSocketFactory() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ReliableSSLTcpClient.createSocketFactory()");

    // AF: TODO these parameters should be in FactoryParameters
    char[] keyStorePass =  System.getProperty(KS_PASS,"jorampass").toCharArray();
    String keystoreFile = System.getProperty(KS,"./joram_ks");
    String sslContext = System.getProperty(SSLCONTEXT,"SSL");
    String ksType = System.getProperty(KS_TYPE,"JKS");

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "SSLTcpProxyService.createSocketFactory : keystoreFile=" + keystoreFile);
    
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
    
    return ctx.getSocketFactory();
  }
}
