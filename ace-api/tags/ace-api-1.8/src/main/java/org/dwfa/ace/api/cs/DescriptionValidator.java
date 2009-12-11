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
package org.dwfa.ace.api.cs;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.tapi.TerminologyException;

public class DescriptionValidator extends SimpleValidator {

	private I_TermFactory termFactory;
	private Map<UUID, Integer> cache = new HashMap<UUID, Integer>();
	
	@Override
	protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf)
			throws IOException, TerminologyException {
		termFactory = tf;
		
		/*
		 * The universal bean descriptions must be converted and compared with a
		 * thin descriptios from the term factory. This validator will return
		 * false if, for each description in the UniverasalAceBean: 1. The
		 * concept ids are not equal 2. One of the starting descriptions
		 * (descriptions whose time is not Long.MAX_VALUE) 3. The number of
		 * starting descriptions equals the number of descriptions
		 */
		for (UniversalAceDescription desc : bean.getDescriptions()) {
			Set<I_DescriptionPart> startParts = new HashSet<I_DescriptionPart>();
			I_DescriptionVersioned thinDesc = tf.getDescription(getNativeId(desc.getDescId()), getNativeId(desc.getConceptId()));
			if (thinDesc.getConceptId() != getNativeId(desc.getConceptId())) {
				return false; // Test 1
			}
			for (UniversalAceDescriptionPart part : desc.getVersions()) {
				if (part.getTime() != Long.MAX_VALUE) {
					I_DescriptionPart newPart = tf.newDescriptionPart();
					newPart.setInitialCaseSignificant(part
							.getInitialCaseSignificant());
					newPart.setLang(part.getLang());
					newPart.setPathId(getNativeId(part.getPathId()));
					newPart.setStatusId(getNativeId(part.getStatusId()));
					newPart.setText(part.getText());
					newPart.setTypeId(getNativeId(part.getTypeId()));
					newPart.setVersion(tf.convertToThinVersion(part.getTime()));

					startParts.add(newPart);
					if (thinDesc.getVersions().contains(newPart) == false) {
						return false; // test 2
					}
				}
			}
			if (startParts.size() != thinDesc.getVersions().size()) {
				System.out.println("number of description parts is different");
				return false; // test 3
			}
		}

		// passed all tests for all descriptions
		return true;
	}

	private int getNativeId(Collection<UUID> uuids)	throws TerminologyException, IOException {

		Integer cacheValue = null;
		Iterator<UUID> uuidsIterator = uuids.iterator();
		while (cacheValue == null && uuidsIterator.hasNext()) {
			cacheValue = cache.get(uuidsIterator.next());
		}

		if (cacheValue == null) {
			cacheValue = termFactory.uuidToNative(uuids);
			for (UUID uuid : uuids) {
				cache.put(uuid, cacheValue);
			}
		}

		return cacheValue;
	}

}
