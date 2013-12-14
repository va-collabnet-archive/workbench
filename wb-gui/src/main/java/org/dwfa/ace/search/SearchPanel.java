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
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractSpinnerModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.TreePath;

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.LINK_TYPE;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.gui.concept.LineagePlugin;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionTableModel.DESC_FIELD;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.ace.table.DescriptionTableRenderer;
import org.dwfa.ace.table.DescriptionsFromCollectionTableModel;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.ace.tree.ExpandPathToNodeStateListener;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concurrent.future.FutureHelper;
import org.ihtsdo.taxonomy.TaxonomyTree;
import org.ihtsdo.taxonomy.model.NodePath;
import org.ihtsdo.taxonomy.model.TaxonomyModel;
import org.ihtsdo.taxonomy.nodes.RootNode;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;
import org.ihtsdo.taxonomy.path.PathExpander;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.util.swing.GuiUtil;

public class SearchPanel extends JPanel implements I_MakeCriterionPanel, I_HostConceptPlugins {

    @Override
    public boolean getShowHistory() {
        throw new UnsupportedOperationException("Not supported yet. 1");
    }

    @Override
    public boolean getShowRefsets() {
        throw new UnsupportedOperationException("Not supported yet. 2");
    }

    @Override
    public boolean getUsePrefs() {
        throw new UnsupportedOperationException("Not supported yet. 3");
    }

    @Override
    public I_GetConceptData getHierarchySelection() {
       return config.getHierarchySelection();
    }

    @Override
    public void unlink() {
        throw new UnsupportedOperationException("Not supported yet. 5");
    }

    @Override
    public void setToggleState(TOGGLES toggle, boolean state) {
        throw new UnsupportedOperationException("Not supported yet. 6");
    }

    @Override
    public boolean getToggleState(TOGGLES toggle) {
        throw new UnsupportedOperationException("Not supported yet. 7");
    }

    @Override
    public void setAllTogglesToState(boolean state) {
        throw new UnsupportedOperationException("Not supported yet. 8");
    }

    @Override
    public void setLinkType(LINK_TYPE link) {
        throw new UnsupportedOperationException("Not supported yet. 9");
    }

    @Override
    public I_AmTermComponent getTermComponent() {
       return termComponent;
    }

    @Override
    public void setTermComponent(I_AmTermComponent termComponent) {
        this.termComponent = (I_GetConceptData) termComponent;
    }

    @Override
    public I_ConfigAceFrame getConfig() {
        return config;
    }

