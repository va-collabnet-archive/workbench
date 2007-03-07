package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class SetProcessSubjectBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SetProcessSubjectBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor newSubject =
                new PropertyDescriptor("newSubject", SetProcessSubject.class);
            newSubject.setBound(true);
            newSubject.setPropertyEditorClass(JTextFieldEditor.class);
            newSubject.setDisplayName("set process subject");
            newSubject.setShortDescription("Sets the subject of the process to the provided value.");


            PropertyDescriptor rv[] =
                {newSubject};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetProcessSubject.class);
        bd.setDisplayName("<html><font color='green'><center>Set Process Subject");
        return bd;
    }

}
