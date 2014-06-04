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
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.query.helper.RefsetHelper;

/**
 * The Class PromotionRefset.
 */
public class PromotionRefset extends Refset {

    /**
     * The active value nid.
     */
    int activeValueNid;

    /**
     * Instantiates a new promotion refset.
     *
     * @param refsetConcept the refset concept
     * @throws Exception the exception
     */
    public PromotionRefset(I_GetConceptData refsetConcept) throws Exception {
        super();
        //TODO: validate if refsetConcept is promotion refset?
        this.refsetConcept = refsetConcept;
        this.refsetName = refsetConcept.toString();
        this.refsetId = refsetConcept.getConceptNid();
        this.termFactory = Terms.get();
        this.activeValueNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
    }

    /**
     * Gets the last promotion tuple.
     *
     * @param componentId the component id
     * @param config the config
     * @return the last promotion tuple
     * @throws TerminologyException the terminology exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public RefexVersionBI getLastPromotionTuple(int componentId, I_ConfigAceFrame config) throws TerminologyException, IOException {
        I_GetConceptData component = termFactory.getConcept(componentId);
        Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
        for (RefexChronicleBI<?> promotionMember : members) {
            if (promotionMember.getRefexNid() == this.refsetId) {
                try {
                    return promotionMember.getVersion(config.getViewCoordinate());
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        }
        return null;
    }

    /**
     * Gets the promotion status.
     *
     * @param componentId the component id
     * @param config the config
     * @return the promotion status
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public I_GetConceptData getPromotionStatus(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
        RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
        if (lastTuple == null) {
            return null;
        } else {
            RefexNidVersionBI promotionExtensionPart = (RefexNidVersionBI) lastTuple;
            return termFactory.getConcept(promotionExtensionPart.getNid1());
        }
    }

    /**
     * Gets the last status time.
     *
     * @param componentId the component id
     * @param config the config
     * @return the last status time
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public Long getLastStatusTime(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
        RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
        if (lastTuple == null) {
            return null;
        } else {
            RefexNidVersionBI promotionExtensionPart = (RefexNidVersionBI) lastTuple;
            return promotionExtensionPart.getTime();
        }
    }

    /**
     * Gets the last promotion author.
     *
     * @param componentId the component id
     * @param config the config
     * @return the last promotion author
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public I_GetConceptData getLastPromotionAuthor(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
        RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
        if (lastTuple == null) {
            return null;
        } else {
            RefexNidVersionBI promotionExtensionPart = (RefexNidVersionBI) lastTuple;
            return termFactory.getConcept(promotionExtensionPart.getAuthorNid());
        }
    }

    /**
     * Gets the previous promotion status.
     *
     * @param componentId the component id
     * @param config the config
     * @return the previous promotion status
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public I_GetConceptData getPreviousPromotionStatus(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
        I_GetConceptData component = termFactory.getConcept(componentId);
        Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
        for (RefexChronicleBI<?> promotionMember : members) {
            if (promotionMember.getRefexNid() == this.refsetId) {
                try {
                    RefexNidVersionBI promotionExtensionPart = (RefexNidVersionBI) promotionMember.getVersion(config.getViewCoordinate());
                    return termFactory.getConcept(promotionExtensionPart.getNid1());
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        }
        return null;
    }

    /**
     * Gets the previous status time.
     *
     * @param componentId the component id
     * @param config the config
     * @return the previous status time
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public Long getPreviousStatusTime(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
        I_GetConceptData component = termFactory.getConcept(componentId);
        Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
        Long lastStatusTime = getLastStatusTime(componentId, config);
        for (RefexChronicleBI<?> promotionMember : members) {
            if (promotionMember.getRefexNid() == this.refsetId) {
                Collection<? extends RefexVersionBI> loopParts = promotionMember.getVersions(config.getViewCoordinate());
                long lastVersion = Long.MIN_VALUE;
                RefexNidVersionBI promotionExtensionPart = null;
                Collection<? extends RefexVersionBI> versions = promotionMember.getVersions(config.getViewCoordinate());

                long previousToLastVersion = Long.MIN_VALUE;
                for (RefexVersionBI loopPart : loopParts) {
                    if (loopPart.getTime() >= previousToLastVersion && loopPart.getTime() < lastStatusTime) {
                        previousToLastVersion = loopPart.getTime();
                        promotionExtensionPart = (RefexNidVersionBI) loopPart;
                    }
                }
                if (promotionExtensionPart != null) {
                    return promotionExtensionPart.getTime();
                }
            }
        }
        return null;
    }

    /**
     * Sets the promotion status.
     *
     * @param componentId the component id
     * @param statusConceptId the status concept id
     * @throws Exception the exception
     */
    public void setPromotionStatus(int componentId, int statusConceptId) throws Exception {
        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
        TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                config.getViewCoordinate());

