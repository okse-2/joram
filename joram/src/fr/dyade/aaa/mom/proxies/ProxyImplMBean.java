/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.proxies;


/**
 * The <code>ProxyImplMBean</code> interface defines the JMX instrumentation
 * for administering a JORAM proxy.
 */
public interface ProxyImplMBean
{
  /** Returns the user name. */
  public String getUserName();

  /** Returns the user password. */
  public String getUserPassword();

  /** Deletes the proxy. */
  public void delete();

  /**
   * Changes the user name.
   *
   * @param name  New name.
   *
   * @exception Exception  If the new name is already taken.
   */
  public void updateUserName(String name) throws Exception;

  /**
   * Changes the user password.
   *
   * @param pass  New password.
   */
  public void updateUserPassword(String pass);
}
