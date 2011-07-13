/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package stomp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Simple test for STOMP Access to Joram
 */
public class StompTest {
  private static String host = "127.0.0.1";
  private static int port = 61613;
  private static String login = "anonymous";
  private static String passcode = "anonymous";
  
  private Socket socket;
  private ByteArrayOutputStream inputBuffer;
  
  private static final String STOMP_NULL = "\u0000";

  public static void main(String[] args) {
    try {
      host = System.getProperty("StompHostname", host);
      port = Integer.getInteger("StompPort", port).intValue();
      login = System.getProperty("StompLogin", login);
      passcode = System.getProperty("StompPasscode", passcode);
      
      StompTest test = new StompTest();
      test.testConnect();
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }

  public StompTest() throws Exception {
    socket = new Socket(host, port);
    inputBuffer = new ByteArrayOutputStream();
  }

  public void testConnect() throws Exception {
    inputBuffer = new ByteArrayOutputStream();

    socket = new Socket(host, port);
    System.out.println("Open connection to localhost:" + port);

    String connect = "CONNECT\n" +
      "login: " + login + "\n" + "passcode: " + passcode + "\n" +
      "request-id: 1\n" + "\n" + STOMP_NULL;
    sendFrame(connect);
    System.out.println("Send frame:" + connect + "\n------------------------------");
    
    String result = receiveFrame(10000);
    System.out.println("Receive frame:" + result + "\n------------------------------");

    String send = "SEND\n" +
      "destination:/queue/" + "#0.0.1026" + "\n\n" +
      "Hello World from STOMP client" + STOMP_NULL;
    sendFrame(send);
    System.out.println("Send frame:" + send + "\n------------------------------");

    String subscribe = "SUBSCRIBE\n" +
      "destination:/queue/" + "#0.0.1026" + "\n" + "ack:auto\n\n" + STOMP_NULL;
    sendFrame(subscribe);
    System.out.println("Send frame:" + subscribe + "\n------------------------------");

    try {
      while (true) {
        String msg1 = receiveFrame(10000);
        System.out.println("Receive frame:\n" + msg1 + "\n------------------------------");
      }
    } catch (SocketTimeoutException exc) {
    }

    String disconnect = "DISCONNECT\n" + "\n\n" + STOMP_NULL;
    sendFrame(disconnect);
    System.out.println("Send frame:" + disconnect);
  }



  public void sendFrame(String data) throws Exception {
    byte[] bytes = data.getBytes("UTF-8");
    OutputStream outputStream = socket.getOutputStream();
    for (int i = 0; i < bytes.length; i++) {
      outputStream.write(bytes[i]);
    }
    outputStream.flush();
  }

  public String receiveFrame(long timeOut) throws Exception {
    socket.setSoTimeout((int) timeOut);
    InputStream is = socket.getInputStream();
    int c = 0;
    for (; ;) {
      c = is.read();
      if (c < 0) {
        throw new IOException("socket closed.");
      } else if (c == 0) {
        c = is.read();
        byte[] ba = inputBuffer.toByteArray();
        inputBuffer.reset();
        return new String(ba, "UTF-8");
      } else {
        inputBuffer.write(c);
      }
    }
  }

}
