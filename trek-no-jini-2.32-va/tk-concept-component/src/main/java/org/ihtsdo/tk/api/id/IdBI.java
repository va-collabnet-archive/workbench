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
package org.ihtsdo.tk.api.id;

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;

import java.util.Set;
import org.ihtsdo.tk.api.VersionPointBI;

/**
 * The Interface IdBI contains methods for interacting with a generic type of
 * identifier.
 *
 */
public interface IdBI extends VersionPointBI {

    /**
     * Gets all the nids associated with this id.
     *
     * @return the nids associated with this id
     * @throws IOException signals that an I/O exception has occurred
     */
    Set<Integer> getAllNidsForId() throws IOException;

    /**
     * Gets the author nid associated with this id.
     *
     * @return the author nid
     */
    int getAuthorNid();

    /**
     * Gets the id authority nid associated with this id.
     *
     * @return the authority nid
     */
    int getAuthorityNid();

    /**
     * Gets the id denotation value associated with this id.
     *
     * @return the id denotation value
     */
    Object getDenotation();

    /**
     * Gets the stamp nid associated with this id.
     *
     * @return the stamp nid
     */
    int getStampNid();

    /**
     * Gets the status nid associated with this id.
     *
     * @return the status nid
     */
    int getStatusNid();

    /**
     * Gets the module nid associated with this id.
     *
     * @return the module nid
     */
    int getModuleNid();
}
