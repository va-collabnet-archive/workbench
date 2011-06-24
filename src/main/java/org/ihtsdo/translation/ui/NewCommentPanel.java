/**
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

package org.ihtsdo.translation.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.project.refset.CommentsRefset;

/**
 * The Class IssuesPanel.
 */
public class NewCommentPanel extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	public NewCommentPanel() {
		initComponents();
		updateCombo1();
		txtComm.requestFocusInWindow();
	}
	
	private void updateCombo1() {
		comboBox1.removeAllItems();
		List<I_GetConceptData> types = CommentsRefset.getCommentTypes();
		Collections.sort(types,
				new Comparator<I_GetConceptData>()
				{
					public int compare(I_GetConceptData f1, I_GetConceptData f2)
					{
						return f1.toString().compareTo(f2.toString());
					}
				});
		for (I_GetConceptData loopType : types) {
			comboBox1.addItem(loopType);
		}
	}
	
	private void updateCombo2() {
		comboBox2.removeAllItems();
		if (comboBox1.getSelectedItem() != null) {
			I_GetConceptData type = (I_GetConceptData) comboBox1.getSelectedItem();
			List<I_GetConceptData> subTypes = CommentsRefset.getCommentSubTypes(type);
			Collections.sort(subTypes,
					new Comparator<I_GetConceptData>()
					{
						public int compare(I_GetConceptData f1, I_GetConceptData f2)
						{
							return f1.toString().compareTo(f2.toString());
						}
					});
			for (I_GetConceptData loopSubType : subTypes) {
				comboBox2.addItem(loopSubType);
			}
//			try {
//				comboBox2.addItem(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_COMMENT_OTHER.getUids()));
//			} catch (TerminologyException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
	}
	
	public String getNewComment(){
		return txtComm.getText();
	}
	
	public I_GetConceptData getCommentType() {
		if (comboBox1.getSelectedItem() != null) {
			return (I_GetConceptData) comboBox1.getSelectedItem();
		} else {
			return null;
		}
	}
	
	public I_GetConceptData getCommentSubType() {
		if (comboBox2.getSelectedItem() != null) {
			return (I_GetConceptData) comboBox2.getSelectedItem();
		} else {
			return null;
		}
	}

	private void comboBox1ItemStateChanged(ItemEvent e) {
		updateCombo2();
	}
	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		panel1 = new JPanel();
		label2 = new JLabel();
		comboBox1 = new JComboBox();
		label3 = new JLabel();
		comboBox2 = new JComboBox();
		scrollPane2 = new JScrollPane();
		txtComm = new JTextArea();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {26, 0, 405, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 26, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};

		//---- label1 ----
		label1.setText("New Comment");
		label1.setFont(label1.getFont().deriveFont(label1.getFont().getStyle() | Font.BOLD, label1.getFont().getSize() + 2f));
		add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label2 ----
			label2.setText("Type");
			panel1.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- comboBox1 ----
			comboBox1.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					comboBox1ItemStateChanged(e);
				}
			});
			panel1.add(comboBox1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label3 ----
			label3.setText("Subtype");
			panel1.add(label3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel1.add(comboBox2, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== scrollPane2 ========
		{

			//---- txtComm ----
			txtComm.setRows(10);
			scrollPane2.setViewportView(txtComm);
		}
		add(scrollPane2, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 5), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label1;
	private JPanel panel1;
	private JLabel label2;
	private JComboBox comboBox1;
	private JLabel label3;
	private JComboBox comboBox2;
	private JScrollPane scrollPane2;
	private JTextArea txtComm;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
