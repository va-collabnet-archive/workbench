/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api.media;

import java.io.IOException;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TypedComponentVersionBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;


/**
 * The Interface MediaVersionBI provides methods for interacting with, or
 * creating version of, a type of media.
 *
 * @param <A> the type of object returned by the analog generator
 * @see ComponentVersionBI
 */
public interface MediaVersionBI<A extends MediaAnalogBI>
        extends TypedComponentVersionBI,
		MediaChronicleBI, AnalogGeneratorBI<A> {

    /**
     * Gets the media associated with this media version.
     *
     * @return the media
     */
    public byte[] getMedia();

    /**
     * Gets a text description associated with this media version.
     *
     * @return the text description
     */
    public String getTextDescription();

    /**
     * Gets a string representing the media format.
     *
     * @return the format of this media
     */
    public String getFormat();

    /**
     * 
     * @return the enclosing concept nid
     */
    @Override
    public int getConceptNid();
    
    /**
     * 
     * @param viewCoordinate the view coordinate specifying which version is active or inactive
     * @return the media blueprint, which can be constructed to create
     * a <code>MediaChronicleBI</code>
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
     */
    @Override
    public MediaCAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;

}
