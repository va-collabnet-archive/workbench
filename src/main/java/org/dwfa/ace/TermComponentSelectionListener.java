package org.dwfa.ace;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.tree.I_GetConceptDataForTree;

public class TermComponentSelectionListener implements TreeSelectionListener {

	I_ContainTermComponent linkedComponent;

	public TermComponentSelectionListener(I_ContainTermComponent linkedComponent) {
		super();
		this.linkedComponent = linkedComponent;
	}

	public void valueChanged(TreeSelectionEvent e) {
		Object obj = e.getPath().getLastPathComponent();
		I_GetConceptDataForTree cb;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
		cb = (I_GetConceptDataForTree) node.getUserObject();
		System.out.println("Selected: " + cb + " nativeId: " + cb.getConceptId());
		linkedComponent.setTermComponent(cb.getCoreBean());
	}

}
