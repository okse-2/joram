/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
package com.scalagent.kjoram.ksoap;

import com.scalagent.kjoram.Driver;
import com.scalagent.kjoram.FactoryParameters;
import com.scalagent.kjoram.jms.*;

import java.io.IOException;
import java.util.Vector;

import com.scalagent.kjoram.excepts.IllegalStateException;
import com.scalagent.kjoram.excepts.JMSException;
import com.scalagent.kjoram.excepts.JMSSecurityException;
import com.scalagent.ksoap.*;

/**
 * A <code>SoapConnection</code> links a Joram client and a Joram platform
 * with HTTP connections.
 * <p>
 * Requests and replies travel through the connections in SOAP (XML) format.
 */
public class SoapConnection implements com.scalagent.kjoram.ConnectionItf {
  /** String URL of the SOAP service this object communicates with. */
  private String serviceUrl = null;
  /** Identifier of the connection. */
  private int cnxId;
  /** */
  private HttpConnection httpConnect = null;

  private String name = null;

  /**
   * Creates a <code>SoapConnection</code> instance.
   *
   * @param params  Factory parameters.
   * @param name  Name of user.
   * @param password  Password of user.
   *
   * @exception JMSSecurityException  If the user identification is incorrrect.
   * @exception IllegalStateException  If the server is not reachable.
   */
  public SoapConnection(FactoryParameters factParams, String name,
                        String password) throws JMSException {
    connect(factParams, name, password);
    this.name = name;
  }

  public String getUserName() {
    return name;
  }

  /**
   * Creates a driver for the connection.
   *
   * @param cnx  The calling <code>Connection</code> instance.
   */
  public Driver createDriver(com.scalagent.kjoram.Connection cnx) {
    Driver driver = new SoapDriver(cnx,serviceUrl,cnxId);
    driver.setDaemon(true);
    return driver;
  }

  /**
   * Sending a JMS request through the SOAP protocol.
   *
   * @exception IllegalStateException  If the SOAP service fails.
   */
  public synchronized void send(AbstractJmsRequest request)
                           throws IllegalStateException {
    try {
      if (httpConnect == null)
        httpConnect = new HttpConnection(serviceUrl);
      httpConnect.call(request,name,cnxId);
    } catch (Exception exc) {
      httpConnect = null;
      throw new IllegalStateException("The SOAP call failed: "
                                      + exc.getMessage());
    }
  }

  /** Closes the <code>SoapConnection</code>. */
  public void close() { 
    try {
      send(new CnxCloseRequest());
    }
    catch (Exception exc) {}
  }

  /**
   * Actually tries to set a first SOAP connection with the server.
   *
   * @param params  Factory parameters.
   * @param name  The user's name.
   * @param password  The user's password.
   *
   * @exception JMSSecurityException  If the user identification is incorrrect.
   * @exception IllegalStateException  If the SOAP service fails.
   */
  private void connect(FactoryParameters factParams, String name,
                       String password)
               throws JMSException {
    // Setting the timer values:
    long startTime = System.currentTimeMillis();
    long endTime = startTime + factParams.connectingTimer * 1000;
    long currentTime;
    long nextSleep = 2000;
    boolean tryAgain;
    int attemptsC = 0;
    Object resp;
    String error;

    serviceUrl = "http://" + factParams.getHost() + ":" + 
      factParams.getPort() + "/soap/servlet/rpcrouter";
    HttpTransport httpTransport = new HttpTransport(serviceUrl,"ProxyService");

    while (true) {
      attemptsC++;

      try {
        SoapObject sO = ConversionSoapHelper.getSoapObject(
          new SetCnx(name,password,new Integer(factParams.soapCnxPendingTimer)));
        resp = httpTransport.call(sO);
        cnxId = ConversionSoapHelper.getSetCnx((SoapObject)resp);

        if (cnxId == -1) {
          throw new JMSSecurityException("Can't open the connection with"
                                         + " the server on host "
                                         + factParams.getHost()
                                         + " and port "
                                         + factParams.getPort()
                                         + ": invalid user identification.");
        }
        break;
      } catch (Exception exc) {
        error = exc.getMessage();
        tryAgain = true;
      }
      // Trying again to connect:
      if (tryAgain) {
        currentTime = System.currentTimeMillis();
        // Keep on trying as long as timer is ok:
        if (currentTime < endTime) {

          if (currentTime + nextSleep > endTime)
            nextSleep = endTime - currentTime;

          // Sleeping for a while:
          try {
            Thread.sleep(nextSleep);
          }
          catch (InterruptedException intExc) {}

          // Trying again!
          nextSleep = nextSleep * 2;
          continue;
        }
        // If timer is over, throwing an IllegalStateException:
        else {
          long attemptsT = (System.currentTimeMillis() - startTime) / 1000;
          throw new IllegalStateException("Could not open the connection"
                                          + " with server on host "
                                          + factParams.getHost()
                                          + " and port "
                                          + factParams.getPort()
                                          + " after " + attemptsC
                                          + " attempts during "
                                          + attemptsT + " secs: "
                                          + error);
        }
      }
    }
  }
}
