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

public class Request {
  int id;

  public void setId(int id) {
    this.id = id;
  }

  InetAddress inet;

  public void setInet(InetAddress inet) {
    this.inet = inet;
  }

  public InetAddress getInet() {
    return inet;
  }

  int port;

  public void setPort(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  /**
   * The input stream associated with this Request.
   */
  InputStream input;

  /**
   * The protocol name and version associated with this Request.
   */
  protected String protocol = null;

  /**
   * Set the protocol name and version associated with this Request.
   *
   * @param protocol Protocol name and version
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   * Return the protocol and version used to make this Request.
   */
  public String getProtocol() {
    return (this.protocol);
  }

  private boolean valid = false;
  private boolean response = false;

  public synchronized boolean waitResponse() {
    while (!response) {
      try {
        wait();
      } catch (InterruptedException e) {}
    }
    response = false;
    return valid;
  }

  public synchronized void validResponse(boolean valid) {
    this.valid = valid;
    this.response = true;
    notify();
  }

  public void recycle() {
    input = null;
    inet = null;
    port = -1;
  }
}
