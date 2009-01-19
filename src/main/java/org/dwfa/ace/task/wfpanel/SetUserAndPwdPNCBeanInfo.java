package org.dwfa.ace.task.wfpanel;

import java.beans.BeanDescriptor;

public class SetUserAndPwdPNCBeanInfo extends PreviousNextOrCancelBeanInfo {

	   /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetUserAndPwdPNC.class);
        bd.setDisplayName("<html><font color='green'><center>Set User and Pwd<br>Prev, Next, or Cancel");
        return bd;
    }

}
