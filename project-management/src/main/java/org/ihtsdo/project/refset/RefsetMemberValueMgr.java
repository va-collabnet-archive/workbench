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

import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.query.helper.RefsetHelper;

/**
 * The Class RefsetMemberValueMgr.
 */
public class RefsetMemberValueMgr {

    /**
     * The term factory.
     */
    private I_TermFactory termFactory;
    /**
     * The refset id.
     */
    private int refsetId;
    /**
     * The config.
     */
    private I_ConfigAceFrame config;
    /**
     * The member type.
     */
    private int memberType;
    /**
     * The refset concept.
     */
    private I_GetConceptData refsetConcept;

    /**
     * Instantiates a new refset member value mgr.
     *
     * @param refsetConcept the refset concept
     * @throws Exception the exception
     */
    public RefsetMemberValueMgr(I_GetConceptData refsetConcept) throws Exception {
        this.refsetId = refsetConcept.getConceptNid();
        this.termFactory = Terms.get();
        config = termFactory.getActiveAceFrameConfig();
        memberType = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
        this.refsetConcept = refsetConcept;
    }

    /**
     * Put concept member.
     *
     * @param conceptMemberId the concept member id
     * @throws Exception the exception
     */
    public void putConceptMember(int conceptMemberId) throws Exception {

        boolean alreadyMember = false;
        Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(conceptMemberId);
        for (I_ExtendByRef extension : extensions) {
            if (extension.getRefsetId() == this.refsetId) {
                alreadyMember = true;
                I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(extension);
                if (!TerminologyProjectDAO.isActive(lastPart.getStatusNid())) {
                    for (PathBI editPath : config.getEditingPathSet()) {
                        I_ExtendByRefPart part = (I_ExtendByRefPart) lastPart.makeAnalog(
                                SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(),
                                Long.MAX_VALUE,
                                config.getDbConfig().getUserConcept().getNid(),
                                config.getEditCoordinate().getModuleNid(),
                                editPath.getConceptNid());
                        extension.addVersion(part);
                    }
                    termFactory.addUncommittedNoChecks(extension);
                    termFactory.addUncommitted(refsetConcept);
//					termFactory.commit();
                }
            }
        }

        if (!alreadyMember) {
            RefsetHelper helper = new RefsetHelper(config.getViewCoordinate(), config.getEditCoordinate());
            helper.newConceptRefsetExtension(refsetId, conceptMemberId, memberType);

            for (I_ExtendByRef extension : termFactory.getRefsetExtensionMembers(refsetConcept.getConceptNid())) {
                if (extension.getComponentNid() == conceptMemberId
                        && extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
                    termFactory.addUncommittedNoChecks(refsetConcept);
                    termFactory.addUncommittedNoChecks(extension);
                }
            }
//			for (I_ExtendByRef extension : termFactory.getConcept(conceptMemberId).getExtensions()) {
//				if (extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
//					termFactory.addUncommittedNoChecks(extension);
//					termFactory.addUncommitted(refsetConcept);
//				}
//			}		
        }
        return;
    }

    /**
     * Del concept member.
     *
     * @param conceptMemberId the concept member id
     * @throws Exception the exception
     */
    public void delConceptMember(int conceptMemberId) throws Exception {

        Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(
                conceptMemberId);
        for (I_ExtendByRef extension : extensions) {
            if (extension.getRefsetId() == this.refsetId) {
                I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(extension);
                for (PathBI editPath : config.getEditingPathSet()) {
                    I_ExtendByRefPart part = (I_ExtendByRefPart) lastPart.makeAnalog(
                            SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(),
                            Long.MAX_VALUE,
                            config.getDbConfig().getUserConcept().getNid(),
                            config.getEditCoordinate().getModuleNid(),
                            editPath.getConceptNid());
                    extension.addVersion(part);
                }
                termFactory.addUncommittedNoChecks(refsetConcept);
                termFactory.addUncommittedNoChecks(extension);
//				termFactory.commit();
            }
        }
        return;
    }
}
