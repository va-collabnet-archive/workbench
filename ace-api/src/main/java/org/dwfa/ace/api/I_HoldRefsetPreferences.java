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
package org.dwfa.ace.api;

import org.dwfa.ace.refset.I_RefsetDefaultsBoolean;
import org.dwfa.ace.refset.I_RefsetDefaultsConInt;
import org.dwfa.ace.refset.I_RefsetDefaultsConcept;
import org.dwfa.ace.refset.I_RefsetDefaultsConceptConceptString;
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

    public I_RefsetDefaultsConceptConceptString getConceptConceptStringPreferences();

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
