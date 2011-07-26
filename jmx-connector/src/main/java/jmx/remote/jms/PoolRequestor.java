/**
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): Djamel-Eddine Boumchedda
 * 
 */

package jmx.remote.jms;

/**
 * In the Class <b>PoolRequestor</b>,is produced the required number of requestor to allow a management tool (JConsole) to do monitoring of an applications  in multithreading.
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */

import javax.jms.Connection;

import fr.dyade.aaa.common.Pool;
import fr.dyade.aaa.util.Operation;

public class PoolRequestor {
  Connection connection;

  public PoolRequestor(Connection conn) {
    connection = conn;
  }

  private static Pool pool = null;

  public void initPool(int capacity) {
    pool = new Pool("Pool Requestor", capacity);
  }

  public Requestor allocRequestor() {
    Requestor requestor = null;

    try {
      requestor = (Requestor) pool.allocElement();
    } catch (Exception exc) {
      return new Requestor(connection);
    }

    return requestor;
  }

  public void freeRequestor(Requestor requestor) {
    pool.freeElement(requestor);
  }

}
