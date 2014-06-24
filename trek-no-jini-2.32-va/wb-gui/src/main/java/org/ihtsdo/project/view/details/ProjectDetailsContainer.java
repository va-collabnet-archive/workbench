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

package org.ihtsdo.project.view.details;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

/**
 * The Class ProjectDetailsContainer.
 *
 * @author Alejandro Rodriguez
 */
public class ProjectDetailsContainer extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The guest panel. */
	private JPanel guestPanel;
	
	/**
	 * Instantiates a new project details container.
	 */
	public ProjectDetailsContainer() {
		initComponents();
	}
	
	/**
	 * Adds the content.
	 *
	 * @param panel the panel
	 */
	public void addContent(JPanel panel){
		if (panel!=null){
			this.guestPanel=panel;
			add(this.guestPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
		}
	}
	
	/**
	 * Removes the content.
	 */
	public void removeContent(){
		if (this.guestPanel!=null){
			remove(this.guestPanel);
			this.guestPanel=null;
		}
	}
	
	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
