package org.ihtsdo.tk.api.description;

import java.io.IOException;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TypedComponentVersionBI;
import org.ihtsdo.tk.api.blueprint.DescCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;


/**
 * 
 * @author kec
 * @param <A>
 */
public interface DescriptionVersionBI<A extends DescriptionAnalogBI>
	extends TypedComponentVersionBI, 
			DescriptionChronicleBI, 
			AnalogGeneratorBI<A> {
	
    /**
     * Get the text value of this version of this description.
     * @return a <code>String</code> object representing the text value of this description. 
     */
    public String getText();

        /**
         * 
         * @return
         */
        public boolean isInitialCaseSignificant();

        /**
         * 
         * @return
         */
        public String getLang();
    
        /**
         * 
         * @param vc
         * @return
         * @throws IOException
         * @throws ContradictionException
         * @throws InvalidCAB
         */
        @Override
    public DescCAB makeBlueprint(ViewCoordinate vc, org.ihtsdo.tk.api.blueprint.IdDirective idDirective, org.ihtsdo.tk.api.blueprint.RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB;
}
