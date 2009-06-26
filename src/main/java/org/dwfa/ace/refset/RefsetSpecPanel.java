package org.dwfa.ace.refset;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.tree.TermTreeHelper;

public class RefsetSpecPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	RefsetSpecEditor editor;

	public RefsetSpecPanel(ACE ace) throws Exception {
		super(new GridBagLayout());
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setOneTouchExpandable(true);
		editor = new RefsetSpecEditor(ace);
		split.setTopComponent(editor.getContentPanel());
		TermTreeHelper treeHelper = new TermTreeHelper(ace.getAceFrameConfig());
		JTabbedPane bottomTabs = new JTabbedPane();
		bottomTabs.addTab("table view", new JLabel("insert table here"));
		bottomTabs.addTab("hierarchical view", treeHelper.getHierarchyPanel());
		split.setBottomComponent(bottomTabs);
		split.setDividerLocation(200);
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.BOTH;

		add(editor.getTopPanel(), c);
		c.gridy++;
		c.weightx = 1.0;
		c.weighty = 1.0;
		add(split, c);
		
	}

	public I_GetConceptData getRefsetInSpecEditor() {
		return (I_GetConceptData) editor.getTermComponent();
	}

	public I_ThinExtByRefVersioned getSelectedRefsetClauseInSpecEditor() {
		return getSelectedRefsetClauseInSpecEditor();
	}

	public JTree getTreeInSpecEditor() {
		return editor.getTreeInSpecEditor();
	}

	public I_GetConceptData getRefsetSpecInSpecEditor() {
		return editor.getRefsetSpecInSpecEditor();
	}
}
