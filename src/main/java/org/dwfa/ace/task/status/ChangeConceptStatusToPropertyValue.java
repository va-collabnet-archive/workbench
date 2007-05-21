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

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.AttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/status", type = BeanType.TASK_BEAN) })
public class ChangeConceptStatusToPropertyValue extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;
	
    private String activeConceptPropName = AttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    private String newStatusPropName = AttachmentKeys.NEW_STATUS.getAttachmentKey();

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(newStatusPropName);
		out.writeObject(activeConceptPropName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			newStatusPropName = (String) in.readObject();
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
				.readAttachement(AttachmentKeys.ACE_FRAME_CONFIG.name());
			
			I_GetConceptData concept = (I_GetConceptData) process.readProperty(activeConceptPropName);
			if (config.getEditingPathSet().size() == 0) {
				throw new TaskFailedException("You must select at least one editing path. ");
			}
			
			I_TermFactory termFactory = (I_TermFactory) worker
			.readAttachement(AttachmentKeys.I_TERM_FACTORY.name());

			Set<I_ConceptAttributePart> partsToAdd = new HashSet<I_ConceptAttributePart>();
			
			Set<I_Position> positionsForEdit = new HashSet<I_Position>();
			for (I_Path editPath: config.getEditingPathSet()) {
				positionsForEdit.add(termFactory.newPosition(editPath, Integer.MAX_VALUE));
			}
			I_GetConceptData newStatusConcept = (I_GetConceptData) process.readProperty(newStatusPropName);
			for (I_Path editPath: config.getEditingPathSet()) {
				List<I_ConceptAttributeTuple>  tuples = concept.getConceptTuples(
						null, positionsForEdit);
				for (I_ConceptAttributeTuple t: tuples) {
					if (t.getConceptStatus() != newStatusConcept.getConceptId()) {
						I_ConceptAttributePart newPart = t.duplicatePart();
						newPart.setPathId(editPath.getConceptId());
						newPart.setVersion(Integer.MAX_VALUE);
						newPart.setConceptStatus(newStatusConcept.getConceptId());
						partsToAdd.add(newPart);
					}
				}
			}
			for (I_ConceptAttributePart p: partsToAdd) {
				concept.getConceptAttributes().addVersion(p);
			}
			termFactory.addUncommitted(concept);
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

	public String getNewStatusPropName() {
		return newStatusPropName;
	}

	public void setNewStatusPropName(String newStatusPropName) {
		this.newStatusPropName = newStatusPropName;
	}

}
