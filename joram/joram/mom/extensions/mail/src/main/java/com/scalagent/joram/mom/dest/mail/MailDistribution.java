/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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

import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.objectweb.joram.mom.dest.DistributionHandler;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

public class MailDistribution implements DistributionHandler {

  public static Logger logger = Debug.getLogger(MailDistribution.class.getName());

  private String smtpServer = null;

  private String to = null;
  private String cc = null;
  private String bcc = null;
  private String from = null;
  private String subject = null;
  private String selector = null;

  public void distribute(Message message) throws Exception {
    sendJavaMail(message);
  }

  public void init(Properties properties, boolean firstTime) {
    smtpServer = properties.getProperty("smtpServer", smtpServer);
    to = properties.getProperty("to", to);
    cc = properties.getProperty("cc", cc);
    bcc = properties.getProperty("bcc", bcc);
    from = properties.getProperty("from", from);
    subject = properties.getProperty("subject", subject);
    selector = properties.getProperty("selector", selector);
  }

  public void sendJavaMail(Message message) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + " sendJavaMail(" + message + ")");

    if (smtpServer == null || smtpServer.length() <= 0) {
      logger.log(BasicLevel.ERROR, "--- " + this + " sendJavaMail : smtpServer is empty.");
      throw new Exception("sendJavaMail : smtpServer is empty.");
    }

    Properties props = System.getProperties();
    props.put("mail.smtp.host", smtpServer);
    Session session = Session.getDefaultInstance(props);
    MimeMessage msg = new MimeMessage(session);
    MimeMultipart mimeMultiPart = new MimeMultipart();
    MimeBodyPart mimeBodyPart = new MimeBodyPart();

    msg.setFrom(new InternetAddress(from));

    if (to != null && to != "") {
      StringTokenizer st = new StringTokenizer(to, ",");
      while (st.hasMoreTokens()) {
        msg.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(st.nextToken(), false));
      }
    }

    if (cc != null && cc != "") {
      StringTokenizer st = new StringTokenizer(cc, ",");
      while (st.hasMoreTokens()) {
        msg.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse(st.nextToken(), false));
      }
    }

    if (bcc != null && bcc != "") {
      StringTokenizer st = new StringTokenizer(bcc, ",");
      while (st.hasMoreTokens()) {
        msg.setRecipients(javax.mail.Message.RecipientType.BCC, InternetAddress.parse(st.nextToken(), false));
      }
    }

    msg.setSubject(subject);

    if (ConversionHelper.toBoolean(message.getProperty("showProperties"))) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "--- " + this + " showProperties option enabled.");
      try {
        mimeMultiPart.addBodyPart(getMultipartProp(message));
      } catch (Exception exc) {
        logger.log(BasicLevel.WARN, "--- " + this + " sendJavaMail: setMultipartProp", exc);
      }
    }

    if (message.type == Message.TEXT) {
      mimeBodyPart.setText(message.getText());
      mimeMultiPart.addBodyPart(mimeBodyPart);
    } else {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "not yet implemented");
      //message.getBytes()
    }

    msg.setHeader("X-Mailer", "JORAM:JavaMailUtil");
    msg.setSentDate(new Date());

    msg.setContent(mimeMultiPart);
    Transport.send(msg);
  }

  private MimeBodyPart getMultipartProp(Message msg) throws Exception {
    MimeBodyPart mbp = new MimeBodyPart();

    StringBuffer buf = new StringBuffer();
    buf.append("type=" + msg.type + "\n");
    buf.append("id=" + msg.id + "\n");
    buf.append("persistent=" + msg.persistent + "\n");
    buf.append("priority=" + msg.priority + "\n");
    buf.append("expiration=" + msg.expiration + "\n");
    buf.append("timestamp=" + msg.timestamp + "\n");
    buf.append("toId=" + msg.toId + "\n");
    buf.append("destType=" + msg.toType + "\n");
    buf.append("correlationId=" + msg.correlationId + "\n");
    buf.append("replyToId=" + msg.replyToId + "\n");
    buf.append("replyDestType=" + msg.replyToType + "\n");
    buf.append("deliveryCount=" + msg.deliveryCount + "\n");
    buf.append("denied=" + msg.redelivered + "\n");

    if (msg.getProperty("JMS_JORAM_ERRORCOUNT") != null) {
      int errorCount = ((Integer) msg.getProperty("JMS_JORAM_ERRORCOUNT")).intValue();
      buf.append("errorCount=" + errorCount + "\n");
      for (int i = 1; i <= errorCount; i++) {
        buf.append("errorCode" + i + "=" + msg.getProperty("JMS_JORAM_ERRORCODE_" + i) + "\n");
        buf.append("errorCause" + i + "=" + msg.getProperty("JMS_JORAM_ERRORCAUSE_" + i) + "\n");
      }
    }

    mbp.setText(buf.toString());
    return mbp;
  }

  public void close() {
    // Nothing to do
  }

}
