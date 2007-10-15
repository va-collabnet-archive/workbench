package org.dwfa.vodb.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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
public class ThinExtByRefVersioned {

    private int refsetId;

    private int memberId;

    private int componentId;

    private int typeId; // Use an enumeration when reading/writing, and convert
                        // it to the corresponding concept nid...

    private List<ThinExtByRefPart> versions;

    public ThinExtByRefVersioned(int refsetId, int memberId, int componentId, int typeId) {
        this(refsetId, memberId, componentId, typeId, 1);
    }

    public ThinExtByRefVersioned(int refsetId, int memberId, int componentId, int typeId, int partCount) {
        super();
        this.refsetId = refsetId;
        this.memberId = memberId;
        this.componentId = componentId;
        this.typeId = typeId;
        this.versions = new ArrayList<ThinExtByRefPart>(partCount);
    }

    public int getMemberId() {
        return memberId;
    }

    public int getComponentId() {
        return componentId;
    }

    public int getTypeId() {
        return typeId;
    }

    public List<? extends ThinExtByRefPart> getVersions() {
        return versions;
    }

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

    public void addVersion(ThinExtByRefPart part) {
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine("Adding part: " + part + " to member: " + memberId);
        }
        versions.add(part);
    }

    public void setRefsetId(int refsetId) {
        this.refsetId = refsetId;
    }

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

    private static void setStandardFields(UniversalAceExtByRefPart part, VodbEnv vodb, ThinExtByRefPart thinPart) throws TerminologyException, IOException {
        thinPart.setPathId(vodb.uuidToNative(part.getPathUid()));
        thinPart.setStatus(vodb.uuidToNative(part.getStatusUid()));
        thinPart.setVersion(ThinVersionHelper.convert(part.getTime()));
    }
}
