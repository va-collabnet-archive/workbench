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

/**
 * The Interface DescriptionVersionBI provides methods for interacting with, or
 * creating version of, a description.
 *
 * @param <A> the type of object returned by the analog generator
 * @see ComponentVersionBI
 */
public interface DescriptionVersionBI<A extends DescriptionAnalogBI>
        extends TypedComponentVersionBI,
        DescriptionChronicleBI,
        AnalogGeneratorBI<A> {

    /**
     * Gets the text of this version of a description.
     *
     * @return a <code>String</code> representing the text of the description.
     */
    public String getText();

    /**
     * Checks if the description text is initial case significant.
     *
     * @return <code>true</code>, if is text is initial case significant
     */
    public boolean isInitialCaseSignificant();

    /**
     * Gets the language that the description is in. Used to determine what
     * language the description is in when comparing descriptions or making new
     * descriptions.
     *
     * @return a two character abbreviation of the language of the description
     */
    public String getLang();

    /**
     * @param viewCoordinate the view coordinate specifying which version of the
     * description to make a blueprint of
     * @return the description blueprint, which can be constructed to create
     * a <code>DescriptionChronicleBI</code>
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of the
     * description was returned for the specified view coordinate
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
     */
    @Override
    public DescriptionCAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;

    /**
     * Tests to see if the description text matches the given
     * <code>pattern</code>.
     *
     * @param pattern the description text matches the regular express as
     * specified in the <code>pattern</code>
     * @return <code>true</code>, if matches
     */
    public boolean matches(Pattern pattern);
}
