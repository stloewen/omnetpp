/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.omnetpp.ned.editor.graph.edit;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.*;
import org.eclipse.gef.*;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.SnapFeedbackPolicy;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.tools.DeselectAllTracker;
import org.eclipse.gef.tools.MarqueeDragTracker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.omnetpp.ned.editor.graph.edit.policies.NedLayoutEditPolicy;
import org.omnetpp.ned.editor.graph.misc.FreeformDesktopLayout;
import org.omnetpp.ned.editor.graph.misc.MessageFactory;
import org.omnetpp.ned.editor.graph.model.Container;

/**
 * Holds all other NedEditParts under this. It is activated by
 * LogicEditorPart, to hold the entire model. It is sort of a blank board where
 * all other EditParts get added.
 */
public class DiagramEditPart extends ContainerEditPart implements LayerConstants {

    protected AccessibleEditPart createAccessible() {
        return new AccessibleGraphicalEditPart() {
            public void getName(AccessibleEvent e) {
                e.result = MessageFactory.LogicDiagram_LabelText;
            }
        };
    }

    /**
     * Installs EditPolicies specific to this.
     */
    protected void createEditPolicies() {
        super.createEditPolicies();

        installEditPolicy(EditPolicy.NODE_ROLE, null);
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new NedLayoutEditPolicy((XYLayout) getContentPane()
                .getLayoutManager()));

        installEditPolicy("Snap Feedback", new SnapFeedbackPolicy()); //$NON-NLS-1$
    }

    /**
     * Returns a Figure to represent this.
     * 
     * @return Figure.
     */
    protected IFigure createFigure() {
        Figure f = new FreeformLayer();
        f.setLayoutManager(new FreeformDesktopLayout());
        f.setBorder(new MarginBorder(5));
        // f.setBorder(new GroupBoxBorder(" Module "));
        return f;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == SnapToHelper.class) {
            List snapStrategies = new ArrayList();
            Boolean val = (Boolean) getViewer().getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);
            if (val != null && val.booleanValue()) snapStrategies.add(new SnapToGuides(this));
            val = (Boolean) getViewer().getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
            if (val != null && val.booleanValue()) snapStrategies.add(new SnapToGeometry(this));
            val = (Boolean) getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
            if (val != null && val.booleanValue()) snapStrategies.add(new SnapToGrid(this));

            if (snapStrategies.size() == 0) return null;
            if (snapStrategies.size() == 1) return (SnapToHelper) snapStrategies.get(0);

            SnapToHelper ss[] = new SnapToHelper[snapStrategies.size()];
            for (int i = 0; i < snapStrategies.size(); i++)
                ss[i] = (SnapToHelper) snapStrategies.get(i);
            return new CompoundSnapToHelper(ss);
        }
        return super.getAdapter(adapter);
    }

    public DragTracker getDragTracker(Request req) {
        if (req instanceof SelectionRequest && ((SelectionRequest) req).getLastButtonPressed() == 3)
            return new DeselectAllTracker(this);
        return new MarqueeDragTracker();
    }

    /**
     * Returns <code>NULL</code> as it does not hold any connections.
     * 
     * @return ConnectionAnchor
     */
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart editPart) {
        return null;
    }

    /**
     * Returns <code>NULL</code> as it does not hold any connections.
     * 
     * @return ConnectionAnchor
     */
    public ConnectionAnchor getSourceConnectionAnchor(int x, int y) {
        return null;
    }

    /**
     * Returns <code>NULL</code> as it does not hold any connections.
     * 
     * @return ConnectionAnchor
     */
    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart editPart) {
        return null;
    }

    /**
     * Returns <code>NULL</code> as it does not hold any connections.
     * 
     * @return ConnectionAnchor
     */
    public ConnectionAnchor getTargetConnectionAnchor(int x, int y) {
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (Container.PROP_ROUTER.equals(evt.getPropertyName()))
            refreshVisuals();
        else
            super.propertyChange(evt);
    }

    protected void refreshVisuals() {
        ConnectionLayer cLayer = (ConnectionLayer) getLayer(CONNECTION_LAYER);
        cLayer.setAntialias(SWT.ON);

        if (getContainerModel().getConnectionRouter().equals(Container.ROUTER_MANUAL)) {
            AutomaticRouter router = new FanRouter();
            router.setNextRouter(new BendpointConnectionRouter());
            cLayer.setConnectionRouter(router);
        } else if (getContainerModel().getConnectionRouter().equals(Container.ROUTER_MANHATTAN))
            cLayer.setConnectionRouter(new ManhattanConnectionRouter());
        else
            cLayer.setConnectionRouter(new ShortestPathConnectionRouter(getFigure()));
    }

}