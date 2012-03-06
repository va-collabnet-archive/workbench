/*
 * Created by JFormDesigner on Wed Feb 15 20:26:36 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.*;
import javax.swing.*;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.translation.ui.*;

/**
 * @author Guillermo Reynoso
 */
public class LanguageTermPanel extends JPanel {
	public LanguageTermPanel() {
		initComponents();
	}

	public void pupulateTable(TranslationProject project, I_GetConceptData concept){
		
	}
	
	public void setTitle(String title){
		titleLabel.setText(title);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		titleLabel = new JLabel();
		scrollPane4 = new JScrollPane();
		tabSou = new ZebraJTable();

		// ======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 0.0, 1.0, 1.0E-4 };

		// ---- titleLabel ----
		titleLabel.setText("Title");
		titleLabel.setMaximumSize(new Dimension(25, 16));
		titleLabel.setMinimumSize(new Dimension(25, 16));
		titleLabel.setPreferredSize(new Dimension(25, 16));
		titleLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		add(titleLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== scrollPane4 ========
		{
			scrollPane4.setPreferredSize(new Dimension(23, 27));

			// ---- tabSou ----
			tabSou.setPreferredScrollableViewportSize(new Dimension(20, 20));
			tabSou.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			scrollPane4.setViewportView(tabSou);
		}
		add(scrollPane4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JLabel titleLabel;
	private JScrollPane scrollPane4;
	private ZebraJTable tabSou;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
