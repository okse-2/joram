/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */


package fr.dyade.aaa.agent;

import fr.dyade.aaa.util.*;
import java.util.*;

/**
 * <code>Thread</code> used for recuperation of value on each A3 server.
 */
public final class ThreadAdmin extends Thread {
public static final String RCS_VERSION="@(#)$Id: ThreadAdmin.java,v 1.1.1.1 2000-05-30 11:45:24 tachkeni Exp $";
    
    /**
     * frequence of recuperation of values (in millisecond)
     */
    public static int sleepTime = 4000;
    
    /**
     * boolean of running Thread
     */
    protected static boolean work;
    
    /**
     * pointer on the udpAgentAdmin local
     */
    AdminEventReactor udpAgentAdmin;
    
    
    /**
     * construct a ThreadAdmin
     */
    public ThreadAdmin(AdminEventReactor uaa){
	super("ThreadAdmin");
	udpAgentAdmin = uaa;
	work = true;
    }

    /**
     * running code
     */
    public void run(){
	while (work){
	    synchronized(this){
		try {
		    if (!udpAgentAdmin.hasListeners(ServerEventType.SERVER_THROUGHPUT)) this.wait(); // if no listener, the thread is stopped
		    udpAgentAdmin.throughputEventReact(Server.getServerId(),Channel.getCounter(),Server.qin.nbElementAdd,Server.qout.nbElementAdd);
		    Channel.resetCounter();
		    this.sleep(sleepTime);
		} catch (Exception e){
		    System.out.println("Exception in ThreadAdmin");
		    e.printStackTrace();
		}
	    }
	} 
    }
    
    public synchronized void wakeUp(){
	this.notify();
    }
}
