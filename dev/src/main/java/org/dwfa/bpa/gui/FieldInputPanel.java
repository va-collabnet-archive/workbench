/*
 * Created on Apr 19, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui;



import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dwfa.bpa.process.I_Workspace;


/**
 * @author kec
 *
 */
public class FieldInputPanel extends GridBagPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 3911170059445156359L;
    private String[] labels = new String[0];
    private String[] values = new String[0];
    private JTextField[] valueFields = new JTextField[0];
    private JButton cancel = new JButton("cancel");
    private JButton complete = new JButton("complete");
    private int columns = 1;
    /**
     * @param title
     */
    public FieldInputPanel(String title, I_Workspace workspace) {
        super(title, workspace);
        redoLayout();
    }
    
    public void setFields(int columns, String[] labels, String[] defaults) {
        if ((labels == null) || (defaults == null)) {
            throw new NullPointerException("parameters cannot be null");
        }
        this.columns = columns;
        this.labels = labels;
        this.values = defaults;
        this.valueFields = new JTextField[labels.length];
        redoLayout();
    }
    
    /**
     * 
     */
    private void redoLayout() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        
        for (int i = 0; i < this.labels.length; i++) {
            if (i % this.columns == 0) {
                c.gridy++;
                c.gridx = 0;
            } else {
                c.gridx++;
            }
            c.gridwidth = 1;
            JLabel label = new JLabel(this.labels[i], JLabel.RIGHT);
            label.setBorder(BorderFactory.createEmptyBorder(1,1,1,4));
            this.add(label, c);
            c.gridx++;
            this.valueFields[i] = new JTextField(this.values[i]);
            c.gridwidth = 2;
            this.add(this.valueFields[i], c);
        }
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridy++;
        this.add(new JPanel(), c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy++;
        c.gridx = 0;
        c.weighty = 0;
        c.weightx = 0;
        for (int i = 1; i < this.columns; i++) {
            c.gridwidth = 2;
            this.add(new JPanel(), c);
            c.gridx = c.gridx + 2;
        }
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridx++;
        this.cancel.setEnabled(false);
        this.add(this.cancel, c);
        c.gridx++;
        this.complete.setEnabled(false);
        this.add(this.complete, c);
        c.gridx++;
        this.add(new JLabel("   "), c);
    }

    public Map<String, String> getFields() {
        Map<String, String> fields = new HashMap<String, String>();
        for (int i = 0; i < labels.length; i++) {
            fields.put(labels[i], this.valueFields[i].getText());
        }
        return fields;
    }

    public void addCompleteActionListener(ActionListener l) {
        this.complete.addActionListener(l);
    }
    public void removeCompleteActionListener(ActionListener l) {
        this.complete.removeActionListener(l);
    }
    public void addCancelActionListener(ActionListener l) {
        this.cancel.addActionListener(l);
    }
    public void removeCancelActionListener(ActionListener l) {
        this.cancel.removeActionListener(l);
    }
    public void setCompleteEnabled(boolean enabled) {
        this.complete.setEnabled(enabled);   
    }
    public void setCancelEnabled(boolean enabled) {
        this.cancel.setEnabled(enabled);   
    }
    
    public void setCompleteLabel(String completeStr) {
        this.complete.setText(completeStr);
        
    }
}
