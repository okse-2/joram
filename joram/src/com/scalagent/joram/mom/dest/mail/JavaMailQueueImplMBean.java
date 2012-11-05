/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s): 
 */
package com.scalagent.joram.mom.dest.mail;

import org.objectweb.joram.mom.dest.QueueImplMBean;

public interface JavaMailQueueImplMBean extends QueueImplMBean {
  /**
   * Returns the default SMTP server for outgoing mail.
   *
   * @return the default SMTP server.
   */
  String getSMTPServer();

  /**
   * Sets or unsets the default SMTP server for outgoing mail.
   *
   * @param period The default SMTP server or null for unsetting
   *               previous value.
   */
  void setSMTPServer(String smtpServer);

  /**
   * Returns the default <code>to</code> field.
   *
   * @return the default <code>to</code> field.
   */
  String getDefaultTo();

  /**
   * Sets or unsets the default <code>to</code> field.
   *
   * @param to 	The default <code>to</code> field or null for unsetting
   *            previous value.
   */
  void setDefaultTo(String to);

  /**
   * Returns the default <code>cc</code> field.
   *
   * @return the default <code>cc</code> field.
   */
  String getDefaultCC();

  /**
   * Sets or unsets the default <code>cc</code> field.
   *
   * @param cc 	The default <code>cc</code> field or null for unsetting
   *            previous value.
   */
  void setDefaultCC(String cc);

  /**
   * Returns the default <code>bcc</code> field.
   *
   * @return the default <code>bcc</code> field.
   */
  String getDefaultBcc();

  /**
   * Sets or unsets the default <code>bcc</code> field.
   *
   * @param bcc The default <code>bcc</code> field or null for unsetting
   *            previous value.
   */
  void setDefaultBcc(String bcc);

  /**
   * Returns the default <code>from</code> field.
   *
   * @return the default <code>from</code> field.
   */
  String getDefaultFrom();

  /**
   * Sets or unsets the default <code>from</code> field.
   *
   * @param from The default <code>from</code> field or null for unsetting
   *             previous value.
   */
  void setDefaultFrom(String from);

  /**
   * Returns the default <code>subject</code> field.
   *
   * @return the default <code>subject</code> field.
   */
  String getDefaultSubject();

  /**
   * Sets or unsets the default <code>subject</code> field.
   *
   * @param subject 	The default <code>subject</code> field or null for
   *             	unsetting previous value.
   */
  void setDefaultSubject(String subject);

  /**
   * Returns the default <code>selector</code>.
   *
   * @return the default <code>selector</code>.
   */
  String getDefaultSelector();

  /**
   * Sets or unsets the default <code>selector</code>.
   *
   * @param selector 	The default <code>selector</code> or null for
   *             	unsetting previous value.
   */
  void setDefaultSelector(String selector);

  /**
   * Returns the period value to collect mail, -1 if not set.
   *
   * @return the period value to collect mail; -1 if not set.
   */
  long getPopPeriod();

  /**
   * Sets or unsets the period value to collect mail.
   *
   * @param period The period value to collect mail or -1 for unsetting
   *               previous value.
   */
  void setPopPeriod(long period);

  /**
   * Returns the pop server for incoming mail.
   *
   * @return the pop server for incoming mail.
   */
  String getPopServer();

  /**
   * Sets or unsets the pop server for incoming mail.
   *
   * @param server 	The pop server or null for unsetting previous value.
   */
  void setPopServer(String server);

  /**
   * Returns the username for pop account.
   *
   * @return the username for pop account.
   */
  String getPopUser();

  /**
   * Sets or unsets the username for pop account.
   *
   * @param user	The username for pop account or null for
   *			unsetting previous value.
   */
  void setPopUser(String user);

  /**
   * Returns the password for pop account.
   *
   * @return the password for pop account.
   */
  String getPopPassword();

  /**
   * Sets or unsets the password for pop account.
   *
   * @param pass	The password for pop account or null for
   *			unsetting previous value.
   */
  void setPopPassword(String pass);

  /**
   * Returns  the default <code>expunge</code> field.
   *
   * @return the default <code>expunge</code> field.
   */
  boolean getExpunge();

  /**
   * Sets or unsets the default <code>expunge</code> field.
   *
   * @param expunge	The default <code>expunge</code> field or null for
   *			unsetting previous value.
   */
  void setExpunge(boolean expunge);
}
