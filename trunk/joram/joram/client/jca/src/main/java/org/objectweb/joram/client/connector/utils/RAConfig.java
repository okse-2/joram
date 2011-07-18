/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - Bull SA
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.client.connector.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import fr.dyade.aaa.agent.conf.A3CML;
import fr.dyade.aaa.agent.conf.A3CMLConfig;
import fr.dyade.aaa.agent.conf.A3CMLServer;
import fr.dyade.aaa.agent.conf.A3CMLService;

/**
 *
 */
public class RAConfig {

  private static final String RA_XML = "META-INF/ra.xml";
  private static final String JORAM_CONFIG_JAR = "joram-config.jar";
  private static final String A3SERVERS_XML = "a3servers.xml";
  private static final String A3DEBUG_CFG = "a3debug.cfg";
  private static final String RA_PROPERTIES = "ra.properties";
  private static final String JORAMADMIN_CFG = "joram-admin.cfg";
  private static final String JORAMADMIN_XML = "joramAdmin.xml";
  private static final int BUFFER_SIZE = 2048;
  private static boolean debug = false;
  private static boolean verbose = false;
  private static String confDir = null;
  private static String tmpDir = null;


  private RAConfig() {
  }

  public static void main(String [] args) throws Exception {
    RAConfig raconfig = new RAConfig();

    debug = new Boolean(System.getProperty("debug","false")).booleanValue();

    String rarName = null;
    String jarName = null;
    String raProperties = null;
    String extractFile = null;
    String hostName = null;
    String port = null;
    String serverId = null;
    String path = null;
    String newFileName = null;
    String oldFileName = null;
    String rootName = null;
    String rootPasswd = null;
    String identityClass = null;

    int command = -1;

    try {
      int i = 0;
      while (i < args.length) {
        if (args[i].equals("-rar")) {
          rarName = args[i+1];
          i = i + 2;
        } else if (args[i].equals("-jar")) {
          jarName = args[i+1];
          i = i + 2;
        } else if (args[i].equals("-c")) {
          command = 1;
          i++;
        } else if (args[i].equals("-u")) {
          command = 2;
          raProperties = args[i+1];
          i = i + 2;
        } else if (args[i].equals("-x")) {
          command = 3;
          extractFile = args[i+1];
          i = i + 2;
        } else if (args[i].equals("-uhp")) {
          command = 4;
          hostName = args[i+1];
          port = args[i+2];
          serverId = args[i+3];
          i = i + 3;
        } else if (args[i].equals("-ua3")) {
          command = 5;
          hostName = args[i+1];
          port = args[i+2];
          serverId = args[i+3];
          i = i + 3;
        } else if (args[i].equals("-uz")) {
          command = 6;
          path = args[i+1];
          newFileName = args[i+2];
          oldFileName = args[i+3];
          i = i + 3;
        } else if (args[i].equals("-urpi")) {
          command = 7;
          rootName = args[i+1];
          rootPasswd = args[i+2];
          identityClass = args[i+3];
          i = i + 3;
        } else if (args[i].equals("-conf")) {
          confDir = args[i+1];
          i = i + 2;
        } else if (args[i].equals("-v")) {
          verbose = true;
          i = i + 1;
        } else
          i++;
      }
    } catch (Exception e) {
      usage();
      e.printStackTrace();
      System.exit(1);
    }

    //  Get the temp directory
    tmpDir = System.getProperty("java.io.tmpdir") + "/";

    switch (command) {
    case 1: // createRaProperties
      if (rarName == null)
        usage();
      raconfig.createRaProperties(rarName);
      break;
    case 2: // updateRAR
      if (raProperties == null)
        usage();
      raconfig.updateRAR(raProperties, true);
      break;
    case 3: // extractFile
      if (extractFile != null) {
        if (rarName != null) {
          raconfig.extractFromRAR(rarName,extractFile);
        } else if (jarName != null) {
          raconfig.extractFromJAR(jarName,extractFile);
        } else
          usage();
      } else
        usage();
      break;
    case 4: // updateHostPort
      if (rarName == null
          || hostName == null
          || port == null
          || serverId == null)
        usage();
      raconfig.updateHostPort(rarName,hostName,port,new Short(serverId).shortValue());
      break;
    case 5: // updateA3Servers
      if (rarName == null
          || hostName == null
          || port == null
          || serverId == null)
        usage();
      raconfig.updateA3Servers(rarName,hostName,port,new Short(serverId).shortValue());
      break;
    case 6: // updateZIP
      if ( path != null
           && newFileName != null) {
        if (rarName != null)
          raconfig.updateZIP(rarName,path,newFileName,oldFileName);
        if (jarName != null)
          raconfig.updateZIP(jarName,path,newFileName,oldFileName);
      } else
        usage();
      break;
    case 7: // update rootName rootPasswd identityClass
      if (rootName == null 
          || rootPasswd == null
          || identityClass == null)
        usage();
      raconfig.updateRootName(rarName, rootName, rootPasswd, identityClass);
      break;
    default:
      usage();
      break;
    }
  }

