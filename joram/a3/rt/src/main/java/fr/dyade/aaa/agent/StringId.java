/*
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.agent;

public final class StringId {
  private final static int BUFLEN = 35;

  // Per-thread buffer for string/stringbuffer conversion
  private static ThreadLocal perThreadBuffer = new ThreadLocal() {
    protected synchronized Object initialValue() {
      return new char[BUFLEN];
    }
  };

  /**
   * Returns a string representation of id: label + x + sep + y + sep + z.
   * At least x must be provided and higher or equal to 0, if y or z are less
   * than 0 they are ignored.
   */
  public final static String toStringId(char label, char sep,
                                        int x, int y, int z) {
    int idx = BUFLEN;
    char[] buf = (char[]) (perThreadBuffer.get());
    if (z >= 0) {
      idx = getChars(z, buf, idx);
      buf[--idx] = sep;
    }
    if (y >= 0) {
      idx = getChars(y, buf, idx);
      buf[--idx] = sep;
    }
    idx = getChars(x, buf, idx);
    buf[--idx] = label;

    return new String(buf, idx, BUFLEN - idx);
  }

  final static char [] DigitTens = {
    '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
    '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
    '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
    '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
    '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
    '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
    '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
    '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
    '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
    '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
  } ; 

  final static char [] DigitOnes = { 
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  } ;

  private final static int getChars(int i, char[] buf, int idx) {
    int q, r;
    int charPos = idx;

    // Generate two digits per iteration
    while (i >= 65536) {
      q = i / 100;
      // really: r = i - (q * 100);
      r = i - ((q << 6) + (q << 5) + (q << 2));
      i = q;
      buf [--charPos] = DigitOnes[r];
      buf [--charPos] = DigitTens[r];
    }

    // Fall thru to fast mode for smaller numbers
    // assert(i <= 65536, i);
    for (;;) { 
      q = (i * 52429) >>> (16+3);
      r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
      buf [--charPos] = DigitOnes[r];
      i = q;
      if (i == 0) break;
    }
    return charPos;
  }
}
