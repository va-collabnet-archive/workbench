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
package org.ihtsdo.helper.version;

import org.ihtsdo.helper.version.RelativePositionComputerBI;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.VersionPointBI;

/**
 * The Class RelativePositionComputer contains methods for computing the
 * relative position of a
 * <code>PositionBI</code> or a
 * <code>VersionPointBI</code>.
 *
 */
public class RelativePositionComputer implements RelativePositionComputerBI {

    private static ConcurrentHashMap<PositionBI, RelativePositionComputerBI> mapperCache =
            new ConcurrentHashMap<>();

    /**
     * Gets a relative position computer based on the given
     * <code>position</code>.
     *
     * @param position the position
     * @return the relative position computer
     */
    public static RelativePositionComputerBI getComputer(PositionBI position) {
        RelativePositionComputerBI pm = mapperCache.get(position);

        if (pm != null) {
            return pm;
        }

        pm = new RelativePositionComputer(position);

        RelativePositionComputerBI existing = mapperCache.putIfAbsent(position, pm);

        if (existing != null) {
            pm = existing;
        }

        return pm;
    }
    PositionBI destination;
    HashMap<Integer, Segment> pathNidSegmentMap;

    /**
     * Instantiates a new relative position computer based on the given
     * <code>destination</code>.
     *
     * @param destination the destination associated with this relative position
     * computer
     */
    public RelativePositionComputer(PositionBI destination) {
        this.destination = destination;
        pathNidSegmentMap = setupPathNidSegmentMap(destination);
    }

    /**
     * The Class Segment represents a segment of a path.
     */
    private static class Segment {

        int segmentNid;
        int pathNid;
        long endTime;
        BitSet precedingSegments;

        /**
         * Instantiates a new segment of a path.
         *
         * @param segmentNid the nid associated with this segment
         * @param pathNid the nid associated with path that contains this
         * segment
         * @param endTime the time indicating the end of the segment
         * @param precedingSegments the preceding segments in the path
         */
        public Segment(int segmentNid, int pathNid, long endTime, BitSet precedingSegments) {
            this.segmentNid = segmentNid;
            this.pathNid = pathNid;
            this.endTime = endTime;
            this.precedingSegments = new BitSet(precedingSegments.size());
            this.precedingSegments.or(precedingSegments);
        }

        /**
         * Tests if this segment contains the position specified by the given
         * <code>path</code> and
         * <code>time</code>.
         *
         * @param pathNid the nid representing the path of the position
         * @param time the time of the position
         * @return <code>true</code>, if the path nid matches this segment's
         * path nid and the time is less than or equal to this segment's end
         * time
         */
        public boolean containsPosition(int pathNid, long time) {
            if (this.pathNid == pathNid && time != Long.MIN_VALUE) {
                return time <= endTime;
            }
            return false;
        }
    }

    /**
     * Sets up the path nid segment map for the given
     * <code>destination</code>.
     *
     * @param destination the destination position containing the path to map
     * @return the a map of path nids to path segment
     */
    private static HashMap<Integer, Segment> setupPathNidSegmentMap(PositionBI destination) {
        HashMap<Integer, Segment> pathNidSegmentMap = new HashMap<>();
        AtomicInteger segmentNidSequence = new AtomicInteger(0);
        BitSet precedingSegments = new BitSet();
        addOriginsToPathNidSegmentMap(destination, pathNidSegmentMap, segmentNidSequence, precedingSegments);

        return pathNidSegmentMap;

    }

