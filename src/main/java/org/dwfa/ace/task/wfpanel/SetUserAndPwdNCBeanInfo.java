package org.dwfa.ace.task.wfpanel;

import java.beans.BeanDescriptor;

public class SetUserAndPwdNCBeanInfo extends PreviousNextOrCancelBeanInfo {

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(SetUserAndPwdNC.class);
		bd.setDisplayName("<html><font color='green'><center>Set User and Pwd<br>Next or Cancel");
		return bd;
	}

}
