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
/*
 * Created on Mar 10, 2005
 */
package org.dwfa.bpa.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dwfa.bpa.gui.GridBagPanel.GridBagPanelConstraints;

/**
 * @author kec
 * 
 */
public class ChangePanelPropertiesPanel extends JPanel implements ActionListener, ChangeListener {
    /**
     * 
     */
    private static final long serialVersionUID = -1591986134080579161L;

    private JLabel label = new JLabel(" ");

    private GridBagPanel panel;

    private JCheckBox display = new JCheckBox("display");

    private JSpinner weightxSpinner;

    private JSpinner weightySpinner;

    private JSpinner xSpinner;

    private JSpinner ySpinner;

    private JSpinner heightSpinner;

    private JSpinner widthSpinner;

    private JSpinner layerSpinner;

    private JSpinner positionInLayerSpinner;

    String[] fillList = { "None", "Horizontal", "Vertical", "Both" };

    private JComboBox fillOptions;

    /**
     *  
     */
    public ChangePanelPropertiesPanel() {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridwidth = 3;
        this.add(this.label, c);
        this.add(this.display, c);

        this.xSpinner = this.newGridSpinner();
        addLabel(this.xSpinner, "grid x:", c);
        this.ySpinner = this.newGridSpinner();
        addLabel(this.ySpinner, "grid y:", c);

        this.heightSpinner = this.newGridSpinner();
        addLabel(this.heightSpinner, "grid height:", c);
        this.widthSpinner = this.newGridSpinner();
        addLabel(this.widthSpinner, "grid width:", c);

        fillOptions = new JComboBox(fillList);
        fillOptions.addActionListener(this);
        addLabel(this.fillOptions, "fill options:", c);

        this.display.addActionListener(this);
        this.weightxSpinner = newWeightSpinner();
        addLabel(this.weightxSpinner, "weight x:", c);
        this.weightySpinner = newWeightSpinner();
        addLabel(this.weightySpinner, "weight y:", c);

        this.layerSpinner = this.newLayerAndPosSpinner();
        addLabel(this.layerSpinner, "layer:", c);
        this.positionInLayerSpinner = this.newLayerAndPosSpinner();
        addLabel(this.positionInLayerSpinner, "layer pos:", c);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        this.add(new JPanel(), c);

    }

    /**
     * @return
     */
    private void addLabel(JComponent component, String label, GridBagConstraints c) {
        c.weightx = 0.01;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridwidth = 1;
        add(new JLabel(label, JLabel.RIGHT), c);
        c.weightx = 0;
        c.gridx = 1;
        add(component, c);
        c.weightx = 1;
        c.gridx = 2;
        add(new JPanel(), c);
    }

    /**
     *  
     */
    private JSpinner newWeightSpinner() {
        SpinnerNumberModel model = new SpinnerNumberModel(0.0, 0.0, 1.0, 0.1);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        JFormattedTextField textField = editor.getTextField();
        textField.setColumns(3);
        spinner.addChangeListener(this);
        return spinner;
    }

    private JSpinner newGridSpinner() {
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 10, 1);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        JFormattedTextField textField = editor.getTextField();
        textField.setColumns(3);
        spinner.addChangeListener(this);
        return spinner;
    }

    private JSpinner newLayerAndPosSpinner() {
        SpinnerNumberModel model = new SpinnerNumberModel(0, -1, 100, 1);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        JFormattedTextField textField = editor.getTextField();
        textField.setColumns(3);
        spinner.addChangeListener(this);
        return spinner;
    }

    public void setPanel(GridBagPanel panel) {
        this.panel = panel;
        if (panel != null) {
            GridBagPanelConstraints c = panel.getConstraints();
            this.label.setText(panel.toString());
            this.display.setSelected(panel.isShownInLayout());
            this.weightxSpinner.setValue(new Double(panel.getWeightx()));
            this.weightySpinner.setValue(new Double(panel.getWeighty()));
            this.xSpinner.setValue(new Integer(panel.getGridx()));
            this.ySpinner.setValue(new Integer(panel.getGridy()));
            this.heightSpinner.setValue(new Integer(panel.getGridheight()));
            this.widthSpinner.setValue(new Integer(panel.getGridwidth()));
            this.layerSpinner.setValue(new Integer(c.layer));
            this.positionInLayerSpinner.setValue(new Integer(c.positionInLayer));
            switch (panel.getFill()) {
            case GridBagConstraints.NONE:
                this.fillOptions.setSelectedItem("None");
                break;
            case GridBagConstraints.HORIZONTAL:
                this.fillOptions.setSelectedItem("Horizontal");
                break;
            case GridBagConstraints.VERTICAL:
                this.fillOptions.setSelectedItem("Vertical");
                break;
            case GridBagConstraints.BOTH:
                this.fillOptions.setSelectedItem("Both");
                break;
            }

        } else {
            this.label.setText("");
        }
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (this.panel != null) {
            if (e.getSource() == this.display) {
                if (this.display.isSelected() != this.panel.isShownInLayout()) {
                    this.panel.setShowInLayout(this.display.isSelected());
                }

            } else if (e.getSource() == this.fillOptions) {
                if (this.fillOptions.getSelectedItem().equals("None")) {
                    this.panel.setFill(GridBagConstraints.NONE);
                } else if (this.fillOptions.getSelectedItem().equals("Horizontal")) {
                    this.panel.setFill(GridBagConstraints.HORIZONTAL);
                } else if (this.fillOptions.getSelectedItem().equals("Vertical")) {
                    this.panel.setFill(GridBagConstraints.VERTICAL);
                } else if (this.fillOptions.getSelectedItem().equals("Both")) {
                    this.panel.setFill(GridBagConstraints.BOTH);
                }

            }
        }
    }

    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
        if (this.panel != null) {
            if (e.getSource() == this.weightxSpinner) {
                Double value = (Double) this.weightxSpinner.getValue();
                this.panel.setWeightx(value.doubleValue());
            } else if (e.getSource() == this.weightySpinner) {
                Double value = (Double) this.weightySpinner.getValue();
                this.panel.setWeighty(value.doubleValue());
            } else if (e.getSource() == this.xSpinner) {
                Integer value = (Integer) this.xSpinner.getValue();
                this.panel.setGridx(value.intValue());
            } else if (e.getSource() == this.ySpinner) {
                Integer value = (Integer) this.ySpinner.getValue();
                this.panel.setGridy(value.intValue());
            } else if (e.getSource() == this.heightSpinner) {
                Integer value = (Integer) this.heightSpinner.getValue();
                this.panel.setGridheight(value.intValue());
            } else if (e.getSource() == this.widthSpinner) {
                Integer value = (Integer) this.widthSpinner.getValue();
                this.panel.setGridwidth(value.intValue());
            } else if (e.getSource() == this.layerSpinner) {
                Integer value = (Integer) this.layerSpinner.getValue();
                GridBagPanelConstraints c = this.panel.getConstraints();
                c.layer = value;
                this.panel.setConstraints(c);
            } else if (e.getSource() == this.positionInLayerSpinner) {
                Integer value = (Integer) this.positionInLayerSpinner.getValue();
                GridBagPanelConstraints c = this.panel.getConstraints();
                c.positionInLayer = value;
                this.panel.setConstraints(c);
            }
        }
    }

}
