package org.dwfa.ace.task.wfpanel;

import java.beans.BeanDescriptor;

public class SetAdminUserAndPwdPNCBeanInfo extends PreviousNextOrCancelBeanInfo {

	   /**
  * @see java.beans.BeanInfo#getBeanDescriptor()
  */
 public BeanDescriptor getBeanDescriptor() {
     BeanDescriptor bd = new BeanDescriptor(SetAdminUserAndPwdPNC.class);
     bd.setDisplayName("<html><font color='green'><center>Set Administrative<br>User and Pwd<br>Prev, Next, or Cancel");
     return bd;
 }

}
