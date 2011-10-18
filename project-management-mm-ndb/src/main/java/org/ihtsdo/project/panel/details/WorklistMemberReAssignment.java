/*
 * Created by JFormDesigner on Wed Aug 25 14:43:06 GMT-03:00 2010
 */

package org.ihtsdo.project.panel.details;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.ProcessID;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.project.FileLink;
import org.ihtsdo.project.FileLinkAPI;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.TranslationHelperPanel;

/**
 * @author Guillermo Reynoso
 */
public class WorklistMemberReAssignment extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String DELETE_OPTION = "Delete from queue";
	private I_ConfigAceFrame config;
	private I_Work worker;
	private HashMap<UUID, String> contract;
	private String assignedStat;
	private WorkList workList;
	public WorklistMemberReAssignment() {
	}

	public WorklistMemberReAssignment(WorkList workList,I_ConfigAceFrame config, I_Work worker) {
		initComponents();
		this.workList=workList;
		try{
			I_TermFactory tf = Terms.get();
			this.config=config;
			this.worker=worker;
			
			ProjectPermissionsAPI permissionApi = new ProjectPermissionsAPI(
					config);

			pBarW.setVisible(false);
			DefaultTableModel model = getMembersTableModel();
			assignedStat=tf.getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids()).toString();
			getMemberList(workList, model);
			membersTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			
			membersTable.setAutoCreateRowSorter(true);
			
			comboBox1.addItem(DELETE_OPTION);
			for (String address : config.getAddressesList()) {
				if (address.trim().endsWith(".inbox")) {
					comboBox1.addItem(address);
				}
			}

			comboBox1.setSelectedItem(workList.getDestination());

			FileLinkAPI flApi = new FileLinkAPI(config);
			FileLink link1 = new FileLink(new File("sampleProcesses/WrlstMemberReassign.bp"), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_QUEUE_UTILS_CATEGORY.getUids()));
			flApi.putLinkInConfig(link1);

			List<FileLink> links = flApi.getLinksForCategory(tf.getConcept(
					ArchitectonicAuxiliary.Concept.TRANSLATION_QUEUE_UTILS_CATEGORY.getUids()));
			
			for (FileLink fLink:links){
				comboBox2.addItem(fLink);
			}
			DefaultTableModel model2= new DefaultTableModel();
			model2.addColumn("WorkList member");
			model2.addColumn("Status");

			membersTable2.setModel(model2);

			membersTable2.setDefaultEditor( model2.getColumnClass(0),null);
			membersTable2.setDefaultEditor( model2.getColumnClass(1),null);
			membersTable2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			membersTable2.setAutoCreateRowSorter(true);
			
			boolean canReassign = permissionApi
			.checkPermissionForProject(
					config.getDbConfig().getUserConcept(),
					tf
					.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY
							.localize().getNid()),
							tf
							.getConcept(ArchitectonicAuxiliary.Concept.REASSINGNMENTS_PERMISSION
									.localize().getNid()));
			
			if (canReassign) {
				comboBox1.setEnabled(true);
				comboBox2.setEnabled(true);
				button3.setEnabled(true);
				bAdd.setEnabled(true);
				bDel.setEnabled(true);
			} else {
				comboBox1.setEnabled(false);
				comboBox2.setEnabled(false);
				button3.setEnabled(false);
				bAdd.setEnabled(false);
				bDel.setEnabled(false);
			}
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private DefaultTableModel getMembersTableModel() {
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("WorkList member");
		model.addColumn("Status");
		membersTable.setModel(model);
		membersTable.setDefaultEditor( model.getColumnClass(0),null);
		membersTable.setDefaultEditor( model.getColumnClass(1),null);
		return model;
	}

	private void getMemberList(WorkList workList, DefaultTableModel model) throws TerminologyException, IOException {
		
		List<WorkListMember> members = workList.getWorkListMembers();
		Collections.sort(members, new Comparator<WorkListMember>() {
			public int compare(WorkListMember f1, WorkListMember f2) {
				return f1.toString().compareTo(f2.toString());
			}
		});
		for (WorkListMember member : members) {
			I_GetConceptData activityStatus = Terms.get().getConcept(member
					.getActivityStatus());
			model.addRow(new Object[]{member,activityStatus.toString()});
		}
	}

	private void button3ActionPerformed() {
		sendWorklistMembers((String) comboBox1.getSelectedItem());
	}
	
	private void sendWorklistMembers(String userAddress) {
		DefaultTableModel model=(DefaultTableModel) membersTable2.getModel();
		if (model.getRowCount()>0){
			pBarW.setVisible(true);
			String destination;
			if (userAddress.equals(DELETE_OPTION)){
				destination=null;
			}
			else{
				destination=userAddress;
			}
			contract=new HashMap<UUID,String>();
			for (int i=0;i<model.getRowCount();i++){
				UUID memberUUID = ((WorkListMember)model.getValueAt(i,0)).getUids().iterator().next();
				contract.put(memberUUID,destination );
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					FileLink selectedBPFile = (FileLink) comboBox2.getSelectedItem();
					BusinessProcess selectedProcess = null;
					String finalDest=(String)comboBox1.getSelectedItem();
					try {
						selectedProcess = TerminologyProjectDAO
						.getBusinessProcess(selectedBPFile.getFile());
						UUID contractUuid=UUID.randomUUID();
						selectedProcess.writeAttachment(ProcessAttachmentKeys.QUEUE_UTIL_CONTRACT_UUID.getAttachmentKey(), contractUuid);
						selectedProcess.writeAttachment(ProcessAttachmentKeys.QUEUE_UTIL_CONTRACT.getAttachmentKey(), contract);
						selectedProcess.setSubject(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW);
						
						String outboxQueueName = config.getUsername() + ".outbox";
			    		ServiceID serviceID = null;
			    		Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
			    		Entry[] attrSetTemplates = new Entry[] { new Name(outboxQueueName) };
			    		ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
			    		ServiceItemFilter filter = null;

			    		ServiceItem service = worker.lookup(template, filter);
			    		if (service == null) {
			    			throw new TaskFailedException("No queue with the specified address could be found: "
			    					+ outboxQueueName);
			    		}
			    		I_QueueProcesses q = (I_QueueProcesses) service.service;

						for (String address : config.getAddressesList()) {
							if (address.trim().endsWith(".inbox") && !address.equals(finalDest)) {
								
								selectedProcess.setDestination(address) ;
								selectedProcess.setProcessID(new ProcessID(UUID.randomUUID()));
								System.out.println(
					    				"Moving process " + selectedProcess.getProcessID() + " to outbox: " + outboxQueueName);
					    	
								q.write(selectedProcess, worker.getActiveTransaction());
								worker.commitTransactionIfActive();
					    		System.out.println("Moved process " + selectedProcess.getProcessID() + " to queue: " + outboxQueueName);

							}
						}

						pBarW.setVisible(false);
						JOptionPane.showMessageDialog(
								WorklistMemberReAssignment.this,
								"Worklist members sent!", "Message",
								JOptionPane.INFORMATION_MESSAGE);

						//			worker.execute(process);
					} catch (Exception e) {
						// error getting the workflow
						pBarW.setVisible(false);
						e.printStackTrace();
						JOptionPane.showMessageDialog(WorklistMemberReAssignment.this, e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			pBarW.setVisible(false);
			
		}
	}

	private void bAddActionPerformed() {
		addMembersToTargetTable();
	}

	private void addMembersToTargetTable() {
		int[] sRows=membersTable.getSelectedRows();
		DefaultTableModel model = (DefaultTableModel)membersTable.getModel();
		DefaultTableModel model2 = (DefaultTableModel)membersTable2.getModel();
		for (int i=sRows.length-1;i>-1;i--){
			int rowModel=membersTable.convertRowIndexToModel(sRows[i]);
			String status = (String)model.getValueAt(rowModel, 1);
			if (status.equals(assignedStat)){

				JOptionPane.showMessageDialog(WorklistMemberReAssignment.this,
						"Members with status "  + status  + " cannot be reassigned." , "",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		for (int i=sRows.length-1;i>-1;i--){
			int rowModel=membersTable.convertRowIndexToModel(sRows[i]);
			WorkListMember member = (WorkListMember)model.getValueAt(rowModel, 0);
			String status = (String)model.getValueAt(rowModel, 1);
			
			model2.addRow(new Object[]{member,status});
			model.removeRow(rowModel);
			membersTable.validate();
			
		}
		
	}

	private void bDelActionPerformed() {
		delMembersFromTargetTable();
	}

	private void delMembersFromTargetTable() {
		int[] sRows=membersTable2.getSelectedRows();
		DefaultTableModel model = (DefaultTableModel)membersTable.getModel();
		DefaultTableModel model2 = (DefaultTableModel)membersTable2.getModel();
		for (int i=sRows.length-1;i>-1;i--){
			int rowModel=membersTable2.convertRowIndexToModel(sRows[i]);
			WorkListMember member = (WorkListMember)model2.getValueAt(rowModel, 0);
			String status = (String)model2.getValueAt(rowModel, 1);
			
			model.addRow(new Object[]{member,status});
			model2.removeRow(rowModel);
			membersTable2.validate();
			
		}
		
	}

	private void button1ActionPerformed(ActionEvent e) {

		DefaultTableModel model = getMembersTableModel();
		
		try {
			getMemberList(workList, model);
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label9 = new JLabel();
		label10 = new JLabel();
		membersTableScrollPanel = new JScrollPane();
		membersTable = new JTable();
		panel1 = new JPanel();
		bAdd = new JButton();
		bDel = new JButton();
		membersTableScrollPanel2 = new JScrollPane();
		membersTable2 = new JTable();
		panel2 = new JPanel();
		button1 = new JButton();
		comboBox2 = new JComboBox();
		label8 = new JLabel();
		comboBox1 = new JComboBox();
		button3 = new JButton();
		pBarW = new JProgressBar();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 1.0E-4};

		//---- label9 ----
		label9.setText("WorkList members");
		add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label10 ----
		label10.setText("WorkList members to re-assign");
		add(label10, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== membersTableScrollPanel ========
		{
			membersTableScrollPanel.setViewportView(membersTable);
		}
		add(membersTableScrollPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

			//---- bAdd ----
			bAdd.setText(">");
			bAdd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bAddActionPerformed();
				}
			});
			panel1.add(bAdd, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- bDel ----
			bDel.setText("<");
			bDel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bDelActionPerformed();
				}
			});
			panel1.add(bDel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== membersTableScrollPanel2 ========
		{
			membersTableScrollPanel2.setViewportView(membersTable2);
		}
		add(membersTableScrollPanel2, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button1 ----
			button1.setText("Refresh");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel2.add(button1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel2.add(comboBox2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label8 ----
			label8.setText("Destination:");
			panel2.add(label8, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel2.add(comboBox1, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button3 ----
			button3.setText("Send");
			button3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed();
				}
			});
			panel2.add(button3, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 5, 0), 0, 0));

		//---- pBarW ----
		pBarW.setIndeterminate(true);
		add(pBarW, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label9;
	private JLabel label10;
	private JScrollPane membersTableScrollPanel;
	private JTable membersTable;
	private JPanel panel1;
	private JButton bAdd;
	private JButton bDel;
	private JScrollPane membersTableScrollPanel2;
	private JTable membersTable2;
	private JPanel panel2;
	private JButton button1;
	private JComboBox comboBox2;
	private JLabel label8;
	private JComboBox comboBox1;
	private JButton button3;
	private JProgressBar pBarW;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
