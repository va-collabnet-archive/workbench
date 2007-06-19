package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ShowFileInWebBrowserBeanInfo extends SimpleBeanInfo {

	/**
	 * Bean info for ShowFileInWebBrowser class.
	 * @author Susan Castillo
	 *
	 */
    public ShowFileInWebBrowserBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
			PropertyDescriptor reasonForDupHtmlStrPropName =
			     new PropertyDescriptor("reasonForDupHtmlStrPropName", ShowFileInWebBrowser.class);
			reasonForDupHtmlStrPropName.setBound(true);
			reasonForDupHtmlStrPropName.setDisplayName("<html><font color='green'>HTLM Reason for Dup key:");
			reasonForDupHtmlStrPropName.setShortDescription("html data key");
			reasonForDupHtmlStrPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			
			PropertyDescriptor uuidStrPropName =
			     new PropertyDescriptor("uuidStrPropName", ShowFileInWebBrowser.class);
			uuidStrPropName.setBound(true);
			uuidStrPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			uuidStrPropName.setDisplayName("<html><font color='green'>Uuid data key:");
			uuidStrPropName.setShortDescription("Uuid data key.");

			
            PropertyDescriptor rv[] = {reasonForDupHtmlStrPropName,  uuidStrPropName};
            return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowFileInWebBrowser.class);
        bd.setDisplayName("<html><font color='green'><center>Show Attachment<br>In Web Browser");
        return bd;
    }

}
