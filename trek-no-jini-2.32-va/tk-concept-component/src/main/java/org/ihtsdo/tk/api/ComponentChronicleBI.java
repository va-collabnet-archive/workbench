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

/**
 * The Interface ComponentChronicleBI contains terminology generic methods for
 * editing and interacting with a component. A ComponentChronicleBI contains all
 * of the versions of a component.
 *
 * @param <T> the generic type of the versions that the chronicle contains
 * @see org.ihtsdo.tk.api.ComponentBI
 */
public interface ComponentChronicleBI<T extends ComponentVersionBI>
        extends ComponentBI {

    /**
     * Gets a particular version of a component based the given
     * <code>viewCoordinate</code>.
     *
     * @param viewCoordinate the view coordinate specifying which version of the
     * component to return
     * @return the specified version of the component
     * @throws ContradictionException if there is more than one version for a
     * particular <code>viewCoordinate</code>
     */
    T getVersion(ViewCoordinate viewCoordinate) throws ContradictionException;

    /**
     * Gets all the versions of a component based the
     * <code>viewCoordinate</code>.
     *
     * @param viewCoordinate the view coordinate specifying which version of the
     * component to return
     * @return the specified versions of the component
     */
    Collection<? extends T> getVersions(ViewCoordinate viewCoordinate);

    /**
     * Gets the all versions of a component.
     *
     * @return all the versions the component
     */
    Collection<? extends T> getVersions();

    /**
     * Checks if the component is uncommitted.
     *
     * @return <code>true</code>, if the component is uncommitted
     */
    boolean isUncommitted();

    /**
     * Gets the all stamp nids for all versions on a component.
     *
     * @return the stamp nids of the component
     * @throws IOException signals that an I/O exception has occurred
     * @see org.ihtsdo.tk.api.StampBI
     */
    Set<Integer> getAllStampNids() throws IOException;

    /**
     * Gets the positions represented in the versions of a component.
     *
     * @return the positions found in the component
     * @throws IOException signals that an I/O exception has occurred
     * @see org.ihtsdo.tk.api.PositionBI
     */
    Set<PositionBI> getPositions() throws IOException;

    /**
     * Gets the primordial version, the first version, of a component.
     *
     * @return the primordial version of the component
     */
    T getPrimordialVersion();

    /**
     * Makes an uncommitted analog of the component based on the given
     * <code>viewCoordinate</code> and created based on the specifications given
     * by the
     * <code>editCoordinate</code>. The analog is only created for the versions
     * which are not a part of the baseline version and is created using the
     * Contradiction Manager specified in the
     * <code>viewCoordinate</code> and will resolve contradictions accordingly.
     *
     * @param editCoordinate the edit coordinate specifying the editing metadata
     * to use in creation
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @return <code>true</code>, if analogs were successfully created
     * @throws Exception indicates an exception has occurred indicates an exception has occurred
     * @see org.ihtsdo.tk.api.AnalogBI
     */
    boolean makeAdjudicationAnalogs(EditCoordinate editCoordinate, ViewCoordinate viewCoordinate) throws Exception;

    /**
     * Gets the concept that a component is a part of.
     *
     * @return the component's enclosing concept
     */
    ConceptChronicleBI getEnclosingConcept();
}
