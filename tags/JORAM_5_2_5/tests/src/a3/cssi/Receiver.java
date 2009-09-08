/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 ScalAgent Distributed Technologies
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
package a3.cssi;

import java.util.Vector;
import java.util.Hashtable;

import fr.dyade.aaa.agent.*;

public class Receiver extends Agent implements ReceiverMBean {
  public Receiver(short serverId) {
    super(serverId);
    nbTokens = 0;
    nbErrors = 0;
  }

  int bounce;

  public int getBounce() {
    return bounce;
  }

  int nbTokens;

  public int getNbTokens() {
    return nbTokens;
  }

  int nbErrors;

  public int getNbErrors() {
    return nbErrors;
  }

  public void reset() {
    nbTokens = 0;
    nbErrors = 0;
  }

  long last = 0L;

  public long getLastTime() {
    return last;
  }

  public void react(AgentId from, Notification not) {
    try {
      if (not instanceof Token) {
        Token token = (Token) not;

        nbTokens += 1;
        last = System.currentTimeMillis();

        if ((bounce != 0) &&
            (bounce != token.bounce -1)) {
          System.out.println("ERROR recv#" + token.bounce + " from " + from);
          nbErrors += 1;
        }
        bounce = token.bounce;
      } else {
        super.react(from, not);
      }
    }  catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}
