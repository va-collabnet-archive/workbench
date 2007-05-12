package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.IncrementEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetConceptViewToConceptBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SetConceptViewToConceptBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor propName =
                new PropertyDescriptor("propName", SetConceptViewToConcept.class);
            propName.setBound(true);
            propName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            propName.setDisplayName("<html><font color='green'>Concept property:");
            propName.setShortDescription("Name of the property containing the concept. ");
            
            PropertyDescriptor hostIndex =
                new PropertyDescriptor("hostIndex", SetConceptViewToConcept.class);
            hostIndex.setBound(true);
            hostIndex.setPropertyEditorClass(IncrementEditor.class);
            hostIndex.setDisplayName("<html><font color='green'>Concept tab:");
            hostIndex.setShortDescription("Index of the concept tab to put the concept into. ");
            
            PropertyDescriptor rv[] = { propName, hostIndex };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetConceptViewToConcept.class);
        bd.setDisplayName("<html><font color='green'><center>Set Concept View<br>to Concept");
        return bd;
    }

}