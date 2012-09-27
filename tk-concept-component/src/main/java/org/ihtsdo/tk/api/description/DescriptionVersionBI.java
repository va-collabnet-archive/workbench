/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.api.description;

import java.io.IOException;
import java.util.regex.Pattern;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TypedComponentVersionBI;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

// TODO: Auto-generated Javadoc
/**
 * The Interface DescriptionVersionBI for the
 * {@link org.ihtsdo.concept.component.description.Description} Class.
 *
 * @param <A> the generic type
 * @see org.ihtsdo.tk.api.ComponentVersionBI
 */
public interface DescriptionVersionBI<A extends DescriptionAnalogBI>
        extends TypedComponentVersionBI,
        DescriptionChronicleBI,
        AnalogGeneratorBI<A> {

    /**
     * Gets the text of the version of the description.
     *
     * @return a <code>String</code> representing the text of the
     * description.
     */
    public String getText();

    /**
     * Checks if the description text is initial case significant.
     *
     * @return true, if is text is initial case significant
     */
    public boolean isInitialCaseSignificant();

    /**
     * Gets the language that the description is in.
     *
     * @return a <code>String</code> representing the language of the
     * description
     */
    public String getLang();

    /**
     * @param viewCoordinate the view coordinate
     * @return the description blueprint
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     * @throws InvalidCAB the invalid cab
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
     */
    @Override
    public DescriptionCAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;

    /**
     * Tests to see if the description text matches the given pattern.
     *
     * @param pattern the pattern
     * @return true, if matches
     */
    public boolean matches(Pattern pattern);
}
