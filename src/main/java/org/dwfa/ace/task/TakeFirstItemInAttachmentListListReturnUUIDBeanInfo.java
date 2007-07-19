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
public class TakeFirstItemInAttachmentListListReturnUUIDBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public TakeFirstItemInAttachmentListListReturnUUIDBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor listListNamePropName =
                new PropertyDescriptor("listListNamePropName", TakeFirstItemInAttachmentListListReturnUUID.class);
            listListNamePropName.setBound(true);
            listListNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            listListNamePropName.setDisplayName("<html><font color='green'>Name list of lists:");
            listListNamePropName.setShortDescription("Name of list of lists.");

            PropertyDescriptor uuidListPropName =
                new PropertyDescriptor("uuidListPropName", TakeFirstItemInAttachmentListListReturnUUID.class);
            uuidListPropName.setBound(true);
            uuidListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListPropName.setDisplayName("<html><font color='green'>Concept UUID:");
            uuidListPropName.setShortDescription("Concept UUID.");

            PropertyDescriptor rv[] = { listListNamePropName, uuidListPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TakeFirstItemInAttachmentListListReturnUUID.class);
        bd.setDisplayName("<html><font color='green'><center>Take 1st Item<br>In Attachment<br> List of Lists<br> Return  UUID");
        return bd;
    }

}
