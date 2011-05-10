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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.swing.JButton;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;


/**
 * The Class TermmedUtils.
 * @deprecated 
 */
public class TermmedUtils 
{
	
	/** The vodb directory. */
	static File vodbDirectory;
	
	/** The read only. */
	static boolean readOnly = false;
	
	/** The cache size. */
	static Long cacheSize = Long.getLong("600000000");
	
	/** The db setup config. */
	private static DatabaseSetupConfig dbSetupConfig;

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * 
	 * @throws Exception the exception
	 */
	public static void main( String[] args ) throws Exception
	{
		System.out.println( "Attempting connection..." );
		vodbDirectory = new File("/Users/alo/Documents/TermMed/Eclipse/MrcmMaven/miniSct-ide-sa/target/sct-wb-ide-sa-bundle.dir/berkeley-db");
		if (LocalVersionedTerminology.get() != null) {
			return;
		}

		if (dbSetupConfig == null) {
			dbSetupConfig = new DatabaseSetupConfig();
		}
		try {
			Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);

			I_TermFactory termFactory = Terms.get();
			I_GetConceptData concept = termFactory.getConcept(
					new UUID[] {UUID.fromString("c7243365-510d-3e5f-82b3-7286b27d7698")});
			System.out.println("Looking extensions for concept: " + getFSN(concept, termFactory));
			
			//int status2 = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.ADJUDICATED.getUids().iterator().next());
			int status2 = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.AMBIGUOUS.getUids().iterator().next());

			//addRefsetMetadata(concept, status2, "Test 23");
			
			List<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(concept.getConceptNid());
			
