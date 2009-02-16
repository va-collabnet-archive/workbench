package org.dwfa.ace.task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class CloneConcept extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do...

	}

	/**
	 * @TODO use a type 1 uuid generator instead of a random uuid...
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		I_GetConceptData newConcept = null;
		try {
			I_TermFactory tf = LocalVersionedTerminology.get();
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
			.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
			Set<I_Position> clonePositions = new HashSet<I_Position>();
			for (I_Path path: config.getEditingPathSet()) {
				clonePositions.add(tf.newPosition(path, Integer.MAX_VALUE));
			}
			
			I_HostConceptPlugins host = (I_HostConceptPlugins) worker
					.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());
			
			I_GetConceptData conceptToClone = (I_GetConceptData) host.getTermComponent();
			
			newConcept = LocalVersionedTerminology.get().newConcept(UUID.randomUUID(), false, config);
			
			for (I_DescriptionTuple desc: conceptToClone.getDescriptionTuples(config.getAllowedStatus(), null, 
					clonePositions)) {
				tf.newDescription(UUID.randomUUID(), newConcept, desc.getLang(), 
						"Clone of " + desc.getText(),
						tf.getConcept(desc.getTypeId()),
						config);
			}

			for (I_RelTuple rel: conceptToClone.getSourceRelTuples(config.getAllowedStatus(), 
					null, clonePositions, false)){
				tf.newRelationship(UUID.randomUUID(), 
						newConcept, tf.getConcept(rel.getRelTypeId()), 
						tf.getConcept(rel.getC2Id()), tf.getConcept(rel.getCharacteristicId()), 
						tf.getConcept(rel.getRefinabilityId()), tf.getConcept(rel.getStatusId()), 
						rel.getGroup(), config);
			}
						
			host.setTermComponent(newConcept);

			return Condition.CONTINUE;
		} catch (TerminologyException e) {
			undoEdits(newConcept, LocalVersionedTerminology.get());
			throw new TaskFailedException(e);
		} catch (IOException e) {
			undoEdits(newConcept, LocalVersionedTerminology.get());
			throw new TaskFailedException(e);
		}
	}

	private void undoEdits(I_GetConceptData newConcept, I_TermFactory termFactory) {
		if (termFactory != null) {
			if (newConcept != null) {
				termFactory.forget(newConcept);
			}
		}
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

}
