package org.dwfa.ace.task.commit;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class TestForIsaBeanInfo extends TestForUneditedDefaultsBeanInfo {

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(TestForIsa.class);
		bd.setDisplayName("<html><font color='green'><center>Test For<br>IS_A");
		return bd;
	}

}
