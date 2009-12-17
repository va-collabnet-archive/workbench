/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class PerformLuceneSearchBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public PerformLuceneSearchBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor searchRoot = new PropertyDescriptor("searchRoot", PerformLuceneSearch.class);
            searchRoot.setBound(true);
            searchRoot.setPropertyEditorClass(ConceptLabelPropEditor.class);
            searchRoot.setDisplayName("Query root:");
            searchRoot.setShortDescription("Root used for query. Null search the whole database");

            PropertyDescriptor searchString = new PropertyDescriptor("searchString", PerformLuceneSearch.class);
            searchString.setBound(true);
            searchString.setPropertyEditorClass(JTextFieldEditor.class);
            searchString.setDisplayName("Query String:");
            searchString.setShortDescription("Used to query lucene");

            PropertyDescriptor rv[] = { searchString, searchRoot };
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
