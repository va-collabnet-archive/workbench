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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractSpinnerModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_HostConceptPlugins.LINK_TYPE;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.bpa.util.SortClickListener;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel;
import org.ihtsdo.ace.table.WorkflowHistoryTableRenderer;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel.WORKFLOW_FIELD;
import org.ihtsdo.ace.task.search.I_TestWorkflowHistorySearchResults;

public class WorkflowHistorySearchPanel extends JPanel implements I_MakeCriterionPanel {

	protected static final String DEFAULT_TIME_STAMP = "MM/dd/yyyy";

    public class EraseListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //model.setDescriptions(new ArrayList<I_DescriptionVersioned>());
        }

    }

    private class FilterSearchActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            config.setSearchWithDescTypeFilter(searchWithDescTypeFilter.isSelected());
        }
    }

    public class MaximizeSearchListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JToggleButton toggle = (JToggleButton) e.getSource();
            criterion.setVisible(!toggle.isSelected());
            addToList.setVisible(!toggle.isSelected());
            loadButton.setVisible(!toggle.isSelected());
            progressBar.setVisible(!toggle.isSelected());
            saveButton.setVisible(!toggle.isSelected());
            searchButton.setVisible(!toggle.isSelected());
            addButton.setVisible(!toggle.isSelected());
            removeButton.setVisible(!toggle.isSelected());
            linkSpinner.setVisible(!toggle.isSelected());
            showHistory.setVisible(!toggle.isSelected());
            searchWithDescTypeFilter.setVisible(!toggle.isSelected());
            //eraseButton.setVisible(!toggle.isSelected());
            for (WorkflowHistoryCriterionPanel test : criterionPanels) {
                test.setVisible(toggle.isSelected());
            }
        }

    }

    public class AddToList implements ActionListener {


        public void actionPerformed(ActionEvent e) {
        }

    }

    public class SaveQuery implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                // Create a file dialog box to prompt for a new file to display
                updateExtraCriterion();
                FileDialog f = new FileDialog((Frame) WorkflowHistorySearchPanel.this.getTopLevelAncestor(), "Save query (.query)",
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
                }

            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    private class SearchSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            try {
				// Ignore extra messages.
				if (e.getValueIsAdjusting())
				    return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
				    // no rows are selected
				} else {
				    int viewRowIndex = lsm.getMinSelectionIndex();
				    lastSelectedRow = viewRowIndex;
				}
			} catch (Exception e1) {
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

        public Object getNextValue() {
            currentSelection++;
            if (currentSelection >= items.length) {
                currentSelection = 0;
            }
            return getValue();
        }

        public Object getPreviousValue() {
            currentSelection--;
            if (currentSelection < 0) {
                currentSelection = items.length - 1;
            }
            return getValue();
        }

        public Object getValue() {
            return items[currentSelection];
        }

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

	private static final int EXPECTED_YEAR_MONTH = 0;
	private static final int EXPECTED_YEAR_DATE = 1;
	private static final int EXPECTED_YEAR_LOCATION = 2;
	private static final int REQUIRED_DATE_LENGTH = 2;
	private static final int REQUIRED_YEAR_LENGTH = 4;
	private static final int REQUIRED_MONTH_LENGTH = 2;
	private static final int EXPECTED_DATE_PARTS = 3;

    private WorkflowHistoryTableModel model;

    private JButton searchButton;

    private JButton searchSetting;

    private JButton stopButton;

    private JProgressBar progressBar;

    private JTableWithDragImage WfHistoryTable;

    private Set<I_ContainTermComponent> linkedComponents = new HashSet<I_ContainTermComponent>();

    private I_ConfigAceFrame config;

    List<WorkflowHistoryCriterionPanel> criterionPanels = new ArrayList<WorkflowHistoryCriterionPanel>();

    private JPanel criterion = new JPanel();

    private JButton loadButton;

    private JButton saveButton;

    private JToggleButton showHistory;

    private JToggleButton searchWithDescTypeFilter;;

    private List<I_TestWorkflowHistorySearchResults> extraCriterion;

    private JButton addToList;

    private JButton addButton;

    private JButton removeButton;

    //private JButton eraseButton;

    private LINK_TYPE linkType = LINK_TYPE.UNLINKED;

    private JSpinner linkSpinner;

    private JCheckBox workflowInProgress;
    private JCheckBox workflowCompleted;
    private JCheckBox pastReleases;
    private String 	  timestampBefore = null;
    private String    timestampAfter = null;

    private int lastSelectedRow = -1;

    public WorkflowHistorySearchPanel(I_ConfigAceFrame config, ACE ace) {
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





        gbc.gridx++;

        workflowInProgress = new JCheckBox();
        workflowInProgress.setText("Search Workflows In Progress");
        workflowInProgress.setSelected(true);
        workflowInProgress.setToolTipText("<html>Searches workflows that have yet to be completed</html>");
        add(workflowInProgress, gbc);
        gbc.gridx++;

        workflowCompleted = new JCheckBox();
        workflowCompleted.setText("Search Completed Workflows");
        workflowCompleted.setSelected(false);
        workflowCompleted.setToolTipText("<html>Searches workflows that have been completed</html>");
        add(workflowCompleted, gbc);
        gbc.gridx++;

        pastReleases = new JCheckBox();
        pastReleases.setText("Search Workflows From Past Releases");
        pastReleases.setSelected(false);
        pastReleases.setToolTipText("<html>Searches workflows from prior releases of SNOMED</html>");
        add(pastReleases, gbc);




        gbc.gridx++;
        gbc.gridheight = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

/*
        eraseButton = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/delete2.png")));
        eraseButton.setToolTipText("Erase the search results, and select & focus on search text.");
        eraseButton.setVisible(true);
        eraseButton.addActionListener(new EraseListener());
        add(eraseButton, gbc);
 */

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

        LinkListModel linkSpinnerModel = new LinkListModel(ImageIconList.toArray(new ImageIcon[ImageIconList.size()]),
            LINK_TYPE.UNLINKED.ordinal());

        linkSpinner = new JSpinner(linkSpinnerModel);
        linkSpinner.setBorder(BorderFactory.createEmptyBorder(3, 3, 2, 5));

        linkSpinner.setEditor(new LinkEditor(linkSpinner));

        add(linkSpinner, gbc);

        /*
        showHistory = new JToggleButton(new ImageIcon(ACE.class.getResource("/24x24/plain/history.png")));
        showHistory.setToolTipText("<html>when selected, shows descriptions with any status value (including" +
        		                     "<br>historical descriptions). When not selected, shows only the descriptions " +
        		                     "<br>with the allowed status values set in the preferences.");
        add(showHistory, gbc);
        gbc.gridx++;

        searchWithDescTypeFilter = new JToggleButton(new ImageIcon(
            ACE.class.getResource("/24x24/plain/component_preferences.png")));
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
*/
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        gbc.gridheight = 3;

        add(criterion, gbc);

        gbc.gridy = gbc.gridy + gbc.gridheight;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridy++;
        gbc.gridheight = 1;

         if (getWorkflowHistoryCriterionPanels() != null) {
            getWorkflowHistoryCriterionPanels().add(makeCriterionPanel());
         }
        	layoutCriterion();


		// Results below...
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridheight = 1;

		model = new WorkflowHistoryTableModel(new WORKFLOW_FIELD[] { WORKFLOW_FIELD.FSN, 
																	 WORKFLOW_FIELD.EDITOR, 
																	 WORKFLOW_FIELD.STATE, 
																	 WORKFLOW_FIELD.TIMESTAMP}, 
																	 config);
		/*
		 *         model = new WorkflowHistoryTableModel(new WORKFLOW_FIELD[] {
		 *       		WORKFLOW_FIELD.FSN, 	WORKFLOW_FIELD.ACTION, 	WORKFLOW_FIELD.STATE,
		 *       		WORKFLOW_FIELD.EDITOR,	WORKFLOW_FIELD.PATH, 	WORKFLOW_FIELD.TIMESTAMP},
		 *       		config);
		 */

        WfHistoryTable = new JTableWithDragImage(model);
        WfHistoryTable.setAutoCreateColumnsFromModel(true);
        SortClickListener.setupSorter(WfHistoryTable);
        WfHistoryTable.setDragEnabled(true);
        WfHistoryTable.setTransferHandler(new TerminologyTransferHandler(this));
        WorkflowHistoryTableRenderer renderer = new WorkflowHistoryTableRenderer(config, true);
        WfHistoryTable.setDefaultRenderer(Number.class, renderer);
        WfHistoryTable.setDefaultRenderer(String.class, renderer);
        WfHistoryTable.setDefaultRenderer(Boolean.class, renderer);
        WfHistoryTable.addMouseListener(new DescSearchResultsTablePopupListener(config, ace, workflowHistorySearchPanelId));
        
        WORKFLOW_FIELD[] columnEnums = model.getColumnEnums();

        for (int i = 0; i < WfHistoryTable.getColumnCount(); i++) {
            TableColumn column = WfHistoryTable.getColumnModel().getColumn(i);
            WORKFLOW_FIELD columnDesc = columnEnums[i];
            column.setIdentifier(columnDesc);
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
        }

        // Set up tool tips for column headers.
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 14;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JScrollPane scrollPane = new JScrollPane(WfHistoryTable);
        JToggleButton maximizeTable = new JToggleButton(new ImageIcon(
            ACE.class.getResource("/16x16/plain/fit_to_size.png")));
        maximizeTable.setToolTipText("show/hide search criterion");
        maximizeTable.setSelected(false);
        maximizeTable.addActionListener(new MaximizeSearchListener());

        scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, maximizeTable);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, gbc);
        WfHistoryTable.getSelectionModel().addListSelectionListener(new SearchSelectionListener());

    }

    public void setQuery(QueryBean qb) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        criterionPanels.clear();
