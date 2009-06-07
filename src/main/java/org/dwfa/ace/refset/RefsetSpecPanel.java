package org.dwfa.ace.refset;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

public class RefsetSpecPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	RefsetEditorPanel editor;
	RefsetViewerPanel viewer;

	public RefsetSpecPanel(ACE ace) throws Exception {
		super(new GridLayout(1,1));
		JSplitPane split = new JSplitPane();
		split.setOneTouchExpandable(true);
		add(split);
		editor = new RefsetEditorPanel(ace);
		split.setLeftComponent(editor);
		viewer = new RefsetViewerPanel(ace.getAceFrameConfig(), editor);
		split.setRightComponent(viewer);
		split.setDividerLocation(700);
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
