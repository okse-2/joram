/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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

import java.util.StringTokenizer;

/**
 *
 */
public class MetaData {
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
  	getVersion();
  	joramMagic[5] = (byte) major;
  	joramMagic[6] = (byte) minor;
  	joramMagic[7] = (byte) protocol;
  }
  
  private static void getVersion() {
  	// Read version from the package
  	Package pkg = MetaData.class.getPackage();
  	if (pkg != null) {
  		String implVersion = pkg.getImplementationVersion();
  		if (implVersion != null) {
  			version = implVersion;
  			StringTokenizer st = new StringTokenizer(implVersion, ".");
  			major = Integer.parseInt(st.nextToken());
  			minor = Integer.parseInt(st.nextToken());
  			build = Integer.parseInt(st.nextToken());
  			protocol = Integer.parseInt(st.nextToken("-").substring(1));
  		}
  	}
  }
  
//  static {
//    int idx1 = 0;
//    int idx2 = 0;
//    
//    try {
//      idx2 = version.indexOf('.');
//      if (idx2 != -1) {
//        joramMagic[5] = Byte.parseByte(version.substring(idx1, idx2));
//      } else {
//        throw new IllegalArgumentException();
//      }
//    
//      idx1 = idx2 +1;
//      idx2 = version.indexOf('.', idx1);
//      if (idx2 != -1) {
//        joramMagic[6] = Byte.parseByte(version.substring(idx1, idx2));
//      } else {
//        throw new IllegalArgumentException();
//      }
//    
//      idx1 = idx2 +1;
//      idx2 = version.indexOf('.', idx1);
//      if (idx2 != -1) {
//        joramMagic[7] = Byte.parseByte(version.substring(idx1 +1, idx2));
//      } else {
//        throw new IllegalArgumentException();
//      }
//    } catch (Exception exc) {
//      // Should never happen
//    }
//
//    joramMagic[5] = (byte) major;
//    joramMagic[6] = (byte) minor;
//    joramMagic[7] = (byte) protocol;    
//  }

}