    /**
     * Adds the origins to path nid segment map.
     *
     * @param destination the destination containing the path to add
     * @param pathNidRpcNidMap the map of segments to path nids
     * @param segmentNidSequence the segment nid sequence representing the max
     * assigned segment nid
     * @param precedingSegments the preceding segments
     */
    private static void addOriginsToPathNidSegmentMap(PositionBI destination,
            HashMap<Integer, Segment> pathNidRpcNidMap, AtomicInteger segmentNidSequence, BitSet precedingSegments) {
        Segment segment = new Segment(segmentNidSequence.getAndIncrement(), destination.getPath().getConceptNid(),
                destination.getTime(), precedingSegments);
        precedingSegments.set(segment.segmentNid);
        pathNidRpcNidMap.put(destination.getPath().getConceptNid(), segment);
        for (PositionBI origin : destination.getAllOrigins()) {
            addOriginsToPathNidSegmentMap(origin, pathNidRpcNidMap, segmentNidSequence, precedingSegments);
        }
    }

    /**
     *
     * @param v1 the first part of the comparison.
     * @param v2 the second part of the comparison.
     * @param precedencePolicy the precedence policy
     * @return the <code>RelativePosition</code> of part1 compared to part2 with
     * respect to the destination position of the class's instance.
     */
    @Override
    public RelativePosition fastRelativePosition(VersionPointBI v1, VersionPointBI v2, Precedence precedencePolicy) {
        if (v1.getPathNid() == v2.getPathNid()) {
            Segment seg = (Segment) pathNidSegmentMap.get(v1.getPathNid());
            if (seg.containsPosition(v1.getPathNid(), v1.getTime())
                    && seg.containsPosition(v2.getPathNid(), v2.getTime())) {
                if (v1.getTime() < v2.getTime()) {
                    return RelativePosition.BEFORE;
                }
                if (v1.getTime() > v2.getTime()) {
                    return RelativePosition.AFTER;
                }
                if (v1.getTime() == v2.getTime()) {
                    return RelativePosition.EQUAL;
                }
            }
            return RelativePosition.UNREACHABLE;
        }

        Segment seg1 = (Segment) pathNidSegmentMap.get(v1.getPathNid());
        Segment seg2 = (Segment) pathNidSegmentMap.get(v2.getPathNid());
        if (seg1 == null || seg2 == null) {
            return RelativePosition.UNREACHABLE;
        }
        if (!(seg1.containsPosition(v1.getPathNid(), v1.getTime())
                && seg2.containsPosition(v2.getPathNid(), v2.getTime()))) {
            return RelativePosition.UNREACHABLE;
        }
        if (precedencePolicy == Precedence.TIME) {
            if (v1.getTime() < v2.getTime()) {
                return RelativePosition.BEFORE;
            }
            if (v1.getTime() > v2.getTime()) {
                return RelativePosition.AFTER;
            }
            if (v1.getTime() == v2.getTime()) {
                return RelativePosition.EQUAL;
            }
        }
        if (seg1.precedingSegments.get(seg2.segmentNid) == true) {
            return RelativePosition.BEFORE;
        }
        if (seg2.precedingSegments.get(seg1.segmentNid) == true) {
            return RelativePosition.AFTER;
        }
        return RelativePosition.CONTRADICTION;
    }

    /**
     *
     * @return the position representing the destination
     */
    @Override
    public PositionBI getDestination() {
        return destination;
    }

    /**
     *
     * @param version the part to be tested to determine if it is on route to
     * the destination.
     * @return <code>true</code> if the part's position is on the route to the
     * destination of the class's instance.
     */
    @Override
    public boolean onRoute(VersionPointBI v) {
        Segment seg = (Segment) pathNidSegmentMap.get(v.getPathNid());
        if (seg != null) {
            return seg.containsPosition(v.getPathNid(), v.getTime());
        }
        return false;
    }

    /**
     *
     * @param v1 the first part of the comparison.
     * @param v2 the second part of the comparison.
     * @return the <code>RelativePosition</code> of v1 compared to v2 with
     * respect to the destination position of the class's instance.
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public RelativePosition relativePosition(VersionPointBI v1, VersionPointBI v2) throws IOException {
        if (!(onRoute(v1) && onRoute(v2))) {
            return RelativePosition.UNREACHABLE;
        }
        return fastRelativePosition(v1, v2, Precedence.PATH);
    }
}
