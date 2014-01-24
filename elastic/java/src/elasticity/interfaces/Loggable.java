/*
 *  JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 - 2014 ScalAgent Distributed Technologies
 * Copyright (C) 2013 - 2014 Université Joseph Fourier
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
 * Initial developer(s): Université Joseph Fourier
 * Contributor(s): ScalAgent Distributed Technologies
 */

package elasticity.interfaces;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class Loggable {
	protected Logger logger;

	/**
	 * Initializes the logger Object.
	 * 
	 * @throws Exception
	 */
	protected void initLogger() throws Exception {
		//Initializes the logger.
		String currentClassName = this.getClass().getName();
		FileHandler fh = new FileHandler(currentClassName+".log", false);
		SimpleFormatter formatter = new SimpleFormatter();
		fh.setFormatter(formatter);
		logger = Logger.getLogger(currentClassName);
		logger.addHandler(fh);
		logger.setLevel(Level.ALL);

	}
}
