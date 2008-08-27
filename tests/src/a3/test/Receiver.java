/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s):ScalAgent D.T.
 * Contributor(s): 
 */
package a3.test;

import java.util.Vector;
import java.util.Hashtable;

import fr.dyade.aaa.agent.*;

public class Receiver extends Agent implements ReceiverMBean {

  public static AgentId id;

  public static void main(String args[]) throws Exception {
    AgentServer.init(new String[] {"0", "./s0"});

    Receiver receiver = new Receiver((short) 0);
    id = receiver.getId();
    receiver.bounce = Integer.getInteger("bounce", 10).intValue();
    receiver.payload = Integer.getInteger("payload", 10000).intValue();
    receiver.pause = Integer.getInteger("pause", 5000).intValue();
    receiver.deploy();

    AgentServer.start();
  }

  boolean running;

  public Receiver(short serverId) {
    super(serverId);
    running = true;
    nbTokens = 0;
    ids = new Vector<AgentId>();
    tokens = new Hashtable<AgentId, Token>();
  }

  int bounce;

  public int getBounce() {
    return bounce;
  }

  public void setBounce(int bounce) {
    this.bounce = bounce;
  }

  int payload;

  public int getPayload() {
    return payload;
  }

  public void setPayload(int payload) {
    this.payload = payload;
  }

  int pause;

  public int getPause() {
    return pause;
  }

  public void setPause(int pause) {
    this.pause = pause;
  }

  int nbTokens;

  public int getNbTokens() {
    return nbTokens;
  }

  int nbErrors;

  public int getNbErrors() {
    return nbErrors;
  }

  public int getNbSenders() {
    return tokens.size();
  }

  public void reset() {
    nbTokens = 0;
    nbErrors = 0;
  }

  public void start() {
    running = true;
    while (! ids.isEmpty()) {
      sendTo(ids.remove(0), new Token(bounce, payload, pause));
    }
  }

  public void stop() {
    running = false;
  }

  Vector<AgentId> ids;
  Hashtable<AgentId, Token> tokens;

  public void react(AgentId from, Notification not) {
    try {
      if (not instanceof Token) {
        Token token = (Token) not;

        nbTokens += 1;

        Token last = tokens.put(from, token);
        if ((last != null) &&
            (last.bounce != 0) &&
            (last.bounce != token.bounce +1)) {
          System.out.println("ERROR recv#" + token.bounce + " from " + from);
          nbErrors += 1;
        }

        if (token.bounce == 0) {
          if (running)
            sendTo(from, new Token(bounce, payload, pause));
          else
            ids.addElement(from);
        }
      } else {
        super.react(from, not);
      }
    }  catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}
