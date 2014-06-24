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
package org.ihtsdo.wizard;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

/**
 * The Class WizardFrame.
 *
 * @author Guillermo Reynoso
 */
public class WizardFrame extends JDialog {

    /**
     * The panels.
     */
    private I_fastWizard[] panels;
    /**
     * The index.
     */
    private int index;
    /**
     * The actual panel.
     */
    private I_fastWizard actualPanel;
    /**
     * The result.
     */
    private I_wizardResult result;
    /**
     * The map collector.
     */
    private HashMap<String, Object> mapCollector;

    /**
     * Instantiates a new wizard frame.
     *
     * @param panels the panels
     * @param result the result
     * @param notifier the notifier
     */
    public WizardFrame(I_fastWizard[] panels, I_wizardResult result, I_notifyPanelChange notifier) {
        initComponents();
        this.notifier = notifier;
        this.panels = panels;
        index = 0;
//		updateButtons();
        this.result = result;
        isNotifiying = false;
        this.mapCollector = new HashMap<String, Object>();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                setVisible(false);
                removeAll();
                dispose();
                System.gc();

            }
        });
    }

    /**
     * Adds the panels.
     *
     * @param addPanels the add panels
     */
    public void addPanels(I_fastWizard[] addPanels) {
        int newLen = panels.length + addPanels.length;
        I_fastWizard[] newPanels = new I_fastWizard[newLen];
        int cont = 0;
        for (int i = 0; i < panels.length; i++) {
            newPanels[cont] = panels[i];
            cont++;
        }
        for (int i = 0; i < addPanels.length; i++) {
            newPanels[cont] = addPanels[i];
            cont++;
        }
        panels = newPanels;
    }

    /**
     * Gets the panels.
     *
     * @return the panels
     */
    public I_fastWizard[] getPanels() {
        return panels;
    }

    /**
     * Sets the panels.
     *
     * @param panels the panels
     * @param index the index
     */
    public void setPanels(I_fastWizard[] panels, int index) {
        this.panels = panels;
        this.index = index;
        updatePanel();
    }

    /**
     * Sets the panels.
     *
     * @param panels the new panels
     */
    public void setPanels(I_fastWizard[] panels) {
        this.panels = panels;
        updatePanel();

    }

    /**
     * Update buttons.
     */
    private void updateButtons() {
        if (index == 0) {
            bback.setEnabled(false);
        } else {
            bback.setEnabled(true);
        }

        if (index == panels.length - 1) {
            bnext.setText("Finish");
        } else {
            bnext.setText("Next");
        }

    }

    /**
     * Bback action performed.
     */
    private void bbackActionPerformed() {
        index--;
        updatePanel();
    }

    /**
     * Bnext action performed.
     */
    private void bnextActionPerformed() {
        try {
            getMap();
        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (bnext.getText().equals("Finish")) {
            finish();
            return;
        }
        panel1.removeAll();
        index++;
        updatePanel();

    }

    /**
     * Finish.
     */
    private void finish() {
        result.setResultMap(mapCollector);
        this.setVisible(false);
        this.removeAll();
        this.dispose();
        System.gc();
    }

    /**
     * Gets the map.
     *
     * @return the map
     * @throws Exception the exception
     */
    private void getMap() throws Exception {
        mapCollector.putAll(actualPanel.getData());
    }

    /**
     * Inits the components.
     */
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel1 = new JPanel();
        bback = new JButton();
        bnext = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout) contentPane.getLayout()).columnWidths = new int[]{15, 0, 0, 0, 10, 0};
        ((GridBagLayout) contentPane.getLayout()).rowHeights = new int[]{15, 0, 0, 0};
        ((GridBagLayout) contentPane.getLayout()).columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout) contentPane.getLayout()).rowWeights = new double[]{0.0, 1.0, 0.0, 1.0E-4};

        //======== panel1 ========
        {
            panel1.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
            panel1.setLayout(new GridBagLayout());
            ((GridBagLayout) panel1.getLayout()).columnWidths = new int[]{0, 0};
            ((GridBagLayout) panel1.getLayout()).rowHeights = new int[]{0, 0};
            ((GridBagLayout) panel1.getLayout()).columnWeights = new double[]{1.0, 1.0E-4};
            ((GridBagLayout) panel1.getLayout()).rowWeights = new double[]{1.0, 1.0E-4};
        }
        contentPane.add(panel1, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

        //---- bback ----
        bback.setText("Back");
        bback.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bbackActionPerformed();
            }
        });
        contentPane.add(bback, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

        //---- bnext ----
        bnext.setText("Next");
        bnext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bnextActionPerformed();
            }
        });
        contentPane.add(bnext, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    /**
     * The panel1.
     */
    private JPanel panel1;
    /**
     * The bback.
     */
    private JButton bback;
    /**
     * The bnext.
     */
    private JButton bnext;
    /**
     * The notifier.
     */
    private I_notifyPanelChange notifier;
    /**
     * The is notifiying.
     */
    private boolean isNotifiying;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    /**
     * Sets the panel.
     *
     * @param i the new panel
     */
    public void setPanel(int i) {

        index = i;
        updatePanel();

    }

    /**
     * Update panel.
     */
    private void updatePanel() {
        panel1.removeAll();
        updateButtons();
        if (!isNotifiying) {
            isNotifiying = true;
            notifyLauncher();
            isNotifiying = false;
        }
        ((JPanel) panels[index]).revalidate();
        ((JPanel) panels[index]).validate();
        actualPanel = panels[index];
        panel1.add((JPanel) actualPanel, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));
        ((JPanel) actualPanel).repaint();
        panel1.revalidate();
        ((JPanel) actualPanel).revalidate();
        ((JPanel) actualPanel).validate();

        panel1.repaint();
        this.setSize(this.getWidth() - 1, this.getHeight() - 1);
        this.validate();
        this.setSize(this.getWidth() + 1, this.getHeight() + 1);

    }

    /**
     * Notify launcher.
     */
    private void notifyLauncher() {
        if (notifier != null) {
            notifier.notifyThis(this, index, mapCollector);
        }

    }

    /**
     * Gets the map collector.
     *
     * @return the map collector
     */
    public HashMap<String, Object> getMapCollector() {
        return mapCollector;
    }
}
