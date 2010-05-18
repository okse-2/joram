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
package org.ow2.joram.design.model.joram.diagram.edit.policies;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
import org.eclipse.gmf.runtime.notation.View;
import org.ow2.joram.design.model.joram.JoramPackage;
import org.ow2.joram.design.model.joram.diagram.edit.parts.AdminProxyEditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.ConnectionManagerEditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.CustomServiceEditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.DistributedJNDIServerEditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.JNDIServerEditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.TCPProxyServiceEditPart;
import org.ow2.joram.design.model.joram.diagram.part.JoramDiagramUpdater;
import org.ow2.joram.design.model.joram.diagram.part.JoramNodeDescriptor;
import org.ow2.joram.design.model.joram.diagram.part.JoramVisualIDRegistry;

/**
 * @generated
 */
public class JORAMServicesCanonicalEditPolicy extends CanonicalEditPolicy {

  /**
   * @generated
   */
  Set myFeaturesToSynchronize;

  /**
   * @generated NOT
   */
  @Override
  protected String getFactoryHint(IAdaptable elementAdapter) {
    CanonicalElementAdapter element = (CanonicalElementAdapter) elementAdapter;
    int VID = JoramVisualIDRegistry.getNodeVisualID((View) getHost().getModel(), (EObject) element
        .getRealObject());
    return JoramVisualIDRegistry.getType(VID);
  }

  /**
   * @generated
   */
  protected List getSemanticChildrenList() {
    View viewObject = (View) getHost().getModel();
    List result = new LinkedList();
    for (Iterator it = JoramDiagramUpdater.getJORAMServices_7001SemanticChildren(viewObject).iterator(); it
        .hasNext();) {
      result.add(((JoramNodeDescriptor) it.next()).getModelElement());
    }
    return result;
  }

  /**
   * @generated
   */
  protected boolean isOrphaned(Collection semanticChildren, final View view) {
    int visualID = JoramVisualIDRegistry.getVisualID(view);
    switch (visualID) {
    case AdminProxyEditPart.VISUAL_ID:
    case ConnectionManagerEditPart.VISUAL_ID:
    case JNDIServerEditPart.VISUAL_ID:
    case DistributedJNDIServerEditPart.VISUAL_ID:
    case TCPProxyServiceEditPart.VISUAL_ID:
    case CustomServiceEditPart.VISUAL_ID:
      if (!semanticChildren.contains(view.getElement())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @generated
   */
  protected String getDefaultFactoryHint() {
    return null;
  }

  /**
   * @generated
   */
  protected Set getFeaturesToSynchronize() {
    if (myFeaturesToSynchronize == null) {
      myFeaturesToSynchronize = new HashSet();
      myFeaturesToSynchronize.add(JoramPackage.eINSTANCE.getScalAgentServer_Services());
    }
    return myFeaturesToSynchronize;
  }

}
