/*
 * Created by JFormDesigner on Wed Dec 29 13:29:38 GMT-03:00 2010
 */

package org.ihtsdo.qa.store.gui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.qa.store.QAStoreBI;
import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.QACase;
import org.ihtsdo.qa.store.model.QADatabase;
import org.ihtsdo.qa.store.model.QaCaseComment;
import org.ihtsdo.qa.store.model.Rule;
import org.ihtsdo.qa.store.model.TerminologyComponent;
import org.ihtsdo.rules.RulesLibrary;

/**
 * @author Guillermo Reynoso
 */
public class QACaseDetailsPanel extends JPanel {
	private static final long serialVersionUID = 4598159209452180160L;

	private Rule rule;
	private TerminologyComponent component;
	private QACase selectedCase;
	private Set<I_GetConceptData> users;
	private LinkedHashSet<DispositionStatus> dispositionStatuses;
	private DefaultListModel commentsModel;
	private TerminologyComponent headerComponent;
	private QADatabase qaDatabase;
	private final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MMM-dd HH:mm:ss");
	private QAStoreBI store;
	private String currentUser;
	private List<QaCaseComment> comments;
	private CommentDialog commentDialog;

	public QACaseDetailsPanel(Rule rule, TerminologyComponent component,
			QACase selectedCase,
			LinkedHashSet<DispositionStatus> dispositionStatuses,
			TerminologyComponent headerComponent, QADatabase qaDatabase,
			QAStoreBI store) {
		initComponents();
		this.rule = rule;
		this.component = component;
		this.selectedCase = selectedCase;
		this.headerComponent = headerComponent;
		this.qaDatabase = qaDatabase;
		this.store = store;
		this.dispositionStatuses = dispositionStatuses;

		initCustomComponents();
	}