  /**
   * Usage of RAConfig.
   */
  public static void usage() {
    StringBuffer buff = new StringBuffer();

    buff.append("\n\n");
    buff.append("\nConfigure your Joram RAR:");
    buff.append("\n  -resource adapter deployment descriptor (ra.xml)");
    buff.append("\n  -joram server configuration file (a3servers.xml)");
    buff.append("\n\n");
    buff.append("\nSimple: create ra.properties, modify ra.properties and update rar.");
    buff.append("\n  create ra.properties         : java RAconfig -rar rarName -c");
    buff.append("\n  update RAR                   : java RAconfig -u ra.properties");
    buff.append("\n");
    buff.append("\nChange host and port value in ra.xml and a3servers.xml.");
    buff.append("\n  update RAR (host/port)       : java RAconfig -rar rarName -uhp host port serverId");
    buff.append("\nChange host and port value only in a3servers.xml");
    buff.append("\n  update A3servers (host/port) : java RAconfig -rar rarName -ua3 host port serverId");
    buff.append("\n\n");
    buff.append("\nExpert: extract ra.xml and a3servers.xml (joram-config.jar), modify and update jar/rar.");
    buff.append("\n  extract file from RAR        : java RAconfig -rar rarName -x fileName");
    buff.append("\n  update RAR                   : java RAconfig -rar rarName -uz path newFileName oldFileName");
    buff.append("\n");
    buff.append("\nchange rootName rootPass       : java RAconfig -rar rarName -urpi rootName rootPasswd identityClass");
    buff.append("\n");
    buff.append("\n  extract file from JAR        : java RAconfig -jar jarName -x fileName");
    buff.append("\n  update JAR                   : java RAconfig -jar jarName -uz path newFileName oldFileName");
    buff.append("\n\n");
    buff.append("\nVerbose                        : -v");
    buff.append("\n\n");
    buff.append("\nexample :");
    buff.append("\n   java RAconfig -u ra.properties");
    buff.append("\n   java RAconfig -rar joram.rar -uhp localhost 16010 0");
    buff.append("\n   java RAconfig -rar joram.rar -uz META-INF/ra.xml ra.xml META-INF/ra.xml");
    buff.append("\n\n\n");

    System.out.println(buff.toString());
  }


