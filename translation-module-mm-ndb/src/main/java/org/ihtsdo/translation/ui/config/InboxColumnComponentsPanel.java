/*
 * Created by JFormDesigner on Tue Jun 29 16:56:16 GMT-03:00 2010
 */

package org.ihtsdo.translation.ui.config;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.LinkedHashSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;

/**
 * @author Guillermo Reynoso
 */
public class InboxColumnComponentsPanel extends JPanel {

	private static final long serialVersionUID = -1901943679008508361L;
	private ConfigTranslationModule confTrans;
	DefaultListModel currentList = new DefaultListModel();
	DefaultListModel avalableList = new DefaultListModel();
	I_TermFactory tf = Terms.get();
	//private boolean inboxColumnComponentPermission;
	
	public InboxColumnComponentsPanel(I_ConfigAceFrame config, ConfigTranslationModule confTrans2) {
		initComponents();
		initCosutomComponents();
		initializeLists();
		this.confTrans = confTrans2;
		if(confTrans == null || config == null){
			error.setText("Problems initializing configuration see the logfile for more details");
		}
//		
//		ProjectPermissionsAPI permissionApi = new ProjectPermissionsAPI(config);
//		
//		try {
//			inboxColumnComponentPermission = permissionApi.checkPermissionForProject(
//					config.getDbConfig().getUserConcept(), 
//					tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
//					tf.getConcept(ArchitectonicAuxiliary.Concept.MODIFY_INBOX_COLUMNS_DISPLAY_PERMISSION.localize().getNid()));
//			if(!inboxColumnComponentPermission){
//				SwingUtils.disabledAllComponents(this);
//			}
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (TerminologyException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	}

	private void initCosutomComponents() {
		
		configPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		initializeLists();
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				InboxColumn selectedVal = (InboxColumn)avalableInboxColumnComponents.getSelectedValue();
				if(selectedVal != null){
					avalableList.removeElement(selectedVal);
					//If the available list is empty disable the add button
					if(avalableList.isEmpty()){
						addButton.setEnabled(false);
					}
					//If remove button was disabled, enable it.
					if(!removeButton.isEnabled()){
						removeButton.setEnabled(true);
					}
					currentList.addElement(selectedVal);
					setButtonsEnabled(true);
				}
			}
		});
		
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				InboxColumn selectedVal = (InboxColumn)currentInboxColumnComponents.getSelectedValue();
				if(selectedVal != null){
					currentList.removeElement(selectedVal);
					if(currentList.isEmpty()){
						removeButton.setEnabled(false);
					}
					if(!addButton.isEnabled()){
						addButton.setEnabled(true);
					}
					avalableList.addElement(selectedVal);
					setButtonsEnabled(true);
				}
				
			}
		});
		
		setButtonsEnabled(false);
		
		applyButton.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (!currentList.isEmpty()) {
						LinkedHashSet<InboxColumn> inboxColumnComponents = new LinkedHashSet<InboxColumn>();
						Enumeration<InboxColumn> elements = (Enumeration<InboxColumn>) currentList.elements();
						while (elements.hasMoreElements()) {
							InboxColumn treeComponent = (InboxColumn) elements.nextElement();
							inboxColumnComponents.add(treeComponent);
						}
						confTrans.setColumnsDisplayedInInbox(inboxColumnComponents);
						setButtonsEnabled(false);
					} else {
						confTrans.setColumnsDisplayedInInbox(null);
					}
				} catch (Exception e) {
					initializeLists();
					error.setText("Could not save the configuration, please contact your administrator.");
					e.printStackTrace();
				}
			}
		});
		revertConfig.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				initializeLists();
				setButtonsEnabled(false);
			}
		});
	}
	
	public void initializeLists() {
		if(confTrans != null){
			currentList = new DefaultListModel();
			avalableList = new DefaultListModel();
			
			currentInboxColumnComponents.setModel(currentList);
			avalableInboxColumnComponents.setModel(avalableList);
			
			LinkedHashSet<InboxColumn> currentComponents = confTrans.getColumnsDisplayedInInbox();
			if(currentComponents != null && !currentComponents.isEmpty()){
				for (InboxColumn treeComponent : currentComponents) {
					currentList.addElement(treeComponent);
				}
			}
			
			InboxColumn[] avalableInboxColComponents = InboxColumn.values();
			for (InboxColumn inboxColumnComponent : avalableInboxColComponents) {
				if(currentComponents != null && !currentComponents.contains(inboxColumnComponent)){
					avalableList.addElement(inboxColumnComponent);
				}else if(currentComponents == null){
					avalableList.addElement(inboxColumnComponent);
				}
			}
		}
		addButton.setEnabled(!avalableList.isEmpty());
		removeButton.setEnabled(!currentList.isEmpty());
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		buttonPanel = new JPanel();
		applyButton = new JButton();
		revertConfig = new JButton();
		errorContainer = new JPanel();
		error = new JLabel();
		panel1 = new JPanel();
		label1 = new JLabel();
		configPanel = new JPanel();
		label2 = new JLabel();
		label3 = new JLabel();
		scrollPane1 = new JScrollPane();
		avalableInboxColumnComponents = new JList();
		scrollPane2 = new JScrollPane();
		currentInboxColumnComponents = new JList();
		addButton = new JButton();
		removeButton = new JButton();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		//======== buttonPanel ========
		{
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));

			//---- applyButton ----
			applyButton.setText("Apply");
			buttonPanel.add(applyButton);

			//---- revertConfig ----
			revertConfig.setText("Cancel");
			buttonPanel.add(revertConfig);
		}
		add(buttonPanel, BorderLayout.SOUTH);

		//======== errorContainer ========
		{
			errorContainer.setLayout(new BorderLayout(5, 5));

			//---- error ----
			error.setForeground(UIManager.getColor("Button.light"));
			errorContainer.add(error, BorderLayout.SOUTH);
		}
		add(errorContainer, BorderLayout.CENTER);

		//======== panel1 ========
		{
			panel1.setLayout(new BorderLayout(5, 20));

			//---- label1 ----
			label1.setText("Source FSN is always used as First Column");
			panel1.add(label1, BorderLayout.NORTH);

			//======== configPanel ========
			{
				configPanel.setLayout(new GridBagLayout());
				((GridBagLayout)configPanel.getLayout()).columnWidths = new int[] {160, 47, 155, 0};
				((GridBagLayout)configPanel.getLayout()).rowHeights = new int[] {0, 35, 10, 30, 38, 0};
				((GridBagLayout)configPanel.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0, 1.0E-4};
				((GridBagLayout)configPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

				//---- label2 ----
				label2.setText("Available components");
				configPanel.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label3 ----
				label3.setText("Current components");
				configPanel.add(label3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== scrollPane1 ========
				{
					scrollPane1.setViewportView(avalableInboxColumnComponents);
				}
				configPanel.add(scrollPane1, new GridBagConstraints(0, 1, 1, 4, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== scrollPane2 ========
				{
					scrollPane2.setViewportView(currentInboxColumnComponents);
				}
				configPanel.add(scrollPane2, new GridBagConstraints(2, 1, 1, 4, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));

				//---- addButton ----
				addButton.setText(">");
				configPanel.add(addButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- removeButton ----
				removeButton.setText("<");
				configPanel.add(removeButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 5), 0, 0));
			}
			panel1.add(configPanel, BorderLayout.CENTER);
		}
		add(panel1, BorderLayout.NORTH);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel buttonPanel;
	private JButton applyButton;
	private JButton revertConfig;
	private JPanel errorContainer;
	private JLabel error;
	private JPanel panel1;
	private JLabel label1;
	private JPanel configPanel;
	private JLabel label2;
	private JLabel label3;
	private JScrollPane scrollPane1;
	private JList avalableInboxColumnComponents;
	private JScrollPane scrollPane2;
	private JList currentInboxColumnComponents;
	private JButton addButton;
	private JButton removeButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	private void setButtonsEnabled(boolean b){
		applyButton.setEnabled(b);
		revertConfig.setEnabled(b);
	}
}
