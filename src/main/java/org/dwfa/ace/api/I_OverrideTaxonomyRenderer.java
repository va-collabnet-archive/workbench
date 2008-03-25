package org.dwfa.ace.api;

import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JTree;

import org.dwfa.tapi.TerminologyException;

public interface I_OverrideTaxonomyRenderer {

	/**
	 * This method is passed in the rendered component based on the default rendering. The method may override or provide
	 * any decorations desired to provide special displays, such as providing special annotations for refset types or for
	 * spelling concerns. 
	 * @param component the rendered component
	 * @param tree the tree this component will be contained within
	 * @param concept the concept that this node corresponds to
	 * @param sel true if selected
	 * @param expanded true if expanded
	 * @param leaf true if a leaf node
	 * @param row the row of the component
	 * @param hasFocus true if 
	 * @param frameConfig
	 * @return the rendered component with any overrides or decorations deemed appropriate by the override class. 
	 */
	public JLabel overrideTreeCellRendererComponent(JLabel component, JTree tree, I_GetConceptData concept, boolean sel, boolean expanded, boolean leaf,
	         int row, boolean hasFocus, I_ConfigAceFrame frameConfig) throws TerminologyException, IOException;
	
}
