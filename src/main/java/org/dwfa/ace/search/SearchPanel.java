package org.dwfa.ace.search;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionsFromCollectionTableModel;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.DescriptionTableModel.DESC_FIELD;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.bpa.util.TableSorter.SortOrder;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinDescVersioned;

public class SearchPanel extends JPanel {

    public class SaveQuery implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                // Create a file dialog box to prompt for a new file to display
                updateExtraCriterion();
                QueryBean qb = new QueryBean(searchPhraseField.getText(), getExtraCriterion());
                FileDialog f = new FileDialog((Frame) SearchPanel.this.getTopLevelAncestor(), "Save query (.query)",
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

        public void actionPerformed(ActionEvent e) {
            try {
                FileDialog dialog = new FileDialog(new Frame(), "Select query");
                File searchFolder = new File("search");
                dialog.setDirectory(searchFolder.getAbsolutePath());
                dialog.setFilenameFilter(new FilenameFilter() {
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
                    model.setDescriptions(new ArrayList<ThinDescVersioned>());
                    //startSearch();
                }

            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    private class SearchSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            // Ignore extra messages.
            if (e.getValueIsAdjusting())
                return;

            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
                // no rows are selected
            } else {
                int selectedRow = lsm.getMinSelectionIndex();
                int modelRow = sortingTable.modelIndex(selectedRow);
                I_DescriptionTuple tuple = model.getDescription(modelRow);
                ConceptBean cb = ConceptBean.get(tuple.getConceptId());
                for (I_ContainTermComponent l : linkedComponents) {
                    l.setTermComponent(cb);
                }

            }
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JTextField searchPhraseField;

    private DescriptionsFromCollectionTableModel model;

    private JButton searchButton;

    private JRadioButton regexRadio;

    private JRadioButton luceneRadio;

    private JButton searchSetting;

    private JButton stopButton;

    private JProgressBar progressBar;

    private JLabel progressInfo;

    private JTableWithDragImage descTable;

    private TableSorter sortingTable;

    private Set<I_ContainTermComponent> linkedComponents = new HashSet<I_ContainTermComponent>();

    private I_ConfigAceFrame config;

    List<CriterionPanel> criterionPanels = new ArrayList<CriterionPanel>();

    private JPanel criterion = new JPanel();

    private JButton loadButton;

    private JButton saveButton;

    private List<I_TestSearchResults> extraCriterion;

    public SearchPanel(I_ConfigAceFrame config) {
        super(new GridBagLayout());
        this.config = config;
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search");
        this.getActionMap().put("search", new AbstractAction("Search on enter") {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

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

        JButton addButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/add2.png")));
        addButton.setIconTextGap(0);
        addButton.addActionListener(new AddCriterion(this));
        add(addButton, gbc);
        gbc.gridx++;
        JButton removeButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/delete2.png")));
        removeButton.setIconTextGap(0);
        removeButton.setEnabled(false);
        add(removeButton, gbc);
        gbc.gridx++;

        gbc.fill = GridBagConstraints.BOTH;
        add(new JLabel(" lucene query: ", JLabel.RIGHT), gbc);

        gbc.weightx = 1;
        gbc.gridx++;
        this.searchPhraseField = new JTextField(40);
        this.searchPhraseField.setDragEnabled(true);
        add(searchPhraseField, gbc);

        gbc.gridx++;
        gbc.weightx = 0;

        regexRadio = new JRadioButton("regex");
        regexRadio.setSelected(false);
        regexRadio.setVisible(false);
        add(regexRadio, gbc);
        gbc.gridy++;
        luceneRadio = new JRadioButton("lucene");
        luceneRadio.setSelected(true);
        luceneRadio.setVisible(false);
        add(luceneRadio, gbc);

        ButtonGroup bg = new ButtonGroup();
        bg.add(regexRadio);
        bg.add(luceneRadio);

        gbc.gridy--;
        gbc.gridx++;
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        add(progressBar, gbc);

        gbc.gridy++;
        progressInfo = new JLabel();
        progressInfo.setVisible(false);
        add(progressInfo, gbc);
        gbc.gridy--;

        // row 0, double height
        gbc.gridheight = 2;
        searchSetting = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/preferences.png")));
        searchSetting.setVisible(false);
        gbc.gridx++;
        add(searchSetting, gbc);

        gbc.gridx++;
        searchButton = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/find.png")));
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

        loadButton = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/read_from_disk.png")));
        loadButton.addActionListener(new LoadQuery());
        add(loadButton, gbc);
        gbc.gridx++;
        saveButton = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/save_to_disk.png")));
        saveButton.addActionListener(new SaveQuery());
        add(saveButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 7;
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

        model = new DescriptionsFromCollectionTableModel(new DESC_FIELD[] { DESC_FIELD.SCORE, DESC_FIELD.TEXT,
                DESC_FIELD.TYPE }, config);
        sortingTable = new TableSorter(model);
        descTable = new JTableWithDragImage(sortingTable);
        descTable.setDragEnabled(true);
        descTable.setTransferHandler(new TerminologyTransferHandler());
        sortingTable.setTableHeader(descTable.getTableHeader());

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
        sortingTable.getTableHeader()
                .setToolTipText("Click to specify sorting; Control-Click to specify secondary sorting");
        sortingTable.setSortingStatus(0, SortOrder.DESCENDING);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 10;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(new JScrollPane(descTable), gbc);
        descTable.getSelectionModel().addListSelectionListener(new SearchSelectionListener());

    }

    public void setQuery(QueryBean qb) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        searchPhraseField.setText(qb.getQueryString());
        criterionPanels.clear();
        AceLog.getAppLog().info("Extra criterion: " + qb.getExtraCriterion());
        for (I_TestSearchResults criterion: qb.getExtraCriterion()) {
            criterionPanels.add(new CriterionPanel(this, criterion));
        }
        layoutCriterion();
    }

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

    public CriterionPanel makeCriterionPanel() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        return new CriterionPanel(this);
    }

    public void setShowProgress(boolean show) {
        searchButton.setVisible(!show);
        stopButton.setVisible(show);
        progressInfo.setVisible(show);
        progressBar.setVisible(show);
        // regexRadio.setVisible(!show);
        // searchSetting.setVisible(!show);
    }

    private void startSearch() {
        updateExtraCriterion();

        if (searchPhraseField.getText().length() > 2) {
            setShowProgress(true);
            model.setDescriptions(new ArrayList<ThinDescVersioned>());
            ACE.threadPool.execute(new SearchStringWorker(this, model, searchPhraseField.getText(), config, luceneRadio
                    .isSelected()));
        } else {
            JOptionPane.showMessageDialog(getRootPane(), "The search string must be longer than 2 characters: "
                    + searchPhraseField.getText(), "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateExtraCriterion() {
        extraCriterion = new ArrayList<I_TestSearchResults>();
        for (CriterionPanel criterionPanel : criterionPanels) {
            extraCriterion.add(criterionPanel.getBean());
        }
    }

    public void setProgressInfo(String string) {
        progressInfo.setText(string);

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
        luceneRadio.setSelected(true);
        searchPhraseField.setText(query);
        this.extraCriterion = extraCriterion;
        startSearch();
    }

    public List<I_TestSearchResults> getExtraCriterion() {
        return extraCriterion;
    }
}
