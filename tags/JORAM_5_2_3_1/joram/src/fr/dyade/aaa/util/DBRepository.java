/*
 * Copyright (C) 2006 - 2008 ScalAgent Distributed Technologies
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
import java.io.FileNotFoundException;

import java.util.Vector;
import java.util.Properties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *  This class allows to use a database through JDBC as repository with the
 * NTransaction module.
 *
 * @see NTransaction
 * @see Repository
 */
final class DBRepository implements Repository {
  String driver = "org.apache.derby.jdbc.EmbeddedDriver";
  String connurl = "jdbc:derby:";
//   String driver = "org.hsqldb.jdbcDriver";
//   String connurl = "jdbc:hsqldb:file:";

  File dir = null;

  private int nbsaved = 0;

  /**
   * Returns the number of save operation to repository.
   *
   * @return The number of save operation to repository.
   */
  public int getNbSavedObjects() {
    return nbsaved;
  }

  private int nbdeleted = 0;

  /**
   * Returns the number of delete operation on repository.
   *
   * @return The number of delete operation on repository.
   */
  public int getNbDeletedObjects() {
    return nbdeleted;
  }

  private int baddeleted = 0;

  /**
   * Returns the number of useless delete operation on repository.
   *
   * @return The number of useless delete operation on repository.
   */
  public int getNbBadDeletedObjects() {
    return baddeleted;
  }

  private int nbloaded = 0;

  /**
   * Returns the number of load operation from repository.
   *
   * @return The number of load operation from repository.
   */
  public int getNbLoadedObjects() {
    return nbloaded;
  }
  
  Connection conn = null;
  
  DBRepository() {}

  PreparedStatement insertStmt = null;
  PreparedStatement updateStmt = null;
  PreparedStatement deleteStmt = null;

  /**
   * Initializes the repository.
   * Opens the connection, evntually creates the database and tables.
   */
  public void init(File dir)  throws IOException {
    this.dir = dir;

    try {
      Class.forName(driver).newInstance();
//       conn = DriverManager.getConnection(connurl + new File(dir, "JoramDB").getPath() + ";shutdown=true;server.no_system_exit=true", "sa", "");
      Properties props = new Properties();
      props.put("user", "user1");
      props.put("password", "user1");

      conn = DriverManager.getConnection(connurl + new File(dir, "JoramDB").getPath() + ";create=true", props);
      conn.setAutoCommit(false);
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
//         s.execute("create cached table JoramDB(name VARCHAR PRIMARY KEY, content VARBINARY(256))");
      s.execute("CREATE TABLE JoramDB (name VARCHAR(256), content LONG VARCHAR FOR BIT DATA, PRIMARY KEY(name))");
      s.close();
      conn.commit();
    } catch (SQLException sqle) {
    }

    try {
      insertStmt = conn.prepareStatement("INSERT INTO JoramDB VALUES (?, ?)");
      updateStmt = conn.prepareStatement("UPDATE JoramDB SET content=? WHERE name=?");
      deleteStmt = conn.prepareStatement("DELETE FROM JoramDB WHERE name=?");
    } catch (SQLException sqle) {
      throw new IOException(sqle.getMessage());
    }
  }

  /**
   * Gets a list of persistent objects that name corresponds to prefix.
   *
   * @return The list of corresponding names.
   */
  public String[] list(String prefix) throws IOException {
    try {
      // Creating a statement lets us issue commands against the connection.
      Statement s = conn.createStatement();
      ResultSet rs = s.executeQuery("SELECT name FROM JoramDB WHERE name LIKE '" + prefix + "%'");

      Vector v = new Vector();
      while (rs.next()) {
        v.add(rs.getString(1));
      }
      rs.close();
      s.close();

      String[] result = new String[v.size()];
      result = (String[]) v.toArray(result);

      return result;
    } catch (SQLException sqle) {
      throw new IOException(sqle.getMessage());
    }
  }

  /**
   * Save the corresponding bytes array.
   */
  public void save(String dirName, String name, byte[] content) throws IOException {
    String fname = null;
    if (dirName == null) {
      fname = name;
    } else {
      fname = new StringBuffer(dirName).append('/').append(name).toString();
    }

    try {
      insertStmt.setString(1, fname);
      insertStmt.setBytes(2, content);
      insertStmt.executeUpdate();
    } catch (SQLException e) {
      try {
        updateStmt.setBytes(1, content);
        updateStmt.setString(2, fname);
        updateStmt.executeUpdate();
      } catch (SQLException sqle) {
        throw new IOException(sqle.getMessage());
      }
    }

    nbsaved += 1;
  }

//   /**
//    * Loads the object.
//    *
//    * @return The loaded object or null if it does not exist.
//    */
//   public Object loadobj(String dirName, String name) throws IOException, ClassNotFoundException {
//     byte[] content = load(dirName, name); 

//     ByteArrayInputStream bis = new ByteArrayInputStream(content);
//     ObjectInputStream ois = new ObjectInputStream(bis);
//     try {
//       Object obj = ois.readObject();
//       return obj;
//     } finally {
//       ois.close();
//       bis.close();
//     }
//   }

  /**
   * Loads the byte array.
   *
   * @return The loaded bytes array.
   */
  public byte[] load(String dirName, String name) throws IOException {
    String fname = null;
    if (dirName == null) {
      fname = name;
    } else {
      fname = new StringBuffer(dirName).append('/').append(name).toString();
    }

    try {
      // Creating a statement lets us issue commands against the connection.
      Statement s = conn.createStatement();
      //
      ResultSet rs = s.executeQuery("SELECT content FROM JoramDB WHERE name='" + fname + "'");

       if (!rs.next()) {
         throw new FileNotFoundException("Cannot find object " + fname);
       }

       byte[] content = rs.getBytes(1);

       rs.close();
       s.close();

       nbloaded += 1;
       return content;
    } catch (SQLException sqle) {
      throw new IOException(sqle.getMessage());
    }
  }

  /**
   * Deletes the corresponding objects in repository.
   */
  public void delete(String dirName, String name) throws IOException {
    String fname = null;
    if (dirName == null) {
      fname = name;
    } else {
      fname = new StringBuffer(dirName).append('/').append(name).toString();
    }

    int nb = 0;
    try {
      // Creating a statement lets us issue commands against the connection.
      Statement s = conn.createStatement();
      //
      nb = s.executeUpdate("DELETE FROM JoramDB WHERE name='" + fname + "'");
    } catch (SQLException sqle) {
      throw new IOException(sqle.getMessage());
    }
    
    if (nb != 1) baddeleted += 1;
    nbdeleted += 1;
  }

  /**
   * Commits all changes to the repository.
   */
  public void commit() throws IOException {
    try {
      conn.commit();
    } catch (SQLException sqle) {
      throw new IOException(sqle.getMessage());
    }
  }

  /**
   * Closes the repository.
   */
  public void close() throws IOException {
    try {
      conn.close();
    } catch (SQLException sqle) {
      throw new IOException(sqle.getMessage());
    }
  }
}
