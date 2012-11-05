/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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
 *
 * Initial developer(s): Dyade
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.util;

/**
 * Provides a kind of unsigned byte functionality.
 */
public class Ubyte {
  /**
   * Gives the unsigned value of a byte.
   */
  public static int unsignedValue(byte b) {
    return (b >= 0 ? (int) b : 0x100 + b);
  }

  /**
   * Gives the Byte value of an unsigned byte value.
   */
  public static byte signedValue(int b) {
    return (b <= Byte.MAX_VALUE ? (byte) b : (byte) (b - 0x100));
  }

  /**
   * Gives the hexa representation of unsigned value of the byte.
   */
  public static String toHexString(byte b) {
    String str = Integer.toHexString(unsignedValue(b));
    switch (str.length()) {
    case 2:
      return str;
    case 1:
      return "0" + str;
    default:
      // should never occur
      throw new IllegalArgumentException("error in Ubyte.toHexString(" + b + ")");
    }
  }

  /**
   * Gives the hexa representation of an array of bytes.
   *
   * @param buffer		array of bytes to print
   * @param start		index of first byte to print in buffer
   * @param stop		index of first byte not to print in buffer
   * @param bytesInBlock	number of bytes to print grouped, or 0
   * @param blockHeader		header for each block of bytes
   * @param blocksInLine	number of blocks to print in a line, or 0
   * @param lineHeader		header for each line
   * @param current		number of bytes already printed in previous calls
   */
  static final char hexaDigits[] = {
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };
  public static String toHexString(
    byte[] buffer, int start, int stop,
    int bytesInBlock, String blockHeader,
    int blocksInLine, String lineHeader,
    int current) {

    StringBuffer output = new StringBuffer();

    if (bytesInBlock == 0) bytesInBlock = Integer.MAX_VALUE;
    if (blocksInLine == 0) blocksInLine = Integer.MAX_VALUE;

    // bytes in block counter
    int k = bytesInBlock - (current % bytesInBlock);
    // blocks in line counter
    int j = blocksInLine - ((current / bytesInBlock) % blocksInLine);

    // finds first header
    String header;
    if (k != bytesInBlock) {
      header = "";
    } else if (j != blocksInLine) {
      header = blockHeader;
    } else {
      header = lineHeader;
    }

    int i = start;
  printLoop:
    while (true) {
      while (j-- > 0) {
	output.append(header);
	while (k-- > 0) {
	  // prints byte
	  int value = unsignedValue(buffer[i]);
	  output.append(hexaDigits[value / 0x10]);
	  output.append(hexaDigits[value % 0x10]);
	  // checks end of buffer
	  if (++i == stop)
	    break printLoop;
	}
	k = bytesInBlock;
	header = blockHeader;
      }
      output.append('\n');
      j = blocksInLine;
      header = lineHeader;
    }

    return output.toString();
  }

  /**
   * Gives the hexa representation of an array of bytes.
   *
   * @param buffer		array of bytes to print
   * @param start		index of first byte to print in buffer
   * @param stop		index of first byte not to print in buffer
   */
  public static String toHexString(byte[] buffer, int start, int stop) {
    return toHexString(buffer, start, stop, 2, " ", 8, "\t", 0);
  }
}
