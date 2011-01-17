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

import org.dwfa.ace.log.AceLog;

public class AddCriterion implements ActionListener {

    /**
     * 
     */
    private final I_MakeCriterionPanel searchPanel;

    /**
     * @param searchPanel
     */
    public AddCriterion(I_MakeCriterionPanel searchPanel) {
        this.searchPanel = searchPanel;
    }

    public void actionPerformed(ActionEvent e) {
        try {
        	if (SearchPanel.class.isInstance(searchPanel))
            searchPanel.getCriterionPanels().add(searchPanel.makeCriterionPanel());
        	else
        	{
        		WorkflowHistoryCriterionPanel panel = ((MySearchPanel)searchPanel).makeCriterionPanel();
        		((MySearchPanel)searchPanel).getWorkflowHistoryCriterionPanels().add(panel);
        	}
        } catch (ClassNotFoundException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InstantiationException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IllegalAccessException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
        searchPanel.layoutCriterion();
    }

}
