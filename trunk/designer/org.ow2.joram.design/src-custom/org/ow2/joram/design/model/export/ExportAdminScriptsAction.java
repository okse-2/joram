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
 */
package org.ow2.joram.design.model.export;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.ListDialog;
import org.ow2.joram.design.model.joram.Config;
import org.ow2.joram.design.model.joram.ConnectionFactory;
import org.ow2.joram.design.model.joram.ConnectionFactoryClass;
import org.ow2.joram.design.model.joram.ConnectionManager;
import org.ow2.joram.design.model.joram.Destination;
import org.ow2.joram.design.model.joram.DistributedJNDIServer;
import org.ow2.joram.design.model.joram.JNDIServer;
import org.ow2.joram.design.model.joram.JORAM;
import org.ow2.joram.design.model.joram.JoramPackage;
import org.ow2.joram.design.model.joram.JoramService;
import org.ow2.joram.design.model.joram.Queue;
import org.ow2.joram.design.model.joram.ScalAgentServer;
import org.ow2.joram.design.model.joram.TCPProxyService;
import org.ow2.joram.design.model.joram.Topic;
import org.ow2.joram.design.model.joram.User;

public class ExportAdminScriptsAction implements IObjectActionDelegate {

  /**
   * Shows a dialog window used to choose an entry point in a list.
   */
  private static TCPProxyService selectEntryPoint(Shell shell, Config rootElement) throws Exception {

    EList<ScalAgentServer> servers = rootElement.getServers();
    List<TCPProxyService> entrypoints = new ArrayList<TCPProxyService>();
    for (ScalAgentServer server : servers) {
      List<JoramService> services = server.getServices();
      for (JoramService service : services) {
        if (service instanceof TCPProxyService) {
          entrypoints.add((TCPProxyService) service);
        }
      }
    }

    if (entrypoints.size() == 0) {
      throw new Exception(
          "Export is not possible if no entry point is available (TCPProxyService is needed).");
    }

    ListDialog dlg = new ListDialog(shell);
    dlg.setInput(entrypoints);
    dlg.setContentProvider(new ArrayContentProvider());
    dlg.setLabelProvider(new LabelProvider() {
      public String getText(Object element) {
        TCPProxyService entry = (TCPProxyService) element;
        ScalAgentServer server = (ScalAgentServer) entry.eContainer();
        return "Server " + server.getSid() + " (" + server.getHostname() + ':' + entry.getPort() + ')';
      }
    });
    dlg.setTitle("Select entry point");
    dlg.setMessage("Select the entry point for joram configuration script:");
    dlg.setInitialSelections(new Object[] { entrypoints.get(0) });
    dlg.open();
    Object[] result = dlg.getResult();
    return (TCPProxyService) (result == null ? null : result[0]);
  }

  /**
   * Shows a dialog window to choose the JNDI used in the script.
   */
  private static JoramService selectJNDIServer(Shell shell, Config rootElement) {
    EList<ScalAgentServer> servers = rootElement.getServers();
    List<JoramService> jndis = new ArrayList<JoramService>();
    for (ScalAgentServer server : servers) {
      List<JoramService> services = server.getServices();
      for (JoramService service : services) {
        if (service instanceof JNDIServer || service instanceof DistributedJNDIServer) {
          jndis.add(service);
        }
      }
    }

    ListDialog dlg = new ListDialog(shell);
    dlg.setInput(jndis);
    dlg.setContentProvider(new ArrayContentProvider());
    dlg.setLabelProvider(new LabelProvider() {
      public String getText(Object element) {
        if (element instanceof JNDIServer) {
          JNDIServer jndi = (JNDIServer) element;
          ScalAgentServer server = (ScalAgentServer) jndi.eContainer();
          return "JNDI on server " + server.getSid() + " (" + server.getHostname() + ':' + jndi.getPort() + ')';
        } else if (element instanceof DistributedJNDIServer) {
          DistributedJNDIServer jndi = (DistributedJNDIServer) element;
          ScalAgentServer server = (ScalAgentServer) jndi.eContainer();
          return "Distributed JNDI on server " + server.getSid() + " (" + server.getHostname() + ':' + jndi.getPort() + ')';
        } else {
          return element.toString();
        }
      }
    });
    dlg.setTitle("Select JNDI");
    dlg.setMessage("Select the jndi used to register objects.");
    dlg.open();
    Object[] result = dlg.getResult();
    if (result == null || result.length == 0) {
      return null;
    } else {
      return (JoramService) result[0];
    }
  }

