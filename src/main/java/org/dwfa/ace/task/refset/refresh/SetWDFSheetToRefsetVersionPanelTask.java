package org.dwfa.ace.task.refset.refresh;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.wfdetailsSheet.ClearWorkflowDetailsSheet;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Shows the Refset Version panel in the WF Details panel.
 * 
 * @author Perry Reid
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class SetWDFSheetToRefsetVersionPanelTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private transient Exception ex = null;

    /**
     * 
     * @param out
     * @throws IOException
     */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
	}
	/**
	 * 
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	 private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	     int objDataVersion = in.readInt();
	     if (objDataVersion <= dataVersion) {
	         if (objDataVersion >= 1) {
	        	 // Read version 1 data fields...
	        	 profilePropName = (String) in.readObject();
	         } else {
	             // Set version 1 default values...
	         }
	         // Initialize transient properties...
	     } else {
	         throw new IOException("Can't handle dataversion: " + objDataVersion);
	     }
	 }  

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process,
			final I_Work worker) throws TaskFailedException {
		try {
			ex = null;
			if (SwingUtilities.isEventDispatchThread()) {
				doRun(process, worker);
			} else {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						doRun(process, worker); 
					}
				});
			}
		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} 
		if (ex != null) {
			throw new TaskFailedException(ex);
		}
		return Condition.CONTINUE;
	}

	private void doRun(final I_EncodeBusinessProcess process,
			final I_Work worker) {
		I_ConfigAceFrame config;
		try {
			config = (I_ConfigAceFrame) process.readProperty(getProfilePropName());
			ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
			clear.setProfilePropName(getProfilePropName());
			clear.evaluate(process, worker);
			JPanel wfSheet = config.getWorkflowDetailsSheet();
	        int width = 475;
	        int height = 625;
	        wfSheet.setSize(width, height);
	        wfSheet.setLayout(new BorderLayout());
            wfSheet.add(new PanelRefsetVersion(config), BorderLayout.NORTH);

		} catch (Exception e) {
			ex = e;
		}
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do

	}
	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

	/**
	 * 
	 * @return
	 */
	public String getProfilePropName() {
		return profilePropName;
	}

	/**
	 * 
	 * @param profilePropName
	 */
	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}



}
