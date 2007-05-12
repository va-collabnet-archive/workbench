package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CopyFirstItemInListBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public CopyFirstItemInListBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor propName =
                new PropertyDescriptor("propName", CopyFirstItemInList.class);
            propName.setBound(true);
            propName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            propName.setDisplayName("<html><font color='green'>List property:");
            propName.setShortDescription("Name of the property to copy the concept into. ");
            
            PropertyDescriptor rv[] = { propName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CopyFirstItemInList.class);
        bd.setDisplayName("<html><font color='green'><center>Copy First Item<br>In List");
        return bd;
    }

}
