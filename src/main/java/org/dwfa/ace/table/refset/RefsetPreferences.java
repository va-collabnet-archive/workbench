package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.dwfa.ace.api.I_HoldRefsetPreferences;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.I_RefsetDefaultsBoolean;
import org.dwfa.ace.refset.I_RefsetDefaultsConInt;
import org.dwfa.ace.refset.I_RefsetDefaultsConcept;
import org.dwfa.ace.refset.I_RefsetDefaultsInteger;
import org.dwfa.ace.refset.I_RefsetDefaultsLanguage;
import org.dwfa.ace.refset.I_RefsetDefaultsLanguageScoped;
import org.dwfa.ace.refset.I_RefsetDefaultsMeasurement;
import org.dwfa.ace.refset.I_RefsetDefaultsString;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;

public class RefsetPreferences implements I_HoldRefsetPreferences, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private I_RefsetDefaultsBoolean booleanPreferences = new RefsetDefaultsBoolean();

    private I_RefsetDefaultsConcept conceptPreferences = new RefsetDefaultsConcept();

    private I_RefsetDefaultsInteger integerPreferences = new RefsetDefaultsInteger();

    private I_RefsetDefaultsLanguage languagePreferences = new RefsetDefaultsLanguage();

    private I_RefsetDefaultsLanguageScoped languageScopedPreferences = new RefsetDefaultsLanguageScoped();

    private I_RefsetDefaultsMeasurement measurementPreferences = new RefsetDefaultsMeasurement();

    private I_RefsetDefaultsString stringPreferences = new RefsetDefaultsString();
    
    private I_RefsetDefaultsConInt conIntPreferences = new RefsetDefaultsConInt();


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(booleanPreferences);
        out.writeObject(conceptPreferences);
        out.writeObject(integerPreferences);
        out.writeObject(languagePreferences);
        out.writeObject(languageScopedPreferences);
        out.writeObject(measurementPreferences);
        out.writeObject(conIntPreferences);
    }

    @SuppressWarnings("unchecked")
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
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public RefsetPreferences() throws TerminologyException, IOException {
        super();
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getBooleanPreferences()
     */
    public I_RefsetDefaultsBoolean getBooleanPreferences() {
        return booleanPreferences;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getConceptPreferences()
     */
    public I_RefsetDefaultsConcept getConceptPreferences() {
        return conceptPreferences;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getIntegerPreferences()
     */
    public I_RefsetDefaultsInteger getIntegerPreferences() {
        return integerPreferences;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getLanguagePreferences()
     */
    public I_RefsetDefaultsLanguage getLanguagePreferences() {
        return languagePreferences;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getMeasurementPreferences()
     */
    public I_RefsetDefaultsMeasurement getMeasurementPreferences() {
        return measurementPreferences;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getLanguageScopedPreferences()
     */
    public I_RefsetDefaultsLanguageScoped getLanguageScopedPreferences() {
        return languageScopedPreferences;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getStringPreferences()
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

	/* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_DefineRefsetPreferences#getConIntPreferences()
     */
	public I_RefsetDefaultsConInt getConIntPreferences() {
		return conIntPreferences;
	}

}
