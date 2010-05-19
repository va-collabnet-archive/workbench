package org.dwfa.ace;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.config.AceFrameConfig;

public class ChangeSetConfigPanel extends JPanel implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private I_ConfigAceDb dbConfig;
    private JComboBox userPolicy;
    private JComboBox classifierPolicy;
    private JComboBox refsetPolicy;
    private JComboBox writerPerformance;

    public ChangeSetConfigPanel(AceFrameConfig aceFrameConfig) {
        super(new GridBagLayout());
        this.dbConfig = aceFrameConfig.getDbConfig();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridy = 0;
        userPolicy = setupCombo(gbc, "user changes: ", ChangeSetPolicy.values(), this, dbConfig.getUserChangesChangeSetPolicy());
        classifierPolicy = setupCombo(gbc, "classifier changes: ", ChangeSetPolicy.values(), this, dbConfig.getClassifierChangesChangeSetPolicy());
        refsetPolicy = setupCombo(gbc, "refset computer changes: ", ChangeSetPolicy.values(), this, dbConfig.getRefsetChangesChangeSetPolicy());
        writerPerformance = setupCombo(gbc, "writer performance: ", ChangeSetWriterThreading.values(), this, dbConfig.getChangeSetWriterThreading());
        
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        add(new JPanel(), gbc);
        
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    }

    private JComboBox setupCombo(GridBagConstraints gbc, String labelStr, Object[] comboValues, ActionListener action, Object selection) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(labelStr);
        this.add(label, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx++;
        gbc.weightx = 1;
        JComboBox combo = new JComboBox(comboValues);
        this.add(combo, gbc);
        combo.setSelectedItem(selection);
        combo.addActionListener(action);
        gbc.gridy++;
        return combo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == userPolicy) {
            dbConfig.setUserChangesChangeSetPolicy((ChangeSetPolicy) userPolicy.getSelectedItem());
            return;
        }
        if (e.getSource() == classifierPolicy) {
            dbConfig.setClassifierChangesChangeSetPolicy((ChangeSetPolicy) classifierPolicy.getSelectedItem());
            return;
        }
        if (e.getSource() == refsetPolicy) {
            dbConfig.setRefsetChangesChangeSetPolicy((ChangeSetPolicy) refsetPolicy.getSelectedItem());
            return;
        }
        if (e.getSource() == writerPerformance) {
            dbConfig.setChangeSetWriterThreading((ChangeSetWriterThreading) writerPerformance.getSelectedItem());
            return;
        }   
    }
}
