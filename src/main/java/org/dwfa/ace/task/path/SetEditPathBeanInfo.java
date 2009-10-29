package org.dwfa.ace.task.path;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;

public class SetEditPathBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor editPathEntry =
                new PropertyDescriptor("editPathEntry", getBeanDescriptor().getBeanClass());
            editPathEntry.setBound(true);
            editPathEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            editPathEntry.setDisplayName("<html><font color='green'>editing path:");
            editPathEntry.setShortDescription("The property that contains the editing path.");

            PropertyDescriptor rv[] =
                { editPathEntry };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetEditPath.class);
        bd.setDisplayName("<html><font color='green'><center>set edit path");
        return bd;
    }
}
