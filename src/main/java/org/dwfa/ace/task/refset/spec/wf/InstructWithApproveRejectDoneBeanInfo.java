package org.dwfa.ace.task.refset.spec.wf;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class InstructWithApproveRejectDoneBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor instruction = new PropertyDescriptor("instruction", getBeanDescriptor().getBeanClass());
            instruction.setBound(true);
            instruction.setPropertyEditorClass(JTextFieldEditor.class);
            instruction.setDisplayName("<html><font color='green'>Instruction:");
            instruction.setShortDescription("Instructions to present to the user in the workflow panel. ");

            PropertyDescriptor profilePropName =
                    new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName
                .setShortDescription("The property that will contain the profile this task uses and/or modifies.");

            PropertyDescriptor memberRefsetUuidPropName;
            memberRefsetUuidPropName =
                    new PropertyDescriptor("memberRefsetUuidPropName", getBeanDescriptor().getBeanClass());
            memberRefsetUuidPropName.setBound(true);
            memberRefsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            memberRefsetUuidPropName.setDisplayName("<html><font color='green'>Member refset UUID prop:");
            memberRefsetUuidPropName.setShortDescription("The member refset UUID prop.");

            PropertyDescriptor statusUuidPropName;
            statusUuidPropName = new PropertyDescriptor("statusUuidPropName", getBeanDescriptor().getBeanClass());
            statusUuidPropName.setBound(true);
            statusUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            statusUuidPropName.setDisplayName("<html><font color='green'>Promotion status UUID prop:");
            statusUuidPropName.setShortDescription("The promotion status UUID prop.");

            PropertyDescriptor rv[] = { instruction, profilePropName, memberRefsetUuidPropName, statusUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(InstructWithApproveRejectDone.class);
        bd.setDisplayName("<html><font color='green'><center>Instruct and Wait:<br>Approve, Reject,<br>Done");
        return bd;
    }
}
