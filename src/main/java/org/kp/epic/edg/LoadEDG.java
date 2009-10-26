package org.kp.epic.edg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
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
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.util.id.Type5UuidFactory;

@BeanList(specs = { @Spec(directory = "tasks/kp/edg", type = BeanType.TASK_BEAN) })
public class LoadEDG extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private String inputFilePropName = "A: INPUT_FILE";

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(inputFilePropName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			inputFilePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do
	}

	private static final int SNOMED_ID_FIELD = 0;
	private static final int DESCRIPTION_FIELD = 1;
	private static final int DESCRIPTION_TYPE_FIELD = 2;
	private static final int REFSET_NAME_FIELD = 3;
	private static final int DESCRIPTION_STATUS_FIELD = 4;
	private static final int REFSET_REASON_FOR_SOFT_DELETE_FIELD = 5;
	private static final int DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD = 6;
	private static final int DESCRIPTION_EXTERNAL_DOT1_FIELD = 7;
	private static final int DESCRIPTION_EXTERNAL_ITEM_11_FIELD = 8;
	private static final int DESCRIPTION_EXTERNAL_ITEM_40_FIELD = 9;
	private static final int DESCRIPTION_EXTERNAL_ITEM_100_FIELD = 10;
	private static final int DESCRIPTION_EXTERNAL_ITEM_200_FIELD = 11;
	private static final int DESCRIPTION_EXTERNAL_ITEM_207_FIELD = 12;
	private static final int DESCRIPTION_EXTERNAL_ITEM_2000_FIELD = 13;
	private static final int DESCRIPTION_EXTERNAL_ITEM_7010_FIELD = 14;
	
    int count = 0;

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
	       String s = null;
		try {
			String inputFileName = (String) process
					.readProperty(inputFilePropName);
			File inputFile = new File(inputFileName);
			if (!inputFile.exists() || !inputFile.canRead()) {
				throw new TaskFailedException("Specified file '"
						+ inputFileName
						+ "' either does not exist or cannot be read");
			}

			I_TermFactory tf = LocalVersionedTerminology.get();
			
		    BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		       reader.readLine(); // skip first line...
	            I_ConfigAceFrame profile = (I_ConfigAceFrame) worker
				.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

	            int line = 0;
	            while ((s=reader.readLine())!=null){
	            line++;
	            if (line % 1000 == 0) {
					 AceLog.getAppLog().info("Line: " + line);
	            }
		         String fields[] = s.split("\t");
		         if (fields != null && fields.length > 0) {
		        	 for (int i = 0; i < fields.length; i++) {
		        		 if (fields[i].startsWith("\"") && fields[i].endsWith("\"")) {
		        			 fields[i] = fields[i].substring(1, fields[i].length() - 1);
		        		 }
		        	 }
			         String snomedCid = fields[SNOMED_ID_FIELD];
			         UUID conceptUuid = Type3UuidFactory.fromSNOMED(snomedCid);
			         count++;
			         checkForDescriptionAndAdd(tf, fields, conceptUuid, profile);
		         }
		       }
		       reader.close();
				 AceLog.getAppLog().info("Processed: " + count + " entries.");
		     
			return Condition.CONTINUE;

		} catch (Exception e) {
			AceLog.getEditLog().log(Level.SEVERE, "Exception processing: "  + s);
			throw new TaskFailedException(e);
		}
	}

	private static void checkForDescriptionAndAdd(I_TermFactory tf, String[] fields,
			UUID conceptUuid, I_ConfigAceFrame config) throws TerminologyException, IOException, NoSuchAlgorithmException {
		I_GetConceptData concept = tf.getConcept(new UUID[] { conceptUuid });
		I_DescriptionVersioned matchedDesc = null;
		for (I_DescriptionVersioned desc: concept.getDescriptions()) {
			for (I_DescriptionPart dt: desc.getVersions()) {
				if (dt.getText().toLowerCase().equals(fields[DESCRIPTION_FIELD].toLowerCase())) {
					 matchedDesc = desc;
					break;
				}
			}
			if (matchedDesc != null) {
				break;
			}
		}
		if (matchedDesc == null) {
			// Create new description. 
			UUID newDescId = Type5UuidFactory.get("org.kp.desc." +  
					fields[DESCRIPTION_FIELD].toLowerCase() + 
					concept.getUids().iterator().next());
			matchedDesc = tf.newDescription(newDescId, concept, "en", fields[DESCRIPTION_FIELD].toLowerCase(), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()), 
					config);
		}
		
		I_Path path = config.getEditingPathSet().iterator().next();
		int booleanExtNid = RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.localize().getNid();
		int stringExtNid = RefsetAuxiliary.Concept.STRING_EXTENSION.localize().getNid();
		int currentNid = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		int pathNid = path.getConceptId();
		
		// Add identifiers
		I_IdVersioned descId = tf.getId(matchedDesc.getDescId());
		
		// clinical item 11
		if (fields[DESCRIPTION_EXTERNAL_ITEM_11_FIELD] != null && 
				fields[DESCRIPTION_EXTERNAL_ITEM_11_FIELD].length() > 0) {
		I_IdPart clinicalItem11 = descId.getVersions().iterator().next().duplicate();
		descId.addVersion(clinicalItem11);
		clinicalItem11.setPathId(pathNid);
		clinicalItem11.setSource(tf.uuidToNative(UUID.fromString("e3dadc2a-196d-5525-879a-3037af99607d")));
		clinicalItem11.setSourceId(fields[DESCRIPTION_EXTERNAL_ITEM_11_FIELD]);
		clinicalItem11.setVersion(Integer.MAX_VALUE);
		}
		// clinical dot 1
		if (fields[DESCRIPTION_EXTERNAL_DOT1_FIELD] != null && 
				fields[DESCRIPTION_EXTERNAL_DOT1_FIELD].length() > 0) {
		I_IdPart clinicalDot1 = descId.getVersions().iterator().next().duplicate();
		descId.addVersion(clinicalDot1);
		clinicalDot1.setPathId(pathNid);
		clinicalDot1.setSource(tf.uuidToNative(UUID.fromString("e49a55a7-319d-5744-b8a9-9b7cc86fd1c6")));
		clinicalDot1.setSourceId(fields[DESCRIPTION_EXTERNAL_DOT1_FIELD]);
		clinicalDot1.setVersion(Integer.MAX_VALUE);
		}
		
		// CSMID
		if (fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD] != null && 
				fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD].length() > 0) {
		I_IdPart csmid = descId.getVersions().iterator().next().duplicate();
		descId.addVersion(csmid);
		csmid.setPathId(pathNid);
		csmid.setSource(tf.uuidToNative(UUID.fromString("01384e4a-844c-5c23-aac5-fc0a28b6a2b7")));
		csmid.setSourceId(fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);
		csmid.setVersion(Integer.MAX_VALUE);
		}

		tf.addUncommitted(concept);

		// Add to refsets...
		
		// Region refset...
		UUID refsetConceptId = Type5UuidFactory.get("org.kp.refset." + fields[REFSET_NAME_FIELD]);
		UUID memberId = Type5UuidFactory.get("org.kp.refset.member." + fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD] + "." + 
				refsetConceptId + ".true");
		int memberNid = tf.uuidToNativeWithGeneration(memberId, 
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), 
				path, Integer.MAX_VALUE);
		if (tf.hasExtension(memberNid) == false) {
			I_ThinExtByRefVersioned regionMember = tf.newExtensionNoChecks(tf.uuidToNative(refsetConceptId), 
					memberNid, 
					matchedDesc.getDescId(), 
					booleanExtNid);
			I_ThinExtByRefPartBoolean regionMemberPart = tf.newBooleanExtensionPart();
			regionMemberPart.setPathId(pathNid);
			regionMemberPart.setStatusId(currentNid);
			regionMemberPart.setVersion(Integer.MAX_VALUE);
			regionMemberPart.setValue(true);
			regionMember.addVersion(regionMemberPart);
		}
		
		//item 207
		if (fields[DESCRIPTION_EXTERNAL_ITEM_207_FIELD] != null && 
				fields[DESCRIPTION_EXTERNAL_ITEM_207_FIELD].length() > 0) {
		UUID item207RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 207");
		UUID item207MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 207." + 
				fields[DESCRIPTION_EXTERNAL_ITEM_207_FIELD] + 
				fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);
		int item207MemberNid = tf.uuidToNativeWithGeneration(item207MemberId, 
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), 
				path, Integer.MAX_VALUE);
		if (tf.hasExtension(item207MemberNid) == false) {
		I_ThinExtByRefVersioned item207Member = tf.newExtensionNoChecks(tf.uuidToNative(item207RefsetId), 
				item207MemberNid, 
				matchedDesc.getDescId(), 
				stringExtNid);
		I_ThinExtByRefPartInteger item207MemberPart = tf.newIntegerExtensionPart();
		item207MemberPart.setPathId(pathNid);
		item207MemberPart.setStatusId(currentNid);
		item207MemberPart.setVersion(Integer.MAX_VALUE);
		item207MemberPart.setValue(Integer.parseInt(fields[DESCRIPTION_EXTERNAL_ITEM_207_FIELD]));
		item207Member.addVersion(item207MemberPart);
		}
		}
		
		//item 7010
		if (fields[DESCRIPTION_EXTERNAL_ITEM_7010_FIELD] != null && 
				fields[DESCRIPTION_EXTERNAL_ITEM_7010_FIELD].length() > 0) {
		UUID item7010RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 7010");
		UUID item7010MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 7010." + 
				fields[DESCRIPTION_EXTERNAL_ITEM_7010_FIELD] + 
				fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);
		int item7010MemberNid = tf.uuidToNativeWithGeneration(item7010MemberId, 
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), 
				path, Integer.MAX_VALUE);
		if (tf.hasExtension(item7010MemberNid) == false) {
		I_ThinExtByRefVersioned item7010Member = tf.newExtensionNoChecks(tf.uuidToNative(item7010RefsetId), 
				item7010MemberNid, 
				matchedDesc.getDescId(), 
				stringExtNid);
		I_ThinExtByRefPartString item7010MemberPart = tf.newStringExtensionPart();
		item7010MemberPart.setPathId(pathNid);
		item7010MemberPart.setStatusId(currentNid);
		item7010MemberPart.setVersion(Integer.MAX_VALUE);
		item7010MemberPart.setStringValue(fields[DESCRIPTION_EXTERNAL_ITEM_7010_FIELD]);
		item7010Member.addVersion(item7010MemberPart);
		}
		}
		
		//item 2000
		if (fields[DESCRIPTION_EXTERNAL_ITEM_2000_FIELD] != null && 
				fields[DESCRIPTION_EXTERNAL_ITEM_2000_FIELD].length() > 0) {
		UUID item2000RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 2000");
		UUID item2000MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 2000." + 
				fields[DESCRIPTION_EXTERNAL_ITEM_2000_FIELD] + 
				fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);
		I_ThinExtByRefVersioned item2000Member;
		int item2000MemberNid = tf.uuidToNativeWithGeneration(item2000MemberId, 
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), 
				path, Integer.MAX_VALUE);
		if (tf.hasExtension(item2000MemberNid) == false) {
		if (tf.hasExtension(tf.uuidToNative(item2000RefsetId))) {
			item2000Member = tf.newExtensionNoChecks(tf.uuidToNative(item2000RefsetId), 
					tf.uuidToNativeWithGeneration(item2000MemberId, 
							ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), 
							path, Integer.MAX_VALUE), 
					matchedDesc.getDescId(), 
					stringExtNid);
			I_ThinExtByRefPartString item2000MemberPart = tf.newStringExtensionPart();
			item2000MemberPart.setPathId(pathNid);
			item2000MemberPart.setStatusId(currentNid);
			item2000MemberPart.setVersion(Integer.MAX_VALUE);
			item2000MemberPart.setStringValue(fields[DESCRIPTION_EXTERNAL_ITEM_2000_FIELD]);
			item2000Member.addVersion(item2000MemberPart);
		} 
		}
		}
		
		
		//item 200
		if (fields[DESCRIPTION_EXTERNAL_ITEM_200_FIELD] != null && 
				fields[DESCRIPTION_EXTERNAL_ITEM_200_FIELD].length() > 0) {
		UUID item200RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 200");
		UUID item200MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 200." + 
				fields[DESCRIPTION_EXTERNAL_ITEM_200_FIELD] + 
				fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);
		int item200MemberNid = tf.uuidToNativeWithGeneration(item200MemberId, 
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), 
				path, Integer.MAX_VALUE);
		if (tf.hasExtension(item200MemberNid) == false) {
		I_ThinExtByRefVersioned item200Member = tf.newExtensionNoChecks(tf.uuidToNative(item200RefsetId), 
				item200MemberNid, 
				matchedDesc.getDescId(), 
				stringExtNid);
		I_ThinExtByRefPartString item200MemberPart = tf.newStringExtensionPart();
		item200MemberPart.setPathId(pathNid);
		item200MemberPart.setStatusId(currentNid);
		item200MemberPart.setVersion(Integer.MAX_VALUE);
		item200MemberPart.setStringValue(fields[DESCRIPTION_EXTERNAL_ITEM_200_FIELD]);
		item200Member.addVersion(item200MemberPart);
		}
		}
		
		
		//item 100
		if (fields[DESCRIPTION_EXTERNAL_ITEM_100_FIELD] != null && 
				fields[DESCRIPTION_EXTERNAL_ITEM_100_FIELD].length() > 0) {
		UUID item100RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 100");
		UUID item100MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 100." + 
				fields[DESCRIPTION_EXTERNAL_ITEM_100_FIELD] + 
				fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);
		int item100MemberNid = tf.uuidToNativeWithGeneration(item100MemberId, 
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), 
				path, Integer.MAX_VALUE);
		if (tf.hasExtension(item100MemberNid) == false) {
		I_ThinExtByRefVersioned item100Member = tf.newExtensionNoChecks(tf.uuidToNative(item100RefsetId), 
				item100MemberNid, 
				matchedDesc.getDescId(), 
				stringExtNid);
		I_ThinExtByRefPartString item100MemberPart = tf.newStringExtensionPart();
		item100MemberPart.setPathId(pathNid);
		item100MemberPart.setStatusId(currentNid);
		item100MemberPart.setVersion(Integer.MAX_VALUE);
		item100MemberPart.setStringValue(fields[DESCRIPTION_EXTERNAL_ITEM_100_FIELD]);
		item100Member.addVersion(item100MemberPart);
		}
		}
		
		//item 40
		if (fields[DESCRIPTION_EXTERNAL_ITEM_40_FIELD] != null && 
				fields[DESCRIPTION_EXTERNAL_ITEM_40_FIELD].length() > 0) {
		UUID item40RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 40");
		UUID item40MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 40." + 
				fields[DESCRIPTION_EXTERNAL_ITEM_40_FIELD]+ 
				fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);
		int item40MemberNid = tf.uuidToNativeWithGeneration(item40MemberId, 
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), 
				path, Integer.MAX_VALUE);
		if (tf.hasExtension(item40MemberNid) == false) {
		I_ThinExtByRefVersioned item40Member = tf.newExtensionNoChecks(tf.uuidToNative(item40RefsetId), 
				item40MemberNid, 
				matchedDesc.getDescId(), 
				stringExtNid);
		I_ThinExtByRefPartString item40MemberPart = tf.newStringExtensionPart();
		item40MemberPart.setPathId(pathNid);
		item40MemberPart.setStatusId(currentNid);
		item40MemberPart.setVersion(Integer.MAX_VALUE);
		item40MemberPart.setStringValue(fields[DESCRIPTION_EXTERNAL_ITEM_40_FIELD]);
		item40Member.addVersion(item40MemberPart);
		}
		}
		
		
		if (fields[DESCRIPTION_STATUS_FIELD].trim().toLowerCase().equals("slate for sd")) {
			if (fields[REFSET_REASON_FOR_SOFT_DELETE_FIELD] != null && 
					fields[REFSET_REASON_FOR_SOFT_DELETE_FIELD].length() > 0) {
				//Reason for soft delete
				UUID reasonForSoftDeleteRefsetId = Type5UuidFactory.get("org.kp.refset.Reason for Soft Delete");
				UUID reasonForSoftDeleteMemberId = Type5UuidFactory.get("org.kp.refset.member.Reason for Soft Delete." + 
						fields[REFSET_REASON_FOR_SOFT_DELETE_FIELD] + 
						fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);
				int reasonForSoftDeleteMemberNid = tf.uuidToNativeWithGeneration(reasonForSoftDeleteMemberId, 
						ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), 
						path, Integer.MAX_VALUE);
				if (tf.hasExtension(reasonForSoftDeleteMemberNid) == false) {
				I_ThinExtByRefVersioned reasonMember = tf.newExtensionNoChecks(tf.uuidToNative(reasonForSoftDeleteRefsetId), 
						reasonForSoftDeleteMemberNid, 
						matchedDesc.getDescId(), 
						stringExtNid);
				I_ThinExtByRefPartString reasonMemberPart = tf.newStringExtensionPart();
				reasonMemberPart.setPathId(pathNid);
				reasonMemberPart.setStatusId(currentNid);
				reasonMemberPart.setVersion(Integer.MAX_VALUE);
				reasonMemberPart.setStringValue(fields[REFSET_REASON_FOR_SOFT_DELETE_FIELD]);
				reasonMember.addVersion(reasonMemberPart);
				}
			}
			
		}
		
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

	public String getInputFilePropName() {
		return inputFilePropName;
	}

	public void setInputFilePropName(String inputFilePropName) {
		this.inputFilePropName = inputFilePropName;
	}
	
}
