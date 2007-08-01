package org.dwfa.ace.task.assignment;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.IncrementEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info to BatchingAssignments class.
 * @author Susan Castillo
 *
 */
public class BatchingAssignmentsBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public BatchingAssignmentsBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            
            PropertyDescriptor uuidList2PropName =
                new PropertyDescriptor("uuidList2PropName", getBeanDescriptor().getBeanClass());
            uuidList2PropName.setBound(true);
            uuidList2PropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidList2PropName.setDisplayName("<html><font color='green'>Output uuid list of list:");
            uuidList2PropName.setShortDescription("uuid List");
            
            PropertyDescriptor uuidListListPropName =
                new PropertyDescriptor("uuidListListPropName", getBeanDescriptor().getBeanClass());
            uuidListListPropName.setBound(true);
            uuidListListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListListPropName.setDisplayName("<html><font color='green'>Input uuid list of list:");
            uuidListListPropName.setShortDescription("UUID List of Lists");
            
            PropertyDescriptor listListSize =
                new PropertyDescriptor("listListSize", getBeanDescriptor().getBeanClass());
            listListSize.setBound(true);
            listListSize.setPropertyEditorClass(IncrementEditor.class);
            listListSize.setDisplayName("<html><font color='green'> number");
            listListSize.setShortDescription("number");

            PropertyDescriptor rv[] = { uuidListListPropName, uuidList2PropName, listListSize };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(BatchingAssignments.class);
        bd.setDisplayName("<html><font color='green'><center>Batch Assignments");
        return bd;
    }

}