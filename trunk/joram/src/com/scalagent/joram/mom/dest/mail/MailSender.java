/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package com.scalagent.joram.mom.dest.mail;

public interface MailSender {
  
  /**
   * Returns the SMTP server for outgoing mail.
   * 
   * @return the SMTP server.
   */
  String getSMTPServer();

  /**
   * Sets or unsets the SMTP server for outgoing mail.
   * 
   * @param period
   *          The SMTP server or null for unsetting previous value.
   */
  void setSMTPServer(String smtpServer);

  /**
   * Returns the <code>to</code> field.
   * 
   * @return the <code>to</code> field.
   */
  String getTo();

  /**
   * Sets or unsets the <code>to</code> field.
   * 
   * @param to
   *          The default <code>to</code> field or null for unsetting previous
   *          value.
   */
  void setTo(String to);

  /**
   * Returns the <code>cc</code> field.
   * 
   * @return the <code>cc</code> field.
   */
  String getCC();

  /**
   * Sets or unsets the <code>cc</code> field.
   * 
   * @param cc
   *          The <code>cc</code> field or null for unsetting previous value.
   */
  void setCC(String cc);

  /**
   * Returns the <code>bcc</code> field.
   * 
   * @return the <code>bcc</code> field.
   */
  String getBcc();

  /**
   * Sets or unsets the <code>bcc</code> field.
   * 
   * @param bcc
   *          The <code>bcc</code> field or null for unsetting previous value.
   */
  void setBcc(String bcc);

  /**
   * Returns the <code>from</code> field.
   * 
   * @return the <code>from</code> field.
   */
  String getFrom();

  /**
   * Sets or unsets the <code>from</code> field.
   * 
   * @param from
   *          The <code>from</code> field or null for unsetting previous value.
   */
  void setFrom(String from);

  /**
   * Returns the <code>subject</code> field.
   * 
   * @return the <code>subject</code> field.
   */
  String getSubject();

  /**
   * Sets or unsets the <code>subject</code> field.
   * 
   * @param subject
   *          The <code>subject</code> field or null for unsetting previous
   *          value.
   */
  void setSubject(String subject);

  /**
   * Returns the <code>selector</code>.
   * 
   * @return the <code>selector</code>.
   */
  String getSelector();

  /**
   * Sets or unsets the <code>selector</code>.
   * 
   * @param selector
   *          The default <code>selector</code> or null for unsetting previous
   *          value.
   */
  void setSelector(String selector);

}
