package org.ihtsdo.qa.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.UUID;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.ihtsdo.rules.context.RulesDeploymentPackageReference;

public class MapPkgListRenderer extends DefaultListCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<UUID, Color> map;

	public MapPkgListRenderer(HashMap<UUID, Color> map) {
		super();
		this.map = map;
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
		setBackground(Color.WHITE);
		for (UUID loopUuid : map.keySet()) {
			if (pkg.getUuids().contains(loopUuid)) {
				setBackground(map.get(loopUuid));
			}
		}
		return this;
	}
}
