package org.objectweb.joram.client.jms.admin.server;

import java.io.*;
import java.net.*;
import java.util.*;

import fr.dyade.aaa.agent.AgentServer;

import org.objectweb.joram.client.jms.admin.*;
import org.objectweb.joram.mom.proxies.tcp.TcpProxyService;

/**
 * This class starts a Joram server without almost any configuration.
 * It just needs to know an existing Joram server (host, port) and 
 * the root login. The existing server is usually "s0" from the base configuration.
 * These data are dpecified by 4 environment properties: ADMIN_HOST_NAME, 
 * ADMIN_PORT, ROOT_USER_NAME and ROOT_USER_PWD.
 *
 * This server uses the current directory to store some data. You can specify another
 * directory with the property BASE_DIR_PATH.
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
    int adminPort = Integer.getInteger(ADMIN_PORT, 2560).intValue();
    String rootUserName = System.getProperty(ROOT_USER_NAME, "root");
    String rootUserPwd = System.getProperty(ROOT_USER_PWD, "root");
    AdminModule.connect(
      adminHostName,
      adminPort,
      rootUserName,
      rootUserPwd, 60);
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
            AdminModule.addDomain(
              "D0", adminServerId,
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
      
      AdminModule.addServer(
        serverId,
        hostName, 
        domainName, 
        port1, 
        serverName,
        new String[]{"org.objectweb.joram.mom.proxies.tcp.TcpProxyService"},
        new String[]{"" + port2});
      
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
    
    System.getProperties().put(AgentServer.CFG_DIR_PROPERTY,
                               baseDirPath);
    fr.dyade.aaa.agent.AgentServer.init(
      (short)serverId, 
      new File(baseDir, JORAM_SERVER_DATA).getPath(), 
      null);
    fr.dyade.aaa.agent.AgentServer.start();
    
    org.objectweb.joram.client.jms.admin.User user = 
      org.objectweb.joram.client.jms.admin.User.create(
        "anonymous", "anonymous", serverId);

    AdminModule.disconnect();
  }

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

  private static void saveServerId(int sid) 
    throws IOException {
    FileOutputStream fos = new FileOutputStream(
      new File(baseDir, SERVER_ID));
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeInt(sid);
    oos.flush();
    fos.flush();
    fos.getFD().sync();
    fos.close();
  }

  private static int loadServerId() 
    throws IOException {
    FileInputStream fis = new FileInputStream(
      new File(baseDir, SERVER_ID));
    ObjectInputStream ois = new ObjectInputStream(fis);
    int res = ois.readInt();
    fis.close();
    return res;
  }

  public final static int getTcpEntryPointPort() {
    return TcpProxyService.getListenPort();
  }

  public static void stop() {
    AgentServer.stop();
  }

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
