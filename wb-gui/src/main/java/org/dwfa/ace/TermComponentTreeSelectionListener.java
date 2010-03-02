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
package org.dwfa.ace;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.tree.I_GetConceptDataForTree;

public class TermComponentTreeSelectionListener implements TreeSelectionListener {

    I_ContainTermComponent linkedComponent;

    public TermComponentTreeSelectionListener(I_ContainTermComponent linkedComponent) {
        super();
        this.linkedComponent = linkedComponent;
    }

    public void valueChanged(TreeSelectionEvent e) {
        handleChange(e);
    }

    private void handleChange(TreeSelectionEvent e) {
        I_GetConceptData currentBean = (I_GetConceptData) linkedComponent.getTermComponent();
		if (currentBean != null) {
		    setLinkedComponent(e);
		} else {
		    setLinkedComponent(e);
		}
    }

    private void setLinkedComponent(TreeSelectionEvent e) {
        Object obj = e.getPath().getLastPathComponent();
        I_GetConceptDataForTree cb;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
        cb = (I_GetConceptDataForTree) node.getUserObject();
        if (cb != null) {
            linkedComponent.setTermComponent(cb.getCoreBean());
        }
    }

}
