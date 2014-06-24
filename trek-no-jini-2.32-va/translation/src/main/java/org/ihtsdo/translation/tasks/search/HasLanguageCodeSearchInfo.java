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
package org.ihtsdo.translation.tasks.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.ace.task.search.AbstractSeachTestSearchInfo;
import org.ihtsdo.translation.tasks.LangCodeSelectorEditor;

/**
 * The Class HasLanguageCodeSearchInfo.
 */
public class HasLanguageCodeSearchInfo extends AbstractSeachTestSearchInfo {

	/* (non-Javadoc)
	 * @see org.dwfa.ace.task.search.AbstractSeachTestSearchInfo#getAdditionalPropertyDescriptors()
	 */
	@Override
	protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
		try {

            PropertyDescriptor statusTerm =
                new PropertyDescriptor("languageCode", getBeanDescriptor().getBeanClass());
            statusTerm.setBound(true);
            statusTerm.setPropertyEditorClass(LangCodeSelectorEditor.class);
            statusTerm.setDisplayName("<html><font color='green'>LanguageCode:");
            statusTerm.setShortDescription("The concept to test for status is kind of.");

            PropertyDescriptor rv[] =
                { statusTerm };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    
    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(HasLanguageCode.class);
        bd.setDisplayName("language code");
        return bd;
    }
}
