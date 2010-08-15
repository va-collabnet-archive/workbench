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
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.tapi.TerminologyException;

public class DescriptionValidator extends SimpleValidator {

    private boolean timeLenient = false;
    private StringBuffer failureReport;

    @Override
    protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf) throws IOException, TerminologyException {
        termFactory = tf;
        failureReport = new StringBuffer();

        /*
         * The universal bean descriptions must be converted and compared with a
         * thin descriptions from the term factory. This validator will return
         * false if, for each description in the UniverasalAceBean: 1. The
         * concept ids are not equal 2. One of the starting descriptions
         * (descriptions whose time is not Long.MAX_VALUE) 3. The number of
         * starting descriptions equals the number of descriptions
         */
        for (UniversalAceDescription desc : bean.getDescriptions()) {
            Set<I_DescriptionPart> startParts = new HashSet<I_DescriptionPart>();
            I_DescriptionVersioned thinDesc = tf.getDescription(getNativeId(desc.getDescId()),
                getNativeId(desc.getConceptNid()));
            if (thinDesc.getConceptNid() != getNativeId(desc.getConceptNid())) {
                failureReport.append("description concept ids don't match " + thinDesc + " and " + desc);
                return false; // Test 1
            }
            for (UniversalAceDescriptionPart part : desc.getVersions()) {
                if (part.getTime() != Long.MAX_VALUE) {
                    I_DescriptionPart newPart = tf.newDescriptionPart();
                    newPart.setInitialCaseSignificant(part.getInitialCaseSignificant());
                    newPart.setLang(part.getLang());
                    newPart.setPathId(getNativeId(part.getPathId()));
                    newPart.setStatusId(getNativeId(part.getStatusId()));
                    newPart.setText(part.getText());
                    newPart.setTypeId(getNativeId(part.getTypeId()));
                    newPart.setTime(part.getTime());

                    startParts.add(newPart);

                    if (!containsPart(thinDesc, newPart)) {
                        failureReport.append("concept does not contain a description part match.");
                        failureReport.append("\n       newPart: " + newPart);
                        for (I_DescriptionPart descPart : thinDesc.getMutableParts()) {
                            failureReport.append("\n existing part: " + descPart);
                        }
                        failureReport.append("\n\n");
                        return false; // test 2
                    }
                }
            }
            if (startParts.size() != thinDesc.getMutableParts().size()) {
                failureReport.append("number of concept attribute parts is different for " + bean + " and "
                    + thinDesc.getMutableParts());
                return false; // test 3
            }
        }

        // passed all tests for all descriptions
        return true;
    }

    private boolean containsPart(I_DescriptionVersioned thinDesc, I_DescriptionPart newPart) {
        if (!timeLenient) {
            return thinDesc.getMutableParts().contains(newPart);
        } else {
            boolean match = false;
            for (I_DescriptionPart descriptionPart : thinDesc.getMutableParts()) {
                if (descriptionPart.isInitialCaseSignificant() == newPart.isInitialCaseSignificant()
                    && descriptionPart.getPathId() == newPart.getPathId()
                    && descriptionPart.getStatusId() == newPart.getStatusId()
                    && descriptionPart.getTypeId() == newPart.getTypeId()
                    && descriptionPart.getText().equals(descriptionPart.getText())
                    && descriptionPart.getLang().equals(newPart.getLang())) {

                    // found a match, no need to keep looking
                    match = true;
                    break;
                }
            }

            return match;
        }
    }

    @Override
    public String getFailureReport() {
        return failureReport.toString();
    }

    public boolean isTimeLenient() {
        return timeLenient;
    }

    public void setTimeLenient(boolean timeLenient) {
        this.timeLenient = timeLenient;
    }

}
