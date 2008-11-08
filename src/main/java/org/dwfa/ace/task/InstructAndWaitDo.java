package org.dwfa.ace.task;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
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

@BeanList(specs = { @Spec(directory = "tasks/ace/instruct", type = BeanType.TASK_BEAN) })
public class InstructAndWaitDo extends AbstractTask {

	private static final long serialVersionUID = 1;

	private static final int dataVersion = 2;

	private String instruction = "<html>Instruction";

	private String term = "";

	private String termPropName = ProcessAttachmentKeys.MESSAGE
			.getAttachmentKey();

	private transient Condition returnCondition;

	private transient boolean done;

	// EKM - seems a but easier than passing it around
	private I_ConfigAceFrame config;

	private final String language = "en";

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(instruction);
		out.writeObject(termPropName);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion >= 1) {
			instruction = (String) in.readObject();
			if (objDataVersion >= 2) {
				termPropName = (String) in.readObject();
			}
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	protected void printDescriptionPart(I_DescriptionPart dp) {
		//
		// public int hashCode() {
		// int bhash = 0;
		// if (initialCaseSignificant) {
		// bhash = 1;
		// }
		// return HashFunction.hashCode(new int[] {
		// bhash, lang.hashCode(), pathId,
		// statusId, text.hashCode(),
		// typeId, version
		// });
		// }
		//
		System.out.println(">>>descr init case: "
				+ dp.getInitialCaseSignificant());
		System.out.println(">>>descr lang: " + dp.getLang());
		System.out.println(">>>descr path: " + dp.getPathId());
		System.out.println(">>>descr status: " + dp.getStatusId());
		System.out.println(">>>descr text: " + dp.getText());
		System.out.println(">>>descr type: " + dp.getTypeId());
		System.out.println(">>>descr version: " + dp.getVersion());
	}

	private class DescrActionListener implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				I_GetConceptData con = getSelectedConcept();
				if (con == null)
					return;
				String newDescrString = (String) JOptionPane.showInputDialog(
						null, "Add description to:\n" + con.getInitialText(),
						"Enter description", JOptionPane.QUESTION_MESSAGE,
						null, null, InstructAndWaitDo.this.term);
				if (newDescrString == null)
					return;
				I_TermFactory termFactory = LocalVersionedTerminology.get();
				I_GetConceptData synonym_description_type = termFactory
						.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE
								.getUids());
				// See SearchReplaceTermsInList
				Set<I_Path> paths = config.getEditingPathSet();
				I_DescriptionVersioned newDescr = termFactory.newDescription(
						UUID.randomUUID(), con, language, newDescrString,
						synonym_description_type, config);
				I_DescriptionPart newLastPart = newDescr.getLastTuple()
						.getPart();
				for (I_Path path : paths) {
					if (newLastPart == null) {
						newLastPart = newDescr.getLastTuple().getPart()
								.duplicate();
					}
					// printDescriptionPart(newLastPart);
					// System.out.println(">>> language: " + language);
					newLastPart.setPathId(path.getConceptId());
					newLastPart.setVersion(Integer.MAX_VALUE);
					termFactory.addUncommitted(con);
					newLastPart = null;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	protected I_GetConceptData createNewConcept(String newDescrString)
			throws Exception {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		I_GetConceptData fully_specified_description_type = termFactory
				.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids());
		I_GetConceptData preferred_description_type = termFactory
				.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
						.getUids());
		I_GetConceptData newConcept = LocalVersionedTerminology.get()
				.newConcept(UUID.randomUUID(), false, config);
		termFactory.newDescription(UUID.randomUUID(), newConcept, language,
				newDescrString + " (?????)", fully_specified_description_type,
				config);
		termFactory.newDescription(UUID.randomUUID(), newConcept, language,
				newDescrString, preferred_description_type, config);
		return newConcept;
	}

	private class CloneActionListener implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				I_GetConceptData con = getSelectedConcept();
				if (con == null)
					return;
				String newDescrString = (String) JOptionPane.showInputDialog(
						null, "Clone:\n" + con.getInitialText(),
						"Enter concept name", JOptionPane.QUESTION_MESSAGE,
						null, null, InstructAndWaitDo.this.term);
				if (newDescrString == null)
					return;
				I_GetConceptData newConcept = createNewConcept(newDescrString);
				I_TermFactory termFactory = LocalVersionedTerminology.get();
				Set<I_Position> clonePositions = new HashSet<I_Position>();
				for (I_Path path : config.getEditingPathSet()) {
					clonePositions.add(termFactory.newPosition(path,
							Integer.MAX_VALUE));
				}
				for (I_RelTuple rel : con.getSourceRelTuples(config
						.getAllowedStatus(), null, clonePositions, false)) {
					termFactory.newRelationship(UUID.randomUUID(), newConcept,
							termFactory.getConcept(rel.getRelTypeId()),
							termFactory.getConcept(rel.getC2Id()), termFactory
									.getConcept(rel.getCharacteristicId()),
							termFactory.getConcept(rel.getRefinabilityId()),
							termFactory.getConcept(rel.getStatusId()), rel
									.getGroup(), config);
				}
				I_HostConceptPlugins lcv = config.getListConceptViewer();
				lcv.setTermComponent(newConcept);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}

	private class ChildActionListener implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				I_GetConceptData con = getSelectedConcept();
				if (con == null)
					return;
				String newDescrString = (String) JOptionPane.showInputDialog(
						null, "Create a child of:\n" + con.getInitialText(),
						"Enter concept name", JOptionPane.QUESTION_MESSAGE,
						null, null, InstructAndWaitDo.this.term);
				if (newDescrString == null)
					return;
				I_GetConceptData newConcept = createNewConcept(newDescrString);
				I_TermFactory termFactory = LocalVersionedTerminology.get();
				I_GetConceptData is_a_rel = termFactory
						.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
								.getUids());
				I_GetConceptData defining_characteristic = termFactory
						.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
								.getUids());
				I_GetConceptData optional_refinability = termFactory
						.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY
								.getUids());
				I_GetConceptData current_status = termFactory
						.getConcept(ArchitectonicAuxiliary.Concept.CURRENT
								.getUids());
				termFactory.newRelationship(UUID.randomUUID(), newConcept,
						is_a_rel, con, defining_characteristic,
						optional_refinability, current_status, 0, config);
				I_HostConceptPlugins lcv = config.getListConceptViewer();
				lcv.setTermComponent(newConcept);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}

	private class NewActionListener implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				String newDescrString = (String) JOptionPane.showInputDialog(
						null, "Create a new concept", "Enter concept name",
						JOptionPane.QUESTION_MESSAGE, null, null,
						InstructAndWaitDo.this.term);
				if (newDescrString == null)
					return;
				I_GetConceptData newConcept = createNewConcept(newDescrString);
				I_HostConceptPlugins lcv = config.getListConceptViewer();
				lcv.setTermComponent(newConcept);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}

	private class StepActionListener implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			returnCondition = Condition.ITEM_COMPLETE;
			done = true;
			synchronized (InstructAndWaitDo.this) {
				InstructAndWaitDo.this.notifyAll();
			}

		}

	}

	private class StopActionListener implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			returnCondition = Condition.ITEM_CANCELED;
			done = true;
			synchronized (InstructAndWaitDo.this) {
				InstructAndWaitDo.this.notifyAll();
			}

		}

	}

	private void waitTillDone(Logger l) {
		while (!this.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				l.log(Level.SEVERE, e.getMessage(), e);
			}
		}

	}

	public boolean isDone() {
		return this.done;
	}

	protected List<I_GetConceptData> getSelectedConcepts() throws IOException {
		// System.out.println(">>>getSelectedConcept");
		JList conceptList = config.getBatchConceptList();
		int[] selected = conceptList.getSelectedIndices();
		I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList
				.getModel();
		// for (int i = 0; i < model.getSize(); i++) {
		// I_GetConceptData concept = model.getElementAt(i);
		// I_DescriptionVersioned d = concept.getDescriptions().get(0);
		// System.out.println(">>>" + d + "\nStatus:"
		// + d.getLastTuple().getStatusId());
		// }
		List<I_GetConceptData> ret = new ArrayList<I_GetConceptData>();
		for (int i : selected) {
			ret.add(model.getElementAt(i));
		}
		return ret;
	}

	protected I_GetConceptData getSelectedConcept() throws IOException {
		List<I_GetConceptData> cons = getSelectedConcepts();
		if (cons.size() == 0) {
			JOptionPane.showMessageDialog(null, "Please select a concept");
			return null;
		}
		if (cons.size() != 1) {
			JOptionPane.showMessageDialog(null,
					"Please select only one concept");
			return null;
		}
		return cons.get(0);
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process,
			final I_Work worker) throws TaskFailedException {
		try {

			this.term = (String) process.readProperty(termPropName);
			this.done = false;
			this.config = (I_ConfigAceFrame) worker
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
							.name());
			// this.host = (I_HostConceptPlugins) worker
			// .readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS
			// .name());
			boolean builderVisible = config.isBuilderToggleVisible();
			config.setBuilderToggleVisible(true);
			boolean progressPanelVisible = config.isProgressToggleVisible();
			config.setProgressToggleVisible(false);
			// EKM - changed to subversion toggle
			boolean subversionButtonVisible = config
					.isSubversionToggleVisible();
			config.setSubversionToggleVisible(false);
			boolean inboxButtonVisible = config.isInboxToggleVisible();
			config.setInboxToggleVisible(true);
			// When running from the queue we never see the task list
			final JPanel workflowPanel = config.getWorkflowPanel();
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					Component[] components = workflowPanel.getComponents();
					for (int i = 0; i < components.length; i++) {
						workflowPanel.remove(components[i]);
					}
					workflowPanel.setLayout(new GridBagLayout());
					GridBagConstraints c = new GridBagConstraints();
					c.fill = GridBagConstraints.BOTH;
					c.gridx = 0;
					c.gridy = 0;
					c.weightx = 1.0;
					c.weighty = 0;
					c.anchor = GridBagConstraints.WEST;
					workflowPanel.add(new JPanel(), c); // Filler
					c.gridx++;
					c.weightx = 0.0;
					workflowPanel.add(new JLabel("<html><b>Matches for:</b> "
							+ term), c);
					c.gridx++;
					workflowPanel.add(new JLabel("  "), c);
					c.gridx++;
					c.anchor = GridBagConstraints.SOUTHWEST;

					JButton descrButton = new JButton("Descr");
					descrButton
							.setToolTipText("Add description to selected concept");
					workflowPanel.add(descrButton, c);
					c.gridx++;
					descrButton.addActionListener(new DescrActionListener());

					JButton cloneButton = new JButton("Clone");
					cloneButton.setToolTipText("Clone selected concept");
					workflowPanel.add(cloneButton, c);
					c.gridx++;
					cloneButton.addActionListener(new CloneActionListener());

					JButton childButton = new JButton("Child");
					childButton
							.setToolTipText("Create a child of selected concept");
					workflowPanel.add(childButton, c);
					c.gridx++;
					childButton.addActionListener(new ChildActionListener());

					JButton newButton = new JButton("New");
					newButton.setToolTipText("Create a new concept");
					workflowPanel.add(newButton, c);
					c.gridx++;
					newButton.addActionListener(new NewActionListener());

					JButton stepButton = new JButton("Done");
					workflowPanel.add(stepButton, c);
					c.gridx++;
					stepButton.addActionListener(new StepActionListener());

					JButton stopButton = new JButton("Todo");
					workflowPanel.add(stopButton, c);
					stopButton.addActionListener(new StopActionListener());
					c.gridx++;

					workflowPanel.add(new JLabel("     "), c);
					workflowPanel.validate();
					Container cont = workflowPanel;
					while (cont != null) {
						cont.validate();
						cont = cont.getParent();
					}
					stepButton.requestFocusInWindow();
				}
			});
			synchronized (this) {
				this.waitTillDone(worker.getLogger());
			}
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					Component[] components = workflowPanel.getComponents();
					for (int i = 0; i < components.length; i++) {
						workflowPanel.remove(components[i]);
					}
					workflowPanel.validate();
					Container cont = workflowPanel;
					while (cont != null) {
						cont.validate();
						cont = cont.getParent();
					}
				}

			});
			config.setBuilderToggleVisible(builderVisible);
			config.setProgressToggleVisible(progressPanelVisible);
			config.setSubversionToggleVisible(subversionButtonVisible);
			config.setInboxToggleVisible(inboxButtonVisible);
			return returnCondition;
		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException(e);
		} catch (IntrospectionException e) {
			throw new TaskFailedException(e);
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
		return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public String getTermPropName() {
		return termPropName;
	}

	public void setTermPropName(String termPropName) {
		this.termPropName = termPropName;
	}

	protected String getTrueImage() {
		return "/16x16/plain/media_step_forward.png";
	}

	protected String getFalseImage() {
		return "/16x16/plain/media_stop_red.png";
	}
}
