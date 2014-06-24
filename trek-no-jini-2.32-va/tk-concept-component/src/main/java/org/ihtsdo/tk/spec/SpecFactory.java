/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.spec;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

/**
 * A factory for creating Spec objects.
 */
public class SpecFactory {

    /**
     * Gets a concept spec representing the given
     * <code>conceptChronicle</code> and based on the given
     * <code>viewCoordinate</code>.
     *
     * @param conceptChronicle the concept to represent
     * @param viewCoordinate the view coordinate specifying which versions of the concept are active or inactive
     * @return a concept spec representing the specified concept
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec get(ConceptChronicleBI conceptChronicle, ViewCoordinate viewCoordinate) throws IOException {
        ConceptVersionBI cv = Ts.get().getConceptVersion(viewCoordinate, conceptChronicle.getNid());
        try {
            return new ConceptSpec(cv.getDescriptionsActive().iterator().next().getText(),
                    conceptChronicle.getPrimUuid());
        } catch (ContradictionException ex) {
            for (DescriptionChronicleBI desc : conceptChronicle.getDescriptions()) {
                for (DescriptionVersionBI dv : desc.getVersions(viewCoordinate)) {
                    return new ConceptSpec(dv.getText(),
                            conceptChronicle.getPrimUuid());
                }
            }
            throw new IOException("No current description for: " + cv);
        }
    }

    /**
     * Gets a concept spec representing the given
     * <code>conceptVersion</code>.
     *
     * @param conceptVersion the concept version to represent
     * @return a concept spec representing the specified concept
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec get(ConceptVersionBI conceptVersion) throws IOException {
        try {
            return new ConceptSpec(conceptVersion.getDescriptionsActive().iterator().next().getText(),
                    conceptVersion.getPrimUuid());
        } catch (ContradictionException ex) {
            return new ConceptSpec(
                    conceptVersion.getDescriptions().iterator().next().getVersions().iterator().next().getText(),
                    conceptVersion.getPrimUuid());
        }
    }

    /**
     * Gets a description spec representing the given
     * <code>descriptionVersion</code>.
     *
     * @param descriptionVersion the description version to represent
     * @param viewCoordinate the view coordinate specifying which versions are active or inactive
     * @return the description spec representing the specified concept
     * @throws IOException signals that an I/O exception has occurred
     */
    public static DescriptionSpec get(DescriptionVersionBI descriptionVersion, ViewCoordinate viewCoordinate) throws IOException {
        if (descriptionVersion != null && descriptionVersion.getUUIDs() != null) {
            DescriptionSpec ds = new DescriptionSpec(descriptionVersion.getUUIDs().toArray(new UUID[]{}),
                    get(Ts.get().getConcept(descriptionVersion.getConceptNid()), viewCoordinate),
                    get(Ts.get().getConcept(descriptionVersion.getTypeNid()), viewCoordinate),
                    descriptionVersion.getText());
            ds.setLangText(descriptionVersion.getText());
            return ds;
        } else {
            return null;
        }
    }
}
