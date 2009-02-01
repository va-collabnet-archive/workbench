package org.dwfa.ace.task.gui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/** 
 * This task shows or hides the search component in the ACE Editor UI.
 * 
 * @author susan
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/gui", type = BeanType.TASK_BEAN) })
public class ShowSearch extends AbstractTask {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private static final int dataVersion = 1;
		
	    private Boolean showSearch = true;
	    

		private void writeObject(ObjectOutputStream out) throws IOException {
			out.writeInt(dataVersion);
			out.writeObject(showSearch);
		}

		private void readObject(ObjectInputStream in) throws IOException,
				ClassNotFoundException {
			int objDataVersion = in.readInt();
			if (objDataVersion == dataVersion) {
				showSearch = (Boolean) in.readObject();
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
				config.setShowSearch(showSearch);
				
				return Condition.CONTINUE;
			} catch (IllegalArgumentException e) {
				throw new TaskFailedException(e);
			}
		}

		public Collection<Condition> getConditions() {
			return CONTINUE_CONDITION;
		}

		public int[] getDataContainerIds() {
			return new int[] {};
		}

		public Boolean getShowSearch() {
			return showSearch;
		}

		public void setShowSearch(Boolean showSearch) {
			this.showSearch = showSearch;
		}

}