  /**
   * create ra.properties
   * build from the ra.xml file from RAR.
   * @param rarName  String input RAR file name
   * @throws Exception to throw if an Exception occurs
   */
  private void createRaProperties(String rarName)
    throws Exception {

    if (debug)
      System.out.println("RAConfig.createRaProperties(" + rarName + ")");
    else if (verbose)
      System.out.println("create ra.properties " + rarName);

    File file = new File(rarName);
    if (file.exists()) {
      ZipFile zipFile = new ZipFile(file.getAbsolutePath());
      for (Enumeration zippedFiles = zipFile.entries(); zippedFiles.hasMoreElements(); ) {
        //Retrieve entry of existing files
        ZipEntry currEntry = (ZipEntry) zippedFiles.nextElement();
        if (debug)
          System.out.println("RAConfig.createRaProperties : currEntry = " + currEntry);
        if (currEntry.getName().equalsIgnoreCase(RA_XML)) {
          InputStream reader = zipFile.getInputStream(currEntry);
          StringBuffer buff = new StringBuffer();
          buff.append("RAR_NAME  ");
          buff.append(file.getAbsolutePath());
          buff.append("\n");
          //parse ra.xml file
          buff.append(parse(reader));
          // create ra.properties file
          createFile(RA_PROPERTIES,buff.toString());
          break;
        }
      }
      zipFile.close();
    }
  }


  /**
   * Extract the fileName from the RAR file.
   * @param rarName   RAR file name
   * @param fileName  file name
   * @throws Exception to throw if an Exception occurs
   */
  private void extractFromRAR(String rarName, String fileName)
    throws Exception {

    if (debug)
      System.out.println("RAConfig.extractFromRAR(" + rarName + "," + fileName + ")");
    else if (verbose)
      System.out.println("extract \"" + fileName + "\" from \"" + rarName + "\"");

    InputStream res = null;
    File file = new File(rarName);
    if (file.exists()) {
      ZipFile zipFile = new ZipFile(file.getAbsolutePath());
      for (Enumeration zippedFiles = zipFile.entries(); zippedFiles.hasMoreElements(); ) {
        //Retrieve entry of existing files
        ZipEntry currEntry = (ZipEntry) zippedFiles.nextElement();
        if (debug)
          System.out.println("RAConfig.extractFromRAR : currEntry = " + currEntry);
        if (currEntry.getName().equalsIgnoreCase(fileName)
            || currEntry.getName().equalsIgnoreCase("META-INF/"+fileName)) {
          // the fileName is found.
          res =  zipFile.getInputStream(currEntry);
          break;
        } else if (currEntry.getName().endsWith(".jar")) {
          // search fileName in jar file.
          InputStream reader = zipFile.getInputStream(currEntry);
          res = extractFromJAR(fileName,reader);
          if (res == null) continue;
          
          // the fileName found in jar file.
          reader.close();
          break;
        }
      }
      // extract the fileName from InputStream
      // in the tmp directory.
      if (res != null)
        createFile(fileName,res);
      zipFile.close();
    }
  }

  /**
   * Extract fileName from the JAR file.
   * @param fileName  file name
   * @param reader    Input stream
   * @throws Exception to throw if an Exception occurs
   */
  private InputStream extractFromJAR(String fileName, InputStream reader)
    throws Exception {

    if (debug)
      System.out.println("RAConfig.extractFromJAR(" + fileName +  "," + reader + ")");

    ZipInputStream stream = new ZipInputStream(reader);
    ZipEntry currEntry = stream.getNextEntry();
    while (stream.available() > 0) {
      if (currEntry == null) break;
      if (currEntry.getName().equalsIgnoreCase(fileName)) {
        // the fileName is found, return the InputStream.
        if (debug)
          System.out.println("RAConfig.extractFromJAR : currEntry = " + currEntry);
        else if (verbose)
          System.out.println("extract \"" + fileName + "\" from JAR.");

        return stream;
      }
      currEntry = stream.getNextEntry();
    }
    // close the stream and return null if file not found.
    stream.close();
    return null;
  }

