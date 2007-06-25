package org.dwfa.ace.task.view;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetViewPositionFromPropertyBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile property:");
            profilePropName.setShortDescription("The property containing the profile to change.");


            PropertyDescriptor positionStr =
                new PropertyDescriptor("positionStr", getBeanDescriptor().getBeanClass());
            positionStr.setBound(true);
            positionStr.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            positionStr.setDisplayName("<html><font color='green'>position:");
            positionStr.setShortDescription("The version as a string. Expressed as \"latest\" or yyyy-MM-dd HH:mm:ss.");

            PropertyDescriptor viewPathConceptPropName =
                new PropertyDescriptor("viewPathConceptPropName", getBeanDescriptor().getBeanClass());
            viewPathConceptPropName.setBound(true);
            viewPathConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            viewPathConceptPropName.setDisplayName("<html><font color='green'>view path:");
            viewPathConceptPropName.setShortDescription("The property that contains the concept that identifies the view path.");

            PropertyDescriptor rv[] =
                { profilePropName, positionStr, viewPathConceptPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetViewPositionFromProperty.class);
        bd.setDisplayName("<html><font color='green'><center>set view position<br>from property");
        return bd;
    }
}
