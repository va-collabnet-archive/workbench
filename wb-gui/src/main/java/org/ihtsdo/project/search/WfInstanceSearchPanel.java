/*
 * Created by JFormDesigner on Tue Jan 08 18:27:37 GMT-03:00 2013
 */

package org.ihtsdo.project.search;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.filter.WfProjectFilter;
import org.ihtsdo.project.filter.WfWorklistFilter;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.search.WorkflowInstanceTableModel.WORKFLOW_FIELD;
import org.ihtsdo.project.workflow.api.wf2.implementation.CancelSearch;
import org.ihtsdo.project.workflow.api.wf2.implementation.WfInstanceSearcher;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowStore;
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
	private WfInstanceSearcher instanceSearchWorker;
	private JButton stopButton;
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
		if (filtersWrapper.getComponentCount() > 1) {
			filtersWrapper.remove(filterPanel);
			filtersWrapper.revalidate();
			filtersWrapper.repaint();
		}
	}

	private void initCustomComponents() {
		filtersWrapper.add(new SearchFilterPanel(this));
		filtersWrapper.revalidate();
		filtersWrapper.repaint();
		
		keepSearching = new CancelSearch();

		try {
			config = Terms.get().getActiveAceFrameConfig();
			try{
				searchButton.setIcon(new ImageIcon(ACE.class.getResource("/32x32/plain/gear_find.png")));
			}catch (Exception e){
				
			}
			model = new WorkflowInstanceTableModel(new WORKFLOW_FIELD[] { WORKFLOW_FIELD.FSN, WORKFLOW_FIELD.EDITOR, WORKFLOW_FIELD.STATE,
					WORKFLOW_FIELD.TIMESTAMP }, config);
			table1.setModel(model);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		stopButton = new JButton();
		try{
			stopButton.setIcon(new ImageIcon(ACE.class.getResource("/32x32/plain/stop.png")));
		}catch (Exception e ){
			
		}
		stopButton.setVisible(false);
		stopButton.setToolTipText("stop the current search");
		stopButton.addActionListener(new StopActionListener());
		panel1.add(stopButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

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

		WorkflowStore ws = new WorkflowStore();
		try {
			ProgressListener propertyChangeListener = new ProgressListener(progressBar1);
			Collection<WfProcessInstanceBI> instances = ws.searchWorkflow(filters, model, propertyChangeListener, keepSearching );
			if(instances != null && !instances.isEmpty()){
				for (WfProcessInstanceBI wfProcessInstanceBI : instances) {
					model.addWfInstance(wfProcessInstanceBI);
				}
				propertyChangeListener.progressBar.setIndeterminate(false);
			}
		} catch (Exception e2) {
			e2.printStackTrace();
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
		filtersWrapper = new JPanel();
		tableContainer = new JPanel();
		button1 = new JButton();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();

		// ======== this ========
		setLayout(new BorderLayout());

		// ======== filterPanel ========
		{
			filterPanel.setLayout(new BorderLayout(5, 5));

			// ======== panel1 ========
			{
				panel1.setLayout(new GridBagLayout());
				((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 0, 0 };
				((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0 };
				((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 1.0, 0.0, 1.0E-4 };
				((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };
				panel1.add(progressBar1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 5), 0, 0));

				// ---- searchButton ----
				searchButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						searchButtonActionPerformed(e);
					}
				});
				panel1.add(searchButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
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

			// ---- button1 ----
			button1.setText("Create");
			tableContainer.add(button1);

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
	private JButton button1;
	private JScrollPane scrollPane1;
	private JTable table1;
	// JFormDesigner - End of variables declaration //GEN-END:variables

}