  /**
   * Extract config files from the JAR file.
   * @param jarName    JAR file name
   * @param fileName   file to be extract
   * @throws Exception to throw if an Exception occurs
   */
  private void extractFromJAR(String jarName, String fileName)
    throws Exception {

    if (debug)
      System.out.println("RAConfig.extractFromJAR(" + jarName +  "," + fileName + ")");
    else if (verbose)
      System.out.println("extract \"" + fileName + "\" from \"" + jarName + "\"");

    JarFile jar = new JarFile(jarName);
    ZipEntry entry = jar.getEntry(fileName);
    if (debug)
      System.out.println("RAConfig.extractFromJAR : entry = " + entry);
    // extract the fileName from jar in the tmp directory.
    if (entry != null)
      createFile(fileName,jar.getInputStream(entry));
    jar.close();
  }


  /**
   * write the inputstream in outputstream.
   * @param is    input stream
   * @param os    output stream
   * @throws Exception to throw if an Exception occurs
   */
  private void dump(InputStream is, OutputStream os) throws Exception {
    int n = 0;
    byte[] buffer = new byte[BUFFER_SIZE];
    n = is.read(buffer);
    while (n > 0) {
      os.write(buffer, 0, n);
      n = is.read(buffer);
    }
  }

  private String getFileName(String path) throws Exception {
    int i = path.lastIndexOf("/");
    if (i > 0)
      return path.substring(i+1,path.length());
    i = path.lastIndexOf("\\");
    if (i > 0)
      return path.substring(i+1,path.length());
    return path;
  }

  /**
   * create the filename in the tmp directory
   * by writing the input stream in the file name.
   * @param path        new file
   * @param is          input stream
   * @throws Exception to throw if an Exception occurs
   */
  private void createFile(String path, InputStream is) throws Exception {
    if (debug)
      System.out.println("RAConfig.createFile(" + path + "," + is + ")");

    String fileName = tmpDir + getFileName(path);
    if (debug)
      System.out.println("RAConfig.createFile : fileName = " + fileName);
    else if (verbose)
      System.out.println("create file \"" + fileName + "\"");

    new File(fileName).delete();
    FileOutputStream fos = new FileOutputStream(fileName);
    try {
      dump(is,fos);
    } finally {
      fos.close();
    }
  }

  /**
   * create the filename in the tmp directory,
   * by writing the input in the file name.
   * @param path        new file
   * @param input       string to write
   * @throws Exception to throw if an Exception occurs
   */
  private void createFile(String path, String input) throws Exception {
    if (debug)
      System.out.println("RAConfig.createFile(" + path + "," + input + ")");
    String fileName = tmpDir + getFileName(path);
    if (debug)
      System.out.println("RAConfig.createFile : fileName = " + fileName);
    else if (verbose)
      System.out.println("create file \"" + fileName + "\"");

    new File(fileName).delete();
    ByteArrayInputStream bis = new ByteArrayInputStream(input.getBytes());
    FileOutputStream fos = new FileOutputStream(fileName);
    try {
      dump(bis,fos);
    } finally {
      fos.close();
      bis.close();
    }
  }

  /**
   * parse input stream.
   * @param is    input stream
   * @throws Exception to throw if an Exception occurs
   */
  private String parse(InputStream is) throws Exception {
    if (debug)
      System.out.println("RAConfig.parse(" + is + ")");

    Wrapper wrapper = new Wrapper(debug);
    return wrapper.parse(is);
  }

  /**
   * update input stream with map value.
   * @param is    input stream
   * @param map   map value (see ra.properties)
   * @throws Exception to throw if an Exception occurs
   */
  private String update(InputStream is, Map map) throws Exception {
    if (debug)
      System.out.println("RAConfig.update(" + is + "," + map + ")");

    Wrapper wrapper = new Wrapper(debug);
    return wrapper.update(is,map);
  }

