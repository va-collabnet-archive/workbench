package org.dwfa.ace.task.classify;

import java.util.ArrayList;

public class SnoGrpList extends ArrayList<SnoGrp> {
	private static final long serialVersionUID = 1L;

	public SnoGrpList(){
		super();
	}
	
	/**
	 * Which group(s) in THIS ROLE_GROUP_LIST are NON-REDUNTANT?<br>
	 * <br>
	 * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
	 * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
	 * provide overall computational efficiency.</font>
	 * 
	 * @return SnoGrpList
	 */
	public SnoGrpList whichNonRedundant() {
		int max = this.size();
		if (max <= 1)
			return this; // trivial case.

		// Check in the reverse direction.
		SnoGrpList sgPass1 = new SnoGrpList();
		for (int ai = max - 1; ai >= 0; ai--) {
			SnoGrp groupA = this.get(ai);
			boolean keep = true;
			for (int bi = max - 2; bi > 0; bi--) {
				SnoGrp groupB = this.get(bi);
				if (groupA.subsumes(groupB)) {
					keep = false;
					break;
				}
			}
			if (keep)
				sgPass1.add(groupA); // creates reverse order
		}
		sgPass1.add(this.get(0));

		// Repeat in reverse order.
		// Duplicates will have been eliminated in the first pass.
		SnoGrpList sgPass2 = new SnoGrpList();
		max = sgPass1.size();
		for (int ai = max - 1; ai >= 0; ai--) {
			SnoGrp groupA = this.get(ai);
			boolean keep = true;
			for (int bi = max - 2; bi > 0; bi--) {
				SnoGrp groupB = this.get(bi);
				if (groupA.subsumes(groupB)) {
					keep = false;
					break;
				}
			}
			if (keep)
				sgPass2.add(groupA); // undoes reverse order
		}
		if (max >= 1)
			sgPass2.add(sgPass1.get(0)); // was fully tested on pass1

		return sgPass2;
	}

	/**
	 * Which groups in this DIFFERENTIATE from all groups in groupListB?<br>
	 * <br>
	 * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
	 * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
	 * provide overall computational efficiency.</font>
	 * 
	 * @return SnoGrpList
	 */
	public SnoGrpList whichDifferentiateFrom(SnoGrpList groupListB) {
		if (this.size() == 0)
			return this; // trivial case.

		SnoGrpList sg = new SnoGrpList();
		for (SnoGrp groupA : this) {
			boolean keep = true;
			for (SnoGrp groupB : groupListB) {
				if (groupA.subsumes(groupB)) {
					keep = false;
					break;
				}
			}
			if (keep)
				sg.add(groupA);
		}

		return sg;
	}

	/**
	 * Which groups in this do not have ANY equal group in groupListB?<br>
	 * <br>
	 * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
	 * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
	 * provide overall computational efficiency.</font>
	 * 
	 * @param groupListB
	 * @return
	 */
	public SnoGrpList whichNotEqual(SnoGrpList groupListB) {
		SnoGrpList sg = new SnoGrpList();
		for (SnoGrp groupA : this) {
			boolean foundEqual = false;
			for (SnoGrp groupB : groupListB) {
				if (groupA.equals(groupB)) {
					foundEqual = true;
					break;
				}
			}
			if (!foundEqual) {
				sg.add(groupA);
			}
		}
		return sg;
	}

}
