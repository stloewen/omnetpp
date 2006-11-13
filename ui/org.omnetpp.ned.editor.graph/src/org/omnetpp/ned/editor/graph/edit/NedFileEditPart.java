package org.omnetpp.ned.editor.graph.edit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.omnetpp.ned.editor.graph.edit.policies.NedFileLayoutEditPolicy;
import org.omnetpp.ned2.model.ex.CompoundModuleNodeEx;
import org.omnetpp.ned2.model.ex.NedFileNodeEx;
import org.omnetpp.ned2.model.interfaces.ITopLevelElement;
import org.omnetpp.ned2.model.notification.NEDAttributeChangeEvent;
import org.omnetpp.ned2.model.notification.NEDModelEvent;
import org.omnetpp.resources.NEDResourcesPlugin;

/**
 * Holds all other NedEditParts under this. It is activated by
 * LogicEditorPart, to hold the entire model. It is sort of a blank board where
 * all other EditParts get added.
 */
public class NedFileEditPart extends ContainerEditPart implements LayerConstants {

    /**
     * Installs EditPolicies specific to this.
     */
    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();

        // NedFile cannot be connectoed to anything via connections
        installEditPolicy(EditPolicy.NODE_ROLE, null);
        // 
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
        // nedfile cannot be selected
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
        // this is a root edit part, so it cannot be deleted
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        // install a layout edit policy, this one provides also the creation commands
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new NedFileLayoutEditPolicy());
        // have some snap feedback once it enabled
        // installEditPolicy("Snap Feedback", new SnapFeedbackPolicy()); //$NON-NLS-1$
    }

    /**
     * Returns a Figure to represent this.
     * 
     * @return Figure.
     */
    @Override
    protected IFigure createFigure() {
    	Figure f = new Layer();
    	ToolbarLayout fl = new ToolbarLayout();
    	fl.setStretchMinorAxis(false);
    	fl.setSpacing(20);
    	f.setLayoutManager(fl);
    	
        f.setBorder(new MarginBorder(5));
        return f;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
	@Override
    public Object getAdapter(Class adapter) {
        if (adapter == SnapToHelper.class) {
            List snapStrategies = new ArrayList();
            Boolean val = (Boolean) getViewer().getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
            if (val != null && val.booleanValue()) snapStrategies.add(new SnapToGeometry(this));

            if (snapStrategies.size() == 0) return null;
            if (snapStrategies.size() == 1) return snapStrategies.get(0);

            SnapToHelper ss[] = new SnapToHelper[snapStrategies.size()];
            for (int i = 0; i < snapStrategies.size(); i++)
                ss[i] = (SnapToHelper) snapStrategies.get(i);
            return new CompoundSnapToHelper(ss);
        }
        return super.getAdapter(adapter);
    }

    @Override
    protected List getModelChildren() {
    	return ((NedFileNodeEx)getNEDModel()).getTopLevelElements();
    }

    @Override
    public void modelChanged(NEDModelEvent event) {
        super.modelChanged(event);
        if (!(event.getSource() instanceof ITopLevelElement))
            return;

        // if it's an attribute change, we don't care except it is a name change. in this case we must make
        // a full rehash.
        // FIXME we should check for changes in exteds and like nodes too
        if (event instanceof NEDAttributeChangeEvent 
                && !CompoundModuleNodeEx.ATT_NAME.equals(((NEDAttributeChangeEvent)event).getAttribute()))
            return;

        // invalidat the type info caceh and redraw the children
        NEDResourcesPlugin.getNEDResources().invalidate();
        refreshChildren();
    }
    
}    
