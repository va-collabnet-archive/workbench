/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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

/**
 * The Class QuantityPartitioner.
 */
public class QuantityPartitioner extends RefsetPartitioner implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The partitioned. */
	private int partitioned;
	
	/** The quantity. */
	Integer quantity;

	/**
	 * Instantiates a new quantity partitioner.
	 */
	public QuantityPartitioner() {
		super();
	}
	
	/**
	 * Instantiates a new quantity partitioner.
	 *
	 * @param quantity the quantity
	 */
	public QuantityPartitioner(Integer quantity) {
		super();
		this.quantity = quantity;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.partition.RefsetPartitioner#getMembersToInclude(org.ihtsdo.project.model.PartitionScheme, java.lang.String, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
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

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.partition.RefsetPartitioner#evaluateMember(org.dwfa.ace.api.I_GetConceptData, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	protected boolean evaluateMember(I_GetConceptData member, I_ConfigAceFrame config) {
		boolean result = false;
		
		partitioned++;
		if (partitioned <= quantity) {
			result = true;
		}

		return result;
	}

	/**
	 * Gets the partitioned.
	 *
	 * @return the partitioned
	 */
	public int getPartitioned() {
		return partitioned;
	}

	/**
	 * Sets the partitioned.
	 *
	 * @param partitioned the new partitioned
	 */
	public void setPartitioned(int partitioned) {
		this.partitioned = partitioned;
	}
	
	/**
	 * Gets the quantity.
	 *
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Sets the quantity.
	 *
	 * @param quantity the new quantity
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	/**
	 * Sets the percentage value.
	 *
	 * @param percentage the percentage
	 * @param totalMembers the total members
	 */
	public void setPercentageValue(int percentage, int totalMembers) {
		this.quantity = (percentage * totalMembers)/100;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Quantity";
	}
}
