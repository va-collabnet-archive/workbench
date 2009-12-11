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

import java.sql.SQLException;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public class ConceptConceptConceptRefsetHandler extends ConceptConceptRefsetHandler {

	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple part, boolean sctid, boolean useRf2) throws SQLException, ClassNotFoundException, Exception {
		return formatRefsetLine(tf, part, part.getMemberId(), part.getRefsetId(), part.getComponentId(), sctid, useRf2);
	}

	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefPart part, Integer memberId, int refsetId, int componentId, boolean sctId, boolean useRf2) throws SQLException, ClassNotFoundException, Exception {
	    I_ThinExtByRefPartConceptConceptConcept conceptPart = (I_ThinExtByRefPartConceptConceptConcept) part;

		return super.formatRefsetLine(tf, part, memberId, refsetId, componentId, sctId, useRf2) + MemberRefsetHandler.COLUMN_DELIMITER
					+ toId(tf, conceptPart.getC3id(), sctId, TYPE.CONCEPT);
	}

    /**
     * @throws Exception
     * @throws ClassNotFoundException
     * @throws SQLException
     * @see org.dwfa.mojo.refset.writers.MemberRefsetHandler#formatRefsetLineRF2(org.dwfa.ace.api.I_TermFactory, org.dwfa.ace.api.ebr.I_ThinExtByRefPart, java.lang.Integer, int, int, boolean, boolean)
     */
    @Override
    public String formatRefsetLineRF2(I_TermFactory tf, I_ThinExtByRefPart part, Integer memberId, int refsetNid,
            int componentId, boolean sctId, boolean useRf2, TYPE type) throws SQLException, ClassNotFoundException, Exception {
        I_ThinExtByRefPartConceptConceptConcept conceptPart = (I_ThinExtByRefPartConceptConceptConcept) part;

        return super.formatRefsetLineRF2(tf, part, memberId, refsetNid, componentId, sctId, useRf2, type)
                + MemberRefsetHandler.COLUMN_DELIMITER + toId(tf, conceptPart.getC3id(), sctId, TYPE.CONCEPT);
    }


	@Override
	public String getHeaderLine() {
		return super.getHeaderLine() + MemberRefsetHandler.COLUMN_DELIMITER + "CONCEPT_CONCEPT_CONCEPT_VALUE_ID";
	}

    /**
     * @see org.dwfa.mojo.refset.writers.MemberRefsetHandler#getRF2HeaderLine()
     */
    @Override
    public String getRF2HeaderLine() {
        return super.getRF2HeaderLine() + COLUMN_DELIMITER + "conceptConceptConceptValueId";
    }

	@Override
	protected I_ThinExtByRefPart processLine(String line) {
		I_ThinExtByRefPartConceptConceptConcept part;
		try {

			I_ThinExtByRefVersioned versioned = getExtensionVersioned(line, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION);

			part = getTermFactory().newExtensionPart(I_ThinExtByRefPartConceptConceptConcept.class);
			setGenericExtensionPartFields(part);

			String conceptValue = getNextCurrentRowToken();
			part.setC1id(componentIdVersionedFromIdString(conceptValue).getTermComponentId());
			
			conceptValue = getNextCurrentRowToken();
			part.setC2id(componentIdVersionedFromIdString(conceptValue).getTermComponentId());
			
			conceptValue = getNextCurrentRowToken();
			part.setC3id(componentIdVersionedFromIdString(conceptValue).getTermComponentId());

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
