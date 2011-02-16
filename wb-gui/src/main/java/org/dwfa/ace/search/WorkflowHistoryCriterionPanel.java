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
package org.dwfa.ace.search;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.gui.TaskPanel.EditorGlue;
import org.ihtsdo.ace.task.search.I_TestWorkflowHistorySearchResults;

public class WorkflowHistoryCriterionPanel extends CriterionPanel {

    private List<I_TestWorkflowHistorySearchResults> criterionOptions;

    public I_TestWorkflowHistorySearchResults bean;

	private static final long serialVersionUID = 1L;

	private Map<String, I_TestWorkflowHistorySearchResults> menuBeanMap = new HashMap<String, I_TestWorkflowHistorySearchResults>();

	public class WorkflowCriterionListener implements ActionListener {

	        public void actionPerformed(ActionEvent e) {
	            try {
	                editorPanel.removeAll();
	                editorPanel.setLayout(new GridBagLayout());
	                GridBagConstraints gbc = new GridBagConstraints();
	                gbc.anchor = GridBagConstraints.WEST;
	               // gbc.fill = GridBagConstraints.HORIZONTAL;
	                gbc.gridx = 0;
	                gbc.gridy = 0;
	                gbc.gridwidth = 1;
	                gbc.gridheight = 1;
	                gbc.weightx = 1.0;
	                gbc.weighty = 0;

	                String menuItem = (String) criterionCombo.getSelectedItem();
	                BeanInfo searchInfo = menuInfoMap.get(menuItem);
	                bean = menuBeanMap.get(menuItem);

	                for (PropertyDescriptor pd : searchInfo.getPropertyDescriptors()) {
	                    gbc.weightx = 0.0;
	                    JLabel editorLabel = new JLabel(pd.getDisplayName());
	                    editorLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 2, 5));
	                    editorLabel.setToolTipText(pd.getShortDescription());
	                    editorPanel.add(editorLabel, gbc);
	                    gbc.weightx = 1.0;
	                    gbc.gridx++;
	                    PropertyEditor editor = pd.createPropertyEditor(bean);
	                    if (AbstractButton.class.isAssignableFrom(editor.getCustomEditor().getClass())) {
	                        gbc.weightx = 0.0;
	                    }

	                    Method readMethod = pd.getReadMethod();
	                    Object value = readMethod.invoke(bean, (Object[]) null);
	                    editor.setValue(value);

	                    editor.addPropertyChangeListener(new EditorGlue(editor, pd.getWriteMethod(), bean));
	                    JComponent editorComponent = (JComponent) editor.getCustomEditor();
	                    //editorComponent.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 8));
	                    gbc.fill = GridBagConstraints.BOTH;
	                    editorPanel.add(editorComponent, gbc);

	                    gbc.gridx++;
	                }
	                editorPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
	                editorPanel.invalidate();
	                editorPanel.validate();
	                editorPanel.doLayout();


