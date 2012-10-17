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
 * The Interface VersionPointBI contains methods for interacting with a
 * particular version point. Used to compare positions.
 * 
 * @see RelativePositionComputerBI
 *
 */
public interface VersionPointBI {

    /**
     * Gets the time associated with the version.
     *
     * @return a <code>long</code> representation of the time
     */
    long getTime();

    /**
     * Gets the nid of the path associated with the version.
     *
     * @return the path nid
     */
    int getPathNid();
}
