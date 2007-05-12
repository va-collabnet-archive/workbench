package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class AddChildrenToListBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public AddChildrenToListBeanInfo() {
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
        BeanDescriptor bd = new BeanDescriptor(ConceptListEmpty.class);
        bd.setDisplayName("<html><font color='green'><center>Add Children<br>to List");
        return bd;
    }

}
