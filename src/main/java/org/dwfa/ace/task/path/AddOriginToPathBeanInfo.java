package org.dwfa.ace.task.path;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddOriginToPathBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor pathConceptPropName = new PropertyDescriptor("pathConceptPropName", getBeanDescriptor().getBeanClass());
            pathConceptPropName.setBound(true);
            pathConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            pathConceptPropName.setDisplayName("<html><font color='green'>path concept property:");
            pathConceptPropName.setShortDescription("The path to be modified with an additional origin");

            PropertyDescriptor originPathConceptPropName = new PropertyDescriptor("originPathConceptPropName", getBeanDescriptor().getBeanClass());
            originPathConceptPropName.setBound(true);
            originPathConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            originPathConceptPropName.setDisplayName("<html><font color='green'>origin path concept property:");
            originPathConceptPropName.setShortDescription("The path to be added as an origin");            
            
            PropertyDescriptor originPositionStr = new PropertyDescriptor("originPositionStr", getBeanDescriptor().getBeanClass());
            originPositionStr.setBound(true);
            originPositionStr.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            originPositionStr.setDisplayName("<html><font color='green'>origin position:");
            originPositionStr.setShortDescription("The version as a string. Expressed as \"latest\" or yyyy-MM-dd HH:mm:ss.");            
            
            PropertyDescriptor rv[] = { pathConceptPropName, originPathConceptPropName, originPositionStr };
            return rv;
            
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddOriginToPath.class);
        bd.setDisplayName("<html><font color='green'><center>add origin to path");
        return bd;
    }
}
