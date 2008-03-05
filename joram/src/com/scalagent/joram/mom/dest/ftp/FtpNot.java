/*
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.ftp;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.messages.Message;
import java.util.Vector;

public class FtpNot extends ClientMessages {

  public FtpNot(int clientContext, 
                int requestId,
                Message msg) {
    super(clientContext, requestId);
    addMessage(msg);
  }
}
