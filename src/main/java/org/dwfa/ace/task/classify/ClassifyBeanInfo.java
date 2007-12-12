package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

public class ClassifyBeanInfo extends SimpleBeanInfo {
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor factoryClass =
                new PropertyDescriptor("factoryClass", Classify.class);
            factoryClass.setBound(true);
            factoryClass.setPropertyEditorClass(JTextFieldEditor.class);
            factoryClass.setDisplayName("<html><font color='green'>Factory class:");
            factoryClass.setShortDescription("Enter the factory class to be called.");

            PropertyDescriptor classifyRoot =
                new PropertyDescriptor("classifyRoot", Classify.class);
            classifyRoot.setBound(true);
            classifyRoot.setPropertyEditorClass(QueueTypeEditor.class);
            classifyRoot.setDisplayName("<html><font color='green'>Classification Root Concept:");
            classifyRoot.setShortDescription("Define the root Concept for classification.");

            PropertyDescriptor rv[] = { factoryClass, classifyRoot };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(Classify.class);
        bd.setDisplayName("<html><font color='green'><center>Classify");
        return bd;
    }

}
