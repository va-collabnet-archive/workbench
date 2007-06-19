package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.LocalVersionedTerminology;
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


@BeanList(specs = { @Spec(directory = "tasks/ace/dups", type = BeanType.TASK_BEAN) })
public class AddConceptAndPotDupToList extends AbstractTask {

	/**
	 * Adds a concept and potential duplicates to the list view
	 * @author Susan Castillo
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;
	
	private String conceptUuidStrPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT_UUID.getAttachmentKey();;
	

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(conceptUuidStrPropName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			conceptUuidStrPropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do...

	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
			
			UUID conceptUid = UUID.fromString((String) process.readProperty(conceptUuidStrPropName));
			I_GetConceptData conceptWithPotDup = LocalVersionedTerminology.get().getConcept(new UUID[]{conceptUid});
	
			JList conceptList = config.getBatchConceptList();
			I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
			I_IntSet potDupTypeSet = LocalVersionedTerminology.get().newIntSet();
			I_GetConceptData potDupRelType = LocalVersionedTerminology.get().getConcept(ArchitectonicAuxiliary.Concept.IS_POT_DUP_REL.getUids());
			potDupTypeSet.add(potDupRelType.getConceptId());
			model.clear();
			model.addElement(conceptWithPotDup);
			
			
			for (I_GetConceptData child: conceptWithPotDup.getDestRelOrigins(config.getAllowedStatus(), 
					potDupTypeSet, config.getViewPositionSet(), true)) {
				model.addElement(child);
			}

			return Condition.CONTINUE;
		} catch (IOException e) {
			throw new TaskFailedException(e);
		} catch (TerminologyException e) {
			throw new TaskFailedException(e);
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} catch (IntrospectionException e) {
			throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		}
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public String getConceptUuidStrPropName() {
		return conceptUuidStrPropName;
	}

	public void setConceptUuidStrPropName(String conceptUuidStr) {
		this.conceptUuidStrPropName = conceptUuidStr;
	}

}
