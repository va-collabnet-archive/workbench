/*
 * Created by JFormDesigner on Fri Jun 18 16:53:24 GMT-03:00 2010
 */

package org.ihtsdo.translation.ui.config;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.translation.ui.ConfigTranslationModule;

/**
 * @author Guillermo Reynoso
 */
public class SpellCheckerConfigPanel extends JPanel{

	private static final long serialVersionUID = 1653665739753572691L;
	private ConfigTranslationModule confTrans;
	final ButtonGroup group = new ButtonGroup();
	I_TermFactory tf = Terms.get();
	
	public SpellCheckerConfigPanel(I_ConfigAceFrame config, ConfigTranslationModule confTrans2){
		this.confTrans = confTrans2;
		initComponents();
		initCustomComponents();
		if(config == null || confTrans == null){
			error.setText("Problems initializing configuration see the logfile for more details");
		}
		setButtonsEnabled(false);
	}

	private void initCustomComponents() {
		
		this.setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
		configContainer.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setButtonsEnabled(true);
			}
		};
		
		spellChecker.addChangeListener(cl);
		
		
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				confTrans.setEnableSpellChecker(spellChecker.isSelected());
				setButtonsEnabled(false);
			}
		});
		
		revertButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectCurrentConfButton();
				setButtonsEnabled(false);
			}
		});
	}
	
	public void selectCurrentConfButton(){
		//inboxItemCheckbox.setEnabled(inboxItemConfigurationPermission);
		if(confTrans != null){
			spellChecker.setSelected(confTrans.isEnableSpellChecker());
		}else{
			spellChecker.setEnabled(false);
		}
			
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		configContainer = new JPanel();
		spellChecker = new JCheckBox();
		buttonContainer = new JPanel();
		applyButton = new JButton();
		revertButton = new JButton();
		errorContainer = new JPanel();
		error = new JLabel();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(5, 5));

		//======== configContainer ========
		{
			configContainer.setLayout(new GridBagLayout());
			((GridBagLayout)configContainer.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)configContainer.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
			((GridBagLayout)configContainer.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
			((GridBagLayout)configContainer.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

			//---- spellChecker ----
			spellChecker.setText("Enable spell checker");
			configContainer.add(spellChecker, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));
		}
		add(configContainer, BorderLayout.NORTH);

		//======== buttonContainer ========
		{
			buttonContainer.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));

			//---- applyButton ----
			applyButton.setText("Apply");
			applyButton.setHorizontalAlignment(SwingConstants.LEFT);
			buttonContainer.add(applyButton);

			//---- revertButton ----
			revertButton.setText("Cancel");
			buttonContainer.add(revertButton);
		}
		add(buttonContainer, BorderLayout.SOUTH);

		//======== errorContainer ========
		{
			errorContainer.setLayout(new BorderLayout(5, 5));

			//---- error ----
			error.setForeground(UIManager.getColor("Button.light"));
			errorContainer.add(error, BorderLayout.SOUTH);
		}
		add(errorContainer, BorderLayout.CENTER);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel configContainer;
	private JCheckBox spellChecker;
	private JPanel buttonContainer;
	private JButton applyButton;
	private JButton revertButton;
	private JPanel errorContainer;
	private JLabel error;
	// JFormDesigner - End of variables declaration //GEN-END:variables

	private void setButtonsEnabled(boolean b){
		applyButton.setEnabled(b);
		revertButton.setEnabled(b);
	}
	
}
