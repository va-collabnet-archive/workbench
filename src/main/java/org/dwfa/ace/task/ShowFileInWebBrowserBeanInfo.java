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
			PropertyDescriptor detailHtmlFileNameProp =
			     new PropertyDescriptor("detailHtmlFileNameProp", ShowFileInWebBrowser.class);
			detailHtmlFileNameProp.setBound(true);
			detailHtmlFileNameProp.setDisplayName("<html><font color='green'>Detail Html File:");
			detailHtmlFileNameProp.setShortDescription("html file");
			detailHtmlFileNameProp.setPropertyEditorClass(PropertyNameLabelEditor.class);

			
            PropertyDescriptor rv[] = {detailHtmlFileNameProp};
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
