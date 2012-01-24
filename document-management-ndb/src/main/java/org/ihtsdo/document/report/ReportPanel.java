/*
 * Created by JFormDesigner on Fri Aug 27 15:58:59 GMT-03:00 2010
 */

package org.ihtsdo.document.report;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.report.I_Report.Reports;
import org.ihtsdo.project.panel.PanelHelperFactory;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.util.IconUtilities;

/**
 * @author Guillermo Reynoso
 */
public class ReportPanel extends JPanel {

	private static final long serialVersionUID = -5420825015191719954L;

	public static final String NO_SELECTION_MESSAGE = "No reports selected.";
	public static final String NO_DATA = "No data found to report";

	public static final String EXCEPTION = "Exception creating report, check log for more details.";
	public static final String EXEL_SIZE_EXCEDED_EXCEPTION = "Data size is outside excel allowable range (0..65535). Please choos smaller interval.";
	
	private ButtonGroup buttonGroup1;

	private DefaultListModel reportListModel;
	
	@SuppressWarnings("rawtypes")
	private SwingWorker reportWorker;

	public ReportPanel() {
		initComponents();
		initCustomComponents();
	}

	private void initCustomComponents() {
		// ---- buttonGroup1 ----
		buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(previewButton);
		buttonGroup1.add(excelButton);
		buttonGroup1.add(csvButton);
		previewButton.setSelected(true);
		
		label2.setIcon(IconUtilities.helpIcon);
		label2.setText("");

		progressBar1.setVisible(false);

		reportListModel = new DefaultListModel();
		reportList.setModel(reportListModel);

		reportList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (reportList.getSelectedValue() != null) {
					if (reportList.getSelectedValue() instanceof ProjectHistoricalReport) {
						previewButton.setEnabled(false);
					} else if(reportList.getSelectedValue() instanceof AccumulatedStatusChanges) {
						previewButton.setEnabled(false);
					}else{
						previewButton.setEnabled(true);
					}
					showMessage("");
				}
			}
		});

		for (Reports repEnum : I_Report.Reports.values()) {
			reportListModel.addElement(repEnum.getReport());
		}

	}

	void showError(String error) {
		errorLabel.setForeground(Color.RED);
		errorLabel.setText(error);
	}

	private void showMessage(String message) {
		errorLabel.setForeground(Color.BLACK);
		errorLabel.setText(message);
	}

	private void pdfReportActionPerformed(ActionEvent e) {
		I_Report report = (I_Report) reportList.getSelectedValue();
		if (report != null) {
			if (reportWorker != null && !reportWorker.isDone()) {
				reportWorker.cancel(true);
				reportWorker = null;
			}
			reportWorker = new PdfReportWorker(this, report);
			reportWorker.addPropertyChangeListener(new ReportProgressListener(progressBar1));
			reportWorker.execute();
		} else {
			showError(NO_SELECTION_MESSAGE);
		}
	}

	private void excelReportActionPerformed(ActionEvent e) {
		I_Report report = (I_Report) reportList.getSelectedValue();
		if (report != null) {
			if (reportWorker != null && !reportWorker.isDone()) {
				reportWorker.cancel(true);
				reportWorker = null;
			}
			reportWorker = new ExcelReportWorker(this, report);
			reportWorker.addPropertyChangeListener(new ReportProgressListener(progressBar1));
			reportWorker.execute();
		} else {
			showError(NO_SELECTION_MESSAGE);
		}
	}

	private void getReportActionPerformed(final ActionEvent e) {
		if (previewButton.isSelected()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					pdfReportActionPerformed(e);
				}
			});
		} else if (csvButton.isSelected()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					csvFileActionPerformed(e);
				}
			});
		} else if (excelButton.isSelected()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					excelReportActionPerformed(e);
				}
			});
		}
	}

	private void csvFileActionPerformed(ActionEvent e) {
		I_Report report = (I_Report) reportList.getSelectedValue();
		if (report != null) {
			if (reportWorker != null && !reportWorker.isDone()) {
				reportWorker.cancel(true);
				reportWorker = null;
			}
			reportWorker = new CsvReportWorker(this, report);
			reportWorker.addPropertyChangeListener(new ReportProgressListener(progressBar1));
			reportWorker.execute();
		}
	}

	public Component getFrame(Component component) {
		Component result = null;
		if(component instanceof JFrame){
			result = component;
		}else{
			result = getFrame(component.getParent());
		}
		return result;
	}

	private void closeButtonActionPerformed(ActionEvent e) {
		try {
			TranslationHelperPanel thp = PanelHelperFactory.getTranslationHelperPanel();
			JTabbedPane tp = thp.getTabbedPanel();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.REPORT_PANEL_NAME)) {
						tp.remove(i);
						tp.revalidate();
						tp.repaint();
					}
				}
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void technicalDificultiesMouseClicked(MouseEvent e) {
		technicalDificulties.setVisible(false);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		label1 = new JLabel();
		label3 = new JLabel();
		label2 = new JLabel();
		scrollPane1 = new JScrollPane();
		reportList = new JList();
		technicalDificulties = new JLabel();
		previewButton = new JRadioButton();
		excelButton = new JRadioButton();
		csvButton = new JRadioButton();
		panel3 = new JPanel();
		errorLabel = new JLabel();
		getReport = new JButton();
		closeButton = new JButton();
		progressBar1 = new JProgressBar();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {434, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Report list");
		label1.setFont(label1.getFont().deriveFont(Font.PLAIN, label1.getFont().getSize() + 2f));
		add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label3 ----
		label3.setText("Report type");
		label3.setFont(label3.getFont().deriveFont(Font.PLAIN, label3.getFont().getSize() + 2f));
		add(label3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label2 ----
		label2.setVerticalAlignment(SwingConstants.TOP);
		add(label2, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(reportList);
		}
		add(scrollPane1, new GridBagConstraints(0, 1, 1, 4, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- technicalDificulties ----
		technicalDificulties.setVisible(false);
		technicalDificulties.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				technicalDificultiesMouseClicked(e);
			}
		});
		add(technicalDificulties, new GridBagConstraints(1, 1, 1, 4, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- previewButton ----
		previewButton.setText("Preview");
		add(previewButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- excelButton ----
		excelButton.setText("Excel");
		add(excelButton, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- csvButton ----
		csvButton.setText("Csv");
		add(csvButton, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {109, 193, 0, 0, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 14, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};
			panel3.add(errorLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- getReport ----
			getReport.setText("Get report");
			getReport.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getReportActionPerformed(e);
				}
			});
			panel3.add(getReport, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- closeButton ----
			closeButton.setText("Close");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeButtonActionPerformed(e);
				}
			});
			panel3.add(closeButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));
			panel3.add(progressBar1, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel3, new GridBagConstraints(0, 5, 4, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(previewButton);
		buttonGroup1.add(excelButton);
		buttonGroup1.add(csvButton);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JLabel label1;
	private JLabel label3;
	private JLabel label2;
	private JScrollPane scrollPane1;
	private JList reportList;
	private JLabel technicalDificulties;
	private JRadioButton previewButton;
	private JRadioButton excelButton;
	private JRadioButton csvButton;
	private JPanel panel3;
	private JLabel errorLabel;
	private JButton getReport;
	private JButton closeButton;
	private JProgressBar progressBar1;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
