/**
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
package org.ihtsdo.translation.tasks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;


/**
 * The Class TranslationWorkflowInit.
 */
@BeanList(specs = { @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN) })
public class TranslationWorkflowInit extends AbstractTask {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;
	
	/** The fully_specified_description_type_aux. */
	private I_GetConceptData fully_specified_description_type_aux;
	
	/** The preferred_description_type_aux. */
	private I_GetConceptData preferred_description_type_aux;
	
	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		// Nothing to do...

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {
//			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
//			.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

//			Replaced previous line with next line to force active config use
			
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config =termFactory.getActiveAceFrameConfig();
			I_GetConceptData concept = config.getHierarchySelection();

			I_GetConceptData refsetsParent = termFactory
			.getConcept(new UUID[] {UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da")});
			
			if (refsetsParent.isParentOf(concept)) {
				process.writeAttachment("DLG_MSG", "True");
			} else {
				process.writeAttachment("DLG_MSG", "False");
			}
			
			fully_specified_description_type_aux = termFactory
			.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
					.getUids());
			
			preferred_description_type_aux = termFactory
			.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			
			/*Iterator<I_GetConceptData> conceptIterator = termFactory.getConceptIterator();
			
			while (conceptIterator.hasNext()) {
				I_GetConceptData loopConcept = conceptIterator.next();*/
				I_GetConceptData loopConcept = concept;
				int statusId;
				String comment;
				
				if (isConceptTranslated(loopConcept)) {
					process.writeAttachment("DLG_MSG", "Traducido!");
					comment = "Traducido!";
					statusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.PROCESSED.getUids().iterator().next());
				} else {
					process.writeAttachment("DLG_MSG", "No traducido...");
					comment = "No traducido!";
					statusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FLAGGED_FOR_REVIEW.getUids().iterator().next());
				}
				addRefsetMetadata(loopConcept, statusId, comment);
			//}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}


		return Condition.CONTINUE;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {};
	}
	
	/**
	 * Checks if is concept translated.
	 * 
	 * @param concept the concept
	 * 
	 * @return true, if is concept translated
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public boolean isConceptTranslated(I_GetConceptData concept) throws IOException {
		boolean fsnTranslated = false;
		boolean preferredTermTranslated = false;
		long lastVersion = Long.MIN_VALUE;
		for (I_DescriptionVersioned<?> description : concept.getDescriptions()) {
			I_DescriptionPart lastPart = description.getMutableParts().iterator().next();
			for (I_DescriptionPart part : description.getMutableParts()) {
				if (part.getTime() > lastVersion) {
					lastPart = part;
					lastVersion = part.getTime();
				}
			}
			
			if (lastPart.getTypeNid() == fully_specified_description_type_aux.getConceptNid() &&
					lastPart.getLang().equals("es")) {
				fsnTranslated = true;
			}
			
			if (lastPart.getTypeNid() == preferred_description_type_aux.getConceptNid() &&
					lastPart.getLang().equals("es")) {
				preferredTermTranslated = true;
			}
		}
		return (true && fsnTranslated && preferredTermTranslated);
	}
	
	/**
	 * Adds the refset metadata.
	 * 
	 * @param concept the concept
	 * @param status the status
	 * @param comment the comment
	 * 
	 * @throws Exception the exception
	 */
	public void addRefsetMetadata(I_GetConceptData concept, int status, String comment) throws Exception {
		I_TermFactory termFactory = Terms.get();
		
		// CAMBIAR!! ESTE APUNTA AL PADRE DE LOS REFSETS Y DEBE APUNTAR AL REFSET DE LOS METADATOS DE TRADUCCION
		I_GetConceptData refset = termFactory.getConcept(new UUID[] {UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da")});

		List<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(concept.getConceptNid());
		int statusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.STATUS.getUids().iterator().next());
		int commentId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.XHTML_SYNONYM_DESC_TYPE.getUids().iterator().next());
		int timeStampId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.PROCESSED.getUids().iterator().next());

		int conceptConceptExtensionTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.getUids().iterator().next());
		int stringExtensionTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.getUids().iterator().next());
		int intExtensionTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids().iterator().next());
		I_ExtendByRef statusExtension = null;
		I_ExtendByRef commentExtension = null;
		I_ExtendByRef timeStampExtension = null;
		int memberId;
		int currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());
		I_GetConceptData editPath = termFactory.getConcept(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids());
		boolean newStatusPartNeeded = true;
		boolean newCommentPartNeeded = true;
		
		for (I_ExtendByRef extension : extensions) {
			if (extension.getRefsetId() == refset.getConceptNid()) {
				// Check if there is an status extension on this refset already and if it needs an update
				if (extension.getTypeNid() == conceptConceptExtensionTypeId) {
					I_ExtendByRefPartCidCid part = (I_ExtendByRefPartCidCid) getLastExtensionPart(extension.getMutableParts());
					if (part.getC1id() == statusId) {
						statusExtension = extension;
						if (part.getC2id() == status) {
							newStatusPartNeeded = false;
						} 
					}
				}
				// Check if there is an comments extension on this refset already and if it needs an update
				if (extension.getTypeNid() == stringExtensionTypeId) {
					I_ExtendByRefPartCidString part = (I_ExtendByRefPartCidString) getLastExtensionPart(extension.getMutableParts());
					if (part.getC1id() == commentId) {
						commentExtension = extension;
						if (part.getStringValue().equals(comment)) {
							newCommentPartNeeded = false;
						} 
					}
				}
				// Check if there is an timestamp extension
				if (extension.getTypeNid() == intExtensionTypeId) {
					I_ExtendByRefPartCidInt part = (I_ExtendByRefPartCidInt) getLastExtensionPart(extension.getMutableParts());
					if (part.getC1id() == timeStampId) {
						timeStampExtension = extension;
					}
				}
			}
		}
		
