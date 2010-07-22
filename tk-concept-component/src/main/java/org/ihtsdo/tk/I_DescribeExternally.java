package org.ihtsdo.tk;

import java.util.UUID;

public interface I_DescribeExternally extends I_VersionExternally {

	public boolean isInitialCaseSignificant();

	public String getLang();

	public String getText();
	
	public UUID getTypeUuid();

}