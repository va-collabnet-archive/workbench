/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.task;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * The Class SetDeliveryOrSelfAssignPanel.
 *
 * @author Guillermo Reynoso
 */
public class SetDeliveryOrSelfAssignPanel extends JPanel {

    /**
     * Instantiates a new sets the delivery or self assign panel.
     */
    public SetDeliveryOrSelfAssignPanel() {
        initComponents();
    }

    /**
     * Checks if is delivery.
     *
     * @return true, if is delivery
     */
    public boolean isDelivery() {
        return bDeli.isSelected();
    }

    /**
     * Checks if is self assign.
     *
     * @return true, if is self assign
     */
    public boolean isSelfAssign() {
        return bSelf.isSelected();
    }

    /**
     * Inits the components.
     */
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        bDeli = new JRadioButton();
        bSelf = new JRadioButton();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout) getLayout()).columnWidths = new int[]{20, 0, 15, 0};
        ((GridBagLayout) getLayout()).rowHeights = new int[]{20, 0, 0, 15, 0};
        ((GridBagLayout) getLayout()).columnWeights = new double[]{0.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout) getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0E-4};

        //---- bDeli ----
        bDeli.setText("Deliver to workflow");
        bDeli.setSelected(true);
        add(bDeli, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

        //---- bSelf ----
        bSelf.setText("Execute process now");
        add(bSelf, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(bDeli);
        buttonGroup1.add(bSelf);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    /**
     * The b deli.
     */
    private JRadioButton bDeli;
    /**
     * The b self.
     */
    private JRadioButton bSelf;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
