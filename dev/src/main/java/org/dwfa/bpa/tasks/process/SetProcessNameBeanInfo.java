package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class SetProcessNameBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SetProcessNameBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor newName =
                new PropertyDescriptor("newName", SetProcessName.class);
            newName.setBound(true);
            newName.setPropertyEditorClass(JTextFieldEditor.class);
            newName.setDisplayName("set process name");
            newName.setShortDescription("Sets the name of the process to the provided value.");


            PropertyDescriptor rv[] =
                {newName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetProcessName.class);
        bd.setDisplayName("<html><font color='green'><center>Set Process Name");
        return bd;
    }

}
