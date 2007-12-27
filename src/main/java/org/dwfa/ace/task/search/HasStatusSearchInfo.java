package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

public class HasStatusSearchInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor statusTerm =
                new PropertyDescriptor("statusTerm", getBeanDescriptor().getBeanClass());
            statusTerm.setBound(true);
            statusTerm.setPropertyEditorClass(QueueTypeEditor.class);
            statusTerm.setDisplayName("<html><font color='green'>status kind:");
            statusTerm.setShortDescription("The concept to test for status is kind of.");

            PropertyDescriptor rv[] =
                { statusTerm };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(HasStatus.class);
        bd.setDisplayName("status kind");
        return bd;
    }
}
