package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;

public class IsChildOfSearchInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor parentTerm =
                new PropertyDescriptor("parentTerm", getBeanDescriptor().getBeanClass());
            parentTerm.setBound(true);
            parentTerm.setPropertyEditorClass(ConceptLabelEditor.class);
            parentTerm.setDisplayName("<html><font color='green'>parent:");
            parentTerm.setShortDescription("The concept to test for parentage of.");

            PropertyDescriptor rv[] =
                { parentTerm };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(IsChildOf.class);
        bd.setDisplayName("is child of");
        return bd;
    }
}
