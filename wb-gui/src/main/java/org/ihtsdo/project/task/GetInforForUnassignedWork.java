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
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;

/**
 * The Class GetInforForUnassignedWork.
 *
 * @author Guillermo Reynoso
 */
public class GetInforForUnassignedWork extends JPanel {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The config.
     */
    private I_ConfigAceFrame config;
    /**
     * The selected project.
     */
    private I_TerminologyProject selectedProject = null;
    /**
     * The selected work list.
     */
    private WorkList selectedWorkList = null;

    /**
     * Instantiates a new gets the infor for unassigned work.
     *
     * @param config the config
     */
    public GetInforForUnassignedWork(I_ConfigAceFrame config) {
        this.config = config;
        initComponents();

        for (I_TerminologyProject loopProject : TerminologyProjectDAO.getAllProjects(config)) {
            projectCombo.addItem(loopProject);
        }

    }

    /**
     * Update work lists combo.
     */
    private void updateWorkListsCombo() {
        nacWorkListCombo.removeAllItems();
        if (selectedProject != null) {
            if (TranslationProject.class.isAssignableFrom(selectedProject.getClass())) {
                TranslationProject project = (TranslationProject) selectedProject;
                try {
                    String str = "";
                    if (!(project.getSourceLanguageRefsets() != null && (project.getSourceLanguageRefsets().size() > 0))) {
                        str = "The translation project has not source language.\n";
                    }
                    if (project.getTargetLanguageRefset() == null) {
                        str += "The translation project has not target language";
                    }
                    if (!str.equals("")) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                str, "",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (HeadlessException e) {
                    // TODO Auto-generated catch block
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (TerminologyException e) {
                    // TODO Auto-generated catch block
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        }
        if (projectCombo.getSelectedItem() != null) {
            I_TerminologyProject selectedProject = (I_TerminologyProject) projectCombo.getSelectedItem();
            for (WorkList nacWorkList : TerminologyProjectDAO.getAllNacWorkLists(selectedProject, config)) {
                nacWorkListCombo.addItem(nacWorkList);
            }
        }
    }

    /**
     * Combo box1 action performed.
     *
     * @param e the e
     */
    private void comboBox1ActionPerformed(ActionEvent e) {
        //updateWorkListsCombo();
    }

    /**
     * Project combo item state changed.
     *
     * @param e the e
     */
    private void projectComboItemStateChanged(ItemEvent e) {
        selectedProject = (I_TerminologyProject) projectCombo.getSelectedItem();
        updateWorkListsCombo();
    }

    /**
     * Nac work list combo item state changed.
     *
     * @param e the e
     */
    private void nacWorkListComboItemStateChanged(ItemEvent e) {
        selectedWorkList = (WorkList) nacWorkListCombo.getSelectedItem();
    }

    /**
     * Inits the components.
     */
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        label2 = new JLabel();
        projectCombo = new JComboBox();
        label3 = new JLabel();
        nacWorkListCombo = new JComboBox();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout) getLayout()).columnWidths = new int[]{0, 0, 0};
        ((GridBagLayout) getLayout()).rowHeights = new int[]{0, 0, 0, 0};
        ((GridBagLayout) getLayout()).columnWeights = new double[]{0.0, 1.0, 1.0E-4};
        ((GridBagLayout) getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 1.0E-4};

        //---- label1 ----
        label1.setText("Assign to:");
        add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

        //---- label2 ----
        label2.setText("Project");
        add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

        //---- projectCombo ----
        projectCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comboBox1ActionPerformed(e);
            }
        });
        projectCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                projectComboItemStateChanged(e);
            }
        });
        add(projectCombo, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

        //---- label3 ----
        label3.setText("WorkList");
        add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

        //---- nacWorkListCombo ----
        nacWorkListCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                nacWorkListComboItemStateChanged(e);
            }
        });
        add(nacWorkListCombo, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label1;
    private JLabel label2;
    private JComboBox projectCombo;
    private JLabel label3;
    private JComboBox nacWorkListCombo;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    /**
     * Gets the selected project.
     *
     * @return the selected project
     */
    public I_TerminologyProject getSelectedProject() {
        return selectedProject;
    }

    /**
     * Sets the selected project.
     *
     * @param selectedProject the new selected project
     */
    public void setSelectedProject(I_TerminologyProject selectedProject) {
        this.selectedProject = selectedProject;
    }

    /**
     * Gets the selected work list.
     *
     * @return the selected work list
     */
    public WorkList getSelectedWorkList() {
        return selectedWorkList;
    }

    /**
     * Sets the selected work list.
     *
     * @param selectedWorkList the new selected work list
     */
    public void setSelectedWorkList(WorkList selectedWorkList) {
        this.selectedWorkList = selectedWorkList;
    }
}
