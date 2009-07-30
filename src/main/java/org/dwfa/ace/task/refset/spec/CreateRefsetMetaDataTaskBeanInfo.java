package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateRefsetMetaDataTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public CreateRefsetMetaDataTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {
            PropertyDescriptor newRefsetPropName;
            newRefsetPropName = new PropertyDescriptor("newRefsetPropName",
                    getBeanDescriptor().getBeanClass());
            newRefsetPropName.setBound(true);
            newRefsetPropName
                    .setPropertyEditorClass(PropertyNameLabelEditor.class);
            newRefsetPropName
                    .setDisplayName("<html><font color='green'>refset Name:");
            newRefsetPropName
                    .setShortDescription("The property to put the refset name into.");
            PropertyDescriptor rv[] = { newRefsetPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateRefsetMetaDataTask.class);
        bd
                .setDisplayName("<html><font color='green'><center>Create refset<br>meta data");
        return bd;
    }

}
