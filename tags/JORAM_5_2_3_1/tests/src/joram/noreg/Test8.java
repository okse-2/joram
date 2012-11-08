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
 * Initial developer(s): Freyssinet Andre (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.noreg;

import fr.dyade.aaa.agent.AgentServer;

/**
 *check server start and stop run with no problems
 *
 */
public class Test8 extends framework.TestCase{

  public static void main (String args[]) throws Exception {
      new Test8().run();
  }
    public void run(){
	try{
	    AgentServer.init((short) 0, "./s0", null);
	    System.out.println("init");
	    Thread.sleep(15000L);
	    AgentServer.start();
	    System.out.println("start");
	    Thread.sleep(15000L);
	    AgentServer.stop();
	    System.out.println("stop");
	    Thread.sleep(15000L);
	    AgentServer.reset();
	    System.out.println("reset");
	    Thread.sleep(15000L);
	    AgentServer.init((short) 0, "./s0", null);
	    System.out.println("init");
	    Thread.sleep(15000L);
	    AgentServer.start();
	    System.out.println("start");
	    Thread.sleep(15000L);
	    AgentServer.stop();
	    System.out.println("stop");
	    Thread.sleep(15000L);
	    AgentServer.start();
	    System.out.println("start");
	    Thread.sleep(15000L);
	    AgentServer.stop();
	    System.out.println("stop");
	    Thread.sleep(15000L);
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	 AgentServer.stop();
	 endTest();
	}
    }
}
