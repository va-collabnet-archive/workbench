package org.ihtsdo.project.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
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
import java.awt.event.WindowListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.ihtsdo.project.model.I_TerminologyProject;

public class ProjectNameTypeDialog extends JDialog {

	private static final String NO_NAME = "Projects name is empty";

	private final JPanel contentPanel = new JPanel();

	private HashMap<String, I_TerminologyProject.Type> projectInfo = null;
	private JLabel lblErrorLabel;
	private JComboBox<I_TerminologyProject.Type> comboBox;
	private JTextField txtProjectName;

	/**
	 * Create the dialog.
	 */
	public ProjectNameTypeDialog() {
		initComponents();
		projectInfo = new HashMap<String, I_TerminologyProject.Type>();
		comboBox.removeAllItems();
		for (I_TerminologyProject.Type type : I_TerminologyProject.Type.values()) {
			comboBox.addItem(type);
		}
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close(null);
			}
		});
	}

	/**
	 * Show modal dialog.
	 * 
	 * @return the work list
	 */
	public HashMap<String, I_TerminologyProject.Type> showModalDialog() {
		setModal(true);
		setSize(350, 160);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
		return projectInfo;
	}

	/**
	 * Close.
	 * 
	 * @param canceled
	 *            the canceled
	 */
	private void close(HashMap<String, I_TerminologyProject.Type> canceled) {
		this.projectInfo = canceled;
		dispose();
	}

	/**
	 * Open button action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void okButtonActionPerformed(ActionEvent e) {
		if (!txtProjectName.getText().equals("")) {
			projectInfo.clear();
			projectInfo.put(txtProjectName.getText(), (I_TerminologyProject.Type) comboBox.getSelectedItem());
			close(projectInfo);
		} else {
			lblErrorLabel.setText(NO_NAME);
		}
	}

	/**
	 * Cancel button action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void cancelButtonActionPerformed(ActionEvent e) {
		close(null);
	}

	private void initComponents() {
		setTitle("Project name and type");
		setBounds(100, 100, 363, 160);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblProjectName = new JLabel("Name");
			GridBagConstraints gbc_lblProjectName = new GridBagConstraints();
			gbc_lblProjectName.insets = new Insets(0, 0, 5, 5);
			gbc_lblProjectName.anchor = GridBagConstraints.WEST;
			gbc_lblProjectName.gridx = 0;
			gbc_lblProjectName.gridy = 0;
			contentPanel.add(lblProjectName, gbc_lblProjectName);
		}
		{
			txtProjectName = new JTextField();
			GridBagConstraints gbc_txtProjectName = new GridBagConstraints();
			gbc_txtProjectName.insets = new Insets(0, 0, 5, 0);
			gbc_txtProjectName.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtProjectName.gridx = 1;
			gbc_txtProjectName.gridy = 0;
			contentPanel.add(txtProjectName, gbc_txtProjectName);
			txtProjectName.setColumns(10);
		}
		{
			JLabel lblType = new JLabel("Type");
			GridBagConstraints gbc_lblType = new GridBagConstraints();
			gbc_lblType.anchor = GridBagConstraints.WEST;
			gbc_lblType.insets = new Insets(0, 0, 5, 5);
			gbc_lblType.gridx = 0;
			gbc_lblType.gridy = 1;
			contentPanel.add(lblType, gbc_lblType);
		}
		{
			comboBox = new JComboBox<I_TerminologyProject.Type>();
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 1;
			contentPanel.add(comboBox, gbc_comboBox);
		}
		{
			lblErrorLabel = new JLabel("");
			lblErrorLabel.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					lblErrorLabel.setText("");
				}
			});
			lblErrorLabel.setHorizontalTextPosition(SwingConstants.LEFT);
			lblErrorLabel.setForeground(Color.RED);
			lblErrorLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			GridBagConstraints gbc_lblErrorLabel = new GridBagConstraints();
			gbc_lblErrorLabel.anchor = GridBagConstraints.WEST;
			gbc_lblErrorLabel.gridwidth = 2;
			gbc_lblErrorLabel.insets = new Insets(0, 0, 0, 5);
			gbc_lblErrorLabel.gridx = 0;
			gbc_lblErrorLabel.gridy = 2;
			contentPanel.add(lblErrorLabel, gbc_lblErrorLabel);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed(e);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