  /**
   * update host/port in ra.xml and a3server.xml in RAR.
   * @param rarName   rar file name
   * @param hostName  new host name
   * @param port      new port
   * @param serverId  server Id
   */
  private void updateHostPort(String rarName,
                              String hostName,
                              String port,
                              short serverId) throws Exception {
    if (debug)
      System.out.println("RAConfig.updateHostPort(" + rarName +
                         "," + hostName +
                         "," + port +
                         "," + serverId + ")");
    else if (verbose)
      System.out.println("update (ra.xml and a3server.xml) in \"" + rarName +
                         "\" with host=" + hostName +
                         " port=" + port +
                         " serverId=" + serverId);

    // update ra.xml file
    File file = new File(rarName);
    StringBuffer buff = new StringBuffer();
    buff.append("RAR_NAME  ");
    buff.append(file.getAbsolutePath());
    buff.append("\n[org.objectweb.joram.client.connector.JoramAdapter]");
    buff.append("\nHostName  ");
    buff.append(hostName);
    buff.append("\nServerPort  ");
    buff.append(port);
    buff.append("\nServerId  ");
    buff.append(serverId);
    // create temporary raProperty file
    String tempFile = "ra.properties_tmp";
    createFile(tempFile,buff.toString());
    updateRAR(tmpDir + tempFile, true);
    new File(tmpDir + tempFile).delete();
  }

  /**
   * update host/port in ra.xml and a3server.xml in RAR.
   * @param rarName   rar file name
   * @param rootName  new root name
   * @param rootPasswd  new RootPasswd
   * @param identityClass  new IdentityClass
   * @param serverId  server Id
   */
  private void updateRootName(String rarName,
                              String rootName,
                              String rootPasswd,
                              String identityClass) throws Exception {
    if (debug)
      System.out.println("RAConfig.updateRootName(" + rarName +
                         "," + rootName +
                         "," + rootPasswd +
                         "," + identityClass + ")");
    else if (verbose)
      System.out.println("update (ra.xml) in \"" + rarName +
                         "\" with RootName=" + rootName +
                         " RootPasswd=" + rootPasswd +
                         " IdentityClass=" + identityClass);

    // update ra.xml file
    File file = new File(rarName);
    StringBuffer buff = new StringBuffer();
    buff.append("RAR_NAME  ");
    buff.append(file.getAbsolutePath());
    buff.append("\n[org.objectweb.joram.client.connector.JoramAdapter]");
    buff.append("\nRootName  ");
    buff.append(rootName);
    buff.append("\nRootPasswd  ");
    buff.append(rootPasswd);
    buff.append("\nIdentityClass  ");
    buff.append(identityClass);
    // create temporary raProperty file
    String tempFile = "ra.properties_tmp";
    createFile(tempFile,buff.toString());
    updateRAR(tmpDir + tempFile, false);
    new File(tmpDir + tempFile).delete();
  }
  
