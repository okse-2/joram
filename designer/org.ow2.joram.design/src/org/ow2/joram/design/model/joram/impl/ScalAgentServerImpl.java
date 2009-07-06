/**
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
 *
 * $Id$
 */
package org.ow2.joram.design.model.joram.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.ow2.joram.design.model.joram.JoramPackage;
import org.ow2.joram.design.model.joram.JoramService;
import org.ow2.joram.design.model.joram.JoramVersion;
import org.ow2.joram.design.model.joram.ScalAgentServer;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Scal Agent Server</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.ow2.joram.design.model.joram.impl.ScalAgentServerImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.ow2.joram.design.model.joram.impl.ScalAgentServerImpl#getVersion <em>Version</em>}</li>
 *   <li>{@link org.ow2.joram.design.model.joram.impl.ScalAgentServerImpl#getHostname <em>Hostname</em>}</li>
 *   <li>{@link org.ow2.joram.design.model.joram.impl.ScalAgentServerImpl#getServices <em>Services</em>}</li>
 *   <li>{@link org.ow2.joram.design.model.joram.impl.ScalAgentServerImpl#getSid <em>Sid</em>}</li>
 *   <li>{@link org.ow2.joram.design.model.joram.impl.ScalAgentServerImpl#getStorageDirectory <em>Storage Directory</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ScalAgentServerImpl extends EObjectImpl implements ScalAgentServer {
  /**
   * The default value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected static final String NAME_EDEFAULT = "JORAM";

  /**
   * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected String name = NAME_EDEFAULT;

  /**
   * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getVersion()
   * @generated
   * @ordered
   */
  protected static final JoramVersion VERSION_EDEFAULT = JoramVersion.JORAM_524;

  /**
   * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getVersion()
   * @generated
   * @ordered
   */
  protected JoramVersion version = VERSION_EDEFAULT;

  /**
   * The default value of the '{@link #getHostname() <em>Hostname</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getHostname()
   * @generated
   * @ordered
   */
  protected static final String HOSTNAME_EDEFAULT = "localhost";

  /**
   * The cached value of the '{@link #getHostname() <em>Hostname</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getHostname()
   * @generated
   * @ordered
   */
  protected String hostname = HOSTNAME_EDEFAULT;

  /**
   * The cached value of the '{@link #getServices() <em>Services</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getServices()
   * @generated
   * @ordered
   */
  protected EList<JoramService> services;

  /**
   * The default value of the '{@link #getSid() <em>Sid</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSid()
   * @generated
   * @ordered
   */
  protected static final short SID_EDEFAULT = 0;

  /**
   * The cached value of the '{@link #getSid() <em>Sid</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSid()
   * @generated
   * @ordered
   */
  protected short sid = SID_EDEFAULT;

  /**
   * The default value of the '{@link #getStorageDirectory() <em>Storage Directory</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getStorageDirectory()
   * @generated
   * @ordered
   */
  protected static final String STORAGE_DIRECTORY_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getStorageDirectory() <em>Storage Directory</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getStorageDirectory()
   * @generated
   * @ordered
   */
  protected String storageDirectory = STORAGE_DIRECTORY_EDEFAULT;

  /**
   * This is true if the Storage Directory attribute has been set.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  protected boolean storageDirectoryESet;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ScalAgentServerImpl() {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass() {
    return JoramPackage.Literals.SCAL_AGENT_SERVER;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getName() {
    return name;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setName(String newName) {
    String oldName = name;
    name = newName;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, JoramPackage.SCAL_AGENT_SERVER__NAME, oldName, name));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public JoramVersion getVersion() {
    return version;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setVersion(JoramVersion newVersion) {
    JoramVersion oldVersion = version;
    version = newVersion == null ? VERSION_EDEFAULT : newVersion;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, JoramPackage.SCAL_AGENT_SERVER__VERSION, oldVersion, version));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setHostname(String newHostname) {
    String oldHostname = hostname;
    hostname = newHostname;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, JoramPackage.SCAL_AGENT_SERVER__HOSTNAME, oldHostname, hostname));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<JoramService> getServices() {
    if (services == null) {
      services = new EObjectContainmentEList<JoramService>(JoramService.class, this, JoramPackage.SCAL_AGENT_SERVER__SERVICES);
    }
    return services;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public short getSid() {
    return sid;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSid(short newSid) {
    short oldSid = sid;
    sid = newSid;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, JoramPackage.SCAL_AGENT_SERVER__SID, oldSid, sid));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getStorageDirectory() {
    return storageDirectory;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setStorageDirectory(String newStorageDirectory) {
    String oldStorageDirectory = storageDirectory;
    storageDirectory = newStorageDirectory;
    boolean oldStorageDirectoryESet = storageDirectoryESet;
    storageDirectoryESet = true;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, JoramPackage.SCAL_AGENT_SERVER__STORAGE_DIRECTORY, oldStorageDirectory, storageDirectory, !oldStorageDirectoryESet));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void unsetStorageDirectory() {
    String oldStorageDirectory = storageDirectory;
    boolean oldStorageDirectoryESet = storageDirectoryESet;
    storageDirectory = STORAGE_DIRECTORY_EDEFAULT;
    storageDirectoryESet = false;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.UNSET, JoramPackage.SCAL_AGENT_SERVER__STORAGE_DIRECTORY, oldStorageDirectory, STORAGE_DIRECTORY_EDEFAULT, oldStorageDirectoryESet));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public boolean isSetStorageDirectory() {
    return storageDirectoryESet;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
    switch (featureID) {
      case JoramPackage.SCAL_AGENT_SERVER__SERVICES:
        return ((InternalEList<?>)getServices()).basicRemove(otherEnd, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType) {
    switch (featureID) {
      case JoramPackage.SCAL_AGENT_SERVER__NAME:
        return getName();
      case JoramPackage.SCAL_AGENT_SERVER__VERSION:
        return getVersion();
      case JoramPackage.SCAL_AGENT_SERVER__HOSTNAME:
        return getHostname();
      case JoramPackage.SCAL_AGENT_SERVER__SERVICES:
        return getServices();
      case JoramPackage.SCAL_AGENT_SERVER__SID:
        return new Short(getSid());
      case JoramPackage.SCAL_AGENT_SERVER__STORAGE_DIRECTORY:
        return getStorageDirectory();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @SuppressWarnings("unchecked")
  @Override
  public void eSet(int featureID, Object newValue) {
    switch (featureID) {
      case JoramPackage.SCAL_AGENT_SERVER__NAME:
        setName((String)newValue);
        return;
      case JoramPackage.SCAL_AGENT_SERVER__VERSION:
        setVersion((JoramVersion)newValue);
        return;
      case JoramPackage.SCAL_AGENT_SERVER__HOSTNAME:
        setHostname((String)newValue);
        return;
      case JoramPackage.SCAL_AGENT_SERVER__SERVICES:
        getServices().clear();
        getServices().addAll((Collection<? extends JoramService>)newValue);
        return;
      case JoramPackage.SCAL_AGENT_SERVER__SID:
        setSid(((Short)newValue).shortValue());
        return;
      case JoramPackage.SCAL_AGENT_SERVER__STORAGE_DIRECTORY:
        setStorageDirectory((String)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID) {
    switch (featureID) {
      case JoramPackage.SCAL_AGENT_SERVER__NAME:
        setName(NAME_EDEFAULT);
        return;
      case JoramPackage.SCAL_AGENT_SERVER__VERSION:
        setVersion(VERSION_EDEFAULT);
        return;
      case JoramPackage.SCAL_AGENT_SERVER__HOSTNAME:
        setHostname(HOSTNAME_EDEFAULT);
        return;
      case JoramPackage.SCAL_AGENT_SERVER__SERVICES:
        getServices().clear();
        return;
      case JoramPackage.SCAL_AGENT_SERVER__SID:
        setSid(SID_EDEFAULT);
        return;
      case JoramPackage.SCAL_AGENT_SERVER__STORAGE_DIRECTORY:
        unsetStorageDirectory();
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * Returns always true for server id in order to be referenced correctly by
   * the network ports in the resulting xml.
   * 
   * @generated not
   */
  @Override
  public boolean eIsSet(int featureID) {
    switch (featureID) {
      case JoramPackage.SCAL_AGENT_SERVER__NAME:
        return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
      case JoramPackage.SCAL_AGENT_SERVER__VERSION:
        return version != VERSION_EDEFAULT;
      case JoramPackage.SCAL_AGENT_SERVER__HOSTNAME:
        return HOSTNAME_EDEFAULT == null ? hostname != null : !HOSTNAME_EDEFAULT.equals(hostname);
      case JoramPackage.SCAL_AGENT_SERVER__SERVICES:
        return services != null && !services.isEmpty();
      case JoramPackage.SCAL_AGENT_SERVER__SID:
        return true;
      case JoramPackage.SCAL_AGENT_SERVER__STORAGE_DIRECTORY:
        return isSetStorageDirectory();
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString() {
    if (eIsProxy()) return super.toString();

    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (name: ");
    result.append(name);
    result.append(", version: ");
    result.append(version);
    result.append(", hostname: ");
    result.append(hostname);
    result.append(", sid: ");
    result.append(sid);
    result.append(", StorageDirectory: ");
    if (storageDirectoryESet) result.append(storageDirectory); else result.append("<unset>");
    result.append(')');
    return result.toString();
  }

} //ScalAgentServerImpl
