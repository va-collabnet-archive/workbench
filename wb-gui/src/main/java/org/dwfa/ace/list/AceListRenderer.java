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
package org.dwfa.ace.list;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.log.AceLog;

public class AceListRenderer extends DefaultListCellRenderer {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private I_ConfigAceFrame config;

    public AceListRenderer(I_ConfigAceFrame config) {
        super();
        this.config = config;
    }

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index,
            boolean isSelected, boolean cellHasFocus) {
        JLabel renderComponent = (JLabel) super.getListCellRendererComponent(list, "X", index, isSelected, cellHasFocus);

        if (value != null) {
            if (I_GetConceptData.class.isAssignableFrom(value.getClass())) {
                try {
                    I_GetConceptData concept = (I_GetConceptData) value;
                    boolean uncommitted = concept.isUncommitted();
                    
                    I_DescriptionTuple desc = concept.getDescTuple(config.getShortLabelDescPreferenceList(), config);
                    String text;
                    if (desc == null) {
                    	text = concept.getInitialText();
                    } else {
                    	text = desc.getText();
                    }
                    if (text != null) {
                    	if (uncommitted) {
                            renderComponent.setText(text);
                            renderComponent.setOpaque(true);
                            if (!isSelected) {
                                renderComponent.setBackground(Color.YELLOW);
                            } else {
                            	Border b = renderComponent.getBorder();
                            	if (b == null) {
                            		b = BorderFactory.createEmptyBorder();
                            	}
                        		renderComponent.setBorder(BorderFactory.createCompoundBorder(
                        				BorderFactory.createMatteBorder(0, 6, 0, 6, Color.YELLOW), b));
                            }
                    	} else {
                            renderComponent.setText(text);
                            renderComponent.setOpaque(true);
                            if (!isSelected) {
                                renderComponent.setBackground(Color.WHITE);
                            }
                    	}
                    } else {
                        AceLog.getAppLog().info("element: " + index + " descTuple is null: " + concept.getConceptNid());
                        renderComponent.setText("removing concept with null descTuple: " + concept.getConceptNid());
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                I_GetConceptData concept = (I_GetConceptData) value;
                                I_ModelTerminologyList listModel = (I_ModelTerminologyList) list.getModel();
                                if (index < list.getModel().getSize()) {
                                    I_GetConceptData another = listModel.getElementAt(index);
                                    if (another != null && another.getConceptNid() == concept.getConceptNid()) {
                                        listModel.removeElement(index);
                                        AceLog.getAppLog().info(
                                            "element " + another + " with index " + index + " removed.");
                                    }
                                } else {
                                    AceLog.getAppLog().info(
                                            "element " + value + " with index " + index + 
                                            " >= list model size: " + list.getModel().getSize());
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    this.setText(e.getMessage());
                    AceLog.getAppLog().alertAndLogException(e);
                }
            } else {
                renderComponent.setText(value.toString());
            }
        } else {
            renderComponent.setText("<html><font color=red>Empty");
        }

        return renderComponent;
    }

}
