/*
 * Created by JFormDesigner on Fri Aug 27 15:58:59 GMT-03:00 2010
 */

package org.ihtsdo.document.report;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.bpa.process.Condition;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.report.I_Report.Reports;
import org.ihtsdo.project.panel.PanelHelperFactory;
import org.ihtsdo.project.panel.TranslationHelperPanel;

/**
 * @author Guillermo Reynoso
 */
public class ReportPanel extends JPanel {

	private static final long serialVersionUID = -5420825015191719954L;

	private static final String NO_SELECTION_MESSAGE = "No reports selected.";
	private static final String REPORT_NOT_CREATED = "Report not created.";
	private JFileChooser chooser;

	private DefaultListModel reportListModel;

	public ReportPanel() {
		initComponents();
		initCustomComponents();
	}

	private void initCustomComponents() {

		progressBar1.setVisible(false);

		reportListModel = new DefaultListModel();
		reportList.setModel(reportListModel);

		reportList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(reportList.getSelectedValue() != null){
					if(reportList.getSelectedValue() instanceof ProjectHistoricalReport){
						pdfReport.setEnabled(false);
					}else{
						pdfReport.setEnabled(true);
					}
					showMessage("");
				}
			}
		});

		for (Reports repEnum : I_Report.Reports.values()) {
			reportListModel.addElement(repEnum.getReport());
		}

	}

	private void showError(String error){
		errorLabel.setForeground(Color.RED);
		errorLabel.setText(error);
	}

	private void showMessage(String message){
		errorLabel.setForeground(Color.BLACK);
		errorLabel.setText(message);
	}

	private void pdfReportActionPerformed(ActionEvent e) {
		I_Report report = (I_Report)reportList.getSelectedValue();
		if(report != null){
			progressBar1.setVisible(true);
			JFrame reportFrame = report.getReportPanel();
			if(reportFrame != null){
				reportFrame.setSize(new Dimension(750,800));
				reportFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				reportFrame.setVisible(true);
			}
			progressBar1.setVisible(false);
			
		}else{
			showError(NO_SELECTION_MESSAGE);
		}
	}

	private void excelReportActionPerformed(ActionEvent e) {
		I_Report report = (I_Report)reportList.getSelectedValue();
		if(report != null){
			progressBar1.setVisible(true);
			File excelFile = report.getExcelSourceWorkbook();
			if(excelFile != null){
				try {
					Desktop desktop = null;
					if (Desktop.isDesktopSupported()) {
						desktop = Desktop.getDesktop();
						desktop.open(excelFile);
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}else{
				showError(REPORT_NOT_CREATED);
			}

			progressBar1.setVisible(false);
		}else{
			showError(NO_SELECTION_MESSAGE);
		}
	}

	private void csvFileActionPerformed(ActionEvent e) {
		I_Report report = (I_Report)reportList.getSelectedValue();
		if(report != null){
			progressBar1.setVisible(true);
			chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle("Select folder");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			chooser.setAcceptAllFileFilterUsed(false);
			
			if (chooser.showOpenDialog(this) == JFileChooser.OPEN_DIALOG) {
				 File selectedFolder = chooser.getSelectedFile();
				 SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-hh-mm");
				 File result = new File(selectedFolder, sdf.format(new Date()) + "_" + report.toString().replace(' ', '_') +".csv");
				 File csvFile = report.getCsv();
				 try {
					ExcelReportUtil.copyFile(csvFile, result);
				} catch (IOException e1) {
					e1.printStackTrace();
					progressBar1.setVisible(false);
				}
			} 
			progressBar1.setVisible(false);
			chooser.setVisible(false);
		}
	}

	private void closeButtonActionPerformed(ActionEvent e) {
		try {
			TranslationHelperPanel thp = PanelHelperFactory.getTranslationHelperPanel();
			JTabbedPane tp = thp.getTabbedPanel();
			if(tp != null){
				int tabCount = tp.getTabCount();
				for(int i = 0; i< tabCount; i++){
					if(tp.getTitleAt(i).equals(TranslationHelperPanel.REPORT_PANEL_NAME)){
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

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		pdfReport = new JButton();
		excelReport = new JButton();
		csvFile = new JButton();
		closeButton = new JButton();
		panel2 = new JPanel();
		scrollPane1 = new JScrollPane();
		reportList = new JList();
		panel3 = new JPanel();
		errorLabel = new JLabel();
		progressBar1 = new JProgressBar();
		label1 = new JLabel();
		label2 = new JLabel();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(10, 10));

		//======== panel1 ========
		{
			panel1.setBorder(new MatteBorder(1, 0, 0, 0, Color.gray));
			panel1.setLayout(new FlowLayout(FlowLayout.RIGHT));

			//---- pdfReport ----
			pdfReport.setText("Preview");
			pdfReport.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pdfReportActionPerformed(e);
				}
			});
			panel1.add(pdfReport);

			//---- excelReport ----
			excelReport.setText("Excel report");
			excelReport.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					excelReportActionPerformed(e);
				}
			});
			panel1.add(excelReport);

			//---- csvFile ----
			csvFile.setText("CSV data");
			csvFile.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					csvFileActionPerformed(e);
				}
			});
			panel1.add(csvFile);

			//---- closeButton ----
			closeButton.setText("Close");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeButtonActionPerformed(e);
				}
			});
			panel1.add(closeButton);
		}
		add(panel1, BorderLayout.SOUTH);

		//======== panel2 ========
		{
			panel2.setLayout(new BorderLayout(5, 5));

			//======== scrollPane1 ========
			{
				scrollPane1.setViewportView(reportList);
			}
			panel2.add(scrollPane1, BorderLayout.CENTER);

			//======== panel3 ========
			{
				panel3.setLayout(new GridBagLayout());
				((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {109, 188, 0};
				((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
				panel3.add(errorLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
				panel3.add(progressBar1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel2.add(panel3, BorderLayout.SOUTH);

			//---- label1 ----
			label1.setText("Report list");
			label1.setFont(label1.getFont().deriveFont(Font.BOLD, label1.getFont().getSize() + 2f));
			panel2.add(label1, BorderLayout.NORTH);

			//---- label2 ----
			label2.setText("<html>Choose an item form the \"Report list\",and click<br> \"Preview\" button to visualize the report.<BR> \"Excel report\" button to open excel pivot table<br>report. Or simply get a CSV file clicking <br>\"CSV data\" </html>");
			label2.setVerticalAlignment(SwingConstants.TOP);
			panel2.add(label2, BorderLayout.EAST);
		}
		add(panel2, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JButton pdfReport;
	private JButton excelReport;
	private JButton csvFile;
	private JButton closeButton;
	private JPanel panel2;
	private JScrollPane scrollPane1;
	private JList reportList;
	private JPanel panel3;
	private JLabel errorLabel;
	private JProgressBar progressBar1;
	private JLabel label1;
	private JLabel label2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
