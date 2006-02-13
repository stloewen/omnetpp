package org.omnetpp.ned.editor.graph.misc;

import java.util.Iterator;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

// TODO make it more general and to be able to specify the relative attachment point of each figure
// create a new contraint object for this purpose
public class DesktopLayout extends XYLayout {
    /**
     * Implements the algorithm to layout the components of the given container figure.
     * Each component is laid out using its own layout constraint specifying its size
     * and position.
     * 
     * @see LayoutManager#layout(IFigure)
     */
    public void layout(IFigure parent) {
        Iterator children = parent.getChildren().iterator();
        Point offset = getOrigin(parent);
        IFigure f;
        while (children.hasNext()) {
            f = (IFigure)children.next();
            Rectangle bounds = (Rectangle)getConstraint(f);
            if (bounds == null) continue;

            if (bounds.width == -1 || bounds.height == -1) {
                Dimension preferredSize = f.getPreferredSize(bounds.width, bounds.height);
                bounds = bounds.getCopy();
                if (bounds.width == -1)
                    bounds.width = preferredSize.width;
                if (bounds.height == -1)
                    bounds.height = preferredSize.height;
            }
            bounds = bounds.getTranslated(offset);
            // translate to the middle of the figure
            bounds.translate(-bounds.width / 2, -bounds.height / 2);
            f.setBounds(bounds);
        }
    }
}
