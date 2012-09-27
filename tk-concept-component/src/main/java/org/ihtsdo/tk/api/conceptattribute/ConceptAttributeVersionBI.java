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

import java.io.IOException;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

// TODO: Auto-generated Javadoc
/**
 * The Interface ConceptAttributeVersionBI.
 *
 * @param <A> the generic type
 */
public interface ConceptAttributeVersionBI<A extends ConceptAttributeAnalogBI>
	extends ComponentVersionBI,
        ConceptAttributeChronicleBI,
        AnalogGeneratorBI<A> {

    /**
     * Checks if is defined.
     *
     * @return true, if is defined
     */
    public boolean isDefined();
    
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ComponentVersionBI#makeBlueprint(org.ihtsdo.tk.api.coordinate.ViewCoordinate)
     */
    @Override
    public ConceptAttributeAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;
}
