/*
 * Created by JFormDesigner on Fri Mar 05 15:15:30 GMT-03:00 2010
 */

package org.ihtsdo.project.panel;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.panel.dnd.I_GetItemForModel;
import org.ihtsdo.project.panel.dnd.ObjectTransferHandler;
import org.ihtsdo.project.refset.ExportConceptMemberRefsetAsSubset;
import org.ihtsdo.project.refset.ExportConceptMemberRefsetToRefset;
import org.ihtsdo.project.refset.ImportConceptRefsetAsRefset;
import org.ihtsdo.project.refset.ImportConceptSubsetAsRefset;
import org.ihtsdo.project.util.IconUtilities;

/**
 * @author Guillermo Reynoso
 */
public class ConceptRefsetFromToSubsetPanel extends JPanel {
	/**
	 * 
	 */
	JFileChooser fileChooser;


	public I_GetConceptData refsetExp;


	public I_GetConceptData refsetEd;

	private SimpleDateFormat formatter;


	private Long[] eRes;


	private Integer[] iRes;
	
	private static final long serialVersionUID = 1L;


	public static final String REFSET_LABEL_FOREXPORT = "RefsetLabelForExport";


	private static final String EFFECTIVETIME_SEPARATOR = "-ET ";
	public ConceptRefsetFromToSubsetPanel() {
		initComponents();
		
		importHelpLabel.setIcon(IconUtilities.helpIcon);
		importHelpLabel.setText("");
		
		exportHelpPanel.setIcon(IconUtilities.helpIcon);
		exportHelpPanel.setText("");

		pBarE.setVisible(false);
		pBarI.setVisible(false);
		//		try {
		//			clabelEd = new TermComponentLabel();
		//			clabelExp = new TermComponentLabel();
		//		} catch (TerminologyException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		formatter=new SimpleDateFormat("yyyyMMddHHmmss");
		clabelEd.setText("Drop a Refset here");
		clabelExp.setText("Drop a Refset here");
		try {
			clabelEd.setTransferHandler(new ObjectTransferHandler(Terms.get().getActiveAceFrameConfig(), new GetConceptForLabel("")));


			clabelExp.setTransferHandler(new ObjectTransferHandler(Terms.get().getActiveAceFrameConfig(), new GetConceptForLabel(REFSET_LABEL_FOREXPORT)));
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		panel4.add(clabelEd, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
		//			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		//			new Insets(0, 0, 0, 5), 0, 0));
		//
		//		panel2.add(clabelExp, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
		//			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		//			new Insets(0, 0, 5, 5), 0, 0));


	}

	private void browsImpFile2ActionPerformed() {

		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select a File...");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		String tmpFt=rbIRF1.isSelected()? "RF1":"RF2";
		int returnValue = fileChooser
		.showDialog(new Frame(), "Choose a file with " + tmpFt +  " member format");
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			txtFileImp.setText(fileChooser.getSelectedFile().getPath());
			checkRefsetFromFile(txtFileImp.getText());
		}
	}

	private void checkRefsetFromFile(String importFile) {
		try {
			BufferedReader inputFileReaderCtrl = new BufferedReader(new FileReader(importFile));

			String currentLine;
			currentLine = inputFileReaderCtrl.readLine();
			int lineCount=1;
			String refsetId ;
			I_GetConceptData tmpRefset = null;
			while (currentLine != null && lineCount<101) {

				if (!currentLine.trim().equals("")) {
					String[] lineParts = currentLine.split("\t");
					try {
						if (rbIRF1.isSelected()){
							refsetId = lineParts[0];
							tmpRefset = Terms.get().getConcept( Type3UuidFactory.fromSNOMED(refsetId));
						}else{
							refsetId = lineParts[4];

							try{
								Long.parseLong(refsetId);
								tmpRefset = Terms.get().getConcept( Type3UuidFactory.fromSNOMED(refsetId));
							}catch(NumberFormatException e){
								tmpRefset = Terms.get().getConcept( UUID.fromString(refsetId));
							}
							
						}
						break;
					} catch (TerminologyException e) {
						e.printStackTrace();
						tmpRefset=null;
					}catch (Exception e) {
						lineCount=101;
						e.printStackTrace();
					}
				}
				currentLine = inputFileReaderCtrl.readLine();
				lineCount++;
			}
			inputFileReaderCtrl.close();
			if (tmpRefset!=null){
				rbNew.setSelected(false);
				rbAdd.setSelected(true);
				clabelEd.setText(tmpRefset.toString());
				refsetEd=tmpRefset;
			}else{
				rbNew.setSelected(true);
				rbAdd.setSelected(false);
				int pos=importFile.indexOf(EFFECTIVETIME_SEPARATOR);
				int posini=importFile.lastIndexOf(File.separator);
				posini++;
				if (pos>-1){
					txtNewRefsetName.setText(importFile.substring(posini,pos).trim());
				}else{
					pos=importFile.lastIndexOf(".");
					if (pos>-1){
						txtNewRefsetName.setText(importFile.substring(posini,pos).trim());
					}else{
						txtNewRefsetName.setText(importFile.substring(posini));
					}
						
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void browLogFile2ActionPerformed() {

		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select a File...");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnValue = fileChooser
		.showDialog(new Frame(), "Choose a file to log results");
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			txtImpLogFile.setText(fileChooser.getSelectedFile().getPath());
		}
	}

	private void bImportActionPerformed() {

		txtIRes.setText("");
		iRes=new Integer[]{0,0};
		if (rbNew.isSelected() && txtNewRefsetName.getText().trim().equals("")){
			message("The new refset name to create is blank!");
			return;
		}

		if (rbAdd.isSelected() && (refsetEd==null )){
			message("The existent refset is blank!");
			return;
		}

		if (txtFileImp.getText().trim().equals("")){
			message("The file name to import is blank!");
			return;
		}

		pBarI.setMinimum(0);
		pBarI.setMaximum(100);
		pBarI.setIndeterminate(true);
		pBarI.setVisible(true);
		pBarI.repaint();
		pBarI.revalidate();
		panel9.repaint();
		panel9.revalidate();
		this.revalidate();
		SwingUtilities.updateComponentTreeUI(pBarI);
		
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				Thread appThr = new Thread() {
					public void run() {
						File importFile=new File(txtFileImp.getText().trim());
						if (txtImpLogFile.getText().trim().equals("")){
							txtImpLogFile.setText(txtFileImp.getText().trim() + ".log" );
						}

						File reportFile=new File(txtImpLogFile.getText().trim());

						String strErr="";
						try {
							if (rbIRF1.isSelected()){
								ImportConceptSubsetAsRefset impConcept=new ImportConceptSubsetAsRefset();
								if (rbNew.isSelected() ){
									iRes=impConcept.importFromFile(importFile, reportFile, txtNewRefsetName.getText().trim(),Terms.get().getId(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids()).getNid());

									txtIRes.setText("Imported from file " + importFile.getName()  + " : " + iRes[0] + " concepts");

								}
								else{
									iRes=impConcept.importFromFileToExistRefset(importFile, reportFile,refsetEd,chkIncr.isSelected());

									txtIRes.setText("Imported from file " + importFile.getName()  + " : " + iRes[0] + " concepts");

									if (!chkIncr.isSelected())
										txtIRes.setText(txtIRes.getText() + "\nDeleted from Refset " + refsetEd.toString() +  " : " + iRes[1] + " concepts");
								}
							}else{
								ImportConceptRefsetAsRefset impConcept=new ImportConceptRefsetAsRefset();
								if (rbNew.isSelected() ){
									iRes=impConcept.importFromFile(importFile, reportFile, txtNewRefsetName.getText().trim(),Terms.get().getId(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids()).getNid());
									txtIRes.setText("Imported from file " + importFile.getName()  + " : " + iRes[0] + " concepts");
								}
								else{
									iRes=impConcept.importFromFileToExistRefset(importFile, reportFile,refsetEd,chkIncr.isSelected());
									txtIRes.setText("Imported from file " + importFile.getName()  + " : " + iRes[0] + " concepts");

									if (!chkIncr.isSelected())
										txtIRes.setText(txtIRes.getText() + "\nDeleted from Refset " + refsetEd.toString() +  " : " + iRes[1] + " concepts");
								
								}


							}
						} catch (TerminologyException e) {
							strErr=e.getMessage();
							e.printStackTrace();
						} catch (IOException e) {
							strErr=e.getMessage();
							e.printStackTrace();
						} catch (Exception e) {
							strErr=e.getMessage();
							e.printStackTrace();
						}

						pBarI.setVisible(false);
						ConceptRefsetFromToSubsetPanel.this.revalidate();
						if (strErr==null || !strErr.equals("")){
							JOptionPane.showMessageDialog(ConceptRefsetFromToSubsetPanel.this,
									"Errors in process!\n" + strErr, 
									"Error", JOptionPane.ERROR_MESSAGE);
						}else{
							JOptionPane.showMessageDialog(ConceptRefsetFromToSubsetPanel.this,
									"File imported!", 
									"Message", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				};
				appThr.start();
			}
		});

	}

	/**
	 * Message.
	 * 
	 * @param string the string
	 */
	private void message(String string) {
		JOptionPane.showOptionDialog(   
				this,   
				string,   
				"Information", JOptionPane.DEFAULT_OPTION,   
				JOptionPane.INFORMATION_MESSAGE, null, null,   
				null );   
	}

	private void browsExpFileActionPerformed() {

		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select a Folder...");

		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnValue = fileChooser
		.showDialog(new Frame(), "Choose a folder to export");
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			txtFileExp.setText(fileChooser.getSelectedFile().getPath());
		}
	}

	private void browLogFileActionPerformed() {
		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select a File...");

		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnValue = fileChooser
		.showDialog(new Frame(), "Choose a file to log results");
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			txtExplogFile.setText(fileChooser.getSelectedFile().getPath());
		}
	}

	private void bExportActionPerformed() {

		txtERes.setText("");
		eRes=new Long[]{0l,0l};
		if (refsetExp==null){
			message("The refset to export is blank!");
			return;
		}

		if (txtFileExp.getText().trim().equals("")){
			message("The folder to export is blank!");
			return;
		}		
		
		pBarE.setMinimum(0);
		pBarE.setMaximum(100);
		pBarE.setIndeterminate(true);
		pBarE.setVisible(true);
		pBarE.repaint();
		pBarE.revalidate();
		panel5.repaint();
		panel5.revalidate();
		ConceptRefsetFromToSubsetPanel.this.revalidate();

		SwingUtilities.updateComponentTreeUI(pBarE);

		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				Thread appThr = new Thread() {
					public void run() {

						String fileDate=formatter.format(new java.util.Date().getTime());
						String fileName=	txtFileExp.getText().trim() + File.separator + clabelExp.getText() + EFFECTIVETIME_SEPARATOR + fileDate ;
						
						if (txtExplogFile.getText().trim().equals("")){
							txtExplogFile.setText(fileName + ".log");
						}

						File reportFile=new File(txtExplogFile.getText().trim());
						
						String fileName2=fileName;
						File exportFile2=null;
						if (rbERF2.isSelected()){
							fileName2+="_SCTID";
							fileName2+= (rbTxt.isSelected()? ".txt": ".csv");
							exportFile2=new File(fileName2);
							
							fileName+="_UUID";
						}
						
						fileName+= (rbTxt.isSelected()? ".txt": ".csv");
						File exportFile=new File(fileName);


						String strErr="";
						try {
							if (rbERF1.isSelected()){
								ExportConceptMemberRefsetAsSubset expConcept=new ExportConceptMemberRefsetAsSubset();
								eRes=expConcept.exportFile(exportFile, reportFile, refsetExp,rbCsv.isSelected());
								txtERes.setText("Exported to file " + exportFile.getName()  + " : " + eRes[0] + " lines");
							}else{
								ExportConceptMemberRefsetToRefset expConcept=new ExportConceptMemberRefsetToRefset();
								eRes=expConcept.exportFile(exportFile,exportFile2, reportFile, refsetExp,rbCsv.isSelected());
								txtERes.setText("Exported to UUID file " + exportFile.getName()  + " : " + eRes[0] + " lines.\n" +
												"Exported to SCTID file " + exportFile2.getName()  + " : " + eRes[1] + " lines");

							}
						} catch (TerminologyException e) {
							strErr=e.getMessage();
							e.printStackTrace();
						} catch (IOException e) {
							strErr=e.getMessage();
							e.printStackTrace();
						} catch (Exception e) {
							strErr=e.getMessage();
							e.printStackTrace();
						}
						pBarE.setVisible(false);
						ConceptRefsetFromToSubsetPanel.this.revalidate();
						
						if (!strErr.equals("")){
							JOptionPane.showMessageDialog(ConceptRefsetFromToSubsetPanel.this,
									"Errors in process!\n" + strErr, 
									"Error", JOptionPane.ERROR_MESSAGE);
						}else{
							
							JOptionPane.showMessageDialog(ConceptRefsetFromToSubsetPanel.this,
									"Refset exported!", 
									"Message", JOptionPane.INFORMATION_MESSAGE);
							
						}
					}
				};
				appThr.start();
			}
		});

	}

	private void bCloseActionPerformed() {
		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
			AceFrame ace=config.getAceFrame();
			JTabbedPane tp=ace.getCdePanel().getConceptTabs();
			if (tp!=null){
				int tabCount=tp.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.SUBSET_IMPORT_EXPORT)){
						tp.remove(i);
					}
					tp.repaint();
					tp.revalidate();

				}
			}
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void importHelpLabelMouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("IMPORT_SUBSET_REFSET");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private void exportHelpPanelMouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("EXPORT_SUBSET_REFSET");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}
	public class GetConceptForLabel implements I_GetItemForModel{

		private String labelTarget;
		public GetConceptForLabel(String labelTarget){
			this.labelTarget=labelTarget;
		}
		@Override
		public Object getItemFromConcept(I_GetConceptData concept) throws Exception {
			if (labelTarget.equals(REFSET_LABEL_FOREXPORT)){
				clabelExp.setText(concept.toString());
				refsetExp=concept;
				return null;
			}
			clabelEd.setText(concept.toString());
			refsetEd=concept;

			return null;
		}

	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		tabbedPane1 = new JTabbedPane();
		panel3 = new JPanel();
		importHelpLabel = new JLabel();
		label7 = new JLabel();
		panel1 = new JPanel();
		rbIRF1 = new JRadioButton();
		rbIRF2 = new JRadioButton();
		label5 = new JLabel();
		txtFileImp = new JTextField();
		browsImpFile2 = new JButton();
		label6 = new JLabel();
		txtImpLogFile = new JTextField();
		browLogFile2 = new JButton();
		panel4 = new JPanel();
		rbNew = new JRadioButton();
		txtNewRefsetName = new JTextField();
		rbAdd = new JRadioButton();
		clabelEd = new JLabel();
		chkIncr = new JCheckBox();
		panel9 = new JPanel();
		pBarI = new JProgressBar();
		bImport = new JButton();
		bClose2 = new JButton();
		panel11 = new JPanel();
		label10 = new JLabel();
		scrollPane2 = new JScrollPane();
		txtIRes = new JTextPane();
		panel2 = new JPanel();
		exportHelpPanel = new JLabel();
		label8 = new JLabel();
		panel7 = new JPanel();
		rbERF1 = new JRadioButton();
		rbERF2 = new JRadioButton();
		label1 = new JLabel();
		txtFileExp = new JTextField();
		browsExpFile = new JButton();
		label2 = new JLabel();
		txtExplogFile = new JTextField();
		browLogFile = new JButton();
		label3 = new JLabel();
		clabelExp = new JLabel();
		label9 = new JLabel();
		panel8 = new JPanel();
		rbTxt = new JRadioButton();
		rbCsv = new JRadioButton();
		panel5 = new JPanel();
		pBarE = new JProgressBar();
		bExport = new JButton();
		bClose = new JButton();
		panel10 = new JPanel();
		label4 = new JLabel();
		scrollPane1 = new JScrollPane();
		txtERes = new JTextPane();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== tabbedPane1 ========
		{

			//======== panel3 ========
			{
				panel3.setLayout(new GridBagLayout());
				((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 25, 0, 0, 90, 0};
				((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

				//---- importHelpLabel ----
				importHelpLabel.setText("text");
				importHelpLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						importHelpLabelMouseClicked(e);
					}
				});
				panel3.add(importHelpLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- label7 ----
				label7.setText("Format:");
				panel3.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- rbIRF1 ----
					rbIRF1.setText("Release Format 1");
					rbIRF1.setSelected(true);
					panel1.add(rbIRF1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- rbIRF2 ----
					rbIRF2.setText("Release Format 2");
					panel1.add(rbIRF2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel3.add(panel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label5 ----
				label5.setText("Import from file:");
				panel3.add(label5, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel3.add(txtFileImp, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- browsImpFile2 ----
				browsImpFile2.setText("...");
				browsImpFile2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						browsImpFile2ActionPerformed();
					}
				});
				panel3.add(browsImpFile2, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- label6 ----
				label6.setText("Log file:");
				panel3.add(label6, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel3.add(txtImpLogFile, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- browLogFile2 ----
				browLogFile2.setText("...");
				browLogFile2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						browLogFile2ActionPerformed();
					}
				});
				panel3.add(browLogFile2, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel4 ========
				{
					panel4.setLayout(new GridBagLayout());
					((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {35, 0, 0, 0};
					((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};
					((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

					//---- rbNew ----
					rbNew.setText("New Concept Refset Name:");
					rbNew.setSelected(true);
					panel4.add(rbNew, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel4.add(txtNewRefsetName, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- rbAdd ----
					rbAdd.setText("Add to Concept Refset:");
					panel4.add(rbAdd, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel4.add(clabelEd, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- chkIncr ----
					chkIncr.setText("Incremental");
					panel4.add(chkIncr, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel3.add(panel4, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel9 ========
				{
					panel9.setLayout(new GridBagLayout());
					((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- pBarI ----
					pBarI.setIndeterminate(true);
					panel9.add(pBarI, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- bImport ----
					bImport.setText("Import");
					bImport.setIcon(null);
					bImport.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							bImportActionPerformed();
						}
					});
					panel9.add(bImport, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- bClose2 ----
					bClose2.setText("Close");
					bClose2.setIcon(null);
					bClose2.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							bCloseActionPerformed();
							bCloseActionPerformed();
						}
					});
					panel9.add(bClose2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel3.add(panel9, new GridBagConstraints(1, 7, 2, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel11 ========
				{
					panel11.setLayout(new GridBagLayout());
					((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

					//---- label10 ----
					label10.setText("Results:");
					panel11.add(label10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));
				}
				panel3.add(panel11, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== scrollPane2 ========
				{

					//---- txtIRes ----
					txtIRes.setEnabled(false);
					scrollPane2.setViewportView(txtIRes);
				}
				panel3.add(scrollPane2, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
			}
			tabbedPane1.addTab("Import", panel3);


			//======== panel2 ========
			{
				panel2.setLayout(new GridBagLayout());
				((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 25, 0, 0, 0, 0, 90, 0};
				((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

				//---- exportHelpPanel ----
				exportHelpPanel.setText("text");
				exportHelpPanel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						exportHelpPanelMouseClicked(e);
					}
				});
				panel2.add(exportHelpPanel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- label8 ----
				label8.setText("Format:");
				panel2.add(label8, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel7 ========
				{
					panel7.setLayout(new GridBagLayout());
					((GridBagLayout)panel7.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel7.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel7.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel7.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- rbERF1 ----
					rbERF1.setText("Release Format 1");
					rbERF1.setSelected(true);
					panel7.add(rbERF1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- rbERF2 ----
					rbERF2.setText("Release Format 2");
					panel7.add(rbERF2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel2.add(panel7, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label1 ----
				label1.setText("Export to folder:");
				panel2.add(label1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel2.add(txtFileExp, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- browsExpFile ----
				browsExpFile.setText("...");
				browsExpFile.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						browsExpFileActionPerformed();
					}
				});
				panel2.add(browsExpFile, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- label2 ----
				label2.setText("Log file:");
				panel2.add(label2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel2.add(txtExplogFile, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- browLogFile ----
				browLogFile.setText("...");
				browLogFile.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						browLogFileActionPerformed();
					}
				});
				panel2.add(browLogFile, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- label3 ----
				label3.setText("Refset Concept:");
				panel2.add(label3, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel2.add(clabelExp, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label9 ----
				label9.setText("File format:");
				panel2.add(label9, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel8 ========
				{
					panel8.setLayout(new GridBagLayout());
					((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- rbTxt ----
					rbTxt.setText(".txt");
					rbTxt.setSelected(true);
					panel8.add(rbTxt, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- rbCsv ----
					rbCsv.setText(".csv");
					panel8.add(rbCsv, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel2.add(panel8, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel5 ========
				{
					panel5.setLayout(new GridBagLayout());
					((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {35, 0, 0, 0};
					((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- pBarE ----
					pBarE.setIndeterminate(true);
					panel5.add(pBarE, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- bExport ----
					bExport.setText("Export");
					bExport.setIcon(null);
					bExport.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							bExportActionPerformed();
						}
					});
					panel5.add(bExport, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- bClose ----
					bClose.setText("Close");
					bClose.setIcon(null);
					bClose.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							bCloseActionPerformed();
						}
					});
					panel5.add(bClose, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel2.add(panel5, new GridBagConstraints(1, 8, 2, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel10 ========
				{
					panel10.setLayout(new GridBagLayout());
					((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

					//---- label4 ----
					label4.setText("Results:");
					panel10.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));
				}
				panel2.add(panel10, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== scrollPane1 ========
				{

					//---- txtERes ----
					txtERes.setEnabled(false);
					scrollPane1.setViewportView(txtERes);
				}
				panel2.add(scrollPane1, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
			}
			tabbedPane1.addTab("Export", panel2);

		}
		add(tabbedPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- buttonGroup3 ----
		ButtonGroup buttonGroup3 = new ButtonGroup();
		buttonGroup3.add(rbIRF1);
		buttonGroup3.add(rbIRF2);

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(rbNew);
		buttonGroup1.add(rbAdd);

		//---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(rbERF1);
		buttonGroup2.add(rbERF2);

		//---- buttonGroup4 ----
		ButtonGroup buttonGroup4 = new ButtonGroup();
		buttonGroup4.add(rbTxt);
		buttonGroup4.add(rbCsv);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JTabbedPane tabbedPane1;
	private JPanel panel3;
	private JLabel importHelpLabel;
	private JLabel label7;
	private JPanel panel1;
	private JRadioButton rbIRF1;
	private JRadioButton rbIRF2;
	private JLabel label5;
	private JTextField txtFileImp;
	private JButton browsImpFile2;
	private JLabel label6;
	private JTextField txtImpLogFile;
	private JButton browLogFile2;
	private JPanel panel4;
	private JRadioButton rbNew;
	private JTextField txtNewRefsetName;
	private JRadioButton rbAdd;
	private JLabel clabelEd;
	private JCheckBox chkIncr;
	private JPanel panel9;
	private JProgressBar pBarI;
	private JButton bImport;
	private JButton bClose2;
	private JPanel panel11;
	private JLabel label10;
	private JScrollPane scrollPane2;
	private JTextPane txtIRes;
	private JPanel panel2;
	private JLabel exportHelpPanel;
	private JLabel label8;
	private JPanel panel7;
	private JRadioButton rbERF1;
	private JRadioButton rbERF2;
	private JLabel label1;
	private JTextField txtFileExp;
	private JButton browsExpFile;
	private JLabel label2;
	private JTextField txtExplogFile;
	private JButton browLogFile;
	private JLabel label3;
	private JLabel clabelExp;
	private JLabel label9;
	private JPanel panel8;
	private JRadioButton rbTxt;
	private JRadioButton rbCsv;
	private JPanel panel5;
	private JProgressBar pBarE;
	private JButton bExport;
	private JButton bClose;
	private JPanel panel10;
	private JLabel label4;
	private JScrollPane scrollPane1;
	private JTextPane txtERes;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
