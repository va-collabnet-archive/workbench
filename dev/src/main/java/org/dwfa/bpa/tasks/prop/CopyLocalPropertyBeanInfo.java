package org.dwfa.bpa.tasks.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CopyLocalPropertyBeanInfo extends SimpleBeanInfo {

    public CopyLocalPropertyBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor localPropName =
                new PropertyDescriptor("localPropName", SetLocalProperty.class);
            localPropName.setBound(true);
            localPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            localPropName.setDisplayName("<html><font color='green'>Original property:");
            localPropName.setShortDescription("Name of the property to copy from. ");
            
            PropertyDescriptor remotePropName =
                new PropertyDescriptor("remotePropName", SetLocalProperty.class);
            remotePropName.setBound(true);
            remotePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            remotePropName.setDisplayName("<html><font color='blue'>Copy property:");
            remotePropName.setShortDescription("Name of the property to copy to. ");

            PropertyDescriptor rv[] = { remotePropName, localPropName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(SetLocalProperty.class);
           bd.setDisplayName("<html>Copy Property");
        return bd;
    }

}
