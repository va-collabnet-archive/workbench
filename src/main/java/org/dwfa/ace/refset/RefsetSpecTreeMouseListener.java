package org.dwfa.ace.refset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinExtBinder;

public class RefsetSpecTreeMouseListener implements MouseListener {

	private I_ConfigAceFrame aceConfig;

	public RefsetSpecTreeMouseListener(I_ConfigAceFrame aceConfig) {
		super();
		this.aceConfig = aceConfig;
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
			
			List<I_ThinExtByRefTuple> tuples = specPart.getTuples(aceConfig.getAllowedStatus(), aceConfig.getViewPositionSet(), true);
			
			if (tuples.iterator().hasNext()) {
				JMenuItem retireActionItem = new JMenuItem("Retire");
				retireActionItem.addActionListener(new RetireSpecAction(tuples.iterator().next()));
				popup.add(retireActionItem);
			} else {
				tuples = specPart.getTuples(null, aceConfig.getViewPositionSet(), true);
			}
			if (tuples.iterator().hasNext()) {
				JMenuItem changeActionItem = new JMenuItem("Change...");
				changeActionItem.addActionListener(new ChangeSpecAction(tuples.iterator().next()));
				popup.add(changeActionItem);
			} 
			
		}
		
		return popup;
	}
	
	private static class RetireSpecAction implements ActionListener {
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
		}		
	}
	private static class ChangeSpecAction implements ActionListener {
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
		}		
	}

}