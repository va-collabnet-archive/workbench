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

package org.ihtsdo.translation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.project.refset.CommentsRefset;
import org.ihtsdo.project.util.IconUtilities;

/**
 * The Class CommentPopUpDialog.
 *
 * @author Guillermo Reynoso
 */
@SuppressWarnings("serial")
public class CommentPopUpDialog extends JDialog {

	/** The response. */
	private HashMap<I_GetConceptData, String> response = null;
	
	/** The comment type. */
	private I_GetConceptData commentType;

	/**
	 * Instantiates a new comment pop up dialog.
	 *
	 * @param title the title
	 * @param commentType the comment type
	 */
	public CommentPopUpDialog(String title, I_GetConceptData commentType) {
		this.commentType = commentType;
		initComponents();
		initCustomComponents();
		label1.setText(title.trim());
		pack();
		errorLabel.setVisible(false);
		errorLabel.setIcon(IconUtilities.deleteIcon);
		errorLabel.setForeground(Color.RED);
		errorLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				errorLabel.setVisible(false);
			}
		});
		setVisible(true);
	}

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {
		List<I_GetConceptData> s = new ArrayList<I_GetConceptData>();
		CommentsRefset.getCommentSubTypes(s, commentType);
		for (I_GetConceptData iGetConceptData : s) {
			rejectionReasonCombo.addItem(iGetConceptData);
		}

	}

	/**
	 * Show dialog.
	 *
	 * @return the hash map
	 */
	public HashMap<I_GetConceptData, String> showDialog() {
		return response;
	}

	/**
	 * Ok button action performed.
	 *
	 * @param e the e
	 */
	private void okButtonActionPerformed(ActionEvent e) {
		if(textArea1.getText().trim().equals("")){
			errorLabel.setVisible(true);
			errorLabel.setText("Comment is required for rejection.");
			return;
		}
		if (textArea1.getText() != null && rejectionReasonCombo.getSelectedItem() instanceof I_GetConceptData) {
			response = new HashMap<I_GetConceptData, String>();
			response.put((I_GetConceptData) rejectionReasonCombo.getSelectedItem(), textArea1.getText().trim());
		}
		dispose();
	}

	private void thisWindowClosing(WindowEvent e) {
		response = null;
		dispose();
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		panel1 = new JPanel();
		label2 = new JLabel();
		rejectionReasonCombo = new JComboBox();
		label1 = new JLabel();
		scrollPane1 = new JScrollPane();
		textArea1 = new JTextArea();
		buttonBar = new JPanel();
		errorLabel = new JLabel();
		okButton = new JButton();

		//======== this ========
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setModal(true);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing(e);
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new GridBagLayout());
				((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {316, 0};
				((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 0, 150, 0};
				((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};

				//======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
					((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label2 ----
					label2.setText("Sub Type:");
					panel1.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					panel1.add(rejectionReasonCombo, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				contentPanel.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 10, 0), 0, 0));

				//---- label1 ----
				label1.setText("Enter text:");
				label1.setFont(new Font("Arial", Font.PLAIN, 14));
				contentPanel.add(label1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 10, 0), 0, 0));

				//======== scrollPane1 ========
				{

					//---- textArea1 ----
					textArea1.setLineWrap(true);
					scrollPane1.setViewportView(textArea1);
				}
				contentPanel.add(scrollPane1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};

				//---- errorLabel ----
				errorLabel.setVisible(false);
				buttonBar.add(errorLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JPanel panel1;
	private JLabel label2;
	private JComboBox rejectionReasonCombo;
	private JLabel label1;
	private JScrollPane scrollPane1;
	private JTextArea textArea1;
	private JPanel buttonBar;
	private JLabel errorLabel;
	private JButton okButton;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