  /**
   * Shows a dialog window used to select the elements which will be present in
   * the exported admin script.
   */
  private static Object[] selectExportedScript(Shell shell, Config rootElement, TCPProxyService entrypoint) {
    CheckedTreeSelectionDialog treedlg = new CheckedTreeSelectionDialog(shell, new LabelProvider() {
      public String getText(Object element) {
        if (element instanceof ScalAgentServer) {
          ScalAgentServer server = (ScalAgentServer) element;
          return "Server " + server.getSid() + " (" + server.getName() + ')';
        } else if (element instanceof Queue) {
          Queue queue = (Queue) element;
          return "Queue " + queue.getName();
        } else if (element instanceof Topic) {
          Topic topic = (Topic) element;
          return "Topic " + topic.getName();
        } else if (element instanceof User) {
          User user = (User) element;
          return "User " + user.getName();
        } else if (element instanceof ConnectionFactory) {
          ConnectionFactory cf = (ConnectionFactory) element;
          return "ConnectionFactory " + cf.getJndiName();
        } else {
          return element.toString();
        }
      }
    }, new JoramServerTreeContentProvider());
    treedlg.setInput(rootElement);
    treedlg.setTitle("Build script.");
    treedlg.setMessage("Select the elements which will be in the script.");
    treedlg.setContainerMode(true);
    treedlg.setExpandedElements(rootElement.getServers().toArray());
    treedlg.setInitialSelection(entrypoint.eContainer());
    treedlg.open();
    return treedlg.getResult();
  }


  private ISelection selection;

  private Shell shell;

  public ExportAdminScriptsAction() {
    super();
  }

