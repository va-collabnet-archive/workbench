/*
 * Created by JFormDesigner on Thu Jan 13 18:51:39 GMT-03:00 2011
 */

package org.ihtsdo.qa.store.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.ihtsdo.qa.store.QAStoreBI;
import org.ihtsdo.qa.store.model.Category;
import org.ihtsdo.qa.store.model.Rule;
import org.ihtsdo.qa.store.model.Severity;

/**
 * @author Guillermo Reynoso
 */
public class RulesDetailsPanel extends JPanel {
	private static final long serialVersionUID = -804059589184964153L;
	private QAStoreBI store;
	private Rule rule;
	private List<Severity> severyties;
	private List<Category> categories;
	private boolean isRuleChanged;
	private JMenuItem menuItem;

	public RulesDetailsPanel(QAStoreBI store, Rule rule, List<Category> categories, List<Severity> severyties) {
		initComponents();
		this.store = store;
		this.rule = rule;
		isRuleChanged = false;
		saveButton.setEnabled(false);

		if (severyties == null) {
			this.severyties = store.getAllSeverities();
		} else {
			this.severyties = severyties;
		}
		severityComboBox.addItem("");
		for (Severity severity : this.severyties) {
			severityComboBox.addItem(severity);
		}

		if (categories != null) {
			this.categories = categories;
		} else {
			this.categories = store.getAllCategories();
		}
		categoryComboBox.addItem("");
		for (Category category : this.categories) {
			categoryComboBox.addItem(category);
		}

		createPopupMenu();

		initializeFields();
	}

	private void initializeFields() {
		// General Details
		ruleNameTextArea.setText(rule.getName());
		ruleDescriptionTextArea.setText(rule.getDescription());
		erpResTextArea.setText(rule.getExpectedResult());
		resolutionTextArea.setText(rule.getSuggestedResolution());
		exampleTextArea.setText(rule.getExample());
		docuUrlTextArea.setText(rule.getDocumentationUrl());
		ditaDocLinkUidTextArea.setText(rule.getDitaDocumentationLinkUuid());
		ditaGeneratedTopicTextArea.setText(rule.getDitaGeneratedTopicUuid());

		// Other Details
		ruleUuidTextArea.setText("" + rule.getRuleUuid());
		ruleCodeTextArea.setText(rule.getRuleCode());
		int comboItemsCount = categoryComboBox.getItemCount();
		for (int i = 0; i < comboItemsCount; i++) {
			if (categoryComboBox.getItemAt(i) instanceof Category) {
				Category itemAt = (Category) categoryComboBox.getItemAt(i);
				if (itemAt.getCategoryUuid().toString().equals(rule.getCategory())) {
					categoryComboBox.setSelectedIndex(i);
				}
			}
		}
		int severityComboItemCount = severityComboBox.getItemCount();
		for (int i = 0; i < severityComboItemCount; i++) {
			if (severityComboBox.getItemAt(i) instanceof Severity) {
				Severity itemAt = (Severity) severityComboBox.getItemAt(i);
				if (itemAt.getSeverityUuid().equals(rule.getSeverity().getSeverityUuid())) {
					severityComboBox.setSelectedIndex(i);
				}
			}
		}
		whiteListAllowed.setSelected(rule.isWhitelistAllowed());
		whiteListResetAllowed.setSelected(rule.isWhitelistResetAllowed());
		whiteLIstWhenClosedAllowed.setSelected(rule.isWhitelistResetWhenClosed());
		modifiedByLabel.setText(rule.getModifiedBy());
		modifiedDateLabel.setText("" + rule.getEffectiveTime());
		statusLabelText.setText(rule.getStatus() == 0 ? "Inactive" : "Active");

	}

	private void saveButtonActionPerformed(ActionEvent e) {
		rule.setExpectedResult(erpResTextArea.getText());
		rule.setSuggestedResolution(resolutionTextArea.getText());
		rule.setExample(exampleTextArea.getText());
		rule.setDitaDocumentationLinkUuid(ditaDocLinkUidTextArea.getText());
		rule.setDitaGeneratedTopicUuid(ditaGeneratedTopicTextArea.getText());
		rule.setDocumentationUrl(docuUrlTextArea.getText());
		rule.setRuleCode(ruleCodeTextArea.getText());
		Object category = categoryComboBox.getSelectedItem();
		if (category instanceof Category) {
			rule.setCategory(((Category) category).getCategoryUuid().toString());
		}
		Object severity = severityComboBox.getSelectedItem();
		if (severity instanceof Severity) {
			rule.setSeverity((Severity) severity);
		}
		rule.setWhitelistAllowed(whiteListAllowed.isSelected());
		rule.setWhitelistResetAllowed(whiteListResetAllowed.isSelected());
		rule.setWhitelistResetWhenClosed(whiteLIstWhenClosedAllowed.isSelected());
		store.persistRule(rule);

		saveButton.setEnabled(false);
	}

