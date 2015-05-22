/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001 ScalAgent Distributed Technologies
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


package a3.base;

import fr.dyade.aaa.agent.*;
import framework.TestCase;

public class test4 extends TestCase {
  public test4() {
    super();
  }

  int nbNode = 1;

  protected void setUp() throws Exception {
    nbNode = Integer.getInteger("NbNode", 1).intValue();

    for (int i=1; i<nbNode; i++)
      startAgentServer((short) i);

    int agentWidth = Integer.getInteger("AgentWidth", 10).intValue();
    int agentDepth = Integer.getInteger("AgentDepth", 2).intValue();
    int nbNot = Integer.getInteger("NbNot", 2).intValue();
    int agentSize = Integer.getInteger("AgentSize", 5).intValue();
    int notSize = Integer.getInteger("NotSize", 1).intValue();

    timeout = 10000L *
      ((long) nbNot) * ((long) agentWidth) * ((long) agentDepth);
    timeout = Long.getLong("timeout", timeout).longValue();

    BenchAgent agx;
    AgentId[][] tab = new AgentId[agentDepth][agentWidth];

    for (int i=0; i<agentDepth; i++) {
      for (int j=0; j<agentWidth; j++) {
        if (i == 0) {
          agx = new BenchAgent((short) (j%nbNode), null, agentSize);
        } else {
          agx = new BenchAgent((short) (j%nbNode), tab[i-1], agentSize);
        }
        tab[i][j] = agx.getId();
        agx.deploy();
      }
      agx = null;
    }

    BenchAgent ag = new BenchAgent((short) 0, tab[agentDepth -1], agentSize);
    ag.nbNot = nbNot;
    ag.deploy();

    Channel.sendTo(ag.getId(), new BenchNot(notSize));
  }

  protected void tearDown() {
    for (int i=1; i<nbNode; i++)
      killAgentServer((short) i);
  }

  public static void main(String args[]) {
    new test4().runTest(args);
  }

  static class BenchNot extends Notification {
    public int array[] ;

    BenchNot(int size) {
      array = new int[size];
    }

    public void init() {
      array[0] = array.length;
      for (int i=1; i<array.length; i++)
        array[i] = i;
    }

    public boolean isOk() {
      if (array.length != array[0]) return false;

      for (int i=1; i<array.length; i++)
        if (array[i] != i) return false;
      return true;
    }
  }

  static class BenchAgent extends Agent {
    int idx;
    int nbNot;
//   long startDate, endDate;
//   long total = 0L;
    int array[];

    AgentId father;
    AgentId child[];

    Notification benchNot;

    public BenchAgent(short to, AgentId a[], int size) {
      super(to);
      child = a;
      if (size > 0)
        array = new int[size];
    }

    public void react(AgentId from, Notification not) {
      if (not instanceof BenchNot) {
        benchNot = not;
        if (from.isNullId() || from.equals(getId())) {
          // It's the main test agent.
          father = null;
          // startDate = System.currentTimeMillis();
        } else {
          father = from;
        }
        idx = 0;
      }

      if ((child == null) || (idx == child.length)) {
        if (father == null) {
          // It's the main test agent.
          // endDate = System.currentTimeMillis();
          // System.out.println("dT[" + nbNot + "] = " + (endDate - startDate));
          // total += (endDate - startDate);
          nbNot -= 1;
          if (nbNot != 0) {
            sendTo(getId(), benchNot);
          } else {
            // System.out.println("total = " + total);
            TestCase.endTest();
          }
        } else {
          sendTo(father, new Notification());
        }
      } else {
        sendTo(child[idx++], benchNot);
      }
    }
  }
}
