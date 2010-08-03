package org.ihtsdo.qa.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.ihtsdo.rules.context.RulesDeploymentPackageReference;

public class PkgUpdateListRenderer extends DefaultListCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PkgUpdateListRenderer() {
		super();
	}

	public Component getListCellRendererComponent(JList list, 
			Object value,
			int index, 
			boolean isSelected,
			boolean cellHasFocus) {

		super.getListCellRendererComponent(list, 
				value, 
				index, 
				isSelected, 
				cellHasFocus);
		RulesDeploymentPackageReference pkg = (RulesDeploymentPackageReference) value;
		try {
			pkg.updateKnowledgeBase();
			setBackground(Color.GREEN);
		} catch (Exception e) {
			setBackground(Color.RED);
		}
		return this;
	}
}
