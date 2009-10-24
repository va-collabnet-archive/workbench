package org.dwfa.ace.refset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.IntSet;

public class RefsetSpecTreeMouseListener implements MouseListener {

	private I_ConfigAceFrame aceConfig;
	private RefsetSpecEditor specEditor;

	public RefsetSpecTreeMouseListener(I_ConfigAceFrame aceConfig, RefsetSpecEditor specEditor) {
		super();
		this.aceConfig = aceConfig;
		this.specEditor = specEditor;
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			makeAndShowPopup(e);
		}
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			makeAndShowPopup(e);
		}
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	private void makeAndShowPopup(MouseEvent e) {
		JPopupMenu popup = null;
		if (e.isPopupTrigger()) {
			try {
				JTree tree = (JTree) e.getSource();
				int rowForLocation = tree.getRowForLocation(e.getX(), e.getY());
				int [] selectedRow = tree.getSelectionRows();
				if (rowForLocation < 0 || selectedRow == null || selectedRow[0] != rowForLocation) {
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
							I_ThinExtByRefVersioned specPart = (I_ThinExtByRefVersioned) node
									.getUserObject();
							switch (ThinExtBinder.getExtensionType(specPart)) {
							case CONCEPT_CONCEPT:
								popup = makePopup(e, new File(AceFrame.pluginRoot,
								"refsetspec/branch-popup"), specPart);
								break;
								
							case CONCEPT_CONCEPT_CONCEPT:
								popup = makePopup(e, new File(AceFrame.pluginRoot,
								"refsetspec/structural-query-popup"), specPart);
								break;
								
							case CONCEPT_CONCEPT_STRING:
								popup = makePopup(e, new File(AceFrame.pluginRoot,
								"refsetspec/text-query-popup"), specPart);
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

	private JPopupMenu makePopup(MouseEvent e, File directory, I_ThinExtByRefVersioned specPart)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem noActionItem = new JMenuItem("");
		popup.add(noActionItem);
		ProcessPopupUtil.addSubmenMenuItems(popup, directory, this.aceConfig
				.getWorker());
		if (specPart != null) {
			popup.addSeparator();
			
			boolean uncommitted = false;
			for (I_ThinExtByRefPart part: specPart.getVersions()) {
				if (part.getVersion() == Integer.MAX_VALUE) {
					uncommitted = true;
					break;
				}
			}
			if (uncommitted) {
				JMenuItem cancelActionItem = new JMenuItem("Cancel change");
				cancelActionItem.addActionListener(new CancelChangeAction(specPart));
				popup.add(cancelActionItem);
			} else {
				List<I_ThinExtByRefTuple> tuples = specPart.getTuples(aceConfig.getAllowedStatus(), aceConfig.getViewPositionSet(), true);
				
				if (tuples.iterator().hasNext()) {
					I_ThinExtByRefTuple firstTuple = tuples.iterator().next();
					JMenuItem commentActionItem = new JMenuItem("Comment");
					commentActionItem.addActionListener(new CommentSpecAction(firstTuple));
					popup.add(commentActionItem);
					popup.addSeparator();
					JMenuItem retireActionItem = new JMenuItem("Retire");
					retireActionItem.addActionListener(new RetireSpecAction(firstTuple));
					popup.add(retireActionItem);
					
					JMenuItem changeActionItem = new JMenuItem("Change...");
					changeActionItem.addActionListener(new ChangeSpecAction(firstTuple));
					popup.add(changeActionItem);
				} else {
					tuples = specPart.getTuples(null, aceConfig.getViewPositionSet(), true);
				} 
			}
			
		}
		
		return popup;
	}
	
	private class CommentSpecAction implements ActionListener {
		private I_ThinExtByRefTuple thinExtByRefTuple;
		
		private CommentSpecAction(I_ThinExtByRefTuple thinExtByRefTuple) {
			super();
			this.thinExtByRefTuple = thinExtByRefTuple;
		}

		public void actionPerformed(ActionEvent arg0) {
			String commentText = (String)JOptionPane.showInputDialog(
						aceConfig.getTreeInSpecEditor().getRootPane(),
			                    "",
			                    "Enter comment:",
			                    JOptionPane.PLAIN_MESSAGE,
			                    null,
			                    null,
			                    "");
			if (commentText != null && commentText.length() > 2) {
				try {
					I_GetConceptData refsetIdentityConcept = aceConfig.getRefsetInSpecEditor();
					I_TermFactory tf = LocalVersionedTerminology.get();
					I_IntSet allowedTypes = new IntSet();
					allowedTypes.add(RefsetAuxiliary.Concept.COMMENTS_REL.localize().getNid());
					Set<I_GetConceptData> commentRefsets = 
						refsetIdentityConcept.getSourceRelTargets(aceConfig.getAllowedStatus(), 
							allowedTypes, 
							aceConfig.getViewPositionSet(), false);
					int newMemberId = tf.uuidToNativeWithGeneration(UUID.randomUUID(), 
							ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), 
							aceConfig.getEditingPathSet(), 
							Integer.MAX_VALUE);
					if (commentRefsets.size() > 0) {
						I_GetConceptData commentRefsetIdentityConcept = commentRefsets.iterator().next();
						I_ThinExtByRefVersioned commentExt = tf.newExtension(
								commentRefsetIdentityConcept.getConceptId(), 
								newMemberId, 
								thinExtByRefTuple.getMemberId(), 
								RefsetAuxiliary.Concept.STRING_EXTENSION.localize().getNid());
						LocalVersionedTerminology.get().addUncommitted(thinExtByRefTuple.getCore());
						for (I_Path p: aceConfig.getEditingPathSet()) {
							I_ThinExtByRefPartString commentPart = LocalVersionedTerminology.get().newStringExtensionPart();
							commentPart.setStringValue(commentText);
							commentPart.setPathId(p.getConceptId());
							commentPart.setStatusId(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
							commentPart.setVersion(Integer.MAX_VALUE);
							commentExt.addVersion(commentPart);
						}
						tf.addUncommitted(commentExt);
					}
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}		
	}
	private class RetireSpecAction implements ActionListener {
		private I_ThinExtByRefTuple thinExtByRefTuple;
		
		private RetireSpecAction(I_ThinExtByRefTuple thinExtByRefTuple) {
			super();
			this.thinExtByRefTuple = thinExtByRefTuple;
		}

		public void actionPerformed(ActionEvent arg0) {
			I_ThinExtByRefPart newPart = thinExtByRefTuple.getPart().duplicate();
			newPart.setVersion(Integer.MAX_VALUE);
			try {
				newPart.setStatusId(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
			} catch (IOException e) {
				throw new RuntimeException();
			} catch (TerminologyException e) {
				throw new RuntimeException();
			}
			thinExtByRefTuple.getCore().addVersion(newPart);
			LocalVersionedTerminology.get().addUncommitted(thinExtByRefTuple.getCore());
			specEditor.updateSpecTree(false);
		}		
	}
	private class CancelChangeAction implements ActionListener {
		private I_ThinExtByRefVersioned specPart;
		
		private CancelChangeAction(I_ThinExtByRefVersioned specPart) {
			super();
			this.specPart = specPart;
		}

		public void actionPerformed(ActionEvent arg0) {
			try {
				List<I_ThinExtByRefPart> partsToRemove = new ArrayList<I_ThinExtByRefPart>();
				for (I_ThinExtByRefPart part : specPart.getVersions()) {
					if (part.getVersion() == Integer.MAX_VALUE) {
						partsToRemove.add(part);
					}
				}
				ExtensionByReferenceBean ebrBean = (ExtensionByReferenceBean) LocalVersionedTerminology
						.get().getExtensionWrapper(specPart.getMemberId());
				specPart.getVersions().removeAll(partsToRemove);
				if (specPart.getVersions().size() == 0) {
					ebrBean.discard();
				}
				LocalVersionedTerminology.get().addUncommitted(specPart);
				if (specPart.getVersions().size() == 0) {
					ebrBean.abort();
				}
				specEditor.updateSpecTree(false);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}		
	}
	private class ChangeSpecAction implements ActionListener {
		private I_ThinExtByRefTuple thinExtByRefTuple;
		
		private ChangeSpecAction(I_ThinExtByRefTuple thinExtByRefTuple) {
			super();
			this.thinExtByRefTuple = thinExtByRefTuple;
		}

		public void actionPerformed(ActionEvent arg0) {
			I_ThinExtByRefPart newPart = thinExtByRefTuple.getPart().duplicate();
			newPart.setVersion(Integer.MAX_VALUE);
			thinExtByRefTuple.getCore().addVersion(newPart);
			LocalVersionedTerminology.get().addUncommitted(thinExtByRefTuple.getCore());
			specEditor.updateSpecTree(false);
		}		
	}

}