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

/**
 * The Interface ConceptAttributeVersionBI contains methods specific to interacting with or creating a concept attribute version.
 *
 * @param <A> the type of object returned by the analog generator
 * @see ComponentVersionBI
 */
public interface ConceptAttributeVersionBI<A extends ConceptAttributeAnalogBI>
	extends ComponentVersionBI,
        ConceptAttributeChronicleBI,
        AnalogGeneratorBI<A> {

    /**
     * Checks if this version is defined.
     *
     * @return <code>true</code>, if this version is defined
     */
    public boolean isDefined();
    
    /**
     * 
     * @param viewCoordinate specifying which version of the concept attributes to use for the blueprint
     * @return the concept attribute blueprint which can be constructed to create a <code>ConceptAttributeChronicleBI</code>
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is returned
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     */
    @Override
    public ConceptAttributeAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;
}
