/*
 * Copyright (C) 2001 - SCALAGENT
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
 */
package fr.dyade.aaa.ip;

import java.io.*;
import java.net.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;

/**
 * A specific <code>ServletAgent</code> proxy for HTTP protocol.
 */
public class HttpServletAgent extends ServletAgent {
  /** RCS version number of this file: $Revision: 1.4 $ */
  public static final String RCS_VERSION="@(#)$Id: HttpServletAgent.java,v 1.4 2004-03-16 10:03:45 fmaistre Exp $"; 

  /**
   * Creates a ServletAgent proxy.
   */
  public HttpServletAgent() {
    this(AgentServer.getServerId(), null);
  }

  /**
   * Creates a ServletAgent proxy.
   */
  public HttpServletAgent(String name) {
    this(AgentServer.getServerId(), name);
  }

  /**
   * Creates a ServletAgent proxy.
   */
  public HttpServletAgent(short to, String name) {
    super(to, name);
  }

  /**
   * Provides a string image for this object.
   */
  public String toString() {
    StringBuffer strBuf = new StringBuffer();

    strBuf.append("(").append(super.toString());
    strBuf.append(",port=").append(port);
    strBuf.append(")");

    return strBuf.toString();
  }

  protected Request createRequest() {
    return ((Request) new HttpRequest());
  }

  protected Response createResponse() {
    return ((Response) new HttpResponse());
  }


  /**
   * Parse the incoming Http request and set the corresponding request
   * properties. The current implementation is basic, it only take in
   * account the request-line before calling parseHeader method.
   *
   * @param request The current request
   */
  protected void parseRequest(Request request) throws Exception {
    // Parse the incoming request line
    String line = read(request.input);
    if (line == null)
      throw new Exception("parseRequest.read");
    StringTokenizer st = new StringTokenizer(line);

    String method = null;
    try {
      method = st.nextToken();
    } catch (NoSuchElementException e) {
      method = null;
    }

    String uri = null;
    try {
      uri = st.nextToken();
    } catch (NoSuchElementException e) {
      uri = null;
    }

    String protocol = null;
    try {
      protocol = st.nextToken();
    } catch (NoSuchElementException e) {
      protocol = "HTTP/0.9";
    }

    // Validate the incoming request line
    if (method == null) {
      throw new Exception("httpProcessor.parseRequest.method");
    } else if (uri == null) {
      throw new Exception("httpProcessor.parseRequest.uri");
    }

    // Parse any query parameters out of the request URI
    int idx = uri.indexOf('?');
    if (idx >= 0) {
      ((HttpRequest) request).setQueryString(uri.substring(idx + 1));
      uri = uri.substring(0, idx);
    } else {
      ((HttpRequest) request).setQueryString(null);
    }

    // Set the corresponding request properties
    ((HttpRequest) request).setMethod(method);
    ((HttpRequest) request).setProtocol(protocol);
    ((HttpRequest) request).setRequestURI(uri);
  }

  /**
   * Notify request handler. The current implementation just send a
   * RequestNot notification to proxy agent.
   *
   * @param request The current request
   */
  protected void sendRequest(Request request) throws Exception {
    sendTo(getId(), new RequestNot(request));
  }

  /**
   * Reply to client. The current implementation just write the
   * response content to the output stream.
   *
   * @param req		The current request
   * @param resp	The current response
   */
  protected void finishResponse(Request req,
                                Response resp) throws Exception {
    HttpRequest request = (HttpRequest) req;
    HttpResponse response = (HttpResponse) resp;

    // Prepare a suitable output writer
    final PrintWriter writer = new PrintWriter(
      new OutputStreamWriter(response.output,
                             response.getCharacterEncoding()));

    // Send the HTTP response headers
    if (! "HTTP/0.9".equals(request.getProtocol())) {
      // Send the "Status:" header
      writer.print(request.getProtocol());
      writer.print(" ");
      writer.print(response.getStatus());
      if (response.getMessage() != null) {
        writer.print(" ");
        writer.print(response.getMessage());
      }
      writer.print("\r\n");
      // Send the content-length and content-type headers (if any)
      if (response.getContentType() != null) {
        writer.print("Content-Type: " + response.getContentType() + "\r\n");
      }
      if (response.getContentLength() >= 0) {
        writer.print("Content-Length: " + response.getContentLength() +
                     "\r\n");
      }
      // Send a terminating blank line to mark the end of the headers
      writer.print("\r\n");
      writer.flush();
    }

    // If an HTTP error >= 400 has been created with no content,
    // attempt to create a simple error message
    if ((response.getStatus() >= HttpResponse.SC_BAD_REQUEST) &&
        (response.getContentType() == null) &&
        (response.getContentLength() == 0)) {
      response.setContentType("text/html");
      writer.println("<html>");
      writer.println("<head>");
      writer.println("<title>Error Report</title>");
      writer.println("</head>");
      writer.println("<br><br>");
      writer.println("<body>");
      writer.println("<h1>HTTP Status ");
      writer.print(response.getStatus());
      writer.print(" - ");
      if (response.getMessage() != null)
        writer.print(response.getMessage());
      else
        writer.print(HttpResponse.getStatusMessage(response.getStatus()));
      writer.println("</h1>");
      writer.println("</body>");
      writer.println("</html>");
    } else {
      // Attempt to write the content
      writer.print(response.getContent().toString());
    }
    writer.flush();
  }

  public void service(Request request,
                      Response response) throws Exception {
  }

  /**
   * Read a line from the specified input stream, and strip off the
   * trailing carriage return and newline (if any).  Return the remaining
   * characters that were read as a String.
   *
   * @param input The input stream connected to our socket
   *
   * @returns The line that was read, or <code>null</code> if end-of-file
   *  was encountered
   *
   * @exception IOException if an input/output error occurs
   */
  private String read(InputStream input) throws IOException {
    StringBuffer sb = new StringBuffer();
    while (true) {
      int ch = input.read();
      if (ch < 0) {
        if (sb.length() == 0) {
          return (null);
        } else {
          break;
        }
      } else if (ch == '\r') {
        continue;
      } else if (ch == '\n') {
        break;
      }
      sb.append((char) ch);
    }
    return (sb.toString());

  }
}
