package org.dwfa.ace.refset;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.log.AceLog;
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
							"refsetspec/spec-popup"));
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
								"refsetspec/branch-popup"));
								break;
								
							case CONCEPT_CONCEPT_CONCEPT:
								popup = makePopup(e, new File(AceFrame.pluginRoot,
								"refsetspec/structural-query-popup"));
								break;
								
							case CONCEPT_CONCEPT_STRING:
								popup = makePopup(e, new File(AceFrame.pluginRoot,
								"refsetspec/text-query-popup"));
								break;
							default:
								popup = null;
							}
						}
					} else {
						popup = makePopup(e, new File(AceFrame.pluginRoot,
								"refsetspec/spec-popup"));
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

	private JPopupMenu makePopup(MouseEvent e, File directory)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem noActionItem = new JMenuItem("");
		popup.add(noActionItem);
		ProcessPopupUtil.addSubmenMenuItems(popup, directory, this.aceConfig
				.getWorker());
		return popup;
	}

}