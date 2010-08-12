/*
 *  Copyright 2010 matt.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.dwfa.mojo.export;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.dwfa.mojo.PositionDescriptor;

/**
 *
 * @author matt
 */
public interface OriginProcessor {

    /**
     * Adds to the <code>positions</code> List all the paths that have been
     * promoted to test.
     *
     * Adds release position Map to the <code>releasePathDateMap</code> Map for
     * each test path so the correct module id and time stamp are export for the
     * test paths.
     *
     * @param releasePathDateMap Map of UUID Date maps.
     * @param positions list of export Positions
     * @param excludedPositions list of export Positions to exclude.
     * @return releasePathDateMap populated with the origin positions.
     * @throws Exception if any error accessing the terminology is thrown.
     */
    Map<UUID, Map<UUID, Date>> addOriginPositions(final Map<UUID, Map<UUID, Date>> releasePathDateMap,
            final List<Position> positions, final PositionDescriptor[] excludedPositions) throws Exception;
}
