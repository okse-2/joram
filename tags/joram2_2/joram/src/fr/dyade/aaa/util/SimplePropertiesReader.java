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

package fr.dyade.aaa.util;

import java.io.*;
import java.util.Hashtable;

public class SimplePropertiesReader {
  public static final String RCS_VERSION="@(#)$Id: SimplePropertiesReader.java,v 1.2 2002-01-16 12:46:47 joram Exp $"; 
  private Hashtable propertiesTable;
  private File file;

  public SimplePropertiesReader(File file) {
    this.file = file;
    propertiesTable = new Hashtable();
    putProperties();
  }

  public SimplePropertiesReader(String file) {
    this.file = new File(file);
    propertiesTable = new Hashtable();
    putProperties();
  }

  private String buildString(String line, int index) {
    StringBuffer buf = new StringBuffer();
    StringBuffer fbuf = new StringBuffer();
    for (int i=index; i<line.length(); i++) {
      char c = line.charAt(i);
      if (c=='\\') buf.append('\\');
      buf.append(c);
    }
    for (int i=0; i<buf.length(); i++) {
      char c = buf.charAt(i);
      if (c==' ' || c=='\t') continue;
      for (int j=i;j<buf.length();j++)
        fbuf.append(buf.charAt(j));
      break;
    }
    return fbuf.toString();
  }

  private void putProperties() {
    if (file.exists()) {
      try {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedReader in = new BufferedReader(new InputStreamReader(fileInputStream));
        while (true) {
          // Get next line
          String line = in.readLine();
          if (line == null) {
            fileInputStream.close();
            return;
          }

          if (line.length() > 0) {
            char firstChar = line.charAt(0);
            if (firstChar == '#') continue;
            int i = line.indexOf(' ');
            if (i<0) {
              i = line.indexOf('\t');
              if (i<0) continue;
            }
            String propName = line.substring(0,i);
            String propValue = buildString(line,i);
            try {
              propertiesTable.put(propName,propValue);
            } catch (NullPointerException e) {}
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public String getProperty(String prop) {
    return (String) propertiesTable.get(prop);
  }

  public String toString() {
    return propertiesTable.toString();
  }
}
