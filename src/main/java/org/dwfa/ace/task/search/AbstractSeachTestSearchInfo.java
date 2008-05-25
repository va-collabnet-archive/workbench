package org.dwfa.ace.task.search;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

public abstract class AbstractSeachTestSearchInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor inverted =
                new PropertyDescriptor("inverted", getBeanDescriptor().getBeanClass());
            inverted.setBound(true);
            inverted.setPropertyEditorClass(CheckboxEditor.class);
            inverted.setDisplayName("<html><font color='green'>exclude matches:");
            inverted.setShortDescription("If checked, excludes concepts that match this criteria");
            
            PropertyDescriptor[] childDescriptors = getAdditionalPropertyDescriptors();

            PropertyDescriptor[] rv = new PropertyDescriptor[childDescriptors.length + 1];
            
            rv[0] = inverted;
            for (int i = 1; i < rv.length; i++) {
				rv[i] = childDescriptors[i-1];
			}
            
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }

	protected abstract PropertyDescriptor[] getAdditionalPropertyDescriptors();
}