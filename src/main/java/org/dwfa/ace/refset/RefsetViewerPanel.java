package org.dwfa.ace.refset;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

public class RefsetViewerPanel extends JPanel {

	JTree viewerTree;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RefsetViewerPanel() {
		super(new GridLayout(1,1));
		viewerTree = new JTree();
		viewerTree.setCellRenderer(new RefsetSpecTreeCellRenderer());
		viewerTree.setRootVisible(false);
		viewerTree.setShowsRootHandles(true);
		add(new JScrollPane(viewerTree));
	}
	
	

}
