/*
 * Copyright (C) 2005 - 2012 ScalAgent Distributed Technologies
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
 */
package org.objectweb.joram.client.jms.admin.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.Random;

import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.NameAlreadyUsedException;
import org.objectweb.joram.client.jms.admin.Server;
import org.objectweb.joram.client.jms.admin.StartFailureException;
import org.objectweb.joram.client.jms.admin.UnknownServerException;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 * This class starts a Joram server without almost any configuration.
 * It just needs to know an existing Joram server (host, port) and 
 * the root login. The existing server is usually "s0" from the base configuration.
 * These data are specified by 4 environment properties: ADMIN_HOST_NAME, 
 * ADMIN_PORT, ROOT_USER_NAME and ROOT_USER_PWD.<br><br>
 *
 * This server uses the current directory to store some data. You can specify another
 * directory with the property BASE_DIR_PATH.<br><br>
 * 
 * This new server is added into the first domain found in the Joram platform.
 * If no domain exists, a first domain D0 is created. Notice that this bootstrap
 * mechanism has been designed for a single domain platform. If you need to build
 * more complex configuration with several domains you must use the raw Joram administration API.
 * 
 */
public class ZeroconfJoramServer {

  public static final String BASE_DIR_PATH = "org.objectweb.joram.zeroconf.baseDirPath";
  
  public static final String ADMIN_HOST_NAME = "org.objectweb.joram.zeroconf.adminHostName";
  
  public static final String ADMIN_PORT = "org.objectweb.joram.zeroconf.adminPort";

  public static final String ROOT_USER_NAME = "org.objectweb.joram.zeroconf.rootUserName";

  public static final String ROOT_USER_PWD = "org.objectweb.joram.zeroconf.rootUserPwd";

  public final static String SERVER_ID = "sid";

  public final static String JORAM_SERVER_DATA = "joramServerData";

  public final static String A3_SERVERS_XML = "a3servers.xml";

  private static String baseDirPath;

  private static File baseDir;

  private static String hostName;

  private static String serverName;

  private static void init() throws Exception {
    baseDirPath = System.getProperty(BASE_DIR_PATH, ".");
    baseDir = new File(baseDirPath);

    hostName = InetAddress.getLocalHost().getHostName();
    serverName = hostName + '/' + baseDir.getCanonicalPath();
  }

  private static void adminConnect() throws Exception {
    String adminHostName = System.getProperty(ADMIN_HOST_NAME, "localhost");
    int adminPort = Integer.getInteger(ADMIN_PORT, 16010).intValue();
    String rootUserName = System.getProperty(ROOT_USER_NAME, "root");
    String rootUserPwd = System.getProperty(ROOT_USER_PWD, "root");
    
    ConnectionFactory cf = TcpConnectionFactory.create(adminHostName, adminPort);
    ((TcpConnectionFactory) cf).getParameters().connectingTimer = 60;
    AdminModule.connect(cf, rootUserName, rootUserPwd);
  }

  /**
   * Starts a Joram server without any configuration.
   */
  public static void main(String[] args) throws Exception {
    init();

    int serverId;
    try {
      serverId = loadServerId();
    } catch (IOException exc) {
      baseDir.mkdir();
      adminConnect();

      int adminServerId = AdminModule.getLocalServerId();
      String[] domainNames = AdminModule.getDomainNames(adminServerId);

      String domainName;
      if (domainNames.length > 0) {
        domainName = domainNames[0];
      } else {
        domainName = "D0";
        int initialDomainPort = 17000;
        int tryNb = 50;
        int i = 0;
        Random random = new Random();
        while (i < tryNb) {
          try {
            AdminModule.addDomain("D0", adminServerId,
                                  initialDomainPort + random.nextInt(tryNb) * 100);
            break;
          } catch (NameAlreadyUsedException exc1) {
            throw exc1;
          } catch (StartFailureException exc2) {
            // Try again with a new port
            i++;
          }
        }
      }

      ServerSocket sock1 = new ServerSocket(0);
      int port1 = sock1.getLocalPort();
      ServerSocket sock2 = new ServerSocket(0);
      int port2 = sock2.getLocalPort();

      serverId = newServerId();

      AdminModule.addServer(serverId, hostName, 
                            domainName,  port1, 
                            serverName,
                            new String[]{"org.objectweb.joram.mom.proxies.tcp.TcpProxyService"},
                            new String[] { String.valueOf(port2) });

      String configXml = AdminModule.getConfiguration();
      File sconfig = new File(baseDir, A3_SERVERS_XML);
      FileOutputStream fos = new FileOutputStream(sconfig);
      PrintWriter pw = new PrintWriter(fos);
      pw.println(configXml);
      pw.flush();
      fos.getFD().sync();
      pw.close();
      fos.close();

      // Release the ports
      sock1.close();
      sock2.close();
    }
    startServer(serverId);

    AdminModule.disconnect();
  }
  
