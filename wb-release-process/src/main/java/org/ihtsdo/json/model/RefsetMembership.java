package org.ihtsdo.json.model;

/**
 *
 * @author Alejandro Rodriguez
 */

public class RefsetMembership extends Component {
	
	public enum RefsetMembershipType {
		SIMPLEMAP("SIMPLEMAP"), ATTRIBUTE_VALUE("ATTRIBUTE_VALUE"), ASSOCIATION("ASSOCIATION"), SIMPLE_REFSET("SIMPLE_REFSET");
		private String type;
		
		RefsetMembershipType(String type) {
			this.type = type;
		}
		
		public String getType() {
			return this.type;
		}
		
		@Override
		public String toString() {
			return this.type;
		}
	}
	private String type;
	private Long referencedComponentId;
	private ConceptDescriptor refset;
	private ConceptDescriptor cidValue;
	private String otherValue;

	public ConceptDescriptor getRefset() {
		return refset;
	}

	public void setRefset(ConceptDescriptor refset) {
		this.refset = refset;
	}

	public ConceptDescriptor getCidValue() {
		return cidValue;
	}

	public void setCidValue(ConceptDescriptor cidValue) {
		this.cidValue = cidValue;
	}


	public RefsetMembership() {
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOtherValue() {
		return otherValue;
	}

	public void setOtherValue(String otherValue) {
		this.otherValue = otherValue;
	}

	public Long getReferencedComponentId() {
		return referencedComponentId;
	}

	public void setReferencedComponentId(Long referencedComponentId) {
		this.referencedComponentId = referencedComponentId;
	}

}