  /**
   * update A3SERVERS_XML file
   * @param rarName   rar file name
   * @param hostName  new host name
   * @param port      new port
   * @param serverId  server Id
   */
  private void updateA3Servers(String rarName,
                               String hostName,
                               String port,
                               short serverId)
    throws Exception {
    if (debug)
      System.out.println("RAConfig.updateA3Servers(" + rarName +
                         "," + hostName +
                         "," + port +
                         "," + serverId + ")");
    else if (verbose)
      System.out.println("update (a3server.xml) in \"" + rarName +
                         "\" host=" + hostName +
                         " port=" + port +
                         " serverId=" + serverId);

    // extract the joram-config.jar file from RAR in the temp dir
    extractFromRAR(rarName,JORAM_CONFIG_JAR);

    // if present the a3server.xml source is the confdir one
    // otherwise, we take the RAR one
    if (confDir != null) {
        File f = new File(confDir, A3SERVERS_XML);
        if (f.exists()) {
            copy(f.getPath(), tmpDir + A3SERVERS_XML);
        } else {
          // extract the a3servers.xml file from joram-config.jar in the tmp dir
          extractFromJAR(tmpDir + JORAM_CONFIG_JAR, A3SERVERS_XML);
        }
    } else {
      // extract the a3servers.xml file from joram-config.jar in the tmp dir
      extractFromJAR(tmpDir + JORAM_CONFIG_JAR, A3SERVERS_XML);
    }

    // update A3SERVERS_XML
    A3CMLConfig conf = A3CML.getXMLConfig(tmpDir + A3SERVERS_XML);
    A3CMLServer server = conf.getServer(serverId);
    server.hostname = hostName;
    A3CMLService service =
      server.getService("org.objectweb.joram.mom.proxies.tcp.TcpProxyService");
    service.args = port;
    // write changes to A3SERVERS_XML file
    A3CML.toXML(conf,null, tmpDir + A3SERVERS_XML);

    if (debug)
      System.out.println("RAConfig.updateA3Servers : confDir=" + confDir);
    // update file in conf dir.
    if (confDir != null) {
      File f = new File(confDir, A3SERVERS_XML);
      if (f.exists())
        copy(tmpDir + A3SERVERS_XML, f.getPath());
      if (new File(confDir, JORAMADMIN_CFG).exists())
        updateJoramAdminCfg(hostName, port);
      if (new File(confDir, JORAMADMIN_XML).exists())
        updateJoramAdminXml(hostName, port);
    }

    if (new File(tmpDir +JORAM_CONFIG_JAR).exists()) {
      // update jar
      updateZIP(tmpDir + JORAM_CONFIG_JAR,A3SERVERS_XML,tmpDir + A3SERVERS_XML,A3SERVERS_XML);
      // update rar
      updateZIP(rarName,JORAM_CONFIG_JAR,tmpDir +JORAM_CONFIG_JAR,JORAM_CONFIG_JAR);
      // remove temporary file
      new File(tmpDir + JORAM_CONFIG_JAR).delete();
    }
    new File(tmpDir + A3SERVERS_XML).delete();
  }

  private boolean copy(String file1, String file2)
    throws Exception {
    if (! new File(file1).exists())
      return false;

    new File(file2).delete();

    FileInputStream fis = new FileInputStream(file1);
    FileOutputStream fos = new FileOutputStream(file2);
    try {
      dump(fis,fos);
      return true;
    } finally {
      fos.close();
      fis.close();
    }
  }

  private void updateJoramAdminCfg(String hostName,
                                   String port)
    throws Exception {
    File file = new File(confDir, JORAMADMIN_CFG);
    FileReader fileReader = new FileReader(file);
    BufferedReader reader = new BufferedReader(fileReader);
    boolean end = false;
    String line;
    StringTokenizer tokenizer;
    String firstToken;
    StringBuffer buff = new StringBuffer();

    while (! end) {
      line = reader.readLine();
      if (line == null)
        end = true;
      else {
        tokenizer = new StringTokenizer(line);
        if (tokenizer.hasMoreTokens()) {
          firstToken = tokenizer.nextToken();
          if (firstToken.equalsIgnoreCase("Host")) {
            buff.append("Host   " + hostName + "\n");
            continue;
          }
          else if (firstToken.equalsIgnoreCase("Port")) {
            buff.append("Port   " + port + "\n");
            continue;
          }
        }
        buff.append(line + "\n");
      }
    }
    file.delete();
    ByteArrayInputStream bis = new ByteArrayInputStream(buff.toString().getBytes());
    FileOutputStream fos = new FileOutputStream(file);
    try {
      dump(bis,fos);
    } finally {
      fos.close();
      bis.close();
    }
  }

