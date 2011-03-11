package org.ihtsdo.concept;

import org.ihtsdo.concept.ContradictionIdentifier.CONTRADICTION_INVESTIGATION_TYPE;
import org.ihtsdo.db.bdb.computer.version.PositionMapper.RELATIVE_POSITION;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class DescriptionAttributeComparer extends AttributeComparer {

	private String lcaLanguage = null;
	private boolean lcaInitialCaseSignificance = false;
	private String lcaText = null;
	private int lcaStatusNid = 0;
	
	public DescriptionAttributeComparer() {
		super();
		componentType = CONTRADICTION_INVESTIGATION_TYPE.DESCRIPTION;
	}

	@Override
	boolean hasSameAttributes(ComponentVersionBI v) {
		DescriptionVersionBI descVersion = (DescriptionVersionBI)v;

		if ((!descVersion.getLang().equalsIgnoreCase(lcaLanguage)) ||
			(descVersion.isInitialCaseSignificant() != lcaInitialCaseSignificance)||
			(!descVersion.getText().equalsIgnoreCase(lcaText)) ||
			(descVersion.getStatusNid() != lcaStatusNid))
			return false;
		
		return true;
	}

	@Override
	public void initializeAttributes(ComponentVersionBI v) {
		DescriptionVersionBI descVersion = (DescriptionVersionBI)v;
		comparerInitialized = true;

		lcaLanguage = descVersion.getLang();
		lcaInitialCaseSignificance = descVersion.isInitialCaseSignificant();
		lcaText = descVersion.getText();
		lcaStatusNid = descVersion.getStatusNid();
	}

}
