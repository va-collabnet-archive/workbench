package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

public abstract class AbstractAddRefsetSpecTaskBeanInfo  extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor clauseIsTrue =
                new PropertyDescriptor("clauseIsTrue", getBeanDescriptor().getBeanClass());
            clauseIsTrue.setBound(true);
            clauseIsTrue.setPropertyEditorClass(CheckboxEditor.class);
            clauseIsTrue.setDisplayName("<html><font color='green'>true:");
            clauseIsTrue.setShortDescription("If checked, the clause must be true. If not checked, the clause must be false.");

            PropertyDescriptor rv[] =
                { clauseIsTrue };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public abstract BeanDescriptor getBeanDescriptor();
    
    
}
