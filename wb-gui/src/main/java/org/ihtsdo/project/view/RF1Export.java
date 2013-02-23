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

package org.ihtsdo.project.view;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.idgeneration.IdAssignmentBI;
import org.ihtsdo.idgeneration.IdAssignmentImpl;
import org.ihtsdo.project.dataexport.DataforExport;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.ReleaseUtils;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.view.dnd.I_GetItemForModel;
import org.ihtsdo.project.view.dnd.ObjectTransferHandler;
import org.ihtsdo.tk.api.PathBI;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The Class RF1Export.
 * 
 * @author Alejandro Rodriguez
 */
public class RF1Export extends JPanel {

	/** The file chooser. */
	JFileChooser fileChooser;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The language refset. */
	private I_GetConceptData languageRefset;

	/** The source refset. */
	public I_GetConceptData sourceRefset;

	/** The source path concept. */
	public I_GetConceptData sourcePathConcept;

	/** The nsp nr. */
	private Integer nspNr;

	/** The new sct id. */
	private Long newSctId;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant REFSET_LABEL_FOREXPORT. */
	public static final String REFSET_LABEL_FOREXPORT = "RefsetLabelForExport";

	/** The Constant PATH_LABEL_FOREXPORT. */
	public static final String PATH_LABEL_FOREXPORT = "PathLabelForExport";

	/**
	 * Instantiates a new r f1 export.
	 * 
	 * @param tProj
	 *            the project
	 */
	public RF1Export(I_TerminologyProject tProj) {
		initComponents();
		exportTargetLangHelpLbl.setIcon(IconUtilities.helpIcon);
		exportTargetLangHelpLbl.setText("");
		try {
			if(tProj.getProjectType().equals(I_TerminologyProject.Type.TRANSLATION)){
				this.languageRefset = ((TranslationProject)tProj).getTargetLanguageRefset();
				config = Terms.get().getActiveAceFrameConfig();
				label7.setTransferHandler(new ObjectTransferHandler(Terms.get().getActiveAceFrameConfig(), new GetConceptForLabel(REFSET_LABEL_FOREXPORT)));
				label5.setTransferHandler(new ObjectTransferHandler(Terms.get().getActiveAceFrameConfig(), new GetConceptForLabel(PATH_LABEL_FOREXPORT)));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		}
		pBarE.setVisible(false);
		try {
			loadExportData();
		} catch (Exception e) {
			Object[] options = { "Discard saved data", "Continue" };
			int n = JOptionPane.showOptionDialog(null, "There are problems with previous saved data", "Saved configuration data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, // do
																																																// not
																																																// use
					// a
					// custom Icon
					options, // the titles of buttons
					options[1]); // default button title
			if (n == 0) {
				removeExportDataFile();
			}
			;
		}
	}

	/**
	 * Message.
	 * 
	 * @param string
	 *            the string
	 */
	private void message(String string) {
		JOptionPane.showOptionDialog(this, string, "Information", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
	}

	/**
	 * Brows exp file action performed.
	 */
	private void browsExpFileActionPerformed() {

		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select a Folder...");

		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnValue = fileChooser.showDialog(new Frame(), "Choose a folder to export");
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			txtFileExp.setText(fileChooser.getSelectedFile().getPath());
		}
	}

	/**
	 * Brow log file action performed.
	 */
	private void browLogFileActionPerformed() {
		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select a File...");

		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnValue = fileChooser.showDialog(new Frame(), "Choose a file to log results");
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			txtExplogFile.setText(fileChooser.getSelectedFile().getPath());
		}
	}

