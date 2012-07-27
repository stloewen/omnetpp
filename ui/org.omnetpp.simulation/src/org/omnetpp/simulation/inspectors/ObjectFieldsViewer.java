package org.omnetpp.simulation.inspectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.omnetpp.common.color.ColorFactory;
import org.omnetpp.simulation.SimulationPlugin;
import org.omnetpp.simulation.SimulationUIConstants;
import org.omnetpp.simulation.model.Field;
import org.omnetpp.simulation.model.cObject;
import org.omnetpp.simulation.model.cPacket;

/**
 * Based on TreeViewer, it can display fields of an object in various ways.
 * Can be used in Property View and in inspectors.
 *
 * @author Andras
 */
//TODO implement ordering and filtering of fields
//TODO Field, FieldArrayElement should be adaptable to cObject? that should make it easier to implement inspect-on-doubleclick
public class ObjectFieldsViewer {
    private static final Image IMG_FIELD = SimulationPlugin.getCachedImage("icons/obj16/field.png");
    private static final List<String> CPACKET_BASE_CLASSES = Arrays.asList(new String[]{ "cObject", "cNamedObject", "cOwnedObject", "cMessage", "cPacket" });

    public enum Mode { PACKET, CHILDREN, GROUPED, INHERITANCE, FLAT };
    public enum Ordering { NATURAL, ALPHABETICAL };

    private TreeViewer treeViewer;
    private Mode mode = Mode.GROUPED;
    private Ordering ordering = Ordering.NATURAL;
    private boolean reverseOrder = false;


    /**
     * Represents an intermediate node in the tree
     */
    protected static class FieldArrayElement implements IAdaptable {
        Field field;
        int index;

        public FieldArrayElement(Field field, int index) {
            this.field = field;
            this.index = index;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            // being able to adapt to cObject helps working with the selection
            Object value = field.values[index];
            if (adapter.isInstance(value))
                return value;
            if (adapter.isInstance(this))
                return this;
            return null;
        }

        @Override
        public String toString() {
            return field.toString() + ":[" + index + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime * field.hashCode() + index;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FieldArrayElement other = (FieldArrayElement) obj;
            return (field == other.field || field.equals(other.field)) && index == other.index;
        }

    }

    /**
     * Represents an intermediate node in the tree
     */
    protected static class FieldGroup {
        Object parent;
        String groupName;
        Field[] fields;

        public FieldGroup(Object parent, String groupName, Field[] fields) {
            this.parent = parent;
            this.groupName = groupName;
            this.fields = fields;
        }

        @Override
        public String toString() {
            return parent.toString() + "/" + groupName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = Arrays.hashCode(fields);
            result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
            result = prime * result + ((parent == null) ? 0 : parent.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FieldGroup other = (FieldGroup) obj;
            if (groupName == null && other.groupName != null)
                return false;
            else if (!groupName.equals(other.groupName))
                return false;
            if (parent == null && other.parent != null)
                return false;
            else if (!parent.equals(other.parent))
                return false;
            if (!Arrays.equals(fields, other.fields))
                return false;
            return true;
        }
    }

    protected class TreeContentProvider implements ITreeContentProvider {
        public Object[] getChildren(Object element) {
            // Note: cObject only occurs here as root, at other places it's
            // always a field value somewhere, and as such, is wrapped into
            // Field or FieldArrayElement.
            if (element instanceof cObject) {
                // resolve object to its fields
                cObject object = (cObject)element;
                if (!object.isFilledIn())
                    object.safeLoad(); //FIXME return if failed!
                if (!object.isFieldsFilledIn())
                    object.safeLoadFields(); //FIXME return if failed!
                return groupFieldsOf(object);
            }
            else if (element instanceof FieldGroup) {
                // resolve field group to the fields it contains
                return ((FieldGroup) element).fields;
            }
            else if (element instanceof Field && ((Field)element).isArray) {
                // resolve array field to individual array elements
                Field field = (Field)element;
                FieldArrayElement[] result = new FieldArrayElement[field.values.length];
                for (int i = 0; i < result.length; i++)
                    result[i] = new FieldArrayElement(field, i);
                if (field.isCObject) {
                    for (Object value : field.values) {  //TODO replace loop with controller bulk load operation!!!
                        if (value != null) {
                            ((cObject)value).safeLoad();
                            ((cObject)value).safeLoadFields();
                        }
                    }
                }
                return result;
            }
            else if (element instanceof Field || element instanceof FieldArrayElement) {
                // resolve children of a field value
                boolean isArrayElement = element instanceof FieldArrayElement;
                Field field = !isArrayElement ? (Field)element : ((FieldArrayElement)element).field;
                Object value = !isArrayElement ? field.value : field.values[((FieldArrayElement)element).index];

                if (value instanceof cObject) {
                    // treat like cObject: resolve object to its fields
                    cObject object = (cObject)value;
                    if (!object.isFilledIn())
                        object.safeLoad(); //FIXME return if failed!
                    if (!object.isFieldsFilledIn())
                        object.safeLoadFields(); //FIXME return if failed!
                    return groupFieldsOf(object);
                }

                //TODO if struct, return its fields, etc
            }
            return new Object[0];
        }

