/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;

import com.scalagent.kjoram.*;
import com.scalagent.kjndi.ksoap.*;

import java.io.*;
import java.util.*;

/**
 */
public class KConsumer extends MIDlet implements CommandListener {

  Display display;
  Form mainScreen;
  int max = 5;

  Topic topic;
  Queue queue;
  MsgListener listener;

  public void addText(String text) {
    if (max < mainScreen.size())
      mainScreen.delete(0);
    mainScreen.append(text);
    display.setCurrent(mainScreen);
  }
  public void commandAction(Command cmd, Displayable d) {

  }
  
  protected void startApp() throws MIDletStateChangeException {
    display = Display.getDisplay(this);
    mainScreen = new Form("Consumes messages ");
    addText("Consumes messages.");
    display.setCurrent(mainScreen);

    try {
      SoapNamingContext ictx = new SoapNamingContext("X.X.X.X",8080);
      System.out.println("ictx=" + ictx);
      queue = (Queue) ictx.lookup("queue");
      System.out.println("queue=" + queue);
      topic = (Topic) ictx.lookup("topic");
      System.out.println("topic=" + topic);
            
      listener = new MsgListener(this);

      Thread t = new Thread() {
          public void run() {
            subscribeTopic(listener,topic);
          }
        };
      t.start();

      Thread t1 = new Thread() {
          public void run() {
            subscribeQueue(queue);
          }
        };
      t1.start();
      
      // activiate the screen
      display.setCurrent(mainScreen);
    } catch (Exception exc) {
      System.out.println("////////// EXCEPTION " + exc);
      exc.printStackTrace();
    }
  }

  void subscribeTopic(MessageListener listener, Topic topic) {
    System.out.println("KConsumer.subscribeTopic(" + topic + ")");
    try {
      ConnectionFactory cf = 
        new com.scalagent.kjoram.ksoap.SoapConnectionFactory(
          "X.X.X.X", 8080, 360000);
      com.scalagent.kjoram.Connection cnx = cf.createConnection();
      Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer tConsumer = sess.createConsumer(topic);
      tConsumer.setMessageListener(listener);
      cnx.start();
      mainScreen.setCommandListener(this);
    } catch (Exception exc) {
      System.out.println("subscribeTopic ::::: EXCEPTION");
      exc.printStackTrace();
    }
  }

  void subscribeQueue(Queue queue) {
    System.out.println("KConsumer.subscribeQueue(" + queue + ")");
    try {
      ConnectionFactory cf = 
        new com.scalagent.kjoram.ksoap.SoapConnectionFactory(
          "X.X.X.X", 8080, 360000);
      com.scalagent.kjoram.Connection cnx = cf.createConnection();
      Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer qConsumer = sess.createConsumer(queue);
      TextMessage msg;
      cnx.start();
      for (int i = 0; i < 100; i++) {
        msg = (TextMessage) qConsumer.receive();
        //System.out.println("### from queue: " + msg.getText());
        addText("\nfrom queue: " + msg.getText());
        display.setCurrent(mainScreen);
      }
      mainScreen.setCommandListener(this);
    } catch (Exception exc) {
      System.out.println("subscribeQueue ::::: EXCEPTION");
      exc.printStackTrace();
    } 
  }

  protected void pauseApp() {
  }

  protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
  }
}


