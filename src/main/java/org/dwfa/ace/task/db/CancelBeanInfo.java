package org.dwfa.ace.task.db;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class CancelBeanInfo extends SimpleBeanInfo {

   public PropertyDescriptor[] getPropertyDescriptors() {
      PropertyDescriptor rv[] = {};
      return rv;
   }

   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
      BeanDescriptor bd = new BeanDescriptor(Cancel.class);
      bd.setDisplayName("<html><font color='green'><center>Cancel Changes");
      return bd;
   }
}