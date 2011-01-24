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
import java.io.IOException;
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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.TreeComponent;

/**
 * @author Guillermo Reynoso
 */
public class TargetTreeComponentsPanel extends JPanel {
	
	private static final long serialVersionUID = 2988632000328434848L;
	private ConfigTranslationModule confTrans;
	DefaultListModel currentList = new DefaultListModel();
	DefaultListModel avalableList = new DefaultListModel();
	I_TermFactory tf = Terms.get();
	//private boolean targetTreeComponentPermission;
	
	public TargetTreeComponentsPanel(I_ConfigAceFrame config, ConfigTranslationModule confTrans) {
		this.confTrans =  confTrans;
		initComponents();
		initCosutomComponents();
		if(config == null || confTrans == null){
			error.setText("Problems initializing configuration see the logfile for more details");
		}
		
	}
	
	private void initCosutomComponents() {
		
		configPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		initializeLists();
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TreeComponent selectedVal = (TreeComponent)avalableTreeComponents.getSelectedValue();
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
				TreeComponent selectedVal = (TreeComponent)currentTreeComponents.getSelectedValue();
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
						LinkedHashSet<TreeComponent> targetTreeComponents = new LinkedHashSet<TreeComponent>();
						Enumeration<TreeComponent> elements = (Enumeration<TreeComponent>) currentList
								.elements();
						while (elements.hasMoreElements()) {
							TreeComponent treeComponent = (TreeComponent) elements
									.nextElement();
							targetTreeComponents.add(treeComponent);
						}
						confTrans.setTargetTreeComponents(targetTreeComponents);
						setButtonsEnabled(false);
					} else {
						confTrans.setTargetTreeComponents(null);
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
		
			avalableTreeComponents.setModel(avalableList);
			currentTreeComponents.setModel(currentList);

			LinkedHashSet<TreeComponent> currentComponents = confTrans.getTargetTreeComponents();
			if(currentComponents != null && !currentComponents.isEmpty()){
				for (TreeComponent treeComponent : currentComponents) {
					currentList.addElement(treeComponent);
				}
			}
			
			TreeComponent[] avalableTreeComponents = TreeComponent.values();
			for (TreeComponent treeComponent : avalableTreeComponents) {
				if(currentComponents != null && !currentComponents.contains(treeComponent)){
					avalableList.addElement(treeComponent);
				}else if(currentComponents == null){
					avalableList.addElement(treeComponent);
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
		configPanel = new JPanel();
		label2 = new JLabel();
		label3 = new JLabel();
		scrollPane1 = new JScrollPane();
		avalableTreeComponents = new JList();
		scrollPane2 = new JScrollPane();
		currentTreeComponents = new JList();
		addButton = new JButton();
		removeButton = new JButton();
		errorContainer = new JPanel();
		error = new JLabel();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		//======== buttonPanel ========
		{
			buttonPanel.setBorder(null);
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));

			//---- applyButton ----
			applyButton.setText("Apply");
			buttonPanel.add(applyButton);

			//---- revertConfig ----
			revertConfig.setText("Cancel");
			buttonPanel.add(revertConfig);
		}
		add(buttonPanel, BorderLayout.SOUTH);

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
				scrollPane1.setViewportView(avalableTreeComponents);
			}
			configPanel.add(scrollPane1, new GridBagConstraints(0, 1, 1, 4, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//======== scrollPane2 ========
			{
				scrollPane2.setViewportView(currentTreeComponents);
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
		add(configPanel, BorderLayout.NORTH);

		//======== errorContainer ========
		{
			errorContainer.setLayout(new BorderLayout(5, 5));

			//---- error ----
			error.setForeground(UIManager.getColor("Button.light"));
			errorContainer.add(error, BorderLayout.SOUTH);
		}
		add(errorContainer, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel buttonPanel;
	private JButton applyButton;
	private JButton revertConfig;
	private JPanel configPanel;
	private JLabel label2;
	private JLabel label3;
	private JScrollPane scrollPane1;
	private JList avalableTreeComponents;
	private JScrollPane scrollPane2;
	private JList currentTreeComponents;
	private JButton addButton;
	private JButton removeButton;
	private JPanel errorContainer;
	private JLabel error;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	private void setButtonsEnabled(boolean b){
		applyButton.setEnabled(b);
		revertConfig.setEnabled(b);
	}
	
}
