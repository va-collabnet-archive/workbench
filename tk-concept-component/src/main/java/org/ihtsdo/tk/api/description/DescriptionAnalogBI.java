/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api.description;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.TypedComponentAnalogBI;

// TODO: Auto-generated Javadoc
/**
 * The Interface DescriptionAnalogBI.
 *
 * @param <A> the generic type
 */
public interface DescriptionAnalogBI<A extends DescriptionAnalogBI>
        extends TypedComponentAnalogBI, DescriptionVersionBI<A> {

    /**
     * Sets the initial case significant.
     *
     * @param initialCaseSignificant the new initial case significant
     * @throws PropertyVetoException the property veto exception
     */
    public void setInitialCaseSignificant(boolean initialCaseSignificant) throws PropertyVetoException;
    
    /**
     * Sets the lang.
     *
     * @param lang the new lang
     * @throws PropertyVetoException the property veto exception
     */
    public void setLang(String lang) throws PropertyVetoException;
    
    /**
     * Sets the text.
     *
     * @param text the new text
     * @throws PropertyVetoException the property veto exception
     */
    public void setText(String text) throws PropertyVetoException;

}