  private static void startServer(int serverId) throws Exception {
    System.getProperties().put(AgentServer.CFG_DIR_PROPERTY, baseDirPath);
    AgentServer.init((short) serverId, 
                     new File(baseDir, JORAM_SERVER_DATA).getPath(), 
                     null);
    AgentServer.start();

    User.create("anonymous", "anonymous", serverId);
  }

  /**
   * Allocate a new identifier for the created server.
   * 
   * @return The new identifier.
   * @throws Exception
   */
  private static int newServerId() throws Exception {
    List serverIds = AdminModule.getServersIds();
    int newSid = 0;
    for (int i = 0; i < serverIds.size(); i++) {
      Integer sid = (Integer)serverIds.get(i);
      if (newSid <= sid.intValue()) newSid = sid.intValue() + 1;
    }
    saveServerId(newSid);
    return newSid;
  }

  /**
   * Save the unique identifier of the created server for future use.
   * 
   * @param sid The unique identifier of the created server.
   * @throws IOException An error occurs during writing.
   */
  private static void saveServerId(int sid) throws IOException {
    FileOutputStream fos = new FileOutputStream(new File(baseDir, SERVER_ID));
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeInt(sid);
    oos.flush();
    fos.flush();
    fos.getFD().sync();
    fos.close();
  }

  /**
   * Try to get a previously created configuration.
   * 
   * @return The unique identifier of the server
   * @throws IOException An error occurs during reading.
   */
  private static int loadServerId() throws IOException {
    FileInputStream fis = new FileInputStream(new File(baseDir, SERVER_ID));
    ObjectInputStream ois = new ObjectInputStream(fis);
    int res = ois.readInt();
    fis.close();
    return res;
  }

  /**
   * Stop the created server.
   */
  public static void stop() {
    AgentServer.stop();
  }

  /**
   * Destroy the created server.
   * Remove it from the global configuration then clean the datas.
   * 
   * @throws Exception
   */
  public static void destroy() throws Exception {
    init();
    if (baseDir.exists()) {
      int serverId;

      try {
        serverId = loadServerId();
      } catch (IOException exc) {
        // Nothing to do
        serverId = -1;
      }
      if (serverId > 0) {
        adminConnect();

        // Check that this server is the one registered
        // with the identifier 'serverId'.
        Server[] servers = AdminModule.getServers();
        for (int i = 0; i < servers.length; i++) {
          if (servers[i].getId() == serverId &&
              servers[i].getName() == serverName &&
              servers[i].getHostName() == hostName) {
            try {
              AdminModule.removeServer(serverId);
            } catch (UnknownServerException exc) {
              // Nothing to do
            }
          }
        }
      }
      new File(baseDir, SERVER_ID).delete();
      File serverDataDir = new File(baseDir, JORAM_SERVER_DATA);
      if (serverDataDir.exists()) {
        File[] files = serverDataDir.listFiles();
        for (int i = 0; i < files.length; i++) {
          files[i].delete();
        }
        serverDataDir.delete();
      }
      new File(baseDir, A3_SERVERS_XML).delete();
    }
  }
}
