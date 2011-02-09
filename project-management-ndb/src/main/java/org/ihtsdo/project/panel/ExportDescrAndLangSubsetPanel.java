/*
 * Created by JFormDesigner on Fri Mar 05 15:15:30 GMT-03:00 2010
 */

package org.ihtsdo.project.panel;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.panel.dnd.I_GetItemForModel;
import org.ihtsdo.project.panel.dnd.ObjectTransferHandler;
import org.ihtsdo.project.refset.ExportDescriptionAndLanguageSubset;
import org.ihtsdo.tk.api.Precedence;

/**
 * @author Guillermo Reynoso
 */
public class ExportDescrAndLangSubsetPanel extends JPanel {
	/**
	 * 
	 */
	JFileChooser fileChooser;

	private Long[] eRes;

	private I_ConfigAceFrame config;

	private I_GetConceptData languageRefset;

	public I_GetConceptData sourceRefset;

	private static final long serialVersionUID = 1L;

	public static final String REFSET_LABEL_FOREXPORT = "RefsetLabelForExport";


	public ExportDescrAndLangSubsetPanel(TranslationProject project) {
		initComponents();
		try {
			this.languageRefset=project.getTargetLanguageRefset();
			DefaultListModel lModel=new DefaultListModel();
			config=Terms.get().getActiveAceFrameConfig();
			I_IntSet isaType = Terms.get().newIntSet();
			isaType.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			
			I_GetConceptData transStatusCpt = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_STATUS.getUids());

			lModel.addElement(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_DELIVERED_STATUS.getUids()));
			lModel.addElement(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids()));

			for (I_GetConceptData child : transStatusCpt.getDestRelOrigins(
					config.getAllowedStatus(),
					isaType,
					config.getViewPositionSetReadOnly(),
					Precedence.TIME,
					config.getConflictResolutionStrategy())) {
				lModel.addElement(child);
			}
			list1.setModel(lModel);
			list2.setModel(new DefaultListModel());

			label7.setTransferHandler(new ObjectTransferHandler(Terms.get().getActiveAceFrameConfig(), new GetConceptForLabel(REFSET_LABEL_FOREXPORT)));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pBarE.setVisible(false);

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

		if (txtRelDate.getText().trim().equals("")){
			message("The release date must have the format YYYYMMDD !");
			return;
		}
		if (txtFileExp.getText().trim().equals("")){
			message("The folder to export is blank!");
			return;
		}		
		File file=new File(txtFileExp.getText().trim());
		if (!file.exists()){
			message("The folder to export does not exists!");
			return;	
		}
		if(languageRefset == null){
			message("Project target language not found!");
			return;	
			
		}
		if (chkNotTrans.isSelected() && sourceRefset==null){
			message("Drag and drop a source refset language!");
			return;	
			
		}
		DefaultListModel lModel = (DefaultListModel) list2.getModel();
		final HashSet<Integer> excludedStatus=new HashSet<Integer>();
		
		for (int i=0;i<lModel.getSize();i++){
			excludedStatus.add(((I_GetConceptData)lModel.getElementAt(i)).getConceptNid());
		}
		
		pBarE.setMinimum(0);
		pBarE.setMaximum(100);
		pBarE.setIndeterminate(true);
		pBarE.setVisible(true);
		pBarE.repaint();
		pBarE.revalidate();
		ExportDescrAndLangSubsetPanel.this.revalidate();

		SwingUtilities.updateComponentTreeUI(pBarE);

		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				Thread appThr = new Thread() {
					public void run() {

						String descFileName=	txtFileExp.getText().trim() + File.separator + "sct1_Descriptions_"  + txtRelDate.getText() + ".txt" ;
						String subMemFileName=txtFileExp.getText().trim() + File.separator + "der1_SubsetMembers_"  + txtRelDate.getText() + ".txt" ;
						
						if (txtExplogFile.getText().trim().equals("")){
							txtExplogFile.setText(descFileName + ".log");
						}

						File reportFile=new File(txtExplogFile.getText().trim());

						File exportDescFile=new File(descFileName);
						File exportSubsFile=new File(subMemFileName);


						String strErr="";
						try {
								ExportDescriptionAndLanguageSubset expConcept=new ExportDescriptionAndLanguageSubset(config, exportDescFile,exportSubsFile, reportFile, languageRefset,excludedStatus,sourceRefset, chkNotTrans.isSelected());
								Terms.get().iterateConcepts(expConcept);
								eRes=expConcept.getResults();
								expConcept.closeFiles();
								txtERes.setText("Exported to file " + exportDescFile.getName()  + " : " + eRes[0] + " lines\nExported to file " + exportSubsFile.getName()  + " : " + eRes[1] + " lines");
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
						ExportDescrAndLangSubsetPanel.this.revalidate();

						if (!strErr.equals("")){
							JOptionPane.showMessageDialog(ExportDescrAndLangSubsetPanel.this,
									"Errors in process!\n" + strErr, 
									"Error", JOptionPane.ERROR_MESSAGE);
						}else{

							JOptionPane.showMessageDialog(ExportDescrAndLangSubsetPanel.this,
									"Refset exported!", 
									"Message", JOptionPane.INFORMATION_MESSAGE);

						}
					}
				};
				appThr.start();
			}
		});

	}


	private void button1ActionPerformed(ActionEvent e) {
		excludeSelectedStatus();
	}

	private void excludeSelectedStatus() {
		int[] sRows=list1.getSelectedIndices();
		if (sRows!=null){
			DefaultListModel model = (DefaultListModel)list1.getModel();
			DefaultListModel model2 = (DefaultListModel)list2.getModel();
			for (int i=sRows.length-1;i>-1;i--){
				I_GetConceptData status = (I_GetConceptData)model.getElementAt(sRows[i]);

				model2.addElement(status);
				model.remove(sRows[i]);

			}
			list1.validate();
		}
	}




	private void button2ActionPerformed(ActionEvent e) {
		includeSelectedStatus();
	}
	private void includeSelectedStatus() {
		int[] sRows=list2.getSelectedIndices();
		if (sRows!=null){
			DefaultListModel model = (DefaultListModel)list1.getModel();
			DefaultListModel model2 = (DefaultListModel)list2.getModel();
			for (int i=sRows.length-1;i>-1;i--){
				I_GetConceptData status = (I_GetConceptData)model2.getElementAt(sRows[i]);

				model.addElement(status);
				model2.remove(sRows[i]);

			}
			list2.validate();
		}

	}

	private void chkNotTransActionPerformed() {
		if (chkNotTrans.isSelected()){
			label7.setText("Drop a Refset here");
			sourceRefset=null;
		}else{
			label7.setText("");
			sourceRefset=null;
		}
	}

	class GetConceptForLabel implements I_GetItemForModel{

		private String labelTarget;
		public GetConceptForLabel(String labelTarget){
			this.labelTarget=labelTarget;
		}
		@Override
		public Object getItemFromConcept(I_GetConceptData concept) throws Exception {
			if (labelTarget.equals(REFSET_LABEL_FOREXPORT)){
				label7.setText(concept.toString());
				sourceRefset=concept;
				return null;
			}
			return null;

		}

	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		label3 = new JLabel();
		label5 = new JLabel();
		scrollPane2 = new JScrollPane();
		list1 = new JList();
		scrollPane3 = new JScrollPane();
		list2 = new JList();
		label6 = new JLabel();
		txtRelDate = new JTextField();
		button1 = new JButton();
		label1 = new JLabel();
		txtFileExp = new JTextField();
		browsExpFile = new JButton();
		button2 = new JButton();
		label2 = new JLabel();
		txtExplogFile = new JTextField();
		browLogFile = new JButton();
		chkNotTrans = new JCheckBox();
		label7 = new JLabel();
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

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {15, 125, 0, 125, 15, 0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 90, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

			//---- label3 ----
			label3.setText("Included status");
			panel2.add(label3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label5 ----
			label5.setText("Excluded status");
			panel2.add(label5, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//======== scrollPane2 ========
			{
				scrollPane2.setViewportView(list1);
			}
			panel2.add(scrollPane2, new GridBagConstraints(1, 1, 1, 6, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//======== scrollPane3 ========
			{
				scrollPane3.setViewportView(list2);
			}
			panel2.add(scrollPane3, new GridBagConstraints(3, 1, 1, 6, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label6 ----
			label6.setText("Release date:");
			panel2.add(label6, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel2.add(txtRelDate, new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- button1 ----
			button1.setText(">");
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel2.add(button1, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label1 ----
			label1.setText("Export to folder:");
			panel2.add(label1, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel2.add(txtFileExp, new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0,
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
			panel2.add(browsExpFile, new GridBagConstraints(7, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- button2 ----
			button2.setText("<");
			button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			panel2.add(button2, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label2 ----
			label2.setText("Log file:");
			panel2.add(label2, new GridBagConstraints(5, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel2.add(txtExplogFile, new GridBagConstraints(6, 3, 1, 1, 0.0, 0.0,
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
			panel2.add(browLogFile, new GridBagConstraints(7, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- chkNotTrans ----
			chkNotTrans.setText("Include not translated concept with terms from:");
			chkNotTrans.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chkNotTransActionPerformed();
				}
			});
			panel2.add(chkNotTrans, new GridBagConstraints(1, 7, 4, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel2.add(label7, new GridBagConstraints(5, 7, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label4 ----
			label4.setText("Results:");
			panel2.add(label4, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- pBarE ----
			pBarE.setIndeterminate(true);
			panel2.add(pBarE, new GridBagConstraints(3, 8, 3, 1, 0.0, 0.0,
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
			panel2.add(bExport, new GridBagConstraints(6, 8, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//======== scrollPane1 ========
			{
				scrollPane1.setViewportView(txtERes);
			}
			panel2.add(scrollPane1, new GridBagConstraints(1, 9, 7, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel2;
	private JLabel label3;
	private JLabel label5;
	private JScrollPane scrollPane2;
	private JList list1;
	private JScrollPane scrollPane3;
	private JList list2;
	private JLabel label6;
	private JTextField txtRelDate;
	private JButton button1;
	private JLabel label1;
	private JTextField txtFileExp;
	private JButton browsExpFile;
	private JButton button2;
	private JLabel label2;
	private JTextField txtExplogFile;
	private JButton browLogFile;
	private JCheckBox chkNotTrans;
	private JLabel label7;
	private JLabel label4;
	private JProgressBar pBarE;
	private JButton bExport;
	private JScrollPane scrollPane1;
	private JTextPane txtERes;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
