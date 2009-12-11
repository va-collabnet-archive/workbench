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

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class BooleanRefsetHandler extends MemberRefsetHandler {
	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple, boolean sctid) throws TerminologyException, IOException {
		I_ThinExtByRefPartBoolean booleanPart = (I_ThinExtByRefPartBoolean) tuple.getPart();
		
		return super.formatRefsetLine(tf, tuple, sctid) + MemberRefsetHandler.FILE_DELIMITER
					+ (booleanPart.getValue() ? 1 : 0); // 1 for true, 0 for false
	}
	
	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefPart part, Integer memberId, int refsetId, int componentId, boolean sctId) throws TerminologyException, IOException {
		I_ThinExtByRefPartBoolean booleanPart = (I_ThinExtByRefPartBoolean) part;
		
		return super.formatRefsetLine(tf, part, memberId, refsetId, componentId, sctId) + MemberRefsetHandler.FILE_DELIMITER
					+ (booleanPart.getValue() ? 1 : 0); // 1 for true, 0 for false
	}

	@Override
	public String getHeaderLine() {
		return super.getHeaderLine() + MemberRefsetHandler.FILE_DELIMITER + "BOOLEAN_VALUE";
	}

	@Override
	protected I_ThinExtByRefPart processLine(String line) {
		I_ThinExtByRefPartBoolean part;
		try {
			
			I_ThinExtByRefVersioned versioned = getExtensionVersioned(line, RefsetAuxiliary.Concept.BOOLEAN_EXTENSION);
			
			part = getTermFactory().newBooleanExtensionPart();
			setGenericExtensionPartFields(part);

			String value = getNextCurrentRowToken();
			assert value.equals("0") || value.equals("1");
			
			part.setValue(value.equals("1") ? true : false);
			
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
