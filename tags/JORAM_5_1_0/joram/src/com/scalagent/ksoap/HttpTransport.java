/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
package com.scalagent.ksoap;

import java.io.*;
import javax.microedition.io.*;

import org.kxml.*;
import org.kxml.io.*;
import org.kxml.parser.*;
import org.kxml.wap.*;

public class HttpTransport {

  String url;
  String soapAction = "\"\"";

  SoapEnvelope requestEnvelope = new SoapEnvelope();
  SoapEnvelope responseEnvelope = new SoapEnvelope();

  HttpConnection connection;
  OutputStream os;
  InputStream is;
  InputStreamReader reader;

  private boolean connected = false;
  public String requestDump;
  public String responseDump;
  
  public HttpTransport(String url, String soapAction) {
    this.url = url;
    this.soapAction = soapAction;
  }

  public Object call(SoapObject obj) throws IOException, InterruptedException {
    requestEnvelope.setBody(obj);
    call();
    if (responseEnvelope.getBody() instanceof SoapFault)
      throw((SoapFault)responseEnvelope.getBody());
    return responseEnvelope.getBody();
  }

  public void call() throws IOException {
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,"\n>> HttpTransport.call()");

    ByteArrayOutputStream bos = new ByteArrayOutputStream ();
    XmlWriter xw = new XmlWriter(new OutputStreamWriter (bos));
    requestEnvelope.write(xw);
    xw.flush();
    bos.write('\r');
    bos.write('\n');
    byte [] requestData = bos.toString().getBytes();
    bos = null;
    xw = null;

    if (KSoapTracing.dbg) {
      requestDump = new String(requestData);
      KSoapTracing.log(KSoapTracing.DEBUG,"Request : " + requestDump);
    }

    try {
      connected = true;
      connection = (HttpConnection)Connector.open(url,Connector.READ_WRITE,true);
      connection.setRequestProperty("Content-Type","text/xml");
      connection.setRequestProperty("Content-Length",""+requestData.length);
      connection.setRequestProperty("User-Agent","kSOAP/1.0");
      connection.setRequestProperty("Cookie","");

      connection.setRequestMethod(HttpConnection.POST);

      os = connection.openOutputStream();
      os.write(requestData,0,requestData.length);

      for (int i= 0; i<5; i++) {
        try {
          os.close ();
          break;
        } catch (Throwable e) {
          //System.out.println("EXCEPTION CLOSE ++++++ " + e);
        }
      }

      requestData = null;
      XmlParser xp = null;

      is = connection.openInputStream();

      while (true) {
        try {
          if (KSoapTracing.dbg) {
            bos = new ByteArrayOutputStream();
            byte [] buf = new byte [256];
            
            while (true) {
              int rd = is.read(buf, 0, 256);
              if (rd == -1) break;
              bos.write(buf,0,rd);
            }
            buf = bos.toString().getBytes();
            responseDump = new String(buf);
            KSoapTracing.log(KSoapTracing.DEBUG,
                             "HttpTransport.call()  Reply : " + responseDump);
            is.close();
            is = new ByteArrayInputStream(buf);
          }
          
          reader = new InputStreamReader(is);
          xp = new XmlParser(reader);
          responseEnvelope.read(xp);

          if (KSoapTracing.dbg)
            KSoapTracing.log(KSoapTracing.DEBUG,
                             "HttpTransport.call() responce = " + responseEnvelope.getBody());
          break;
        } catch (Throwable e) {
          if (e instanceof InterruptedIOException) {
            //System.out.println("+++++++ EXCEPTION");
          } else {
            KSoapTracing.log(KSoapTracing.ERROR,e.toString());
            if (e instanceof IOException)
              throw (IOException)e;
          }
        }
      }

      if (KSoapTracing.dbg)
        KSoapTracing.log(KSoapTracing.DEBUG,
                         "<< HttpTransport.call()");
    } finally {
      if (KSoapTracing.dbg)
        KSoapTracing.log(KSoapTracing.DEBUG,
                         "<< HttpTransport.call()" + connected +"\n\n");
      if (!connected) {
        KSoapTracing.log(KSoapTracing.ERROR,"HttpTransport.call() EXCEPTION");
        throw new InterruptedIOException();
      }
      reset();
    }
  }

  public void reset() {
    connected = false;
    if (reader != null) {
      try {
        reader.close();
      } catch (Throwable e) {}
      reader = null;
    }
    if (is != null) {
      try {
        is.close();
      } catch (Throwable e) {}
      is = null;
    }
    if (connection != null) {
      try {
        connection.close();
      } catch (Throwable e) {}
      connection = null;
    }
  }
  
}
