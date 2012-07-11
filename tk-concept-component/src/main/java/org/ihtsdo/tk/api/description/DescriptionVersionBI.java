package org.ihtsdo.tk.api.description;

import java.io.IOException;
import java.util.regex.Pattern;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TypedComponentVersionBI;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
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
         * @param viewCoordinate
         * @return
         * @throws IOException
         * @throws ContradictionException
         * @throws InvalidCAB
         */
        @Override
    public DescriptionCAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;
    
    public boolean matches(Pattern pattern);
}
