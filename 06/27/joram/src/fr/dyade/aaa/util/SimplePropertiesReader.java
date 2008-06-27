/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */

package fr.dyade.aaa.util;

import java.io.*;
import java.util.Hashtable;

public class SimplePropertiesReader {
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