        protected Object[] groupFieldsOf(cObject object) {
            //TODO observe ordering, possibly filtering!
            if (mode == Mode.CHILDREN) {
                cObject[] children = object.getChildObjects();
                try {
                    object.getController().loadUnfilledObjects(children);
                }
                catch (IOException e) {
                    e.printStackTrace();  //TODO how to handle exceptions properly...
                }
                return children;
            }
            else if (mode == Mode.FLAT) {
                return object.getFields();
            }
            else if (mode == Mode.GROUPED) {
                Field[] fields = object.getFields();
                Map<String,List<Field>> groups = new LinkedHashMap<String, List<Field>>();
                for (Field f : fields) {
                    if (!groups.containsKey(f.groupProperty))
                        groups.put(f.groupProperty, new ArrayList<Field>());
                    groups.get(f.groupProperty).add(f);
                }
                List<Object> result = new ArrayList<Object>(groups.size());
                if (groups.containsKey(null))
                    result.addAll(groups.get(null));
                for (String groupName : groups.keySet())
                    if (groupName != null)
                        result.add(new FieldGroup(object, groupName, groups.get(groupName).toArray(new Field[]{})));
                return result.toArray();
            }
            else if (mode == Mode.INHERITANCE) {
                //XXX this is a copypasta of the above, only groupProperty is replaced by declaredOn
                Field[] fields = object.getFields();
                Map<String,List<Field>> groups = new LinkedHashMap<String, List<Field>>();
                for (Field f : fields) {
                    if (!groups.containsKey(f.declaredOn))
                        groups.put(f.declaredOn, new ArrayList<Field>());
                    groups.get(f.declaredOn).add(f);
                }
                List<Object> result = new ArrayList<Object>(groups.size());
                if (groups.containsKey(""))
                    result.addAll(groups.get(""));
                for (String groupName : groups.keySet())
                    if (!groupName.equals(""))
                        result.add(new FieldGroup(object, groupName, groups.get(groupName).toArray(new Field[]{})));
                return result.toArray();
            }
            else if (mode == Mode.PACKET) {
                List<Object> result = new ArrayList<Object>();
                cObject pk = object;
                while (pk instanceof cPacket) {
                    if (!pk.isFilledIn())
                        pk.safeLoad();
                    if (!pk.isFieldsFilledIn())
                        pk.safeLoadFields();

                    // add its non-builtin fields
                    List<Field> fields = new ArrayList<Field>();
                    for (Field f : pk.getFields())
                        if (!CPACKET_BASE_CLASSES.contains(f.declaredOn))
                            fields.add(f);

                     // and form a new field group from it
                    result.add(new FieldGroup(object, pk.getClassName(), fields.toArray(new Field[]{})));

                    // go down to the encapsulated packet
                    pk = ((cPacket)pk).getEncapsulatedPacket();  //TODO also follow segments[] and suchlike!!!

                }
                return result.toArray();
            }
            return null;
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);  // leave out toplevel element itself (the inspected object) from the tree
        }

        public Object getParent(Object element) {
            if (element instanceof Field)
                return ((Field) element).owner;
            else if (element instanceof FieldArrayElement)
                return ((FieldArrayElement) element).field;
            else if (element instanceof FieldGroup)
                return ((FieldGroup) element).parent;
            else // note: cObject can only occur in this tree as root, so we must ignore cObject.getOwner() here!
                return null;
        }

        public boolean hasChildren(Object element) {
            // complicated, so reduce it to getChildren()
            return getChildren(element).length > 0;
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Do nothing
        }

        public void dispose() {
            // Do nothing
        }
    }

    protected class TreeLabelProvider implements IStyledLabelProvider {

        private class ColorStyler extends Styler {
            Color color;
            public ColorStyler(Color color) { this.color = color; }
            @Override public void applyStyles(TextStyle textStyle) { textStyle.foreground = color; }
        };

        private Styler blueStyle = new ColorStyler(ColorFactory.BLUE4);
        private Styler greyStyle = new ColorStyler(ColorFactory.GREY60);
        private Styler brownStyle = new ColorStyler(ColorFactory.BURLYWOOD4);

