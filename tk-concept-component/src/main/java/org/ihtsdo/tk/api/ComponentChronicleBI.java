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
package org.ihtsdo.tk.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

// TODO: Auto-generated Javadoc
/**
 * The Interface ComponentChronicleBI for the
 * {@link org.ihtsdo.concept.component.ConceptComponent} Class. The
 * ComponentChronicleBI contains all of the versions of a component.
 *
 * @param <T> the generic type
 * @see org.ihtsdo.tk.api.ComponentBI
 */
public interface ComponentChronicleBI<T extends ComponentVersionBI>
        extends ComponentBI {

    /**
     * Gets the version.
     *
     * @param viewCoordinate the view coordinate
     * @return the version
     * @throws ContradictionException the contradiction exception
     */
    T getVersion(ViewCoordinate viewCoordinate) throws ContradictionException;

    /**
     * Gets the versions.
     *
     * @param viewCoordinate the view coordinate
     * @return the versions
     */
    Collection<? extends T> getVersions(ViewCoordinate viewCoordinate);

    /**
     * Gets the versions.
     *
     * @return the versions
     */
    Collection<? extends T> getVersions();

    /**
     * Checks if is uncommitted.
     *
     * @return true, if is uncommitted
     */
    boolean isUncommitted();

    /**
     * Gets the all stamp nids.
     *
     * @return the all stamp nids
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Set<Integer> getAllStampNids() throws IOException;

    /**
     * Gets the positions.
     *
     * @return the positions
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Set<PositionBI> getPositions() throws IOException;

    /**
     * Gets the primordial version.
     *
     * @return the primordial version
     */
    T getPrimordialVersion();

    /**
     * Make adjudication analogs.
     *
     * @param editCoordinate the edit coordinate
     * @param viewCoordinate the view coordinate
     * @return true, if successful
     * @throws Exception the exception
     */
    boolean makeAdjudicationAnalogs(EditCoordinate editCoordinate, ViewCoordinate viewCoordinate) throws Exception;

    /**
     * Gets the enclosing concept.
     *
     * @return the enclosing concept
     */
    ConceptChronicleBI getEnclosingConcept();
}
