package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;

/**
 * Bean info to AddRoot class.
 * @author Christine Hill
 *
 */
public class AddRootBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public AddRootBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor root =
                new PropertyDescriptor("root", AddRoot.class);
            root.setBound(true);
            root.setPropertyEditorClass(ConceptLabelPropEditor.class);
            root.setDisplayName("<html><font color='green'>Drag root here:");
            root.setShortDescription("Choose root to add to hierarchy " +
                    "and drag it here.");

            PropertyDescriptor rv[] = { root };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddRoot.class);
        bd.setDisplayName("<html><font color='green'><center>Add Root");
        return bd;
    }

}
