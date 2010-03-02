/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.refset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class RefsetSpecTreeMouseListener extends MouseAdapter {

	private I_ConfigAceFrame aceConfig;
	private RefsetSpecEditor specEditor;

	public RefsetSpecTreeMouseListener(I_ConfigAceFrame aceConfig,
			RefsetSpecEditor specEditor) {
		super();
		this.aceConfig = aceConfig;
		this.specEditor = specEditor;
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			try {
				makeAndShowPopup(e);
			} catch (TerminologyException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			try {
				makeAndShowPopup(e);
			} catch (TerminologyException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private void makeAndShowPopup(MouseEvent e) throws TerminologyException {
		JPopupMenu popup = null;
		if (e.isPopupTrigger()) {
			try {
				JTree tree = (JTree) e.getSource();
				int rowForLocation = tree.getRowForLocation(e.getX(), e.getY());
				int[] selectedRow = tree.getSelectionRows();
				if (rowForLocation < 0 || selectedRow == null
						|| selectedRow[0] != rowForLocation) {
					tree.clearSelection();
					popup = makePopup(e, new File(AceFrame.pluginRoot,
							"refsetspec/spec-popup"), null);
				} else {
					TreePath selPath = tree.getPathForLocation(e.getX(), e
							.getY());
					if (selPath != null) {
						if (rowForLocation != -1) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
									.getLastPathComponent();
							I_ExtendByRef specPart = (I_ExtendByRef) node
									.getUserObject();
							switch (EConcept.REFSET_TYPES.nidToType(specPart.getTypeId())) {
							case CID_CID:
								popup = makePopup(e, new File(
										AceFrame.pluginRoot,
										"refsetspec/branch-popup"), specPart);
								break;

							case CID_CID_CID:
								popup = makePopup(e, new File(
										AceFrame.pluginRoot,
										"refsetspec/structural-query-popup"),
										specPart);
								break;

							case CID_CID_STR:
								popup = makePopup(e, new File(
										AceFrame.pluginRoot,
										"refsetspec/text-query-popup"),
										specPart);
								break;
							default:
								popup = null;
							}
						}
					} else {
						popup = makePopup(e, new File(AceFrame.pluginRoot,
								"refsetspec/spec-popup"), null);
					}
				}
				if (popup != null) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			} catch (FileNotFoundException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			} catch (IOException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			} catch (ClassNotFoundException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}
		}
	}

	private JPopupMenu makePopup(MouseEvent e, File directory,
			I_ExtendByRef specPart) throws FileNotFoundException,
			IOException, ClassNotFoundException, TerminologyException {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem noActionItem = new JMenuItem("");
		popup.add(noActionItem);
		ProcessPopupUtil.addSubmenMenuItems(popup, directory, this.aceConfig
				.getWorker());
		if (specPart != null) {
			popup.addSeparator();

			boolean uncommitted = false;
			for (I_ExtendByRefPart part : specPart.getMutableParts()) {
				if (part.getVersion() == Integer.MAX_VALUE) {
					uncommitted = true;
					break;
				}
			}
			if (uncommitted) {
				JMenuItem cancelActionItem = new JMenuItem("Cancel change");
				cancelActionItem.addActionListener(new CancelChangeAction(
						specPart));
				popup.add(cancelActionItem);
			} else {
				List<I_ExtendByRefVersion> tuples = (List<I_ExtendByRefVersion>) specPart
						.getTuples(aceConfig.getAllowedStatus(), aceConfig
								.getViewPositionSetReadOnly(), true);

				if (tuples.iterator().hasNext()) {
					I_ExtendByRefVersion firstTuple = tuples.iterator().next();
					I_GetConceptData refsetConcept = Terms.get().getConcept(
							firstTuple.getRefsetId());
					I_DescriptionTuple refsetDesc = refsetConcept.getDescTuple(
							aceConfig.getTableDescPreferenceList(), aceConfig);
					String prompt = "Add comment for '" + refsetDesc.getText()
							+ "'";
					JMenuItem commentActionItem = new JMenuItem(prompt + "...");
					commentActionItem.addActionListener(new CommentSpecAction(
							firstTuple, prompt));
					popup.add(commentActionItem);
					popup.addSeparator();
					JMenuItem retireActionItem = new JMenuItem("Retire");
					retireActionItem.addActionListener(new RetireSpecAction(
							firstTuple));
					popup.add(retireActionItem);

					JMenuItem changeActionItem = new JMenuItem("Change...");
					changeActionItem.addActionListener(new ChangeSpecAction(
							firstTuple));
					popup.add(changeActionItem);
				} else {
					tuples = (List<I_ExtendByRefVersion>) specPart.getTuples(
							null, aceConfig.getViewPositionSetReadOnly(), true);
				}
			}

		}

		return popup;
	}

	private class CommentSpecAction implements ActionListener {
		private I_ExtendByRefVersion thinExtByRefTuple;
		private String prompt;

		private CommentSpecAction(I_ExtendByRefVersion thinExtByRefTuple,
				String prompt) {
			super();
			this.thinExtByRefTuple = thinExtByRefTuple;
			this.prompt = prompt;
		}

		public void actionPerformed(ActionEvent arg0) {
			String commentText = (String) JOptionPane.showInputDialog(aceConfig
					.getTreeInSpecEditor().getRootPane(), "", prompt
					+ ":             ", JOptionPane.PLAIN_MESSAGE, null, null,
					"");
			if (commentText != null && commentText.length() > 2) {
				try {
					I_GetConceptData refsetIdentityConcept = aceConfig
							.getRefsetInSpecEditor();
					I_HelpRefsets refsetHelper = Terms.get().getRefsetHelper(
							aceConfig);
					Set<? extends I_GetConceptData> commentRefsets = refsetHelper
							.getCommentsRefsetForRefset(refsetIdentityConcept,
									aceConfig);
					if (commentRefsets.size() > 0) {
						for (I_GetConceptData commentRefsetIdentityConcept : commentRefsets) {
							RefsetPropertyMap refsetMap = new RefsetPropertyMap(
									REFSET_TYPES.STR);
							refsetMap.put(REFSET_PROPERTY.STRING_VALUE,
									commentText);
							I_ExtendByRef newExtension = refsetHelper
									.getOrCreateRefsetExtension(
											commentRefsetIdentityConcept
													.getNid(),
											thinExtByRefTuple.getMemberId(),
											REFSET_TYPES.STR, refsetMap);
							Terms.get().addUncommittedNoChecks(newExtension);
						}
					}
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}
	}

	private class RetireSpecAction implements ActionListener {
		private I_ExtendByRefVersion thinExtByRefTuple;

		private RetireSpecAction(I_ExtendByRefVersion thinExtByRefTuple) {
			super();
			this.thinExtByRefTuple = thinExtByRefTuple;
		}

		public void actionPerformed(ActionEvent arg0) {
			try {
				I_ExtendByRefPart currentPart = thinExtByRefTuple
						.getMutablePart();
				I_ExtendByRefPart newPart = (I_ExtendByRefPart) currentPart
						.makeAnalog(ArchitectonicAuxiliary.Concept.RETIRED
								.localize().getNid(), currentPart.getPathId(),
								Long.MAX_VALUE);
				thinExtByRefTuple.getCore().addVersion(newPart);
			} catch (IOException e) {
				throw new RuntimeException();
			} catch (TerminologyException e) {
				throw new RuntimeException();
			}
			Terms.get().addUncommitted(
					thinExtByRefTuple.getCore());
			specEditor.updateSpecTree(false);
		}
	}

	private class CancelChangeAction implements ActionListener {
		private I_ExtendByRef specPart;

		private CancelChangeAction(I_ExtendByRef specPart) {
			super();
			this.specPart = specPart;
		}

		public void actionPerformed(ActionEvent arg0) {
			List<I_ExtendByRefPart> partsToRemove = new ArrayList<I_ExtendByRefPart>();
			for (I_ExtendByRefPart part : specPart.getMutableParts()) {
				if (part.getVersion() == Integer.MAX_VALUE) {
					partsToRemove.add(part);
				}
			}
			specPart.getMutableParts().removeAll(partsToRemove);
			if (specPart.getMutableParts().size() == 0) {
				try {
					Terms.get().forget(specPart);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
			specEditor.updateSpecTree(false);
		}
	}

	private class ChangeSpecAction implements ActionListener {
		private I_ExtendByRefVersion thinExtByRefTuple;

		private ChangeSpecAction(I_ExtendByRefVersion thinExtByRefTuple) {
			super();
			this.thinExtByRefTuple = thinExtByRefTuple;
		}

		public void actionPerformed(ActionEvent arg0) {

			I_ExtendByRefPart current = thinExtByRefTuple.getMutablePart();
			I_ExtendByRefPart newPart = (I_ExtendByRefPart) current
					.makeAnalog(current.getStatusId(), current.getPathId(),
							Long.MAX_VALUE);

			thinExtByRefTuple.getCore().addVersion(newPart);
			Terms.get().addUncommitted(
					thinExtByRefTuple.getCore());
			specEditor.updateSpecTree(false);
		}
	}

}
