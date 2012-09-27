package org.ihtsdo.db.bdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.commit.I_TestDataConstraints;

public class BdbDataCheckManager {

    private static List<I_TestDataConstraints> commitTests = new ArrayList<I_TestDataConstraints>();

    private static List<I_TestDataConstraints> creationTests = new ArrayList<I_TestDataConstraints>();

    private static Map<I_GetConceptData, Collection<AlertToDataConstraintFailure>> dataCheckMap =
            new HashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>>();


}
