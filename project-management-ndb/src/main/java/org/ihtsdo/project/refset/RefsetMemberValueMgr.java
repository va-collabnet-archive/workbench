package org.ihtsdo.project.refset;


import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.tk.api.PathBI;

public class RefsetMemberValueMgr {

	private I_TermFactory termFactory;
	private int refsetId;
	private I_ConfigAceFrame config;
	private int memberType;

	public RefsetMemberValueMgr(I_GetConceptData refsetConcept) throws Exception {
		this.refsetId = refsetConcept.getConceptNid();
		this.termFactory = Terms.get();
		config=termFactory.getActiveAceFrameConfig();
		memberType=termFactory.getId(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getNid();
	}
	
	public void putConceptMember(int conceptMemberId) throws Exception {

		boolean alreadyMember = false;
		Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(conceptMemberId);
		for (I_ExtendByRef extension : extensions) {
			if (extension.getRefsetId()==this.refsetId){
				alreadyMember = true;
				I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(extension);
				if (!TerminologyProjectDAO.isActive(lastPart.getStatusNid())) {
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPart part = (I_ExtendByRefPart) 
						lastPart.makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(extension);
//					termFactory.commit();
				}
			}
		}

		if (!alreadyMember) {
			RefsetPropertyMap bpm=new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, memberType);
			termFactory.getRefsetHelper(config).newRefsetExtension(
					this.refsetId, 
					conceptMemberId, 
					EConcept.REFSET_TYPES.CID, 
					bpm,
					config); 
			
			for (I_ExtendByRef extension : termFactory.getConcept(conceptMemberId).getExtensions()) {
				if (extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
					termFactory.addUncommittedNoChecks(extension);
//					termFactory.commit();
				}
			}		
		}
		return;
	}

	public void delConceptMember(int conceptMemberId) throws Exception {

		Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(
				conceptMemberId);
		for (I_ExtendByRef extension : extensions) {
			if (extension.getRefsetId() == this.refsetId) {
				I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(extension);
				for (PathBI editPath : config.getEditingPathSet()) {
					I_ExtendByRefPart part = (I_ExtendByRefPart) 
					lastPart.makeAnalog(
							ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
							editPath.getConceptNid(),
							Long.MAX_VALUE);
					extension.addVersion(part);
				}
				termFactory.addUncommittedNoChecks(extension);
//				termFactory.commit();
			}
		}	
		return;
	}
}
