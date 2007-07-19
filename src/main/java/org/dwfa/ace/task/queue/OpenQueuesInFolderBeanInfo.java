package org.dwfa.ace.task.queue;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;

public class OpenQueuesInFolderBeanInfo extends SimpleBeanInfo {
	   public PropertyDescriptor[] getPropertyDescriptors() {
	        try {

	            PropertyDescriptor queueDir =
	                new PropertyDescriptor("queueDir", getBeanDescriptor().getBeanClass());
	            queueDir.setBound(true);
	            queueDir.setPropertyEditorClass(JTextFieldEditorOneLine.class);
	            queueDir.setDisplayName("<html><font color='green'>queue dir:");
	            queueDir.setShortDescription("The directory containing the queues to open (searched recursively).");

	            PropertyDescriptor rv[] = { queueDir };
	            return rv;
	        } catch (IntrospectionException e) {
	             throw new Error(e.toString());
	        }
	    }

/**
 * @see java.beans.BeanInfo#getBeanDescriptor()
 */
public BeanDescriptor getBeanDescriptor() {
    BeanDescriptor bd = new BeanDescriptor(OpenQueuesInFolder.class);
    bd.setDisplayName("<html><font color='green'><center>Open Queues in Folder");
    return bd;
}

}
