package org.dwfa.ace.task.refset.spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public abstract class AbstractAddRefsetSpecTask extends AbstractTask {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public final void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do
	}

	public final Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			I_ConfigAceFrame configFrame = (I_ConfigAceFrame) worker
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
							.name());
			if (configFrame.getEditingPathSet().size() == 0) {
				String msg = "Unable to add AND. Editing path set is empty.";
				JOptionPane.showMessageDialog(null, msg);
				throw new TaskFailedException(msg);
			}

			I_GetConceptData refset = configFrame.getRefsetInSpecEditor();
			if (refset != null) {
				JTree specTree = configFrame.getTreeInSpecEditor();
				
				int refsetId = refset.getConceptId();
				int componentId = refsetId;

				TreePath selection = specTree.getSelectionPath();
				
				if (selection != null) {
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selection.getLastPathComponent();
					I_ThinExtByRefVersioned selectedSpec = (I_ThinExtByRefVersioned) selectedNode.getUserObject();
					componentId = selectedSpec.getMemberId();
				}
				
				
				I_TermFactory tf = LocalVersionedTerminology.get();
				int typeId = getRefsetPartTypeId();
				int memberId = tf.uuidToNativeWithGeneration(UUID.randomUUID(),
						ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), 
						configFrame.getEditingPathSet(), Integer.MAX_VALUE);

				
				I_ThinExtByRefVersioned ext = tf.newExtension(refsetId, memberId, componentId, typeId);
				for (I_Path p: configFrame.getEditingPathSet()) {
					I_ThinExtByRefPart specPart = createAndPopulatePart(
							tf, p, configFrame);
					ext.addVersion(specPart);
				}
				tf.addUncommitted(ext);
				
				
			} else {
				throw new TaskFailedException("Unable to complete operation. Refset is null.");
			}
			return Condition.CONTINUE;
		} catch (Exception ex) {
			throw new TaskFailedException(ex);
		}
	}

	protected abstract I_ThinExtByRefPart createAndPopulatePart(
			I_TermFactory tf, I_Path p, I_ConfigAceFrame configFrame) throws IOException,
			TerminologyException;

	protected abstract int getRefsetPartTypeId() throws IOException, TerminologyException;
	
	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

}
