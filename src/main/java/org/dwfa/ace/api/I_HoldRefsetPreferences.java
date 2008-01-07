package org.dwfa.ace.api;

import org.dwfa.ace.refset.I_RefsetDefaultsBoolean;
import org.dwfa.ace.refset.I_RefsetDefaultsConInt;
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

public interface I_HoldRefsetPreferences {
   public I_RefsetDefaultsBoolean getBooleanPreferences();
   public I_RefsetDefaultsConcept getConceptPreferences();
   public I_RefsetDefaultsConInt getConIntPreferences();
   public I_RefsetDefaultsInteger getIntegerPreferences();
   public I_RefsetDefaultsMeasurement getMeasurementPreferences();
   public I_RefsetDefaultsLanguage getLanguagePreferences();
   public I_RefsetDefaultsLanguageScoped getLanguageScopedPreferences();
   public I_RefsetDefaultsString getStringPreferences();
   public I_RefsetDefaultsCrossMap getCrossMapPreferences();
   public I_RefsetDefaultsCrossMapForRel getCrossMapForRelPreferences();
   public I_RefsetDefaultsTemplate getTemplatePreferences();
   public I_RefsetDefaultsTemplateForRel getTemplateForRelPreferences();

}
