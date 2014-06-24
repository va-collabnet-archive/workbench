/*
 * Created by JFormDesigner on Tue Jan 08 18:27:37 GMT-03:00 2013
 */
package org.ihtsdo.project.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.FilteredImageSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.filter.WfProjectFilter;
import org.ihtsdo.project.filter.WfWorklistFilter;
import org.ihtsdo.project.search.WorkflowInstanceTableModel.WORKFLOW_FIELD;
import org.ihtsdo.project.workflow.api.wf2.implementation.CancelSearch;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowStore;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;

/**
 * @author Guillermo Reynoso
 */
public class WfInstanceSearchPanel extends JPanel implements WFSearchFilterContainerBI {

    private I_ConfigAceFrame config;
    private static final long serialVersionUID = 1L;
    private WorkflowInstanceTableModel model;
    private ArrayList<WfFilterBI> filters;
    private JButton stopButton;
    private JButton addToListButton;
    private CancelSearch keepSearching;

    public WfInstanceSearchPanel() {
        initComponents();
        initCustomComponents();
    }

    @Override
    public void addNewFilterPanel() {
        filtersWrapper.add(new SearchFilterPanel(this));
        filtersWrapper.revalidate();
        filtersWrapper.repaint();
    }

    @Override
    public void removeFilterPanel(JComponent filterPanel) {
        filtersWrapper.remove(filterPanel);
        filtersWrapper.revalidate();
        filtersWrapper.repaint();
    }

