package org.dwfa.ace.task.signpost;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class SetSignpostToggleIconBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor iconResource =
                new PropertyDescriptor("iconResource", getBeanDescriptor().getBeanClass());
            iconResource.setBound(true);
            iconResource.setPropertyEditorClass(JTextFieldEditor.class);
            iconResource.setDisplayName("<html><font color='green'>Signpost toggle icon");
            iconResource.setShortDescription("Set the signpost toggle icon from classpath resource.<br><br>" +
                                             "help.png, help2.png, question_and_answer.png,<br>" +
                                             "about.png, lightbulb.png, lightbulb_on.png,<br>" + 
                                             "message.png, message_add.png, message_delete.png,<br>" +
                                             "gear.png, gear_view.png, gears.png, gears_view.png");

            PropertyDescriptor rv[] = { iconResource };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetSignpostToggleIcon.class);
        bd.setDisplayName("<html><font color='green'><center>Set Signpost <br>"
                + "toggle icon");
        return bd;
    }

}
