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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.ow2.joram.design.model.joram.JoramPackage;
import org.ow2.joram.design.model.joram.NetworkDomain;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Network Domain</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.ow2.joram.design.model.joram.impl.NetworkDomainImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.ow2.joram.design.model.joram.impl.NetworkDomainImpl#getNetworkClass <em>Network Class</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class NetworkDomainImpl extends EObjectImpl implements NetworkDomain {
  /**
   * The default value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected static final String NAME_EDEFAULT = "D0";

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
   * The default value of the '{@link #getNetworkClass() <em>Network Class</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getNetworkClass()
   * @generated
   * @ordered
   */
  protected static final String NETWORK_CLASS_EDEFAULT = "fr.dyade.aaa.agent.PoolNetwork";

  /**
   * The cached value of the '{@link #getNetworkClass() <em>Network Class</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getNetworkClass()
   * @generated
   * @ordered
   */
  protected String networkClass = NETWORK_CLASS_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected NetworkDomainImpl() {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass() {
    return JoramPackage.Literals.NETWORK_DOMAIN;
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
      eNotify(new ENotificationImpl(this, Notification.SET, JoramPackage.NETWORK_DOMAIN__NAME, oldName, name));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getNetworkClass() {
    return networkClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setNetworkClass(String newNetworkClass) {
    String oldNetworkClass = networkClass;
    networkClass = newNetworkClass;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, JoramPackage.NETWORK_DOMAIN__NETWORK_CLASS, oldNetworkClass, networkClass));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType) {
    switch (featureID) {
      case JoramPackage.NETWORK_DOMAIN__NAME:
        return getName();
      case JoramPackage.NETWORK_DOMAIN__NETWORK_CLASS:
        return getNetworkClass();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eSet(int featureID, Object newValue) {
    switch (featureID) {
      case JoramPackage.NETWORK_DOMAIN__NAME:
        setName((String)newValue);
        return;
      case JoramPackage.NETWORK_DOMAIN__NETWORK_CLASS:
        setNetworkClass((String)newValue);
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
      case JoramPackage.NETWORK_DOMAIN__NAME:
        setName(NAME_EDEFAULT);
        return;
      case JoramPackage.NETWORK_DOMAIN__NETWORK_CLASS:
        setNetworkClass(NETWORK_CLASS_EDEFAULT);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * Returns always true for domain dame in order to be referenced correctly by
   * the network ports in the resulting xml.
   * 
   * @generated not
   */
  @Override
  public boolean eIsSet(int featureID) {
    switch (featureID) {
      case JoramPackage.NETWORK_DOMAIN__NAME:
        return true;
      case JoramPackage.NETWORK_DOMAIN__NETWORK_CLASS:
        return NETWORK_CLASS_EDEFAULT == null ? networkClass != null : !NETWORK_CLASS_EDEFAULT.equals(networkClass);
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
    result.append(", networkClass: ");
    result.append(networkClass);
    result.append(')');
    return result.toString();
  }

} //NetworkDomainImpl
