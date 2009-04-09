package org.dwfa.ace.task.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/refset", type = BeanType.TASK_BEAN) })
public class GetWorkingRefset extends AbstractTask {

	private static final long serialVersionUID = 1360688704199093367L;

	private static final int dataVersion = 1;
	
    private String componentPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();


	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
        out.writeObject(this.componentPropName);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			this.componentPropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		try {
			
			I_TermFactory tf = LocalVersionedTerminology.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			
			I_GetConceptData refset = config.getContext();
			
			process.setProperty(componentPropName, refset);
			
			return Condition.CONTINUE;
			
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

	public String getComponentPropName() {
		return componentPropName;
	}

	public void setComponentPropName(String componentPropName) {
		this.componentPropName = componentPropName;
	}
	


}