	private void initCustomComponents() {
		try {
			currentUser = Terms.get().getActiveAceFrameConfig().getUsername();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		users = RulesLibrary.getUsers();

		assignedTo.removeAllItems();
		for (Object user : users) {
			assignedTo.addItem(((I_GetConceptData) user).toString());
		}
		assignedTo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent item) {

			}
		});
		assignedTo.revalidate();
		assignedTo.repaint();

		dispositionStatusCombo.removeAllItems();
		dispositionStatusCombo.addItem("");
		for (DispositionStatus object : this.dispositionStatuses) {
			if (rule.isWhitelistAllowed() && object.getName().equals("Cleared")) {
				dispositionStatusCombo.addItem(object);
			} else if (!object.getName().equals("Cleared")) {
				dispositionStatusCombo.addItem(object);
			}
		}
		dispositionStatusCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent item) {
			}
		});
		dispositionStatusCombo.revalidate();
		dispositionStatusCombo.repaint();

		commentsModel = new DefaultListModel();
		commentsList.setModel(commentsModel);
		comments = selectedCase.getComments();
		commentDialog = new CommentDialog(new JDialog(), store, currentUser,
				selectedCase.getCaseUuid());
		initFields();
		saveButton.setEnabled(false);
	}

	private void initFields() {
		ruleNameTextArea.setText(rule.getName());
		StringBuilder ruleDescription = new StringBuilder("Description: "
				+ rule.getDescription() + "\n");
		ruleDescription.append("Expected pattern: " + rule.getExpectedResult()
				+ "\n");
		ruleDescription.append("Suggested resolution: "
				+ rule.getSuggestedResolution() + "\n");
		ruleDescription.append("Example: " + rule.getExample());
		ruleDescriptionTextArea.setText(ruleDescription.toString());
		componentNameTextArea.setText(component.getComponentName());
		caseDetails.setText(selectedCase.getDetail());
		annotation.setText(selectedCase.getDispositionAnnotation() == null ? ""
				: selectedCase.getDispositionAnnotation());
		int dispoCount = dispositionStatusCombo.getItemCount();
		for (int i = 0; i < dispoCount; i++) {
			Object currentStatus = dispositionStatusCombo.getItemAt(i);
			if (currentStatus instanceof DispositionStatus) {
				DispositionStatus currentDispo = (DispositionStatus) currentStatus;
				if (currentDispo.getDispositionStatusUuid().equals(
						selectedCase.getDispositionStatusUuid())) {
					dispositionStatusCombo.setSelectedIndex(i);
				}
			}
		}

		dispositionStatusEditionDate.setText(selectedCase
				.getDispositionStatusDate() == null ? "" : sdf
				.format(selectedCase.getDispositionStatusDate().getTime()));
		dispositionStatusEditorLabel.setText(selectedCase
				.getDispositionStatusEditor() == null ? "" : selectedCase
				.getDispositionStatusEditor());

		assignedTo.setSelectedItem(selectedCase.getAssignedTo() == null ? ""
				: selectedCase.getAssignedTo());
		by.setText(selectedCase.getAssignmentEditor());
		assignedDate.setText(selectedCase.getAssignmentDate() == null ? ""
				: sdf.format(selectedCase.getAssignmentDate().getTime()));

		initComments();

		// RULE DETAILS
		ruleUuidTxt.setText(rule.getRuleUuid().toString());
		ruleCodeTxt.setText(rule.getRuleCode() == null ? "" : rule
				.getRuleCode());
		databaseTextA.setText(qaDatabase.getName());
		pathTextA.setText(headerComponent.getComponentName());
		whiteListAllowed.setSelected(rule.isWhitelistAllowed());
		resetWhiteListAllowed.setSelected(rule.isWhitelistResetAllowed());
	}

	private void initComments() {
		commentsModel.clear();
		if (comments != null && !comments.isEmpty()) {
			for (QaCaseComment qaCaseComment : comments) {
				commentsModel.addElement(qaCaseComment);
			}
		} else if (comments == null) {
			comments = new ArrayList<QaCaseComment>();
		}
	}

	private void saveButtonActionPerformed(ActionEvent e) {
		Object selectedAssignedTo = assignedTo.getSelectedItem();
		String assignedStr;
		if (selectedAssignedTo != null) {
			assignedStr = (String) selectedAssignedTo.toString();
		} else {
			assignedStr = "";
		}

		DispositionStatus selectedDispo = null;
		try {
			selectedDispo = (DispositionStatus) dispositionStatusCombo
					.getSelectedItem();
		} catch (ClassCastException ex) {
			caseDetailsError.setText("Disposition status cant be empty");
			return;
		}
		String anotString = annotation.getText();

		if ((selectedCase.getComments() == null || comments.size() < selectedCase
				.getComments().size())
				|| !selectedDispo.getDispositionStatusUuid().equals(
						selectedCase.getDispositionStatusUuid())
				|| !anotString
						.equals(selectedCase.getDispositionAnnotation() == null ? ""
								: selectedCase.getDispositionAnnotation())
				|| (!assignedStr.equals("") && !assignedStr.equals(selectedCase
						.getAssignmentEditor() == null ? "" : selectedCase
						.getAssignmentEditor()))) {

			Calendar date = Calendar.getInstance();
			if (!assignedStr.equals("")
					&& !assignedStr
							.equals(selectedCase.getAssignmentEditor() == null ? ""
									: selectedCase.getAssignmentEditor())) {
				by.setText(currentUser);
				assignedDate.setText(sdf.format(date.getTime()));
				selectedCase.setAssignedTo(assignedStr);
				selectedCase.setAssignmentEditor(by.getText());
				selectedCase.setAssignmentDate(date);
			}

			if (!selectedDispo.getDispositionStatusUuid().equals(
					selectedCase.getDispositionStatusUuid())) {
				selectedCase.setDispositionStatusUuid(selectedDispo
						.getDispositionStatusUuid());
				dispositionStatusEditionDate
						.setText(sdf.format(date.getTime()));
				dispositionStatusEditorLabel.setText(currentUser);
				selectedCase.setDispositionStatusEditor(currentUser);
				selectedCase.setDispositionStatusDate(date);
			}

			if (!anotString
					.equals(selectedCase.getDispositionAnnotation() == null ? ""
							: selectedCase.getDispositionAnnotation())) {
				selectedCase.setDispositionAnnotation(anotString);
				dispositionStatusEditionDate
						.setText(sdf.format(date.getTime()));
				dispositionStatusEditorLabel.setText(currentUser);
				selectedCase.setDispositionStatusEditor(currentUser);
				selectedCase.setDispositionStatusDate(date);
			}
			if (selectedCase.getComments() == null
					|| comments.size() < selectedCase.getComments().size()) {
				selectedCase.setComments(comments);
			}
			store.persistQACase(selectedCase);
			caseDetailsError.setText("");
		}

	}

	private void addCommentButtonActionPerformed(ActionEvent e) {
		QaCaseComment comment = commentDialog.showModalDialog();
		comments.add(comment);
		initComments();
	}

	private void assignedToItemStateChanged(ItemEvent e) {
		saveButton.setEnabled(true);
		if (dispositionStatusCombo.getSelectedItem() instanceof DispositionStatus) {
			caseDetailsError.setText("");
		}
	}

	private void dispositionStatusComboItemStateChanged(ItemEvent e) {
		saveButton.setEnabled(true);
	}

	private void annotationKeyPressed(KeyEvent e) {
		saveButton.setEnabled(true);
	}

	private void button1ActionPerformed(ActionEvent e) {
		initCustomComponents();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		tabbedPane1 = new JTabbedPane();
		scrollPane2 = new JScrollPane();
		panel3 = new JPanel();
		label1 = new JLabel();
		ruleNameTextArea = new JTextArea();
		label2 = new JLabel();
		scrollPane1 = new JScrollPane();
		ruleDescriptionTextArea = new JTextArea();
		separator1 = new JSeparator();
		label3 = new JLabel();
		componentNameTextArea = new JTextArea();
		label4 = new JLabel();
		scrollPane3 = new JScrollPane();
		caseDetails = new JTextArea();
		separator2 = new JSeparator();
		label5 = new JLabel();
		dispositionStatusCombo = new JComboBox();
		label6 = new JLabel();
		annotation = new JTextArea();
		label14 = new JLabel();
		dispositionStatusEditorLabel = new JLabel();
		label15 = new JLabel();
		dispositionStatusEditionDate = new JLabel();
		separator3 = new JSeparator();
		label7 = new JLabel();
		assignedTo = new JComboBox();
		label8 = new JLabel();
		by = new JLabel();
		label9 = new JLabel();
		assignedDate = new JLabel();
		caseDetailsError = new JLabel();
		scrollPane5 = new JScrollPane();
		panel4 = new JPanel();
		scrollPane4 = new JScrollPane();
		commentsList = new JList();
		addCommentButton = new JButton();
		scrollPane6 = new JScrollPane();
		panel5 = new JPanel();
		label10 = new JLabel();
		ruleUuidTxt = new JTextArea();
		label11 = new JLabel();
		ruleCodeTxt = new JTextArea();
		label12 = new JLabel();
		databaseTextA = new JTextArea();
		label13 = new JLabel();
		pathTextA = new JTextArea();
		separator4 = new JSeparator();
		whiteListAllowed = new JCheckBox();
		resetWhiteListAllowed = new JCheckBox();
		panel2 = new JPanel();
		button1 = new JButton();
		saveButton = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {740, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {391, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setBorder(new EmptyBorder(5, 5, 5, 5));
			panel1.setLayout(new BorderLayout());

			//======== tabbedPane1 ========
			{

				//======== scrollPane2 ========
				{

					//======== panel3 ========
					{
						panel3.setBorder(new EmptyBorder(5, 5, 0, 0));
						panel3.setLayout(new GridBagLayout());
						((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 127, 0, 0, 0, 114, 60, 0, 0};
						((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {25, 66, 12, 25, 30, 0, 28, 26, 0, 0, 0, 0};
						((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
						((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

						//---- label1 ----
						label1.setText("Rule name");
						panel3.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- ruleNameTextArea ----
						ruleNameTextArea.setEditable(false);
						panel3.add(ruleNameTextArea, new GridBagConstraints(1, 0, 7, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- label2 ----
						label2.setText("Rule Description");
						label2.setVerticalAlignment(SwingConstants.TOP);
						panel3.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//======== scrollPane1 ========
						{

							//---- ruleDescriptionTextArea ----
							ruleDescriptionTextArea.setRows(4);
							ruleDescriptionTextArea.setEditable(false);
							scrollPane1.setViewportView(ruleDescriptionTextArea);
						}
						panel3.add(scrollPane1, new GridBagConstraints(1, 1, 7, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));
						panel3.add(separator1, new GridBagConstraints(0, 2, 8, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- label3 ----
						label3.setText("Component Name");
						panel3.add(label3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- componentNameTextArea ----
						componentNameTextArea.setEditable(false);
						panel3.add(componentNameTextArea, new GridBagConstraints(1, 3, 7, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- label4 ----
						label4.setText("Detail");
						label4.setVerticalAlignment(SwingConstants.TOP);
						panel3.add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//======== scrollPane3 ========
						{

							//---- caseDetails ----
							caseDetails.setRows(4);
							caseDetails.setEditable(false);
							scrollPane3.setViewportView(caseDetails);
						}
						panel3.add(scrollPane3, new GridBagConstraints(1, 4, 7, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));
						panel3.add(separator2, new GridBagConstraints(0, 5, 8, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- label5 ----
						label5.setText("Disposition status");
						panel3.add(label5, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- dispositionStatusCombo ----
						dispositionStatusCombo.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(ItemEvent e) {
								dispositionStatusComboItemStateChanged(e);
							}
						});
						panel3.add(dispositionStatusCombo, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- label6 ----
						label6.setText("Annotation");
						panel3.add(label6, new GridBagConstraints(2, 6, 2, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- annotation ----
						annotation.addKeyListener(new KeyAdapter() {
							@Override
							public void keyPressed(KeyEvent e) {
								annotationKeyPressed(e);
							}
						});
						panel3.add(annotation, new GridBagConstraints(4, 6, 4, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- label14 ----
						label14.setText("Edited by");
						panel3.add(label14, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));
						panel3.add(dispositionStatusEditorLabel, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- label15 ----
						label15.setText("Date");
						panel3.add(label15, new GridBagConstraints(2, 7, 2, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));
						panel3.add(dispositionStatusEditionDate, new GridBagConstraints(4, 7, 3, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));
						panel3.add(separator3, new GridBagConstraints(0, 8, 8, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- label7 ----
						label7.setText("Assigned to");
						panel3.add(label7, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- assignedTo ----
						assignedTo.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(ItemEvent e) {
								assignedToItemStateChanged(e);
							}
						});
						panel3.add(assignedTo, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- label8 ----
						label8.setText("By");
						panel3.add(label8, new GridBagConstraints(3, 9, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));
						panel3.add(by, new GridBagConstraints(4, 9, 2, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- label9 ----
						label9.setText("Date");
						panel3.add(label9, new GridBagConstraints(6, 9, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));
						panel3.add(assignedDate, new GridBagConstraints(7, 9, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- caseDetailsError ----
						caseDetailsError.setForeground(Color.red);
						panel3.add(caseDetailsError, new GridBagConstraints(0, 10, 6, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));
					}
					scrollPane2.setViewportView(panel3);
				}
				tabbedPane1.addTab("Case details", scrollPane2);


				//======== scrollPane5 ========
				{

					//======== panel4 ========
					{
						panel4.setBorder(new EmptyBorder(5, 5, 0, 0));
						panel4.setLayout(new GridBagLayout());
						((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {413, 0, 0};
						((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0, 0};
						((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
						((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

						//======== scrollPane4 ========
						{
							scrollPane4.setViewportView(commentsList);
						}
						panel4.add(scrollPane4, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- addCommentButton ----
						addCommentButton.setText("Add");
						addCommentButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								addCommentButtonActionPerformed(e);
							}
						});
						panel4.add(addCommentButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					scrollPane5.setViewportView(panel4);
				}
				tabbedPane1.addTab("Case comments", scrollPane5);


				//======== scrollPane6 ========
				{

					//======== panel5 ========
					{
						panel5.setBorder(new EmptyBorder(5, 5, 0, 0));
						panel5.setLayout(new GridBagLayout());
						((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 449, 0, 0};
						((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {25, 25, 25, 25, 0, 0, 0, 0};
						((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};
						((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

						//---- label10 ----
						label10.setText("Rule UUID");
						panel5.add(label10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- ruleUuidTxt ----
						ruleUuidTxt.setEditable(false);
						panel5.add(ruleUuidTxt, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- label11 ----
						label11.setText("Rule code");
						panel5.add(label11, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- ruleCodeTxt ----
						ruleCodeTxt.setEditable(false);
						panel5.add(ruleCodeTxt, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- label12 ----
						label12.setText("Database");
						panel5.add(label12, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- databaseTextA ----
						databaseTextA.setEditable(false);
						panel5.add(databaseTextA, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- label13 ----
						label13.setText("Path");
						panel5.add(label13, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- pathTextA ----
						pathTextA.setEditable(false);
						panel5.add(pathTextA, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));
						panel5.add(separator4, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- whiteListAllowed ----
						whiteListAllowed.setText("Whitelist allowed");
						whiteListAllowed.setEnabled(false);
						panel5.add(whiteListAllowed, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- resetWhiteListAllowed ----
						resetWhiteListAllowed.setText("Reset whitelist allowed");
						resetWhiteListAllowed.setEnabled(false);
						panel5.add(resetWhiteListAllowed, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));
					}
					scrollPane6.setViewportView(panel5);
				}
				tabbedPane1.addTab("Rule details", scrollPane6);

			}
			panel1.add(tabbedPane1, BorderLayout.CENTER);

			//======== panel2 ========
			{
				panel2.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

				//---- button1 ----
				button1.setText("Refresh");
				button1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button1ActionPerformed(e);
					}
				});
				panel2.add(button1);

				//---- saveButton ----
				saveButton.setText("Save");
				saveButton.setEnabled(false);
				saveButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						saveButtonActionPerformed(e);
					}
				});
				panel2.add(saveButton);
			}
			panel1.add(panel2, BorderLayout.SOUTH);
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel1;
	private JTabbedPane tabbedPane1;
	private JScrollPane scrollPane2;
	private JPanel panel3;
	private JLabel label1;
	private JTextArea ruleNameTextArea;
	private JLabel label2;
	private JScrollPane scrollPane1;
	private JTextArea ruleDescriptionTextArea;
	private JSeparator separator1;
	private JLabel label3;
	private JTextArea componentNameTextArea;
	private JLabel label4;
	private JScrollPane scrollPane3;
	private JTextArea caseDetails;
	private JSeparator separator2;
	private JLabel label5;
	private JComboBox dispositionStatusCombo;
	private JLabel label6;
	private JTextArea annotation;
	private JLabel label14;
	private JLabel dispositionStatusEditorLabel;
	private JLabel label15;
	private JLabel dispositionStatusEditionDate;
	private JSeparator separator3;
	private JLabel label7;
	private JComboBox assignedTo;
	private JLabel label8;
	private JLabel by;
	private JLabel label9;
	private JLabel assignedDate;
	private JLabel caseDetailsError;
	private JScrollPane scrollPane5;
	private JPanel panel4;
	private JScrollPane scrollPane4;
	private JList commentsList;
	private JButton addCommentButton;
	private JScrollPane scrollPane6;
	private JPanel panel5;
	private JLabel label10;
	private JTextArea ruleUuidTxt;
	private JLabel label11;
	private JTextArea ruleCodeTxt;
	private JLabel label12;
	private JTextArea databaseTextA;
	private JLabel label13;
	private JTextArea pathTextA;
	private JSeparator separator4;
	private JCheckBox whiteListAllowed;
	private JCheckBox resetWhiteListAllowed;
	private JPanel panel2;
	private JButton button1;
	private JButton saveButton;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
