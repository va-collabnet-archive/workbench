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
package org.ihtsdo.tk.api.relationship;

import java.io.IOException;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TypedComponentVersionBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

// TODO: Auto-generated Javadoc
/**
 * The Interface RelationshipVersionBI.
 *
 * @param <A> the generic type
 */
public interface RelationshipVersionBI<A extends RelationshipAnalogBI>
        extends TypedComponentVersionBI,
        RelationshipChronicleBI,
        AnalogGeneratorBI<A> {

    /**
     * Gets the refinability nid.
     *
     * @return the refinability nid
     */
    public int getRefinabilityNid();

    /**
     * Gets the characteristic nid.
     *
     * @return the characteristic nid
     */
    public int getCharacteristicNid();

    /**
     * Gets the group.
     *
     * @return the group
     */
    public int getGroup();

    /**
     * Checks if is inferred.
     *
     * @return true, if is inferred
     */
    public boolean isInferred();
    
    /**
     * Checks if is stated.
     *
     * @return true, if is stated
     */
    public boolean isStated();
    
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ComponentVersionBI#makeBlueprint(org.ihtsdo.tk.api.coordinate.ViewCoordinate)
     */
    @Override
    public RelationshipCAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;

}
