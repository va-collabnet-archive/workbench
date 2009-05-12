package org.dwfa.ace.task.developer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/developer", type = BeanType.TASK_BEAN) })
public class TestIdAllocation extends AbstractTask{
	
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}//End method writeObject

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {

		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}//End method readObject

		
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do...
	}//End method complete
	
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
	
		try {
			I_TermFactory termFactory = LocalVersionedTerminology.get();
			for (int i = 0; i < 10000; i++) {
				termFactory.uuidToNativeWithGeneration(UUID.randomUUID(), ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
						termFactory.getPaths(), Integer.MAX_VALUE);
			}
			return Condition.CONTINUE;
		} catch (IOException e) {
			throw new TaskFailedException(e);
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
		
	}//End method evaluate

	
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}//End method getConditions	

	public int[] getDataContainerIds() {
        return new int[] {};
	}

}