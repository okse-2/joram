/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
package fr.dyade.aaa.agent;

/**
 * Thrown to indicate that the specified service is not declared on a
 * particular server. 
 *
 * @author  Andre Freyssinet
 */
public class UnknownServiceException extends Exception {

public static final String RCS_VERSION="@(#)$Id: UnknownServiceException.java,v 1.10 2003-06-23 13:37:51 fmaistre Exp $"; 

  /**
   *  Constructs a new <code>UnknownServiceException</code> with no
   * detail message.
   */
  public UnknownServiceException() {
    super();
  }

  /**
   *  Constructs a new <code>UnknownServiceException</code> with the
   * specified  detail message. 
   *
   * @param   s   the detail message.
   */
  public UnknownServiceException(String s) {
    super(s);
  }
}
