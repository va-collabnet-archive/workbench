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
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.tapi.TerminologyException;

public class RelationshipValidator extends SimpleValidator {

    boolean timeLenient = false;
    private StringBuffer failureReport;

    @Override
    protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf) throws IOException, TerminologyException {
        /*
         * The universal bean relationships must be converted and compared with
         * a thin relationships
         * from the term factory.
         * This validator will return false if, for each relationship in the
         * UniversalAceBean:
         * 
         * 1. The number of starting source relationships doesn't equals the
         * number of
         * source relationships in term factory.
         * 2. There isn't a corresponding relationship in the database with the
         * same
         * relationship ID.
         * 3. There isn't a corresponding relationship in the database with the
         * same source
         * relationship ID.
         * 4. There isn't a corresponding relationship in the database with the
         * same destination
         * relationship ID.
         * 5. One of the starting relationships (relationships whose time is not
         * Long.MAX_VALUE)
         * doesn't match one of the thin relationships in term factory
         */

        termFactory = tf;
        failureReport = new StringBuffer();

        I_GetConceptData concept = tf.getConcept(tf.uuidToNative(bean.getId().getUIDs()));
        List<? extends I_RelVersioned> databaseRelationships = concept.getSourceRels();
        List<UniversalAceRelationship> beanRelationships = bean.getSourceRels();

        if (databaseRelationships.size() > beanRelationships.size()) {
            failureReport.append("number of relationship doesn't match " + databaseRelationships + " and "
                + beanRelationships + "\nfor change bean " + bean);
            return false; // test 1
        }

        int relationshipMismatches = 0;
        for (UniversalAceRelationship beanRelationship : beanRelationships) {

            boolean foundMatch = false;

            int beanRelId = getNativeId(beanRelationship.getRelId());
            int beanC1Id = getNativeId(beanRelationship.getC1Id());
            int beanC2Id = getNativeId(beanRelationship.getC2Id());

            for (I_RelVersioned databaseRelationship : databaseRelationships) {
                if (relationshipsEqual(beanRelationship, databaseRelationship, beanRelId, beanC1Id, beanC2Id)) {
                    foundMatch = true;
                    break;
                }
            }

            if (!foundMatch) {
                failureReport.append("Failed to find matching relationship for \n   " + beanRelationship);
                for (I_RelVersioned databaseRelationship : databaseRelationships) {
                    failureReport.append("\n   " + databaseRelationship);
                }
                relationshipMismatches++;
                failureReport.append("\nfor bean\n\n" + bean);
                failureReport.append("\n\n------\n\n");
            }
        }
        // if the database has the same number less versions than we found
        // matches, then it is OK
        return relationshipMismatches == beanRelationships.size() - databaseRelationships.size();
    }

    public boolean relationshipsEqual(UniversalAceRelationship beanRelationship, I_RelVersioned databaseRelationship,
            int beanRelId, int beanC1Id, int beanC2Id) throws IOException, TerminologyException {

        if (beanRelId != databaseRelationship.getRelId()) {
            return false; // Test 2
        }

        if (beanC1Id != databaseRelationship.getC1Id()) {
            failureReport.append("Relationship ids equal, but c1 ids unequal: " + beanC1Id + " id in database: "
                + databaseRelationship.getC1Id());
            return false; // Test 3
        }

        if (beanC2Id != databaseRelationship.getC2Id()) {
            failureReport.append("Relationship ids equal, but c2 ids unequal: " + beanC2Id + " id in database: "
                + databaseRelationship.getC2Id());
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

                if (containsPart(databaseRelationship, newPart) == false) {

                    failureReport.append("concept does not contain a relationship part match. relId: ");
                    failureReport.append(beanRelId);
                    failureReport.append(" c1id: ");
                    failureReport.append(beanC2Id);
                    failureReport.append(" c2id: ");
                    failureReport.append(beanC2Id);
                    failureReport.append("\n newPart: " + newPart);
                    for (I_RelPart repPart : databaseRelationship.getVersions()) {
                        failureReport.append("\nexisting: " + repPart);
                    }
                    failureReport.append("\n\n");
                    return false; // Test 5
                }
            }
        }

        return true;
    }

    public String getFailureReport() {
        return failureReport.toString();
    }

    private boolean containsPart(I_RelVersioned databaseRelationship, I_RelPart newPart) {
        if (!timeLenient) {
            return databaseRelationship.getVersions().contains(newPart);
        } else {
            boolean match = false;
            for (I_RelPart relPart : databaseRelationship.getVersions()) {
                if (relPart.getPathId() == newPart.getPathId()
                    && relPart.getCharacteristicId() == newPart.getCharacteristicId()
                    && relPart.getGroup() == newPart.getGroup()
                    && relPart.getRefinabilityId() == newPart.getRefinabilityId()
                    && relPart.getRelTypeId() == newPart.getRelTypeId()
                    && relPart.getStatusId() == newPart.getStatusId()) {

                    // found a match, no need to keep looking
                    match = true;
                    break;
                }
            }

            return match;
        }
    }

    public boolean isTimeLenient() {
        return timeLenient;
    }

    public void setTimeLenient(boolean timeLenient) {
        this.timeLenient = timeLenient;
    }

}
