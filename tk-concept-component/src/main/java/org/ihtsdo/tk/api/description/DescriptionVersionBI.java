package org.ihtsdo.tk.api.description;

import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.TypedComponentVersionBI;

public interface DescriptionVersionBI 
	extends TypedComponentVersionBI, 
			DescriptionChronicleBI, 
			AnalogGeneratorBI<DescriptionAnalogBI> {
	
	public String getText();

    public boolean isInitialCaseSignificant();

    public String getLang();

}
