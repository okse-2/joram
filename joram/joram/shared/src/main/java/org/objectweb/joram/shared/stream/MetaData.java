/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.stream;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 *
 */
public class MetaData {
	public static Logger logger = Debug.getLogger(MetaData.class.getName());
	
  public static byte[] joramMagic = {'J', 'O', 'R', 'A', 'M', 0, 0, 0};
  
  /** Joram's implementation version. */
  public static String version = "major.minor.build.protocol";
  
  /** Joram's major version number */
  public static int major = 0;
  
  /** Joram's minor version number */
  public static int minor = 0;
  
  /** Joram's build version number */
  public static int build = 0;
  
  /** Joram's protocol version number */
  public static int protocol = 0;
  
  static {
  	getVersionInResource();
  	joramMagic[5] = (byte) major;
  	joramMagic[6] = (byte) minor;
  	joramMagic[7] = (byte) protocol;
  }
   
  private static void getVersionInResource() {
  	String implVersion = null;
  	// Read version from joram.version file in bundle. 
  	try {
  		InputStream in = MetaData.class.getResourceAsStream("/joram.version");
  		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
  		String v = reader.readLine();
  		while (v != null) {
  			if (v.contains("version"))
  				implVersion = (v.substring(v.indexOf('=')+1, v.length())).trim();
  			else if (v.contains("protocol"))
  				protocol = Integer.parseInt(v.substring(v.indexOf('=')+1, v.length()).trim());
  			v = reader.readLine();
  		}

  	} catch (Exception e) {
  		if (logger.isLoggable(BasicLevel.DEBUG))
	      logger.log(BasicLevel.DEBUG, "MetaData.getVersionInResource:: EXCEPTION", e);
  	}
  	if (implVersion != null) {
  		try {
  			version = implVersion.trim();
  			StringTokenizer st = new StringTokenizer(implVersion, ".");
  			major = Integer.parseInt(st.nextToken());
  			minor = Integer.parseInt(st.nextToken());
  			String b = st.nextToken();
  			//remove -SNAPSHOT
  			int i = b.indexOf("-");
  			if (i > 0)
  				b = b.substring(0, i);
  			build = Integer.parseInt(b);
  		} catch (Exception e) {
  			if (logger.isLoggable(BasicLevel.DEBUG))
  	      logger.log(BasicLevel.DEBUG, "MetaData.getVersionInResource:: EXCEPTION", e);
  		}
  	}
  }
}
