package org.dwfa.ace.task.wfpanel;

import java.beans.BeanDescriptor;

public class SetAdminUserAndPwdNCBeanInfo extends PreviousNextOrCancelBeanInfo {

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(SetAdminUserAndPwdNC.class);
		bd.setDisplayName("<html><font color='green'><center>Set Administrative<br>User and Pwd<br>Next or Cancel");
		return bd;
	}

}
