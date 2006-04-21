package org.omnetpp.ned.editor.graph.edit;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.omnetpp.ned.editor.graph.edit.policies.NedContainerEditPolicy;
import org.omnetpp.ned2.model.INEDChangeListener;
import org.omnetpp.ned2.model.INedModule;
import org.omnetpp.ned2.model.NEDElement;

/**
 * Provides support for Container EditParts.
 */
abstract public class ContainerEditPart 
   extends AbstractGraphicalEditPart implements INEDChangeListener  {

    @Override
    public void activate() {
        if (isActive()) return;
        super.activate();
        // register as listener of the model object
        getNEDModel().addListener(this);
    }

    /**
     * Makes the EditPart insensible to changes in the model by removing itself
     * from the model's list of listeners.
     */
    @Override
    public void deactivate() {
        if (!isActive()) return;
        super.deactivate();
        getNEDModel().removeListener(this);
    }

    /**
     * Installs the desired EditPolicies for this.
     */
    @Override
    protected void createEditPolicies() {
    	// XXX check wheter this policy is ok on Submodule too?
        installEditPolicy(EditPolicy.CONTAINER_ROLE, new NedContainerEditPolicy());
    }

    /**
     * Returns the model associated with this as a NEDElement.
     * 
     * @return The model of this as a NedElement.
     */
    protected NEDElement getNEDModel() {
        return (NEDElement) getModel();
    }

    // TODO we must pass the old element too
    public void attributeChanged(NEDElement node, String attr) {
        if (INedModule.ATT_SRC_CONNECTION.equals(attr))
            refreshSourceConnections();
        
        else if (INedModule.ATT_DEST_CONNECTION.equals(attr)) 
            refreshTargetConnections();
        
        else    
            refreshVisuals();

	}

	public void childInserted(NEDElement node, NEDElement where, NEDElement child) {
        // TODO connection adding and removal should be optimzed (eg. use a separate 
        // connection added/removed event)
        // maybe refreshTargetConnections() is not needed here
//        refreshVisuals();
		refreshChildren();
//		refreshSourceConnections();
//		refreshTargetConnections();
	}

	public void childRemoved(NEDElement node, NEDElement child) {
//        refreshVisuals();
		refreshChildren();

//		refreshSourceConnections();
//		refreshTargetConnections();
	}
}