        @Override
        public StyledString getStyledText(Object element) {
            StyledString result = new StyledString();
            if (element instanceof cObject) {
                cObject object = (cObject) element;
                result.append(object.getFullName());
                result.append("(" + object.getClassName() + ")", greyStyle);
                return result;
            }
            else if (element instanceof Field || element instanceof FieldArrayElement) {
                boolean isArrayElement = element instanceof FieldArrayElement;
                Field field = !isArrayElement ? (Field)element : ((FieldArrayElement)element).field;
                Object value = !isArrayElement ? field.value : field.values[((FieldArrayElement)element).index];

                // the @label property can be used to override the field name
                String name = field.labelProperty!=null ? field.labelProperty : field.name;

                // if it's an unexpanded array, return "name[size]" immediately
                if (field.isArray && !isArrayElement) {
                    result.append(name + "[" + field.values.length + "]");
                    result.append(" (" + field.type + ")", greyStyle);
                    return result;
                }

                // when showing array elements, omit name and just show "[index]" instead
                if (isArrayElement)
                    name = "[" + ((FieldArrayElement)element).index + "]";

                // we'll want to print the field type, except for expanded array elements
                // (no need to repeat it, as it's printed in the "name[size]" node already)
                String typeNameText = isArrayElement ? "" : " (" + field.type + ")";

                if (field.isCompound) {
                    // if it's an object, try to say something about it...
                    if (field.isCObject) {
                        result.append(name + " = ");
                        if (value == null)
                            result.append("NULL", blueStyle);
                        else {
                            cObject valueObject = (cObject) value;
                            if (!valueObject.isFilledIn()) //XXX better
                                valueObject.safeLoad();
                            if (!valueObject.isFieldsFilledIn())
                                valueObject.safeLoadFields();

                            result.append("(" + valueObject.getClassName() + ") " + valueObject.getFullName(), blueStyle);
                            String infoTxt = valueObject.getInfo();
                            if (!infoTxt.equals(""))
                                result.append(": " + infoTxt, brownStyle);
                        }
                        result.append(typeNameText, greyStyle);
                        return result;
                    }
                    else {
                        // a value can be generated via operator<<
                        result.append(name);
                        if (!value.toString().equals("")) {
                            result.append(" = ");
                            result.append(value.toString(), blueStyle);
                        }
                        result.append(typeNameText, greyStyle);
                        return result;
                    }
                }
                else {
                    // plain field, return "name = value" text
                    if (field.enumProperty != null) {
                        typeNameText += " - enum " + field.enumProperty;
                        value = field.valueSymbolicName + " (" + value.toString() + ")";
                    }
                    if (field.type.equals("string"))
                        value = "\"" + value.toString() + "\"";

                    result.append(name);
                    if (!value.toString().equals("")) {
                        result.append(" = ");
                        result.append(value.toString(), blueStyle);
                    }
                    result.append(typeNameText, greyStyle);
                    return result;
                }
            }
            else if (element instanceof FieldGroup) {
                FieldGroup group = (FieldGroup) element;
                return result.append(group.groupName);
            }
            else {
                return result.append(element.toString());
            }
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof FieldGroup)
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

            Object value = element;
            if (element instanceof Field)
                value = ((Field) element).value;
            if (element instanceof FieldArrayElement) {
                FieldArrayElement fieldArrayElement = (FieldArrayElement)element;
                value = fieldArrayElement.field.values[fieldArrayElement.index];
            }
            if (value instanceof cObject) {
                cObject object = (cObject)value;
                if (!object.isFilledIn()) //XXX better
                    object.safeLoad();
                if (!object.isFieldsFilledIn())
                    object.safeLoadFields();
                String icon = object.getIcon(); // note: may be empty
                return SimulationPlugin.getCachedImage(SimulationUIConstants.IMG_OBJ_DIR + icon + ".png", SimulationUIConstants.IMG_OBJ_COGWHEEL);
            }

            return IMG_FIELD;
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

    }

    /**
     * Constructor.
     */
    public ObjectFieldsViewer(Composite parent, int style) {
        treeViewer = new TreeViewer(parent, style);
        treeViewer.setLabelProvider(new DecoratingStyledCellLabelProvider(new TreeLabelProvider(), null, null));
        treeViewer.setContentProvider(new TreeContentProvider());
    }

    public void setInput(cObject object) {
        treeViewer.setInput(object);
        refresh();
    }

    public TreeViewer getTreeViewer() {
        return treeViewer;
    }

    public Tree getTree() {
        return getTreeViewer().getTree();
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        refresh();
    }

    public Ordering getOrdering() {
        return ordering;
    }

    public void setOrdering(Ordering ordering) {
        this.ordering = ordering;
        refresh();
    }

    public boolean isReverseOrder() {
        return reverseOrder;
    }

    public void setReverseOrder(boolean reverseOrder) {
        this.reverseOrder = reverseOrder;
        refresh();
    }

    public void refresh() {
        treeViewer.refresh();
    }


}
