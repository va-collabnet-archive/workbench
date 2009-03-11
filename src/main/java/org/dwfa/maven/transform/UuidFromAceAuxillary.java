package org.dwfa.maven.transform;

import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.Transform;
import org.dwfa.maven.transform.AbstractTransform;


/**
 * Transforms a constant to a NID.
 *
 */
public class UuidFromAceAuxillary extends AbstractTransform {

    /**
     * The param which needs to be mapped to a UUID.
     */
    public String param;

    /**
     * Transforms a constant to a UUID.
     * @param input The input string (corresponds to one field in a
     * table/file).
     * @return String A string containing the result of the transformation.
     * @throws Exception Throws any exception caused by execution of the
     * transform.
     */
    public final String transform(final String input) throws Exception {
       ArchitectonicAuxiliary.Concept c = ArchitectonicAuxiliary.Concept.valueOf(param);
       UUID uid = c.getUids().iterator().next();
        if (getChainedTransform() != null) {
            return setLastTransform(getChainedTransform().transform(
                  uid.toString()));
        } else {
            return setLastTransform(uid.toString());
        }

    }

    /**
     * Sets up the transform.
     * @param transformer Reference to caller of this transform.
     */
    public final void setupImpl(final Transform transformer) {
    }
}


