/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 - ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.ow2.joram.mom.amqp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 *
 */
public class MetaData {
	 public static Logger logger = Debug.getLogger(MetaData.class.getName());
	 
  /** Joram AMQP implementation version. */
  public static String version = "0.0.0";
  
  /** Joram's implementation version. */
  public static String joram_version = "0.0.0";
  
  static {
  	getVersionInResource();
  }
   
  private static void getVersionInResource() {
  	String implVersion = null;
  	String implJoramVersion = null;
  	// Read version from joramAMQP.version file in bundle. 
  	try {
  		InputStream in = MetaData.class.getResourceAsStream("/joramAMQP.version");
  		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
  		String v = reader.readLine();
  		while (v != null) {
  			if (v.contains("amqp.version"))
  				implVersion = (v.substring(v.indexOf('=')+1, v.length())).trim();
  			else if (v.contains("joram.version"))
  				implJoramVersion = (v.substring(v.indexOf('=')+1, v.length())).trim();
  			v = reader.readLine();
  		}

  	} catch (Exception e) {
  		if (logger.isLoggable(BasicLevel.DEBUG))
  			logger.log(BasicLevel.DEBUG, "MetaData.getVersionInResource:: EXCEPTION", e);
  	}
  	if (implVersion != null) {
  		version = implVersion.trim();
  	}
  	if (implJoramVersion != null) {
  		joram_version = implJoramVersion.trim();
  	}
  }
}
