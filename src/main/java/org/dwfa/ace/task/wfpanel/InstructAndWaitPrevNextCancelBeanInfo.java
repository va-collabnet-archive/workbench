package org.dwfa.ace.task.wfpanel;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class InstructAndWaitPrevNextCancelBeanInfo extends SimpleBeanInfo {
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor instruction = new PropertyDescriptor(
					"instruction", getBeanDescriptor().getBeanClass());
			instruction.setBound(true);
			instruction.setPropertyEditorClass(JTextFieldEditor.class);
			instruction
					.setDisplayName("<html><font color='green'>Instruction:");
			instruction
					.setShortDescription("Instructions to present to the user in the workflow panel. ");

			PropertyDescriptor rv[] = { instruction };
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
	}

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(
				InstructAndWaitPrevNextCancel.class);
		bd.setDisplayName("<html><font color='green'><center>Instruct and Wait<br>prev,next,cancel");
		return bd;
	}
}
