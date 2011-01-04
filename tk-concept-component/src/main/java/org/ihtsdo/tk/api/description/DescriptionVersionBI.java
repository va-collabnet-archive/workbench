package org.ihtsdo.tk.api.description;

import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.TypedComponentVersionBI;

public interface DescriptionVersionBI<A extends DescriptionAnalogBI>
	extends TypedComponentVersionBI, 
			DescriptionChronicleBI, 
			AnalogGeneratorBI<A> {
	
	public String getText();

    public boolean isInitialCaseSignificant();

    public String getLang();
    
}
