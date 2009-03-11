package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetProcessNameFromPropBeanInfo extends SimpleBeanInfo {

    public SetProcessNameFromPropBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            
            PropertyDescriptor newNameProp =
                new PropertyDescriptor("newNameProp", SetProcessNameFromProp.class);
            newNameProp.setBound(true);
            newNameProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newNameProp.setDisplayName("<html><font color='green'>new name prop:");
            newNameProp.setShortDescription("Name of the property containing the new process name. ");
            

            PropertyDescriptor rv[] = { newNameProp };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(SetProcessNameFromProp.class);
           bd.setDisplayName("<html><font color='blue'>Set Name<br>From Property");
        return bd;
    }

}
