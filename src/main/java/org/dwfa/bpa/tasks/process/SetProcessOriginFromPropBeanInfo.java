package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetProcessOriginFromPropBeanInfo extends SimpleBeanInfo {

    public SetProcessOriginFromPropBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            
            PropertyDescriptor newOriginProp =
                new PropertyDescriptor("newOriginProp", SetProcessOriginFromProp.class);
            newOriginProp.setBound(true);
            newOriginProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newOriginProp.setDisplayName("<html><font color='green'>new origin prop:");
            newOriginProp.setShortDescription("Name of the property containing the new origin for the process. ");
            

            PropertyDescriptor rv[] = { newOriginProp };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(SetProcessOriginFromProp.class);
           bd.setDisplayName("<html><font color='blue'>Set Origin<br>From Property");
        return bd;
    }

}
