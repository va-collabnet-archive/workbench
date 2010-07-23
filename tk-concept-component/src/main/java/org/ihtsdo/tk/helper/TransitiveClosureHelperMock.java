package org.ihtsdo.tk.helper;

import java.util.UUID;

public class TransitiveClosureHelperMock extends AbstractTransitiveClosureHelper {

	public TransitiveClosureHelperMock() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.rules.I_TransitiveClosureHelper#isParentOf(java.util.UUID, java.util.UUID)
	 */
	public boolean isParentOf(UUID parent, UUID subtype) throws Exception {
		return (parent.equals(subtype));
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rules.I_TransitiveClosureHelper#isParentOfOrEqualTo(java.util.UUID, java.util.UUID)
	 */
	public boolean isParentOfOrEqualTo(UUID parent, UUID subtype) throws Exception {
		//System.out.println("Comparing: " + parent + " to " + subtype);
		return (parent.equals(subtype));
	}

	@Override
	public boolean isParentOf(String parents, UUID subtype) throws Exception {
		return (UUID.fromString(parents.substring(0, parents.indexOf(",")).trim()).equals(subtype));
	}

	@Override
	public boolean isParentOfOrEqualTo(String parents, UUID subtype)
			throws Exception {
		return (UUID.fromString(parents.substring(0, parents.indexOf(",")).trim()).equals(subtype));
	}

}
