/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.translation.tasks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * The Class SearchSimilarityToSignpostBeanInfo.
 */
public class SearchSimilarityToSignpostBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new search similarity to signpost bean info.
	 */
	public SearchSimilarityToSignpostBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {
			PropertyDescriptor queryInput = new PropertyDescriptor("query", SearchSimilarityToSignpost.class);
			queryInput.setBound(true);
			queryInput.setPropertyEditorClass(JTextFieldEditor.class);
			queryInput.setDisplayName("query");
			queryInput.setShortDescription("Query to search.");
			
			PropertyDescriptor sourceLangCode = new PropertyDescriptor("sourceLangCode", SearchSimilarityToSignpost.class);
			sourceLangCode.setBound(true);
			sourceLangCode.setPropertyEditorClass(LangCodeSelectorEditor.class);
			sourceLangCode.setDisplayName("sourceLangCode");
			sourceLangCode.setShortDescription("Select a sourceLangCode.");
			
			PropertyDescriptor targetLangCode = new PropertyDescriptor("targetLangCode", SearchSimilarityToSignpost.class);
			targetLangCode.setBound(true);
			targetLangCode.setPropertyEditorClass(LangCodeSelectorEditor.class);
			targetLangCode.setDisplayName("targetLangCode");
			targetLangCode.setShortDescription("Select a targetLangCode.");
			
			PropertyDescriptor rv[] =
			{queryInput, sourceLangCode, targetLangCode};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(SearchSimilarityToSignpost.class);
		bd.setDisplayName("<html><font color='green'><center>Search Similarity<br>to Signpost");
		return bd;
	}

}
