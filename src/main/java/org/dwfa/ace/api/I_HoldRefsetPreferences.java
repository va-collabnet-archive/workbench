package org.dwfa.ace.api;

import org.dwfa.ace.refset.I_RefsetDefaultsBoolean;
import org.dwfa.ace.refset.I_RefsetDefaultsConcept;
import org.dwfa.ace.refset.I_RefsetDefaultsInteger;
import org.dwfa.ace.refset.I_RefsetDefaultsLanguage;
import org.dwfa.ace.refset.I_RefsetDefaultsLanguageScoped;
import org.dwfa.ace.refset.I_RefsetDefaultsMeasurement;

public interface I_HoldRefsetPreferences {
   public I_RefsetDefaultsBoolean getBooleanPreferences();
   public I_RefsetDefaultsConcept getConceptPreferences();
   public I_RefsetDefaultsInteger getIntegerPreferences();
   public I_RefsetDefaultsMeasurement getMeasurementPreferences();
   public I_RefsetDefaultsLanguage getLanguagePreferences();
   public I_RefsetDefaultsLanguageScoped getLanguageScopedPreferences();
}
