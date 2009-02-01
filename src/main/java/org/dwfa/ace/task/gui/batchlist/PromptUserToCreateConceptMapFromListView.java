package org.dwfa.ace.task.gui.batchlist;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.wfpanel.InstructAndWaitNC;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/listview", type = BeanType.TASK_BEAN) })
public class PromptUserToCreateConceptMapFromListView extends AbstractTask {

	/**
	    * 
	    */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private String conceptMapPropName = ProcessAttachmentKeys.CON_CON_MAP
			.getAttachmentKey();

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(conceptMapPropName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			conceptMapPropName = (String) in.readObject();
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
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
							.name());
			JList conceptList = config.getBatchConceptList();
			I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList
					.getModel();
			AceLog.getAppLog().info("Mapping list of size: " + model.getSize());
			Map<I_GetConceptData, I_GetConceptData> conceptMap = new HashMap<I_GetConceptData, I_GetConceptData>();
			
			for (int i = 0; i < model.getSize(); i++) {
				I_GetConceptData concept = model.getElementAt(i);
				InstructAndWaitNC instructAndWait = new InstructAndWaitNC();
					process.setProperty(instructAndWait.getProfilePropName(), config);
					instructAndWait.setInstruction("<html>Please select the substitution for:<br>" + 
							concept.getInitialText() + ". ");
					Condition c = instructAndWait.evaluate(process, worker);
					if (c != Condition.CONTINUE) {
						throw new TaskFailedException("Encountered: " + c);
					} else {
						conceptMap.put(concept, config.getHierarchySelection());
					}
			}
			process.setProperty(conceptMapPropName, conceptMap);
			return Condition.CONTINUE;
		} catch (IntrospectionException e) {
			throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
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

	public String getConceptMapPropName() {
		return conceptMapPropName;
	}

	public void setConceptMapPropName(String conceptMapPropName) {
		this.conceptMapPropName = conceptMapPropName;
	}

}
