package org.dwfa.bpa.tasks.transaction;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class AbortWorkflowTransactionBeanInfo extends SimpleBeanInfo {

   public PropertyDescriptor[] getPropertyDescriptors() {
       return new PropertyDescriptor[0];
   }        
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(AbortWorkflowTransaction.class);
       bd.setDisplayName("<html><font color='blue'><center>Abort<br>Workflow Transaction");
       return bd;
   }

}
