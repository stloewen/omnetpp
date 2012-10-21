package org.omnetpp.simulation.inspectors;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.omnetpp.common.color.ColorFactory;
import org.omnetpp.simulation.canvas.IInspectorContainer;
import org.omnetpp.simulation.canvas.SelectionItem;
import org.omnetpp.simulation.canvas.SelectionUtils;
import org.omnetpp.simulation.figures.FigureUtils;
import org.omnetpp.simulation.inspectors.actions.InspectParentAction;
import org.omnetpp.simulation.inspectors.actions.SetModeAction;
import org.omnetpp.simulation.inspectors.actions.SortAction;
import org.omnetpp.simulation.model.cObject;
import org.omnetpp.simulation.ui.ObjectFieldsViewer;
import org.omnetpp.simulation.ui.ObjectFieldsViewer.Mode;
import org.omnetpp.simulation.ui.ObjectFieldsViewer.Ordering;

/**
 *
 * @author Andras
 */
//TODO adaptive label: display the most useful info that fits in the available space
public class ObjectFieldsInspectorPart extends AbstractSWTInspectorPart {
    private Composite frame;
    private Label label;
    private ObjectFieldsViewer viewer;

    public ObjectFieldsInspectorPart(IInspectorContainer parent, cObject object) {
        super(parent, object);
    }

    @Override
    protected Control createControl(Composite parent) {
        if (!object.isFilledIn())
            object.safeLoad(); // for getClassName() in next line

        frame = new Composite(parent, SWT.BORDER);
        frame.setSize(300, 200);
        frame.setLayout(new GridLayout(1, false));

        label = new Label(frame, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        viewer = new ObjectFieldsViewer(frame, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
        viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setMode(ObjectFieldsViewer.getPreferredMode(object));
        viewer.setInput(object);

        // double-click to inspect
        viewer.getTree().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // inspect the selected object(s)
                ISelection selection = viewer.getSelection();
                List<cObject> objects = SelectionUtils.getObjects(selection, cObject.class);
                getContainer().inspect(objects, true);
            }
        });

        // export selection
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent e) {
                // wrap to SelectionItems and export to the inspector canvas
                Object[] array = ((IStructuredSelection)e.getSelection()).toArray();
                for (int i = 0; i < array.length; i++)
                    array[i] = new SelectionItem(ObjectFieldsInspectorPart.this, array[i]);
                getContainer().select(array, true);
            }
        });

        // add context menu
        final MenuManager menuManager = new MenuManager("#PopupMenu");
        viewer.getTree().setMenu(menuManager.createContextMenu(viewer.getTree()));
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                getContainer().populateContextMenu(menuManager, viewer.getSelection());
            }
        });

        frame.layout();

        getContainer().addMoveResizeSupport(frame);
        getContainer().addMoveResizeSupport(label);

        return frame;
    }

    @Override
    public boolean isMaximizable() {
        return false;
    }

    public Mode getMode() {
        return viewer.getMode();
    }

    public void setMode(Mode mode) {
        viewer.setMode(mode);
        getContainer().updateFloatingToolbarActions();
    }

    public Ordering getOrdering() {
        return viewer.getOrdering();
    }

    public void setOrdering(Ordering ordering) {
        viewer.setOrdering(ordering);
        getContainer().updateFloatingToolbarActions();
    }

    @Override
    public void populateContextMenu(MenuManager contextMenuManager, Point p) {
    }

    @Override
    public void populateFloatingToolbar(ToolBarManager manager) {
        manager.add(my(new InspectParentAction()));
        manager.add(new Separator());
        manager.add(my(new SortAction()));
        manager.add(new Separator());
        manager.add(my(new SetModeAction(Mode.PACKET)));
        manager.add(my(new SetModeAction(Mode.ESSENTIALS)));
        manager.add(my(new SetModeAction(Mode.CHILDREN)));
        manager.add(my(new SetModeAction(Mode.GROUPED)));
        manager.add(my(new SetModeAction(Mode.INHERITANCE)));
        manager.add(my(new SetModeAction(Mode.FLAT)));
    }

    @Override
    public void refresh() {
        super.refresh();

        if (!isDisposed()) {
            String text = "(" + object.getShortTypeName() + ") " + object.getFullPath() + " - " + object.getInfo();
            label.setText(text);
            label.setToolTipText(text); // because label text is usually not fully visible

            viewer.refresh();
        }
    }

    @Override
    public int getDragOperation(Control control, int x, int y) {
        Point size = control.getSize();
        if (control == getSWTControl())
            return FigureUtils.getBorderResizeInsideMoveDragOperation(x, y, new Rectangle(0, 0, size.x, size.y));
        if (control == label)
            return FigureUtils.getMoveOnlyDragOperation(x, y, new Rectangle(0, 0, size.x, size.y));
        return 0;
    }

    protected void setSelectionMark(boolean isSelected) {
        super.setSelectionMark(isSelected);
        label.setBackground(isSelected ? ColorFactory.GREY50 : null);
    }
}
