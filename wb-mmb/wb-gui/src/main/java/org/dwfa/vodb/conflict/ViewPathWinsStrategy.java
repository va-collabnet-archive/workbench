package org.dwfa.vodb.conflict;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTuple;

public class ViewPathWinsStrategy extends ContradictionManagementStrategy {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected <T extends I_AmPart> boolean doesConflictExist(List<T> versions) {
        return false;
    }

    @Override
    public String getDescription() {
        return "<html>This resolution strategy implements resolution that"
        + "<ul><li>checks if conflicting members are present on the users view path(s),</li>"
        + "<li>and if so, suppresses the members that are NOT on the view path(s) from </li>"
        + "<li>participating in the potential contradiction.</ul>"
        + "</html>";
    }

    @Override
    public String getDisplayName() {
        return "Suppress versions NOT on a view path from contradictions";
    }
    @Override
    public <T extends I_AmPart> List<T> resolveParts(List<T> parts) {
        List<T> returnValues = new ArrayList<T>(2);
        for (T v: parts) {
            if (config.getViewPositionSetReadOnly().getViewPathNidSet().contains(v.getPathId())) {
                returnValues.add(v);
            }
        }
        if (returnValues.size() == 0) {
            returnValues.addAll(parts);
        }
        return returnValues;
    }

    @Override
    public <T extends I_AmPart> List<T> resolveParts(T part1, T part2) {
        List<T> returnValues = new ArrayList<T>(2);
        if (config.getViewPositionSetReadOnly().getViewPathNidSet().contains(part1.getPathId())) {
            returnValues.add(part1);
        }
        if (config.getViewPositionSetReadOnly().getViewPathNidSet().contains(part2.getPathId())) {
            returnValues.add(part2);
        }
        if (returnValues.size() == 0) {
            returnValues.add(part1);
            returnValues.add(part2);
        }
        return returnValues;
    }

    @Override
    public <T extends I_AmTuple> List<T> resolveTuples(List<T> tuples) {
        List<T> returnValues = new ArrayList<T>(2);
        for (T v: tuples) {
            if (config.getViewPositionSetReadOnly().getViewPathNidSet().contains(v.getPathId())) {
                returnValues.add(v);
            }
        }
        if (returnValues.size() == 0) {
            returnValues.addAll(tuples);
        }
        return returnValues;
    }

}
