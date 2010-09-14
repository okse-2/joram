package org.ow2.joram.admin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

  private static BundleContext m_context;

  public static BundleContext getContext() {
    return m_context;
  }

  public void start(BundleContext context) throws Exception {
    m_context = context;
  }

  public void stop(BundleContext context) throws Exception {
  }

}
