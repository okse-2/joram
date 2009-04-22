/*
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
package framework;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * This class checks the report file to find 'FAILED' occurrences and exits in
 * an error status if found. This is useful to show errors when running tests on
 * <a href="http://forge.ow2.org/bamboo/browse/JORAM">bamboo</a>.
 */
public class CheckReportFile {

  public static void main(String[] args) throws Exception {
    FileReader fr = new FileReader(System.getProperty("framework.TestCase.OutFile"));
    BufferedReader br = new BufferedReader(fr);
    String line = br.readLine();
    int failedCount = 0;

    while (line != null) {
      if (line.indexOf("FAILED") != -1) {
        failedCount++;
        System.out.println(line);
      }
      line = br.readLine();
    }

    System.out.println();
    System.out.println("Failed tests: " + failedCount);
    if (failedCount > 0) {
      System.exit(-1);
    } else {
      System.exit(0);
    }
  }

}
