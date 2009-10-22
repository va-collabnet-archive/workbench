package org.dwfa.ace.task.refset.spec.wf;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class InstructWithPromotionRefsetAndDoneBeanInfo extends SimpleBeanInfo {
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

            PropertyDescriptor processPropName =
                    new PropertyDescriptor("processPropName", getBeanDescriptor().getBeanClass());
            processPropName.setBound(true);
            processPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            processPropName.setDisplayName("process prop");
            processPropName.setShortDescription("A property containing a process which is loaded, set, then launched.");

            PropertyDescriptor rv[] = { instruction, profilePropName, processPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(InstructWithPromotionRefsetAndDone.class);
        bd.setDisplayName("<html><font color='green'><center>Instruct and Wait:<br>Promotion refset and done");
        return bd;
    }
}
