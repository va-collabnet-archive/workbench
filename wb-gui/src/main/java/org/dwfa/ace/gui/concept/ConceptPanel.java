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
package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.AbstractSpinnerModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentDataCheckSelectionListener;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.TermComponentListSelectionListener;
import org.dwfa.ace.TermComponentTreeSelectionListener;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.tk.api.PathBI;

public class ConceptPanel extends JPanel implements I_HostConceptPlugins, PropertyChangeListener, Scrollable {

    private class ShowHistoryListener implements ActionListener {

        private class ShowHistoryAction extends AbstractAction {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;
            I_GetConceptData concept;

            public ShowHistoryAction(I_GetConceptData concept) {
                super(concept.toString());
                this.concept = concept;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                ConceptPanel.this.setTermComponent(concept);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (tabHistoryList.size() > 1) {
                JPopupMenu popup = new JPopupMenu();
                List<I_GetConceptData> historyToRemove = new ArrayList<I_GetConceptData>();
                for (I_GetConceptData historyItem : tabHistoryList) {
                    try {
                        if (historyItem.getConceptNid() != 0 && Terms.get().getUids(historyItem.getConceptNid()) != null) {
                            JMenuItem menuItem = new JMenuItem(new ShowHistoryAction(historyItem));
                            popup.add(menuItem);
                        } else {
                            historyToRemove.add(historyItem);
                        }
                    } catch (IOException e1) {
                        historyToRemove.add(historyItem);
                    } catch (TerminologyException e2) {
                        historyToRemove.add(historyItem);
                    }
                }
                tabHistoryList.removeAll(historyToRemove);
                Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(mouseLocation, ConceptPanel.this);
                popup.show(ConceptPanel.this, mouseLocation.x, mouseLocation.y);
            }
        }
    }

