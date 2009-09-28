package org.dwfa.ace.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class GetSearchCriterionFromWorkflowDetailsPanelAndSearchBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
         try {  

             PropertyDescriptor positionSetPropName =
                 new PropertyDescriptor("positionSetPropName", getBeanDescriptor().getBeanClass());
             positionSetPropName.setBound(true);
             positionSetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             positionSetPropName.setDisplayName("<html><font color='green'>position set prop:");
             positionSetPropName.setShortDescription("The property that will contain the position set.");

             PropertyDescriptor profilePropName =
                 new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
             profilePropName.setBound(true);
             profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             profilePropName.setDisplayName("<html><font color='green'>profile prop:");
             profilePropName.setShortDescription("The property that contains the working profile.");

             PropertyDescriptor resultSetPropName =
                 new PropertyDescriptor("resultSetPropName", getBeanDescriptor().getBeanClass());
             resultSetPropName.setBound(true);
             resultSetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             resultSetPropName.setDisplayName("<html><font color='green'>results prop:");
             resultSetPropName.setShortDescription("The property this task will place the results into (a uuid list-list).");

            PropertyDescriptor rv[] =
                { profilePropName, positionSetPropName, resultSetPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetSearchCriterionFromWorkflowDetailsPanelAndSearch.class);
        bd.setDisplayName("<html><font color='green'><center>Get Criterion<br>and Search Concepts");
        return bd;
    }
}
