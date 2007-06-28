package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddConceptAndPotDupToListBeanInfo extends SimpleBeanInfo {

	/**
	 * Bean info for AddConceptAndPotDupToList class.
	 * @author Susan Castillo
	 *
	 */
    public AddConceptAndPotDupToListBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor conceptUuidStrPropName =
                new PropertyDescriptor("conceptUuidStrPropName", AddConceptAndPotDupToList.class);
            conceptUuidStrPropName.setBound(true);
            conceptUuidStrPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptUuidStrPropName.setDisplayName("<html><font color='green'>Uuid:");
            conceptUuidStrPropName.setShortDescription("Uuid");

            PropertyDescriptor rv[] = { conceptUuidStrPropName };
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
        bd.setDisplayName("<html><font color='green'><center>Add Concept and<br>Pot Dup<br>to List");
        return bd;
    }

}
