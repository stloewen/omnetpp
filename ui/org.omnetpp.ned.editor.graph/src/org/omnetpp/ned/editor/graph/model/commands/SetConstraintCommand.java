package org.omnetpp.ned.editor.graph.model.commands;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.omnetpp.ned2.model.INedModule;

/**
 * Change the size and location of the element
 * @author rhornig
 *
 */
public class SetConstraintCommand extends org.eclipse.gef.commands.Command {

    private Point newPos;
    private Dimension newSize;
    private Point oldPos;
    private Dimension oldSize;
    private INedModule module;

    @Override
    public String getLabel() {
        if (oldSize.equals(newSize)) 
            return "Move " + module.getName();
        return "Resize " + module.getName();
    }

    @Override
    public void execute() {
        // XXX just for debugging
//        System.out.println(ModelUtil.printPojoElementTree((NEDElement)module, ""));
        oldSize = module.getSize();
        oldPos = module.getLocation();
        redo();
    }

    @Override
    public void redo() {
        module.setSize(newSize);
        module.setLocation(newPos);
    }

    public void setLocation(Rectangle r) {
        setLocation(r.getLocation());
        setSize(r.getSize());
    }

    public void setLocation(Point p) {
        newPos = p;
    }

    public void setModule(INedModule newModule) {
        module = newModule;
    }

    public void setSize(Dimension p) {
        newSize = p;
    }

    @Override
    public void undo() {
        module.setSize(oldSize);
        module.setLocation(oldPos);
    }

}