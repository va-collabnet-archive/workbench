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
import org.ihtsdo.tk.api.AnalogBI;

import org.ihtsdo.tk.api.TypedComponentAnalogBI;

/**
 * The Interface DescriptionAnalogBI contains methods for editing a description analog.
 * The preferred method of editing terminology is through a blueprint.
 * 
 * @param <A> the type of object returned by the analog generator
 * @see AnalogBI
 * @eee CreateOrAmendBlueprint
 */
public interface DescriptionAnalogBI<A extends DescriptionAnalogBI>
        extends TypedComponentAnalogBI, DescriptionVersionBI<A> {

    /**
     * Sets the description as initial case significant.
     *
     * @param initialCaseSignificant set to <code>true</code> to make the description initial case significant
     * @throws PropertyVetoException if the new value is not valid
     */
    public void setInitialCaseSignificant(boolean initialCaseSignificant) throws PropertyVetoException;
    
    /**
     * Sets the language of the description.
     *
     * @param lang the two character abbreviation of the language the description is in
     * @throws PropertyVetoException if the new value is not valid
     */
    public void setLang(String lang) throws PropertyVetoException;
    
    /**
     * Sets the text of the description.
     *
     * @param text the text of the description to display
     * @throws PropertyVetoException if the new value is not valid
     */
    public void setText(String text) throws PropertyVetoException;

}
