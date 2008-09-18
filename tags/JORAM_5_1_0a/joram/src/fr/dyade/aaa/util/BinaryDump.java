/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.util;

import java.text.DecimalFormat;

/**
 *
 */
public class BinaryDump {
  public static final String EOL = System.getProperty("line.separator");

  private static final char _hexcodes[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  private static final int _shifts[] = {60, 56, 52, 48, 44, 40, 36, 32, 28, 24, 20, 16, 12, 8, 4, 0};

  /**
   * dump an array of bytes to a String
   *
   * @param data the byte array to be dumped
   * @param offset its offset, whatever that might mean
   * @param index initial index into the byte array
   *
   * @exception ArrayIndexOutOfBoundsException if the index is
   *            outside the data array's bounds
   * @return output string
   */
  public static String dump(final byte [] data,
                            final long offset,
                            final int index) {
    if ((index < 0) || (index >= data.length))
      throw new ArrayIndexOutOfBoundsException("illegal index: " + index + ", length=" + data.length);

    long display_offset = offset + index;
    StringBuffer buffer = new StringBuffer(74);

    for (int j = index; j < data.length; j += 16) {
      int chars_read = data.length - j;

      if (chars_read > 16) {
        chars_read = 16;
      }
      buffer.append(dump(display_offset)).append(' ');
      for (int k = 0; k < 16; k++) {
        if (k < chars_read) {
          buffer.append(dump(data[ k + j ]));
        } else {
          buffer.append("  ");
        }
        buffer.append(' ');
      }
      for (int k = 0; k < chars_read; k++) {
        if ((data[ k + j ] >= ' ') && (data[ k + j ] < 127)) {
          buffer.append(( char ) data[ k + j ]);
        } else {
          buffer.append('.');
        }
      }
      buffer.append(EOL);
      display_offset += chars_read;
    }                 
    return buffer.toString();
  }


  private static String dump(final long value) {
    StringBuffer buf = new StringBuffer();
    buf.setLength(0);
    for (int j = 0; j < 8; j++) {
      buf.append( _hexcodes[ (( int ) (value >> _shifts[ j + _shifts.length - 8 ])) & 15 ]);
    }
    return buf.toString();
  }

  private static String dump(final byte value) {
    StringBuffer buf = new StringBuffer();
    buf.setLength(0);
    for (int j = 0; j < 2; j++)
    {
      buf.append(_hexcodes[ (value >> _shifts[ j + 6 ]) & 15 ]);
    }
    return buf.toString();
  }

  /**
   * Dumps the byte array in hexadecimal format.
   *
   * @param value     The value to convert
   * @return          A String representing the array of bytes
   */
  public static String toHex(final byte[] value) {
    return toHex(value, 0, value.length);
  }

  /**
   * dump an array of bytes to a String
   *
   * @param data the byte array to be dumped
   * @param offset its offset, whatever that might mean
   * @param index initial index into the byte array
   *
   * @exception ArrayIndexOutOfBoundsException if the index is
   *            outside the data array's bounds
   * @return output string
   */
  public static String toHex(final byte[] value, int offset, int length) {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append('[');
    for(int i = offset; i < offset+length; i++) {
      strbuf.append(toHex(value[i]));
      strbuf.append(", ");
    }
    strbuf.append(']');
    return strbuf.toString();
  }

  /**
   * Converts the parameter to a hex value breaking the results into lines.
   *
   * @param value        The value to convert
   * @param bytesPerLine The maximum number of bytes per line. The next byte
   *                     will be written to a new line
   * @return             A String representing the array of bytes
   */
  public static String toHex(final byte[] value, final int bytesPerLine) {
    final int digits = (int) Math.round(Math.log(value.length) / Math.log(10) + 0.5);
    final StringBuffer formatString = new StringBuffer();
    for (int i = 0; i < digits; i++)
      formatString.append('0');
    formatString.append(": ");
    final DecimalFormat format = new DecimalFormat(formatString.toString());
    
    StringBuffer retVal = new StringBuffer();
    retVal.append(format.format(0));
    int i = -1;
    for(int x = 0; x < value.length; x++) {
      if (++i == bytesPerLine) {
        retVal.append('\n');
        retVal.append(format.format(x));
        i = 0;
      }
      retVal.append(toHex(value[x]));
      retVal.append(", ");
    }
    return retVal.toString();
  }

  /**
   * Converts the parameter to a hex value.
   *
   * @param value     The value to convert
   * @return          The result right padded with 0
   */
  public static String toHex(final byte value) {
    return toHex(value, 2);
  }

  private static String toHex(final long value, final int digits) {
    StringBuffer result = new StringBuffer(digits);
    for (int j = 0; j < digits; j++) {
      result.append( _hexcodes[ (int) ((value >> _shifts[ j + (16 - digits) ]) & 15)]);
    }
    return result.toString();
  }
}
