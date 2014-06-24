package org.ihtsdo.arena;

import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;

public class PreferencesNode extends DefaultMutableTreeNode {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static class PrefObject implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String prefLabel;
		private JComponent prefPanel;
		
		@Override
		public String toString() {
			return prefLabel;
		}
	}

	public PreferencesNode(String prefLabel, JComponent prefPanel) {
		super(new PrefObject());
		PrefObject pref = (PrefObject) getUserObject();
		pref.prefLabel = prefLabel;
		pref.prefPanel = prefPanel;
	}

	public JComponent getPrefPanel() {
		return ((PrefObject) getUserObject()).prefPanel;
	}
	
}
