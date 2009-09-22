package org.dwfa.ace.task.refset.spec.wf;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetInboxAddressFromUuidTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public SetInboxAddressFromUuidTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor nextUserTermEntryPropName;
            nextUserTermEntryPropName =
                    new PropertyDescriptor("nextUserTermEntryPropName", getBeanDescriptor().getBeanClass());
            nextUserTermEntryPropName.setBound(true);
            nextUserTermEntryPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            nextUserTermEntryPropName.setDisplayName("<html><font color='green'>next person:");
            nextUserTermEntryPropName.setShortDescription("The next person the BP will go to.");

            PropertyDescriptor uuidPropName;
            uuidPropName = new PropertyDescriptor("uuidPropName", getBeanDescriptor().getBeanClass());
            uuidPropName.setBound(true);
            uuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidPropName.setDisplayName("<html><font color='green'>UUID prop name:");
            uuidPropName.setShortDescription("The property with UUID.");

            PropertyDescriptor rv[] = { nextUserTermEntryPropName, uuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetInboxAddressFromUuidTask.class);
        bd.setDisplayName("<html><font color='green'><center>Set Inbox addr<br>from UUID");
        return bd;
    }

}
