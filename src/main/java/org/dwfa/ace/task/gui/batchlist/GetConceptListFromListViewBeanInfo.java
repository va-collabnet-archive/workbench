package org.dwfa.ace.task.gui.batchlist;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class GetConceptListFromListViewBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor conceptListPropName =
                new PropertyDescriptor("conceptListPropName", getBeanDescriptor().getBeanClass());
            conceptListPropName.setBound(true);
            conceptListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptListPropName.setDisplayName("<html><font color='green'>Concept list property:");
            conceptListPropName.setShortDescription("The property set to the concept list rom the list view.");

            PropertyDescriptor rv[] = { conceptListPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }
    
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetConceptListFromListView.class);
        bd.setDisplayName("<html><font color='green'><center>Get Concept List<br>from List View");
        return bd;
    }

}
