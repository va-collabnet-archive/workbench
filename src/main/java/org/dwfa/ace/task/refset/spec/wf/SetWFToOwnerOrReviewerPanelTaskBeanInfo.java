package org.dwfa.ace.task.refset.spec.wf;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetWFToOwnerOrReviewerPanelTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public SetWFToOwnerOrReviewerPanelTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor reviewerUuidPropName;
            reviewerUuidPropName = new PropertyDescriptor("reviewerUuidPropName", getBeanDescriptor().getBeanClass());
            reviewerUuidPropName.setBound(true);
            reviewerUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            reviewerUuidPropName.setDisplayName("<html><font color='green'>reviewer prop name:");
            reviewerUuidPropName.setShortDescription("The property to put the reviewer uuid  into.");

            PropertyDescriptor ownerUuidPropName;
            ownerUuidPropName = new PropertyDescriptor("ownerUuidPropName", getBeanDescriptor().getBeanClass());
            ownerUuidPropName.setBound(true);
            ownerUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            ownerUuidPropName.setDisplayName("<html><font color='green'>owner uuid prop name:");
            ownerUuidPropName.setShortDescription("The property to put the owner uuid into.");

            PropertyDescriptor rv[] = { reviewerUuidPropName, ownerUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetWFToOwnerOrReviewerPanelTask.class);
        bd.setDisplayName("<html><font color='green'><center>Set WF Panel to<br>select owner or<br>reviewer panel");
        return bd;
    }

}
