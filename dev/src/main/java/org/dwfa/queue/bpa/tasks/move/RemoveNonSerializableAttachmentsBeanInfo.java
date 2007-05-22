package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class RemoveNonSerializableAttachmentsBeanInfo extends SimpleBeanInfo {

	/**
	 * 
	 */
	public RemoveNonSerializableAttachmentsBeanInfo() {
		super();
	}
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RemoveNonSerializableAttachments.class);
        bd.setDisplayName("<html><font color='red'>Remove Nonserializable<br>Attachments");
        return bd;
    }

}