	                WorkflowHistoryCriterionPanel.this.invalidate();
	                WorkflowHistoryCriterionPanel.this.validate();
	                WorkflowHistoryCriterionPanel.this.doLayout();
	            } catch (Exception ex) {
	                AceLog.getAppLog().alertAndLogException(ex);
	            }
	        }

	    }

    public WorkflowHistoryCriterionPanel(I_MakeCriterionPanel searchPanel, I_TestWorkflowHistorySearchResults beanToSet) {
        this(searchPanel, beanToSet, new ArrayList<I_TestWorkflowHistorySearchResults>());
    }

    public WorkflowHistoryCriterionPanel(I_MakeCriterionPanel searchPanel, I_TestWorkflowHistorySearchResults beanToSet,
            List<I_TestWorkflowHistorySearchResults> criterionOptions) {
    	super(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;

        JButton addButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/add2.png")));
        addButton.setIconTextGap(0);
        addButton.addActionListener(new AddCriterion(searchPanel));
        add(addButton, gbc);
        addButton.setToolTipText("add a new AND search clause to end of query");
        gbc.gridx++;
        JButton removeButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/delete2.png")));
        removeButton.addActionListener(new RemoveCriterion(searchPanel, this));
        removeButton.setIconTextGap(0);
        add(removeButton, gbc);
        removeButton.setToolTipText("remove this search clause");

        gbc.gridx++;

        setupWorkflowHistoryCriterionOptions(criterionOptions);

        criterionCombo = new JComboBox(comboItems.toArray()) {
            /**
			 *
			 */
            private static final long serialVersionUID = 1L;

            @Override
            public void setSize(Dimension d) {
                d.width = getMinimumSize().width;
                super.setSize(d);
            }

            @Override
            public void setSize(int width, int height) {
                super.setSize(getMinimumSize().width, height);
            }

            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x, y, getMinimumSize().width, height);
            }

            @Override
            public void setBounds(Rectangle r) {
                r.width = getMinimumSize().width;
                super.setBounds(r);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = getMinimumSize().width;
                return d;
            }

        };
        criterionCombo.setMinimumSize(new Dimension(175, 20));
        gbc.fill = GridBagConstraints.VERTICAL;
        add(criterionCombo, gbc);
        criterionCombo.addActionListener(new WorkflowCriterionListener());
        if (criterionCombo.getItemCount() > 0) {
         criterionCombo.setSelectedIndex(0);
        }
        gbc.fill = GridBagConstraints.NONE;

        gbc.weightx = 1;
        //gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx++;
/*
        gbc.weightx = 1;
        gbc.gridx++;
        this.searchPhraseField = new JTextField(200);
        this.searchPhraseField.setDragEnabled(true);
        this.searchPhraseField.setMinimumSize(new Dimension(400, 20));
        this.searchPhraseField.setText("search criteria");
        add(searchPhraseField, gbc);

        gbc.gridx++;
        gbc.gridheight = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
*/


        add(editorPanel, gbc);
    }

    public WorkflowHistoryCriterionPanel(I_MakeCriterionPanel searchPanel) {
        this(searchPanel, null);
    }

    @SuppressWarnings("unchecked")
    public void setupWorkflowHistoryCriterionOptions(List<I_TestWorkflowHistorySearchResults> criterionOptions) {
          File searchPluginFolder = new File("search/workflow");

        this.criterionOptions = new ArrayList<I_TestWorkflowHistorySearchResults>();
        if (criterionOptions == null || criterionOptions.isEmpty()) {
            File[] searchPlugins = searchPluginFolder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".task");
                }
            });
            if (searchPlugins != null) {
                for (File plugin : searchPlugins) {
                    try {
                        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                            plugin)));
                        Object pluginObj = ois.readObject();
                        ois.close();
                        if (I_TestWorkflowHistorySearchResults.class.isAssignableFrom(pluginObj.getClass())) {
                            criterionOptions.add((I_TestWorkflowHistorySearchResults) pluginObj);
                        }
                    } catch (IOException ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    } catch (ClassNotFoundException ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    }
                }
            } else {
                AceLog.getAppLog().log(Level.WARNING, "No search plugins in folder: " + searchPluginFolder.getAbsolutePath(),
                    new Exception("No search plugins in folder: " + searchPluginFolder.getAbsolutePath()));
            }
        }
        for (I_TestWorkflowHistorySearchResults bean : criterionOptions) {
            try {
                String searchInfoClassName = bean.getClass().getName() + "SearchInfo";
                Class<BeanInfo> searchInfoClass = (Class<BeanInfo>) bean.getClass().getClassLoader().loadClass(
                    searchInfoClassName);
                BeanInfo searchInfo = searchInfoClass.newInstance();
                comboItems.add(searchInfo.getBeanDescriptor().getDisplayName());
                menuInfoMap.put(searchInfo.getBeanDescriptor().getDisplayName(), searchInfo);
                menuBeanMap.put(searchInfo.getBeanDescriptor().getDisplayName(), bean);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

    }

    public I_TestWorkflowHistorySearchResults getBean() {
        return bean;
    }
}
