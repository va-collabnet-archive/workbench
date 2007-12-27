package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AbstractSearchTestBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile.");

            PropertyDescriptor componentPropName =
                new PropertyDescriptor("componentPropName", getBeanDescriptor().getBeanClass());
            componentPropName.setBound(true);
            componentPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            componentPropName.setDisplayName("<html><font color='green'>component prop:");
            componentPropName.setShortDescription("The property that contains the component to test.");

            PropertyDescriptor rv[] =
                { componentPropName, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AbstractSearchTest.class);
        bd.setDisplayName("<html><font color='green'><center>Test Component<br>for Search");
        return bd;
    }
}
