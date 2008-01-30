package org.dwfa.ace.task.ebr;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;

public class CountExtensionsInRefsetBeanInfo extends SimpleBeanInfo {

 
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor refsetTermEntry =
                new PropertyDescriptor("refsetTermEntry", getBeanDescriptor().getBeanClass());
            refsetTermEntry.setBound(true);
            refsetTermEntry.setPropertyEditorClass(ConceptLabelEditor.class);
            refsetTermEntry.setDisplayName("RefSet:");
            refsetTermEntry.setShortDescription("The identity concept for the refset of interest.");

            PropertyDescriptor rv[] =
                { refsetTermEntry };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CountExtensionsInRefset.class);
        bd.setDisplayName("<html><font color='green'><center>Count Extensions<br>in Refset");
        return bd;
    }
}
