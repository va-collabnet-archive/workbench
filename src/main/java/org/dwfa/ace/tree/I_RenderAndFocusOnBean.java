package org.dwfa.ace.tree;

import java.awt.Rectangle;

import javax.swing.tree.TreeCellRenderer;

import org.dwfa.vodb.types.ConceptBean;

public interface I_RenderAndFocusOnBean extends TreeCellRenderer {

	public ConceptBean getFocusBean();

	public void setFocusBean(ConceptBean focusBean);
	
	public Rectangle getIconRect(int parentDepth);

}