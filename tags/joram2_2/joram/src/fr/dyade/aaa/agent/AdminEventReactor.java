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

import java.io.*;
import java.util.*;

/**
 * Agent to allow remote agent creation.
 * @author  Philippe laumay
 * @version 1.0 08/99
 */

// This interface is use by Administred Server 
// to notify Events to a listener (now the UdpAdminProxy)
public interface AdminEventReactor {
public static final String RCS_VERSION="@(#)$Id: AdminEventReactor.java,v 1.7 2002-01-16 12:46:47 joram Exp $";
    
    /**
     * this method is called to now if an event is listen by somebody
     * @param serverEventType the type of event. (Use the predefined attributs)
     * @return true if there are listener(s), false otherwise.
     */
    public boolean hasListeners(int serverEventType);

    /**
     * react for an event.
     * @param ser the ServerEventReport of this event
     */
    public void eventReact(ServerEventReport ser);

     /**
      * react from a throughput_SER 
      * @param serverid      the Server Id
      * @param nbElementSend the number of element Send
      * @param nbElementqin  the number of element in the queue In
      * @param nbElementqout the number of element in the queue Out
      */
    public void throughputEventReact(short serverid, int nbElementSend, int nbElementqin, int nbElementqout);

    /**
     * react for an Factory event.
     * @param desc the AgentDesc.
     * @param ser the ServerEventType of this event : AgentCreated or AgentDeleted
     */
    public void factoryEventReact(AgentDesc desc,int set);
}
