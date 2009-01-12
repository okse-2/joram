/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies 
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
package fr.dyade.aaa.admin.cmd;

public class RemoveDomainCmd extends DomainCmd {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

/**
  * remove domain
  *
  * @param name          domain name
  * @param networkClass  network class
  */
  public RemoveDomainCmd(String name, String networkClass) {
    super(name,networkClass);
  }

  public String toString() {
    return "RemoveDomainCmd" + super.toString();
  }
}
