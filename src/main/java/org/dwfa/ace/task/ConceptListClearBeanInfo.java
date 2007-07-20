package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ConceptListClearBeanInfo extends SimpleBeanInfo {
    
    public PropertyDescriptor[] getPropertyDescriptors() {
             PropertyDescriptor rv[] = { };
            return rv;
      }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConceptListClear.class);
        bd.setDisplayName("<html><font color='green'><center>Clear Concept List");
        return bd;
    }

}
