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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram.tcp;

import fr.dyade.aaa.joram.Connection;
import fr.dyade.aaa.joram.Driver;
import fr.dyade.aaa.joram.FactoryParameters;
import fr.dyade.aaa.mom.jms.AbstractJmsRequest;

import java.io.*;
import java.net.*;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

/**
 * A <code>TcpConnection</code> links a Joram client and a Joram platform
 * with a TCP socket.
 * <p>
 * Requests and replies travel through the socket after serialization.
 */
public class TcpConnection implements fr.dyade.aaa.joram.ConnectionItf 
{ 
  /** Connection socket. */ 
  private Socket socket = null;
  /** Connection data output stream. */
  private DataOutputStream dos = null;
  /** Connection data input stream. */
  private DataInputStream dis = null;
  /** Connection object output stream. */
  private ObjectOutputStream oos = null;
  /** Connection object input stream. */
  private ObjectInputStream ois = null;

  /**
   * Creates a <code>TcpConnection</code> instance.
   *
   * @param params  Factory parameters.
   * @param name  Name of user.
   * @param password  Password of user.
   *
   * @exception JMSSecurityException  If the user identification is incorrrect.
   * @exception IllegalStateException  If the server is not reachable.
   */
  public TcpConnection(FactoryParameters params, String name,
                       String password) throws JMSException
  {
    connect(params, name, password);
  }

  
  /**
   * Creates a driver for the connection.
   *
   * @param cnx  The calling <code>Connection</code> instance.
   */
  public Driver createDriver(Connection cnx)
  {
    Driver driver = new TcpDriver(cnx, ois);
    driver.setDaemon(true);
    return driver;
  }

  /**
   * Sending a JMS request through the TCP connection.
   *
   * @exception IllegalStateException  If the connection is broken.
   */
  public synchronized void send(AbstractJmsRequest request)
                           throws IllegalStateException
  {
    try {
      oos.writeObject(request);
      oos.reset();
    }
    catch (IOException exc) {
      IllegalStateException jmsExc;
      jmsExc = new IllegalStateException("The connection is broken.");
      jmsExc.setLinkedException(exc);
      throw jmsExc;
    }
  }

  /** Closes the TCP connection. */
  public void close()
  {
    try {
      dos.close();
    }
    catch (IOException exc) {}
    try {
      dis.close();
    }
    catch (IOException exc) {}
    try {
      oos.close();
    }
    catch (IOException exc) {}
    try {
      ois.close();
    }
    catch (IOException exc) {}
    try {
      socket.close();
    }
    catch (IOException exc) {}
  }

  /**
   * Actually tries to open the TCP connection with the server.
   *
   * @param params  Factory parameters.
   * @param name  The user's name.
   * @param password  The user's password.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not reachable.
   */
  private void connect(FactoryParameters params, String name,
                       String password) throws JMSException
  {
    // Setting the timer values:
    long startTime = System.currentTimeMillis();
    long endTime = startTime + params.connectingTimer * 1000;
    long currentTime;
    long nextSleep = 2000;
    int attemptsC = 0;

    InetAddress serverAddr = null;
    int serverPort = params.getPort();

    while (true) {
      attemptsC++;

      try {
        // Opening the connection with the server:
        serverAddr = InetAddress.getByName(params.getHost());
        socket = new Socket(serverAddr, serverPort);

        if (socket != null) {
          socket.setTcpNoDelay(true);
          socket.setSoTimeout(0);
          socket.setSoLinger(true, 1000);

          dos = new DataOutputStream(socket.getOutputStream());
          dis = new DataInputStream(socket.getInputStream());
   
          // Sending the connection request to the server:    
          dos.writeUTF("USER: " + name + " " + password);
          String reply = (String) dis.readUTF();

          // Processing the server's reply:
          int status = Integer.parseInt(reply.substring(0,1));
          int index = reply.indexOf("INFO: ");
          String info = null;
          if (index != -1)
            info = reply.substring(index + 6);
 
          // If successfull, opening the connection with the client proxy: 
          if (status == 0) {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            break;
          }
          // If unsuccessful because of an user id error, throwing a
          // JMSSecurityException:
          if (status == 1) 
            throw new JMSSecurityException("Can't open the connection with the"
                                           + " server "
                                           + serverAddr.toString()
                                           + " on port " + serverPort
                                           + ": " + info);
        }
        // If socket can't be created, throwing an IO exception:
        else
          throw new IOException("Can't create the socket connected to server "
                                + serverAddr.toString()
                                + ", port " + serverPort);
      }
      catch (Exception exc) {
        // IOExceptions notify that the connection could not be opened,
        // possibly because the server is not listening: trying again.
        if (exc instanceof IOException) {
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
            IllegalStateException jmsExc =
              new IllegalStateException("Could not open the connection"
                                        + " with server "
                                        + serverAddr
                                        + " on port " + serverPort
                                        + " after " + attemptsC
                                        + " attempts during "
                                        + attemptsT + " secs: server is"
                                        + " not listening" );
            jmsExc.setLinkedException(exc);
            throw jmsExc;
          }
        }
        // UnknownHostException says that the server's host is unknown.
        else if (exc instanceof UnknownHostException) {
          IllegalStateException jmsExc =
            new IllegalStateException("Server's host is unknown.");
          jmsExc.setLinkedException(exc);
          throw jmsExc;
        }
        // Forwarding JMS security exceptions:
        else if (exc instanceof JMSSecurityException)
          throw (JMSSecurityException) exc;
      }
    }
  }
}
