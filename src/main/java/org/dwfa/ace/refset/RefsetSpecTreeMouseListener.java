package org.dwfa.ace.refset;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.log.AceLog;

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
		/*
		JTree tree = (JTree) e.getSource();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		int selRow = tree.getRowForLocation(e.getX(), e.getY());
		// AceLog.getLog().info("Selected row: " + selRow);
		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		if (selPath != null) {
			if (selRow != -1) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
						.getLastPathComponent();
				if (e.isPopupTrigger()) {
					makeAndShowPopup(e);
				}
			}
		}
		*/
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
		JPopupMenu popup;
		try {
			popup = makePopup(e);
			popup.show(e.getComponent(), e.getX(), e.getY());
		} catch (FileNotFoundException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (ClassNotFoundException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
	}

	private JPopupMenu makePopup(MouseEvent e) throws FileNotFoundException, IOException, ClassNotFoundException {
			JPopupMenu popup = new JPopupMenu();
			JMenuItem noActionItem = new JMenuItem("");
			popup.add(noActionItem);
			ProcessPopupUtil.addSubmenMenuItems(popup, new File(AceFrame.pluginRoot, "taxonomy"), 
					this.aceConfig.getWorker());
			return popup;
	}


}