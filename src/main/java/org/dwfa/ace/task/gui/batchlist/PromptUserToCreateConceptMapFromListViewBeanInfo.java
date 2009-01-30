package org.dwfa.ace.task.gui.batchlist;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class PromptUserToCreateConceptMapFromListViewBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor conceptMapPropName =
                new PropertyDescriptor("conceptMapPropName", getBeanDescriptor().getBeanClass());
            conceptMapPropName.setBound(true);
            conceptMapPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptMapPropName.setDisplayName("<html><font color='green'>concept map prop:");
            conceptMapPropName.setShortDescription("The property that contains the resulting concept map.");

            PropertyDescriptor rv[] =
                { conceptMapPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(PromptUserToCreateConceptMapFromListView.class);
        bd.setDisplayName("<html><font color='green'><center>Create Concept Map<br>from Concepts<br>in List View");
        return bd;
    }
}
