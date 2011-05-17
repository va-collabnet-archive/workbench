/*
 * Created by JFormDesigner on Tue Aug 31 19:05:45 GMT-03:00 2010
 */

package org.ihtsdo.project.panel;

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.util.DatePicker;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;

/**
 * @author Guillermo Reynoso
 */
public class ProjectDatePeriodDialog extends JDialog {

	private static final long serialVersionUID = 1225212384010395373L;

	private static final String ALL_FIELDS_ARE_REQUIRED = "All fields are required.";

	public static final String PROJECT_KEY = "Project";
	public static final String START_DATE_KEY = "Start Date";
	public static final String END_DATE_KEY = "End Date";
	public static final String PERIOD_KEY = "Period";

	private DefaultComboBoxModel projectComboModel;
	private I_ConfigAceFrame config;
	private HashMap<String, Object> data = null;

	DatePicker startDate;
	DatePicker endDate;

	public ProjectDatePeriodDialog(I_ConfigAceFrame config) {
		this.config = config;
		initComponents();
		initCustomComponents();
	}

	private void initCustomComponents() {
		errorLabel.setForeground(Color.RED);

		projectComboModel = new DefaultComboBoxModel();
		projectComboBox.setModel(projectComboModel);

		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
		projectComboModel.addElement("");
		for (I_TerminologyProject iTerminologyProject : projects) {
			projectComboModel.addElement(iTerminologyProject);
		}

		// date picker
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		GregorianCalendar minDate = new GregorianCalendar();
		minDate.setTimeInMillis(1);

		startDate = new DatePicker(minDate, Calendar.getInstance(), dateFormat);
		// ======== date1 ========
		{
			startDate.setLayout(new GridBagLayout());
			((GridBagLayout) startDate.getLayout()).columnWidths = new int[] { 0, 0, 0 };
			((GridBagLayout) startDate.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) startDate.getLayout()).columnWeights = new double[] { 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) startDate.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };
		}
		projectPanel.add(startDate, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

		endDate = new DatePicker(minDate, Calendar.getInstance(), dateFormat);
		// ======== date2 ========
		{
			endDate.setLayout(new GridBagLayout());
			((GridBagLayout) endDate.getLayout()).columnWidths = new int[] { 0, 0 };
			((GridBagLayout) endDate.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) endDate.getLayout()).columnWeights = new double[] { 0.0, 1.0E-4 };
			((GridBagLayout) endDate.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };
		}

		projectPanel.add(endDate, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close(null);
			}
		});

	}

	public HashMap<String, Object> showModalDialog() {
		setModal(true);
		this.setPreferredSize(new Dimension(440, 210));
		pack();
		setVisible(true);
		return data;
	}

	private void close(HashMap<String, Object> canceled) {
		this.data = canceled;
		dispose();
	}

	private void openButtonActionPerformed(ActionEvent e) {
		Calendar startDate;
		Calendar endDate;
		if ((startDate = getStartDate()) != null && (endDate = getEndDate()) != null && !period.getText().equals("")) {
			Object project = projectComboBox.getSelectedItem();
			Long p = null;
			if (!(project instanceof I_TerminologyProject)) {
				errorLabel.setText(ALL_FIELDS_ARE_REQUIRED);
				return;
			}
			try {
				if(startDate.compareTo(endDate) != 0){
					p = Long.valueOf(period.getText().trim());
				}
			} catch (NumberFormatException x) {
				errorLabel.setText("Invalid period");
				return;
			}
			if(startDate.compareTo(endDate) > 0){
				errorLabel.setText("Start date must earlier than end date");
				return;
			}
			data = new HashMap<String, Object>();
			data.put(PROJECT_KEY, projectComboBox.getSelectedItem());
			data.put(PERIOD_KEY, p);
			data.put(START_DATE_KEY, startDate);
			data.put(END_DATE_KEY, endDate);
			close(data);
		} else {
			errorLabel.setText(ALL_FIELDS_ARE_REQUIRED);
		}
	}

	private void cancelButtonActionPerformed(ActionEvent e) {
		close(null);
	}

	public Calendar getStartDate() {
		return (Calendar) startDate.getSelectedDate();
	}

	public Calendar getEndDate() {
		return (Calendar) endDate.getSelectedDate();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		buttonPanel = new JPanel();
		openButton = new JButton();
		cancelButton = new JButton();
		panel3 = new JPanel();
		projectPanel = new JPanel();
		label1 = new JLabel();
		projectComboBox = new JComboBox();
		startDateLabel = new JLabel();
		label3 = new JLabel();
		periodLabel = new JLabel();
		panel4 = new JPanel();
		period = new JTextField();
		errorLabel = new JLabel();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(5, 5));

		//======== buttonPanel ========
		{
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

			//---- openButton ----
			openButton.setText("Open");
			openButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openButtonActionPerformed(e);
				}
			});
			buttonPanel.add(openButton);

			//---- cancelButton ----
			cancelButton.setText("cancel");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cancelButtonActionPerformed(e);
				}
			});
			buttonPanel.add(cancelButton);
		}
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

			//======== projectPanel ========
			{
				projectPanel.setBorder(new EmptyBorder(3, 5, 0, 5));
				projectPanel.setLayout(new GridBagLayout());
				((GridBagLayout)projectPanel.getLayout()).columnWidths = new int[] {0, 177, 0};
				((GridBagLayout)projectPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
				((GridBagLayout)projectPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
				((GridBagLayout)projectPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

				//---- label1 ----
				label1.setText("Project");
				projectPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 7, 7), 0, 0));
				projectPanel.add(projectComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 7, 0), 0, 0));

				//---- startDateLabel ----
				startDateLabel.setText("Start-Date");
				projectPanel.add(startDateLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 7, 7), 0, 0));

				//---- label3 ----
				label3.setText("End-Date");
				projectPanel.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 7, 7), 0, 0));

				//---- periodLabel ----
				periodLabel.setText("Interval (days)");
				projectPanel.add(periodLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 7), 0, 0));

				//======== panel4 ========
				{
					panel4.setLayout(new GridBagLayout());
					((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {70, 0};
					((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					panel4.add(period, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				projectPanel.add(panel4, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel3.add(projectPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));
			panel3.add(errorLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		contentPane.add(panel3, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel buttonPanel;
	private JButton openButton;
	private JButton cancelButton;
	private JPanel panel3;
	private JPanel projectPanel;
	private JLabel label1;
	private JComboBox projectComboBox;
	private JLabel startDateLabel;
	private JLabel label3;
	private JLabel periodLabel;
	private JPanel panel4;
	private JTextField period;
	private JLabel errorLabel;
	// JFormDesigner - End of variables declaration //GEN-END:variables

}
