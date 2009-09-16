package org.dwfa.ace.task.profile;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile", type = BeanType.TASK_BEAN) })
public class CreateUserPathAndQueues extends AbstractTask {
	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	private String commitProfilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	private String positionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			profilePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public String getProfilePropName() {
		return profilePropName;
	}

	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}


	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process,
			final I_Work worker) throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) process.readProperty(profilePropName);
			I_ConfigAceFrame commitConfig = (I_ConfigAceFrame) process.readProperty(commitProfilePropName);
			Set<I_Position> positionSet = (Set<I_Position>) process.readProperty(positionSetPropName);

			// Create new concept for user...
			createUser(config, commitConfig);
			
			// Create new path for user...
			createPath(config, commitConfig, positionSet);
			
			// Create inbox
			config.getQueueAddressesToShow().add(config.getDbConfig().getUsername() + ".inbox");
			// Create todo box
			config.getQueueAddressesToShow().add(config.getDbConfig().getUsername() + ".todo");
			// Create complete box
			config.getQueueAddressesToShow().add(config.getDbConfig().getUsername() + ".completed");
			
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
		return Condition.CONTINUE;
	}
	
	private void createUser(I_ConfigAceFrame config, I_ConfigAceFrame commitConfig) throws TaskFailedException, TerminologyException, IOException {
        AceLog.getAppLog().info("Create new path for user: " + config.getDbConfig().getFullName());
        if (config.getDbConfig().getFullName() == null || config.getDbConfig().getFullName().length() == 0) {
            JOptionPane.showMessageDialog(config.getWorkflowPanel().getTopLevelAncestor(), 
            		"Full name cannot be empty.");
            throw new TaskFailedException();
        }
             	//Needs a concept record...
           	I_GetConceptData userConcept = LocalVersionedTerminology.get().newConcept(UUID.randomUUID(), false, commitConfig);
          	
        	//Needs a description record...
        	I_DescriptionVersioned fsDesc = LocalVersionedTerminology.get().newDescription(
        			UUID.randomUUID(), userConcept, "en", config.getDbConfig().getFullName(), 
        			ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), commitConfig);
        	userConcept.getUncommittedDescriptions().add(fsDesc);
        	I_DescriptionVersioned prefDesc = LocalVersionedTerminology.get().newDescription(
        			UUID.randomUUID(), userConcept, "en", config.getDbConfig().getUsername(), 
        			ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), commitConfig);
        	userConcept.getUncommittedDescriptions().add(prefDesc);
        	I_DescriptionVersioned inboxDesc = LocalVersionedTerminology.get().newDescription(
        			UUID.randomUUID(), userConcept, "en", config.getDbConfig().getUsername() + ".inbox", 
        			ArchitectonicAuxiliary.Concept.USER_INBOX.localize(), commitConfig);
        	userConcept.getUncommittedDescriptions().add(inboxDesc);

        	I_TermFactory tf = LocalVersionedTerminology.get();
        	//Needs a relationship record...
        	I_RelVersioned rel = LocalVersionedTerminology.get().newRelationship(UUID.randomUUID(), userConcept, 
        			tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), 
        			tf.getConcept(ArchitectonicAuxiliary.Concept.USER.getUids()), 
        			tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
        			tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()), 
        			tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()), 0, commitConfig);
        	
        	userConcept.getUncommittedSourceRels().add(rel);
	}
	
	private void createPath(I_ConfigAceFrame config, I_ConfigAceFrame commitConfig, Set<I_Position> positionSet) throws Exception {
        AceLog.getAppLog().info("Create new path for user: " + config.getDbConfig().getFullName());
        if (config.getDbConfig().getFullName() == null || config.getDbConfig().getFullName().length() == 0) {
            JOptionPane.showMessageDialog(config.getWorkflowPanel().getTopLevelAncestor(), 
            		"Full name cannot be empty.");
            throw new TaskFailedException();
        }
         AceLog.getAppLog().info(positionSet.toString());
        if (positionSet.size() == 0) {
            JOptionPane.showMessageDialog(config.getWorkflowPanel().getTopLevelAncestor(), 
            		"You must select at least one origin for path.");
            return;
        }
       	UUID newPathUid = UUID.randomUUID();
         	
            	//Needs a concept record...
           	I_GetConceptData pathConcept = LocalVersionedTerminology.get().newConcept(newPathUid, false, commitConfig);
          	
        	//Needs a description record...
        	I_DescriptionVersioned fsDesc = LocalVersionedTerminology.get().newDescription(
        			UUID.randomUUID(), pathConcept, "en", config.getDbConfig().getFullName() + " development path", 
        			ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), commitConfig);
        	pathConcept.getUncommittedDescriptions().add(fsDesc);
        	I_DescriptionVersioned prefDesc = LocalVersionedTerminology.get().newDescription(
        			UUID.randomUUID(), pathConcept, "en", config.getDbConfig().getUsername() + " development path", 
        			ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), commitConfig);
        	pathConcept.getUncommittedDescriptions().add(prefDesc);

        	I_TermFactory tf = LocalVersionedTerminology.get();
        	//Needs a relationship record...
        	I_RelVersioned rel = LocalVersionedTerminology.get().newRelationship(UUID.randomUUID(), pathConcept, 
        			tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), 
        			tf.getConcept(ArchitectonicAuxiliary.Concept.DEVELOPMENT.getUids()), 
        			tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
        			tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()), 
        			tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()), 0, commitConfig);
        	
        	pathConcept.getUncommittedSourceRels().add(rel);
        	        	        	        	
	}


	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do

	}
	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

	public String getPositionSetPropName() {
		return positionSetPropName;
	}

	public void setPositionSetPropName(String positionSetPropName) {
		this.positionSetPropName = positionSetPropName;
	}

}
