/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.kjndi.ksoap;

import java.io.IOException;
import java.io.InterruptedIOException;

import com.scalagent.ksoap.*;

/**
 * A <code>HttpConnection</code> class allows to send AbstractJmsRequest
 * and receive AbstractJmsReply
 */
public class HttpConnection {

  /** The http Connection. */
  protected HttpTransport httpConnect;
  /** Server's address. */
  protected String serverUrl;
  /** to print all SOAP messages. */
  protected boolean debug = false;
  /** to compress data. */
  protected boolean compress = false;


  public HttpConnection(String serverUrl) {
    this.serverUrl = serverUrl;

    // Create a httpConnection
    httpConnect = new HttpTransport(serverUrl,"ProxyService");
  }

  /**
   * sends a request to the server.
   *
   * @param request  The request to send.
   * @return The server reply.
   */
  public Object call(String action, String name, Object object) throws Exception {
      SoapObject sO;
      Object result = null;

      try {
        httpConnect.reset();

        if (debug) {
          System.out.println("JNDI HttpConnection.call(" + action + "," +  object + ")");
        }

        // Transform object in a SoapObject
        sO = ConversionSoapHelper.getSoapObject(action,name,object);

        // Send the request and wait the reply
        int timer=1;
        while (true) {
          try {
            result = httpConnect.call(sO);
            break;
          } catch (InterruptedIOException iIOE) {
          } catch (IOException ioE) {
            ioE.printStackTrace();
            //retry to connect to the proxy
            System.out.println("JNDI timer=" + timer);
            timer++;
            Thread.sleep(timer*1000);
            if (timer > 1)
              break;
            timer++;
          }
        }
      }
      // Catching an exception because of...
      catch (Exception e) {
        Exception jE = null;
        // ... a broken connection:
        if (e instanceof IOException)
          jE = new IllegalStateException("Connection is broken.");
        // ... an interrupted exchange:
        else if (e instanceof InterruptedException)
          jE = new InterruptedException("Interrupted request.");
        throw jE;
      }

      // Transform SoapObject in a reply
      Object reply = ConversionSoapHelper.getObject((SoapObject)result);
      if (debug) {
        System.out.println("JNDI HttpConnection.call : " + 
                           action + " reply=" + reply + ")");
      }
      // Finally, returning the reply:
      return reply;
    }

}
