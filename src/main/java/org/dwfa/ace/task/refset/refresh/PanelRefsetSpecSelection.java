package org.dwfa.ace.task.refset.refresh;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_GetConceptData;

/**
 * The panel that allows user to input:
 * 1) refset name (from pulldown menu)
 * 
 * @author Perry Reid
 * 
 */
public class PanelRefsetSpecSelection extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // components
    private JLabel refsetNameLabel;
    private JComboBox refsetNameComboBox;
    private Set<I_GetConceptData> refsets;
    private JPanel wfPanel;

    public PanelRefsetSpecSelection(Set<I_GetConceptData> refsets, JPanel wfPanel) {
        super();
        this.wfPanel = wfPanel;
        this.refsets = refsets;
        init();
    }

    private void init() {
        setDefaultValues();
        addListeners();
        layoutComponents();
    }

    private void setDefaultValues() {

        // labels
        refsetNameLabel = new JLabel("Refset name (required):");

        // buttons and boxes
        refsetNameComboBox = new JComboBox(refsets.toArray());

    }

    private void addListeners() {
        refsetNameComboBox.addActionListener(new RefsetListener());
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        // refset name label & box
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(refsetNameLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        if (refsets.size() == 0) {
            this.add(new JLabel("No available refsets."), gridBagConstraints);
        } else {
            this.add(refsetNameComboBox, gridBagConstraints);
        }



//         // column filler
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 5;
//        gridBagConstraints.gridy = 7 + fileCount;
//        gridBagConstraints.weighty = 1.0;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
//        this.add(Box.createGlue(), gridBagConstraints);

//        this.setPreferredSize(new Dimension(0, 0));
//        this.setMaximumSize(new Dimension(0, 0));
//        this.setMinimumSize(new Dimension(0, 0));
//        this.revalidate();
//
//        this.setPreferredSize(null);
//        this.setMaximumSize(null);
//        this.setMinimumSize(null);
//        this.repaint();
//
//        wfPanel.setPreferredSize(new Dimension(0, 0));
//        wfPanel.setMaximumSize(new Dimension(0, 0));
//        wfPanel.setMinimumSize(new Dimension(0, 0));
//
//        wfPanel.setPreferredSize(null);
//        wfPanel.setMaximumSize(null);
//        wfPanel.setMinimumSize(null);
//        wfPanel.repaint();

    }

    
    
    
    class RefsetListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            layoutComponents();
        }
    }



//    public String getComments() {
//        String result = commentsTextField.getText();
//        if (result == null) {
//            return null;
//        } else if (result.trim().equals("")) {
//            return null;
//        } else {
//            return result;
//        }
//    }

 
    public I_GetConceptData getRefset() {
        if (refsets.size() == 0) {
            return null;
        } else {
            return (I_GetConceptData) refsetNameComboBox.getSelectedItem();
        }
    }

 }