  private void updateJoramAdminXml(String hostName,
                                   String port)
    throws Exception {
    File file = new File(confDir, JORAMADMIN_XML);
    FileReader fileReader = new FileReader(file);
    BufferedReader reader = new BufferedReader(fileReader);
    boolean end = false;
    String line;
    StringBuffer buff = new StringBuffer();
    int i = -1;

    while (! end) {
      line = reader.readLine();
      if (line == null)
        end = true;
      else {
        if (line.trim().startsWith("<connect")) {
          while (true) {
            i = line.indexOf("host");
            if (i > 0) {
              buff.append(line.substring(0,i+10));
              buff.append(hostName);
              int j = line.indexOf("\"",i+11);
              buff.append(line.substring(j,line.length()) + "\n");
              if (line.trim().endsWith("/>")) {
                line = reader.readLine();
                break;
              }
              line = reader.readLine();
              continue;
            }
            i = line.indexOf("port");
            if (i > 0) {
              buff.append(line.substring(0,i+6));
              buff.append(port);
              int j = line.indexOf("\"",i+7);
              buff.append(line.substring(j,line.length()) + "\n");
              if (line.trim().endsWith("/>")) {
                line = reader.readLine();
                break;
              }
              line = reader.readLine();
              continue;
            }
            buff.append(line + "\n");
            if (line.trim().endsWith("/>")) {
              line = reader.readLine();
              break;
            }
            line = reader.readLine();
          }
        } else if (line.trim().startsWith("<tcp")) {
          while (true) {
            i = line.indexOf("host");
            if (i > 0) {
              buff.append(line.substring(0,i+6));
              buff.append(hostName);
              int j = line.indexOf("\"",i+7);
              buff.append(line.substring(j,line.length()) + "\n");
              if (line.trim().endsWith("/>")) {
                line = reader.readLine();
                break;
              }
              line = reader.readLine();
              continue;
            }
            i = line.indexOf("port");
            if (i > 0) {
              buff.append(line.substring(0,i+6));
              buff.append(port);
              int j = line.indexOf("\"",i+7);
              buff.append(line.substring(j,line.length()) + "\n");
              if (line.trim().endsWith("/>")) {
                line = reader.readLine();
                break;
              }
              line = reader.readLine();
              continue;
            }
            buff.append(line + "\n");
            if (line.trim().endsWith("/>"))
              break;
            line = reader.readLine();
          }
        }
        buff.append(line + "\n");
      }
    }
    file.delete();
    ByteArrayInputStream bis = new ByteArrayInputStream(buff.toString().getBytes());
    FileOutputStream fos = new FileOutputStream(file);
    try {
      dump(bis,fos);
    } finally {
      fos.close();
      bis.close();
    }
  }

  /**
   * update RA_XML file
   * @param raProperties   ra.properties file
   */
  private void updateRAR(String raProperties, boolean updateA3servers) throws Exception {
    if (debug)
      System.out.println("RAConfig.updateRAR(" + raProperties + ")");

    FileReader file = new FileReader(raProperties);
    LineNumberReader lnr = new LineNumberReader(file);

    // get RAR name
    String line = lnr.readLine();
    while (line != null) {
      if (line.startsWith("RAR_NAME"))
        break;
      line = lnr.readLine();
    }
    int i = line.indexOf("RAR_NAME");
    String rarName = line.substring(i+"RAR_NAME".length()).trim();
    if (debug)
      System.out.println("RAConfig.updateRAR : rarName = " + rarName);
    else if (verbose)
      System.out.println("update rar \"" + rarName +
                         "\" with \"" + raProperties + "\"");

    // create Map of properties
    Map map = new Hashtable();
    Hashtable prop = null;
    String nodeName = null;
    line = lnr.readLine();
    while (line != null) {
      if (line.startsWith("[")) {
        prop = new Hashtable();
        nodeName = line.substring(line.indexOf("[")+1,line.indexOf("]"));
        map.put(nodeName.trim(),prop);
      } else {
        StringTokenizer st = new StringTokenizer(line);
        if (st.countTokens() == 2 && prop != null) {
          prop.put(st.nextToken(),st.nextToken());
        }
      }
      line = lnr.readLine();
    }
    file.close();

    if (debug)
      System.out.println("RAConfig.updateRAR : map = " + map);

    // create ra.xml file with new values
    File rarFile = new File(rarName);
    if (rarFile.exists()) {
      ZipFile zipFile = new ZipFile(rarFile.getAbsolutePath());
      for (Enumeration zippedFiles = zipFile.entries(); zippedFiles.hasMoreElements(); ) {
        // Retrieve entry of existing files
        ZipEntry currEntry = (ZipEntry) zippedFiles.nextElement();
        if (debug)
          System.out.println("RAConfig.updateRAR : currEntry = " + currEntry);
        if (currEntry.getName().equalsIgnoreCase(RA_XML)) {
          InputStream reader = zipFile.getInputStream(currEntry);
          createFile("ra.xml",update(reader,map));
          reader.close();
          break;
        }
      }
      zipFile.close();
    }

    // update rar
    updateZIP(rarName,RA_XML, tmpDir + "ra.xml",RA_XML);

    if (updateA3servers) {
      // update a3servers.xml (host and port).
      prop = (Hashtable) map.get("org.objectweb.joram.client.connector.JoramAdapter");
      if (prop != null) {
        if (debug)
          System.out.println("RAConfig.updateRAR : prop = " + prop);
        String host = (String) prop.get("HostName");
        short serverId = -1;
        String sid = (String) prop.get("ServerId");
        if (sid != null)
          serverId = new Short(sid).shortValue();
        else {
          if (debug)
            System.out.println("RAConfig.updateRAR : ServerId not found in ra.properties");
        }
        String port = (String) prop.get("ServerPort");
        if (host != null && host.length() > 0
            && port != null && port.length() > 0
            && serverId >= 0) {
          updateA3Servers(rarName,host,port,serverId);
        }
      }
    }
    new File(tmpDir + "ra.xml").delete();
  }

