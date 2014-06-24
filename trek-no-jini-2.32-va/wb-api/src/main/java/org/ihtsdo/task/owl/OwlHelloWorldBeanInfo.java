package org.ihtsdo.task.owl;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class OwlHelloWorldBeanInfo extends SimpleBeanInfo {
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor owlHelloWorldText = new PropertyDescriptor("owlHelloWorldText", getBeanDescriptor().getBeanClass());
            owlHelloWorldText.setBound(true);
            owlHelloWorldText.setPropertyEditorClass(JTextFieldEditor.class);
            owlHelloWorldText.setDisplayName("<html><font color='green'>OWL Hello World Text:");
            owlHelloWorldText.setShortDescription("Hello world text to display to the user.");

            PropertyDescriptor owlHelloWorldTextProperty = new PropertyDescriptor("owlHelloWorldTextProperty",
                getBeanDescriptor().getBeanClass());
            owlHelloWorldTextProperty.setBound(true);
            owlHelloWorldTextProperty.setPropertyEditorClass(PropertyNameLabelEditor.class);
            owlHelloWorldTextProperty.setDisplayName("<html><font color='green'>Text Property:");
            owlHelloWorldTextProperty.setShortDescription("Text that is appended to the hello world text.");

            PropertyDescriptor rv[] = { owlHelloWorldText, owlHelloWorldTextProperty };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(OwlHelloWorld.class);
        bd.setDisplayName("<html><font color='green'><center>OWL Hello World");
        return bd;
    }

}
