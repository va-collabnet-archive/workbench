package org.ihtsdo.concept;


import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.contradiction.ComponentType;

public class RefsetAttributeComparer extends AttributeComparer {

	private int compId = 0;
	private int referencedComponentNid = 0;
	private int typeId = 0;
	private int refsetId = 0;
	private int lcaStatusNid = 0;
	private int hashCode = 0;

	public RefsetAttributeComparer() {
		super();
		componentType = ComponentType.REFSET;
	}

	@Override
	boolean hasSameAttributes(ComponentVersionBI v) {
		I_ExtendByRefVersion  refsetVersion = (I_ExtendByRefVersion )v;

		if ((refsetVersion.getComponentId() != compId) || 
			(refsetVersion.getReferencedComponentNid() != referencedComponentNid) || 
			(refsetVersion.getTypeId() != typeId) ||
			(refsetVersion.getRefsetId() != refsetId) ||
			(refsetVersion.getStatusNid() != lcaStatusNid) ||
			(refsetVersion.hashCodeOfParts() != hashCode)) {
			return false;
		}

		return true;
	}

	@Override
	public void initializeAttributes(ComponentVersionBI v) {
		I_ExtendByRefVersion refsetVersion = (I_ExtendByRefVersion)v;
		
	    compId = refsetVersion.getComponentId();
		referencedComponentNid = refsetVersion.getReferencedComponentNid();
	    typeId = refsetVersion.getTypeId();
		refsetId = refsetVersion.getRefsetId();
		lcaStatusNid = refsetVersion.getStatusNid();
	    hashCode = refsetVersion.hashCodeOfParts();
		
		comparerInitialized = true;
		
	}

}
