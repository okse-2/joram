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

public class Response {
  int id;

  public void setId(int id) {
    this.id = id;
  }

  OutputStream output;

  /**
   * The content type associated with this Response.
   */
  protected String contentType = null;

  /**
   * Return the content type that was set or calculated for this response,
   * or <code>null</code> if no content type was set.
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Set the content type for this Response.
   *
   * @param type The new content type
   */
  public void setContentType(String type) {
    contentType = type;
    encoding = "ISO-8859-1";
    if ((type != null) && (type.indexOf(';') >= 0)) {
      int start = contentType.indexOf("charset=");
      if (start < 0) return;
      encoding = contentType.substring(start + 8);
      int end = encoding.indexOf(';');
      if (end >= 0)
        encoding = encoding.substring(0, end);
      encoding = encoding.trim();
      if ((encoding.length() > 2) && (encoding.startsWith("\""))
          && (encoding.endsWith("\""))) {
        encoding = encoding.substring(1, encoding.length() - 1);
        encoding = encoding.trim();
      }
    }
  }

  /**
   * The character encoding associated with this Response.
   */
  protected String encoding = null;

  /**
   * Return the character encoding used for this Response.
   */
  public String getCharacterEncoding() {
    if (encoding == null)
      return ("ISO-8859-1");
    else
      return (encoding);
  }

  public void recycle() {
    output = null;
  }
}
