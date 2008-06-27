/*
 * Copyright (C) 2007 - 2008 ScalAgent Distributed Technologies
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

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 *  The DerbyDBTransaction class implements a transactionnal storage through
 * a Derby embeded database.
 *
 * @see Transaction
 */
public final class DerbyDBTransaction extends DBTransaction {

  private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
  private String connurl = "jdbc:derby:";

  protected void initDB() throws IOException {
    try {
      Class.forName(driver).newInstance();
      Properties props = new Properties();
      props.put("user", "user1");
      props.put("password", "user1");
      conn = DriverManager.getConnection(connurl + new File(dir, "JoramDB").getPath() + ";create=true", props);
    } catch (IllegalAccessException exc) {
      throw new IOException(exc.getMessage());
    } catch (ClassNotFoundException exc) {
      throw new IOException(exc.getMessage());
    } catch (InstantiationException exc) {
      throw new IOException(exc.getMessage());
    } catch (SQLException sqle) {
      throw new IOException(sqle.getMessage());
    }

    try {
      // Creating a statement lets us issue commands against the connection.
      Statement s = conn.createStatement();

      // We create the table.
      s.execute("CREATE TABLE JoramDB (name VARCHAR(256), content LONG VARCHAR FOR BIT DATA, PRIMARY KEY(name))");

      s.close();
      conn.commit();
    } catch (SQLException sqle) {
      if (logmon.isLoggable(BasicLevel.INFO))
        logmon.log(BasicLevel.INFO, "DBTransaction, init() DB already exists");
    }
  }
}
