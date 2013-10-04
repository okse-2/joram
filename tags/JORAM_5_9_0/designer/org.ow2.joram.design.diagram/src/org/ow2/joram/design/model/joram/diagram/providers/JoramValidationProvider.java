/*
 * Copyright (C) 2009 - 2010 ScalAgent Distributed Technologies
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
 * 
 */
package org.ow2.joram.design.model.joram.diagram.providers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.validation.AbstractModelConstraint;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.IClientSelector;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.notation.View;
import org.ow2.joram.design.model.joram.AdminProxy;
import org.ow2.joram.design.model.joram.Config;
import org.ow2.joram.design.model.joram.CustomService;
import org.ow2.joram.design.model.joram.DistributedJNDIServer;
import org.ow2.joram.design.model.joram.Host;
import org.ow2.joram.design.model.joram.JNDIServer;
import org.ow2.joram.design.model.joram.JoramPackage;
import org.ow2.joram.design.model.joram.JoramService;
import org.ow2.joram.design.model.joram.NetworkPort;
import org.ow2.joram.design.model.joram.ScalAgentServer;
import org.ow2.joram.design.model.joram.TCPProxyService;
import org.ow2.joram.design.model.joram.diagram.edit.parts.ConfigEditPart;
import org.ow2.joram.design.model.joram.diagram.expressions.JoramAbstractExpression;
import org.ow2.joram.design.model.joram.diagram.expressions.JoramOCLFactory;
import org.ow2.joram.design.model.joram.diagram.part.JoramDiagramEditorPlugin;
import org.ow2.joram.design.model.joram.diagram.part.JoramVisualIDRegistry;
import org.ow2.joram.design.model.joram.diagram.preferences.DiagramGeneralPreferencePage;

/**
 * @generated
 */
public class JoramValidationProvider {

  /**
   * @generated
   */
  private static boolean constraintsActive = false;

  /**
   * @generated
   */
  public static boolean shouldConstraintsBePrivate() {
    return false;
  }

  /**
   * @generated
   */
  public static void runWithConstraints(TransactionalEditingDomain editingDomain, Runnable operation) {
    final Runnable op = operation;
    Runnable task = new Runnable() {
      public void run() {
        try {
          constraintsActive = true;
          op.run();
        } finally {
          constraintsActive = false;
        }
      }
    };
    if (editingDomain != null) {
      try {
        editingDomain.runExclusive(task);
      } catch (Exception e) {
        JoramDiagramEditorPlugin.getInstance().logError("Validation failed", e); //$NON-NLS-1$
      }
    } else {
      task.run();
    }
  }

  /**
   * @generated
   */
  static boolean isInDefaultEditorContext(Object object) {
    if (shouldConstraintsBePrivate() && !constraintsActive) {
      return false;
    }
    if (object instanceof View) {
      return constraintsActive
          && ConfigEditPart.MODEL_ID.equals(JoramVisualIDRegistry.getModelID((View) object));
    }
    return true;
  }

  /**
   * @generated
   */
  public static class DefaultCtx implements IClientSelector {

    /**
     * @generated
     */
    public boolean selects(Object object) {
      return isInDefaultEditorContext(object);
    }
  }

  /**
   * @generated
   */
  public static class Adapter3 extends AbstractModelConstraint {

    /**
     * @generated
     */
    private JoramAbstractExpression expression;

    /**
     * @generated
     */
    public IStatus validate(IValidationContext ctx) {
      final Object context = ctx.getTarget().eGet(JoramPackage.eINSTANCE.getNetworkDomain_Name());
      if (context == null) {
        return ctx.createFailureStatus(new Object[] { formatElement(ctx.getTarget()) });
      }
      if (expression == null) {
        expression = JoramOCLFactory.getExpression("size() > 0", EcorePackage.eINSTANCE.getEString());
      }
      Object result = expression.evaluate(context);
      if (result instanceof Boolean && ((Boolean) result).booleanValue()) {
        return Status.OK_STATUS;
      }
      return ctx.createFailureStatus(new Object[] { formatElement(ctx.getTarget()) });
    }
  }

  /**
   * @generated
   */
  public static class Adapter5 extends AbstractModelConstraint {

