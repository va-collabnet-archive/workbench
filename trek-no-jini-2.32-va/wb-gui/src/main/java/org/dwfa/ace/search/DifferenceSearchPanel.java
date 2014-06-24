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
package org.dwfa.ace.search;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.search.DifferenceConceptStatus;
import org.dwfa.ace.task.search.DifferenceFullySpecifiedName;
import org.dwfa.ace.task.search.DifferencePreferredName;
import org.dwfa.ace.task.search.DifferenceRelsDestination;
import org.dwfa.ace.task.search.DifferenceRelsSource;
import org.dwfa.ace.task.search.DifferenceRelsSourceOrDestination;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.ace.task.search.IsKindOf;
import org.dwfa.ace.task.search.RefsetMatch;

public class DifferenceSearchPanel extends JPanel implements I_MakeCriterionPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    List<CriterionPanel> criterionPanels = new ArrayList<CriterionPanel>();

    private JPanel criterion = new JPanel();

    private List<I_TestSearchResults> extraCriterion;

    public DifferenceSearchPanel(I_ConfigAceFrame config) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        super(new GridBagLayout());
        Border b = BorderFactory.createEmptyBorder(5, 5, 0, 0);
        setBorder(b);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;

        getCriterionPanels().add(makeCriterionPanel());
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 6;
        gbc.gridheight = 3;

        layoutCriterion();
        add(criterion, gbc);

        gbc.weighty = 1;
        gbc.gridy = gbc.gridy + gbc.gridheight;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        add(new JPanel(), gbc);

    }

    public void layoutCriterion() {
        criterion.removeAll();
        criterion.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0;

        for (JPanel criterionPanel : criterionPanels) {
            criterion.add(criterionPanel, gbc);
            criterionPanel.invalidate();
            criterionPanel.validate();
            criterionPanel.doLayout();
            gbc.gridy++;
        }
        criterion.invalidate();
        criterion.validate();
        criterion.doLayout();

        this.invalidate();
        this.validate();
        this.doLayout();
    }

    public CriterionPanel makeCriterionPanel() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        List<I_TestSearchResults> criterionOptions = new ArrayList<I_TestSearchResults>();
        criterionOptions.add(new DifferenceConceptStatus());
        criterionOptions.add(new DifferenceFullySpecifiedName());
        criterionOptions.add(new DifferencePreferredName());
        criterionOptions.add(new DifferenceRelsDestination());
        criterionOptions.add(new DifferenceRelsSource());
        criterionOptions.add(new DifferenceRelsSourceOrDestination());
        criterionOptions.add(new RefsetMatch());
        criterionOptions.add(new IsKindOf());
        return new CriterionPanel(this, null, criterionOptions);
    }

    public List<CriterionPanel> getCriterionPanels() {
        return criterionPanels;
    }

    public List<I_TestSearchResults> getCriterion() {
        List<I_TestSearchResults> results = new ArrayList<I_TestSearchResults>();
        for (CriterionPanel p : criterionPanels) {
            results.add(p.getBean());
        }
        return results;
    }

}
