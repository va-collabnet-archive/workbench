package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class WriteAttachmentToDiskBeanInfo extends SimpleBeanInfo {

	/**
	 * Bean info for WriteAttachmentToDisk class.
	 * @author Susan Castillo
	 *
	 */
    public WriteAttachmentToDiskBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
              try {
				PropertyDescriptor uuidStrPropName =
				     new PropertyDescriptor("uuidStrPropName", WriteAttachmentToDisk.class);
				uuidStrPropName.setBound(true);
				uuidStrPropName.setDisplayName("<html><font color='green'>Uuid key:");
				uuidStrPropName.setShortDescription("Uuid data key");
				uuidStrPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
				
				PropertyDescriptor htmlDataPropName =
				     new PropertyDescriptor("htmlDataPropName", WriteAttachmentToDisk.class);
				htmlDataPropName.setBound(true);
				htmlDataPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
				htmlDataPropName.setDisplayName("<html><font color='green'>html data key:");
				htmlDataPropName.setShortDescription("html data key.");

				
	            PropertyDescriptor rv[] = {uuidStrPropName,  htmlDataPropName};
	            return rv;
			} catch (IntrospectionException e) {
				throw new Error(e.toString());
			}
      }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(WriteAttachmentToDiskBeanInfo.class);
        bd.setDisplayName("<html><font color='green'><center>Write Attachment<br>to Disk");
        return bd;
    }
}
