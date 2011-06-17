package org.ow2.joram.mom.amqp.tests;

import org.junit.After;
import org.junit.Before;
import org.ow2.joram.mom.amqp.tests.classic.SCAdminClassic;
import org.ow2.joram.mom.amqp.tests.osgi.SCAdminOSGi;

public class BaseTst {

  protected SCAdmin admin;

  public BaseTst() {
    String className = System.getProperty("TestFramework", SCAdminOSGi.class.getName());
    try {
      admin = (SCAdmin) Class.forName(className).newInstance();
    } catch (Exception exc) {
      System.out.println("Error instantiating framework SCAdmin class, use default.");
      admin = new SCAdminClassic();
    }
  }

  @Before
  public void start() throws Exception {
    admin.cleanRunDir();
    admin.startAgentServer((short) 0);
  }

  @After
  public void stop() throws Exception {
    admin.stopAgentServer((short) 0);
  }

}