  /**
   * Update the RAR file
   * @param zipName     String JAR or RAR file
   * @param path        path in JAR or RAR file
   * @param newFileName add new filename
   * @param oldFileName remove old file from JAR or RAR
   */
  private void updateZIP(String zipName,
                         String path,
                         String newFileName,
                         String oldFileName)
    throws Exception {

    if (debug)
      System.out.println("RAConfig.updateZIP(" + zipName +
                         "," + path + "," + newFileName +
                         "," + oldFileName + ")");
    else if (verbose)
      System.out.println("updateZIP \"" + zipName +
                         "\", path \"" + path +
                         "\", new file \"" + newFileName +
                         "\", old file \"" + oldFileName + "\"");


    ZipEntry entry = null;

    File file = new File(zipName);
    if (file.exists()) {
      ZipFile zipFile = new ZipFile(file.getAbsolutePath());
      Enumeration zipEntries = zipFile.entries();
      // create your output zip file
      ZipOutputStream newZip = new ZipOutputStream(
        new FileOutputStream(new File(file.getAbsolutePath() + "_TMP")));

      // Get all data (except the oldFileName) from zip file and
      // write it to the tmp zip file
      while (zipEntries.hasMoreElements()) {
        entry = (ZipEntry) zipEntries.nextElement();
        if (entry.getName().equalsIgnoreCase(oldFileName))
          continue;
        newZip.putNextEntry(new ZipEntry(entry.getName()));
        InputStream is = zipFile.getInputStream(entry);
        try {
          dump(is,newZip);
        } finally {
          newZip.closeEntry();
          is.close();
        }
      }
      zipFile.close();

      // Write the new fileName to the zip
      entry = new ZipEntry(path);
      newZip.putNextEntry(entry);
      try {
        FileInputStream fis = new FileInputStream(newFileName);
        dump(fis,newZip);
        fis.close();
      } catch (Exception exc) {
        System.out.println("Error reading input file: " + newFileName + " " + exc);
        newZip.close();
        new File(file.getAbsolutePath() + "_TMP").delete();
        throw exc;
      } finally {
        newZip.flush();
        newZip.closeEntry();
        newZip.finish();
        newZip.close();
      }
      String toRename = file.getAbsolutePath() + "_TMP";
      new File(file.getAbsolutePath()).delete();
      new File(toRename).renameTo(file);
    }
  }
}