    private class LabelListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateTab(label.getTermComponent());
            if (label.getTermComponent() != null) {
                config.setLastViewed((I_GetConceptData) label.getTermComponent());
                if (tabHistoryList.size() == 0) {
                    tabHistoryList.addFirst((I_GetConceptData) label.getTermComponent());
                } else if ((tabHistoryList.size() > 0)
                        && (label.getTermComponent().equals(tabHistoryList.getFirst()) == false)) {
                    tabHistoryList.addFirst((I_GetConceptData) label.getTermComponent());
                }
                while (tabHistoryList.size() > 20) {
                    tabHistoryList.removeLast();
                }
            }
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    private class ShowPluginComponentActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        contentScroller.setViewportView(getContentPane());
                    } catch (TerminologyException e) {
                        AceLog.getAppLog().alertAndLog(ConceptPanel.this, Level.SEVERE,
                                "Database Exception: " + e.getLocalizedMessage(), e);
                    } catch (IOException e) {
                        AceLog.getAppLog().alertAndLog(ConceptPanel.this, Level.SEVERE,
                                "Database Exception: " + e.getLocalizedMessage(), e);
                    }
                }
            });
        }
    }

    private class UncommittedChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent arg0) {
            setTermComponent(getTermComponent());
        }
    }

    private class FixedToggleChangeActionListener implements ActionListener, PropertyChangeListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            perform();
        }

        private void perform() {
            firePropertyChange(I_HostConceptPlugins.SHOW_HISTORY, !historyButton.isSelected(),
                    historyButton.isSelected());
            try {
                contentScroller.setViewportView(getContentPane());
            } catch (TerminologyException e1) {
                AceLog.getAppLog().alertAndLog(ConceptPanel.this, Level.SEVERE,
                        "Database Exception: " + e1.getLocalizedMessage(), e1);
            } catch (IOException e1) {
                AceLog.getAppLog().alertAndLog(ConceptPanel.this, Level.SEVERE,
                        "Database Exception: " + e1.getLocalizedMessage(), e1);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent arg0) {
            perform();
        }
    }

    private class UpdateTogglesPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent arg0) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    updateToggles();
                }
            });
        }
    }
    private TermComponentLabel label;
    private JToggleButton historyButton;
    private JScrollPane contentScroller;
    private JToggleButton usePrefButton;
    private I_ConfigAceFrame config;
    public static ImageIcon HISTORY_ICON = new ImageIcon(ACE.class.getResource("/24x24/plain/history2.png"));
    public static ImageIcon UNLINKED_ICON = new ImageIcon(ACE.class.getResource("/24x24/plain/carabiner.png"));
    public static ImageIcon SEARCH_LINK_ICON = new ImageIcon(ACE.class.getResource("/24x24/plain/carabiner_find.png"));
    public static ImageIcon TREE_LINK_ICON = new ImageIcon(ACE.class.getResource("/24x24/plain/carabiner_tree.png"));
    public static ImageIcon LIST_LINK_ICON = new ImageIcon(ACE.class.getResource("/24x24/plain/carabiner_up_arrow.png"));
    public static ImageIcon DATA_CHECK_LINK_ICON = new ImageIcon(
            ACE.class.getResource("/24x24/plain/carabiner_alert.png"));
    public static ImageIcon SMALL_SEARCH_LINK_ICON = new ImageIcon(ACE.class.getResource("/16x16/plain/find.png"));
    public static ImageIcon SMALL_TREE_LINK_ICON = new ImageIcon(ACE.class.getResource("/16x16/plain/text_tree.png"));
    public static ImageIcon SMALL_LIST_LINK_ICON = new ImageIcon(
            ACE.class.getResource("/16x16/plain/arrow_up_green.png"));
    public static ImageIcon SMALL_ALERT_LINK_ICON = new ImageIcon(ACE.class.getResource("/16x16/plain/warning.png"));
    public static ImageIcon ARENA_LINK_ICON = new ImageIcon(ACE.class.getResource("/16x16/plain/eye.png"));
    public ImageIcon tabIcon;
    private JTabbedPane conceptTabs;
    private FixedToggleChangeActionListener fixedToggleChangeActionListener;
    private PropertyChangeListener labelListener = new LabelListener();
    private JToggleButton refsetToggleButton;
    private JButton componentHistoryButton;
    private Integer panelId;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private class LinkListModel extends AbstractSpinnerModel {

        ImageIcon[] items;
        int currentSelection = 0;

        public LinkListModel(ImageIcon[] items, int currentSelection) {
            super();
            this.items = items;
            this.currentSelection = currentSelection;
            if (currentSelection >= items.length) {
                this.currentSelection = items.length - 1;
            }
        }

        @Override
        public Object getNextValue() {
            currentSelection++;
            if (currentSelection >= items.length) {
                currentSelection = 0;
            }
            return getValue();
        }

        @Override
        public Object getPreviousValue() {
            currentSelection--;
            if (currentSelection < 0) {
                currentSelection = items.length - 1;
            }
            return getValue();
        }

        @Override
        public Object getValue() {
            return items[currentSelection];
        }

        @Override
        public void setValue(Object value) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] == value) {
                    currentSelection = i;
                    changeLinkListener(LINK_TYPE.values()[i]);
                    break;
                }
            }
            fireStateChanged();
        }
    }

    private class LinkEditor extends JLabel implements ChangeListener {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public LinkEditor(JSpinner spinner) {
            setOpaque(true);

            // Get info from the model.
            LinkListModel myModel = (LinkListModel) (spinner.getModel());
            Icon value = (Icon) myModel.getValue();
            setIcon(value);
            spinner.addChangeListener(this);

            // Set tool tip text.
            updateToolTipText(spinner);

            // Set size info.
            Dimension size = new Dimension(value.getIconWidth() + 4, value.getIconHeight() + 4);
            setMinimumSize(size);
            setPreferredSize(size);
        }

        protected void updateToolTipText(JSpinner spinner) {
            String toolTipText = spinner.getToolTipText();
            if (toolTipText != null) {
                // JSpinner has tool tip text. Use it.
                if (!toolTipText.equals(getToolTipText())) {
                    setToolTipText(toolTipText);
                }
            } else {
                // Define our own tool tip text.
                LinkListModel myModel = (LinkListModel) (spinner.getModel());
                Icon value = (Icon) myModel.getValue();
                if (value == SEARCH_LINK_ICON) {
                    setToolTipText("This panel is linked to the search selection");
                    tabIcon = SMALL_SEARCH_LINK_ICON;
                } else if (value == TREE_LINK_ICON) {
                    setToolTipText("This panel is linked to the hierarchy selection");
                    tabIcon = SMALL_TREE_LINK_ICON;
                } else if (value == UNLINKED_ICON) {
                    setToolTipText("This panel is not linked to other selections");
                    tabIcon = null;
                } else if (value == LIST_LINK_ICON) {
                    setToolTipText("This panel is linked to the list selection above");
                    tabIcon = SMALL_LIST_LINK_ICON;
                } else if (value == DATA_CHECK_LINK_ICON) {
                    setToolTipText("This panel is linked to the data check selection");
                    tabIcon = SMALL_ALERT_LINK_ICON;
                } else if (value == SMALL_ALERT_LINK_ICON) {
                    setToolTipText("This panel is linked to the last selected concept in the arena");
                    tabIcon = ARENA_LINK_ICON;
                } else if (value == ARENA_LINK_ICON) {
                    setToolTipText("This panel is linked to the search selection");
                    tabIcon = SMALL_SEARCH_LINK_ICON;
                }
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JSpinner mySpinner = (JSpinner) (e.getSource());
            LinkListModel myModel = (LinkListModel) (mySpinner.getModel());
            setIcon((Icon) myModel.getValue());
            updateToolTipText(mySpinner);
            updateTab(label.getTermComponent());
        }
    }

    public ConceptPanel(HOST_ENUM host_enum, I_ConfigAceFrame config, LINK_TYPE link, Integer panelId, String pluginRoot) throws
            IOException, ClassNotFoundException, NoSuchAlgorithmException, TerminologyException {
        this(host_enum, config, link, null, panelId, pluginRoot);
    }

    public ConceptPanel(HOST_ENUM host_enum, I_ConfigAceFrame config, LINK_TYPE link, boolean enableListLink, Integer panelId, String pluginRoot)
            throws IOException, ClassNotFoundException, NoSuchAlgorithmException, TerminologyException {
        this(host_enum, config, link, null, enableListLink, panelId, pluginRoot);
    }

    public ConceptPanel(HOST_ENUM host_enum, I_ConfigAceFrame config, LINK_TYPE link, JTabbedPane conceptTabs, Integer panelId, String pluginRoot)
            throws IOException, ClassNotFoundException, NoSuchAlgorithmException, TerminologyException {
        this(host_enum, config, link, conceptTabs, false, panelId, pluginRoot);
    }
    LinkedList<I_GetConceptData> tabHistoryList;
    private HOST_ENUM host_enum;
    private LinkListModel linkSpinnerModel;
    private JSpinner linkSpinner;
    private LinkEditor linkEditor;
    private String pluginRoot;

    public ConceptPanel(HOST_ENUM host_enum,
            I_ConfigAceFrame config,
            LINK_TYPE link,
            JTabbedPane conceptTabs,
            boolean enableListLink,
            Integer panelId,
            String pluginRoot)
            throws IOException, ClassNotFoundException, NoSuchAlgorithmException, TerminologyException {
        super(new GridBagLayout());
        this.config = config;
        this.pluginRoot = pluginRoot;
        this.panelId = panelId;
        this.host_enum = host_enum;

        this.tabHistoryList = (LinkedList<I_GetConceptData>) config.getTabHistoryMap().get(
                "tab " + panelId);
        if (this.tabHistoryList == null) {
            this.tabHistoryList = new LinkedList<I_GetConceptData>();
            config.getTabHistoryMap().put("tab " + this.panelId, this.tabHistoryList);
        }
        UpdateTogglesPropertyChangeListener updateListener = new UpdateTogglesPropertyChangeListener();
        config.addPropertyChangeListener("visibleComponentToggles", updateListener);

        addPropertyChangeListener("conflictResolutionStrategy", this);
        addPropertyChangeListener("highlightConflictsInComponentPanel", this);

        config.addPropertyChangeListener("uncommitted", new UncommittedChangeListener());
        label = new TermComponentLabel(config);
        label.setBackground(Color.RED);
        fixedToggleChangeActionListener = new FixedToggleChangeActionListener();
        config.addPropertyChangeListener("visibleRefsets", fixedToggleChangeActionListener);
        config.addPropertyChangeListener(this);
        this.conceptTabs = conceptTabs;
        GridBagConstraints c = new GridBagConstraints();

        List<ImageIcon> ImageIconList = new ArrayList<ImageIcon>();
        ImageIconList.add(UNLINKED_ICON);
        ImageIconList.add(SEARCH_LINK_ICON);
        ImageIconList.add(TREE_LINK_ICON);
        if (ACE.editMode) {
            ImageIconList.add(DATA_CHECK_LINK_ICON);
        }
        if (enableListLink) {
            ImageIconList.add(LIST_LINK_ICON);
        }

        linkSpinnerModel = new LinkListModel(ImageIconList.toArray(new ImageIcon[ImageIconList.size()]),
                link.ordinal());

        linkSpinner = new JSpinner(linkSpinnerModel);
        linkSpinner.setBorder(BorderFactory.createEmptyBorder(3, 3, 2, 5));
        linkEditor = new LinkEditor(linkSpinner);
        linkSpinner.setEditor(linkEditor);
        changeLinkListener(link);

        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        add(linkSpinner, c);
        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        add(label, c);
        c.weightx = 0.0;
        c.gridx++;
        componentHistoryButton = new JButton(HISTORY_ICON);
        componentHistoryButton.addActionListener(new ShowHistoryListener());
        componentHistoryButton.setToolTipText("click to show history of concepts displayed in this viewer");
        add(componentHistoryButton, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(getToggleBar(), c);
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.gridy++;

        contentScroller = new JScrollPane(getContentPane());
        contentScroller.getVerticalScrollBar().setUnitIncrement(20);
        add(contentScroller, c);
        setBorder(BorderFactory.createRaisedBevelBorder());
        label.addPropertyChangeListener("termComponent", labelListener);
        if (this.tabHistoryList.size() > 0 && this.tabHistoryList.getFirst() != null) {
            this.setTermComponent(this.tabHistoryList.getFirst());
        }
        addMouseListener(new ProgrammersPopupListener(this));
    }

    public JComponent getContentPane() throws TerminologyException, IOException {
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        for (I_PluginToConceptPanel plugin : new TreeSet<I_PluginToConceptPanel>(config.getConceptPanelPlugins(host_enum))) {
            if (plugin.showComponent()) {
                content.add(plugin.getComponent(this), c);
                c.gridy++;
            }
        }
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        content.add(new JPanel(), c);
        return content;
    }

    public JComponent getToggleBar() throws IOException, ClassNotFoundException {
        JPanel toggleBar = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;

        JPanel leftTogglePane = new JPanel(new FlowLayout());
        toggleBar.add(leftTogglePane, c);

        JPanel rightTogglePane = new JPanel(new FlowLayout());

        ShowPluginComponentActionListener l = new ShowPluginComponentActionListener();
        for (I_PluginToConceptPanel plugin : new TreeSet<I_PluginToConceptPanel>(config.getConceptPanelPlugins(host_enum))) {
            if (plugin != null) {
                if (plugin.getToggleBarComponents() != null) {
                    for (JComponent component : plugin.getToggleBarComponents()) {
                        leftTogglePane.add(component);
                        if (config.isToggleVisible(TOGGLES.fromId(plugin.getId()))) {
                            component.setVisible(true);
                            component.setEnabled(true);
                        } else {
                            component.setVisible(false);
                            component.setEnabled(false);
                        }
                    }
                    plugin.addShowComponentListener(l);
                } else {
                    AceLog.getAppLog().warning(plugin + " has null components");
                }
            } else {
                AceLog.getAppLog().warning(
                        plugin
                        + " is null: "
                        + new TreeSet<I_PluginToConceptPanel>(config.getConceptPanelPlugins(
                        host_enum)));
            }
        }
        fixedToggleChangeActionListener = new FixedToggleChangeActionListener();

        refsetToggleButton = new JToggleButton(new ImageIcon(ACE.class.getResource("/24x24/plain/paperclip.png")));
        refsetToggleButton.setSelected(false);
        refsetToggleButton.setVisible(ACE.editMode);
        refsetToggleButton.setToolTipText("show/hide refset entries of types selected in the preferences");
        refsetToggleButton.addActionListener(fixedToggleChangeActionListener);
        leftTogglePane.add(refsetToggleButton);

        usePrefButton = new JToggleButton(
                new ImageIcon(ACE.class.getResource("/24x24/plain/component_preferences.png")));
        usePrefButton.setSelected(false);
        usePrefButton.setVisible(ACE.editMode);
        usePrefButton.setToolTipText("use preferences to filter views");
        usePrefButton.addActionListener(fixedToggleChangeActionListener);
        leftTogglePane.add(usePrefButton);

        historyButton = new JToggleButton(new ImageIcon(ACE.class.getResource("/24x24/plain/history.png")));
        historyButton.setSelected(false);
        historyButton.addActionListener(fixedToggleChangeActionListener);
        historyButton.setToolTipText("show/hide the history records");
        leftTogglePane.add(historyButton);

        IdPlugin idPlugin = (IdPlugin) config.getConceptPanelPlugin(host_enum,
                TOGGLES.ID.getPluginId());
        idPlugin.getToggleButton().addActionListener(fixedToggleChangeActionListener);

        c.gridx++;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        toggleBar.add(new JPanel(), c);

        File componentPluginDir = new File(pluginRoot + File.separator + "component");
        File[] plugins = componentPluginDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String fileName) {
                return fileName.toLowerCase().endsWith(".bp");
            }
        });
        if (plugins != null) {
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.NONE;
            c.gridx++;
            toggleBar.add(rightTogglePane, c);
            boolean exceptions = false;
            StringBuilder exceptionMessage = new StringBuilder();
            exceptionMessage.append("<html>Exception(s) reading the following plugin(s): <p><p>");
            for (File f : plugins) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    BusinessProcess bp = (BusinessProcess) ois.readObject();
                    ois.close();
                    byte[] iconBytes = (byte[]) bp.readAttachement("button_icon");
                    if (iconBytes != null) {
                        ImageIcon icon = new ImageIcon(iconBytes);
                        JButton pluginButton = new JButton(icon);
                        pluginButton.setToolTipText(bp.getSubject());
                        pluginButton.addActionListener(new PluginListener(f));
                        rightTogglePane.add(pluginButton, c);
                    } else {
                        JButton pluginButton = new JButton(bp.getName());
                        pluginButton.setToolTipText(bp.getSubject());
                        pluginButton.addActionListener(new PluginListener(f));
                        rightTogglePane.add(pluginButton, c);
                    }
                } catch (Throwable e) {
                    exceptions = true;
                    exceptionMessage.append("Exception reading plugin: ").append(f.getAbsolutePath()).append("<p>");
                    AceLog.getAppLog().log(Level.SEVERE, "Exception reading: " + f.getAbsolutePath(), e);
                }
            }

            if (exceptions) {
                exceptionMessage.append("<p>Please see the log file for more details.");
                JOptionPane.showMessageDialog(this, exceptionMessage.toString());
            }
        }

        updateToggles();

        return toggleBar;
    }

    private void updateToggles() {
        for (TOGGLES t : TOGGLES.values()) {
            boolean visible = config.isToggleVisible(t);
            if (config.getConceptPanelPlugin(host_enum, t.getPluginId()) != null) {
                I_PluginToConceptPanel plugin = config.getConceptPanelPlugin(host_enum,
                        t.getPluginId());
                for (JComponent toggleComponent : plugin.getToggleBarComponents()) {
                    toggleComponent.setVisible(visible);
                    toggleComponent.setEnabled(visible);
                }
            } else {
                switch (t) {
                    case HISTORY:
                        historyButton.setVisible(visible);
                        historyButton.setEnabled(visible);
                        break;
                    case PREFERENCES:
                        if (ACE.editMode) {
                            usePrefButton.setVisible(visible);
                            usePrefButton.setEnabled(visible);
                        }
                        break;
                    case REFSETS:
                        refsetToggleButton.setVisible(visible);
                        refsetToggleButton.setEnabled(visible);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private class PluginListener implements ActionListener {

        File pluginProcessFile;

        private PluginListener(File pluginProcessFile) {
            super();
            this.pluginProcessFile = pluginProcessFile;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                FileInputStream fis = new FileInputStream(pluginProcessFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis);
                final BusinessProcess bp = (BusinessProcess) ois.readObject();
                ois.close();
                getConfig().setStatusMessage("Executing: " + bp.getName());
                final MasterWorker worker = getConfig().getWorker();
                // Set concept bean
                // Set config

                worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), getConfig());
                bp.writeAttachment(ProcessAttachmentKeys.I_GET_CONCEPT_DATA.name(), label.getTermComponent());
                worker.writeAttachment(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name(), ConceptPanel.this);
                Runnable r = new Runnable() {

                    private String exceptionMessage;

                    @Override
                    public void run() {
                        I_EncodeBusinessProcess process = bp;
                        try {
                            worker.getLogger().log(
                                    Level.INFO, "Worker: {0} ({1}) executing process: {2}",
                                    new Object[]{worker.getWorkerDesc(), worker.getId(), process.getName()});
                            worker.execute(process);
                            SortedSet<ExecutionRecord> sortedRecords = new TreeSet<ExecutionRecord>(
                                    process.getExecutionRecords());
                            Iterator<ExecutionRecord> recordItr = sortedRecords.iterator();
                            StringBuilder buff = new StringBuilder();
                            while (recordItr.hasNext()) {
                                ExecutionRecord rec = recordItr.next();
                                buff.append("\n");
                                buff.append(rec.toString());
                            }
                            worker.getLogger().info(buff.toString());
                            exceptionMessage = "";
                        } catch (Throwable e1) {
                            worker.getLogger().log(Level.WARNING, e1.toString(), e1);
                            exceptionMessage = e1.toString();
                        }
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                getConfig().setStatusMessage("<html><font color='#006400'>execute");
                                if (exceptionMessage.equals("")) {
                                    getConfig().setStatusMessage(
                                            "<html>Execution of <font color='blue'>" + bp.getName() + "</font> complete.");
                                } else {
                                    getConfig().setStatusMessage(
                                            "<html><font color='blue'>Process complete: <font color='red'>"
                                            + exceptionMessage);
                                }
                            }
                        });
                    }
                };
                new Thread(r).start();
            } catch (Exception e1) {
                getConfig().setStatusMessage("Exception during execution.");
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }
    }

    @Override
    public I_AmTermComponent getTermComponent() {
        return label.getTermComponent();
    }

    @Override
    public void setTermComponent(final I_AmTermComponent termComponent) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                label.setTermComponent(termComponent);
                contentScroller.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            }
        });

    }

    private void updateTab(final I_AmTermComponent termComponent) {
        int titleLength = 15;
        if (conceptTabs != null) {
            int index = conceptTabs.indexOfComponent(this);
            if (index >= 0) {
                if (termComponent != null) {
                    I_GetConceptData cb = (I_GetConceptData) termComponent;
                    String desc;
                    try {
                        I_DescriptionTuple tdt = cb.getDescTuple(getConfig().getShortLabelDescPreferenceList(),
                                getConfig());
                        if (tdt != null) {
                            desc = tdt.getText();
                        } else {
                            desc = cb.getInitialText();
                        }
                    } catch (IOException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                        setTermComponent(null);
                        return;
                    }
                    String shortDesc;
                    if (desc.length() > titleLength) {
                        shortDesc = desc.substring(0, titleLength);
                        shortDesc = shortDesc + "...";
                    } else {
                        shortDesc = desc;
                    }
                    conceptTabs.setTitleAt(index, shortDesc);
                    conceptTabs.setToolTipTextAt(index, desc);
                } else {
                    if (conceptTabs.getTabCount() > index) {
                        conceptTabs.setTitleAt(index, "empty");
                        conceptTabs.setToolTipTextAt(index, "empty");
                    }
                }
                conceptTabs.setIconAt(index, tabIcon);
            }
        }
    }

    public void addTermChangeListener(PropertyChangeListener l) {
        addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, l);
    }

    public void removeTermChangeListener(PropertyChangeListener l) {
        removePropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, l);
    }

    @Override
    public boolean getUsePrefs() {
        return usePrefButton.isSelected();
    }

    public boolean showHistory() {
        return historyButton.isSelected();
    }

    public I_ConfigAceFrame getConfig() {
        return config;
    }
    private TermComponentTreeSelectionListener treeListener;
    private TermComponentListSelectionListener listListener;
    private TermComponentDataCheckSelectionListener dataCheckListener;
    private JList linkedList;
    private ACE ace;

    public void setAce(ACE ace, LINK_TYPE link) {
        this.ace = ace;
        changeLinkListener(link);
    }

    public void changeLinkListener(LINK_TYPE type) {
        if (ace != null) {
            if (treeListener != null) {
                ace.removeTaxonomySelectionListener(treeListener);
                treeListener = null;
            }
            if (listListener != null) {
                linkedList.removeListSelectionListener(listListener);
                listListener = null;
            }
            if (dataCheckListener != null) {
                ace.removeDataCheckListener(dataCheckListener);
                dataCheckListener = null;
            }
            ace.removeSearchLinkedComponent(this);
            switch (type) {
                case TREE_LINK:
                    treeListener = new TermComponentTreeSelectionListener(this);
                    ace.addTaxonomySelectionListener(treeListener);
                    break;
                case SEARCH_LINK:
                    ace.addSearchLinkedComponent(this);
                    break;
                case UNLINKED:
                    break;
                case LIST_LINK:
                    if (linkedList != null) {
                        listListener = new TermComponentListSelectionListener(this);
                        linkedList.addListSelectionListener(listListener);
                    }
                    break;
                case DATA_CHECK_LINK:
                    dataCheckListener = new TermComponentDataCheckSelectionListener(this);
                    ace.addDataCheckListener(dataCheckListener);
                    break;
                case ARENA_LINK:
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (propertyName.equals("viewPositions") || propertyName.equals("conflictResolutionStrategy")
                || propertyName.equals("highlightConflictsInComponentPanel")) {
            fixedToggleChangeActionListener.actionPerformed(null);
        } else if (propertyName.equals("commit")) {
            if (label.getTermComponent() != null) {
                I_GetConceptData cb = (I_GetConceptData) label.getTermComponent();
                try {
                    if (cb.getConceptAttributes() == null) {
                        label.setTermComponent(null);
                    }
                } catch (IOException e) {
                    label.setTermComponent(null);
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
            this.firePropertyChange("commit", null, null);
        }
    }

    @Override
    public boolean getShowHistory() {
        return historyButton.isSelected();
    }

    @Override
    public I_GetConceptData getHierarchySelection() {
        return config.getHierarchySelection();
    }

    public LogWithAlerts getEditLog() {
        return AceLog.getEditLog();
    }

    public JList getLinkedList() {
        return linkedList;
    }

    public void setLinkedList(JList linkedList) {
        this.linkedList = linkedList;
    }

    @Override
    public void unlink() {
        if (SwingUtilities.isEventDispatchThread()) {
            linkSpinnerModel.setValue(UNLINKED_ICON);
            linkSpinner.setValue(UNLINKED_ICON);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        linkSpinnerModel.setValue(UNLINKED_ICON);
                        linkSpinner.setValue(UNLINKED_ICON);
                    }
                });
            } catch (InterruptedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (InvocationTargetException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }

    public I_GetConceptData getConcept(Collection<UUID> ids) throws TerminologyException, IOException {
        return Terms.get().getConcept(ids);
    }

    public I_GetConceptData getConcept(UUID[] ids) throws TerminologyException, IOException {
        return Terms.get().getConcept(Arrays.asList(ids));
    }

    public I_Position newPosition(PathBI path, int version) {
        return new Position(version, path);
    }

    public I_IntSet newIntSet() {
        return new IntSet();
    }

    public void addUncommitted(I_GetConceptData concept) {
        Terms.get().addUncommitted(concept);
    }

    @Override
    public void setAllTogglesToState(final boolean state) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (I_PluginToConceptPanel plugin : new TreeSet<I_PluginToConceptPanel>(config.getConceptPanelPlugins(host_enum))) {
                    for (JComponent component : plugin.getToggleBarComponents()) {
                        if (JToggleButton.class.isAssignableFrom(component.getClass())) {
                            JToggleButton toggle = (JToggleButton) component;
                            if (toggle.isSelected() == state) {
                                // nothing to do...
                            } else {
                                toggle.doClick();
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void setLinkType(LINK_TYPE link) {
        changeLinkListener(link);
    }

    @Override
    public void setToggleState(final TOGGLES toggle, final boolean state) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                I_PluginToConceptPanel plugin = config.getConceptPanelPlugin(host_enum,
                        toggle.getPluginId());
                if (plugin != null) {
                    for (JComponent component : plugin.getToggleBarComponents()) {
                        if (JToggleButton.class.isAssignableFrom(component.getClass())) {
                            JToggleButton toggleButton = (JToggleButton) component;
                            if (toggleButton.isSelected() == state) {
                                // nothing to do...
                            } else {
                                toggleButton.doClick();
                            }
                        }
                    }
                } else {
                    switch (toggle) {
                        case HISTORY:
                            if (historyButton.isSelected() == state) {
                                // nothing to do...
                            } else {
                                historyButton.doClick();
                            }
                            break;
                        case PREFERENCES:
                            if (usePrefButton.isSelected() == state) {
                                // nothing to do...
                            } else {
                                usePrefButton.doClick();
                            }
                            break;
                        case REFSETS:
                            if (refsetToggleButton.isSelected() == state) {
                                // nothing to do...
                            } else {
                                refsetToggleButton.doClick();
                            }
                            break;
                        default:
                            throw new UnsupportedOperationException(" Can't handle toggle: " + toggle);
                    }
                }
            }
        });

    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(30, 30);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
        return 75;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
        return 10;
    }

    @Override
    public boolean getShowRefsets() {
        return refsetToggleButton.isSelected();
    }

    @Override
    public boolean getToggleState(TOGGLES toggle) {
        I_PluginToConceptPanel plugin = config.getConceptPanelPlugin(host_enum, toggle.getPluginId());
        if (plugin != null) {
            for (JComponent component : plugin.getToggleBarComponents()) {
                if (JToggleButton.class.isAssignableFrom(component.getClass())) {
                    JToggleButton toggleButton = (JToggleButton) component;
                    return toggleButton.isSelected();
                }
            }
        } else {
            switch (toggle) {
                case HISTORY:
                    return historyButton.isSelected();
                case PREFERENCES:
                    return usePrefButton.isSelected();
                case REFSETS:
                    return refsetToggleButton.isSelected();
            }
        }
        throw new UnsupportedOperationException(" Can't handle toggle: " + toggle);
    }
}
