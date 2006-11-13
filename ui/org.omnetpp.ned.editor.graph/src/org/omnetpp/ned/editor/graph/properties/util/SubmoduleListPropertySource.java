package org.omnetpp.ned.editor.graph.properties.util;

import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.omnetpp.ned2.model.ex.CompoundModuleNodeEx;
import org.omnetpp.ned2.model.ex.ParamNodeEx;
import org.omnetpp.ned2.model.ex.SubmoduleNodeEx;

/**
 * @author rhornig
 * Property source to display all submodules for a given compound module
 */
public class SubmoduleListPropertySource extends NotifiedPropertySource {
    public final static String CATEGORY = "submodules";
    public final static String DESCRIPTION = "List of submodules (direct and inherited)";
    protected CompoundModuleNodeEx model;
    protected PropertyDescriptor[] pdesc;
    protected int totalSubmoduleCount;
    protected int inheritedSubmoduleCount;
    
    public SubmoduleListPropertySource(CompoundModuleNodeEx model) {
        super(model);
        this.model = model;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<SubmoduleNodeEx> submodules = model.getSubmodules();
        
        pdesc = new PropertyDescriptor[submodules.size()];
        totalSubmoduleCount = inheritedSubmoduleCount = 0;
        for(SubmoduleNodeEx smodule : submodules) {
            String definedIn = "";
            if (smodule.getCompoundModule() != model) {
                inheritedSubmoduleCount++;
                definedIn= " (inherited from "+smodule.getCompoundModule().getName()+")";
            }
            String typeString = "".equals(smodule.getLikeType()) || smodule.getLikeType() == null ? 
                            smodule.getType() : "<"+smodule.getLikeParam()+"> like "+smodule.getLikeType();
            pdesc[totalSubmoduleCount] = new PropertyDescriptor(smodule, typeString);
            pdesc[totalSubmoduleCount].setCategory(CATEGORY);
            pdesc[totalSubmoduleCount].setDescription("Submodule "+smodule.getNameWithIndex()+" with type "+typeString
                                                        +definedIn+" - (read only)");
            totalSubmoduleCount++;
        }
        
        return pdesc;
    }

    @Override
    public Object getEditableValue() {
        // just a little summary - show the number of submodules
        String summary = "";
        // if the property descriptor is not yet build, build it now
        if (pdesc == null) 
            getPropertyDescriptors();
        
        if (pdesc != null )
            summary ="total: "+totalSubmoduleCount+" (inherited: "+inheritedSubmoduleCount+")";
        return summary;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (!(id instanceof SubmoduleNodeEx))
            return getEditableValue();
        
        return ((SubmoduleNodeEx)id).getNameWithIndex();
    }

    @Override
    public boolean isPropertyResettable(Object id) {
        return false;
    }

    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }

    @Override
    public void resetPropertyValue(Object id) {
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
    }

}
