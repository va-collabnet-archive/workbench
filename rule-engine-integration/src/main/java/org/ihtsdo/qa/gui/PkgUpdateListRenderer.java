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
package org.ihtsdo.qa.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.ihtsdo.rules.context.RulesDeploymentPackageReference;

/**
 * The Class PkgUpdateListRenderer.
 */
public class PkgUpdateListRenderer extends DefaultListCellRenderer {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new pkg update list renderer.
	 */
	public PkgUpdateListRenderer() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
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
