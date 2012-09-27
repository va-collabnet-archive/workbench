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
package org.ihtsdo.tk.api;

import java.beans.PropertyVetoException;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;

// TODO: Auto-generated Javadoc
/**
 * The Interface AnalogBI for the <code>Revision</code> class. TODO-javadoc: should use link?
 *
 */
public interface AnalogBI {

    /**
     * Sets the native id.
     *
     * @param nid the new native id
     * @throws PropertyVetoException the property veto exception
     */
    void setNid(int nid) throws PropertyVetoException;
    
    /**
     * Sets the status nid.
     *
     * @param statusNid the new status nid
     * @throws PropertyVetoException the property veto exception
     */
    void setStatusNid(int statusNid) throws PropertyVetoException;
    
    /**
     * Sets the author nid.
     *
     * @param authorNid the new author nid
     * @throws PropertyVetoException the property veto exception
     */
    void setAuthorNid(int authorNid) throws PropertyVetoException;
    
    /**
     * Sets the module nid.
     *
     * @param moduleNid the new module nid
     * @throws PropertyVetoException the property veto exception
     */
    void setModuleNid(int moduleNid) throws PropertyVetoException;
    
    /**
     * Sets the path nid.
     *
     * @param pathNid the new path nid
     * @throws PropertyVetoException the property veto exception
     */
    void setPathNid(int pathNid) throws PropertyVetoException;
    
    /**
     * Sets the time.
     *
     * @param time the new time
     * @throws PropertyVetoException the property veto exception
     */
    void setTime(long time) throws PropertyVetoException;
    
    /**
     * Adds the long id.
     *
     * @param longId the long id
     * @param authorityNid the authority nid
     * @param statusNid the status nid
     * @param editCoordinate the edit coordinate
     * @param time the time
     * @return true, if successful
     */
    public boolean addLongId(Long longId, int authorityNid, int statusNid, EditCoordinate editCoordinate, long time);

}
