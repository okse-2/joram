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
 * Initial developer(s): Alexander Fedorowicz
 * Contributor(s): Andre Freyssinet (ScalAgent DT)
 */
package fr.dyade.aaa.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class AFastTransaction extends ATransaction {
  protected void newLogFile() throws IOException {
    logFile = new RandomAccessFile(logFilePN, "rwd");
  }

  protected void syncLogFile() throws IOException {
    // Since the file content is already synchronized with the
    // underlying device by using "rws" mode, we don't need to do
    // anything here.
  }
}
