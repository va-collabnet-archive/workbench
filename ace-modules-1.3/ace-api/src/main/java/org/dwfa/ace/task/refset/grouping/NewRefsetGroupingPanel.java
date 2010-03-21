/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.task.refset.grouping;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.spec.SpecRefsetHelper;
import org.dwfa.ace.task.util.DynamicWidthComboBox;
import org.dwfa.cement.RefsetAuxiliary;

/**
 * This panel allows the user to input data required for a new refset grouping concept. e.g. they wish to create a new
 * child of refset, called "Opthamology". This form will gather all the information required to do this.
 * 
 * @author Chrissy Hill
 * 
 */
public class NewRefsetGroupingPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // components
    private JLabel refsetParentLabel;
    private JLabel refsetNameLabel;
    private JTextArea refsetNameTextField;
    private JScrollPane refsetNameScrollPane;
    private DynamicWidthComboBox refsetParentComboBox;

    public NewRefsetGroupingPanel() {
        super();
        init();
    }

    private void init() {
        setDefaultValues();
        addListeners();
        layoutComponents();
    }

    private void setDefaultValues() {

        // labels
        refsetParentLabel = new JLabel("Refset grouping parent (required):");
        refsetNameLabel = new JLabel("Grouping name (required):");

        // combo box
        refsetParentComboBox = new DynamicWidthComboBox(getValidParents().toArray());

        // text field
        refsetNameTextField = new JTextArea();
        refsetNameTextField.setLineWrap(true);
        refsetNameTextField.setWrapStyleWord(true);
        refsetNameScrollPane = new JScrollPane(refsetNameTextField);
        refsetNameScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        refsetNameScrollPane.setMaximumSize(new Dimension(200, 50));
        refsetNameScrollPane.setMinimumSize(new Dimension(200, 50));
        refsetNameScrollPane.setPreferredSize(new Dimension(200, 50));
    }

    private void addListeners() {
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        // refset parent label and combo box
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(refsetParentLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        if (refsetParentComboBox.getItemCount() == 0) {
            this.add(new JLabel("No available parents."), gridBagConstraints);
        } else {
            this.add(refsetParentComboBox, gridBagConstraints);
        }

        // refset name label & text field
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(refsetNameLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(refsetNameScrollPane, gridBagConstraints);

        // filler
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        this.add(Box.createGlue(), gridBagConstraints);

        this.repaint();
        this.revalidate();

    }

    /**
     * Calculates a set of valid parents. Valid parents include the "refset" concept in the refset hierarchy as well as
     * its children.
     * 
     * @return The set of valid parents.
     */
    private Set<I_GetConceptData> getValidParents() {
        HashSet<I_GetConceptData> validParents = new HashSet<I_GetConceptData>();
        try {
            I_GetConceptData refset =
                    LocalVersionedTerminology.get().getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());

            I_IntSet allowedTypes = LocalVersionedTerminology.get().getActiveAceFrameConfig().getDestRelTypes();
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            Set<? extends I_GetConceptData> results =
                    refset.getDestRelOrigins(currentStatuses, allowedTypes, LocalVersionedTerminology.get()
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

            validParents.addAll(results);
            validParents.add(refset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return validParents;
    }

    public String getRefsetName() {
        String result = refsetNameTextField.getText();
        if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

    public I_GetConceptData getRefsetParent() {
        return (I_GetConceptData) refsetParentComboBox.getSelectedItem();
    }
}