package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetProcessDestinationFromPropBeanInfo extends SimpleBeanInfo {

    public SetProcessDestinationFromPropBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            
            PropertyDescriptor destinationProp =
                new PropertyDescriptor("newDestinationProperty", SetProcessDestinationFromProp.class);
            destinationProp.setBound(true);
            destinationProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
            destinationProp.setDisplayName("<html><font color='green'>destination prop:");
            destinationProp.setShortDescription("Name of the property containing the new destination. ");
            

            PropertyDescriptor rv[] = { destinationProp };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(SetProcessDestinationFromProp.class);
           bd.setDisplayName("<html><font color='blue'>Set Destination<br>From Property");
        return bd;
    }

}
