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
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * The Class MultilinePopUpDialog.
 *
 * @author Guillermo Reynoso
 */
@SuppressWarnings("serial")
public class MultilinePopUpDialog extends JDialog {
	
	/** The response. */
	private String response= "";
	
	/**
	 * Instantiates a new multiline pop up dialog.
	 *
	 * @param title the title
	 */
	public MultilinePopUpDialog(String title) {
		initComponents();
		label1.setText(title.trim());
        pack();
        setVisible(true);
	}
//	
//	public static void main(String argv[]){
//		System.out.println(MultilinePopUpDialog.showDialog("Hola"));
//	}

	/**
 * Show dialog.
 *
 * @return the string
 */
public String showDialog()
	{
		return response;
	}
	
	/**
	 * Ok button action performed.
	 *
	 * @param e the e
	 */
	private void okButtonActionPerformed(ActionEvent e) {
		if(textArea1.getText()!=null)
		response= textArea1.getText().trim();
		dispose();
	}

	/**
	 * Cancel button action performed.
	 *
	 * @param e the e
	 */
	private void cancelButtonActionPerformed(ActionEvent e) {
		dispose();
	}
	
	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label1 = new JLabel();
		scrollPane1 = new JScrollPane();
		textArea1 = new JTextArea();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new GridBagLayout());
				((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {15, 321, 10, 0};
				((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {10, 0, 20, 150, 0};
				((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
				((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 1.0E-4};

				//---- label1 ----
				label1.setText("Enter text:");
				label1.setFont(new Font("Arial", Font.PLAIN, 14));
				contentPanel.add(label1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== scrollPane1 ========
				{

					//---- textArea1 ----
					textArea1.setLineWrap(true);
					scrollPane1.setViewportView(textArea1);
				}
				contentPanel.add(scrollPane1, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

				//---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed(e);
					}
				});
				buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The dialog pane. */
	private JPanel dialogPane;
	
	/** The content panel. */
	private JPanel contentPanel;
	
	/** The label1. */
	private JLabel label1;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The text area1. */
	private JTextArea textArea1;
	
	/** The button bar. */
	private JPanel buttonBar;
	
	/** The ok button. */
	private JButton okButton;
	
	/** The cancel button. */
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
