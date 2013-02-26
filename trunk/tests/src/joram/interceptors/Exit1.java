/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s): 
 */
package joram.interceptors;

import java.util.Properties;

import org.objectweb.joram.mom.util.MessageInterceptor;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

public class Exit1 implements MessageInterceptor {

	 public static Logger logger = Debug.getLogger(Exit1.class.getName());
	 
	public boolean handle(Message m, Properties prop) {
	  logger.log(BasicLevel.DEBUG, getClass().getName() + " prop = " + prop);
		logger.log(BasicLevel.DEBUG, getClass().getName() + " m = " + m);
		if (m != null)
			m.setProperty("interceptor.1", "Exit1");
		return true;
	}

  public void init(Properties prop) {
    logger.log(BasicLevel.DEBUG, getClass().getName() + " prop = " + prop);
  }

}
