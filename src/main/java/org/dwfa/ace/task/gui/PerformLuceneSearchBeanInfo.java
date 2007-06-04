package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

public class PerformLuceneSearchBeanInfo extends SimpleBeanInfo {

	    /**
	     * 
	     */
	    public PerformLuceneSearchBeanInfo() {
	        super();
	     }

	    public PropertyDescriptor[] getPropertyDescriptors() {
	        try {  
	            PropertyDescriptor searchRoot =
	                new PropertyDescriptor("searchRoot", PerformLuceneSearch.class);
	            searchRoot.setBound(true);
	            searchRoot.setPropertyEditorClass(QueueTypeEditor.class);
	            searchRoot.setDisplayName("Query root:");
	            searchRoot.setShortDescription("Root used for query. Null search the whole database");
	            
	            PropertyDescriptor searchString =
	                new PropertyDescriptor("searchString", PerformLuceneSearch.class);
	            searchString.setBound(true);
	            searchString.setPropertyEditorClass(JTextFieldEditor.class);
	            searchString.setDisplayName("Query String:");
	            searchString.setShortDescription("Used to query lucene");

	            PropertyDescriptor rv[] =
	                { searchString, searchRoot };
	            return rv;
	        } catch (IntrospectionException e) {
	             throw new Error(e.toString());
	        }
	     }        
	    /**
	     * @see java.beans.BeanInfo#getBeanDescriptor()
	     */
	    public BeanDescriptor getBeanDescriptor() {
	        BeanDescriptor bd = new BeanDescriptor(ShowSearch.class);
	        bd.setDisplayName("<html><font color='green'><center>Query String");
	        return bd;
	    }
}
