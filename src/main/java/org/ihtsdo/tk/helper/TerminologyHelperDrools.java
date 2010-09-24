package org.ihtsdo.tk.helper;


public class TerminologyHelperDrools {

	public TerminologyHelperDrools() {
	}
	
	public boolean isMemberOf(String conceptUUID, String refsetUUID) {
		return true;
	}
	
	public boolean isActive(String conceptUUID) {
		return true;
	}
	
	public boolean isParentOf(String parent, String subtype) throws Exception {
		return (parent.equals(subtype));
	}

	public boolean isParentOfOrEqualTo(String parent, String subtype) throws Exception {
		return (parent.equals(subtype));
	}

}
