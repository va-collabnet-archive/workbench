package org.dwfa.vodb.conflict;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTuple;
import org.ihtsdo.tk.api.ComponentVersionBI;

public class EditPathLosesStrategy extends ContradictionManagementStrategy {

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
        + "<ul><li>checks if conflicting members are present on the users edit path(s),</li>"
        + "<li>and if so, suppresses members on the edit path(s) from participating in the</li>"
        + "<li>potential contradiction.</ul>"
        + "</html>";
    }

    @Override
    public String getDisplayName() {
        return "Suppress edit path versions from contradictions";
    }
    @Override
    public <T extends I_AmPart> List<T> resolveParts(List<T> parts) {
        List<T> returnValues = new ArrayList<T>(2);
        for (T v: parts) {
            if (!config.getEditingPathSetReadOnly().getPathNidSet().contains(v.getPathId())) {
                returnValues.add(v);
            }
        }
        if (returnValues.size() == 0) {
            returnValues.addAll(parts);
        }
        return returnValues;
    }

    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
        List<T> returnValues = new ArrayList<T>(2);
        if (!config.getEditingPathSetReadOnly().getPathNidSet().contains(part1.getPathNid())) {
            returnValues.add(part1);
        }
        if (!config.getEditingPathSetReadOnly().getPathNidSet().contains(part2.getPathNid())) {
            returnValues.add(part2);
        }
        if (returnValues.size() == 0) {
            returnValues.add(part1);
            returnValues.add(part2);
        }
        return returnValues;
    }

    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions) {
        List<T> returnValues = new ArrayList<T>(2);
        for (T v: versions) {
            if (!config.getEditingPathSetReadOnly().getPathNidSet().contains(v.getPathNid())) {
                returnValues.add(v);
            }
        }
        if (returnValues.size() == 0) {
            returnValues.addAll(versions);
        }
        return returnValues;
    }
    @Override
    public <T extends I_AmTuple> List<T> resolveTuples(List<T> tuples) {
        List<T> returnValues = new ArrayList<T>(2);
        for (T v: tuples) {
            if (!config.getEditingPathSetReadOnly().getPathNidSet().contains(v.getPathNid())) {
                returnValues.add(v);
            }
        }
        if (returnValues.size() == 0) {
            returnValues.addAll(tuples);
        }
        return returnValues;
    }

}
