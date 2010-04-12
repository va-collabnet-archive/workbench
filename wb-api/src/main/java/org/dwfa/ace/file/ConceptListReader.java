/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.file;

import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.TerminologyRuntimeException;

/**
 * Implements IterableFileReader to convert each line of the file to a concept.<br>
 * Expected columns in the tab delimited file are:
 * <ul>
 * <li>1. Identifier (String) - may be any type of identifier, SCTID, UUID, etc.
 * <li>2. Description (String) - any valid description for the concept. Used to
 * validate the ID is correct.
 * </ul>
 */
public class ConceptListReader extends IterableFileReader<I_GetConceptData> {

    protected I_TermFactory termFactory = Terms.get();

    /**
     * @throws TerminologyRuntimeException if processing fails during iteration
     */
    @Override
    protected I_GetConceptData processLine(String line) {

        try {
            String[] columns = line.split("\t");
            String conceptId = columns[0];
            String description = columns[1];

            if (conceptId.length() == 0 || description.length() == 0) {
                throw new TerminologyException("Invalid file format");
            }

            for (I_GetConceptData concept : termFactory.getConcept(conceptId)) {
                // validate against the description to ensure the id matches
                if (verifyDescription(concept, description)) {
                    return concept;
                }
            }

            throw new TerminologyException("Cannot find a concept with ID " + conceptId + " and the description '"
                + description + "'");

        } catch (IndexOutOfBoundsException ex) {
            throw new TerminologyRuntimeException("Invalid file format");
        } catch (Exception ex) {
            throw new TerminologyRuntimeException(ex);
        }
    }

    protected boolean verifyDescription(I_GetConceptData concept, String description) throws Exception {
        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        // check that the description parameter corresponds to one of the
        // concept's descriptions
        List<? extends I_DescriptionTuple> descriptionTuples = concept.getDescriptionTuples(null, null, null,
            config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_DescriptionTuple tuple : descriptionTuples) {
            if (description.toLowerCase().trim().equals(tuple.getText().toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }
}