    public class EraseListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            model.setDescriptions(new ArrayList<I_DescriptionVersioned<?>>());
            searchPhraseField.selectAll();
            searchPhraseField.requestFocusInWindow();
        }
    }

    private class FilterSearchActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            config.setSearchWithDescTypeFilter(searchWithDescTypeFilter.isSelected());
        }
    }

    public class MaximizeSearchListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JToggleButton toggle = (JToggleButton) e.getSource();
            criterion.setVisible(!toggle.isSelected());
            addToList.setVisible(!toggle.isSelected());
            showFsn.setVisible(!toggle.isSelected());
            showId.setVisible(!toggle.isSelected());
            showLineage.setVisible(!toggle.isSelected());
            loadButton.setVisible(!toggle.isSelected());
            progressBar.setVisible(!toggle.isSelected());
            saveButton.setVisible(!toggle.isSelected());
            searchButton.setVisible(!toggle.isSelected());
            searchPhraseField.setVisible(!toggle.isSelected());
            if (searchTypeCombo.getSelectedItem().equals(REFSET_QUERY)) {
                searchPhraseField.setVisible(false);
                refsetIdentityField.setVisible(!toggle.isSelected());
            } else {
                searchPhraseField.setVisible(!toggle.isSelected());
                refsetIdentityField.setVisible(false);
            }
            searchTypeCombo.setVisible(!toggle.isSelected());
            addButton.setVisible(!toggle.isSelected());
            removeButton.setVisible(!toggle.isSelected());
            linkSpinner.setVisible(!toggle.isSelected());
            showHistory.setVisible(!toggle.isSelected());
            searchWithDescTypeFilter.setVisible(!toggle.isSelected());
            eraseButton.setVisible(!toggle.isSelected());
            for (CriterionPanel test : criterionPanels) {
                test.setVisible(toggle.isSelected());
            }
        }
    }

    public class AddToList implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                JList conceptList = config.getBatchConceptList();
                I_ModelTerminologyList conceptListModel = (I_ModelTerminologyList) conceptList.getModel();

                HashSet<Integer> conceptsAdded = new HashSet<Integer>();
                for (int i = 0; i < model.getRowCount(); i++) {
                    I_DescriptionTuple desc = model.getDescription(i);
                    if (conceptsAdded.contains(desc.getConceptNid()) == false) {
                        conceptsAdded.add(desc.getConceptNid());
                        I_GetConceptData cb = Terms.get().getConcept(desc.getConceptNid());
                        conceptListModel.addElement(cb);
                    }
                }
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }
    public class ShowFsn implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            TableColumnModel columnModel = descTable.getColumnModel();
            boolean remove = false;
            Enumeration<TableColumn> columns = columnModel.getColumns();
            TableColumn col = columns.nextElement();
            int columnIndex = 0;
            int i = 0;
            while(col != null){
                if(col.getModelIndex() == fsnModelIndex){
                    remove = true;
                    columnIndex = i;
                }
                if(columns.hasMoreElements()){
                    col = columns.nextElement();
                }else{
                    col = null;
                }
                
                i++;
            }
            if(remove){
                TableColumn column = columnModel.getColumn(columnIndex);
                columnModel.removeColumn(columnModel.getColumn(columnIndex));
            }else{
                TableColumn column = new TableColumn(fsnModelIndex);

                DESC_FIELD[] columnEnums = model.getColumnEnums();
                DESC_FIELD columnDesc = columnEnums[fsnModelIndex];
                column.setIdentifier(columnDesc);
                column.setHeaderValue(columnDesc.getColumnName());
                column.setPreferredWidth(columnDesc.getPref());
                column.setMaxWidth(columnDesc.getMax());
                column.setMinWidth(columnDesc.getMin());

                columnModel.addColumn(column);
            }
        }
    }
    
    public class ShowConceptId implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            TableColumnModel columnModel = descTable.getColumnModel();
            boolean remove = false;
            Enumeration<TableColumn> columns = columnModel.getColumns();
            TableColumn col = columns.nextElement();
            int columnIndex = 0;
            int i = 0;
            while(col != null){
                if(col.getModelIndex() == idModelIndex){
                    remove = true;
                    columnIndex = i;
                }
                if(columns.hasMoreElements()){
                    col = columns.nextElement();
                }else{
                    col = null;
                }
                
                i++;
            }
            if(remove){
                TableColumn column = columnModel.getColumn(columnIndex);
                columnModel.removeColumn(columnModel.getColumn(columnIndex));
            }else{
                TableColumn column = new TableColumn(idModelIndex);

                DESC_FIELD[] columnEnums = model.getColumnEnums();
                DESC_FIELD columnDesc = columnEnums[idModelIndex];
                column.setIdentifier(columnDesc);
                column.setHeaderValue(columnDesc.getColumnName());
                column.setPreferredWidth(columnDesc.getPref());
                column.setMaxWidth(columnDesc.getMax());
                column.setMinWidth(columnDesc.getMin());

                columnModel.addColumn(column);
            }
        }
    }
    
    public class ShowLineageView implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                boolean visible = lineageScrollPane.isVisible();
                lineageScrollPane.setVisible(!visible);
                GuiUtil.tickle(SearchPanel.this);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    public class SaveQuery implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Create a file dialog box to prompt for a new file to display
                updateExtraCriterion();
                QueryBean qb = new QueryBean(searchPhraseField.getText(), extraCriterion);
                FileDialog f =
                        new FileDialog((Frame) SearchPanel.this.getTopLevelAncestor(), "Save query (.query)",
                        FileDialog.SAVE);
                File searchFolder = new File("search");
                f.setDirectory(searchFolder.getAbsolutePath());
                f.setVisible(true); // Display dialog and wait for response
                if (f.getFile() != null) {
                    String fileName = f.getFile();
                    if (fileName.endsWith(".query") == false) {
                        fileName = fileName + ".query";
                    }
                    File processBinaryFile = new File(f.getDirectory(), fileName);
                    FileOutputStream fos = new FileOutputStream(processBinaryFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(qb);
                    oos.close();
                }
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    public class LoadQuery implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                FileDialog dialog = new FileDialog(new Frame(), "Select query");
                File searchFolder = new File("search");
                dialog.setDirectory(searchFolder.getAbsolutePath());
                dialog.setFilenameFilter(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".query");
                    }
                });
                dialog.setVisible(true);
                if (dialog.getFile() != null) {
                    File selectedFile = new File(dialog.getDirectory(), dialog.getFile());
                    FileInputStream fis = new FileInputStream(selectedFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    QueryBean qb = (QueryBean) ois.readObject();
                    ois.close();
                    setQuery(qb);
                    model.setDescriptions(new ArrayList<I_DescriptionVersioned<?>>());
                    // startSearch();
                }

            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    private class SearchSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            try {
                // Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                    lineageScrollPane.setVisible(false);
                    // no rows are selected
                } else {
                    int viewRowIndex = lsm.getMinSelectionIndex();
                    lastSelectedRow = viewRowIndex;
                    int modelRow = descTable.convertRowIndexToModel(viewRowIndex);
                    I_DescriptionTuple tuple = model.getDescription(modelRow);
                    I_GetConceptData cb = Terms.get().getConcept(tuple.getConceptNid());
                    for (I_ContainTermComponent l : linkedComponents) {
                        l.setTermComponent(cb);
                    }
                    if (linkType == LINK_TYPE.TREE_LINK) {
                        TaxonomyModel model = (TaxonomyModel) ace.getTree().getModel();
                        RootNode root = model.getRoot();
                        for (Long childNodeId : root.children) {
                            TaxonomyNode childNode = model.getNodeStore().get(childNodeId);
                            TreePath path = NodePath.getTreePath(model, childNode);
                            ace.getTree().collapsePath(path);
                        }

                        PathExpander epl = new PathExpander(ace.getTree(), config,
                                (ConceptChronicleBI) termComponent);
                        config.setHierarchySelection((I_GetConceptData) termComponent);
                        FutureHelper.addFuture(ace.threadPool.submit(epl));
                    }
                    setTermComponent(cb);
                }
                lineagePlugin.update();
            } catch (TerminologyException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            } catch (IOException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }
    }

    private class LinkListModel extends AbstractSpinnerModel {

        ImageIcon[] items;
        int currentSelection = 0;

        public LinkListModel(ImageIcon[] items) {
            this(items, 0);
        }

        public LinkListModel(ImageIcon[] items, int currentSelection) {
            super();
            this.items = items;
            this.currentSelection = currentSelection;
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
                if (value == ConceptPanel.TREE_LINK_ICON) {
                    setToolTipText("The search selection is linked to the hierarchy view...");
                    linkType = LINK_TYPE.TREE_LINK;
                } else if (value == ConceptPanel.UNLINKED_ICON) {
                    setToolTipText("This search selection is not linked to the hierarchy view...");
                    linkType = LINK_TYPE.UNLINKED;
                }
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JSpinner mySpinner = (JSpinner) (e.getSource());
            LinkListModel myModel = (LinkListModel) (mySpinner.getModel());
            setIcon((Icon) myModel.getValue());
            updateToolTipText(mySpinner);
        }
    }
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JTextField searchPhraseField;
    private TermComponentLabel refsetIdentityField;
    private DescriptionsFromCollectionTableModel model;
    private JButton searchButton;
    private static final String LUCENE_QUERY = "lucene query";
    private static final String REGEX_QUERY = "regex query";
    private static final String REFSET_QUERY = "refset query";
    private static final String[] QUERY_TYPES = {LUCENE_QUERY, REGEX_QUERY, REFSET_QUERY};
    private JComboBox searchTypeCombo;
    private JButton searchSetting;
    private JButton stopButton;
    private JProgressBar progressBar;
    private JTableWithDragImage descTable;
    private Set<I_ContainTermComponent> linkedComponents = new HashSet<I_ContainTermComponent>();
    private I_ConfigAceFrame config;
    List<CriterionPanel> criterionPanels = new ArrayList<CriterionPanel>();
    private JPanel criterion = new JPanel();
    private JButton loadButton;
    private JButton saveButton;
    private JToggleButton showHistory;
    private JToggleButton searchWithDescTypeFilter;
    private int fsnModelIndex;
    private int idModelIndex;
    private I_GetConceptData termComponent;

    private List<I_TestSearchResults> extraCriterion;
    private JButton showId;
    private JButton showFsn;
    private JButton showLineage;
    private JButton addToList;
    private JButton addButton;
    private JButton removeButton;
    private JButton eraseButton;
    private LINK_TYPE linkType = LINK_TYPE.UNLINKED;
    private JSpinner linkSpinner;
    private int lastSelectedRow = -1;
    private LineagePlugin lineagePlugin = new LineagePlugin(true, 0);
    private JScrollPane lineageScrollPane;
    private ACE ace;

    public SearchPanel(I_ConfigAceFrame config, ACE ace) throws TerminologyException, IOException {
        super(new GridBagLayout());
        this.ace = ace;
        this.config = config;
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search");
        this.getActionMap().put("search", new AbstractAction("Search on enter") {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent evt) {
                startSearch();
            }
        });

        Border b = BorderFactory.createEmptyBorder(5, 5, 0, 0);
        setBorder(b);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;

        addButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/add2.png")));
        addButton.setIconTextGap(0);
        addButton.addActionListener(new AddCriterion(this));
        add(addButton, gbc);
        addButton.setToolTipText("add a new AND search clause to end of query");
        gbc.gridx++;
        removeButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/delete2.png")));
        removeButton.setIconTextGap(0);
        removeButton.setEnabled(false);
        add(removeButton, gbc);
        gbc.gridx++;

        gbc.fill = GridBagConstraints.BOTH;
        searchTypeCombo = new JComboBox(QUERY_TYPES) {

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
        searchTypeCombo.setSelectedItem(LUCENE_QUERY);
        searchTypeCombo.setMinimumSize(new Dimension(175, 20));
        add(searchTypeCombo, gbc);
        searchTypeCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (searchTypeCombo.getSelectedItem().equals(REFSET_QUERY)) {
                    searchPhraseField.setVisible(false);
                    refsetIdentityField.setVisible(true);
                    refsetIdentityField.revalidate();
                    revalidate();
                } else {
                    searchPhraseField.setVisible(true);
                    refsetIdentityField.setVisible(false);
                }

            }
        });

        gbc.weightx = 1;
        gbc.gridx++;
        this.searchPhraseField = new JTextField(200);
        this.searchPhraseField.setDragEnabled(false);
        this.searchPhraseField.setMinimumSize(new Dimension(400, 20));
        this.searchPhraseField.setText("search");
        add(searchPhraseField, gbc);

        refsetIdentityField = new TermComponentLabel();
        refsetIdentityField.setVisible(false);
        refsetIdentityField.setMinimumSize(new Dimension(400, 20));
        add(refsetIdentityField, gbc);

        gbc.gridx++;
        gbc.gridheight = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        eraseButton = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/delete2.png")));
        eraseButton.setToolTipText("Erase the search results, and select & focus on search text.");
        eraseButton.setVisible(true);
        eraseButton.addActionListener(new EraseListener());
        add(eraseButton, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx++;
        gbc.gridheight = 1;

        gbc.weightx = 0.75;
        progressBar = new JProgressBar();
        progressBar.setVisible(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        progressBar.setMinimumSize(new Dimension(300, 20));
        add(progressBar, gbc);

        gbc.weightx = 0;

        gbc.fill = GridBagConstraints.NONE;

        // row 0, double height
        gbc.gridheight = 2;

        searchSetting = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/preferences.png")));
        searchSetting.setVisible(false);
        gbc.gridx++;
        add(searchSetting, gbc);

        gbc.gridx++;
        searchButton = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/gear_find.png")));
        searchButton.addActionListener(getActionMap().get("search"));
        searchButton.setToolTipText("perform a search");
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(searchButton, gbc);

        stopButton = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/stop.png")));
        stopButton.setVisible(false);
        stopButton.setToolTipText("stop the current search");
        // stopButton.setBorder(BorderFactory.createLineBorder(Color.red));
        add(stopButton, gbc);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx++;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;

        gbc.fill = GridBagConstraints.BOTH;

        List<ImageIcon> ImageIconList = new ArrayList<ImageIcon>();
        ImageIconList.add(ConceptPanel.UNLINKED_ICON);
        ImageIconList.add(ConceptPanel.TREE_LINK_ICON);

        LinkListModel linkSpinnerModel =
                new LinkListModel(ImageIconList.toArray(new ImageIcon[ImageIconList.size()]), LINK_TYPE.UNLINKED.ordinal());

        linkSpinner = new JSpinner(linkSpinnerModel);
        linkSpinner.setBorder(BorderFactory.createEmptyBorder(3, 3, 2, 5));

        linkSpinner.setEditor(new LinkEditor(linkSpinner));

        add(linkSpinner, gbc);

        gbc.gridx++;
        showHistory = new JToggleButton(new ImageIcon(ACE.class.getResource("/24x24/plain/history.png")));
        showHistory.setToolTipText("<html>when selected, shows descriptions with any status value (including"
                + "<br>historical descriptions). When not selected, shows only the descriptions "
                + "<br>with the allowed status values set in the preferences.");
        add(showHistory, gbc);
        gbc.gridx++;

        searchWithDescTypeFilter =
                new JToggleButton(new ImageIcon(ACE.class.getResource("/24x24/plain/component_preferences.png")));
        searchWithDescTypeFilter.setToolTipText("filter search using preferences");
        searchWithDescTypeFilter.setSelected(config.searchWithDescTypeFilter());
        searchWithDescTypeFilter.addActionListener(new FilterSearchActionListener());
        add(searchWithDescTypeFilter, gbc);
        gbc.gridx++;

        loadButton = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/read_from_disk.png")));
        loadButton.setToolTipText("read search specification from disk");
        loadButton.addActionListener(new LoadQuery());
        add(loadButton, gbc);
        gbc.gridx++;
        saveButton = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/save_to_disk.png")));
        saveButton.setToolTipText("save search specification to disk");
        saveButton.addActionListener(new SaveQuery());
        add(saveButton, gbc);

        gbc.gridx++;
        addToList = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/notebook_add.png")));
        addToList.addActionListener(new AddToList());
        addToList.setToolTipText("add concepts from search results to list view");
        add(addToList, gbc);
        
        gbc.gridx++;
        showFsn = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/truck_red.png")));
        showFsn.addActionListener(new ShowFsn());
        showFsn.setToolTipText("show fsn for concept that contains description");
        add(showFsn, gbc);
        
        gbc.gridx++;
        showId = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/id_card.png")));
        showId.addActionListener(new ShowConceptId());
        showId.setToolTipText("show concept SCTID (UUID)");
        add(showId, gbc);
        
        gbc.gridx++;
        showLineage = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/nav_up_right_green.png")));
        showLineage.addActionListener(new ShowLineageView());
        showLineage.setToolTipText("show lineage for conept that contains description");
        add(showLineage, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        gbc.gridheight = 3;

        add(criterion, gbc);

        gbc.gridy = gbc.gridy + gbc.gridheight;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        // Results below...
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridheight = 1;
        
        model =
                new DescriptionsFromCollectionTableModel(new DESC_FIELD[]{DESC_FIELD.SCORE, DESC_FIELD.STATUS,
                    DESC_FIELD.TEXT, DESC_FIELD.TYPE, DESC_FIELD.DESC_FSN, DESC_FIELD.CONCEPT_ID}, config);
        descTable = new JTableWithDragImage(model);
        descTable.setAutoCreateColumnsFromModel(true);
        DESC_FIELD[] columns = model.getColumnEnums();
        int modelIndex = 0;
        for(DESC_FIELD column : columns){
            if(column.equals(DESC_FIELD.DESC_FSN)){
                fsnModelIndex = modelIndex;
            }
            if(column.equals(DESC_FIELD.CONCEPT_ID)){
                idModelIndex = modelIndex;
            }
            modelIndex++;
        }
        descTable.getColumnModel().removeColumn(descTable.getColumnModel().getColumn(5));
        descTable.getColumnModel().removeColumn(descTable.getColumnModel().getColumn(4));
        
        //SortClickListener.setupSorter(descTable); //replaced by auto sort capable sorter below
        TableRowSorter<DescriptionsFromCollectionTableModel> trs = new TableRowSorter<DescriptionsFromCollectionTableModel>(model);
        trs.setSortsOnUpdates(true);
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        trs.setSortKeys(sortKeys);        
        descTable.setRowSorter(trs);
        
        descTable.setDragEnabled(false);
        descTable.setTransferHandler(new TerminologyTransferHandler(this));
        DescriptionTableRenderer renderer = new DescriptionTableRenderer(config, true);
        descTable.setDefaultRenderer(Number.class, renderer);
        descTable.setDefaultRenderer(StringWithDescTuple.class, renderer);
        descTable.setDefaultRenderer(String.class, renderer);
        descTable.setDefaultRenderer(Boolean.class, renderer);
        descTable.addMouseListener(new DescSearchResultsTablePopupListener(config, ace, searchPanelId));

        DESC_FIELD[] columnEnums = model.getColumnEnums();

        for (int i = 0; i < descTable.getColumnCount(); i++) {
            TableColumn column = descTable.getColumnModel().getColumn(i);
            DESC_FIELD columnDesc = columnEnums[i];
            column.setIdentifier(columnDesc);
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
        }

        // Set up tool tips for column headers.
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 17;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JScrollPane scrollPane = new JScrollPane(descTable);
        JToggleButton maximizeTable =
                new JToggleButton(new ImageIcon(ACE.class.getResource("/16x16/plain/fit_to_size.png")));
        maximizeTable.setToolTipText("show/hide search criterion");
        maximizeTable.setSelected(false);
        maximizeTable.addActionListener(new MaximizeSearchListener());

        scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, maximizeTable);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitPane.setTopComponent(scrollPane);
        descTable.getSelectionModel().addListSelectionListener(new SearchSelectionListener());
        JComponent lineageComponent = lineagePlugin.getComponent(this);
        lineageScrollPane = new JScrollPane(lineageComponent);
        lineageScrollPane.setVisible(false);
        lineageScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        splitPane.setBottomComponent(lineageScrollPane);
        add(splitPane, gbc);

    }

    public void setQuery(QueryBean qb) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        searchPhraseField.setText(qb.getQueryString());
        criterionPanels.clear();
        for (I_TestSearchResults queryCriterion : qb.getExtraCriterion()) {
            criterionPanels.add(new CriterionPanel(this, queryCriterion));
        }
        layoutCriterion();
    }

    @Override
    public void layoutCriterion() {
        criterion.removeAll();
        criterion.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0;

        for (JPanel criterionPanel : criterionPanels) {
            criterion.add(criterionPanel, gbc);
            criterionPanel.invalidate();
            criterionPanel.validate();
            criterionPanel.doLayout();
            gbc.gridy++;
        }
        criterion.invalidate();
        criterion.validate();
        criterion.doLayout();

        this.invalidate();
        this.validate();
        this.doLayout();
    }

    @Override
    public CriterionPanel makeCriterionPanel() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return new CriterionPanel(this);
    }

    public void setShowProgress(boolean show) {
        searchButton.setVisible(!show);
        stopButton.setVisible(show);
        // progressBar.setVisible(show);
        // regexRadio.setVisible(!show);
        // searchSetting.setVisible(!show);
    }

    private void startSearch() {
        lastSelectedRow = -1;
        updateExtraCriterion();

        if (searchTypeCombo.getSelectedItem().equals(REFSET_QUERY)) {
            if (refsetIdentityField.getTermComponent() == null) {
                return;
            }
            setShowProgress(true);
            model.setDescriptions(new ArrayList<I_DescriptionVersioned<?>>());
            ACE.threadPool.execute(new SearchRefsetWorker(this, model,
                    (I_GetConceptData) refsetIdentityField.getTermComponent(), config));

        } else if (searchPhraseField.getText().length() > 1) {
            if (checkLuceneQuery(searchPhraseField.getText())) {
                setShowProgress(true);
                model.setDescriptions(new ArrayList<I_DescriptionVersioned<?>>());
                ACE.threadPool.execute(new SearchStringWorker(this, model, searchPhraseField.getText(), config,
                        searchTypeCombo.getSelectedItem().equals(LUCENE_QUERY)));
            }
        } else if (searchPhraseField.getText().length() == 0) {
            if (this.extraCriterion.size() > 0) {
                setShowProgress(true);
                model.setDescriptions(new ArrayList<I_DescriptionVersioned<?>>());
                ACE.threadPool.execute(new SearchAllWorker(this, model, config));
            } else {
                JOptionPane.showMessageDialog(getRootPane(),
                        "<html>Unindexed search (a search with an empty query string),<br>"
                        + "requires at least one advanced search criterion. ", "Search Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(getRootPane(), "The search string must be longer than 1 character: "
                    + searchPhraseField.getText(), "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateExtraCriterion() {
        extraCriterion = new ArrayList<I_TestSearchResults>();
        for (CriterionPanel criterionPanel : criterionPanels) {
            I_TestSearchResults test = criterionPanel.getBean();
            if (test != null) {
                extraCriterion.add(test);
            }
        }
    }

    public void setProgressInfo(String string) {
        progressBar.setStringPainted(true);
        progressBar.setString(string);
    }

    public void setProgressIndeterminate(boolean b) {
        progressBar.setIndeterminate(b);
    }

    public void setProgressMaximum(int descCount) {
        progressBar.setMaximum(descCount);
    }

    public void setProgressValue(int i) {
        progressBar.setValue(i);
    }

    public int getProgressMaximum() {
        return progressBar.getMaximum();
    }

    public int getProgressValue() {
        return progressBar.getValue();
    }

    public void addStopActionListener(ActionListener stopListener) {
        stopButton.addActionListener(stopListener);

    }

    public void removeStopActionListener(ActionListener stopListener) {
        stopButton.removeActionListener(stopListener);
    }

    public void addLinkedComponent(I_ContainTermComponent component) {
        linkedComponents.add(component);
    }

    public void removeLinkedComponent(I_ContainTermComponent component) {
        linkedComponents.remove(component);
    }

    public void performLuceneSearch(String query, List<I_TestSearchResults> extraCriterion) {
        if (checkLuceneQuery(query)) {
            searchTypeCombo.setSelectedItem(LUCENE_QUERY);
            searchPhraseField.setText(query);
            this.extraCriterion = extraCriterion;
            startSearch();
        }
    }

    private boolean checkLuceneQuery(String query) {
        if (query != null && query.length() > 0) {
            // check for short wildcard
            if (query.contains("*")) {
                String[] queryParts = query.split(" ");
                for (int i = 0; i < queryParts.length; i++) {
                    if (queryParts[i].contains("*")) {
                        if (queryParts[i].length() < 4) {
                            JOptionPane.showMessageDialog(this, "The wildcard clause '" + queryParts[i]
                                    + "' must start with at least 3 characters before the *.");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public List<I_TestSearchResults> getExtraCriterion() {
        List<I_TestSearchResults> extraCriterionCopy = new ArrayList<I_TestSearchResults>(extraCriterion);
        if (showHistory.isSelected() == false) {
            extraCriterionCopy.add(new ActiveConceptAndDescTest());
        }
        return extraCriterionCopy;
    }

    public void changeLinkListener(LINK_TYPE type) {
        this.linkType = type;
    }

    public I_DescriptionTuple getSearchResultsSelection() {
        int selectedRow = lastSelectedRow;
        if (descTable.getSelectedRow() >= 0) {
            selectedRow = descTable.getSelectedRow();
        }
        if (selectedRow >= 0) {
            int modelRow = descTable.convertRowIndexToModel(selectedRow);
            I_DescriptionTuple tuple = model.getDescription(modelRow);
            return tuple;
        } else {
            return null;
        }
    }

    @Override
    public List<CriterionPanel> getCriterionPanels() {
        return criterionPanels;
    }

    public void focusOnInput() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (searchPhraseField.isVisible()) {
                    searchPhraseField.selectAll();
                    searchPhraseField.requestFocusInWindow();
                }
            }
        });
    }
}
