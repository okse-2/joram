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

import java.lang.*;

/**
 * this class group the ServerEventType constants.
 */
public final class ServerEventType {
public static final String RCS_VERSION="@(#)$Id: ServerEventType.java,v 1.3 2000-10-05 15:15:23 tachkeni Exp $";

public final static int ERROR = 10;

public final static int AGENT_CREATED = 0;
public final static int AGENT_DELETED = 1;
public final static int SERVER_ACTIVE = 2;
public final static int SERVER_NOACTIVE = 3;
public final static int SERVER_THROUGHPUT = 4;

public final static int MAX_EVENT = 5;

/* the SET behind are not a Event type but was send after a Request */
public final static int SERVER_LIST = 6;
public final static int SERVER_PROPERTIES = 7;
public final static int AGENTS_LIST = 8;
}
