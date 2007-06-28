package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info for TakeFirstItemInAttachmentListReturnUUID class.
 * @author Susan Castillo
 *
 */
public class TakeFirstItemInAttachmentListReturnUUIDBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public TakeFirstItemInAttachmentListReturnUUIDBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor uuidListPropName =
                new PropertyDescriptor("uuidListPropName", TakeFirstItemInAttachmentListReturnUUID.class);
            uuidListPropName.setBound(true);
            uuidListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListPropName.setDisplayName("<html><font color='green'>Name of list:");
            uuidListPropName.setShortDescription("Name of the temporary list.");

            PropertyDescriptor potDupUuidPropName =
                new PropertyDescriptor("potDupUuidPropName", TakeFirstItemInAttachmentListReturnUUID.class);
            potDupUuidPropName.setBound(true);
            potDupUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            potDupUuidPropName.setDisplayName("<html><font color='green'>Uuid:");
            potDupUuidPropName.setShortDescription("Uuid");

            PropertyDescriptor rv[] = { uuidListPropName, potDupUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TakeFirstItemInAttachmentList.class);
        bd.setDisplayName("<html><font color='green'><center>Take 1st Item<br>In Attachment List<br> Return Uuid");
        return bd;
    }

}
