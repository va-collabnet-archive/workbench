package org.dwfa.ace;

import java.awt.Component;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.tree.I_GetConceptDataForTree;
import org.dwfa.vodb.types.ConceptBean;

public class TermComponentSelectionListener implements TreeSelectionListener {

	I_ContainTermComponent linkedComponent;

	public TermComponentSelectionListener(I_ContainTermComponent linkedComponent) {
		super();
		this.linkedComponent = linkedComponent;
	}

	public void valueChanged(TreeSelectionEvent e) {
		try {
			ConceptBean currentBean = (ConceptBean) linkedComponent
					.getTermComponent();
			if (currentBean != null) {
				if (currentBean.isUncommitted()) {
					int option = JOptionPane.showConfirmDialog((Component) linkedComponent, 
							"This view contains an uncommited concept. If you continue, the " +
							"focused concept will change...", 
							"Uncommitted component", 
							JOptionPane.OK_CANCEL_OPTION);
					if (JOptionPane.OK_OPTION == option) {
						setLinkedComponent(e);
					}
				} else {
					setLinkedComponent(e);
				}
			} else {
				setLinkedComponent(e);
			}
		} catch (IOException e1) {
			AceLog.getLog().alertAndLog(Level.SEVERE, e1.getLocalizedMessage(), e1);
		}
	}

	private void setLinkedComponent(TreeSelectionEvent e) {
		Object obj = e.getPath().getLastPathComponent();
		I_GetConceptDataForTree cb;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
		cb = (I_GetConceptDataForTree) node.getUserObject();
		linkedComponent.setTermComponent(cb.getCoreBean());
	}

}
