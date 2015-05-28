/*
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.ow2.joram.design.model.export.wizard;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.ow2.joram.design.model.joram.AdminProxy;
import org.ow2.joram.design.model.joram.Config;
import org.ow2.joram.design.model.joram.ConnectionManager;
import org.ow2.joram.design.model.joram.CustomProperty;
import org.ow2.joram.design.model.joram.CustomService;
import org.ow2.joram.design.model.joram.DistributedJNDIServer;
import org.ow2.joram.design.model.joram.HttpNetworkProperties;
import org.ow2.joram.design.model.joram.JNDIServer;
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

public class ExportA3ConfWizard extends Wizard {

  private static final String CONNECTION_MANAGER_CLASS = "org.objectweb.joram.mom.proxies.ConnectionManager";

  private static final String ADMIN_PROXY_CLASS = "fr.dyade.aaa.agent.AdminProxy";

  private static final String TCP_PROXY_CLASS = "org.objectweb.joram.mom.proxies.tcp.TcpProxyService";

  private static final String JNDI_SERVER_CLASS = "fr.dyade.aaa.jndi2.server.JndiServer";

  private static final String DISTRIBUTED_JNDI_SERVER_CLASS = "fr.dyade.aaa.jndi2.distributed.DistributedJndiServer";

  private WizardNewFileCreationPage page1;

  private IStructuredSelection myselection;

  protected Config joramConf;

  public ExportA3ConfWizard(Config conf, IStructuredSelection myselection) {
    this.joramConf = conf;
    this.myselection = myselection;
  }

  public void addPages() {

    page1 = new WizardNewFileCreationPage("JoramAdminNewFileCreationPage", myselection);
    page1.setFileName("a3servers.xml");
    page1.setFileExtension("xml");
    page1.setAllowExistingResources(true);
    page1.setTitle("Save servers configuration.");
    page1.setDescription("Create a new servers configuration.");

    addPage(page1);
  }

  public boolean performFinish() {
    try {
      String generated = generateA3Conf(joramConf);

      IFile outputFile = page1.createNewFile();
      outputFile.setContents(new ByteArrayInputStream(generated.getBytes()), true, true, null);
    } catch (Exception exc) {
      MessageDialog.openError(getShell(), "Export Plug-in", "Export to a3servers.xml failed: "
          + exc.getMessage());
      return false;
    }
    return true;
  }

  private String generateA3Conf(Config rootElement) {

    StringBuilder sb = new StringBuilder();
    sb.append("<config>\n");

    if (rootElement.getProperties() != null) {
      appendProperties(rootElement.getProperties().getProperties(), sb, null, "  ");
    }

    for (NetworkDomain domain : rootElement.getDomains()) {
      sb.append('\n');
      sb.append("  <domain name='" + domain.getName());
      sb.append("' network='" + domain.getNetwork().getLiteral() + "' />\n");
      appendProperties(domain.getProperties(), sb, domain, "  ");
    }

    for (ScalAgentServer server : rootElement.getServers()) {
      sb.append('\n');
      sb.append("  <server id='" + server.getSid());
      sb.append("' name='s" + server.getSid());
      sb.append("' hostname='" + server.getHost().getHostName() + "'>\n");

      appendServices(server.getServices(), sb);

      EList<NetworkPort> ports = server.getNetwork();
      for (NetworkPort port : ports) {
        sb.append("    <network domain='" + port.getDomain().getName());
        sb.append("' port='" + port.getPort() + "' />\n");
      }

      appendProperties(server.getProperties(), sb, null, "    ");

      sb.append("  </server>\n");
    }
    sb.append("</config>\n");

    return sb.toString();
  }

  private StringBuilder appendProperties(EList<Property> properties, StringBuilder sb, NetworkDomain domain,
      String indent) {
    String pre = (domain == null) ? (indent + "<property name='") : (indent + "<property name='"
        + domain.getName() + '.');
    String mid = "' value='";
    String suf = "' />\n";
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
        sb.append(pre + CONNECTION_MANAGER_CLASS + mid + cm.getUser() + ' ' + cm.getPassword() + suf);
      } else if (service instanceof AdminProxy) {
        AdminProxy ap = (AdminProxy) service;
        sb.append(pre + ADMIN_PROXY_CLASS + mid + ap.getPort() + suf);
      } else if (service instanceof TCPProxyService) {
        TCPProxyService tcpps = (TCPProxyService) service;
        sb.append(pre + TCP_PROXY_CLASS + mid + tcpps.getPort() + suf);
      } else if (service instanceof JNDIServer) {
        JNDIServer jndi = (JNDIServer) service;
        sb.append(pre + JNDI_SERVER_CLASS + mid + jndi.getPort() + suf);
      } else if (service instanceof CustomService) {
        CustomService cs = (CustomService) service;
        if (cs.getArgs() == null) {
          sb.append(pre + cs.getClassName() + suf);
        } else {
          sb.append(pre + cs.getClassName() + mid + cs.getArgs() + suf);
        }
      } else if (service instanceof DistributedJNDIServer) {
        DistributedJNDIServer distjndi = (DistributedJNDIServer) service;
        sb.append(pre + DISTRIBUTED_JNDI_SERVER_CLASS + mid + distjndi.getPort());
        EList<ScalAgentServer> jndiServers = distjndi.getKnownServers();
        for (ScalAgentServer jndiServer : jndiServers) {
          sb.append(" " + jndiServer.getSid());
        }
        sb.append(suf);
      }
    }
    return sb;
  }

}
