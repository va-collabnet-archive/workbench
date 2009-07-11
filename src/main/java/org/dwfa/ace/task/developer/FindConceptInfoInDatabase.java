package org.dwfa.ace.task.developer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/developer", type = BeanType.TASK_BEAN) })
public class FindConceptInfoInDatabase extends AbstractTask {

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
		// nothing to do. 

	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		
		try{
				UUID uuid = UUID.fromString("af46800c-c545-387f-a5c3-4e53bb4575ae");

				int nid = termFactory.uuidToNative(uuid);
				
				I_GetConceptData concept = termFactory.getConcept(nid);
				worker.getLogger().info("Found concept: " + concept);
				worker.getLogger().info("Concept attributes: " + concept.getConceptAttributes());
					
		}
		catch(Exception e){throw new TaskFailedException(e);}	
		return Condition.CONTINUE;
	}

	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

}
