package org.dwfa.ace.refset;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.dwfa.ace.api.I_ConfigAceFrame;

public class RefsetViewerPanel extends JPanel {

	JTree viewerTree;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RefsetViewerPanel(I_ConfigAceFrame configAceFrame) {
		super(new GridLayout(1,1));
		viewerTree = new JTree();
		viewerTree.setCellRenderer(new DefaultTreeCellRenderer());
		viewerTree.setRootVisible(false);
		viewerTree.setShowsRootHandles(true);
		add(new JScrollPane(viewerTree));
	}
	
	

}
