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
package org.dwfa.ace.edit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.log.AceLog;

public abstract class AddComponent implements ActionListener {
    private I_ContainTermComponent termContainer;
    private I_ConfigAceFrame config;

    public AddComponent(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
        super();
        this.termContainer = termContainer;
        this.config = config;
    }

    public final void actionPerformed(ActionEvent e) {
        try {
            if (termContainer.getConfig().getEditingPathSet().size() == 0) {
                JOptionPane.showMessageDialog(new JFrame(), "You must select an editing path before editing...");
                return;
            }
            doEdit(termContainer, e, config);
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    protected abstract void doEdit(I_ContainTermComponent termContainer, ActionEvent e, I_ConfigAceFrame config)
            throws Exception;

}
