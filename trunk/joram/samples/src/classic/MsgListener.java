/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package classic;

import javax.jms.*;

/**
 * Implements the <code>javax.jms.MessageListener</code> interface.
 */
public class MsgListener implements MessageListener
{
  String ident = null;

  public MsgListener()
  {}

  public MsgListener(String ident)
  {
    this.ident = ident;
  }

  public void onMessage(Message msg)
  {
    try {
      if (msg instanceof TextMessage) {
        if (ident == null) 
          System.out.println(((TextMessage) msg).getText());
        else
          System.out.println(ident + ": " + ((TextMessage) msg).getText());
      }
      else if (msg instanceof ObjectMessage) {
        if (ident == null) 
          System.out.println(((ObjectMessage) msg).getObject());
        else
          System.out.println(ident + ": " + ((ObjectMessage) msg).getObject());
      }
    }
    catch (JMSException jE) {
      System.err.println("Exception in listener: " + jE);
    }
  }
}
