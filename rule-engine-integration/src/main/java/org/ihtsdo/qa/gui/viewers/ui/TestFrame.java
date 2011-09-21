/*
 * Created by JFormDesigner on Tue Aug 09 16:15:31 GMT-03:00 2011
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
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 * @author Guillermo Reynoso
 */
public class TestFrame extends JFrame {
	private Object concept;

	public TestFrame() {
		initComponents();
		initCustomComponents();
	}

	public TestFrame(I_GetConceptData concept) {
		this.concept = concept;
		initComponents();
		initCustomComponents();
	}

	public TestFrame(ConceptVersionBI conceptBi) {
		this.concept = conceptBi;
		initComponents();
		initCustomComponents();
	}

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
			e.printStackTrace();
		}

	}

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
	private JSplitPane contentSplitPanel;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
