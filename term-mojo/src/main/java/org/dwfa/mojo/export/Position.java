package org.dwfa.mojo.export;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.util.AceDateFormat;

public class Position {

    private Date timePoint;
    private I_GetConceptData path;

    public Position(PositionDescriptor positionDescriptor) throws Exception {
        timePoint = AceDateFormat.getRf2DateFormat().parse(positionDescriptor.getTimeString());
        path = positionDescriptor.getPath().getVerifiedConcept();
    }

    public <T extends I_AmTuple> List<T> getMatchingTuples(List<T> list) {
        List<T> matchingTuples = new ArrayList<T>();

        for (T tuple : list) {
            if(tuple.getPathId() == path.getConceptId()
                    && timePoint.getTime() <= tuple.getTime()){
                matchingTuples.add(tuple);
            }
        }
        return matchingTuples;
    }
}
