/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.qa.gui.viewers.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.ihtsdo.qa.gui.viewers.TreeEditorObjectWrapper;
import org.ihtsdo.qa.gui.viewers.utils.IconUtilities;

/**
 * The Class DetailsIconRenderer.
 */
public class DetailsIconRenderer extends DefaultTreeCellRenderer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6407929046854012597L;

	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		if (node != null && (node.getUserObject() instanceof TreeEditorObjectWrapper)) {
			TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) node.getUserObject();
			if (nodeObject != null) {
				label.setIcon(IconUtilities.getIconForConceptDetails(nodeObject.getType()));
				if (nodeObject.getType() == IconUtilities.getDefined()) {
					label.setToolTipText("Fully defined concept");
				} else if (nodeObject.getType() == IconUtilities.getPrimitive()) {
					label.setToolTipText("Primitive concept");
				} else if (nodeObject.getType() == IconUtilities.getInactive()) {
					label.setToolTipText("Inactive concept");
				} else if (nodeObject.getType() == IconUtilities.getPrimitiveParent()) {
					label.setToolTipText("Primitive parent");
				} else if (nodeObject.getType() == IconUtilities.getDefinedParent()) {
					label.setToolTipText("Fully defined parent");
				} else if (nodeObject.getType() == IconUtilities.getInactiveParent()) {
					label.setToolTipText("Inactive parent");
				}
			}
		}
		return label;
	}

}
