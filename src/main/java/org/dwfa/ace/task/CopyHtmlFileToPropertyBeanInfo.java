package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info for CopyHtmlFileToProperty class.
 * @author Susan Castillo
 *
 */
public class CopyHtmlFileToPropertyBeanInfo extends SimpleBeanInfo {

    public CopyHtmlFileToPropertyBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
              try {
				PropertyDescriptor detailHtmlFileNameProp =
				     new PropertyDescriptor("detailHtmlFileNameProp", CopyHtmlFileToProperty.class);
				detailHtmlFileNameProp.setBound(true);
				detailHtmlFileNameProp.setDisplayName("<html><font color='green'>Html File Name:");
				detailHtmlFileNameProp.setShortDescription("File Name");
				detailHtmlFileNameProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
				
				PropertyDescriptor htmlDataPropName =
				     new PropertyDescriptor("htmlDataPropName", CopyHtmlFileToProperty.class);
				htmlDataPropName.setBound(true);
				htmlDataPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
				htmlDataPropName.setDisplayName("<html><font color='green'>Html data key:");
				htmlDataPropName.setShortDescription("Html data key.");

				
	            PropertyDescriptor rv[] = {detailHtmlFileNameProp,  htmlDataPropName};
	            return rv;
			} catch (IntrospectionException e) {
				throw new Error(e.toString());
			}
      }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CopyHtmlFileToProperty.class);
        bd.setDisplayName("<html><font color='green'><center>Copy Html File<br>to Property");
        return bd;
    }
}