    private void initCustomComponents() {
        filtersWrapper.revalidate();
        filtersWrapper.repaint();

        keepSearching = new CancelSearch();

        try {
            config = Terms.get().getActiveAceFrameConfig();
            try {
                searchButton.setIcon(new ImageIcon(ACE.class.getResource("/32x32/plain/gear_find.png")));
                addToListButton.setIcon(new ImageIcon(ACE.class.getResource("/24x24/plain/notebook_add.png")));
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
            model = new WorkflowInstanceTableModel(new WORKFLOW_FIELD[]{WORKFLOW_FIELD.FSN, WORKFLOW_FIELD.EDITOR, WORKFLOW_FIELD.STATE, WORKFLOW_FIELD.TIMESTAMP}, config);
            table1.setCellSelectionEnabled(false);
            table1.setColumnSelectionAllowed(false);
            table1.setRowSelectionAllowed(true);
            table1.setAutoCreateRowSorter(true);
            table1.setModel(model);
            DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(table1, DnDConstants.ACTION_COPY, new DragGestureListenerWithImage(new TermLabelDragSourceListener(), table1));

        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }

        stopButton = new JButton();
        try {
            stopButton.setIcon(new ImageIcon(ACE.class.getResource("/32x32/plain/stop.png")));
        } catch (Exception e) {

        }
        stopButton.setVisible(false);
        stopButton.setToolTipText("stop the current search");
        stopButton.addActionListener(new StopActionListener());
        panel1.add(stopButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    }

    /**
     * The listener interface for receiving termLabelDragSource events. The
     * class that is interested in processing a termLabelDragSource event
     * implements this interface, and the object created with that class is
     * registered with a component using the component's      <code>addTermLabelDragSourceListener<code> method. When
     * the termLabelDragSource event occurs, that object's appropriate
     * method is invoked.
     *
     * @see TermLabelDragSourceEvent
     */
    private class TermLabelDragSourceListener implements DragSourceListener {

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.
         * DragSourceDropEvent)
         */
        public void dragDropEnd(DragSourceDropEvent dsde) {
            // TODO Auto-generated method stub
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.
         * DragSourceDragEvent)
         */
        public void dragEnter(DragSourceDragEvent dsde) {
            // TODO Auto-generated method stub
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent
         * )
         */
        public void dragExit(DragSourceEvent dse) {
            // TODO Auto-generated method stub
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent
         * )
         */
        public void dragOver(DragSourceDragEvent dsde) {
            // TODO Auto-generated method stub
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.
         * DragSourceDragEvent)
         */
        public void dropActionChanged(DragSourceDragEvent dsde) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * The Class DragGestureListenerWithImage.
     */
    private class DragGestureListenerWithImage implements DragGestureListener {

        /**
         * The dsl.
         */
        DragSourceListener dsl;

        /**
         * The j tree.
         */
        JTable jTable;

        /**
         * Instantiates a new drag gesture listener with image.
         *
         * @param dsl the dsl
         * @param jTree the j tree
         */
        public DragGestureListenerWithImage(DragSourceListener dsl, JTable jTable) {

            super();
            this.jTable = jTable;
            this.dsl = dsl;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd
         * .DragGestureEvent)
         */
        public void dragGestureRecognized(DragGestureEvent dge) {
            int selRow = jTable.getSelectedRow();
            if (selRow != -1) {
                WorkflowInstanceTableModel tModel = (WorkflowInstanceTableModel) jTable.getModel();
                try {
                    Object obj = (Object) tModel.getValueAt(selRow, WorkflowInstanceTableModel.WORKFLOW_FIELD.FSN.getColumnNumber());
                    if (obj instanceof WorkflowResultItem) {
                        WorkflowResultItem rItem = (WorkflowResultItem) obj;
                        I_GetConceptData concept = Terms.get().getConcept(rItem.getConceptUuid());
                        if (concept != null) {
                            Image dragImage = getDragImage(concept);
                            Point imageOffset = new Point(-10, -(dragImage.getHeight(jTable) + 1));
                            dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, getTransferable(concept), dsl);
                        }
                    }
                } catch (InvalidDnDOperationException e) {
                    AceLog.getAppLog().info(e.toString());
                } catch (Exception ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        }

        /**
         * Gets the transferable.
         *
         * @param obj the obj
         * @return the transferable
         * @throws TerminologyException the terminology exception
         * @throws IOException Signals that an I/O exception has occurred.
         */
        private Transferable getTransferable(I_GetConceptData obj) throws TerminologyException, IOException {
            return new ConceptTransferable(Terms.get().getConcept(obj.getConceptNid()));
        }

        /**
         * Gets the drag image.
         *
         * @param obj the obj
         * @return the drag image
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public Image getDragImage(I_GetConceptData obj) throws IOException {

            I_DescriptionTuple desc = obj.getDescTuple(config.getTreeDescPreferenceList(), config);
            if (desc == null) {
                desc = obj.getDescs().iterator().next().getFirstTuple();
            }
            JLabel dragLabel = TermLabelMaker.newLabel(desc, false, false).getLabel();
            dragLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            Image dragImage = createImage(dragLabel.getWidth(), dragLabel.getHeight());
            dragLabel.setVisible(true);
            Graphics og = dragImage.getGraphics();
            og.setClip(dragLabel.getBounds());
            dragLabel.paint(og);
            og.dispose();
            FilteredImageSource fis = new FilteredImageSource(dragImage.getSource(), TermLabelMaker.getTransparentFilter());
            dragImage = Toolkit.getDefaultToolkit().createImage(fis);
            return dragImage;
        }
    }

    private class StopActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            stopButton.setVisible(false);
            searchButton.setVisible(true);
            keepSearching.cancel(false);
        }
    }

    private void searchButtonActionPerformed(ActionEvent e) {
        model.clearResults();
        keepSearching.cancel(true);
        searchButton.setVisible(false);
        stopButton.setVisible(true);
        filters = new ArrayList<WfFilterBI>();

        Component[] components = filtersWrapper.getComponents();
        for (Component component : components) {
            if (component instanceof SearchFilterPanel) {
                SearchFilterPanel sfp = (SearchFilterPanel) component;
                WfFilterBI filter = sfp.getWfFilter();
                if (filter != null) {
                    filters.add(filter);
                }
            }
        }
        filters.add(((SearchFilterPanel) searchFilterPanel).getWfFilter());

        WorkflowStore ws = new WorkflowStore();
        ProgressListener propertyChangeListener = new ProgressListener(progressBar1);
        try {
            Collection<WfProcessInstanceBI> instances = ws.searchWorkflow(filters, model, propertyChangeListener, keepSearching);
            if (instances != null) {
                for (WfProcessInstanceBI wfProcessInstanceBI : instances) {
                    model.addWfInstance(wfProcessInstanceBI);
                }
                propertyChangeListener.progressBar.setIndeterminate(false);
                searchButton.setVisible(true);
                stopButton.setVisible(false);
            }
        } catch (Exception e2) {
            propertyChangeListener.progressBar.setIndeterminate(false);
            searchButton.setVisible(true);
            stopButton.setVisible(false);
            e2.printStackTrace();
        }

    }

    private void addToListButtonActionPerformed(ActionEvent e) {
        JList conceptList = config.getBatchConceptList();
        I_ModelTerminologyList conceptListModel = (I_ModelTerminologyList) conceptList.getModel();
        LinkedList<Object[]> data = model.getData();
        for (Object[] row : data) {
            WorkflowResultItem concept = (WorkflowResultItem) row[0];
            try {
                I_GetConceptData cb = Terms.get().getConcept(concept.getConceptUuid());
                conceptListModel.addElement(cb);
            } catch (TerminologyException | IOException ex) {
                Logger.getLogger(WfInstanceSearchPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class ProgressListener implements PropertyChangeListener {

        // Prevent creation without providing a progress bar.
        @SuppressWarnings("unused")
        private ProgressListener() {
        }

        public ProgressListener(JProgressBar progressBar) {
            this.progressBar = progressBar;
            this.progressBar.setVisible(true);
            this.progressBar.setIndeterminate(true);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                progressBar.setIndeterminate(false);
                searchButton.setVisible(true);
                stopButton.setVisible(false);
            }
        }

        private JProgressBar progressBar;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY
        // //GEN-BEGIN:initComponents
        filterPanel = new JPanel();
        panel1 = new JPanel();
        progressBar1 = new JProgressBar();
        searchButton = new JButton();
        addToListButton = new JButton();
        filtersWrapper = new JPanel();
        tableContainer = new JPanel();
        scrollPane1 = new JScrollPane();
        table1 = new JTable();
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ======== this ========
        setLayout(new BorderLayout());

        // ======== filterPanel ========
        {
            filterPanel.setLayout(new BorderLayout(5, 5));

            // ======== panel1 ========
            {
                panel1.setLayout(new GridBagLayout());
                ((GridBagLayout) panel1.getLayout()).columnWidths = new int[]{0, 264, 0, 0};
                ((GridBagLayout) panel1.getLayout()).rowHeights = new int[]{21, 0};
                ((GridBagLayout) panel1.getLayout()).columnWeights = new double[]{0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout) panel1.getLayout()).rowWeights = new double[]{0.0, 1.0E-4};
                List<WfFilterBI>filters = new ArrayList<>();
                filters.add(new WfProjectFilter());
                filters.add(new WfWorklistFilter());
                searchFilterPanel = new SearchFilterPanel(this, filters);
                GridBagConstraints gbc_searchFilterPanel = new GridBagConstraints();
                gbc_searchFilterPanel.insets = new Insets(0, 0, 0, 5);
                gbc_searchFilterPanel.gridx = 0;
                gbc_searchFilterPanel.gridy = 0;
                panel1.add(searchFilterPanel, gbc_searchFilterPanel);
                panel1.add(progressBar1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
                progressBar1.setBorder(new EmptyBorder(0, 0, 0, 0));

                // ---- addToListButton ----
                addToListButton.setToolTipText("add all concepts in search panel to list view");
                addToListButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addToListButtonActionPerformed(e);
                    }
                });

                // ---- searchButton ----
                searchButton.setToolTipText("search workflow");
                searchButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        searchButtonActionPerformed(e);
                    }
                });
                panel1.add(searchButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                panel1.add(addToListButton, new GridBagConstraints(10, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.EAST, new Insets(0, 0, 0, 0), 0, 0));
            }
            filterPanel.add(panel1, BorderLayout.PAGE_START);

            // ======== filtersWrapper ========
            {
                filtersWrapper.setLayout(new BoxLayout(filtersWrapper, BoxLayout.Y_AXIS));
            }
            filterPanel.add(filtersWrapper, BorderLayout.CENTER);
        }
        add(filterPanel, BorderLayout.NORTH);

        // ======== tableContainer ========
        {
            tableContainer.setLayout(new BoxLayout(tableContainer, BoxLayout.X_AXIS));

            // ======== scrollPane1 ========
            {
                scrollPane1.setViewportView(table1);
            }
            tableContainer.add(scrollPane1);
        }
        add(tableContainer, BorderLayout.CENTER);
        // //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY
    // //GEN-BEGIN:variables
    private JPanel filterPanel;
    private JPanel panel1;
    private JProgressBar progressBar1;
    private JButton searchButton;
    private JPanel filtersWrapper;
    private JPanel tableContainer;
    private JScrollPane scrollPane1;
    private JTable table1;
    private SearchFilterPanel searchFilterPanel;
    // JFormDesigner - End of variables declaration //GEN-END:variables

}
