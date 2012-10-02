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

/**
 * Interface for the
 * <code>STAMP</code> class. A stamp nid, is an identifier which represents the
 * status, time, author, module, and path for a particular component.
 */
public interface StampBI extends VersionPointBI {

    /**
     * Gets the status nid for this stamp.
     *
     * @return the status nid
     */
    int getStatusNid();

    /**
     * Gets the author nid for this stamp.
     *
     * @return author nid
     */
    int getAuthorNid();

    /**
     * Gets the module nid for this stamp.
     *
     * @return the module nid
     */
    int getModuleNid();

    /**
     * Returns the stamp nid with which the STAMP object was constructed.
     *
     * @return stamp nid
     */
    int getStampNid();
}
