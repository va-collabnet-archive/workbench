package org.dwfa.ace.task.refset.spec.wf;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetWFToSelectPromotionListPanelTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public SetWFToSelectPromotionListPanelTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor memberRefsetUuidPropName;
            memberRefsetUuidPropName =
                    new PropertyDescriptor("memberRefsetUuidPropName", getBeanDescriptor().getBeanClass());
            memberRefsetUuidPropName.setBound(true);
            memberRefsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            memberRefsetUuidPropName.setDisplayName("<html><font color='green'>Member refset UUID prop:");
            memberRefsetUuidPropName.setShortDescription("The member refset UUID prop.");

            PropertyDescriptor rv[] = { memberRefsetUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetWFToSelectPromotionListPanelTask.class);
        bd.setDisplayName("<html><font color='green'><center>Set WF Panel to<br>select promotion list<br>panel");
        return bd;
    }

}