	private void enableSaveButton() {
		saveButton.setEnabled(true);
	}

	private void erpResTextAreaKeyPressed(KeyEvent e) {
		enableSaveButton();
	}

	private void resolutionTextAreaKeyPressed(KeyEvent e) {
		enableSaveButton();
	}

	private void exampleTextAreaKeyPressed(KeyEvent e) {
		enableSaveButton();
	}

	private void docuUrlTextAreaKeyPressed(KeyEvent e) {
		if (!docuUrlTextArea.getText().equals("")) {
			menuItem.setEnabled(true);
		} else {
			menuItem.setEnabled(false);
		}
		enableSaveButton();
	}

	private void ditaDocLinkUidTextAreaKeyPressed(KeyEvent e) {
		enableSaveButton();
	}

	private void ditaGeneratedTopicTextAreaKeyPressed(KeyEvent e) {
		enableSaveButton();
	}

	private void ruleCodeTextAreaKeyPressed(KeyEvent e) {
		enableSaveButton();
	}

	private void categoryComboBoxItemStateChanged(ItemEvent e) {
		enableSaveButton();
	}

	private void severityComboBoxItemStateChanged(ItemEvent e) {
		enableSaveButton();
	}

	private void whiteListAllowedItemStateChanged(ItemEvent e) {
		enableSaveButton();
	}

	private void whiteListResetAllowedItemStateChanged(ItemEvent e) {
		enableSaveButton();
	}

