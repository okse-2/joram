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

public class HttpRequest extends Request {
  /** The request method associated with this Request. */
  protected String method = null;

  /**
   * Set the HTTP request method used for this Request.
   *
   * @param method 	The request method
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * Return the HTTP request method used in this Request.
   *
   * @return		The request method
   */
  public String getMethod() {
    return method;
  }

  /**
   * The query string for this request.
   */
  protected String queryString = null;

  /**
   * Set the query string for this Request.
   *
   * @param query 	The query string
   */
  public void setQueryString(String query) {
    this.queryString = query;
  }

  /**
   * Return the query string associated with this request.
   *
   * @return		The query string
   */
  public String getQueryString() {
    return (queryString);
  }

  /**
   * The request URI associated with this request.
   */
  protected String requestURI = null;

  /**
   * Set the unparsed request URI for this Request.
   *
   * @param uri		The request URI
   */
  public void setRequestURI(String uri) {
    this.requestURI = uri;
  }

  /**
   * Return the request URI for this request.
   *
   * @return		The request URI
   */
  public String getRequestURI() {
    return (requestURI);
  }

  public void recycle() {
    method = null;
    queryString = null;
    requestURI = null;
    super.recycle();
  }
}
