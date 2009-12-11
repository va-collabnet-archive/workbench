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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.tapi.TerminologyException;

public class RelationshipValidator extends SimpleValidator {

    I_TermFactory termFactory;
	private Map<UUID, Integer> cache = new HashMap<UUID, Integer>();
    @Override
    protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf)
        throws IOException, TerminologyException {
        /*
         * The universal bean relationships must be converted and compared with a thin relationships
         * from the term factory.
         * This validator will return false if, for each relationship in the UniversalAceBean:
         *
         * 1. The number of starting source relationships doesn't equals the number of
         *          source relationships in term factory.
         * 2. There isn't a corresponding relationship in the database with the same
         *       relationship ID.
         * 3. There isn't a corresponding relationship in the database with the same source
         *       relationship ID.
         * 4. There isn't a corresponding relationship in the database with the same destination
         *       relationship ID.
         * 5. One of the starting relationships (relationships whose time is not Long.MAX_VALUE)
         *       doesn't match one of the thin relationships in term factory
         *
         */

        termFactory = tf;

        I_GetConceptData concept = tf.getConcept(tf.uuidToNative(bean.getId().getUIDs()));
        List<I_RelVersioned> databaseRelationships = concept.getSourceRels();
        List<UniversalAceRelationship> beanRelationships = bean.getSourceRels();
        
        if (databaseRelationships.size() != beanRelationships.size()) {
            System.out.println("number of relationships different");
            return false; // test 1
        }
        
        //take a copy of the list so that we can remove matches from it as we go - reduce the number of comparisons
        List<I_RelVersioned> databaseRelationshipsCopy = new ArrayList<I_RelVersioned>(databaseRelationships.size());
        for (I_RelVersioned relVersioned : databaseRelationships) {
        	databaseRelationshipsCopy.add(relVersioned);
		}
        
        Iterator<I_RelVersioned> databaseRelationshipIterator = databaseRelationshipsCopy.iterator();

        for (UniversalAceRelationship beanRelationship : beanRelationships) {

            boolean foundMatch = false;
            
        	int beanRelId = getNativeId(beanRelationship.getRelId());
			int beanC1Id = getNativeId(beanRelationship.getC1Id());
			int beanC2Id = getNativeId(beanRelationship.getC2Id());
			
            while (databaseRelationshipIterator.hasNext()) {
            	I_RelVersioned databaseRelationship = databaseRelationshipIterator.next();

				if (relationshipsEqual(beanRelationship, databaseRelationship, beanRelId, beanC1Id, beanC2Id)) {
            		foundMatch = true;
            		databaseRelationshipIterator.remove();
            		break;
            	}
            }

            if (!foundMatch) {
            	AceLog.getEditLog().info("Failed to find matching relationship");
                return false;
            }
        }
        return true;
    }

    public boolean relationshipsEqual(UniversalAceRelationship beanRelationship,
            I_RelVersioned databaseRelationship, int beanRelId, int beanC1Id, int beanC2Id)  throws IOException, TerminologyException {

        if (beanRelId != databaseRelationship.getRelId()) {
            //System.out.println("rel ids different");
            return false; // Test 2
        }

        if (beanC1Id != databaseRelationship.getC1Id()) {
            //System.out.println("c1 id diff");
            return false; // Test 3
         }

        if (beanC2Id != databaseRelationship.getC2Id()) {
            //System.out.println("c2 id diff");
            return false; // Test 4
        }

        for (UniversalAceRelationshipPart part : beanRelationship.getVersions()) {
            if (part.getTime() != Long.MAX_VALUE) {
                I_RelPart newPart = termFactory.newRelPart();
                newPart.setVersion(termFactory.convertToThinVersion(part.getTime()));
                newPart.setPathId(getNativeId(part.getPathId()));
                newPart.setCharacteristicId(getNativeId(part.getCharacteristicId()));
                newPart.setGroup(part.getGroup());
                newPart.setRefinabilityId(getNativeId(part.getRefinabilityId()));
                newPart.setRelTypeId(getNativeId(part.getRelTypeId()));
                newPart.setStatusId(getNativeId(part.getStatusId()));

                if (databaseRelationship.getVersions().contains(newPart) == false) {
                	AceLog.getEditLog().info("parts different");

                    AceLog.getEditLog().info(databaseRelationship.getVersions().toString());
                    AceLog.getEditLog().info("..........");
                    AceLog.getEditLog().info("new part: " + newPart);
                    return false; // Test 5
                }
            }
        }

        return true;
    }

	private int getNativeId(Collection<UUID> uuids)
			throws TerminologyException, IOException {

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