        I_GetConceptData component = termFactory.getConcept(componentId);
        boolean statusAlreadyPresent = false;
        RefexVersionBI oldStatus = getLastPromotionTuple(componentId, config);
        if (oldStatus != null) {
            RefexNidVersionBI oldStatusCnid = (RefexNidVersionBI) oldStatus;
            if (oldStatusCnid.getNid1() != statusConceptId) {
                I_ExtendByRef oldExtension = termFactory.getExtension(oldStatus.getNid());
                long lastVersion = Long.MIN_VALUE;
                I_ExtendByRefPartCid promotionStatusExtensionPart = null;
                List<? extends I_ExtendByRefPart> loopParts = oldExtension.getMutableParts();
                for (I_ExtendByRefPart loopPart : loopParts) {
                    if (loopPart.getTime() >= lastVersion) {
                        lastVersion = loopPart.getTime();
                        promotionStatusExtensionPart = (I_ExtendByRefPartCid) loopPart;
                    }
                }
                for (PathBI editPath : config.getEditingPathSet()) {
                    I_ExtendByRefPartCid newPromotionStatusPart = (I_ExtendByRefPartCid) promotionStatusExtensionPart.makeAnalog(
                            SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(),
                            Long.MAX_VALUE,
                            config.getDbConfig().getUserConcept().getNid(),
                            config.getEditCoordinate().getModuleNid(),
                            editPath.getConceptNid());
                    newPromotionStatusPart.setC1id(statusConceptId);
                    oldExtension.addVersion(newPromotionStatusPart);
                }
                //termFactory.addUncommittedNoChecks(refsetConcept);
                termFactory.addUncommittedNoChecks(component);


//				for (PathBI editPath : config.getEditingPathSet()) {
//					RefexNidAnalogBI newVersion = 
//						(RefexNidAnalogBI) oldStatusCnid.makeAnalog(activeValueNid,
//								config.getDbConfig().getUserConcept().getNid(),
//								editPath.getConceptNid(), 
//								Long.MAX_VALUE);
//					newVersion.setCnid1(statusConceptId);
//					oldStatus.getChronicle().getVersions().add(newVersion);
//				}
//				termFactory.addUncommittedNoChecks(component);
            }
        } else {
            I_GetConceptData newMemberConcept = termFactory.getConcept(componentId);
            RefsetHelper helper = new RefsetHelper(config.getViewCoordinate(), config.getEditCoordinate());
            helper.newConceptRefsetExtension(refsetId, componentId, statusConceptId);

            //termFactory.addUncommittedNoChecks(refsetConcept);
            termFactory.addUncommittedNoChecks(newMemberConcept);
//			RefexCAB newSpec = new RefexCAB(
//					TK_REFEX_TYPE.CID,
//					componentId,
//					refsetId);
//			newSpec.put(RefexProperty.CNID1, statusConceptId);
//			RefexChronicleBI<?> newRefex = tc.construct(newSpec);
//			termFactory.addUncommittedNoChecks(component);
        }

        return;
    }
}
