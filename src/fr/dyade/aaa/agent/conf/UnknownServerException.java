/*
 * Copyright (C) 2002-2003 ScalAgent Distributed Technologies 
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
 */
package fr.dyade.aaa.agent.conf;

/**
 * Thrown to indicate that the specified agent server does not exist. 
 */
public class UnknownServerException extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   *  Constructs a new <code>UnknownServerException</code> with no
   * detail message.
   */
  public UnknownServerException() {
    super();
  }

  /**
   *  Constructs a new <code>UnknownServerException</code> with the
   * specified  detail message. 
   *
   * @param   s   the detail message.
   */
  public UnknownServerException(String s) {
    super(s);
  }
}
