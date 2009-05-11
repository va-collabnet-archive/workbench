package org.dwfa.ace.task.refset.members;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public final class WriteRefsetDescriptionsBeanInfo extends SimpleBeanInfo {

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
              try {
                PropertyDescriptor filePropName =
                     new PropertyDescriptor("filePropertyName", WriteRefsetDescriptions.class);
                filePropName.setBound(true);
                filePropName.setDisplayName("<html><font color='green'>Output directory key:");
                filePropName.setShortDescription("Output directory key");
                filePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
                PropertyDescriptor rv[] = {filePropName};
                return rv;
            } catch (IntrospectionException e) {
                throw new Error(e.toString());
            }
      }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(WriteRefsetDescriptions.class);
        bd.setDisplayName("<html><font color='green'><center>Export Refsets<br>to Disk");
        return bd;
    }
}