	/**
	 * B export action performed.
	 */
	private void bExportActionPerformed() {

		txtERes.setText("");
		nspNr = null;
		try {
			nspNr = Integer.parseInt(txtNsp.getText());
		} catch (Exception E) {
			message("Namespace should be a number");
			return;
		}
	
		if (chkIDGen.isSelected() && (txtIDURL.getText().trim().equals("") || txtIDUser.getText().trim().equals("") || txtIDPass.getPassword().toString().trim().equals(""))) {
			message("The id generation cannot be performed without web service data!");
			return;
		}

		if (txtRelDate.getText().trim().equals("")) {
			message("The release date must have the format YYYYMMDD !");
			return;
		}
		if (txtFileExp.getText().trim().equals("")) {
			message("The folder to export is blank!");
			return;
		}
		File file = new File(txtFileExp.getText().trim());
		if (!file.exists()) {
			message("The folder to export does not exists!");
			return;
		}
		if (rbExist.isSelected()) {
			if (txtSubsetID.getText().trim().equals("")) {
				message("The subset Id cannot be blank");
				return;

			}
			try {
				newSctId = Long.parseLong(txtSubsetID.getText());
			} catch (Exception E) {
				message("Subset Id should be a number");
				return;
			}
		}
		if (languageRefset == null) {
			message("Project target language not found!");
			return;

		}
		if (sourcePathConcept == null) {
			message("Path to export not found!");
			return;

		}

		if (chkNotTrans.isSelected() && sourceRefset == null) {
			message("Drag and drop a source refset language!");
			return;

		}

		pBarE.setMinimum(0);
		pBarE.setMaximum(100);
		pBarE.setIndeterminate(true);
		pBarE.setVisible(true);
		pBarE.repaint();
		pBarE.revalidate();
		RF1Export.this.revalidate();

		SwingUtilities.updateComponentTreeUI(pBarE);

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				Thread appThr = new Thread() {
					public void run() {

						File expF = new File(txtFileExp.getText().trim());
						if (!expF.exists())
							expF.mkdirs();

						LanguageMembershipRefset tgtLangRefset = null;
						try {
							tgtLangRefset = new LanguageMembershipRefset(languageRefset, config);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(RF1Export.this, "Errors in process!\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						String tgtLangCode = tgtLangRefset.getLangCode(config);

						String descFileName = txtFileExp.getText().trim() + File.separator + "sct1_Descriptions_" + tgtLangCode + "_" + txtRelDate.getText() + ".txt";
						String subMemFileName = txtFileExp.getText().trim() + File.separator + "der1_SubsetMembers_" + tgtLangCode + "_" + txtRelDate.getText() + ".txt";

						if (txtExplogFile.getText().trim().equals("")) {
							txtExplogFile.setText(descFileName + ".log");
						}

						File reportFile = new File(txtExplogFile.getText().trim());

						File exportDescFile = new File(descFileName);
						File exportSubsFile = new File(subMemFileName);

						saveExportData();
						String strErr = "";
						try {
							if (rbNew.isSelected()) {
								try {
									IdAssignmentBI idAssignment = new IdAssignmentImpl(txtIDURL.getText().trim(), txtIDUser.getText().trim(), txtIDPass.getPassword().toString());

									newSctId = idAssignment.createSCTID(UUID.randomUUID(), nspNr, nspNr == 0 ? "03":"13", txtRelDate.getText().trim(), txtRelDate.getText().trim(), "12345");
									System.out.println("New Subset ID: " + newSctId);
								} catch (Exception e) {
									JOptionPane.showMessageDialog(RF1Export.this, "Id assignment service is not avalable <br>" + "Check your connection data, and try again later.", "Warning", JOptionPane.WARNING_MESSAGE);
								} finally {
									pBarE.setVisible(false);
//									return;
								}
							}
							PathBI sourcePath = Terms.get().getPath(sourcePathConcept.getNid());
							I_ConfigAceFrame releaseConfig = ReleaseUtils.getNewConfigFromPath(sourcePath, config);
//							RF1DataExport expConcept = new RF1DataExport(releaseConfig, exportDescFile, exportSubsFile, reportFile, languageRefset, newSctId, sourceRefset, chkNotTrans.isSelected());
//							Terms.get().iterateConcepts(expConcept);
//
//							expConcept.closeFiles();
//							if (chkIDGen.isSelected()) {
//								expConcept.idAssignmentProcess(txtRelDate.getText().trim(), chkIdInsert.isSelected(), txtIDUser.getText().trim(), txtIDPass.getPassword().toString(), txtIDURL.getText().trim(), txtNsp.getText().trim());
//							}
//
//							txtERes.setText(expConcept.getLog());
						} catch (TerminologyException e) {
							strErr = e.getMessage();
							AceLog.getAppLog().alertAndLogException(e);
						} catch (IOException e) {
							strErr = e.getMessage();
							AceLog.getAppLog().alertAndLogException(e);
						} catch (Exception e) {
							strErr = e.getMessage();
							AceLog.getAppLog().alertAndLogException(e);
						} finally {
							pBarE.setVisible(false);
						}
						RF1Export.this.revalidate();

						if (!strErr.equals("")) {
							JOptionPane.showMessageDialog(RF1Export.this, "Errors in process!\n" + strErr, "Error", JOptionPane.ERROR_MESSAGE);
						} else {

							JOptionPane.showMessageDialog(RF1Export.this, "Language exported!", "Message", JOptionPane.INFORMATION_MESSAGE);

						}
					}
				};
				appThr.start();
			}
		});

	}

	/**
	 * Removes the export data file.
	 */
	private void removeExportDataFile() {

		File file = new File("config/DataforExport.xml");
		if (file.exists())
			file.delete();
	}

	/**
	 * Save export data.
	 */
	private void saveExportData() {
		DataforExport dfe = new DataforExport();
		dfe.setChkIDGen(chkIDGen.isSelected() ? "1" : "0");
		dfe.setChkNotTrans(chkNotTrans.isSelected() ? "1" : "0");
		dfe.setChkIdInsert(chkIdInsert.isSelected() ? "1" : "0");
		try {
			if (sourcePathConcept != null)
				dfe.setSourcePathConceptUuid(sourcePathConcept.getUids().iterator().next().toString());

			if (sourceRefset != null)
				dfe.setSourceRefsetUuid(sourceRefset.getUids().iterator().next().toString());
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		dfe.setNspNr(txtNsp.getText().trim());
		dfe.setTxtFileExp(txtFileExp.getText().trim());
		dfe.setTxtIDPass(txtIDPass.getPassword().toString());
		dfe.setTxtIDURL(txtIDURL.getText().trim());
		dfe.setTxtIDUser(txtIDUser.getText().trim());
		dfe.setTxtRelDate(txtRelDate.getText().trim());

		writeExportData(dfe);
	}

	/**
	 * Load export data.
	 */
	private void loadExportData() {
		DataforExport dfe = readExportData();
		if (dfe != null) {
			chkIDGen.setSelected(dfe.getChkIDGen().equals("1") ? true : false);
			chkNotTrans.setSelected(dfe.getChkNotTrans().equals("1") ? true : false);
			chkIdInsert.setSelected(dfe.getChkIdInsert().equals("1") ? true : false);
			try {
				if (dfe.getSourceRefsetUuid() != null && dfe.getSourceRefsetUuid() != "") {
					sourceRefset = Terms.get().getConcept(UUID.fromString(dfe.getSourceRefsetUuid()));
					label7.setText(sourceRefset.toString());
				}
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			try {
				if (dfe.getSourcePathConceptUuid() != null && dfe.getSourcePathConceptUuid() != "") {
					sourcePathConcept = Terms.get().getConcept(UUID.fromString(dfe.getSourcePathConceptUuid()));
					label5.setText(sourcePathConcept.toString());
				}
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			txtNsp.setText(dfe.getNspNr());
			txtFileExp.setText(dfe.getTxtFileExp());
			txtIDPass.setText(dfe.getTxtIDPass());
			txtIDURL.setText(dfe.getTxtIDURL());
			txtIDUser.setText(dfe.getTxtIDUser());
			txtRelDate.setText(dfe.getTxtRelDate());
		}
	}

	/**
	 * Read export data.
	 * 
	 * @return the datafor export
	 */
	public static DataforExport readExportData() {

		File file = new File("config/DataforExport.xml");

		if (file.exists()) {
			XStream xStream = new XStream(new DomDriver());
			DataforExport dfe = (DataforExport) xStream.fromXML(file);
			return dfe;
		}
		return null;
	}

	/**
	 * Write export data.
	 * 
	 * @param dataforExport
	 *            the datafor export
	 */
	public static void writeExportData(DataforExport dataforExport) {
		File outputFile = new File("config/DataforExport.xml");
		XStream xStream = new XStream(new DomDriver());

		FileOutputStream rfos;
		try {
			rfos = new FileOutputStream(outputFile);
			OutputStreamWriter rosw = new OutputStreamWriter(rfos, "UTF-8");
			xStream.toXML(dataforExport, rosw);
		} catch (FileNotFoundException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (UnsupportedEncodingException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

	}

	/**
	 * Chk not trans action performed.
	 */
	private void chkNotTransActionPerformed() {
		if (chkNotTrans.isSelected()) {
			label7.setText("Drop a Refset here");
			sourceRefset = null;
		} else {
			label7.setText("");
			sourceRefset = null;
		}
	}

	/**
	 * Export target lang help lbl mouse clicked.
	 * 
	 * @param e
	 *            the e
	 */
	private void exportTargetLangHelpLblMouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("EXPORT_DESC_LANGUAGE");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Button1 action performed.
	 */
	private void button1ActionPerformed() {
		// try {
		// if (txtRelDate.getText().trim().equals("")){
		// message("The release date must have the format YYYYMMDD !");
		// return;
		// }
		// NewSubsetPanel cPanel;
		// cPanel = new NewSubsetPanel();
		//
		// int action = JOptionPane.showOptionDialog(null, cPanel,
		// "Enter new subset data", JOptionPane.OK_CANCEL_OPTION,
		// JOptionPane.QUESTION_MESSAGE, null, null, null);
		//
		// this.requestFocus();
		//
		// if (action == JOptionPane.CANCEL_OPTION) {
		// return;
		// }
		// String nsp=cPanel.getNamespace();
		// Integer nspNr=null;
		// try{
		// nspNr=Integer.parseInt(nsp);
		// }catch (Exception E){
		// message("Namespace should be a number");
		// return;
		// }
		// String PartitionID = cPanel.getPartition();
		//
		// IdAssignmentBI idAssignment = new
		// IdAssignmentImpl("http://mgr.servers.aceworkspace.net:50040/axis2/services/id_generator","userName","passwd");
		//
		// Long newSctId = idAssignment.createSCTID(UUID.randomUUID(), nspNr,
		// PartitionID, txtRelDate.getText().trim(),
		// txtRelDate.getText().trim(), "12345");
		// System.out.println("New SCTID: " + newSctId);
		//
		// txtSubsetID.setText(newSctId.toString());
		// } catch (Exception e) {
		// AceLog.getAppLog().alertAndLogException(e);
		// }

	}

	private void rbNewItemStateChanged(ItemEvent e) {
		if(e.getStateChange() == ItemEvent.SELECTED){
			chkIDGen.setSelected(true);
			chkIDGen.setEnabled(false);
		}else if(e.getStateChange() == ItemEvent.DESELECTED){
			chkIDGen.setEnabled(true);
		}
	}

	/**
	 * The Class GetConceptForLabel.
	 */
	class GetConceptForLabel implements I_GetItemForModel {

		/** The label target. */
		private String labelTarget;

		/**
		 * Instantiates a new gets the concept for label.
		 * 
		 * @param labelTarget
		 *            the label target
		 */
		public GetConceptForLabel(String labelTarget) {
			this.labelTarget = labelTarget;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.ihtsdo.project.panel.dnd.I_GetItemForModel#getItemFromConcept
		 * (org.dwfa.ace.api.I_GetConceptData)
		 */
		@Override
		public Object getItemFromConcept(I_GetConceptData concept) throws Exception {
			if (labelTarget.equals(REFSET_LABEL_FOREXPORT)) {
				label7.setText(concept.toString());
				sourceRefset = concept;
				return null;
			}
			if (labelTarget.equals(PATH_LABEL_FOREXPORT)) {
				label5.setText(concept.toString());
				sourcePathConcept = concept;
				return null;
			}
			return null;

		}

	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		scrollPane2 = new JScrollPane();
		panel2 = new JPanel();
		exportTargetLangHelpLbl = new JLabel();
		panel5 = new JPanel();
		label3 = new JLabel();
		label5 = new JLabel();
		label17 = new JLabel();
		txtNsp = new JTextField();
		label6 = new JLabel();
		txtRelDate = new JTextField();
		label8 = new JLabel();
		panel6 = new JPanel();
		rbNew = new JRadioButton();
		rbExist = new JRadioButton();
		txtSubsetID = new JTextField();
		label1 = new JLabel();
		txtFileExp = new JTextField();
		browsExpFile = new JButton();
		label2 = new JLabel();
		txtExplogFile = new JTextField();
		browLogFile = new JButton();
		panel1 = new JPanel();
		chkNotTrans = new JCheckBox();
		label7 = new JLabel();
		panel4 = new JPanel();
		chkIDGen = new JCheckBox();
		label11 = new JLabel();
		label9 = new JLabel();
		txtIDURL = new JTextField();
		label10 = new JLabel();
		txtIDUser = new JTextField();
		labelp = new JLabel();
		txtIDPass = new JPasswordField();
		panel3 = new JPanel();
		chkIdInsert = new JCheckBox();
		label4 = new JLabel();
		pBarE = new JProgressBar();
		bExport = new JButton();
		scrollPane1 = new JScrollPane();
		txtERes = new JTextPane();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {632, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== scrollPane2 ========
		{

			//======== panel2 ========
			{
				panel2.setLayout(new GridBagLayout());
				((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {13, 131, 167, 0, 8, 0};
				((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {13, 0, 13, 0, 13, 0, 0, 95, 8, 0};
				((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};

				//---- exportTargetLangHelpLbl ----
				exportTargetLangHelpLbl.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						exportTargetLangHelpLblMouseClicked(e);
					}
				});
				panel2.add(exportTargetLangHelpLbl, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel5 ========
				{
					panel5.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
					panel5.setLayout(new GridBagLayout());
					((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
					((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
					((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- label3 ----
					label3.setText("Path to export");
					panel5.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label5 ----
					label5.setText("Drag and drop a path here");
					label5.setBackground(new Color(153, 204, 255));
					label5.setForeground(new Color(107, 102, 102));
					panel5.add(label5, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label17 ----
					label17.setText("Namespace");
					panel5.add(label17, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel5.add(txtNsp, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label6 ----
					label6.setText("Release date:");
					panel5.add(label6, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel5.add(txtRelDate, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label8 ----
					label8.setText("Subset ID:");
					panel5.add(label8, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//======== panel6 ========
					{
						panel6.setLayout(new GridBagLayout());
						((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
						((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, 1.0E-4};
						((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- rbNew ----
						rbNew.setText("New");
						rbNew.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(ItemEvent e) {
								rbNewItemStateChanged(e);
							}
						});
						panel6.add(rbNew, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- rbExist ----
						rbExist.setText("Existent:");
						rbExist.setSelected(true);
						panel6.add(rbExist, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));
						panel6.add(txtSubsetID, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel5.add(panel6, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label1 ----
					label1.setText("Export to folder:");
					panel5.add(label1, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel5.add(txtFileExp, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
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
					panel5.add(browsExpFile, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label2 ----
					label2.setText("Log file:");
					panel5.add(label2, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel5.add(txtExplogFile, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
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
					panel5.add(browLogFile, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== panel1 ========
					{
						panel1.setLayout(new GridBagLayout());
						((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
						((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- chkNotTrans ----
						chkNotTrans.setText("Replace missing target FSNs with component from language refset:");
						chkNotTrans.setVerticalAlignment(SwingConstants.TOP);
						chkNotTrans.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								chkNotTransActionPerformed();
							}
						});
						panel1.add(chkNotTrans, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- label7 ----
						label7.setBackground(new Color(153, 204, 255));
						label7.setForeground(new Color(107, 102, 102));
						label7.setText("Drag and drop a language refset here");
						panel1.add(label7, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel5.add(panel1, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel2.add(panel5, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel4 ========
				{
					panel4.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
					panel4.setLayout(new GridBagLayout());
					((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {115, 0, 80, 0};
					((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
					((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
					((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- chkIDGen ----
					chkIDGen.setText("Perform Id Assignment");
					panel4.add(chkIDGen, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label11 ----
					label11.setText("       ID Generator params");
					panel4.add(label11, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label9 ----
					label9.setText("           URL:");
					panel4.add(label9, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel4.add(txtIDURL, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label10 ----
					label10.setText("           Username:");
					panel4.add(label10, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel4.add(txtIDUser, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- labelp ----
					labelp.setText("           Password:");
					panel4.add(labelp, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					panel4.add(txtIDPass, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel2.add(panel4, new GridBagConstraints(1, 3, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel3 ========
				{
					panel3.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
					panel3.setLayout(new GridBagLayout());
					((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
					((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- chkIdInsert ----
					chkIdInsert.setText("Insert generated Ids inside of Workbench");
					panel3.add(chkIdInsert, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel2.add(panel3, new GridBagConstraints(1, 5, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label4 ----
				label4.setText("Results:");
				panel2.add(label4, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- pBarE ----
				pBarE.setIndeterminate(true);
				panel2.add(pBarE, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- bExport ----
				bExport.setText("Export");
				bExport.setIcon(null);
				bExport.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bExportActionPerformed();
					}
				});
				panel2.add(bExport, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== scrollPane1 ========
				{
					scrollPane1.setViewportView(txtERes);
				}
				panel2.add(scrollPane1, new GridBagConstraints(1, 7, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
			}
			scrollPane2.setViewportView(panel2);
		}
		add(scrollPane2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- buttonGroup3 ----
		ButtonGroup buttonGroup3 = new ButtonGroup();
		buttonGroup3.add(rbNew);
		buttonGroup3.add(rbExist);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JScrollPane scrollPane2;
	private JPanel panel2;
	private JLabel exportTargetLangHelpLbl;
	private JPanel panel5;
	private JLabel label3;
	private JLabel label5;
	private JLabel label17;
	private JTextField txtNsp;
	private JLabel label6;
	private JTextField txtRelDate;
	private JLabel label8;
	private JPanel panel6;
	private JRadioButton rbNew;
	private JRadioButton rbExist;
	private JTextField txtSubsetID;
	private JLabel label1;
	private JTextField txtFileExp;
	private JButton browsExpFile;
	private JLabel label2;
	private JTextField txtExplogFile;
	private JButton browLogFile;
	private JPanel panel1;
	private JCheckBox chkNotTrans;
	private JLabel label7;
	private JPanel panel4;
	private JCheckBox chkIDGen;
	private JLabel label11;
	private JLabel label9;
	private JTextField txtIDURL;
	private JLabel label10;
	private JTextField txtIDUser;
	private JLabel labelp;
	private JPasswordField txtIDPass;
	private JPanel panel3;
	private JCheckBox chkIdInsert;
	private JLabel label4;
	private JProgressBar pBarE;
	private JButton bExport;
	private JScrollPane scrollPane1;
	private JTextPane txtERes;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
