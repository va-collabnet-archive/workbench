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

// TODO: Auto-generated Javadoc
/**
 * The Interface MediaVersionBI.
 *
 * @param <A> the generic type
 */
public interface MediaVersionBI<A extends MediaAnalogBI>
        extends TypedComponentVersionBI,
		MediaChronicleBI, AnalogGeneratorBI<A> {

    /**
     * Gets the media.
     *
     * @return the media
     */
    public byte[] getMedia();

    /**
     * Gets the text description.
     *
     * @return the text description
     */
    public String getTextDescription();

    /**
     * Gets the format.
     *
     * @return the format
     */
    public String getFormat();

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ComponentBI#getConceptNid()
     */
    public int getConceptNid();
    
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ComponentVersionBI#makeBlueprint(org.ihtsdo.tk.api.coordinate.ViewCoordinate)
     */
    @Override
    public MediaCAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;

}
