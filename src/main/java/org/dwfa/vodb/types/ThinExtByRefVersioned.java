package org.dwfa.vodb.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartBoolean;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartInteger;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartLanguage;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartMeasurement;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartScopedLanguage;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartString;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinVersionHelper;

/**
 * @todo add version to vodb -> added as getProperty...
 * @todo add imported change set info to vodb, need to set theProperty...
 * 
 * @todo have change sets automatically increment as size increases over a
 *       certain size. Added increment to change set file name format.
 * @todo add extension ability
 * 
 * @author kec
 * 
 */
public class ThinExtByRefVersioned implements I_ThinExtByRefVersioned {

    private int refsetId;

    private int memberId;

    private int componentId;

    private int typeId; // Use an enumeration when reading/writing, and convert
                        // it to the corresponding concept nid...

    private List<I_ThinExtByRefPart> versions;

    public ThinExtByRefVersioned(int refsetId, int memberId, int componentId, int typeId) {
        this(refsetId, memberId, componentId, typeId, 1);
    }

    public ThinExtByRefVersioned(int refsetId, int memberId, int componentId, int typeId, int partCount) {
        super();
        this.refsetId = refsetId;
        this.memberId = memberId;
        this.componentId = componentId;
        this.typeId = typeId;
        this.versions = new ArrayList<I_ThinExtByRefPart>(partCount);
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getMemberId()
    */
   public int getMemberId() {
        return memberId;
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getComponentId()
    */
   public int getComponentId() {
        return componentId;
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getTypeId()
    */
   public int getTypeId() {
        return typeId;
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getVersions()
    */
   public List<? extends I_ThinExtByRefPart> getVersions() {
        return versions;
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getRefsetId()
    */
   public int getRefsetId() {
        return refsetId;
    }

    @Override
    public boolean equals(Object obj) {
        ThinExtByRefVersioned another = (ThinExtByRefVersioned) obj;
        return ((refsetId == another.refsetId) && (memberId == another.memberId)
                && (componentId == another.componentId) && (typeId == another.typeId) && (versions
                .equals(another.versions)));
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { refsetId, memberId, componentId, typeId });
    }

    @Override
    public String toString() {
        return "ThinExtByRefVersioned refsetId: " + refsetId + " memberId: " + memberId + 
            " componentId: " + componentId + " typeId: " + typeId + " versions: " + versions;
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#addVersion(org.dwfa.vodb.types.ThinExtByRefPart)
    */
   public void addVersion(I_ThinExtByRefPart part) {
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine("Adding part: " + part + " to member: " + memberId + " for component: " + componentId);
        }
        versions.add(part);
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#setRefsetId(int)
    */
   public void setRefsetId(int refsetId) {
        this.refsetId = refsetId;
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#setTypeId(int)
    */
   public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public static ThinExtByRefPart makePart(UniversalAceExtByRefPart part) throws TerminologyException, IOException {
        VodbEnv vodb = AceConfig.getVodb();
        if (UniversalAceExtByRefPartBoolean.class.isAssignableFrom(part.getClass())) {
            ThinExtByRefPartBoolean thinPart = new ThinExtByRefPartBoolean();
            setStandardFields(part, vodb, thinPart);
            
            UniversalAceExtByRefPartBoolean booleanPart = (UniversalAceExtByRefPartBoolean) part;
            thinPart.setValue(booleanPart.getBooleanValue());
            return thinPart;
        } else if (UniversalAceExtByRefPartConcept.class.isAssignableFrom(part.getClass())) {
            ThinExtByRefPartConcept thinPart = new ThinExtByRefPartConcept();
            setStandardFields(part, vodb, thinPart);
            
            UniversalAceExtByRefPartConcept conceptPart = (UniversalAceExtByRefPartConcept) part;
            thinPart.setConceptId(vodb.uuidToNative(conceptPart.getConceptUid()));
            return thinPart;

        } else if (UniversalAceExtByRefPartInteger.class.isAssignableFrom(part.getClass())) {
            ThinExtByRefPartInteger thinPart = new ThinExtByRefPartInteger();
            setStandardFields(part, vodb, thinPart);
            
            UniversalAceExtByRefPartInteger integerPart = (UniversalAceExtByRefPartInteger) part;
            thinPart.setValue(integerPart.getIntValue());
            return thinPart;

        } else if (UniversalAceExtByRefPartLanguage.class.isAssignableFrom(part.getClass())) {
            ThinExtByRefPartLanguage thinPart = new ThinExtByRefPartLanguage();
            setStandardFields(part, vodb, thinPart);
            
            UniversalAceExtByRefPartLanguage languagePart = (UniversalAceExtByRefPartLanguage) part;
            thinPart.setAcceptabilityId(vodb.uuidToNative(languagePart.getAcceptabilityUids()));
            thinPart.setCorrectnessId(vodb.uuidToNative(languagePart.getCorrectnessUids()));
            thinPart.setDegreeOfSynonymyId(vodb.uuidToNative(languagePart.getDegreeOfSynonymyUids()));
            return thinPart;

        } else if (UniversalAceExtByRefPartMeasurement.class.isAssignableFrom(part.getClass())) {
            ThinExtByRefPartMeasurement thinPart = new ThinExtByRefPartMeasurement();
            setStandardFields(part, vodb, thinPart);
            
            UniversalAceExtByRefPartMeasurement measurementPart = (UniversalAceExtByRefPartMeasurement) part;
            thinPart.setMeasurementValue(measurementPart.getMeasurementValue());
            thinPart.setUnitsOfMeasureId(vodb.uuidToNative(measurementPart.getUnitsOfMeasureUids()));
            return thinPart;

        } else if (UniversalAceExtByRefPartScopedLanguage.class.isAssignableFrom(part.getClass())) {
            ThinExtByRefPartLanguageScoped thinPart = new ThinExtByRefPartLanguageScoped();
            setStandardFields(part, vodb, thinPart);
            
            UniversalAceExtByRefPartScopedLanguage scopedLanguagePart = (UniversalAceExtByRefPartScopedLanguage) part;
            thinPart.setAcceptabilityId(vodb.uuidToNative(scopedLanguagePart.getAcceptabilityUids()));
            thinPart.setCorrectnessId(vodb.uuidToNative(scopedLanguagePart.getCorrectnessUids()));
            thinPart.setDegreeOfSynonymyId(vodb.uuidToNative(scopedLanguagePart.getDegreeOfSynonymyUids()));

            thinPart.setPriority(scopedLanguagePart.getPriority());
            thinPart.setScopeId(vodb.uuidToNative(scopedLanguagePart.getScopeUids()));
            thinPart.setTagId(vodb.uuidToNative(scopedLanguagePart.getTagUids()));
            return thinPart;

        } else if (UniversalAceExtByRefPartString.class.isAssignableFrom(part.getClass())) {
            ThinExtByRefPartString thinPart = new ThinExtByRefPartString();
            setStandardFields(part, vodb, thinPart);
            
            UniversalAceExtByRefPartString stringPart = (UniversalAceExtByRefPartString) part;
            thinPart.setStringValue(stringPart.getStringValue());
            return thinPart;

        } else {
            throw new UnsupportedOperationException("Can't handle UniversalAceExtByRefPart of type: " + part.getClass());
        }
    }

    private static void setStandardFields(UniversalAceExtByRefPart part, VodbEnv vodb, I_ThinExtByRefPart thinPart) throws TerminologyException, IOException {
        thinPart.setPathId(vodb.uuidToNative(part.getPathUid()));
        thinPart.setStatus(vodb.uuidToNative(part.getStatusUid()));
        thinPart.setVersion(ThinVersionHelper.convert(part.getTime()));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelVersioned#addTuples(org.dwfa.ace.IntSet,
     *      org.dwfa.ace.IntSet, java.util.Set, java.util.List, boolean)
     */
    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#addTuples(org.dwfa.ace.api.I_IntSet, java.util.Set, java.util.List, boolean)
    */
   public void addTuples(I_IntSet allowedStatus,
            Set<I_Position> positions, List<I_ThinExtByRefTuple> returnTuples,
            boolean addUncommitted) {
        Set<I_ThinExtByRefPart> uncommittedParts = new HashSet<I_ThinExtByRefPart>();
        if (positions == null) {
            List<I_ThinExtByRefPart> addedParts = new ArrayList<I_ThinExtByRefPart>();
            Set<I_ThinExtByRefPart> rejectedParts = new HashSet<I_ThinExtByRefPart>();
            for (I_ThinExtByRefPart part : versions) {
                if (part.getVersion() == Integer.MAX_VALUE) {
                    uncommittedParts.add(part);
                } else {
                    if ((allowedStatus != null)
                            && (!allowedStatus.contains(part.getStatus()))) {
                        rejectedParts.add(part);
                        continue;
                    }
                    addedParts.add(part);
                }
            }
            for (I_ThinExtByRefPart part : addedParts) {
                boolean addPart = true;
                for (I_ThinExtByRefPart reject : rejectedParts) {
                    if ((part.getVersion() <= reject.getVersion())
                            && (part.getPathId() == reject.getPathId())) {
                        addPart = false;
                        continue;
                    }
                }
                if (addPart) {
                    returnTuples.add(new ThinExtByRefTuple(this, part));
                }
            }
        } else {

            Set<I_ThinExtByRefPart> addedParts = new HashSet<I_ThinExtByRefPart>();
            for (I_Position position : positions) {
                Set<I_ThinExtByRefPart> rejectedParts = new HashSet<I_ThinExtByRefPart>();
                ThinExtByRefTuple possible = null;
                for (I_ThinExtByRefPart part : versions) {
                    if (part.getVersion() == Integer.MAX_VALUE) {
                        uncommittedParts.add(part);
                        continue;
                    } else if ((allowedStatus != null)
                            && (!allowedStatus.contains(part.getStatus()))) {
                        if (possible != null) {
                            I_Position rejectedStatusPosition = new Position(
                                    part.getVersion(), position.getPath()
                                            .getMatchingPath(part.getPathId()));
                            I_Path possiblePath = position.getPath()
                                    .getMatchingPath(possible.getPathId());
                            I_Position possibleStatusPosition = new Position(
                                    possible.getVersion(), possiblePath);

                            if (rejectedStatusPosition.getPath() != null
                                    && rejectedStatusPosition
                                            .isSubsequentOrEqualTo(possibleStatusPosition)
                                    && position
                                            .isSubsequentOrEqualTo(rejectedStatusPosition)) {
                                possible = null;
                            }
                        }
                        rejectedParts.add(part);
                        continue;
                    }
                    if (position.isSubsequentOrEqualTo(part.getVersion(), part
                            .getPathId())) {
                        if (possible == null) {
                            if (!addedParts.contains(part)) {
                                possible = new ThinExtByRefTuple(this, part);
                                addedParts.add(part);
                            }
                        } else {
                            if (possible.getPathId() == part.getPathId()) {
                                if (part.getVersion() > possible.getVersion()) {
                                    if (!addedParts.contains(part)) {
                                        possible = new ThinExtByRefTuple(this, part);
                                        addedParts.add(part);
                                    }
                                }
                            } else {
                        int depth1 = position.getDepth(part.getPathId());
                        int depth2 = position.getDepth(possible.getPathId());
                                if (depth1 < depth2) {
                                    if (!addedParts.contains(part)) {
                                        possible = new ThinExtByRefTuple(this, part);
                                        addedParts.add(part);
                                    }
                                }
                            }
                        }
                    }

                }
                if (possible != null) {
                    I_Path possiblePath = position.getPath().getMatchingPath(
                            possible.getPathId());
                    I_Position possibleStatusPosition = new Position(possible
                            .getVersion(), possiblePath);
                    boolean addPart = true;
                    for (I_ThinExtByRefPart reject : rejectedParts) {
                  int version = reject.getVersion();
                  I_Path matchingPath = position.getPath()
                  .getMatchingPath(reject.getPathId());
                  if (matchingPath != null) {
                     I_Position rejectedStatusPosition = new Position(version, matchingPath);
                     if (rejectedStatusPosition.getPath() != null
                           && rejectedStatusPosition
                                 .isSubsequentOrEqualTo(possibleStatusPosition)
                           && position
                                 .isSubsequentOrEqualTo(rejectedStatusPosition)) {
                        addPart = false;
                        continue;
                     }
                  }
                    }
                    if (addPart) {
                        returnTuples.add(possible);
                    }
                }
            }
        }
        if (addUncommitted) {
            for (I_ThinExtByRefPart p : uncommittedParts) {
                returnTuples.add(new ThinExtByRefTuple(this, p));
            }
        }
    }

   public List<I_ThinExtByRefTuple> getTuples(I_IntSet allowedStatus, Set<I_Position> positions, boolean addUncommitted) {
        List<I_ThinExtByRefTuple> returnTuples = new ArrayList<I_ThinExtByRefTuple>();
        addTuples(allowedStatus, positions, returnTuples, addUncommitted);
        return returnTuples;
    }
}