  /**
   * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
   */
  public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
    shell = targetPart.getSite().getShell();
  }

  /**
   * @see IActionDelegate#run(IAction)
   */
  public void run(final IAction action) {

    if (selection instanceof TreeSelection) {

      final TreeSelection tree = (TreeSelection) selection;
      final Object o = tree.getFirstElement();

      if (o instanceof IFile) {

        try {

          final IFile joramModelFile = (IFile) o;

          // Create the correct URI for JoramAdmin.xml based on *.joram
          URI outputFileURi = URI.createFileURI(joramModelFile.getLocation().toOSString());
          outputFileURi = outputFileURi.trimSegments(1);
          
          File outputFile = new File(outputFileURi.appendSegment("JoramAdmin.xml").toFileString());
          int i = 2;
          while (outputFile.exists()) {
            outputFile = new File(outputFileURi.appendSegment("JoramAdmin (" + i + ")" + ".xml").toFileString());
            i++;
          }

          final JoramPackage ePackage = JoramPackage.eINSTANCE;

          // Create resource set and register your generated resource factory.
          final ResourceSet resourceSet = new ResourceSetImpl();
          resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
          resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("joram",
              new XMIResourceFactoryImpl());

          final Resource resource = resourceSet.getResource(URI.createFileURI(joramModelFile.getLocationURI().getPath()), true);
          final Config rootElement = (Config) resource.getContents().get(0);

          TCPProxyService entrypoint = selectEntryPoint(shell, rootElement);
          if (entrypoint == null) {
            return;
          }
          ScalAgentServer entryServer = (ScalAgentServer) entrypoint.eContainer();
          ConnectionManager cm = null;
          for (JoramService service : entryServer.getServices()) {
            if (service instanceof ConnectionManager) {
              cm = (ConnectionManager) service;
            }
          }
          if (cm == null) {
            throw new Exception("A connection manager service was not found on selected server.");
          }
          
          JoramService jndi = selectJNDIServer(shell, rootElement);

          Object[] exportedElements = selectExportedScript(shell, rootElement, entrypoint);
          if (exportedElements == null) {
            return;
          }
          
          String confString = generateAdminConf(entrypoint, entryServer, cm, jndi, exportedElements);

          FileWriter fw = new FileWriter(outputFile);
          fw.write(confString);
          fw.flush();
          fw.close();

          joramModelFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);

        } catch (Exception e) {
          MessageDialog.openError(shell, "Export Plug-in", "Export to JoramAdmin.xml failed: "
              + e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Generates the contents of the configuration file using the selected
   * elements.
   */
  private String generateAdminConf(TCPProxyService entrypoint, ScalAgentServer entryServer,
      ConnectionManager cm, JoramService jndi, Object[] exportedElements) {
    StringBuilder sb = new StringBuilder();
    sb.append("<JoramAdmin>");
    sb.append("\n");
    sb.append("  <AdminModule>\n");
    sb.append("    <connect host='" + entryServer.getHostname() + "'\n");
    sb.append("             port='" + entrypoint.getPort() + "'\n");
    sb.append("             name='" + cm.getUser() + "'\n");
    sb.append("             password='" + cm.getPassword() + "'/>\n");
    sb.append("  </AdminModule>\n");
    sb.append("\n");
    
    if (jndi!=null) {
      int jndiport = -1;
      if (jndi instanceof JNDIServer) {
        jndiport = ((JNDIServer) jndi).getPort();
      } else {
        jndiport = ((DistributedJNDIServer) jndi).getPort();
      }
      sb.append("  <InitialContext>\n");
      sb.append("    <property name='java.naming.factory.initial' value='fr.dyade.aaa.jndi2.client.NamingContextFactory'/>\n");
      sb.append("    <property name='java.naming.factory.host' value='"
          + ((ScalAgentServer) jndi.eContainer()).getHostname() + "'\n");
      sb.append("    <property name='java.naming.factory.port' value='" + jndiport + "'\n");
      sb.append("  </InitialContext>\n");
      sb.append("\n");
    }
    
    short currentSid = -1;
    for (Object element : exportedElements) {

      if (element instanceof ScalAgentServer) {
        ScalAgentServer server = (ScalAgentServer) element;
        currentSid = server.getSid();

      } else if (element instanceof Destination) {
        sb.append("\n");
        Destination destination = (Destination) element;
        if (destination instanceof Topic) {
          Topic topic = (Topic) destination;
          sb.append("  <Topic name='" + topic.getName() + "' serverId='" + currentSid + "' className='"
              + topic.getClassName() + "'>\n");
        } else {
          Queue queue = (Queue) destination;
          sb.append("  <Queue name='" + queue.getName() + "' serverId='" + currentSid
              + "' className='" + queue.getClassName() + "'");
          if (queue.getDeadMessageQueue() != null) {
            sb.append(" dmq='" + queue.getDeadMessageQueue().getName() + "'");
          }
          sb.append(">\n");
        }
        if (destination.isFreeReader()) {
          sb.append("    <freeReader/>\n");
        }
        if (destination.isFreeWriter()) {
          sb.append("    <freeWriter/>\n");
        }
        EList<User> users = destination.getReaders();
        for (User user : users) {
          sb.append("    <reader user='" + user.getName() + "'/>\n");
        }
        users = destination.getWriters();
        for (User user : users) {
          sb.append("    <writer user='" + user.getName() + "'/>\n");
        }
        if (destination.getJndiName() != null) {
          sb.append("    <jndi name='" + destination.getJndiName() + "'/>\n");
        }
        if (destination instanceof Topic) {
          sb.append("  </Topic>\n");
        } else {
          sb.append("  </Queue>\n");
        }

      } else if (element instanceof User) {
        sb.append("\n");
        User user = (User) element;
        sb.append("  <User name='" + user.getName() + "' password='" + user.getPassword() + "' serverId='"
            + currentSid + "'/>\n");

      } else if (element instanceof ConnectionFactory) {
        sb.append("\n");
        ConnectionFactory cf = (ConnectionFactory) element;
        sb.append("  <ConnectionFactory className='" + cf.getClassName() + "'>\n");
        if (cf.getClassName().equals(ConnectionFactoryClass.TCP_CONNECTION_FACTORY)) {
          sb.append("    <tcp>\n");
        } else if (cf.getClassName().equals(ConnectionFactoryClass.SOAP_CONNECTION_FACTORY)) {
          sb.append("    <soap>\n");
        } else if (cf.getClassName().equals(ConnectionFactoryClass.LOCAL_CONNECTION_FACTORY)) {
          sb.append("    <local>\n");
        }
        if (cf.getJndiName() != null) {
          sb.append("    <jndi name='" + cf.getJndiName() + "'/>\n");
        }
        sb.append("  </ConnectionFactory>\n");
      }
    }
    sb.append("</JoramAdmin>\n");

    return sb.toString();
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    this.selection = selection;
  }

  static class JoramServerTreeContentProvider implements ITreeContentProvider {

    public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof JORAM) {
        return ((JORAM) parentElement).getJmsObjects().toArray();
      }
      return null;
    }

    public Object getParent(Object element) {
      return ((EObject) element).eContainer();
    }

    public boolean hasChildren(Object element) {
      if (element instanceof JORAM) {
        return ((JORAM) element).getJmsObjects().size() > 0;
      }
      return false;
    }

    public Object[] getElements(Object inputElement) {
      return ((Config) inputElement).getServers().toArray();
    }

    public void dispose() {
      // do nothing
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // do nothing
    }

  }

}
