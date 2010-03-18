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
package org.dwfa.mojo.refset.writers;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptIntegerRefsetHandler extends MemberRefsetHandler {
    @Override
    public String formatRefsetLine(I_TermFactory tf, I_ExtendByRefVersion tuple, boolean sctid)
            throws TerminologyException, IOException {
        I_ExtendByRefPartCidInt conceptIntegerPart = (I_ExtendByRefPartCidInt) tuple.getMutablePart();

        return super.formatRefsetLine(tf, tuple, sctid) + MemberRefsetHandler.FILE_DELIMITER
            + toId(tf, conceptIntegerPart.getConceptId(), sctid) + MemberRefsetHandler.FILE_DELIMITER
            + conceptIntegerPart.getIntValue();
    }

    @Override
    public String formatRefsetLine(I_TermFactory tf, I_ExtendByRefPart part, Integer memberId, int refsetId,
            int componentId, boolean sctId) throws TerminologyException, IOException {
        I_ExtendByRefPartCidInt conceptIntegerPart = (I_ExtendByRefPartCidInt) part;

        return super.formatRefsetLine(tf, part, memberId, refsetId, componentId, sctId)
            + MemberRefsetHandler.FILE_DELIMITER + toId(tf, conceptIntegerPart.getConceptId(), sctId)
            + MemberRefsetHandler.FILE_DELIMITER + conceptIntegerPart.getIntValue();
    }

    @Override
    public String getHeaderLine() {
        return super.getHeaderLine() + MemberRefsetHandler.FILE_DELIMITER + "CONCEPT_VALUE"
            + MemberRefsetHandler.FILE_DELIMITER + "INTEGER_VALUE";
    }

    @Override
    protected I_ExtendByRefPart processLine(String line) {
        I_ExtendByRefPartCidInt part;
        try {

            I_ExtendByRef versioned = getExtensionVersioned(line,
                RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION);

            part = getTermFactory().newConceptIntExtensionPart();
            setGenericExtensionPartFields(part);

            String conceptValue = getNextCurrentRowToken();
            part.setConceptId(getNid(UUID.fromString(conceptValue)));

            part.setIntValue(Integer.parseInt(getNextCurrentRowToken()));

            versioned.addVersion(part);

            if (isTransactional()) {
                getTermFactory().addUncommitted(versioned);
            } else {
                getTermFactory().getDirectInterface().writeExt(versioned);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error occred processing file " + sourceFile, e);
        }

        return part;
    }
}