	private void whiteLIstWhenClosedAllowedItemStateChanged(ItemEvent e) {
		enableSaveButton();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		tabbedPane1 = new JTabbedPane();
		scrollBar1 = new JScrollPane();
		panel1 = new JPanel();
		label1 = new JLabel();
		ruleNameTextArea = new JTextArea();
		label2 = new JLabel();
		ruleDescriptionTextArea = new JTextArea();
		label3 = new JLabel();
		erpResTextArea = new JTextArea();
		label4 = new JLabel();
		resolutionTextArea = new JTextArea();
		label5 = new JLabel();
		exampleTextArea = new JTextArea();
		separator1 = new JSeparator();
		label6 = new JLabel();
		docuUrlTextArea = new JTextArea();
		label7 = new JLabel();
		ditaDocLinkUidTextArea = new JTextArea();
		label8 = new JLabel();
		ditaGeneratedTopicTextArea = new JTextArea();
		scrollPane1 = new JScrollPane();
		panel2 = new JPanel();
		label9 = new JLabel();
		ruleUuidTextArea = new JTextArea();
		label14 = new JLabel();
		ruleCodeTextArea = new JTextArea();
		statusLabel = new JLabel();
		statusLabelText = new JLabel();
		label11 = new JLabel();
		categoryComboBox = new JComboBox();
		label12 = new JLabel();
		severityComboBox = new JComboBox();
		separator2 = new JSeparator();
		whiteListAllowed = new JCheckBox();
		whiteListResetAllowed = new JCheckBox();
		whiteLIstWhenClosedAllowed = new JCheckBox();
		separator3 = new JSeparator();
		label13 = new JLabel();
		modifiedByLabel = new JLabel();
		label10 = new JLabel();
		modifiedDateLabel = new JLabel();
		panel3 = new JPanel();
		saveButton = new JButton();

		//======== this ========
		setBorder(null);
		setLayout(new BorderLayout());

		//======== tabbedPane1 ========
		{

			//======== scrollBar1 ========
			{

				//======== panel1 ========
				{
					panel1.setBorder(new EmptyBorder(5, 5, 5, 5));
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {114, 367, 102, 0};
					((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {25, 25, 39, 39, 39, 0, 25, 25, 20, 0};
					((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
					((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- label1 ----
					label1.setText("Rule name:");
					label1.setVerticalAlignment(SwingConstants.TOP);
					panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- ruleNameTextArea ----
					ruleNameTextArea.setEditable(false);
					panel1.add(ruleNameTextArea, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label2 ----
					label2.setText("Description");
					label2.setVerticalAlignment(SwingConstants.TOP);
					panel1.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- ruleDescriptionTextArea ----
					ruleDescriptionTextArea.setEditable(false);
					panel1.add(ruleDescriptionTextArea, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label3 ----
					label3.setText("Expected pattern");
					label3.setVerticalAlignment(SwingConstants.TOP);
					label3.setLabelFor(erpResTextArea);
					panel1.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- erpResTextArea ----
					erpResTextArea.setLineWrap(true);
					erpResTextArea.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							erpResTextAreaKeyPressed(e);
						}
					});
					panel1.add(erpResTextArea, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label4 ----
					label4.setText("Suggested resolution");
					label4.setVerticalAlignment(SwingConstants.TOP);
					panel1.add(label4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- resolutionTextArea ----
					resolutionTextArea.setLineWrap(true);
					resolutionTextArea.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							resolutionTextAreaKeyPressed(e);
						}
					});
					panel1.add(resolutionTextArea, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label5 ----
					label5.setText("Example");
					label5.setVerticalAlignment(SwingConstants.TOP);
					panel1.add(label5, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- exampleTextArea ----
					exampleTextArea.setLineWrap(true);
					exampleTextArea.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							exampleTextAreaKeyPressed(e);
						}
					});
					panel1.add(exampleTextArea, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));
					panel1.add(separator1, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label6 ----
					label6.setText("Documentation URL");
					label6.setVerticalAlignment(SwingConstants.TOP);
					panel1.add(label6, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- docuUrlTextArea ----
					docuUrlTextArea.setLineWrap(true);
					docuUrlTextArea.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							docuUrlTextAreaKeyPressed(e);
						}
					});
					panel1.add(docuUrlTextArea, new GridBagConstraints(1, 6, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label7 ----
					label7.setText("DITA topic link");
					label7.setVerticalAlignment(SwingConstants.TOP);
					panel1.add(label7, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- ditaDocLinkUidTextArea ----
					ditaDocLinkUidTextArea.setLineWrap(true);
					ditaDocLinkUidTextArea.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							ditaDocLinkUidTextAreaKeyPressed(e);
						}
					});
					panel1.add(ditaDocLinkUidTextArea, new GridBagConstraints(1, 7, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label8 ----
					label8.setText("Dita generated topic");
					label8.setVerticalAlignment(SwingConstants.TOP);
					panel1.add(label8, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- ditaGeneratedTopicTextArea ----
					ditaGeneratedTopicTextArea.setLineWrap(true);
					ditaGeneratedTopicTextArea.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							ditaGeneratedTopicTextAreaKeyPressed(e);
						}
					});
					panel1.add(ditaGeneratedTopicTextArea, new GridBagConstraints(1, 8, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				scrollBar1.setViewportView(panel1);
			}
			tabbedPane1.addTab("Rule Details", scrollBar1);


			//======== scrollPane1 ========
			{

				//======== panel2 ========
				{
					panel2.setBorder(new EmptyBorder(5, 5, 5, 5));
					panel2.setLayout(new GridBagLayout());
					((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {85, 0, 207, 85, 63, 232, 127, 0, 0};
					((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {26, 25, 0, 0, 0, 0, 0, 0, 26, 0, 0};
					((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
					((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- label9 ----
					label9.setText("UUID");
					panel2.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- ruleUuidTextArea ----
					ruleUuidTextArea.setEditable(false);
					panel2.add(ruleUuidTextArea, new GridBagConstraints(1, 0, 5, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label14 ----
					label14.setText("Rule Code");
					panel2.add(label14, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- ruleCodeTextArea ----
					ruleCodeTextArea.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							ruleCodeTextAreaKeyPressed(e);
						}
					});
					panel2.add(ruleCodeTextArea, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- statusLabel ----
					statusLabel.setText("Status");
					panel2.add(statusLabel, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel2.add(statusLabelText, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label11 ----
					label11.setText("Category");
					label11.setHorizontalAlignment(SwingConstants.LEFT);
					panel2.add(label11, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- categoryComboBox ----
					categoryComboBox.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							categoryComboBoxItemStateChanged(e);
						}
					});
					panel2.add(categoryComboBox, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label12 ----
					label12.setText("Severity");
					panel2.add(label12, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- severityComboBox ----
					severityComboBox.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							severityComboBoxItemStateChanged(e);
						}
					});
					panel2.add(severityComboBox, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel2.add(separator2, new GridBagConstraints(0, 3, 8, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- whiteListAllowed ----
					whiteListAllowed.setText("Whitelist allowed");
					whiteListAllowed.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							whiteListAllowedItemStateChanged(e);
						}
					});
					panel2.add(whiteListAllowed, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- whiteListResetAllowed ----
					whiteListResetAllowed.setText("Whitelist reset allowed");
					whiteListResetAllowed.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							whiteListResetAllowedItemStateChanged(e);
						}
					});
					panel2.add(whiteListResetAllowed, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- whiteLIstWhenClosedAllowed ----
					whiteLIstWhenClosedAllowed.setText("Whitelist reset when closed allowed");
					whiteLIstWhenClosedAllowed.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							whiteLIstWhenClosedAllowedItemStateChanged(e);
						}
					});
					panel2.add(whiteLIstWhenClosedAllowed, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel2.add(separator3, new GridBagConstraints(0, 7, 8, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label13 ----
					label13.setText("Modified by");
					panel2.add(label13, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel2.add(modifiedByLabel, new GridBagConstraints(2, 8, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label10 ----
					label10.setText("Last modification date");
					panel2.add(label10, new GridBagConstraints(0, 9, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					panel2.add(modifiedDateLabel, new GridBagConstraints(2, 9, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				scrollPane1.setViewportView(panel2);
			}
			tabbedPane1.addTab("Rule Configuration", scrollPane1);

		}
		add(tabbedPane1, BorderLayout.CENTER);

		//======== panel3 ========
		{
			panel3.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

			//---- saveButton ----
			saveButton.setText("Save");
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveButtonActionPerformed(e);
				}
			});
			panel3.add(saveButton);
		}
		add(panel3, BorderLayout.SOUTH);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JTabbedPane tabbedPane1;
	private JScrollPane scrollBar1;
	private JPanel panel1;
	private JLabel label1;
	private JTextArea ruleNameTextArea;
	private JLabel label2;
	private JTextArea ruleDescriptionTextArea;
	private JLabel label3;
	private JTextArea erpResTextArea;
	private JLabel label4;
	private JTextArea resolutionTextArea;
	private JLabel label5;
	private JTextArea exampleTextArea;
	private JSeparator separator1;
	private JLabel label6;
	private JTextArea docuUrlTextArea;
	private JLabel label7;
	private JTextArea ditaDocLinkUidTextArea;
	private JLabel label8;
	private JTextArea ditaGeneratedTopicTextArea;
	private JScrollPane scrollPane1;
	private JPanel panel2;
	private JLabel label9;
	private JTextArea ruleUuidTextArea;
	private JLabel label14;
	private JTextArea ruleCodeTextArea;
	private JLabel statusLabel;
	private JLabel statusLabelText;
	private JLabel label11;
	private JComboBox categoryComboBox;
	private JLabel label12;
	private JComboBox severityComboBox;
	private JSeparator separator2;
	private JCheckBox whiteListAllowed;
	private JCheckBox whiteListResetAllowed;
	private JCheckBox whiteLIstWhenClosedAllowed;
	private JSeparator separator3;
	private JLabel label13;
	private JLabel modifiedByLabel;
	private JLabel label10;
	private JLabel modifiedDateLabel;
	private JPanel panel3;
	private JButton saveButton;
	// JFormDesigner - End of variables declaration //GEN-END:variables

	class PopupListener extends MouseAdapter {
		JPopupMenu popup;

		PopupListener(JPopupMenu popupMenu) {
			popup = popupMenu;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void createPopupMenu() {
		// Create the popup menu.
		JPopupMenu popup = new JPopupMenu();
		menuItem = new JMenuItem("Open URL");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
					java.net.URI uri = new java.net.URI(docuUrlTextArea.getText());
					desktop.browse(uri);
				} catch (Exception e) {
					System.err.println(e.getMessage());
					menuItem.setEnabled(false);
				}

			}
		});
		if (docuUrlTextArea.getText().equals("")) {
			menuItem.setEnabled(false);
		}
		popup.add(menuItem);

		// Add listener to the text area so the popup menu can come up.
		MouseListener popupListener = new PopupListener(popup);
		docuUrlTextArea.addMouseListener(popupListener);
	}

}
