package org.dwfa.ace.task.refset.spec.comment;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateCommentExtTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public CreateCommentExtTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        PropertyDescriptor commentsPropName;
        try {
            commentsPropName = new PropertyDescriptor("commentsPropName", getBeanDescriptor().getBeanClass());

            commentsPropName.setBound(true);
            commentsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            commentsPropName.setDisplayName("<html><font color='green'>comments prop name:");
            commentsPropName
                .setShortDescription("The property that contains the text to be put into the comments ext.");

            PropertyDescriptor rv[] = { commentsPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateCommentExtTask.class);
        bd.setDisplayName("<html><font color='blue'><center>Create comments ext");
        return bd;
    }

}
