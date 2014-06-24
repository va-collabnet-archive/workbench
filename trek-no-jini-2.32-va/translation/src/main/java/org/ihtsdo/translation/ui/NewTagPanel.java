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

package org.ihtsdo.translation.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.ihtsdo.project.view.tag.InboxTag;
import org.ihtsdo.project.view.tag.TagManager;

/**
 * The Class NewTagPanel.
 *
 * @author Guillermo Reynoso
 */
@SuppressWarnings("serial")
public class NewTagPanel extends JDialog {
	
	/** The text. */
	private String text = "<html><body><table><tr><td style=\"background-color: ${bg_color}; width:12px;height:12px;\" >";
	
	/**
	 * Instantiates a new new tag panel.
	 */
	public NewTagPanel() {
		initComponents();
		tagNameField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				colorLabel.setText(TagManager.getInstance().getHeader(tagNameField.getText(), TagManager.getInstance().getHtmlColor(color), TagManager.getInstance().getHtmlColor(textColor)));
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				colorLabel.setText(TagManager.getInstance().getHeader(tagNameField.getText(), TagManager.getInstance().getHtmlColor(color), TagManager.getInstance().getHtmlColor(textColor)));
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {}
		});
	}

	/** The tag. */
	private InboxTag tag = null;
	
	/** The color. */
	private Color color;
	
	/** The text color. */
	private Color textColor = Color.BLACK;

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		new NewTagPanel().showModalDialog();
	}

	/**
	 * Show modal dialog.
	 *
	 * @return the inbox tag
	 */
	public InboxTag showModalDialog() {
		setModal(true);
		this.setPreferredSize(new Dimension(400, 170));
		pack();
		setVisible(true);
		return tag;
	}

	/**
	 * Close.
	 *
	 * @param canceled the canceled
	 */
	private void close(InboxTag canceled) {
		this.tag = canceled;
		dispose();
	}

	/**
	 * Creates the button action performed.
	 *
	 * @param e the e
	 */
	private void createButtonActionPerformed(ActionEvent e) {
		String tagName = tagNameField.getText();
		if (tagName.equals("") || color == null) {
			colorLabel.setForeground(Color.RED);
			colorLabel.setText("Invalid values");
			return;
		}
		InboxTag tag = new InboxTag(tagName, TagManager.getInstance().getHtmlColor(color),TagManager.getInstance().getHtmlColor(textColor), null);
		close(tag);
	}

	/**
	 * Cancel action performed.
	 *
	 * @param e the e
	 */
	private void cancelActionPerformed(ActionEvent e) {
		close(null);
	}

	/**
	 * Color chooser action performed.
	 *
	 * @param e the e
	 */
	private void colorChooserActionPerformed(ActionEvent e) {
		color = JColorChooser.showDialog(this, "TAG Color", Color.GREEN);
		String tagName = tagNameField.getText();
		String colorText = TagManager.getInstance().getHtmlColor(color);
		colorChooser.setText(text.replace("${bg_color}", colorText));
		colorLabel.setText(TagManager.getInstance().getHeader(tagName, colorText, TagManager.getInstance().getHtmlColor(textColor)));
	}

	/**
	 * Text color chooser action performed.
	 *
	 * @param e the e
	 */
	private void textColorChooserActionPerformed(ActionEvent e) {
		textColor  = JColorChooser.showDialog(this, "Text Color", Color.BLACK);
		String tagName = tagNameField.getText();
		String colorText = TagManager.getInstance().getHtmlColor(textColor);
		textColorChooser.setText(text.replace("${bg_color}", colorText));
		colorLabel.setText(TagManager.getInstance().getHeader(tagName, TagManager.getInstance().getHtmlColor(color), colorText));
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		cancel = new JButton();
		createButton = new JButton();
		panel2 = new JPanel();
		label1 = new JLabel();
		tagNameField = new JTextField();
		label2 = new JLabel();
		colorChooser = new JButton();
		label3 = new JLabel();
		textColorChooser = new JButton();
		separator1 = new JSeparator();
		colorLabel = new JLabel();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== panel1 ========
		{
			panel1.setBorder(new EmptyBorder(5, 5, 5, 5));
			panel1.setLayout(new FlowLayout(FlowLayout.RIGHT));

			//---- cancel ----
			cancel.setText("Cancel");
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cancelActionPerformed(e);
				}
			});
			panel1.add(cancel);

			//---- createButton ----
			createButton.setText("Create");
			createButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					createButtonActionPerformed(e);
				}
			});
			panel1.add(createButton);
		}
		contentPane.add(panel1, BorderLayout.SOUTH);

		//======== panel2 ========
		{
			panel2.setBorder(new EmptyBorder(5, 5, 5, 5));
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 22, 79, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 26, 10, 0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Tag Name:");
			panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel2.add(tagNameField, new GridBagConstraints(1, 0, 3, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- label2 ----
			label2.setText("Tag Color:");
			panel2.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- colorChooser ----
			colorChooser.setText("<html><body><table><tr><td style=\"background-color: ${color_bg_color}; width:12px;height:12px;\" >");
			colorChooser.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					colorChooserActionPerformed(e);
				}
			});
			panel2.add(colorChooser, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label3 ----
			label3.setText("Text Color");
			panel2.add(label3, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- textColorChooser ----
			textColorChooser.setText("<html><body><table><tr><td style=\"background-color: ${text_bg_color}; width:12px;height:12px;\" >");
			textColorChooser.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					textColorChooserActionPerformed(e);
				}
			});
			panel2.add(textColorChooser, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 5, 0), 0, 0));
			panel2.add(separator1, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));
			panel2.add(colorLabel, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		contentPane.add(panel2, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The panel1. */
	private JPanel panel1;
	
	/** The cancel. */
	private JButton cancel;
	
	/** The create button. */
	private JButton createButton;
	
	/** The panel2. */
	private JPanel panel2;
	
	/** The label1. */
	private JLabel label1;
	
	/** The tag name field. */
	private JTextField tagNameField;
	
	/** The label2. */
	private JLabel label2;
	
	/** The color chooser. */
	private JButton colorChooser;
	
	/** The label3. */
	private JLabel label3;
	
	/** The text color chooser. */
	private JButton textColorChooser;
	
	/** The separator1. */
	private JSeparator separator1;
	
	/** The color label. */
	private JLabel colorLabel;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