			int conceptConceptExtensionTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.getUids().iterator().next());
			int stringExtensionTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.getUids().iterator().next());
			int intExtensionTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids().iterator().next());
			
			for (I_ExtendByRef extension : extensions) {
				I_GetConceptData refset = termFactory.getConcept(extension.getRefsetId());
				I_ExtendByRefPart lastPart = getLastExtensionPart(extension.getMutableParts());
				if (extension.getTypeNid() == conceptConceptExtensionTypeId) {
					I_ExtendByRefPartCidCid conceptPart = (I_ExtendByRefPartCidCid) lastPart; 
					I_GetConceptData concept1 = termFactory.getConcept(conceptPart.getC1id());
					I_GetConceptData concept2 = termFactory.getConcept(conceptPart.getC2id());
					System.out.println("Refset: " + getFSN(refset, termFactory) + " - Concept1: " + getFSN(concept1, termFactory)
							 + " - Concept2: " + getFSN(concept2, termFactory) + " Versions: " + extension.getMutableParts().size());
				}
				if (extension.getTypeNid() == stringExtensionTypeId) {
					I_ExtendByRefPartCidString conceptPart = (I_ExtendByRefPartCidString) lastPart; 
					I_GetConceptData concept1 = termFactory.getConcept(conceptPart.getC1id());
					System.out.println("Comments: " + getFSN(refset, termFactory) + " - Concept1: " + getFSN(concept1, termFactory)
							 + " - Text: " + conceptPart.getStringValue() + " Versions: " + extension.getMutableParts().size());
				}
				if (extension.getTypeNid() == intExtensionTypeId) {
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					I_ExtendByRefPartCidInt conceptPart = (I_ExtendByRefPartCidInt) lastPart; 
					I_GetConceptData concept1 = termFactory.getConcept(conceptPart.getC1id());
					System.out.println("Refset: " + getFSN(refset, termFactory) + " - Concept1: " + getFSN(concept1, termFactory)
							 + " - Date: " + dateFormat.format(new Date(termFactory.convertToThickVersion(conceptPart.getIntValue())))
							 + " Versions: " + extension.getMutableParts().size());
				}
			}
			
			System.out.println( "Closing..." );
			I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) Terms.get();
			termFactoryImpl.close();
			System.out.println( "Connection closed" );
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		
		System.exit(0);

	}
	
	/**
	 * Gets the last extension part.
	 * 
	 * @param list the list
	 * 
	 * @return the last extension part
	 */
	public static I_ExtendByRefPart getLastExtensionPart(List<? extends I_ExtendByRefPart> list) {
		I_ExtendByRefPart lastPart = null;
		int lastVersion = Integer.MIN_VALUE;
		for (I_ExtendByRefPart part : list) {
			if (part.getVersion() >= lastVersion) {
				lastPart = part;
			}
		}
		return lastPart;
	}
	
	/**
	 * Gets the fSN.
	 * 
	 * @param concept the concept
	 * @param termFactory the term factory
	 * 
	 * @return the fSN
	 * 
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getFSN(I_GetConceptData concept, I_TermFactory termFactory) throws TerminologyException, IOException {
		int lastVersion = Integer.MIN_VALUE;
		String fsn = "";
		int fullyId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids().iterator().next());
		for (I_DescriptionVersioned<?> description : concept.getDescriptions()) {
			for (I_DescriptionPart part : description.getMutableParts()) {
				if (part.getTypeNid() == fullyId && part.getVersion() > lastVersion && !part.getText().contains("metamodel")) {
					fsn = part.getText();
				}
			}
		}
		
		return fsn;
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
	public static void addRefsetMetadata(I_GetConceptData concept, int status, String comment) throws Exception {
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
				System.out.println(extension.getTypeNid() + "|" + getFSN(termFactory.getConcept(extension.getTypeNid()), termFactory)
						+ " - C:" + conceptConceptExtensionTypeId+ "|" + getFSN(termFactory.getConcept(conceptConceptExtensionTypeId), termFactory)
						+ " - S:" + stringExtensionTypeId+ "|" + getFSN(termFactory.getConcept(stringExtensionTypeId), termFactory)
						+ " - I:" + intExtensionTypeId+ "|" + getFSN(termFactory.getConcept(intExtensionTypeId), termFactory));
				
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
					System.out.println("Int ext found!!");
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
//				System.out.println("New CC extension:" + refset.getConceptId() + "|"+ memberId + "|" + concept.getConceptId()+ "|" + conceptConceptExtensionTypeId);
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
//			termFactory.commit();
//		}
//		
//		// Add extension for Commentary
//		if (commentExtension == null) {
//			memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
//					ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
//					termFactory.getPaths(), Integer.MAX_VALUE);
//			commentExtension = termFactory.newExtension(refset.getConceptId(), memberId, concept.getConceptId(), stringExtensionTypeId);
//			System.out.println("New S extension:" + refset.getConceptId() + "|"+ memberId + "|" + concept.getConceptId()+ "|" + stringExtensionTypeId);
//		}
//		
//		if (newCommentPartNeeded) {
//			I_ExtendByRefPartCidString conceptStringPart = termFactory.newConceptStringExtensionPart();
//			conceptStringPart.setPathId(editPath.getConceptId());
//			conceptStringPart.setStatus(currentStatusId);
//			conceptStringPart.setVersion(Integer.MAX_VALUE);
//			conceptStringPart.setC1id(commentId);
//			conceptStringPart.setStr(comment);
//			commentExtension.addVersion(conceptStringPart);
//			termFactory.addUncommitted(commentExtension);
//			termFactory.commit();
//		}
//		
//		// Add extension for Timestamp
//		if (timeStampExtension == null) {
//			memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
//					ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
//					termFactory.getPaths(), Integer.MAX_VALUE);
//			timeStampExtension = termFactory.newExtension(refset.getConceptId(), memberId, concept.getConceptId(), intExtensionTypeId);	
//			System.out.println("New I extension:" + refset.getConceptId() + "|"+ memberId + "|" + concept.getConceptId()+ "|" + intExtensionTypeId);
//		}
//		I_ExtendByRefPartCidInt conceptIntPart = termFactory.newConceptIntExtensionPart();
//		conceptIntPart.setPathId(editPath.getConceptId());
//		conceptIntPart.setStatus(currentStatusId);
//		conceptIntPart.setVersion(Integer.MAX_VALUE);
//		conceptIntPart.setConceptId(timeStampId);
//		conceptIntPart.setIntValue(termFactory.convertToThinVersion((new Date().getTime())));
//		timeStampExtension.addVersion(conceptIntPart);
//		termFactory.addUncommitted(timeStampExtension);
//		termFactory.commit();

	}
	
}
