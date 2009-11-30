package org.dwfa.ace.task.wfdetailsSheet;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.RelationshipTableRenderer;
import org.dwfa.ace.table.SrcRelTableModel;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.bpa.util.TableSorter;

public class RefreshSpecClausePanel  extends JPanel implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private static final String REPLACE_OPTION = "replace concept with:";
    private static final String RETIRE_OPTION = "retire spec clause";
    private static final String SKIP_OPTION = "skip clause and place at end of list";
    
    private JComboBox updateOptions = new JComboBox(new String[] { REPLACE_OPTION, RETIRE_OPTION, SKIP_OPTION });
    private JTextArea editorComments = new JTextArea();
    private TermComponentLabel replacementConceptLabel;
    
    private I_GetConceptData refsetSpec;
    private Set<I_Position> refsetSpecVersionSet;
    private Set<I_Position> sourceTerminologyVersionSet;
    private I_GetConceptData conceptUnderReview;
    private I_ConfigAceFrame frameConfig;
    private List<Collection<UUID>> clausesToUpdate;

    private SrcRelTableModel srcRelTableModel;

    private JTableWithDragImage relTable;
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private HostProxy host = new HostProxy();

    public RefreshSpecClausePanel(I_GetConceptData refsetSpec,
            Set<I_Position> refsetSpecVersionSet, Set<I_Position> sourceTerminologyVersionSet,
            I_GetConceptData conceptUnderReview, List<Collection<UUID>> clauseToUpdate, I_ConfigAceFrame frameConfig) {
        super();
        replacementConceptLabel = new TermComponentLabel(frameConfig);
        this.refsetSpec = refsetSpec;
        this.refsetSpecVersionSet = refsetSpecVersionSet;
        this.sourceTerminologyVersionSet = sourceTerminologyVersionSet;
        this.conceptUnderReview = conceptUnderReview;
        this.clausesToUpdate = clauseToUpdate;
        this.frameConfig = frameConfig;
        updateOptions.setSelectedItem(REPLACE_OPTION);
        
        srcRelTableModel = new SrcRelTableModel(host, getSrcRelColumns(), frameConfig);
        TableSorter relSortingTable = new TableSorter(srcRelTableModel);
        relTable = new JTableWithDragImage(relSortingTable);
        RelationshipTableRenderer renderer = new RelationshipTableRenderer();
        relTable.setDefaultRenderer(StringWithRelTuple.class, renderer);
        relSortingTable.setTableHeader(relTable.getTableHeader());
        relSortingTable
                .getTableHeader()
                .setToolTipText(
                        "Click to specify sorting; Control-Click to specify secondary sorting");
        REL_FIELD[] columnEnums = srcRelTableModel.getColumnEnums();
        for (int i = 0; i < relTable.getColumnCount(); i++) {
            TableColumn column = relTable.getColumnModel().getColumn(i);
            REL_FIELD columnDesc = columnEnums[i];
            column.setIdentifier(columnDesc);
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
        }

        layoutRefreshSpecClausePanel();
        this.updateOptions.addActionListener(this);
        host.setTermComponent(conceptUnderReview);
    }

    private REL_FIELD[] getSrcRelColumns() {
        List<REL_FIELD> fields = new ArrayList<REL_FIELD>();
        fields.add(REL_FIELD.REL_TYPE);
        fields.add(REL_FIELD.DEST_ID);
        return fields.toArray(new REL_FIELD[fields.size()]);
    }

    private void layoutRefreshSpecClausePanel() {
    	this.removeAll();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        
        add(new JLabel("refset spec:"), gbc);
        
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        
        TermComponentLabel refsetSpecLabel = new TermComponentLabel(frameConfig);
        refsetSpecLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        refsetSpecLabel.setTermComponent(refsetSpec);
        refsetSpecLabel.setFrozen(true);
        add(refsetSpecLabel, gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        
        add(new JLabel("specification version:"), gbc);
        
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        
        add(new JLabel(refsetSpecVersionSet.toString()), gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        
        add(new JLabel("terminology version:"), gbc);
        
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        
        add(new JLabel(sourceTerminologyVersionSet.toString()), gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        
        add(new JLabel("concept under review:"), gbc);
        
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        
        TermComponentLabel conceptUnderReviewLabel = new TermComponentLabel(frameConfig);
        conceptUnderReviewLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        conceptUnderReviewLabel.setTermComponent(conceptUnderReview);
        conceptUnderReviewLabel.setFrozen(true);
        add(conceptUnderReviewLabel, gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        
        add(new JLabel("Concept relations:"), gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(new JScrollPane(relTable), gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        
        add(updateOptions, gbc);
        
        
        if (updateOptions.getSelectedItem().equals(REPLACE_OPTION)) {
            
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;
            
            add(new JLabel("Concept replacement:"), gbc);
            
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.EAST;
            
            add(replacementConceptLabel, gbc);
            
         }
        
        gbc.gridy++;
        gbc.gridx = 0;

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.EAST;
        
        add(new JScrollPane(editorComments), gbc);
        
        if (getRootPane() != null) {
            getRootPane().repaint();
        }
        if (this.getParent() != null) {
            this.getParent().doLayout();
            this.getParent().repaint();          
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
		        layoutRefreshSpecClausePanel();
			}
		});
    }
    
    private class HostProxy implements I_HostConceptPlugins {

        @Override
        public I_GetConceptData getHierarchySelection() {
            return frameConfig.getHierarchySelection();
        }

        @Override
        public boolean getShowHistory() {
            return false;
        }

        @Override
        public boolean getShowRefsets() {
            return false;
        }

        @Override
        public boolean getToggleState(TOGGLES toggle) {
            return false;
        }

        @Override
        public boolean getUsePrefs() {
            return false;
        }

        @Override
        public void setAllTogglesToState(boolean state) {
            //nothing to do...
        }

        @Override
        public void setLinkType(LINK_TYPE link) {
            //nothing to do...
        }

        @Override
        public void setToggleState(TOGGLES toggle, boolean state) {
            //nothing to do...
        }

        @Override
        public void unlink() {
            //nothing to do...
        }

        @Override
        public void addPropertyChangeListener(String property, PropertyChangeListener l) {
            pcs.addPropertyChangeListener(property, l);
        }

        @Override
        public I_ConfigAceFrame getConfig() {
            return frameConfig;
        }

        @Override
        public I_AmTermComponent getTermComponent() {
            return conceptUnderReview;
        }

        @Override
        public void removePropertyChangeListener(String property, PropertyChangeListener l) {
            pcs.removePropertyChangeListener(property, l);
        }

        @Override
        public void setTermComponent(I_AmTermComponent termComponent) {
            conceptUnderReview = (I_GetConceptData) termComponent;
            pcs.firePropertyChange("termComponent", null, termComponent);
        }
    }
    
    public void performRefreshAction() {
        if (updateOptions.getSelectedItem().equals(REPLACE_OPTION)) {
        	clausesToUpdate.remove(0);
        	// Do replacement here...
        } else if (updateOptions.getSelectedItem().equals(RETIRE_OPTION)) {
        	clausesToUpdate.remove(0);
        	// Do retire here...
        } else if (updateOptions.getSelectedItem().equals(SKIP_OPTION)) {
        	clausesToUpdate.add(clausesToUpdate.remove(0));
        }	
    }

}
