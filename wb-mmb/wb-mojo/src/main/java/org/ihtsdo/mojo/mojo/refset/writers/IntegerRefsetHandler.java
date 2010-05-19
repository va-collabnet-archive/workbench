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
package org.ihtsdo.mojo.mojo.refset.writers;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;

public class IntegerRefsetHandler extends MemberRefsetHandler {
	@Override
	public String formatRefsetLine(I_TermFactory tf,
			I_ExtendByRefVersion tuple, boolean sctid)
			throws TerminologyException, IOException {
		I_ExtendByRefPartInt integerPart = (I_ExtendByRefPartInt) tuple
				.getMutablePart();

		return super.formatRefsetLine(tf, tuple, sctid)
				+ MemberRefsetHandler.FILE_DELIMITER
				+ integerPart.getIntValue();
	}

	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ExtendByRefPart part,
			Integer memberId, int refsetId, int componentId, boolean sctId)
			throws TerminologyException, IOException {
		I_ExtendByRefPartInt intPart = (I_ExtendByRefPartInt) part;

		return super.formatRefsetLine(tf, part, memberId, refsetId,
				componentId, sctId)
				+ MemberRefsetHandler.FILE_DELIMITER + intPart.getIntValue();
	}

	@Override
	public String getHeaderLine() {
		return super.getHeaderLine() + MemberRefsetHandler.FILE_DELIMITER
				+ "INTEGER_VALUE";
	}

	// @Override
	// protected I_ExtendByRefPart processLine(String line) {
	// I_ExtendByRefPartInteger part;
	// try {
	//
	// I_ExtendByRef versioned = getExtensionVersioned(line,
	// RefsetAuxiliary.Concept.INT_EXTENSION);
	//
	// part = getTermFactory().newIntegerExtensionPart();
	// setGenericExtensionPartFields(part);
	//
	// part.setValue(Integer.parseInt(getNextCurrentRowToken()));
	//
	// versioned.addVersion(part);
	//
	// if (isTransactional()) {
	// getTermFactory().addUncommitted(versioned);
	// } else {
	// getTermFactory().getDirectInterface().writeExt(versioned);
	// }
	//
	// } catch (Exception e) {
	// throw new RuntimeException("Error occred processing file " + sourceFile,
	// e);
	// }
	//
	// return part;
	// }

}
