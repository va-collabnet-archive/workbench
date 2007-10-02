package org.dwfa.ace.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinDescVersioned;

public class DescriptionsFromCollectionTableModel extends DescriptionTableModel {
	public DescriptionsFromCollectionTableModel(DESC_FIELD[] columns, I_ConfigAceFrame config) {
		super(columns, config);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ThinDescVersioned> descriptionList = new ArrayList<ThinDescVersioned>();
	private List<Float> scoreList = new ArrayList<Float>();
	
	@Override
	public I_DescriptionTuple getDescription(int rowIndex) {
		return descriptionList.get(rowIndex).getLastTuple();
	}

	public int getRowCount() {
		return descriptionList.size();
	}

	public void setDescriptions(Collection<ThinDescVersioned> descriptions) {
		descriptionList = new ArrayList<ThinDescVersioned>(descriptions);
		scoreList = null;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableChanged(new TableModelEvent(DescriptionsFromCollectionTableModel.this));
			}});
	}
	
	public void setLuceneMatches(Collection<LuceneMatch> matches) {
		descriptionList = new ArrayList<ThinDescVersioned>(matches.size());
		scoreList = new ArrayList<Float>(matches.size());
		synchronized (matches) {
			for (LuceneMatch m: matches) {
				descriptionList.add(m.getDesc());
				scoreList.add(m.getScore());
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableChanged(new TableModelEvent(DescriptionsFromCollectionTableModel.this));
			}});
	}
	
	public String getScore(int rowIndex) {
		if (scoreList != null) {
			return scoreList.get(rowIndex).toString();
		}
		return "";
	}
	

	@Override
	public Map<Integer, ConceptBean> getReferencedConcepts() {
        Map<Integer, ConceptBean> referencedConcept = new HashMap<Integer, ConceptBean>();
        List<ThinDescVersioned> descriptionListCopy;
        synchronized (descriptionList) {
            descriptionListCopy = new ArrayList<ThinDescVersioned>(descriptionList);
        }
        for (ThinDescVersioned desc: descriptionListCopy) {
            for (I_DescriptionPart part: desc.getVersions()) {
                referencedConcept.put(part.getTypeId(), ConceptBean.get(part.getTypeId()));
            }
            
        }
        
		return referencedConcept;
	}
}
