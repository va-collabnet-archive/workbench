/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.refset;

import java.io.IOException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class AttributeValueRefset.
 */
public class AttributeValueRefset extends Refset {

    /**
     * Instantiates a new attribute value refset.
     *
     * @param refsetConcept the refset concept
     * @throws Exception the exception
     */
    public AttributeValueRefset(I_GetConceptData refsetConcept) throws Exception {
        super();
        this.refsetConcept = refsetConcept;
        this.refsetName = refsetConcept.toString();
        this.refsetId = refsetConcept.getConceptNid();
        termFactory = Terms.get();
    }

    /**
     * Gets the value.
     *
     * @param componentId the component id
     * @param key the key
     * @return the value
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public I_GetConceptData getValue(int componentId, int key) throws IOException, TerminologyException {
        //TODO: move config to parameter
        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
        for (I_ExtendByRef attributeValueMember : termFactory.getAllExtensionsForComponent(componentId, true)) {
            if (attributeValueMember.getRefsetId() == this.refsetId) {
                long lastVersion = Long.MIN_VALUE;
                I_ExtendByRefPartCidCid attributeValueExtensionPart = null;
                for (I_ExtendByRefVersion loopTuple : attributeValueMember.getTuples(
                        config.getConflictResolutionStrategy())) {
                    if (loopTuple.getTime() >= lastVersion) {
                        lastVersion = loopTuple.getTime();
                        attributeValueExtensionPart = (I_ExtendByRefPartCidCid) loopTuple.getMutablePart();
                    }
                }
                if (attributeValueExtensionPart.getC1id() == key) {
                    I_GetConceptData value = termFactory.getConcept(attributeValueExtensionPart.getC2id());
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Put value.
     *
     * @param componentId the component id
     * @param key the key
     * @param value the value
     * @throws Exception the exception
     */
    public void putValue(int componentId, int key, int value) throws Exception {
        //TODO: move config to parameter
        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
        boolean keyAlreadyPresent = false;
        for (I_ExtendByRef attributeValueMember : termFactory.getAllExtensionsForComponent(componentId)) {
            if (attributeValueMember.getRefsetId() == this.refsetId) {
                keyAlreadyPresent = true;
                long lastVersion = Long.MIN_VALUE;
                I_ExtendByRefPartCidCid attributeValueExtensionPart = null;
                for (I_ExtendByRefVersion loopTuple : attributeValueMember.getTuples(
                        config.getConflictResolutionStrategy())) {
                    if (loopTuple.getTime() >= lastVersion) {
                        lastVersion = loopTuple.getTime();
                        attributeValueExtensionPart = (I_ExtendByRefPartCidCid) loopTuple.getMutablePart();
                    }
                }
                if (attributeValueExtensionPart.getC1id() == key) {
                    for (PathBI editPath : config.getEditingPathSet()) {
                        I_ExtendByRefPartCidCid newAttributeValuePart = (I_ExtendByRefPartCidCid) 
                                attributeValueExtensionPart.makeAnalog(
                                    SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(),
                                    Long.MAX_VALUE,
                                    config.getDbConfig().getUserConcept().getNid(),
                                    config.getEditCoordinate().getModuleNid(),
                                    editPath.getConceptNid());
                        newAttributeValuePart.setC2id(value);
                        attributeValueMember.addVersion(newAttributeValuePart);
                    }
                    termFactory.addUncommittedNoChecks(attributeValueMember);
                    return;
                }
            }
        }

        if (!keyAlreadyPresent) {
            I_HelpRefsets refsetHelper = termFactory.getRefsetHelper(config);
            refsetHelper.newRefsetExtension(this.refsetId,
                    componentId, EConcept.REFSET_TYPES.CID_CID,
                    new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, key).with(
                    RefsetPropertyMap.REFSET_PROPERTY.CID_TWO, value), config);

            //termFactory.commit();
        }

        return;
    }
}
