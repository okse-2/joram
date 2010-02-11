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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.ow2.joram.design.model.joram.AdminProxy;
import org.ow2.joram.design.model.joram.Config;
import org.ow2.joram.design.model.joram.ConnectionManager;
import org.ow2.joram.design.model.joram.CustomProperty;
import org.ow2.joram.design.model.joram.CustomService;
import org.ow2.joram.design.model.joram.DistributedJNDIServer;
import org.ow2.joram.design.model.joram.HttpNetworkProperties;
import org.ow2.joram.design.model.joram.JNDIServer;
import org.ow2.joram.design.model.joram.JoramPackage;
import org.ow2.joram.design.model.joram.JoramService;
import org.ow2.joram.design.model.joram.NTransactionProperties;
import org.ow2.joram.design.model.joram.NetworkDomain;
import org.ow2.joram.design.model.joram.NetworkPort;
import org.ow2.joram.design.model.joram.NetworkProperties;
import org.ow2.joram.design.model.joram.PoolNetworkProperties;
import org.ow2.joram.design.model.joram.Property;
import org.ow2.joram.design.model.joram.ScalAgentServer;
import org.ow2.joram.design.model.joram.TCPProxyService;
import org.ow2.joram.design.model.joram.TransactionProperty;

public class ExportA3ConfAction implements IObjectActionDelegate {

  /**
   * 
   */
  private ISelection selection;

  /**
   * 
   */
  private Shell shell;

