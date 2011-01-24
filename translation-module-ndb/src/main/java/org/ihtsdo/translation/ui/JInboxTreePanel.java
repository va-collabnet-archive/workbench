/*
 * Created by JFormDesigner on Mon Mar 15 18:06:53 GMT-03:00 2010
 */

package org.ihtsdo.translation.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

/**
 * @author Alejandro Rodriguez
 */
public class JInboxTreePanel extends MenuComponentPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JInboxTreePanel() {
		initComponents();
		tree1.setRootVisible(false);
		tree1.setShowsRootHandles(false);
	}

	public void setTreeModel(DefaultTreeModel treeModel){
		this.tree1.setModel(treeModel);
		this.tree1.revalidate();
	}
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		scrollPane1 = new JScrollPane();
		tree1 = new JTree();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(tree1);
		}
		add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JScrollPane scrollPane1;
	private JTree tree1;
	private String labelText;
	private Icon icon;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	public JTree getTree() {
		return tree1;
	}

	public void setTreeMouseListener(){
//			InboxTreeMouselistener inboxTreeMouselistener) {
//		this.tree1.addMouseListener(inboxTreeMouselistener);
		
	}

	@Override
	public Icon getLabelIcon() {
		return icon;
	}

	@Override
	public String getLabelText() {
		return labelText;
	}

	@Override
	public void setLabelIcon(Icon icon) {
		this.icon=icon;
	}

	@Override
	public void setLabelText(String labelText) {
		this.labelText=labelText;
	}

}
