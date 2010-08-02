package org.ihtsdo.qa.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.ihtsdo.rules.context.RulesDeploymentPackageReference;

public class PkgValidationListRenderer extends DefaultListCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PkgValidationListRenderer() {
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
		if (pkg.validate()) {
			setBackground(Color.GREEN);
		} else {
			setBackground(Color.RED);
		}
		return this;
	}
}