  /**
   * Constructor for Action1.
   */
  public ExportA3ConfAction() {
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

          /* create the correct URI for a3servers.xml based on *.joram */
          URI outputFileURi = URI.createFileURI(joramModelFile.getLocation().toOSString());
          outputFileURi = outputFileURi.trimSegments(1);
          
          File outputFile = new File(outputFileURi.appendSegment("a3servers.xml").toFileString());
          int i = 2;
          while (outputFile.exists()) {
            outputFile = new File(outputFileURi.appendSegment("a3servers (" + i + ")" + ".xml")
                .toFileString());
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

          StringBuilder sb = new StringBuilder();
          sb.append("<config>\n");

          if (rootElement.getProperties() != null) {
            appendProperties(rootElement.getProperties().getProperties(), sb, null, "  ");
          }

          EList<NetworkDomain> domains = rootElement.getDomains();
          for (NetworkDomain domain : domains) {
            sb.append('\n');
            sb.append("  <domain name='" + domain.getName() + "' network='"
                + domain.getNetwork().getLiteral() + "' />\n");
            appendProperties(domain.getProperties(), sb, domain, "  ");
          }

          EList<ScalAgentServer> servers = rootElement.getServers();
          for (ScalAgentServer server : servers) {
            sb.append('\n');
            sb.append("  <server id='" + server.getSid() + "' name='" + server.getName() + "' hostname='"
                + server.getHostname() + "'>\n");

            appendServices(server.getServices(), sb);

            EList<NetworkPort> ports = server.getNetwork();
            for (NetworkPort port : ports) {
              sb.append("    <network domain='" + port.getDomain().getName() + "' port='" + port.getPort()
                  + "' />\n");
            }

            appendProperties(server.getProperties(), sb, null, "    ");

            sb.append("  </server>\n");
          }
          sb.append("</config>\n");

          FileWriter fw = new FileWriter(outputFile);
          fw.write(sb.toString());
          fw.flush();
          fw.close();

          joramModelFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
          MessageDialog.openInformation(shell, "Export Plug-in", "Export to a3servers.xml completed.");
        } catch (Exception e) {
          MessageDialog.openError(shell, "Export Plug-in", "Export to a3servers.xml failed: "
              + e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }

  private StringBuilder appendProperties(EList<Property> properties, StringBuilder sb, NetworkDomain domain, String indent) {
    String pre = (domain == null) ? (indent + "<property name='") : (indent + "<property name='" + domain.getName() + '.');
    String mid = "' value='";
    String suf = "' />\n";
    if (indent.endsWith("    "))
      System.out.println(properties.size());
    for (Property property : properties) {
      if (property instanceof PoolNetworkProperties) {
        PoolNetworkProperties props = (PoolNetworkProperties) property;
        sb.append(pre + "PoolNetwork.nbMaxCnx" + mid + props.getNbMaxCnx() + suf);
        sb.append(pre + "PoolNetwork.compressedFlows" + mid + props.isCompressedFlows() + suf);
        sb.append(pre + "PoolNetwork.maxMessageInFlow" + mid + props.getMaxMessageInFlow() + suf);
        sb.append(pre + "PoolNetwork.IdleTimeout" + mid + props.getIdleTimeout() + suf);
      } else if (property instanceof HttpNetworkProperties) {
        HttpNetworkProperties props = (HttpNetworkProperties) property;
        sb.append(pre + "ActivationPeriod" + mid + props.getActivationPeriod() + suf);
        sb.append(pre + "NbDaemon" + mid + props.getNbDaemon() + suf);
      } else if (property instanceof NetworkProperties) {
        NetworkProperties props = (NetworkProperties) property;
        sb.append(pre + "backlog" + mid + props.getBacklog() + suf);
        sb.append(pre + "CnxRetry" + mid + props.getCnxRetry() + suf);
        sb.append(pre + "TcpNoDelay" + mid + props.isTcpNoDelay() + suf);
        sb.append(pre + "SoLinger" + mid + props.getSoLinger() + suf);
        sb.append(pre + "SoTimeout" + mid + props.getSoTimeout() + suf);
        sb.append(pre + "ConnectTimeout" + mid + props.getConnectTimeout() + suf);
      } else if (property instanceof NTransactionProperties) {
        NTransactionProperties props = (NTransactionProperties) property;
        sb.append(pre + "NTLogMemorySize" + mid + props.getNTLogMemorySize() + suf);
        sb.append(pre + "NTLogFileSize" + mid + props.getNTLogFileSize() + suf);
        sb.append(pre + "NTNoLockFile" + mid + props.isNTNoLockFile() + suf);
        sb.append(pre + "NTLogThresholdOperation" + mid + props.getNTLogThresholdOperation() + suf);
        sb.append(pre + "NTLogMemoryCapacity" + mid + props.getNTLogMemoryCapacity() + suf);
      } else if (property instanceof TransactionProperty) {
        TransactionProperty prop = (TransactionProperty) property;
        sb.append(pre + "Transaction" + mid + prop.getTransaction() + suf);
      } else if (property instanceof CustomProperty) {
        CustomProperty prop = (CustomProperty) property;
        sb.append(pre + prop.getName() + mid + prop.getValue() + suf);
      }
    }
    return sb;
  }
  
  private StringBuilder appendServices(EList<JoramService> services, StringBuilder sb) {
    String pre = "    <service class='";
    String mid = "' args='";
    String suf = "' />\n";
    for (JoramService service : services) {
      if (service instanceof ConnectionManager) {
        ConnectionManager cm = (ConnectionManager) service;
        sb.append(pre + "org.objectweb.joram.mom.proxies.ConnectionManager" + mid + cm.getUser() + ' '
            + cm.getPassword() + suf);
      } else if (service instanceof AdminProxy) {
        AdminProxy ap = (AdminProxy) service;
        sb.append(pre + "fr.dyade.aaa.agent.AdminProxy" + mid + ap.getPort() + suf);
      } else if (service instanceof TCPProxyService) {
        TCPProxyService tcpps = (TCPProxyService) service;
        sb.append(pre + "org.objectweb.joram.mom.proxies.tcp.TcpProxyService" + mid + tcpps.getPort() + suf);
      } else if (service instanceof JNDIServer) {
        JNDIServer jndi = (JNDIServer) service;
        sb.append(pre + "fr.dyade.aaa.jndi2.server.JndiServer" + mid + jndi.getPort() + suf);
      } else if (service instanceof CustomService) {
        CustomService cs = (CustomService) service;
        if (cs.getArgs() == null) {
          sb.append(pre + cs.getClassName() + suf);
        } else {
          sb.append(pre + cs.getClassName() + mid + cs.getArgs() + suf);
        }
      } else if (service instanceof DistributedJNDIServer) {
        DistributedJNDIServer distjndi = (DistributedJNDIServer) service;
        sb.append(pre + "fr.dyade.aaa.jndi2.distributed.DistributedJndiServer" + mid + distjndi.getPort());
        EList<ScalAgentServer> jndiServers = distjndi.getKnownServers();
        for (ScalAgentServer jndiServer : jndiServers) {
          sb.append(" " + jndiServer.getSid());
        }
        sb.append(suf);
      }
    }
    return sb;
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    this.selection = selection;
  }

}
