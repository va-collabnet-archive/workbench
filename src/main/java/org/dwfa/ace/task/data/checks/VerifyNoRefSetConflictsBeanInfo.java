package org.dwfa.ace.task.data.checks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class VerifyNoRefSetConflictsBeanInfo extends SimpleBeanInfo{
	public VerifyNoRefSetConflictsBeanInfo(){
		super();
	}
	
	public PropertyDescriptor[] getPropertyDescriptors() {

            PropertyDescriptor rv[] = {};
            return rv;
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(VerifyNoRefSetConflicts.class);
        bd.setDisplayName("<html><font color='green'><center>Verify refset spec<br> has no conflicts");
        return bd;
    }
	
}//End class VerifyNoRefSetConflictsBeanInfo