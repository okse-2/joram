/*
 * Copyright (C) 2000 ScalAgent Distributed Technologies
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

public class HttpResponse extends Response {
  /**
   * Status Code and Reason Phrase.
   * The Status-Code element is a 3-digit integer result code of the
   * attempt to understand and satisfy the request.
   * The first digit of the Status-Code defines the class of response:
   * <ul>
   * <li>1xx: Informational - Request received, continuing process
   * <li>2xx: Success - The action was successfully received, understood,
   * and accepted
   * <li>3xx: Redirection - Further action must be taken in order to
   * complete the request
   * <li>4xx: Client Error - The request contains bad syntax or cannot be
   * fulfilled
   * <li>5xx: Server Error - The server failed to fulfill an apparently
   * valid request
   */
  public static final int SC_CONTINUE = 100;
  public static final int SC_SWITCHING_PROTOCOLS = 101;

  public static final int SC_OK = 200;
  public static final int SC_CREATED = 201;
  public static final int SC_ACCEPTED = 202;
  public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
  public static final int SC_NO_CONTENT = 204;
  public static final int SC_RESET_CONTENT = 205;
  public static final int SC_PARTIAL_CONTENT = 206;

  public static final int SC_MULTIPLE_CHOICES = 300;
  public static final int SC_MOVED_PERMANENTLY = 301;
  public static final int SC_MOVED_TEMPORARILY = 302;
  public static final int SC_SEE_OTHER = 303;
  public static final int SC_NOT_MODIFIED = 304;
  public static final int SC_USE_PROXY = 305;

  public static final int SC_BAD_REQUEST = 400;
  public static final int SC_UNAUTHORIZED = 401;
  public static final int SC_PAYMENT_REQUIRED = 402;
  public static final int SC_FORBIDDEN = 403;
  public static final int SC_NOT_FOUND = 404;
  public static final int SC_METHOD_NOT_ALLOWED = 405;
  public static final int SC_NOT_ACCEPTABLE = 406;
  public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
  public static final int SC_REQUEST_TIMEOUT = 408;
  public static final int SC_CONFLICT = 409;
  public static final int SC_GONE = 410;
  public static final int SC_LENGTH_REQUIRED = 411;
  public static final int SC_PRECONDITION_FAILED = 412;
  public static final int SC_REQUEST_ENTITY_TOO_LARGE = 413;
  public static final int SC_REQUEST_URI_TOO_LARGE = 414;
  public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;

  public static final int SC_INTERNAL_SERVER_ERROR = 500;
  public static final int SC_NOT_IMPLEMENTED = 501;
  public static final int SC_BAD_GATEWAY = 502;
  public static final int SC_SERVICE_UNAVAILABLE = 503;
  public static final int SC_GATEWAY_TIMEOUT = 504;
  public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;

  /**
   * The HTTP status code associated with this Response.
   */
  protected int status = SC_OK;

  /**
   * Return the HTTP status code associated with this Response.
   */
  public int getStatus() {
    return (this.status);
  }

  /**
   * Set the HTTP status to be returned with this response.
   *
   * @param status The new HTTP status
   */
  public void setStatus(int status) {
    this.status = status;
    this.message = getStatusMessage(status);
  }

  /**
   * The error message set by <code>sendError()</code>.
   */
  protected String message = getStatusMessage(SC_OK);

  /**
   * Return the error message that was set with <code>sendError()</code>
   * for this Response.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns a default status message for the specified HTTP status code.
   *
   * @param status The status code for which a message is desired
   */
  protected static  String getStatusMessage(int status) {
    switch (status) {
    case SC_CONTINUE:
      return ("Continue");
    case SC_SWITCHING_PROTOCOLS:
      return ("Switching Protocols");
    case SC_OK:
      return ("OK");
    case SC_CREATED:
      return ("Created");
    case SC_ACCEPTED:
      return ("Accepted");
    case SC_NON_AUTHORITATIVE_INFORMATION:
      return ("Non-Authoritative Information");
    case SC_NO_CONTENT:
      return ("No Content");
    case SC_RESET_CONTENT:
      return ("Reset Content");
    case SC_PARTIAL_CONTENT:
      return ("Partial Content");
    case SC_MULTIPLE_CHOICES:
      return ("Multiple Choices");
    case SC_MOVED_PERMANENTLY:
      return ("Moved Permanently");
    case SC_MOVED_TEMPORARILY:
      return ("Moved Temporarily");
    case SC_SEE_OTHER:
      return ("See Other");
    case SC_NOT_MODIFIED:
      return ("Not Modified");
    case SC_USE_PROXY:
      return ("Use Proxy");
    case SC_BAD_REQUEST:
      return ("Bad Request");
    case SC_UNAUTHORIZED:
      return ("Unauthorized");
    case SC_PAYMENT_REQUIRED:
      return ("Payment Required");
    case SC_FORBIDDEN:
      return ("Forbidden");
    case SC_NOT_FOUND:
      return ("Not Found");
    case SC_METHOD_NOT_ALLOWED:
      return ("Method Not Allowed");
    case SC_NOT_ACCEPTABLE:
      return ("Not Acceptable");
    case SC_PROXY_AUTHENTICATION_REQUIRED:
      return ("Proxy Authentication Required");
    case SC_REQUEST_TIMEOUT:
      return ("Request Timeout");
    case SC_CONFLICT:
      return ("Conflict");
    case SC_GONE:
      return ("Gone");
    case SC_LENGTH_REQUIRED:
      return ("Length Required");
    case SC_PRECONDITION_FAILED:
      return ("Precondition Failed");
    case SC_REQUEST_ENTITY_TOO_LARGE:
      return ("Request Entity Too Large");
    case SC_REQUEST_URI_TOO_LARGE:
      return ("Request URI Too Large");
    case SC_UNSUPPORTED_MEDIA_TYPE:
      return ("Unsupported Media Type");
    case SC_INTERNAL_SERVER_ERROR:
      return ("Internal Server Error");
    case SC_NOT_IMPLEMENTED:
      return ("Not Implemented");
    case SC_BAD_GATEWAY:
      return ("Bad Gateway");
    case SC_SERVICE_UNAVAILABLE:
      return ("Service Unavailable");
    case SC_GATEWAY_TIMEOUT:
      return ("Gateway Timeout");
    case SC_HTTP_VERSION_NOT_SUPPORTED:
      return ("HTTP Version Not Supported");
    default:
      return ("HTTP Response Status " + status);
    }
  }

  /**
   * The content to be returned with this response.
   */
  protected StringBuffer content = null;

  /**
   * Return the content for this Response.
   */
  public StringBuffer getContent() {
    if (content == null)
      content = new StringBuffer();

    return content;
  }

  /**
   * Return the content length for this Response.
   */
  public int getContentLength() {
    if (content == null) return 0;

    return content.length();
  }

  public void recycle() {
    status = SC_OK;
    message = getStatusMessage(SC_OK);
    content = null;
    super.recycle();
  }
}
