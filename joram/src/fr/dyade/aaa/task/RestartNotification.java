/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import fr.dyade.aaa.agent.*;
import java.io.*;


/**
 * Notification requesting a <code>Task</code> agent to prepare for restart.
 * Should be called on agents with FAIL or STOP status.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Task
 */
public class RestartNotification extends Notification {

public static final String RCS_VERSION="@(#)$Id: RestartNotification.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 
}
