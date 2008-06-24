package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class SetProcessDestinationBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SetProcessDestinationBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor newDestination =
                new PropertyDescriptor("newDestination", SetProcessDestination.class);
            newDestination.setBound(true);
            newDestination.setPropertyEditorClass(JTextFieldEditor.class);
            newDestination.setDisplayName("set destination");
            newDestination.setShortDescription("Sets the destination of the process to the provided value.");


            PropertyDescriptor rv[] = { newDestination };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetProcessDestination.class);
        bd.setDisplayName("<html><font color='green'><center>Set Destination");
        return bd;
    }

}
