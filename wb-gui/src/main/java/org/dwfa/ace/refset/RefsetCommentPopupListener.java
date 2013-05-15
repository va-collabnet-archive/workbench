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
import java.io.IOException;
import java.util.Collection;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.refset.RefsetSpec;
import org.ihtsdo.tk.refset.SpecRefsetHelper;

public class RefsetCommentPopupListener extends MouseAdapter {
	private I_ConfigAceFrame config;
	private RefsetSpecEditor refsetSpecEditor;
	private String prompt;
	private JPopupMenu popup;
	private I_GetConceptData conceptForComment;

	public RefsetCommentPopupListener(I_ConfigAceFrame config,
			RefsetSpecEditor refsetSpecEditor) {
		super();
		this.config = config;
		this.refsetSpecEditor = refsetSpecEditor;
	}

	public CommentAction getActionListener() {
		return new CommentAction();
	}

	private class CommentAction implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			String commentText = (String) JOptionPane.showInputDialog(config
					.getTreeInSpecEditor().getRootPane(), "", prompt
					+ ":             ", JOptionPane.PLAIN_MESSAGE, null, null,
					"");
			if (commentText != null && commentText.length() > 2) {
				try {
					I_GetConceptData refsetIdentityConcept = config
							.getRefsetInSpecEditor();
					SpecRefsetHelper refsetHelper = new SpecRefsetHelper(config.getViewCoordinate(), config.getEditCoordinate());
                                        RefsetSpec specHelper = new RefsetSpec(refsetIdentityConcept, true, config.getViewCoordinate());

					Collection<? extends ConceptVersionBI> commentRefsets = specHelper.getCommentsRefsetConcepts();
					if (commentRefsets.size() > 0) {
						for (ConceptVersionBI commentRefsetIdentityConcept : commentRefsets) {
							    I_ExtendByRef newExtension =
                                                            (I_ExtendByRef) refsetHelper.newStringRefsetExtension(commentRefsetIdentityConcept.getNid(),
                                                            conceptForComment.getConceptNid(),
                                                            commentText);
                                                    Terms.get().addUncommitted(newExtension);
						}
					}
					refsetSpecEditor.getCommentTableModel().propertyChange(null);
					refsetSpecEditor.getRefsetSpecPanel()
							.getCommentTableModel().propertyChange(null);
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}
	}

	private void makePopup(MouseEvent e) {
		try {
			conceptForComment = config.getRefsetInSpecEditor();
			popup = null;
			popup = new JPopupMenu();
			JMenuItem noActionItem = new JMenuItem("");
			popup.add(noActionItem);
			createPrompt();
			JMenuItem commentItem = new JMenuItem(prompt + "...");
			popup.add(commentItem);
			commentItem.addActionListener(new CommentAction());

		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
	}

	private void createPrompt() throws IOException {
		I_GetConceptData refsetConcept = config.getRefsetInSpecEditor();
		I_DescriptionTuple refsetDesc = refsetConcept.getDescTuple(config
				.getTableDescPreferenceList(), config);
		prompt = "Add comment for '" + refsetDesc.getText() + "'";
	}

	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			if (config.getEditingPathSet().size() > 0) {
				makePopup(e);
				if (popup != null) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			} else {
				JOptionPane.showMessageDialog(config.getTreeInTaxonomyPanel()
						.getTopLevelAncestor(),
						"You must select at least one path to edit on...");
			}
			e.consume();
		}
	}

	public String getPrompt() throws IOException {
		if (prompt == null) {
			createPrompt();
		}
		return prompt;
	}

	public I_GetConceptData getConceptForComment() {
		return conceptForComment;
	}

	public void setConceptForComment(I_GetConceptData conceptForComment) {
		this.conceptForComment = conceptForComment;
	}
}
