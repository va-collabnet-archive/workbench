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
package org.dwfa.ace.checks;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;

public class UncommittedListRenderer implements ListCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        AlertToDataConstraintFailure failure = (AlertToDataConstraintFailure) value;
        if (failure.getRendererComponent() == null) {

            JLabel label = new JLabel();
            label.setText(failure.getAlertMessage());

            JPanel componentPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 1;
            c.gridwidth = 2;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 0;
            componentPanel.add(label, c);
            c.weightx = 0;
            c.gridwidth = 1;
            c.gridy++;
            c.anchor = GridBagConstraints.EAST;
            componentPanel.add(new JLabel("fixes: "), c);
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 1;
            c.gridx++;
            JComboBox testCombo = new JComboBox(new String[] { "fix 1", "fix 2" });
            componentPanel.add(testCombo, c);
            failure.setRendererComponent(componentPanel);

        }
        JComponent component = failure.getRendererComponent();
        component.setBorder(null);

        if (isSelected) {
            component.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 1, 1, 1,
                Color.BLUE), BorderFactory.createEmptyBorder(1, 0, 0, 0)));
        } else {
            component.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                Color.BLACK), BorderFactory.createEmptyBorder(1, 1, 0, 1)));
        }
        component.toString();
        return failure.getRendererComponent();
    }

}
