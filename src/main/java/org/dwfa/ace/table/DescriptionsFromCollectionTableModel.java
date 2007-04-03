package org.dwfa.ace.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinDescVersioned;

public class DescriptionsFromCollectionTableModel extends DescriptionTableModel {
	public DescriptionsFromCollectionTableModel(DESC_FIELD[] columns, AceFrameConfig config) {
		super(columns, config);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ThinDescVersioned> descriptionList = new ArrayList<ThinDescVersioned>();
	
	@Override
	public I_DescriptionTuple getDescription(int rowIndex) {
		return descriptionList.get(rowIndex).getLastTuple();
	}

	public int getRowCount() {
		return descriptionList.size();
	}

	public void setDescriptions(Collection<ThinDescVersioned> descriptions) {
		descriptionList = new ArrayList<ThinDescVersioned>(descriptions);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableChanged(new TableModelEvent(DescriptionsFromCollectionTableModel.this));
			}});
	}
	

	@Override
	public Map<Integer, ConceptBean> getReferencedConcepts() {
		return new HashMap<Integer, ConceptBean>();
	}
}
