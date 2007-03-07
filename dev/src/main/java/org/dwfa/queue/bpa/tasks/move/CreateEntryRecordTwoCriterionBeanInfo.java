package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateEntryRecordTwoCriterionBeanInfo extends SimpleBeanInfo {

    public CreateEntryRecordTwoCriterionBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor queueTypeTwo =
                new PropertyDescriptor("queueTypeTwo", CreateEntryRecordTwoCriterion.class);
            queueTypeTwo.setBound(true);
            queueTypeTwo.setPropertyEditorClass(QueueTypeEditor.class);
            queueTypeTwo.setDisplayName("Queue type:");
            queueTypeTwo.setShortDescription("The first criterion for the queue to generate the entry record for.");

            PropertyDescriptor queueTypeOne =
                new PropertyDescriptor("queueTypeOne", CreateEntryRecordTwoCriterion.class);
            queueTypeOne.setBound(true);
            queueTypeOne.setPropertyEditorClass(QueueTypeEditor.class);
            queueTypeOne.setDisplayName("Queue type:");
            queueTypeOne.setShortDescription("The second criterion for the queue to generate the entry record for.");

            PropertyDescriptor localPropertyName =
                new PropertyDescriptor("localPropName", CreateEntryRecordTwoCriterion.class);
            localPropertyName.setBound(true);
            localPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            localPropertyName.setDisplayName("<html><font color='blue'>Entry record:");
            localPropertyName.setShortDescription("Name of the local property that holds the entry record. ");


            PropertyDescriptor rv[] =
                {queueTypeOne, queueTypeTwo, localPropertyName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateEntryRecordTwoCriterion.class);
        bd.setDisplayName("<html><font color='green'><center>Create Entry Record<br>Two Criterion");
        return bd;
    }
}
