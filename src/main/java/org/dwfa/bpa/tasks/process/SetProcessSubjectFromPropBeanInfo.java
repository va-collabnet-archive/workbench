package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetProcessSubjectFromPropBeanInfo  extends SimpleBeanInfo {

    public SetProcessSubjectFromPropBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            
            PropertyDescriptor newSubjectProp =
                new PropertyDescriptor("newSubjectProp", SetProcessSubjectFromProp.class);
            newSubjectProp.setBound(true);
            newSubjectProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newSubjectProp.setDisplayName("<html><font color='green'>new subject prop:");
            newSubjectProp.setShortDescription("Name of the property containing the new subject for the process. ");
            

            PropertyDescriptor rv[] = { newSubjectProp };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(SetProcessSubjectFromProp.class);
           bd.setDisplayName("<html><font color='blue'>Set Subject<br>From Property");
        return bd;
    }

}
