package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class LoadSetLaunchProcessFromFileBeanInfo  extends SimpleBeanInfo {
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(LoadSetLaunchProcessFromFile.class);
           bd.setDisplayName("<html><center><font color='blue'>Load, Set, Launch<br>Process From File");
        return bd;
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor inputFilePropName =
                new PropertyDescriptor("inputFilePropName", getBeanDescriptor().getBeanClass());
            inputFilePropName.setBound(true);
            inputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            inputFilePropName.setDisplayName("<html><font color='blue'>BP File:");
            inputFilePropName.setShortDescription("Name of the property to read the business process file from... ");

            PropertyDescriptor rv[] =
                { inputFilePropName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
}
