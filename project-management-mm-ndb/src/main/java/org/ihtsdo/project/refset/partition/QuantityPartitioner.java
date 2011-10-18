package org.ihtsdo.project.refset.partition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.PartitionScheme;

public class QuantityPartitioner extends RefsetPartitioner implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int partitioned;
	Integer quantity;

	public QuantityPartitioner() {
		super();
	}
	public QuantityPartitioner(Integer quantity) {
		super();
		this.quantity = quantity;
	}
	
	public List<I_GetConceptData> getMembersToInclude(PartitionScheme scheme,  String name, I_ConfigAceFrame config) throws Exception {
		List<I_GetConceptData> membersNotPartitioned = TerminologyProjectDAO.getMembersNotPartitioned(scheme, config);
		List<I_GetConceptData> membersToIncludeInNewPartition = new ArrayList<I_GetConceptData>();
		if (quantity == null) {
			throw new Exception("Quantity greater than non partitioned members");
		}
		if (quantity > membersNotPartitioned.size()) {
			throw new Exception("Quantity greater than non partitioned members");
		}
		partitioned = 0;
		Collections.sort(membersNotPartitioned,
				new Comparator<I_GetConceptData>()
				{
					public int compare(I_GetConceptData f1, I_GetConceptData f2)
					{
						return f1.toString().compareTo(f2.toString());
					}
				});
		
		for (I_GetConceptData loopMember : membersNotPartitioned) {
			if (evaluateMember(loopMember, config)) {
				membersToIncludeInNewPartition.add(loopMember);
			}
		}
		return membersToIncludeInNewPartition;
	}

	protected boolean evaluateMember(I_GetConceptData member, I_ConfigAceFrame config) {
		boolean result = false;
		
		partitioned++;
		if (partitioned <= quantity) {
			result = true;
		}

		return result;
	}

	public int getPartitioned() {
		return partitioned;
	}

	public void setPartitioned(int partitioned) {
		this.partitioned = partitioned;
	}
	
	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public void setPercentageValue(int percentage, int totalMembers) {
		this.quantity = (percentage * totalMembers)/100;
	}

	public String toString() {
		return "Quantity";
	}
}
