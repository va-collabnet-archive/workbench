package org.dwfa.ace.task.refset.spec.wf;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class HasDisapprovedItemsTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public HasDisapprovedItemsTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {
            PropertyDescriptor promotionUuidPropName =
                    new PropertyDescriptor("promotionUuidPropName", getBeanDescriptor().getBeanClass());
            promotionUuidPropName.setBound(true);
            promotionUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            promotionUuidPropName.setDisplayName("<html><font color='green'>promotion UUID prop:");
            promotionUuidPropName
                .setShortDescription("The property that will contain the UUID of the promotion refset");

            PropertyDescriptor rv[] = { promotionUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(HasDisapprovedItemsTask.class);
        bd.setDisplayName("<html><font color='green'><center>Has members with<br>disapproved promotion<br>status");
        return bd;
    }

}
