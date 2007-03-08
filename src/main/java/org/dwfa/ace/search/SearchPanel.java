package org.dwfa.ace.search;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.table.DescriptionsFromCollectionTableModel;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.DescriptionTableModel.DESC_FIELD;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinDescTuple;
import org.dwfa.vodb.types.ThinDescVersioned;

public class SearchPanel extends JPanel {

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
				ThinDescTuple tuple = model.getDescription(modelRow);
				ConceptBean cb = ConceptBean.get(tuple.getConceptId());
				for (I_ContainTermComponent l: linkedComponents) {
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

	private JCheckBox regexCheck;

	private JButton searchSetting;

	private JButton stopButton;

	private JProgressBar progressBar;

	private JLabel progressInfo;

	private JTableWithDragImage descTable;

	private TableSorter sortingTable;
	
	private Set<I_ContainTermComponent> linkedComponents = new HashSet<I_ContainTermComponent>();

	public SearchPanel(AceFrameConfig config) {
		super(new GridBagLayout());
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search");
		this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				"search");
		this.getActionMap().put("search",
				new AbstractAction("Search on enter") {
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
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;

		add(new JLabel("query: ", JLabel.RIGHT), gbc);

		gbc.weightx = 1;
		gbc.gridx++;
		this.searchPhraseField = new JTextField(40);
		this.searchPhraseField.setDragEnabled(true);
		add(searchPhraseField, gbc);

		gbc.gridx++;
		gbc.weightx = 0;
		regexCheck = new JCheckBox("regex");
		regexCheck.setSelected(true);
		add(regexCheck, gbc);

		gbc.gridx++;
		progressInfo = new JLabel();
		progressInfo.setVisible(false);
		add(progressInfo, gbc);

		// row 0, double height
		gbc.gridheight = 2;
		searchSetting = new JButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/preferences.png")));
		gbc.gridx++;
		add(searchSetting, gbc);

		gbc.gridx++;
		searchButton = new JButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/find.png")));
		searchButton.addActionListener(getActionMap().get("search"));
		gbc.anchor = GridBagConstraints.NORTHWEST;
		add(searchButton, gbc);

		stopButton = new JButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/stop.png")));
		stopButton.setVisible(false);
		// stopButton.setBorder(BorderFactory.createLineBorder(Color.red));
		add(stopButton, gbc);
		gbc.anchor = GridBagConstraints.WEST;
		// start row 1

		gbc.gridheight = 1;
		gbc.gridx = 0;
		gbc.gridy++;
		add(new JLabel("root: ", JLabel.RIGHT), gbc);

		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		TermComponentLabel rootConceptLabel = new TermComponentLabel(config);
		add(rootConceptLabel, gbc);

		gbc.gridx++;
		gbc.weightx = 0;
		add(new JPanel(), gbc);

		gbc.gridx++;
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		add(progressBar, gbc);

		// Results below...
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridheight = 1;

		model = new DescriptionsFromCollectionTableModel(
				new DESC_FIELD[] { DESC_FIELD.TEXT }, config);
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
		sortingTable
				.getTableHeader()
				.setToolTipText(
						"Click to specify sorting; Control-Click to specify secondary sorting");
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 6;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(new JScrollPane(descTable), gbc);
		descTable.getSelectionModel().addListSelectionListener(new SearchSelectionListener());

	}

	public void setShowProgress(boolean show) {
		searchButton.setVisible(!show);
		stopButton.setVisible(show);
		progressInfo.setVisible(show);
		progressBar.setVisible(show);
		regexCheck.setVisible(!show);
		searchSetting.setVisible(!show);
	}

	private void startSearch() {
		if (searchPhraseField.getText().length() > 2) {
			setShowProgress(true);
			model.setDescriptions(new ArrayList<ThinDescVersioned>());
			ACE.threadPool.execute(new SearchStringWorker(this, model,
					searchPhraseField.getText()));
		} else {
			JOptionPane.showMessageDialog(getRootPane(),
					"The search string must be longer than 2 characters: "
							+ searchPhraseField.getText(), "Search Error",
					JOptionPane.ERROR_MESSAGE);
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
}
