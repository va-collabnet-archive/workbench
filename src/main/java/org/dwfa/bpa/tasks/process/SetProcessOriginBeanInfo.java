package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class SetProcessOriginBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SetProcessOriginBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor newOrigin =
                new PropertyDescriptor("newOrigin", SetProcessOrigin.class);
            newOrigin.setBound(true);
            newOrigin.setPropertyEditorClass(JTextFieldEditor.class);
            newOrigin.setDisplayName("set process origin");
            newOrigin.setShortDescription("Sets the origin of the process to the provided value.");


            PropertyDescriptor rv[] =
                {newOrigin};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetProcessOrigin.class);
        bd.setDisplayName("<html><font color='green'><center>Set Process Origin");
        return bd;
    }

}