    /**
     * Checks if each port of the host is used only once.
     * 
     * @generated NOT
     */
    public IStatus validate(IValidationContext ctx) {
      Host host = (Host) ctx.getTarget();

      Set<Integer> usedPorts = new HashSet<Integer>();
      Set<Integer> conflicts = new HashSet<Integer>();

      Config config = (Config) host.eContainer();
      for (ScalAgentServer server : config.getServers()) {
        if (server.getHost() != host) {
          continue;
        }
        for (NetworkPort netPort : server.getNetwork()) {
          Integer port = Integer.valueOf(netPort.getPort());
          if (usedPorts.contains(port)) {
            conflicts.add(port);
          } else {
            usedPorts.add(port);
          }
        }
        for (JoramService service : server.getServices()) {
          Integer port = null;
          if (service instanceof JNDIServer) {
            port = Integer.valueOf(((JNDIServer) service).getPort());
          } else if (service instanceof DistributedJNDIServer) {
            port = Integer.valueOf(((DistributedJNDIServer) service).getPort());
          } else if (service instanceof TCPProxyService) {
            port = Integer.valueOf(((TCPProxyService) service).getPort());
          } else if (service instanceof AdminProxy) {
            port = Integer.valueOf(((AdminProxy) service).getPort());
          }
          if (port != null) {
            if (usedPorts.contains(port)) {
              conflicts.add(port);
            } else {
              usedPorts.add(port);
            }
          }
        }
      }

      if (conflicts.isEmpty()) {
        return ctx.createSuccessStatus();
      } else {
        return ctx.createFailureStatus(conflicts.toString());
      }
    }
  }

  protected static final Properties loadProps() {
    Properties props = new Properties();
    try {
      InputStream fis = new FileInputStream(JoramDiagramEditorPlugin.getInstance().getPreferenceStore()
          .getString(DiagramGeneralPreferencePage.PREF_JORAM_EXTENSION_FILE));
      props.load(fis);
      fis.close();
    } catch (FileNotFoundException exc) {
      return null;
    } catch (IOException exc) {
      return null;
    }
    return props;
  }

  /**
   * @generated
   */
  public static class Adapter6 extends AbstractModelConstraint {

    /**
     * Checks if the topic class name is a known extension.
     * 
     * @generated NOT
     */
    public IStatus validate(IValidationContext ctx) {
      final String topicClassName = (String) ctx.getTarget()
          .eGet(JoramPackage.eINSTANCE.getTopic_ClassName());
      if (topicClassName == null) {
        return ctx.createFailureStatus(new Object[] { formatElement(ctx.getTarget()) });
      }
      Properties props = loadProps();
      if (props != null && props.get(topicClassName) == null) {
        return ctx.createFailureStatus(new Object[] { topicClassName });
      }
      return ctx.createSuccessStatus();
    }
  }

  /**
   * @generated
   */
  public static class Adapter7 extends AbstractModelConstraint {

    /**
     * Checks if the queue class name is a known extension.
     * 
     * @generated NOT
     */
    public IStatus validate(IValidationContext ctx) {
      final String queueClassName = (String) ctx.getTarget()
          .eGet(JoramPackage.eINSTANCE.getQueue_ClassName());
      if (queueClassName == null) {
        return ctx.createFailureStatus(new Object[] { formatElement(ctx.getTarget()) });
      }
      Properties props = loadProps();
      if (props != null && props.get(queueClassName) == null) {
        return ctx.createFailureStatus(new Object[] { queueClassName });
      }
      return ctx.createSuccessStatus();
    }
  }

  /**
   * @generated
   */
  public static class Adapter8 extends AbstractModelConstraint {

    /**
     * Checks if the service class name is a known extension.
     * 
     * @generated NOT
     */
    public IStatus validate(IValidationContext ctx) {
      CustomService customService = (CustomService) ctx.getTarget();
      Properties props = loadProps();
      String serviceClassName = customService.getClassName();
      if (serviceClassName == null || (props != null && props.get(serviceClassName) == null)) {
        return ctx.createFailureStatus(new Object[] { serviceClassName });
      }
      return ctx.createSuccessStatus();
    }
  }

  /**
   * @generated
   */
  public static class Adapter9 extends AbstractModelConstraint {

    /**
     * Checks if Joram extension file has been defined correctly.
     * 
     * @generated NOT
     */
    public IStatus validate(IValidationContext ctx) {
      Properties props = loadProps();
      if (props == null) {
        return ctx.createFailureStatus();
      }
      return ctx.createSuccessStatus();
    }
  }

  /**
   * @generated
   */
  static String formatElement(EObject object) {
    return EMFCoreUtil.getQualifiedName(object, true);
  }

}
