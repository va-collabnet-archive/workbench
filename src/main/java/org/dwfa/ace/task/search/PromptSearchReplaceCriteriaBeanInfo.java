package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class PromptSearchReplaceCriteriaBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public PromptSearchReplaceCriteriaBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
    	try {
            PropertyDescriptor searchStringPropName =
                new PropertyDescriptor("searchStringPropName", PromptSearchReplaceCriteria.class);
            searchStringPropName.setBound(true);
            searchStringPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            searchStringPropName.setDisplayName("<html><font color='green'>Search Prop Name");
            searchStringPropName.setShortDescription("");

            PropertyDescriptor replaceStringPropName =
                new PropertyDescriptor("replaceStringPropName", PromptSearchReplaceCriteria.class);
            replaceStringPropName.setBound(true);
            replaceStringPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            replaceStringPropName.setDisplayName("<html><font color='green'>Replace Prop Name");
            replaceStringPropName.setShortDescription("");

            PropertyDescriptor searchAllPropName =
                new PropertyDescriptor("searchAllPropName", PromptSearchReplaceCriteria.class);
            searchAllPropName.setBound(true);
            searchAllPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            searchAllPropName.setDisplayName("<html><font color='green'>Include All Descriptions Prop Name");
            searchAllPropName.setShortDescription("");
            
            PropertyDescriptor searchFsnPropName =
                new PropertyDescriptor("searchFsnPropName", PromptSearchReplaceCriteria.class);
            searchFsnPropName.setBound(true);
            searchFsnPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            searchFsnPropName.setDisplayName("<html><font color='green'>Include FSN's Prop Name");
            searchFsnPropName.setShortDescription("");
            
            PropertyDescriptor searchPftPropName =
                new PropertyDescriptor("searchPftPropName", PromptSearchReplaceCriteria.class);
            searchPftPropName.setBound(true);
            searchPftPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            searchPftPropName.setDisplayName("<html><font color='green'>Include PT's Prop Name");
            searchPftPropName.setShortDescription("");
            
            PropertyDescriptor searchSynonymPropName =
                new PropertyDescriptor("searchSynonymPropName", PromptSearchReplaceCriteria.class);
            searchSynonymPropName.setBound(true);
            searchSynonymPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            searchSynonymPropName.setDisplayName("<html><font color='green'>Include Synonym's Prop Name");
            searchSynonymPropName.setShortDescription("");
            
            PropertyDescriptor caseSensitivePropName =
                new PropertyDescriptor("caseSensitivePropName", PromptSearchReplaceCriteria.class);
            caseSensitivePropName.setBound(true);
            caseSensitivePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            caseSensitivePropName.setDisplayName("<html><font color='green'>Case Sensitive Prop Name");
            caseSensitivePropName.setShortDescription("");

            PropertyDescriptor rv[] = { searchStringPropName, replaceStringPropName, caseSensitivePropName, searchAllPropName, searchFsnPropName, searchPftPropName, searchSynonymPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
      }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(PromptSearchReplaceCriteria.class);
        bd.setDisplayName("<html><font color='green'><center>Prompt Search and Replace<br>Terms in List");
        return bd;
    }

}
