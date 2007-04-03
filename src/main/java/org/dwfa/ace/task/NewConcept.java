package org.dwfa.ace.task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.concept.I_HostConceptPlugins;
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

@BeanList(specs = { @Spec(directory = "tasks/ace", type = BeanType.TASK_BEAN) })
public class NewConcept extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
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
		try {
			I_GetConceptData concept = (I_GetConceptData) worker
					.readAttachement(AttachmentKeys.I_GET_CONCEPT_DATA.name());
			
			@SuppressWarnings("unused") //here to demo how to get the configuration.
			AceFrameConfig config = (AceFrameConfig) worker
					.readAttachement(AttachmentKeys.ACE_FRAME_CONFIG.name());
			
			I_TermFactory termFactory = (I_TermFactory) worker
					.readAttachement(AttachmentKeys.I_TERM_FACTORY.name());
			
			I_HostConceptPlugins host = (I_HostConceptPlugins) worker
					.readAttachement(AttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());
			
			I_GetConceptData newConcept = termFactory.newConcept(UUID.randomUUID(), false);

			termFactory.newDescription(UUID.randomUUID(), concept, "en", "New Fully Specified Description",
					ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize());
			
			termFactory.newDescription(UUID.randomUUID(), concept, "en", "New Preferred Description",
					ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize());
			
			termFactory.newRelationship(UUID.randomUUID(), concept);
			
			host.setTermComponent(newConcept);

			return Condition.CONTINUE;
		} catch (TerminologyException e) {
			throw new TaskFailedException(e);
		} catch (IOException e) {
			throw new TaskFailedException(e);
		}
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

}
