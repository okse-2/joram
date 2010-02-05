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
import org.ow2.joram.design.model.joram.diagram.edit.parts.ConnectionFactoryEditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.CustomProperty2EditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.CustomPropertyEditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.CustomService2EditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.CustomServiceEditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.PoolNetworkPropertiesEditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.QueueEditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.TCPProxyService2EditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.TopicEditPart;
import org.ow2.joram.design.model.joram.diagram.edit.parts.UserEditPart;
import org.ow2.joram.design.model.joram.diagram.part.JoramDiagramUpdater;
import org.ow2.joram.design.model.joram.diagram.part.JoramNodeDescriptor;
import org.ow2.joram.design.model.joram.diagram.part.JoramVisualIDRegistry;

/**
 * @generated
 */
public class JORAMJMSCanonicalEditPolicy extends CanonicalEditPolicy {

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
    for (Iterator it = JoramDiagramUpdater.getJORAMJMS_7002SemanticChildren(viewObject).iterator(); it
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
    case TopicEditPart.VISUAL_ID:
    case QueueEditPart.VISUAL_ID:
    case UserEditPart.VISUAL_ID:
    case ConnectionFactoryEditPart.VISUAL_ID:
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
      myFeaturesToSynchronize.add(JoramPackage.eINSTANCE.getJORAM_JmsObjects());
    }
    return myFeaturesToSynchronize;
  }

}
