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
package org.ihtsdo.tk.api.blueprint;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

// TODO: Auto-generated Javadoc
/**
 * The Class ConceptAttributeAB.
 *
 * @author kec
 */
public class ConceptAttributeAB extends CreateOrAmendBlueprint {

    /** The defined. */
    public boolean defined;

    /**
     * Instantiates a new concept attribute ab.
     *
     * @param conceptNid the concept nid
     * @param defined the defined
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public ConceptAttributeAB(
            int conceptNid, boolean defined)
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimUuid(),
                defined, null, null);
    }

    /**
     * Instantiates a new concept attribute ab.
     *
     * @param conceptNid the concept nid
     * @param defined the defined
     * @param conceptAttributeVersion the concept attribute version
     * @param viewCoordinate the view coordinate
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public ConceptAttributeAB(
            int conceptNid, boolean defined, ConceptAttributeVersionBI conceptAttributeVersion,
            ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimUuid(),
                defined, conceptAttributeVersion, viewCoordinate);
    }

    /**
     * Instantiates a new concept attribute ab.
     *
     * @param componentUuid the component uuid
     * @param defined the defined
     * @param conceptAttributeVersion the concept attribute version
     * @param viewCoordinate the view coordinate
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public ConceptAttributeAB(
            UUID componentUuid, boolean defined, ConceptAttributeVersionBI conceptAttributeVersion,
            ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        super(componentUuid, conceptAttributeVersion, viewCoordinate);
        this.defined = defined;
    }

    /**
     * Validate.
     *
     * @param conceptAttributeVersion the concept attribute version
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public boolean validate(ConceptAttributeVersionBI conceptAttributeVersion) throws IOException {
        if (conceptAttributeVersion.getStatusNid() != getStatusNid()) {
            return false;
        }
        if (conceptAttributeVersion.getNid() != getComponentNid()) {
            return false;
        }
        if (conceptAttributeVersion.isDefined() != defined) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint#recomputeUuid()
     */
    @Override
    public void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        throw new InvalidCAB ("UUID for ConAttrAB is set when concept is created");
    }
}
