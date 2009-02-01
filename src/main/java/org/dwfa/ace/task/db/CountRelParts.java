package org.dwfa.ace.task.db;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;

import org.dwfa.ace.api.I_ProcessRelationships;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = 
{ @Spec(directory = "tasks/ide/db", type = BeanType.TASK_BEAN)})
public class CountRelParts extends AbstractTask implements I_ProcessRelationships {

	   private static final long serialVersionUID = 1;

	   private static final int dataVersion = 1;
	   
	   private transient int relCount;
	   private transient int partCount;
	   private transient HashSet<I_RelPart> partsSet;
	   
	   
	   private void writeObject(ObjectOutputStream out) throws IOException {
	       out.writeInt(dataVersion);
	    }

	   private void readObject(java.io.ObjectInputStream in) throws IOException,
	           ClassNotFoundException {
	       int objDataVersion = in.readInt();
	       if (objDataVersion == 1) {

	       } else {
	           throw new IOException("Can't handle dataversion: " + objDataVersion);   
	       }

	   }

	   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	           throws TaskFailedException {
	       try {
	    	   partsSet = new HashSet<I_RelPart>();
	         LocalVersionedTerminology.get().iterateRelationships(this);
	         worker.getLogger().info("Total rels: " + relCount);
	         worker.getLogger().info("Rel parts: " + partCount);
	         worker.getLogger().info("Unique parts: " + partsSet.size());
	         
	         
	           return Condition.CONTINUE;
	       } catch (Exception e) {
	           throw new TaskFailedException(e);
	       }
	   }

	   public void complete(I_EncodeBusinessProcess process, I_Work worker)
	           throws TaskFailedException {

	   }

	   public Collection<Condition> getConditions() {
	       return CONTINUE_CONDITION;
	   }

	   public int[] getDataContainerIds() {
	       return new int[] {  };
	   }

	public void processRelationship(I_RelVersioned versionedRel)
			throws Exception {
		relCount++;
		for (I_RelPart part: versionedRel.getVersions()) {
			partCount++;
			partsSet.add(part);
		}
		
	}


	}
