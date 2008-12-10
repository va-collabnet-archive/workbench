package org.dwfa.ace.task.input;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class PromptForNumericInputBeanInfo extends SimpleBeanInfo {


    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor instruction =
                new PropertyDescriptor("instruction", PromptForNumericInput.class);
            instruction.setBound(true);
            instruction.setPropertyEditorClass(JTextFieldEditor.class);
            instruction.setDisplayName("<html><font color='green'>Instruction:");
            instruction.setShortDescription("Instructions to present to the user in the workflow panel. ");

            PropertyDescriptor outputPropName =
                new PropertyDescriptor("outputPropName", getBeanDescriptor().getBeanClass());
            outputPropName.setBound(true);
            outputPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            outputPropName.setDisplayName("<html><font color='green'>Output property name:");
            outputPropName.setShortDescription("Output property name.");

            PropertyDescriptor allowNegative =
                new PropertyDescriptor("allowNegative", getBeanDescriptor().getBeanClass());
            allowNegative.setBound(true);
            allowNegative.setPropertyEditorClass(CheckboxEditor.class);
            allowNegative.setDisplayName("<html><font color='green'>Allow negatives:");
            allowNegative.setShortDescription("");

            PropertyDescriptor allowZero =
                new PropertyDescriptor("allowZero", getBeanDescriptor().getBeanClass());
            allowZero.setBound(true);
            allowZero.setPropertyEditorClass(CheckboxEditor.class);
            allowZero.setDisplayName("<html><font color='green'>Allow zero:");
            allowZero.setShortDescription("");

            PropertyDescriptor allowDouble =
                new PropertyDescriptor("allowDouble", getBeanDescriptor().getBeanClass());
            allowDouble.setBound(true);
            allowDouble.setPropertyEditorClass(CheckboxEditor.class);
            allowDouble.setDisplayName("<html><font color='green'>Allow doubles:");
            allowDouble.setShortDescription("Allow doubles:<br>(alternative is integer only)");

            PropertyDescriptor rv[] = { instruction, outputPropName, allowNegative, allowZero,
                    allowDouble };

            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(PromptForNumericInput.class);
        bd.setDisplayName("<html><font color='green'><center>Prompt for numeric<br>input");
        return bd;
    }
}
