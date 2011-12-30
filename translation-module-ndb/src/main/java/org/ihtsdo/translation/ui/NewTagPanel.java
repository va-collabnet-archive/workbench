/*
 * Created by JFormDesigner on Wed Dec 28 19:06:17 GMT-03:00 2011
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

import org.ihtsdo.project.workflow.tag.InboxTag;
import org.ihtsdo.project.workflow.tag.TagManager;

/**
 * @author Guillermo Reynoso
 */
@SuppressWarnings("serial")
public class NewTagPanel extends JDialog {
	public NewTagPanel() {
		initComponents();
	}

	private InboxTag tag = null;
	private Color color;
	
	private void initCustomComponents() {
	}

	public InboxTag showModalDialog() {
		setModal(true);
		this.setPreferredSize(new Dimension(400, 170));
		pack();
		setVisible(true);
		return tag;
	}

	private void close(InboxTag canceled) {
		this.tag = canceled;
		dispose();
	}

	private void createButtonActionPerformed(ActionEvent e) {
		String tagName = tagNameField.getText();
		if(tagName.equals("") || color == null){
			colorLabel.setForeground(Color.RED);
			colorLabel.setText("Invalid values");
			return;
		}
		InboxTag tag = new InboxTag(tagName, TagManager.getInstance().getHtmlColor(color), null);
		close(tag);
	}

	private void cancelActionPerformed(ActionEvent e) {
		close(null);
	}

	private void colorChooserActionPerformed(ActionEvent e) {
		color = JColorChooser.showDialog(this, "TAG Color", Color.GREEN);
		String tagName = tagNameField.getText();
		String colorText = TagManager.getInstance().getHtmlColor(color);
		colorLabel.setText(TagManager.getInstance().getHeader(tagName, colorText));
	}

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
		colorLabel = new JLabel();
		separator1 = new JSeparator();

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
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 22, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 26, 5, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Tag Name:");
			panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel2.add(tagNameField, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- label2 ----
			label2.setText("Tag Color:");
			panel2.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- colorChooser ----
			colorChooser.setText("<html><body><table><tr><td style=\"background-color: RED; width:12px;height:12px;\" >");
			colorChooser.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					colorChooserActionPerformed(e);
				}
			});
			panel2.add(colorChooser, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 5, 5), 0, 0));
			panel2.add(colorLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 5, 0), 0, 0));
			panel2.add(separator1, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		contentPane.add(panel2, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel1;
	private JButton cancel;
	private JButton createButton;
	private JPanel panel2;
	private JLabel label1;
	private JTextField tagNameField;
	private JLabel label2;
	private JButton colorChooser;
	private JLabel colorLabel;
	private JSeparator separator1;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
