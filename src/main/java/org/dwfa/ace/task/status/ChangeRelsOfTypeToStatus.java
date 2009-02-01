package org.dwfa.ace.task.status;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/status", type = BeanType.TASK_BEAN) })
public class ChangeRelsOfTypeToStatus extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;
	
    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    private TermEntry relType = new TermEntry(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
    private TermEntry newStatus = new TermEntry(ArchitectonicAuxiliary.Concept.RETIRED.getUids());

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(relType);
		out.writeObject(newStatus);
		out.writeObject(activeConceptPropName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			relType = (TermEntry) in.readObject();
			newStatus = (TermEntry) in.readObject();
			activeConceptPropName = (String) in.readObject();
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
			
			I_GetConceptData concept = (I_GetConceptData) process.readProperty(activeConceptPropName);
			if (config.getEditingPathSet().size() == 0) {
				throw new TaskFailedException("You must select at least one editing path. ");
			}
			
			
			Set<I_Position> positionsForEdit = new HashSet<I_Position>();
			for (I_Path editPath: config.getEditingPathSet()) {
				positionsForEdit.add(LocalVersionedTerminology.get().newPosition(editPath, Integer.MAX_VALUE));
			}
			I_GetConceptData newStatusConcept = LocalVersionedTerminology.get().getConcept(newStatus.ids);
			I_GetConceptData relTypeConcept = LocalVersionedTerminology.get().getConcept(relType.ids);
			I_IntSet typeSet = LocalVersionedTerminology.get().newIntSet();
			typeSet.add(relTypeConcept.getConceptId());
			
			for (I_RelTuple relTuple: concept.getSourceRelTuples(config.getAllowedStatus(), 
						typeSet, positionsForEdit, false)) {
				for (I_Path editPath: config.getEditingPathSet()) {
					List<I_RelTuple>  editTuples = concept.getSourceRelTuples(config.getAllowedStatus(), 
							typeSet, positionsForEdit, false);
					Set<I_RelPart> partsToAdd = new HashSet<I_RelPart>();
					for (I_RelTuple t: editTuples) {
						if (t.getStatusId() != newStatusConcept.getConceptId()) {
							I_RelPart newPart = t.duplicatePart();
							newPart.setPathId(editPath.getConceptId());
							newPart.setVersion(Integer.MAX_VALUE);
							newPart.setStatusId(newStatusConcept.getConceptId());
							partsToAdd.add(newPart);
						}
					}
					for (I_RelPart p: partsToAdd) {
						relTuple.getFixedPart().addVersion(p);
					}
				}
			}
			LocalVersionedTerminology.get().addUncommitted(concept);
			return Condition.CONTINUE;
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (IntrospectionException e) {
			throw new TaskFailedException(e);
		} catch (IOException e) {
			throw new TaskFailedException(e);
		} catch (TerminologyException e) {
			throw new TaskFailedException(e);
		}
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public String getActiveConceptPropName() {
		return activeConceptPropName;
	}

	public void setActiveConceptPropName(String propName) {
		this.activeConceptPropName = propName;
	}

	public TermEntry getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(TermEntry newStatus) {
		this.newStatus = newStatus;
	}

	public TermEntry getRelType() {
		return relType;
	}

	public void setRelType(TermEntry relType) {
		this.relType = relType;
	}

}
