package org.dwfa.ace.task.wfdetailsSheet;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetWorkflowDetailsSheetToRefreshSpecClausePanelBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
         try {  

             PropertyDescriptor refsetPositionSetPropName =
                 new PropertyDescriptor("refsetPositionSetPropName", getBeanDescriptor().getBeanClass());
             refsetPositionSetPropName.setBound(true);
             refsetPositionSetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             refsetPositionSetPropName.setDisplayName("<html><font color='green'>refset position set prop:");
             refsetPositionSetPropName.setShortDescription("The property that contains the refset position set.");

             PropertyDescriptor refsetUuidPropName =
                 new PropertyDescriptor("refsetUuidPropName", getBeanDescriptor().getBeanClass());
             refsetUuidPropName.setBound(true);
             refsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             refsetUuidPropName.setDisplayName("<html><font color='green'>refset uuid prop:");
             refsetUuidPropName.setShortDescription("The property that contains the uuid of the refset spec being updated.");

             PropertyDescriptor snomedPositionSetPropName =
                 new PropertyDescriptor("snomedPositionSetPropName", getBeanDescriptor().getBeanClass());
             snomedPositionSetPropName.setBound(true);
             snomedPositionSetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             snomedPositionSetPropName.setDisplayName("<html><font color='green'>SNOMED position set prop:");
             snomedPositionSetPropName.setShortDescription("The property that contains the SNOMED position set.");

             PropertyDescriptor conceptToReplaceUuidPropName =
                 new PropertyDescriptor("conceptToReplaceUuidPropName", getBeanDescriptor().getBeanClass());
             conceptToReplaceUuidPropName.setBound(true);
             conceptToReplaceUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             conceptToReplaceUuidPropName.setDisplayName("<html><font color='green'>concept to replace UUID prop:");
             conceptToReplaceUuidPropName.setShortDescription("The property that contains the UUID of the concept to replace.");

             PropertyDescriptor clauseToUpdateMemberUuidPropName =
                 new PropertyDescriptor("clausesToUpdateMemberUuidPropName", getBeanDescriptor().getBeanClass());
             clauseToUpdateMemberUuidPropName.setBound(true);
             clauseToUpdateMemberUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             clauseToUpdateMemberUuidPropName.setDisplayName("<html><font color='green'>clauses prop:");
             clauseToUpdateMemberUuidPropName.setShortDescription("The property that contains the List<Collectin<UUID>> of member uuids of the clauses being updated.");

             PropertyDescriptor profilePropName =
                 new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
             profilePropName.setBound(true);
             profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             profilePropName.setDisplayName("<html><font color='green'>profile prop:");
             profilePropName.setShortDescription("The property that contains the working profile.");

            PropertyDescriptor rv[] =
                { refsetUuidPropName, refsetPositionSetPropName, snomedPositionSetPropName, 
                    conceptToReplaceUuidPropName, 
                    clauseToUpdateMemberUuidPropName, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetWorkflowDetailsSheetToRefreshSpecClausePanel.class);
        bd.setDisplayName("<html><font color='green'><center>Set Workflow Details<br>Sheet to<br>Refresh Spec<br>Clause Panel");
        return bd;
    }
}
