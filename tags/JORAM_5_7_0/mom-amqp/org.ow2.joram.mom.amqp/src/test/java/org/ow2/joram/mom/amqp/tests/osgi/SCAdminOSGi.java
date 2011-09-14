package org.ow2.joram.mom.amqp.tests.osgi;

import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;
import org.ow2.joram.mom.amqp.AMQPConnectionListener;
import org.ow2.joram.mom.amqp.tests.SCAdmin;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.UnknownServerException;
import fr.dyade.aaa.common.Debug;

public class SCAdminOSGi implements SCAdmin {
  
  /** Map containing all <code>Process</code> of running AgentServers */
  private Map launchedServers = new HashMap();

  public static final Option[] config(short sid) {
    return options(
        CoreOptions.felix(),
//        CoreOptions.cleanCaches(),
        CoreOptions.mavenBundle("org.ow2.jonas.osgi", "monolog", "5.2.0"),
        CoreOptions.mavenBundle("org.objectweb.joram", "a3-rt", SCAdmin.JORAM_VERSION),
        CoreOptions.mavenBundle("org.objectweb.joram", "a3-common", SCAdmin.JORAM_VERSION),
        CoreOptions.mavenBundle("org.objectweb.joram", "mom-amqp", AMQPConnectionListener.JORAM_AMQP_VERSION),
        CoreOptions.mavenBundle("org.objectweb.joram", "a3-osgi", SCAdmin.JORAM_VERSION),
        CoreOptions.workingDirectory(SCAdmin.RUNNING_DIR),
        CoreOptions.systemProperties(new SystemPropertyOption(Debug.DEBUG_FILE_PROPERTY).value(SCAdmin.A3DEBUG_LOCATION)),
        CoreOptions.systemProperties(new SystemPropertyOption(AgentServer.CFG_FILE_PROPERTY).value(SCAdmin.A3SERVERS_LOCATION)),
        CoreOptions.systemProperties(new SystemPropertyOption("NTNoLockFile").value("true")),
        CoreOptions.systemProperties(new SystemPropertyOption("fr.dyade.aaa.agent.AgentServer.id").value(Short.toString(sid))),
        CoreOptions.systemProperties(new SystemPropertyOption("fr.dyade.aaa.agent.AgentServer.storage").value("s" + sid))
    );
  }

  public void startAgentServer(short sid) throws Exception {
    
    TestContainer container = (TestContainer) launchedServers.get(new Short(sid));
    if (container != null) {
      throw new IllegalStateException("AgentServer#" + sid + " already running.");
    }
    
    ExamSystem system = PaxExamRuntime.createTestSystem(config(sid));
    container = PaxExamRuntime.createContainer(system);
    container.start();
    
    launchedServers.put(new Short(sid), container);

    Thread.sleep(1000);

  }

  public void killAgentServer(short sid) throws Exception {
    stopAgentServer(sid);
  }

  public void stopAgentServer(short sid) throws Exception {
    TestContainer container = (TestContainer) launchedServers.remove(new Short(sid));
    if (container == null) {
      throw new UnknownServerException("Server " + sid + " unknown: not started using SCAdmin.");
    }
    container.stop();
  }

  public void cleanRunDir() throws Exception {
    // Add sleep time to properly release file handles.
    Thread.sleep(1000);
    deleteDirectory(new File(SCAdmin.RUNNING_DIR));
  }

  static public boolean deleteDirectory(File path) {
    if (path.exists()) {
      File[] files = path.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          deleteDirectory(files[i]);
        } else {
          files[i].delete();
        }
      }
    }
    return (path.delete());
  }

}
