package org.dwfa.ace.task.refset.members;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.ProcessAttachmentKeys;
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


@BeanList(specs = { @Spec(directory = "tasks/ide/refset/membership", type = BeanType.TASK_BEAN) })
public class AddConceptToRefset extends AbstractTask {

	private static final long serialVersionUID = -1488580246193922770L;

	private static final int dataVersion = 1;
	
	/** the refset we are adding to */
    private String refsetConceptPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();

    /** the concept to be added to the refset */
    private String memberConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    
    /** the value to be given to the new concept extension */
    private String conceptExtValuePropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey(); 
    
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
        out.writeObject(this.refsetConceptPropName);
        out.writeObject(this.memberConceptPropName);
        out.writeObject(this.conceptExtValuePropName);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			this.refsetConceptPropName = (String) in.readObject();
			this.memberConceptPropName = (String) in.readObject();
			this.conceptExtValuePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		try {
			I_GetConceptData refset = (I_GetConceptData) process.readProperty(refsetConceptPropName);
			I_GetConceptData member = (I_GetConceptData) process.readProperty(memberConceptPropName);
			I_GetConceptData value  = (I_GetConceptData) process.readProperty(conceptExtValuePropName);
			
			I_TermFactory termFactory = LocalVersionedTerminology.get();
			
			if (refset == null) {
				throw new TerminologyException("A working refset has not been selected.");
			}
			
			if (member == null) {
				throw new TerminologyException("No member concept selected.");				
			}
			
			if (value == null) {
				throw new TerminologyException("No concept extension value selected.");
			}
			
			getLogger().info(
					"Adding concept '" + member.getInitialText() + 
					"' as member of refset '" + refset.getInitialText() +
					"' with a value '" + value.getInitialText() + "'.");	
			
			int currentStatusId = 
				termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());
			
			// check subject is not already a member
			for (I_ThinExtByRefVersioned extension : 
					termFactory.getAllExtensionsForComponent(member.getConceptId())) {
				
				if (extension.getRefsetId() == refset.getConceptId()) {
					
					// get the latest version
					I_ThinExtByRefPart latestPart = null;
					for(I_ThinExtByRefPart part : extension.getVersions()) {
						if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
							latestPart = part;
						}
					}
					
					// confirm its the right extension value and its status is current
					if (latestPart.getStatus() == currentStatusId) {
						if (latestPart instanceof I_ThinExtByRefPartConcept) {
							int partValue = ((I_ThinExtByRefPartConcept)latestPart).getConceptId();
							if (partValue == value.getConceptId()) {
								// its already a member so skip
								getLogger().info("Concept is already a member of the refset. Skipping.");
								return Condition.CONTINUE;
							}
						}
					}
					
				}
			}

			// create a new extension (with a part for each path the user is editing)
			
			int conceptTypeId = 
				termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next());
			
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			Set<I_Path> userEditPaths = config.getEditingPathSet();
			
			int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
					ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
					termFactory.getPaths(), Integer.MAX_VALUE);

			I_ThinExtByRefVersioned newExtension =
				termFactory.newExtension(refset.getConceptId(), memberId, member.getConceptId(), conceptTypeId);

			for (I_Path editPath : userEditPaths) {
			
				I_ThinExtByRefPartConcept conceptExtension =
					termFactory.newConceptExtensionPart();
	
				conceptExtension.setPathId(editPath.getConceptId());
				conceptExtension.setStatus(currentStatusId);
				conceptExtension.setVersion(Integer.MAX_VALUE);
				conceptExtension.setConceptId(value.getConceptId());
	
				newExtension.addVersion(conceptExtension);
			}
			
			termFactory.addUncommitted(newExtension); 
			termFactory.commit();
			
			return Condition.CONTINUE;
			
		} catch (Exception e) {
			throw new TaskFailedException("Unable to add concept to refset. " + e.getMessage(), e);
		}
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

	public String getRefsetConceptPropName() {
		return refsetConceptPropName;
	}

	public void setRefsetConceptPropName(String refsetConceptPropName) {
		this.refsetConceptPropName = refsetConceptPropName;
	}

	public String getMemberConceptPropName() {
		return memberConceptPropName;
	}

	public void setMemberConceptPropName(String memberConceptPropName) {
		this.memberConceptPropName = memberConceptPropName;
	}

	public String getConceptExtValuePropName() {
		return conceptExtValuePropName;
	}

	public void setConceptExtValuePropName(String conceptExtValuePropName) {
		this.conceptExtValuePropName = conceptExtValuePropName;
	}
	
	

}
