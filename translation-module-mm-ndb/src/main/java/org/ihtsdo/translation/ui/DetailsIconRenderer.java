package org.ihtsdo.translation.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.translation.TreeEditorObjectWrapper;

public class DetailsIconRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(
				JTree tree,
				Object value,
				boolean sel,
				boolean expanded,
				boolean leaf,
				int row,
				boolean hasFocus) {

			JLabel label=(JLabel)super.getTreeCellRendererComponent(
					tree, value, sel,
					expanded, leaf, row,
					hasFocus);
			DefaultMutableTreeNode node =  (DefaultMutableTreeNode)value;
			if (node!=null && (node.getUserObject() instanceof TreeEditorObjectWrapper)){
				TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) node.getUserObject();
				if (nodeObject!=null){
					label.setIcon(IconUtilities.getIconForConceptDetails(nodeObject.getType()));
					switch (nodeObject.getType()){
					case IconUtilities.DEFINED: label.setToolTipText("Fully defined concept");break; 
					case IconUtilities.PRIMITIVE: label.setToolTipText("Primitive concept");break;
					case IconUtilities.INACTIVE: label.setToolTipText("Inactive concept");break;
					case IconUtilities.PRIMITIVE_PARENT: label.setToolTipText("Primitive parent");break; 
					case IconUtilities.DEFINED_PARENT: label.setToolTipText("Fully defined parent");break;
					case IconUtilities.INACTIVE_PARENT: label.setToolTipText("Inactive parent");break;
					}
				}
			}
			return label;
		}

}
