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


/**
 * Provides a kind of unsigned byte functionality.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 */
public class Ubyte {

public static final String RCS_VERSION="@(#)$Id: Ubyte.java,v 1.7 2002-03-06 16:58:48 joram Exp $"; 


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
