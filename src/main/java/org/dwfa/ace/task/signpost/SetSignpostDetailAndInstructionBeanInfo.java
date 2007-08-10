package org.dwfa.ace.task.signpost;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetSignpostDetailAndInstructionBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor instructionHtmlPropName =
                new PropertyDescriptor("instructionHtmlPropName", getBeanDescriptor().getBeanClass());
            instructionHtmlPropName.setBound(true);
            instructionHtmlPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            instructionHtmlPropName.setDisplayName("<html><font color='green'>instruction html");
            instructionHtmlPropName.setShortDescription("Set the signpost instruction html to value contained in this property.");

            PropertyDescriptor detailHtmlPropName =
                new PropertyDescriptor("detailHtmlPropName", getBeanDescriptor().getBeanClass());
            detailHtmlPropName.setBound(true);
            detailHtmlPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            detailHtmlPropName.setDisplayName("<html><font color='green'>detail html");
            detailHtmlPropName.setShortDescription("Set the signpost detail html to value contained in this property.");

            PropertyDescriptor rv[] = { detailHtmlPropName, instructionHtmlPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetSignpostDetailAndInstruction.class);
        bd.setDisplayName("<html><font color='green'><center>Set Signpost<br> detail and instruction");
        return bd;
    }

}
