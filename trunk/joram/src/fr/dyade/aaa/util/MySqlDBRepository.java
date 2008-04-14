/*
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): 
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.util;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import org.apache.commons.dbcp.BasicDataSource;

import fr.dyade.aaa.util.*;

/**
 *  This class allows to use a MySQL database as repository with the
 * NTransaction module.
 * <p>
 * The basic setup at the code to get Database code to run is the same.
 *
 * The additional thing when using MySQL database is:
 * <ol>
 * <li>For the a3servers.xml that will be copy to run directory, we have
 * to add these:
 * <pre>
 *   &lt;property name="Transaction" value="fr.dyade.aaa.util.NTransaction"/&gt;
 *   &lt;property name="NTRepositoryImpl" value="fr.dyade.aaa.util.MySqlDBRepository"/&gt;
 *   &lt;property name="DBDriver" value="org.gjt.mm.mysql.Driver"/&gt;
 *   &lt;property name="ConnURL" value="jdbc:mysql://hostname:3306/instance"/&gt;
 *   &lt;property name="DBUser" value="dbUserName"/&gt;
 *   &lt;property name="DBPass" value="dbPassword"/&gt;
 * </pre>
 * <li>In the start script in the bin directory:
 * <ol>
 * <li>We have to add the following library
 * <pre>
 * CLASSPATH=$CLASSPATH:$VIATOR_LIB/mysql-connector-java-5.0.5-bin.jar
 * CLASSPATH=$CLASSPATH:$VIATOR_LIB/commons-dbcp-1.2.1.jar
 * CLASSPATH=$CLASSPATH:$VIATOR_LIB/commons-pool-1.3.jar
 * </pre>
 * <li>change the java command to add in a few -D parameter
 * <pre>
 * JAVA_ARGS=$JAVA_ARGS"
 * -DNTRepositoryImpl=fr.dyade.aaa.util.MySqlDBRepository
 * -DDBDriver=org.gjt.mm.mysql.Driver
 * -DConnURL=jdbc:mysql://hostname:3306/instance
 * -DDBUser=dbUserName
 * -DDBPass=dbPassword"
 * </pre>
 * <li>may need to up size the max memory for better performace
 * This is what we use: JAVA_ARGS=" -Xms100M -Xmx1024M "
 * </ol>
 * <li>At the MySQL database side:
 * <ol>
 * <li>we have to set the max-allowed-packet to a bigger size, I think the
 * default is 1M, if the number of messages become large, it will fail.
 * "max_allowed_packet=16M"
 * <li>create the JoramDB table first (we decided that we will create it
 * outside here instead of in the code.
 * <pre>
 * CREATE TABLE JoramDB (name VARCHAR(256), content longblob, primary key(name));
 * </pre>
 * </ol>
 *
 * @see NTransaction
 * @see Repository
 */
final class MySqlDBRepository implements Repository {
  /*
  String driver = "org.apache.derby.jdbc.EmbeddedDriver";
  String connurl = "jdbc:derby:";
//   String driver = "org.hsqldb.jdbcDriver";
//   String connurl = "jdbc:hsqldb:file:";
  */
  String driver = System.getProperty("DBDriver", "org.gjt.mm.mysql.Driver");
  String connurl = System.getProperty("ConnURL", "jdbc:mysql://localhost:3306/mysql");
  String user = System.getProperty("DBUser", "root");
  String pass = System.getProperty("DBPass", "");
 
  BasicDataSource ds = null;
  boolean reconnectLoop = false;

  public static Logger logger =
      Debug.getLogger("fr.dyade.aaa.util.MySqlDBRepository");

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
  
  MySqlDBRepository() {}

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
      /*
      props.put("user", "user1");
      props.put("password", "user1");
      */
      props.put("user", user);
      props.put("password", pass);

