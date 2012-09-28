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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.objectweb.joram.mom.dest.AcquisitionHandler;
import org.objectweb.joram.mom.dest.AcquisitionModule;
import org.objectweb.joram.mom.dest.ReliableTransmitter;
import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

public class MailAcquisition implements AcquisitionHandler {

  private static final Logger logger = Debug.getLogger(MailAcquisition.class.getName());

  private String popServer = null;
  private String popUser = null;
  private String popPassword = null;
  private boolean expunge = false;

  private Store store = null;
  private Folder folder = null;

  public void retrieve(ReliableTransmitter transmitter) throws Exception {
    List toExpunge = new ArrayList();
    javax.mail.Message[] msgs = popMail(popServer, popUser, popPassword, expunge);
    List list = null;

    if (msgs != null) {
      list = new ArrayList(msgs.length);
      for (int i = 0; i < msgs.length; i++) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "--- " + this + " doPop : msgs[" + i + "] = " + msgs[i]);
        }
        try {
          Message msg = new Message();
          msg.setText(getBody(msgs[i]));
          msg.type = Message.TEXT;

          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "--- " + this + " doPop : storeMessage m = " + msg);
          }
          if (expunge) {
            toExpunge.add(msgs[i]);
          }
          list.add(msg);
        } catch (Exception exc) {
          if (logger.isLoggable(BasicLevel.ERROR)) {
            logger.log(BasicLevel.ERROR, "--- " + this + " doPop", exc);
          }
          continue;
        }
      }

      transmitter.transmit(list, persistent);
      closeFolder(toExpunge, expunge);
    }
  }

  public javax.mail.Message[] popMail(String popServer, String popUser, String popPassword,
      boolean expunge) {

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "--- " + this + " popMail : " + "popServer=" + popServer + ", popUser="
          + popUser + ", expunge=" + expunge);
    }

    javax.mail.Message[] msgs = null;

    try {
      Properties props = System.getProperties();
      Session session = Session.getDefaultInstance(props, null);

      store = session.getStore("pop3");
      store.connect(popServer, popUser, popPassword);

      folder = store.getDefaultFolder();
      if (folder == null) {
        throw new Exception("No default folder");
      }

      folder = folder.getFolder("INBOX");
      if (folder == null) {
        throw new Exception("No POP3 INBOX");
      }

      if (expunge) {
        folder.open(Folder.READ_WRITE);
        msgs = folder.getMessages();
      } else {
        folder.open(Folder.READ_ONLY);
        msgs = folder.getMessages();
      }
      return msgs;
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "JavaMailUtil.popMail", exc);
      return msgs;
    }
  }

  public void closeFolder(List msgs, boolean expunge) {
    try {
      if (expunge && (msgs != null)) {
        for (Iterator elements = msgs.iterator(); elements.hasNext();) {
          javax.mail.Message msg = (javax.mail.Message) elements.next();
          msg.setFlag(Flags.Flag.DELETED, true);
        }
      }
      if (folder != null) {
        folder.close(expunge);
      }
      if (store != null) {
        store.close();
      }
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "JavaMailUtil.closeFolder", exc);
    }
  }

  boolean persistent = true;
  
  public void setProperties(Properties properties) {
    if (properties.containsKey(AcquisitionModule.PERSISTENT_PROPERTY)) {
      try {
        persistent = ConversionHelper.toBoolean(properties.get(AcquisitionModule.PERSISTENT_PROPERTY));
      } catch (MessageValueException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    popServer = properties.getProperty("popServer", popServer);
    popUser = properties.getProperty("popUser", popUser);
    popPassword = properties.getProperty("popPassword", popPassword);
    expunge = Boolean.valueOf(properties.getProperty("expunge")).booleanValue();
  }

  private String getBody(javax.mail.Message message) throws Exception, MessagingException {

    Part messagePart = message;
    Object content = messagePart.getContent();
    if (content instanceof Multipart) {
      messagePart = ((Multipart) content).getBodyPart(0);
    }

    String contentType = messagePart.getContentType();
    StringBuffer sb = new StringBuffer();

    if (contentType.startsWith("text/plain") || contentType.startsWith("text/html")) {
      InputStream is = messagePart.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      String currentLine = reader.readLine();
      while (currentLine != null) {
        sb.append(currentLine);
        sb.append('\n');
        currentLine = reader.readLine();
      }
    }

    if (content instanceof Multipart) {
      int i = 1;
      while (i < ((Multipart) content).getCount()) {
        messagePart = ((Multipart) content).getBodyPart(i);

        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "--- " + this + " Multipart : part=" + messagePart);
        }

        contentType = messagePart.getContentType();

        if (contentType.startsWith("text/plain") || contentType.startsWith("text/html")) {
          InputStream is = messagePart.getInputStream();
          BufferedReader reader = new BufferedReader(new InputStreamReader(is));

          String currentLine = reader.readLine();
          while (currentLine != null) {
            sb.append(currentLine);
            sb.append('\n');
            currentLine = reader.readLine();
          }
        }
        i++;
      }
    }

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "--- " + this + " getBody : body=" + sb.toString());
    }
    return sb.toString();
  }

  public void close() {
    // Nothing to do
  }

}
