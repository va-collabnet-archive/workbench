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
/**
 * 
 */
package org.dwfa.ace.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

public class RemoveCriterion implements ActionListener {

    /**
     * 
     */
    private final I_MakeCriterionPanel searchPanel;
    JPanel criterionPanel;

    RemoveCriterion(I_MakeCriterionPanel searchPanel, JPanel criterionPanel) {
        super();
        this.searchPanel = searchPanel;
        this.criterionPanel = criterionPanel;
    }

    public void actionPerformed(ActionEvent e) {
    	if (WorkflowHistorySearchPanel.class.isInstance(this.searchPanel))
    	{
    		if (((WorkflowHistorySearchPanel)this.searchPanel).getWorkflowHistoryCriterionPanels().size() > 1)
    			((WorkflowHistorySearchPanel)this.searchPanel).getWorkflowHistoryCriterionPanels().remove(criterionPanel);
    	} else
        this.searchPanel.getCriterionPanels().remove(criterionPanel);
        this.searchPanel.layoutCriterion();
    }

}