//		if (statusExtension == null) {
//				memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
//						ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
//						termFactory.getPaths(), Integer.MAX_VALUE);
//				statusExtension = termFactory.newExtension(refset.getConceptId(), memberId, concept.getConceptId(), conceptConceptExtensionTypeId);
//		}
//		if (newStatusPartNeeded) {
//			// Add extension part for Status
//			I_ExtendByRefPartCidCid conceptConceptPart = termFactory.newConceptConceptExtensionPart();
//			conceptConceptPart.setPathId(editPath.getConceptId());
//			conceptConceptPart.setStatus(currentStatusId);
//			conceptConceptPart.setVersion(Integer.MAX_VALUE);
//			conceptConceptPart.setC1id(statusId);
//			conceptConceptPart.setC2id(status);
//			statusExtension.addVersion(conceptConceptPart);
//			termFactory.addUncommitted(statusExtension); 
//		}
//		
//		// Add extension for Commentary
//		if (commentExtension == null) {
//			memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
//					ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
//					termFactory.getPaths(), Integer.MAX_VALUE);
//			commentExtension = termFactory.newExtension(refset.getConceptId(), memberId, concept.getConceptId(), stringExtensionTypeId);	
//		}
//		
//		if (newCommentPartNeeded) {
//			I_ThinExtByRefPartConceptString conceptStringPart = termFactory.newConceptStringExtensionPart();
//			conceptStringPart.setPathId(editPath.getConceptId());
//			conceptStringPart.setStatus(currentStatusId);
//			conceptStringPart.setVersion(Integer.MAX_VALUE);
//			conceptStringPart.setC1id(commentId);
//			conceptStringPart.setStr(comment);
//			commentExtension.addVersion(conceptStringPart);
//			termFactory.addUncommitted(commentExtension);
//		}
//		
//		// Add extension for Timestamp
//		if (timeStampExtension == null) {
//			memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
//					ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
//					termFactory.getPaths(), Integer.MAX_VALUE);
//			timeStampExtension = termFactory.newExtension(refset.getConceptId(), memberId, concept.getConceptId(), intExtensionTypeId);	
//		}
//		I_ThinExtByRefPartConceptInt conceptIntPart = termFactory.newConceptIntExtensionPart();
//		conceptIntPart.setPathId(editPath.getConceptId());
//		conceptIntPart.setStatus(currentStatusId);
//		conceptIntPart.setVersion(Integer.MAX_VALUE);
//		conceptIntPart.setConceptId(timeStampId);
//		conceptIntPart.setIntValue(termFactory.convertToThinVersion((new Date().getTime())));
//		timeStampExtension.addVersion(conceptIntPart);
//		termFactory.addUncommitted(timeStampExtension);
//		
//		termFactory.commit();

	}
	
	/**
	 * Gets the last extension part.
	 * 
	 * @param list the list
	 * 
	 * @return the last extension part
	 */
	public I_ExtendByRefPart getLastExtensionPart(List<? extends I_ExtendByRefPart> list) {
		I_ExtendByRefPart lastPart = null;
		int lastVersion = Integer.MIN_VALUE;
		for (I_ExtendByRefPart part : list) {
			if (part.getVersion() >= lastVersion) {
				lastPart = part;
			}
		}
		return lastPart;
	}

}
