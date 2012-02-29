/*
 * Created by JFormDesigner on Wed Feb 15 20:26:36 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.*;
import javax.swing.*;

import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.translation.ui.*;

/**
 * @author Guillermo Reynoso
 */
public class SourceLanguageConceptPanel extends JFrame {
	public SourceLanguageConceptPanel() {
		initComponents();
	}
	
	public SourceLanguageConceptPanel(boolean isSourceLanguagePanel, TranslationProject project, WfInstance instance){
		
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label9 = new JLabel();
		scrollPane4 = new JScrollPane();
		tabSou = new ZebraJTable();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0};
		((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

		//---- label9 ----
		label9.setText("Source or Target");
		label9.setMaximumSize(new Dimension(25, 16));
		label9.setMinimumSize(new Dimension(25, 16));
		label9.setPreferredSize(new Dimension(25, 16));
		label9.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		contentPane.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane4 ========
		{
			scrollPane4.setPreferredSize(new Dimension(23, 27));

			//---- tabSou ----
			tabSou.setPreferredScrollableViewportSize(new Dimension(20, 20));
			tabSou.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			scrollPane4.setViewportView(tabSou);
		}
		contentPane.add(scrollPane4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label9;
	private JScrollPane scrollPane4;
	private ZebraJTable tabSou;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
