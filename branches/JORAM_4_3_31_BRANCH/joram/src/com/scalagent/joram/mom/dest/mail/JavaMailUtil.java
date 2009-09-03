/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
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

import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.notifications.*;
import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.shared.messages.*;
import org.objectweb.joram.shared.selectors.*;
import org.objectweb.joram.mom.dest.*;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.objectweb.joram.shared.messages.Message;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>JavaMailUtil</code> class giv utils to get, send, ...
 * mail.
 */
public class JavaMailUtil {

  private Store store = null;
  private Folder folder = null;

  public void sendJavaMail(SenderInfo si, Message message)
    throws Exception {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    "--- " + this +
                                    " sendJavaMail(" + si +
                                    "," + message + ")");
    
    if (si.smtpServer == null || si.smtpServer.length() < 0) {
      MomTracing.dbgDestination.log(BasicLevel.ERROR, 
                                    "--- " + this +
                                    " sendJavaMail : smtpServer is empty.");
      throw new Exception("sendJavaMail : smtpServer is empty.");
    }
    
    
    Properties props = System.getProperties();
    props.put("mail.smtp.host", si.smtpServer);
    Session session = Session.getDefaultInstance(props);
    MimeMessage msg = new MimeMessage(session);
    MimeMultipart mimeMultiPart = new MimeMultipart();
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    
    msg.setFrom(new InternetAddress(si.from));
    
    if (si.to != null) {
      StringTokenizer st = new StringTokenizer(si.to,",");
      while (st.hasMoreTokens()) {
        msg.setRecipients(javax.mail.Message.RecipientType.TO,
                          InternetAddress.parse(st.nextToken(),false));
      }
    } else {
      MomTracing.dbgDestination.log(BasicLevel.ERROR, 
                                    "--- " + this +
                                    " sendJavaMail : to is null.");
      throw new Exception("sendJavaMail : to is null.");
    }
    
    if (si.cc != null) {
      StringTokenizer st = new StringTokenizer(si.cc,",");
      while (st.hasMoreTokens()) {
        msg.setRecipients(javax.mail.Message.RecipientType.CC
                          ,InternetAddress.parse(st.nextToken(),false));
      }
    }
    
    if (si.bcc != null) {
      StringTokenizer st = new StringTokenizer(si.bcc,",");
      while (st.hasMoreTokens()) {
        msg.setRecipients(javax.mail.Message.RecipientType.BCC
                          ,InternetAddress.parse(st.nextToken(),false));
      }
    }
    
    msg.setSubject(si.subject);
    
    if (message.getBooleanProperty("showProperties")) {
      try {
        mimeMultiPart.addBodyPart(getMultipartProp(message));
      } catch (Exception exc) {
        MomTracing.dbgDestination.log(BasicLevel.WARN,
                                      "--- " + this +
                                      " sendJavaMail: setMultipartProp", 
                                      exc);
      }
    }
    
    if (message.getType() == MessageType.TEXT) {
      mimeBodyPart.setText("JoramMessage :\n" + message.getText());
      mimeMultiPart.addBodyPart(mimeBodyPart);
    } else {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG,"not yet impl.");
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
    buf.append("type=" + msg.getType() + "\n");
    buf.append("id=" + msg.getIdentifier() + "\n");
    buf.append("persistent=" + msg.getPersistent() + "\n");
    buf.append("priority=" + msg.getPriority() + "\n");
    buf.append("expiration=" + msg.getExpiration() + "\n");
    buf.append("timestamp=" + msg.getTimestamp() + "\n");
    buf.append("toId=" + msg.getDestinationId() + "\n");
    buf.append("destType=" + msg.toType() + "\n");
    buf.append("correlationId=" + msg.getCorrelationId() + "\n");
    buf.append("replyToId=" + msg.getReplyToId() + "\n");
    buf.append("replyDestType=" + msg.replyToType() + "\n");
    buf.append("deliveryCount=" + msg.deliveryCount + "\n");
    buf.append("denied=" + msg.denied + "\n");
    buf.append("deletedDest=" + msg.deletedDest + "\n");
    buf.append("expired=" + msg.expired + "\n");
    buf.append("notWriteable=" + msg.notWriteable + "\n");
    buf.append("undeliverable=" + msg.undeliverable + "\n");
    // Enumeration getPropertyNames()

