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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 * The Class TestFrame.
 *
 * @author Guillermo Reynoso
 */
public class TestFrame extends JFrame {
	
	/** The concept. */
	private Object concept;

	/**
	 * Instantiates a new test frame.
	 */
	public TestFrame() {
		initComponents();
		initCustomComponents();
	}

	/**
	 * Instantiates a new test frame.
	 *
	 * @param concept the concept
	 */
	public TestFrame(I_GetConceptData concept) {
		this.concept = concept;
		initComponents();
		initCustomComponents();
	}

	/**
	 * Instantiates a new test frame.
	 *
	 * @param conceptBi the concept bi
	 */
	public TestFrame(ConceptVersionBI conceptBi) {
		this.concept = conceptBi;
		initComponents();
		initCustomComponents();
	}

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {
		try {
			if (concept == null) {
				contentSplitPanel.setLeftComponent(new ConceptDetailsPanel());
			} else if (concept instanceof ConceptVersionBI) {
				contentSplitPanel.setLeftComponent(new ConceptDetailsPanel((ConceptVersionBI) concept));
			} else if (concept instanceof I_GetConceptData) {
				contentSplitPanel.setLeftComponent(new ConceptDetailsPanel((I_GetConceptData) concept));
			}
			HierarchyNavigator hierarchyNav = new HierarchyNavigator();
			contentSplitPanel.setRightComponent(hierarchyNav);
			
			if(concept != null && concept instanceof I_GetConceptData){
				hierarchyNav.setFocusConcept((I_GetConceptData)concept);
			}
			Dimension dim = new Dimension(850, 600);
			this.setSize(dim);
			this.setPreferredSize(dim);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		contentSplitPanel = new JSplitPane();

		// ======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		((GridBagLayout) contentPane.getLayout()).columnWidths = new int[] { 0, 0 };
		((GridBagLayout) contentPane.getLayout()).rowHeights = new int[] { 0, 0 };
		((GridBagLayout) contentPane.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) contentPane.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

		// ======== contentSplitPanel ========
		{
			contentSplitPanel.setResizeWeight(0.5);
			contentSplitPanel.setDividerSize(2);
		}
		contentPane.add(contentSplitPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The content split panel. */
	private JSplitPane contentSplitPanel;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
