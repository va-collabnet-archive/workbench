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
public class TakeFirstItemInAttachmentListReturnConceptBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public TakeFirstItemInAttachmentListReturnConceptBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor uuidListPropName =
                new PropertyDescriptor("uuidListPropName", getBeanDescriptor().getBeanClass());
            uuidListPropName.setBound(true);
            uuidListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListPropName.setDisplayName("<html><font color='green'>Name of list:");
            uuidListPropName.setShortDescription("Name of the temporary list.");

            PropertyDescriptor conceptPropName =
                new PropertyDescriptor("conceptPropName", getBeanDescriptor().getBeanClass());
            conceptPropName.setBound(true);
            conceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptPropName.setDisplayName("<html><font color='green'>Concept:");
            conceptPropName.setShortDescription("Concept");

            PropertyDescriptor rv[] = { uuidListPropName, conceptPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TakeFirstItemInAttachmentListReturnConcept.class);
        bd.setDisplayName("<html><font color='green'><center>DON'T USE<br>Take 1st Item<br>In Attachment List<br> Return Concept");
        return bd;
    }

}

