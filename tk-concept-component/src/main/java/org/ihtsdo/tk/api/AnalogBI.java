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

import java.beans.PropertyVetoException;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;

/**
 * The Interface AnalogBI represents an object that is analogous to the one it
 * was created from and provides terminology generic methods for editing that object.
 * The preferred method for editing terminology is through the use of blueprints.
 * 
 * @see CreateOrAmendBlueprint
 */
public interface AnalogBI {

    /**
     * Sets the native id.
     *
     * @param nid the new native id
     * @throws PropertyVetoException if the new value is not valid
     */
    void setNid(int nid) throws PropertyVetoException;

    /**
     * Sets the status of the new component.
     *
     * @param statusNid the new status nid
     * @throws PropertyVetoException if the new value is not valid
     */
    void setStatusNid(int statusNid) throws PropertyVetoException;

    /**
     * Sets the author of the new component.
     *
     * @param authorNid the new author nid
     * @throws PropertyVetoException if the new value is not valid
     */
    void setAuthorNid(int authorNid) throws PropertyVetoException;

    /**
     * Sets the module of the new component.
     *
     * @param moduleNid the new module nid
     * @throws PropertyVetoException if the new value is not valid
     */
    void setModuleNid(int moduleNid) throws PropertyVetoException;

    /**
     * Sets the path of the new component.
     *
     * @param pathNid the new path nid
     * @throws PropertyVetoException if the new value is not valid
     */
    void setPathNid(int pathNid) throws PropertyVetoException;

    /**
     * Sets the time associated with the addition of the new component.
     *
     * @param time the new time
     * @throws PropertyVetoException if the new value is not valid
     */
    void setTime(long time) throws PropertyVetoException;

    /**
     * Adds an additional id as specified by the
     * <code>longId</code>. This is generally an SCTID, however the authority
     * specified by the
     * <code>authorityNid</code> specifies to whom the id belongs.
     *
     * @param longId the id to be added
     * @param authorityNid the nid representing the authority associated with
     * the id
     * @param statusNid the nid representing the status of the id
     * @param editCoordinate the edit coordinate containing the editing metadata
     * to use
     * @param time the time to be associated with the addition
     * @return <code>true</code>, if successful
     */
    public boolean addLongId(Long longId, int authorityNid, int statusNid, EditCoordinate editCoordinate, long time);
}
