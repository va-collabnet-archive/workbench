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

// TODO: Auto-generated Javadoc
/**
 * The Interface ConceptAttributeAnalogBI.
 *
 * @param <A> the generic type
 */
public interface ConceptAttributeAnalogBI<A extends ConceptAttributeAnalogBI>
        extends AnalogBI, ConceptAttributeVersionBI<A> {
	
    /**
     * Sets the defined.
     *
     * @param defined the new defined
     * @throws PropertyVetoException the property veto exception
     */
    public void setDefined(boolean defined) throws PropertyVetoException;

}
