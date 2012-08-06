/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - ScalAgent Distributed Technologies
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
package org.ow2.joram.shell;

//TODO: to be moved
public class ShellDisplay {
  
  static void displayTable(String[][] table) {
    displayTable(table, false);
  }
  
  //TODO: Needs simplifications...
  public static void displayTable(String[][] table, boolean header) {
    if(table == null || table.length==0 || table[0].length == 0)
      return;
    int nbRow = table.length;
    int nbCol = table[0].length;
    for(int i = 1; i < nbRow; i++)
      if(table[i].length != nbCol) {
        System.err.println("Error: Invalid table.");
        return;
      }
    boolean lineheader = header;
    
    //Determines the maximum width of each column
    int[] maxWidth = new int[nbCol];
    for(int c = 0; c < nbCol; c++)
      for(int r = 0; r < nbRow; r++)
        try {
        //If it's the first row or if the current value of column is a new maximum
        if(r==0 || maxWidth[c]<table[r][c].length())
          maxWidth[c]=table[r][c].length();
        } catch(NullPointerException e) {
          System.err.println("Null pointer exception");
          System.err.println("table["+r+"]["+c+"] = " + table[r][c]==null?"null":table[r][c]);
          return;
        }
    
    //Prints the table
    for(int r = 0; r < nbRow; r++) {
      for(int c = 0; c < nbCol; c++) {
        //The first line (headers) has been displayed -> Print the line
        if(lineheader && r == 1) {
          for(int i=0; (c<nbCol-1 && i < maxWidth[c]+3) || i < maxWidth[c]; i++)
            if(i==maxWidth[c]+1)
              System.out.print('+');
            else
              System.out.print('-');
          //To stay on r=1
          if(c==nbCol-1) {
            r--;
            lineheader=false;
          }
        } else {
          System.out.print(table[r][c]);
          //Fill with blank to match the maximum size of the column
          for(int i = table[r][c].length(); i < maxWidth[c]; i++)
            System.out.print(" ");
        }
        //On any row except the line separator
        if(header && c != nbCol-1 && (r != 1 || !lineheader))
          System.out.print(" ! ");
        else if (!lineheader)
          System.out.print("\t");
      }
      System.out.println();
    }
  } 
}