      /*
      conn = DriverManager.getConnection(connurl + new File(dir, "JoramDB").getPath() + ";create=true", props);
      */
      // conn = DriverManager.getConnection(connurl, props); // MySQL (the database must exist and start seperately)
      conn = getConnection();
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
      /*
      s.execute("CREATE TABLE JoramDB (name VARCHAR(256), content LONG VARCHAR FOR BIT DATA, PRIMARY KEY(name))");
      */
      s.execute("CREATE TABLE JoramDB (name VARCHAR(256), content longblob, primary key(name))"); // MySQL
      s.close();
      conn.commit();
    } catch (SQLException sqle) {
        String exceptionString = sqle.toString();
        if (exceptionString.indexOf("CREATE command denied") == -1)
        {
            sqle.printStackTrace();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    try {
      insertStmt = conn.prepareStatement("INSERT INTO JoramDB VALUES (?, ?)");
      updateStmt = conn.prepareStatement("UPDATE JoramDB SET content=? WHERE name=?");
      deleteStmt = conn.prepareStatement("DELETE FROM JoramDB WHERE name=?");
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      throw new IOException(sqle.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      // throw e;
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
      if (sqle instanceof com.mysql.jdbc.CommunicationsException && !reconnectLoop)
      {
          logger.log(BasicLevel.WARN, "Database reconnection problem at list, Reconnecting");
          reconnection();
          reconnectLoop = true;
          String[] result = list(prefix);
          reconnectLoop = false;
          return result;
      } else {
          if (reconnectLoop)
              logger.log(BasicLevel.WARN, "Database reconnection problem at list");

          logger.log(BasicLevel.WARN, "list, problem list " + prefix);
          sqle.printStackTrace();
          throw new IOException(sqle.getMessage());
      }
    } catch (Exception e) {
        logger.log(BasicLevel.WARN, "list, problem list " + prefix + " in e with " + e.getMessage());
        e.printStackTrace();
        throw new IOException(e.getMessage());
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

    boolean requireReconnect = false;

    try {
      insertStmt.setString(1, fname);
      insertStmt.setBytes(2, content);
      insertStmt.executeUpdate();
    } catch (SQLException e) {
      // e.printStackTrace();
      try {
        updateStmt.setBytes(1, content);
        updateStmt.setString(2, fname);
        updateStmt.executeUpdate();
      } catch (com.mysql.jdbc.PacketTooBigException ptbe) {
        System.err.println("MySqlDBRepository.save - was trying to save\ndirName: " + dirName + "\nname: " + name);
        ptbe.printStackTrace();
      } catch (SQLException sqle) {
          if (sqle instanceof com.mysql.jdbc.CommunicationsException && !reconnectLoop)
          {
              logger.log(BasicLevel.WARN, "Database CommunicationsException problem at save, requires Reconnection");
              requireReconnect = true;
          } else {
              if (reconnectLoop)
                 logger.log(BasicLevel.WARN, "Database reconnection problem at save. Still have CommunicationsException");

              logger.log(BasicLevel.WARN, "save, CommunicationsException problem saving " + name);
              sqle.printStackTrace();
              // throw new IOException(sqle.getMessage());
          }
      } catch (NullPointerException ne) {
          if (!reconnectLoop)
          {
              logger.log(BasicLevel.WARN, "Database NullPointerException problem at save, requires Reconnection");
              requireReconnect = true;
          } else {
              if (reconnectLoop)
                 logger.log(BasicLevel.WARN, "Database reconnection problem at save. Still have NullPointerException");

              logger.log(BasicLevel.WARN, "save, NullPointerException problem saving " + name);
              ne.printStackTrace();
              // throw new IOException(sqle.getMessage());
          }
      } catch (Exception e2) {
        logger.log(BasicLevel.WARN, "save, problem saving " + name + " in e2 with " + e2.getMessage());
        e2.printStackTrace();
        // throw e2;
      }
    }

    if (requireReconnect)
    {
        logger.log(BasicLevel.WARN, "Database problem at save, Reconnecting");
        reconnection();
        reconnectLoop = true;
        save(dirName, name, content);
        reconnectLoop = false;
    }

    nbsaved += 1;
  }

  /**
   * Loads the object.
   *
   * @return The loaded object or null if it does not exist.
   */
  public Object loadobj(String dirName, String name) throws IOException, ClassNotFoundException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "loadobj, b4 load call");

    byte[] content = load(dirName, name); 

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "loadobj, after load call");

    ByteArrayInputStream bis = new ByteArrayInputStream(content);
    ObjectInputStream ois = new ObjectInputStream(bis);
    try {
      Object obj = ois.readObject();
      return obj;
    } catch (Exception e) {
      String exceptionString = e.toString();
      if (exceptionString.indexOf("KNOWN PROBLEM") == -1)
      {
          e.printStackTrace();
      }
      throw new IOException(e.getMessage());
    } finally {
      ois.close();
      bis.close();
    }
  }

  /**
   * Loads the byte array.
   *
   * @return The loaded bytes array.
   */
  public byte[] load(String dirName, String name) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "load called");

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
         throw new FileNotFoundException("Cannot find object in JoramDB " + ("serverCounter".equals(fname)?"[KNOWN PROBLEM] ":"") + fname);
       }

       byte[] content = rs.getBytes(1);

       rs.close();
       s.close();

       if (logger.isLoggable(BasicLevel.DEBUG))
           logger.log(BasicLevel.DEBUG, "load, after database call");

       nbloaded += 1;
       return content;
    } catch (SQLException sqle) {
      if (sqle instanceof com.mysql.jdbc.CommunicationsException && !reconnectLoop)
      {
          logger.log(BasicLevel.WARN, "Database reconnection problem at load, Reconnecting");
          reconnection();
          reconnectLoop = true;
          byte[] content = load(dirName, name);
          reconnectLoop = false;
          return content;
      } else {
          if (reconnectLoop)
              logger.log(BasicLevel.WARN, "Database reconnection problem at load");

          logger.log(BasicLevel.WARN, "load, problem load " + name);
          sqle.printStackTrace();
          throw new IOException(sqle.getMessage());
      }
    } catch (Exception e) {
       String exceptionString = e.toString();
       if (exceptionString.indexOf("KNOWN PROBLEM") == -1)
       {
          logger.log(BasicLevel.WARN, "load, problem load " + name + " in e with " + e.getMessage());
          e.printStackTrace();
       }
       throw new FileNotFoundException(e.getMessage());
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
      if (sqle instanceof com.mysql.jdbc.CommunicationsException && !reconnectLoop)
      {
          logger.log(BasicLevel.WARN, "Database reconnection problem at delete, Reconnecting");
          reconnection();
          reconnectLoop = true;
          delete(dirName, name);
          reconnectLoop = false;
      } else {
          if (reconnectLoop)
              logger.log(BasicLevel.WARN, "Database reconnection problem at delete");

          logger.log(BasicLevel.WARN, "delete, problem delete " + name);
          sqle.printStackTrace();
          throw new IOException(sqle.getMessage());
      }
    } catch (Exception e) {
      logger.log(BasicLevel.WARN, "delete, problem delete " + name + " in e with " + e.getMessage());
      e.printStackTrace();
      // throw e;
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
      sqle.printStackTrace();
      throw new IOException(sqle.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      // throw e;
    }
  }

  /**
   * Closes the repository.
   */
  public void close() throws IOException {
    try {
      conn.close();
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      throw new IOException(sqle.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      // throw e;
    }

    if (ds != null)
    try {
        ds.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  // public Connection getConnection() throws SQLException
  private Connection getConnection() throws SQLException
  {
      ds = new BasicDataSource();
      ds.setDriverClassName(driver);
      ds.setUsername(user);
      ds.setPassword(pass);
      ds.setUrl(connurl);

      return ds.getConnection();
  }

  private void closeConnection()
  {
      logger.log(BasicLevel.WARN, "closeConnection in progress");
      try
      {
          if (ds != null)
          {
              ds.close();
          }
      } catch (Exception e) {}

      logger.log(BasicLevel.WARN, "closeConnection success");
      ds = null;
  }

  private void reconnection() throws IOException
  {
      logger.log(BasicLevel.WARN, "reconnection in progress - starting");
      try {
          if (ds != null)
          {
              // conn = ds.getConnection();
              closeConnection(); // attempt to close all connection and recreate DataSource.
          }
          logger.log(BasicLevel.WARN, "reconnection in progress - old connection closed");
          conn = getConnection();
          logger.log(BasicLevel.WARN, "reconnection in progress - getConnection success");
          conn.setAutoCommit(false);

          insertStmt = conn.prepareStatement("INSERT INTO JoramDB VALUES (?, ?)");
          updateStmt = conn.prepareStatement("UPDATE JoramDB SET content=? WHERE name=?");
          deleteStmt = conn.prepareStatement("DELETE FROM JoramDB WHERE name=?");
      } catch (Exception sqle) {
          sqle.printStackTrace();
          throw new IOException(sqle.getMessage());
      }
      logger.log(BasicLevel.WARN, "Database reconnection success");
  }
}
