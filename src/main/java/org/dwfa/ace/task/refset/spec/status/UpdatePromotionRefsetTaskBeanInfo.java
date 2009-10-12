package org.dwfa.ace.task.refset.spec.status;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class UpdatePromotionRefsetTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public UpdatePromotionRefsetTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor refsetSpecUuidPropName;
            refsetSpecUuidPropName =
                    new PropertyDescriptor("refsetSpecUuidPropName", getBeanDescriptor().getBeanClass());
            refsetSpecUuidPropName.setBound(true);
            refsetSpecUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetSpecUuidPropName.setDisplayName("<html><font color='green'>Refset spec UUID prop:");
            refsetSpecUuidPropName.setShortDescription("The refset spec UUID prop.");

            PropertyDescriptor rv[] = { refsetSpecUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(UpdatePromotionRefsetTask.class);
        bd.setDisplayName("<html><font color='green'><center>Update promotion refset");
        return bd;
    }

}
