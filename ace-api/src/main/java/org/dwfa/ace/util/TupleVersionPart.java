package org.dwfa.ace.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_AmVersioned;

public class TupleVersionPart {

    /**
     * Get the latest version of each tuple
     *
     * @param <T> extends I_AmTuple
     * @param list List of I_AmTuple
     * @return List of <T> extends I_AmTuple
     */
    public static <T extends I_AmTuple> Collection<T> getLatestMatchingTuples(Collection<T> list) {
        Map<Integer, T> latestTuples = new HashMap<Integer, T>();

        for (T tuple : list) {
            T latest = latestTuples.get(tuple.getFixedPartId());
            if(latest == null || latest.getVersion() < tuple.getVersion()){
                latestTuples.put(tuple.getFixedPartId(), tuple);
            }
        }

        return latestTuples.values();
    }

    /**
     * Get the latest part
     *
     * @param <T> extends I_AmPart
     * @param list List of I_AmPart
     * @return <T> extends I_AmPart
     */
    public static <T extends I_AmPart> T getLatestPart(Collection<T> list) {
        T latestPart = null;

        for (T part : list) {
            if(latestPart == null || latestPart.getVersion() < part.getVersion()){
                latestPart = part;
            }
        }

        return latestPart;
    }

    /**
     * Check all the versioned parts for duplicates and remove, if no parts left in
     * versioned remove the versioned from the <code>versionedList</code> list
     *
     * NB relies on the I_AmPart's equals method.
     *
     * @param versionedList List<? extends I_AmVersioned>
     */
    public static <P extends I_AmPart> void removeDuplicateParts(List<? extends I_AmVersioned<P>> versionedList) {
        /** Version to iterate over */
        List<I_AmVersioned<P>> versions = new ArrayList<I_AmVersioned<P>>();
        /** Parts to iterate over */
        List<P> partList = new ArrayList<P>();
        /** Unique parts */
        Set<P> partSet = new HashSet<P>();

        versions.addAll((Collection<? extends I_AmVersioned<P>>) versionedList);
        for (I_AmVersioned<P> versioned : versions) {
            if(versioned != null){
                partList.addAll(versioned.getVersions());
                for (P part : partList) {
                    //If we have see this part before remove it from the versioned
                    if (!partSet.add(part)) {
                        versioned.getVersions().remove(part);
                    }
                }
                //If there are no parts in the versioned (all duplicates) remove versioned.
                if (versioned.getVersions().isEmpty()) {
                    versionedList.remove(versioned);
                }
            }
        }
    }
}
