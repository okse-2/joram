/*
 * Copyright (C) 2007 - 2012 ScalAgent Distributed Technologies
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 *  The MySQLDBTransaction class implements a transactionnal storage through
 * a MySQL database.
 *
 * @see Transaction
 */
public final class MySQLDBTransaction extends DBTransaction {

  private String driver = "com.mysql.jdbc.Driver";
  private String connurl = "jdbc:mysql:";

  protected void initDB() throws IOException {
    
    String configFile = System.getProperty("MySQLDBTransactionConfigFile", "MySQL.properties");
    Properties prop = new Properties();
    try {
      prop.load(new FileInputStream(configFile));
    } catch (FileNotFoundException e) {
      if (logmon.isLoggable(BasicLevel.INFO)) {
        logmon.log(BasicLevel.INFO, "File " + configFile + " not found, using default parameters");
      }
    }
    
    String dbHost = prop.getProperty("host", "localhost");
    String dbPort = prop.getProperty("port", "3306");
    String dbName = prop.getProperty("database", "joramdb");
    String dbUser = prop.getProperty("user", "joram");
    String dbPass = prop.getProperty("password", "joram");
    
    try {
      Class.forName(driver).newInstance();
      StringBuffer sb = new StringBuffer(connurl);
      sb.append("//");
      sb.append(dbHost);
      sb.append(':');
      sb.append(dbPort);
      sb.append('/');
      sb.append(dbName);
      sb.append("?user=");
      sb.append(dbUser);
      sb.append("&password=");
      sb.append(dbPass);
      Enumeration enu = prop.keys();
      while (enu.hasMoreElements()) {
        String key = (String)enu.nextElement();
        if (key.equals("host") || key.equals("port") || key.equals("database") || key.equals("user") || key.equals("password")) {
          continue;
        }
        sb.append('&');
        sb.append(key);
        sb.append('=');
        sb.append(prop.get(key));
      }
      conn = DriverManager.getConnection(sb.toString());
      conn.setAutoCommit(false);
    } catch (SQLException e) {
      throw new IOException(e.getMessage());
    } catch (InstantiationException e) {
      throw new IOException(e.getMessage());
    } catch (IllegalAccessException e) {
      throw new IOException(e.getMessage());
    } catch (ClassNotFoundException e) {
      throw new IOException(e.getMessage());
    }

    try {
      // Creating a statement lets us issue commands against the connection.
      Statement s = conn.createStatement();
      
      // We create the table.
      s.execute("CREATE TABLE JoramDB (name VARCHAR(255), content LONGBLOB, PRIMARY KEY(name))");

      s.close();
      conn.commit();
    } catch (SQLException sqle) {
      if (logmon.isLoggable(BasicLevel.INFO))
        logmon.log(BasicLevel.INFO, "DBTransaction, init() DB already exists");
    }
  }
}
