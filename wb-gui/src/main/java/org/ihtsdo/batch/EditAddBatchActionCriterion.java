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
package org.ihtsdo.batch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dwfa.ace.log.AceLog;

public class EditAddBatchActionCriterion implements ActionListener {

   private final Z_I_MakeBatchActionCriterionPanel batchActionPanel; // :WAS: searchPanel

    /**
     * @param batchActionPanel
     */
    public EditAddBatchActionCriterion(Z_I_MakeBatchActionCriterionPanel batchActionPanel) {
        this.batchActionPanel = batchActionPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            batchActionPanel.getCriterionPanels().add(batchActionPanel.makeCriterionPanel());
        } catch (ClassNotFoundException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InstantiationException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IllegalAccessException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
        batchActionPanel.layoutCriterion();
    }

}
