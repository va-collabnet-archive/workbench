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
package org.ihtsdo.tk.api.conceptattribute;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;

/**
 * The Interface ConceptAttributeAnalogBI contains methods for editing a concept attribute analog.
 * The preferred method of editing terminology is through a blueprint.
 *
 * @param <A> the type of object returned by the analog generator
 * @see AnalogBI
 * @see CreateOrAmendBlueprint
 */
public interface ConceptAttributeAnalogBI<A extends ConceptAttributeAnalogBI>
        extends AnalogBI, ConceptAttributeVersionBI<A> {
	
    /**
     * Sets concept as defined in this analog.
     *
     * @param defined set to <code>true</code> to make the concept defined
     * @throws PropertyVetoException if the new value is not valid
     */
    public void setDefined(boolean defined) throws PropertyVetoException;

}
