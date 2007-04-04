package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class NewConceptBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public NewConceptBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
             PropertyDescriptor rv[] = { };
            return rv;
      }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(NewConcept.class);
        bd.setDisplayName("<html><font color='green'><center>Create New Concept");
        return bd;
    }

}