//        for (I_TestWorkflowHistorySearchResults criterion : qb.getExtraCriterion()) {
//            criterionPanels.add(new WorkflowHistoryCriterionPanel(this, criterion));
//        }
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

   @Override
    public WorkflowHistoryCriterionPanel makeCriterionPanel() {
        return new WorkflowHistoryCriterionPanel(this);
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
        if (updateExtraCriterion()) {
		    setShowProgress(true);
		    ACE.threadPool.execute(new SearchWfHistoryStringWorker(this, model, config, this.workflowInProgress.isSelected(), this.workflowCompleted.isSelected(), this.pastReleases.isSelected(), timestampBefore, timestampAfter));
        }
    }

    private boolean updateExtraCriterion() {
        extraCriterion = new ArrayList<I_TestWorkflowHistorySearchResults>();
        boolean timestampPassed = true;
        
        for (WorkflowHistoryCriterionPanel criterionPanel : criterionPanels) 
        {
            I_TestWorkflowHistorySearchResults test = criterionPanel.getBean();
            
            if (test != null) 
            {
	    		// Test to see if time is properly formatted
	        	if (test.getTestType() == I_TestWorkflowHistorySearchResults.timestampBefore) 
	            {
	            	timestampBefore = (String)test.getTestValue();
	            	timestampPassed = testDateFormat(timestampBefore);
	            }
	            else if (test.getTestType() == I_TestWorkflowHistorySearchResults.timestampAfter)
	            {
	            	timestampAfter = (String)test.getTestValue();
	            	timestampPassed = testDateFormat(timestampAfter);
	            }
            	
                extraCriterion.add(test);
            }
        }
        
       	return timestampPassed;

    }

    private boolean testDateFormat(String s) {

    	try {
    		// Simple two day, two month, four year date format
	    	SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TIME_STAMP);
	    	sdf.setLenient(false);
	    	Date d = sdf.parse(s);
	    	
	    	String[] dateParts = s.split("/");
	    	if (dateParts.length != EXPECTED_DATE_PARTS)
	    		throw new ParseException("Error: Month, Date, and Year must be specified and seperated by '/'", 0);
	    	if (hasNonNumericValue(dateParts))
	    		throw new ParseException("Error: Only digits seperated by '/' are allowed", 0);
	    	if (dateParts[EXPECTED_YEAR_LOCATION].length() != REQUIRED_YEAR_LENGTH)
	    		throw new ParseException("Error: Year must be four digits", 0);
	    	if (dateParts[EXPECTED_YEAR_DATE].length() != REQUIRED_DATE_LENGTH)
	    		throw new ParseException("Error: Day must be two digits", 0);
			if (dateParts[EXPECTED_YEAR_MONTH].length() != REQUIRED_MONTH_LENGTH)
	    		throw new ParseException("Error: Month must be two digits", 0);

	    	return true;

    	} catch (ParseException e) {
            // TODO: Request to make the informing mechanism non-Module.   
    		AlertToWfHxSearchFailure alerter = new AlertToWfHxSearchFailure(AlertToWfHxSearchFailure.ALERT_TYPE.INFORMATIONAL, "Error parsing date criterion", e.getMessage());

            WorkflowAlerter alert = new WorkflowAlerter(alerter);
            alert.alert();
            
            //AceLog.getAppLog().alertAndLogException(e);
		}
    	
		return false;
    }
    
    
    
    private boolean hasNonNumericValue(String[] dateParts) {
    	for (int i = 0; i < dateParts.length; i++)
    	{
    		String part = dateParts[i];
    		
    		for (int j = 0; j < part.length(); j++)
    		{
    			if (!Character.isDigit(part.charAt(j)))
    				return true;
    		}
    	}

    	return false;
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

    public void performWorkflowHistorySearch(String query, List<I_TestWorkflowHistorySearchResults> extraCriterion) throws Exception {
        if (checkLuceneQuery(query)) {
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

    public List<I_TestWorkflowHistorySearchResults> getExtraCriterion() {
        List<I_TestWorkflowHistorySearchResults> extraCriterionCopy = new ArrayList<I_TestWorkflowHistorySearchResults>(extraCriterion);
       /*
        if (showHistory.isSelected() == false) {

            extraCriterionCopy.add(new ActiveConceptAndDescTest());
        }
        */
        return extraCriterionCopy;
    }

    public void changeLinkListener(LINK_TYPE type) {
        this.linkType = type;
    }

    public I_DescriptionTuple getSearchResultsSelection() {
        int selectedRow = lastSelectedRow;
        if (WfHistoryTable.getSelectedRow() > 0) {
            selectedRow = WfHistoryTable.getSelectedRow();
        }
        StringWithDescTuple swdt = (StringWithDescTuple) WfHistoryTable.getValueAt(selectedRow, 0);
        if (swdt != null) {
            return swdt.getTuple();
        }
        return null;
    }

    public List<WorkflowHistoryCriterionPanel> getWorkflowHistoryCriterionPanels() {
        return criterionPanels;
    }

    public void focusOnInput() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
            }
        });
    }

	@Override
	public List<CriterionPanel> getCriterionPanels() {
 		return null;
	}

}
