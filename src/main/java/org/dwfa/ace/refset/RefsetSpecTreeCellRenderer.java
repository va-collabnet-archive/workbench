package org.dwfa.ace.refset;

import javax.swing.tree.DefaultTreeCellRenderer;

public class RefsetSpecTreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @todo Get the AND/OR statements indented so it is clear that
	 *       they are not ANDing/ORing the siblings, just the descendants
	 *       Perhaps this should be handled by deliberately "ORing" all 
	 *       descendants, or by sorting the AND/OR statements to the 
	 *       end of the list...
	 *       
	 *       Put Spec concepts in a different color than the query input concepts...
	 *       
	 */
	public RefsetSpecTreeCellRenderer() {
		super();
		setLeafIcon(null);
		setClosedIcon(null);
		setOpenIcon(null);
	}

}