    mbp.setText(buf.toString());
    return mbp;
  }

  public synchronized javax.mail.Message[] popMail(String popServer, 
                                                   String popUser, 
                                                   String popPassword,
                                                   boolean expunge) {
    
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    "--- " + this +
                                    " popMail : " +
                                    "popServer=" + popServer +
                                    ", popUser=" + popUser +
                                    ", expunge=" + expunge);

    javax.mail.Message[] msgs = null;

    try {
      Properties props = System.getProperties();
      Session session = Session.getDefaultInstance(props, null);
      
      store = session.getStore("pop3");
      store.connect(popServer, popUser, popPassword);
      
      folder = store.getDefaultFolder();
      if (folder == null) throw new Exception("No default folder");

      folder = folder.getFolder("INBOX");
      if (folder == null) throw new Exception("No POP3 INBOX");

      if (expunge) {
        folder.open(Folder.READ_WRITE);
        msgs = folder.getMessages();
      } else {
        folder.open(Folder.READ_ONLY);
        msgs = folder.getMessages();
      }
      return msgs;
    } catch (Exception exc) {
      MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                    "JavaMailUtil.popMail", 
                                    exc);
      return msgs;
    }
  }

  public synchronized void closeFolder(Vector msgs, boolean expunge) {
    try {
      if (expunge && (msgs != null)) {
        for (Enumeration elements = msgs.elements();
             elements.hasMoreElements();) {
          javax.mail.Message msg = 
            (javax.mail.Message) elements.nextElement();
          msg.setFlag(Flags.Flag.DELETED,true);
        }
      }
      if (folder != null) folder.close(expunge);
      if (store != null) store.close();
    } catch (Exception exc) {
      MomTracing.dbgDestination.log(BasicLevel.ERROR,
                                    "JavaMailUtil.closeFolder", 
                                    exc);
    } 
  }

  protected Message createMessage(Properties prop, 
                                  String mailId,
                                  String destType,
                                  String toId,
                                  String replyDestType) 
    throws Exception {
    Message msg = new Message();
    
    msg.setIdentifier(mailId);
    msg.setPersistent(ConversionHelper.toBoolean(prop.getProperty("persistent","true")));
    msg.setPriority(ConversionHelper.toInt(prop.getProperty("priority","4")));
    msg.setExpiration(ConversionHelper.toLong(prop.getProperty("expiration","0")));
    if (prop.containsKey("timestamp"))
      msg.setTimestamp(ConversionHelper.toLong(prop.getProperty("timestamp")));
    msg.setDestination(prop.getProperty("toId",toId),
                       prop.getProperty("destType",destType));
    if (prop.containsKey("correlationId"))
      msg.setCorrelationId(prop.getProperty("correlationId"));
    if (prop.containsKey("replyToId"))
      msg.setReplyTo(prop.getProperty("replyToId"),
                     prop.getProperty("replyDestType",replyDestType));
    msg.deliveryCount = ConversionHelper.toInt(prop.getProperty("deliveryCount","0"));
    msg.denied = ConversionHelper.toBoolean(prop.getProperty("denied","false"));
    msg.deletedDest = ConversionHelper.toBoolean(prop.getProperty("deletedDest","false"));
    msg.expired = ConversionHelper.toBoolean(prop.getProperty("expired","false"));
    msg.notWriteable = ConversionHelper.toBoolean(prop.getProperty("notWriteable","false"));
    msg.undeliverable = ConversionHelper.toBoolean(prop.getProperty("undeliverable","false"));
    
//        msg.optionalHeader = (Hashtable) h.getProperty("optionalHeader");
//        msg.properties = (Hashtable) h.getProperty("properties");
    
    msg.setText((String) prop.getProperty("mailMessage"));
    
    return msg;
  }

  public Properties getMOMProperties(javax.mail.Message message)
    throws Exception, MessagingException {
    Properties prop = new Properties();
    
    String subject = message.getSubject();
    prop.setProperty("subject",subject);

    Part messagePart = message;
    Object content = messagePart.getContent();
    if (content instanceof Multipart)
      messagePart=((Multipart)content).getBodyPart(0);
    
    String contentType = messagePart.getContentType();

    if (contentType.startsWith("text/plain")
        || contentType.startsWith("text/html")) {
      InputStream is = messagePart.getInputStream();
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(is));

      String currentLine = reader.readLine();
      while (currentLine != null) {
        currentLine = currentLine.trim();
        if (currentLine.equalsIgnoreCase("JoramMessage")) break;
        if (currentLine.length() > 1) {
          String[] buf = currentLine.split("=");
          prop.setProperty(buf[0],buf[1]);
          if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgDestination.log(BasicLevel.DEBUG,buf[0] + "=" + buf[1]);
        }
        currentLine = reader.readLine();
      }

      StringBuffer sb = new StringBuffer();
      currentLine = reader.readLine();
      while (currentLine != null) {
        sb.append(currentLine + "\n");
        currentLine = reader.readLine();
      }
      prop.setProperty("mailMessage",sb.toString());
    }

    if (content instanceof Multipart) {
      messagePart=((Multipart)content).getBodyPart(1);
      
      contentType = messagePart.getContentType();
      
      if (contentType.startsWith("text/plain")
          || contentType.startsWith("text/html")) {
        InputStream is = messagePart.getInputStream();
        BufferedReader reader = new BufferedReader(
          new InputStreamReader(is));
        
        String currentLine = reader.readLine();
        StringBuffer sb = new StringBuffer();
        while (currentLine != null) {
          if (currentLine.equalsIgnoreCase("JoramMessage")) continue;
          sb.append(currentLine + "\n");
          currentLine = reader.readLine();
        }
        prop.setProperty("mailMessage",sb.toString());
      }
    }

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    "--- " + this +
                                    " getMOMProperties : prop=" + prop);
    return prop;
  }
}
