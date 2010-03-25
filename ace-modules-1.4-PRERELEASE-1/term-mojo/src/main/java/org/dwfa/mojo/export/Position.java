package org.dwfa.mojo.export;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.util.AceDateFormat;

public class Position {
    private static final String LATEST_VERSION_STR = "latest";
    private Date timePoint;
    private List<Integer> statusNids = new ArrayList<Integer>();
    private I_GetConceptData path;

    public Position(PositionDescriptor positionDescriptor) throws Exception {

        if(positionDescriptor.getTimeString().equals(LATEST_VERSION_STR)) {
            timePoint = new Date();
            timePoint.setTime(Long.MAX_VALUE);
        } else {
            timePoint = AceDateFormat.getRf2DateFormat().parse(positionDescriptor.getTimeString());
        }

        path = positionDescriptor.getPath().getVerifiedConcept();
    }

    public <T extends I_AmTuple> List<T> getMatchingTuples(List<T> list) {
        List<T> matchingTuples = new ArrayList<T>();

        for (T tuple : list) {
            if (tuple.getPathId() == path.getConceptId() && tuple.getTime() <= timePoint.getTime()) {
                if (!statusNids.isEmpty()) {
                    for (Integer niInteger : statusNids) {
                        if (niInteger.intValue() == tuple.getStatusId()) {
                            matchingTuples.add(tuple);
                            break;
                        }
                    }
                } else {
                    matchingTuples.add(tuple);
                }
            }
        }
        return matchingTuples;
    }
}
