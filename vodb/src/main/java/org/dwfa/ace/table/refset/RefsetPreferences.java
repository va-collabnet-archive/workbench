/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.dwfa.ace.api.I_HoldRefsetPreferences;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.I_RefsetDefaultsBoolean;
import org.dwfa.ace.refset.I_RefsetDefaultsConInt;
import org.dwfa.ace.refset.I_RefsetDefaultsConceptConceptString;
import org.dwfa.ace.refset.I_RefsetDefaultsConcept;
import org.dwfa.ace.refset.I_RefsetDefaultsCrossMap;
import org.dwfa.ace.refset.I_RefsetDefaultsCrossMapForRel;
import org.dwfa.ace.refset.I_RefsetDefaultsInteger;
import org.dwfa.ace.refset.I_RefsetDefaultsLanguage;
import org.dwfa.ace.refset.I_RefsetDefaultsLanguageScoped;
import org.dwfa.ace.refset.I_RefsetDefaultsMeasurement;
import org.dwfa.ace.refset.I_RefsetDefaultsString;
import org.dwfa.ace.refset.I_RefsetDefaultsTemplate;
import org.dwfa.ace.refset.I_RefsetDefaultsTemplateForRel;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;

public class RefsetPreferences implements I_HoldRefsetPreferences, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 5;

    private I_RefsetDefaultsBoolean booleanPreferences = new RefsetDefaultsBoolean();

    private I_RefsetDefaultsConcept conceptPreferences = new RefsetDefaultsConcept();

    private I_RefsetDefaultsInteger integerPreferences = new RefsetDefaultsInteger();

    private I_RefsetDefaultsLanguage languagePreferences = new RefsetDefaultsLanguage();

    private I_RefsetDefaultsLanguageScoped languageScopedPreferences = new RefsetDefaultsLanguageScoped();

    private I_RefsetDefaultsMeasurement measurementPreferences = new RefsetDefaultsMeasurement();

    private I_RefsetDefaultsString stringPreferences = new RefsetDefaultsString();

    private I_RefsetDefaultsConInt conIntPreferences = new RefsetDefaultsConInt();

    private I_RefsetDefaultsConceptConceptString conceptConceptStringPreferences = new RefsetDefaultsConceptConceptString();

    private I_RefsetDefaultsCrossMap crossMapPreferences = new RefsetDefaultsCrossMap();

    private I_RefsetDefaultsCrossMapForRel crossMapForRelPreferences = new RefsetDefaultsCrossMapForRel();

    private I_RefsetDefaultsTemplate templatePreferences = new RefsetDefaultsTemplate();

    private I_RefsetDefaultsTemplateForRel templateForRelPreferences = new RefsetDefaultsTemplateForRel();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(booleanPreferences);
        out.writeObject(conceptPreferences);
        out.writeObject(integerPreferences);
        out.writeObject(languagePreferences);
        out.writeObject(languageScopedPreferences);
        out.writeObject(measurementPreferences);
        out.writeObject(conIntPreferences);
        out.writeObject(crossMapPreferences);
        out.writeObject(crossMapForRelPreferences);
        out.writeObject(templatePreferences);
        out.writeObject(templateForRelPreferences);
        out.writeObject(stringPreferences);
        out.writeObject(conceptConceptStringPreferences);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            booleanPreferences = (I_RefsetDefaultsBoolean) in.readObject();
            conceptPreferences = (I_RefsetDefaultsConcept) in.readObject();
            integerPreferences = (I_RefsetDefaultsInteger) in.readObject();
            languagePreferences = (I_RefsetDefaultsLanguage) in.readObject();
            languageScopedPreferences = (I_RefsetDefaultsLanguageScoped) in.readObject();
            measurementPreferences = (I_RefsetDefaultsMeasurement) in.readObject();
            if (objDataVersion > 1) {
                conIntPreferences = (I_RefsetDefaultsConInt) in.readObject();
            } else {
                try {
                    conIntPreferences = new RefsetDefaultsConInt();
                } catch (TerminologyException e) {
                    throw new ToIoException(e);
                }
            }
            if (objDataVersion > 2) {
                crossMapPreferences = (I_RefsetDefaultsCrossMap) in.readObject();
                crossMapForRelPreferences = (I_RefsetDefaultsCrossMapForRel) in.readObject();
                templatePreferences = (I_RefsetDefaultsTemplate) in.readObject();
                templateForRelPreferences = (I_RefsetDefaultsTemplateForRel) in.readObject();
            } else {
                try {
                    crossMapPreferences = new RefsetDefaultsCrossMap();
                    crossMapForRelPreferences = new RefsetDefaultsCrossMapForRel();
                    templatePreferences = new RefsetDefaultsTemplate();
                    templateForRelPreferences = new RefsetDefaultsTemplateForRel();
                } catch (TerminologyException e) {
                    throw new ToIoException(e);
                }
            }
            if (objDataVersion > 3) {
                stringPreferences = (I_RefsetDefaultsString) in.readObject();
            } else {
                try {
                    stringPreferences = new RefsetDefaultsString();
                } catch (TerminologyException e) {
                    throw new ToIoException(e);
                }
            }
            if (objDataVersion > 4) {
                conceptConceptStringPreferences = (I_RefsetDefaultsConceptConceptString) in.readObject();
            } else {
                try {
                    conceptConceptStringPreferences = new RefsetDefaultsConceptConceptString();
                } catch (TerminologyException e) {
                    throw new ToIoException(e);
                }
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public RefsetPreferences() throws TerminologyException, IOException {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getBooleanPreferences
     * ()
     */
    public I_RefsetDefaultsBoolean getBooleanPreferences() {
        return booleanPreferences;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getConceptPreferences
     * ()
     */
    public I_RefsetDefaultsConcept getConceptPreferences() {
        return conceptPreferences;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getIntegerPreferences
     * ()
     */
    public I_RefsetDefaultsInteger getIntegerPreferences() {
        return integerPreferences;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getLanguagePreferences
     * ()
     */
    public I_RefsetDefaultsLanguage getLanguagePreferences() {
        return languagePreferences;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getMeasurementPreferences
     * ()
     */
    public I_RefsetDefaultsMeasurement getMeasurementPreferences() {
        return measurementPreferences;
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.dwfa.ace.table.refset.I_DefineRefsetPreferences#
     * getLanguageScopedPreferences()
     */
    public I_RefsetDefaultsLanguageScoped getLanguageScopedPreferences() {
        return languageScopedPreferences;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getStringPreferences
     * ()
     */
    public I_RefsetDefaultsString getStringPreferences() {
        if (stringPreferences == null) {
            try {
                stringPreferences = new RefsetDefaultsString();
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
        return stringPreferences;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getConIntPreferences
     * ()
     */
    public I_RefsetDefaultsConInt getConIntPreferences() {
        return conIntPreferences;
    }

    public I_RefsetDefaultsConceptConceptString getConceptConceptStringPreferences() {
        return conceptConceptStringPreferences;
    }

    public I_RefsetDefaultsCrossMap getCrossMapPreferences() {
        return crossMapPreferences;
    }

    public I_RefsetDefaultsCrossMapForRel getCrossMapForRelPreferences() {
        return crossMapForRelPreferences;
    }

    public I_RefsetDefaultsTemplate getTemplatePreferences() {
        return templatePreferences;
    }

    public I_RefsetDefaultsTemplateForRel getTemplateForRelPreferences() {
        return templateForRelPreferences;
    }

}
