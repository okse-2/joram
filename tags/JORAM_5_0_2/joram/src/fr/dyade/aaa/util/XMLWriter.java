/*
 * Copyright (C) 2000 ScalAgent Distributed Technologies
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
 */

package fr.dyade.aaa.util;

import java.io.*;

public class XMLWriter {

  /**
   * Prints a string with quotes and quoted special characters.
   *
   * @param out		stream to write the string to
   * @param str		string to write
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static void print(PrintWriter out, String str) throws Exception {
    if (str == null) {
      out.print("\"\"");
      return;
    }

    out.print('"');
    int max = str.length();
    for (int i = 0; i < max; i ++) {
      // gets the numeric value of the unicode character
      int b = (int) str.charAt(i);
      if ((b >= 32) && (b <= 126)) {
	// ASCII printable character, includes special characters
	// '"', '\'', '<', '>', '&', '%'
	switch (b) {
	case (int) '"':
	  out.write("&quot;");
	  break;
	case (int) '\'':
	  out.write("&apos;");
	  break;
	case (int) '<':
	  out.write("&lt;");
	  break;
	case (int) '>':
	  out.write("&gt;");
	  break;
	case (int) '&':
	  out.write("&amp;");
	  break;
	case (int) '%':
	  out.write("&#x25;");
	  break;
	default:
	  out.write(b);
	  break;
	}
      } else {
	if (b == 0) {
	  out.write("&#x0;");
	} else {
	  int[] xvalue = new int[4];
	  int len = 0;
	  while (b > 0) {
	    xvalue[len] = b & 0xf;
	    if (xvalue[len] < 10)
	      xvalue[len] += (int) '0';
	    else
	      xvalue[len] += ((int) 'a') - 10;
	    b >>= 4;
	    len ++;
	  }
	  out.write("&#x");
	  while (len-- > 0)
	    out.write(xvalue[len]);
	  out.write(';');
	}
      }
    }
    out.print('"');
  }
}
