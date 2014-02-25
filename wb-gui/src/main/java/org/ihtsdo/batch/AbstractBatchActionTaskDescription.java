/*
 * Copyright 2014 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.batch;

import java.io.IOException;
import java.util.Collection;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 *
 * @author marc
 */
public abstract class AbstractBatchActionTaskDescription
        extends BatchActionTask {

    // DESCRIPTION CRITERIA
    int searchByTextConstraint; // NA | Contains | Begins With | Ends With
    String searchText;
    boolean isSearchCaseSensitive;
    int searchByType; // NA | FSN | Synomym | Description
    int searchByLanguage;

    // REFSET MEMBER
    TK_REFEX_TYPE refsetType;
    int collectionNid;
    // FILTER
    boolean useFilter;
    Object matchValue;

    public AbstractBatchActionTaskDescription() {
        this.searchByTextConstraint = 0; // Does Not Apply
        this.searchText = null;
        this.isSearchCaseSensitive = false;
        this.searchByType = 0; // ----- Does Not Apply
        this.searchByLanguage = 0; // -- Does Not Apply
        this.refsetType = TK_REFEX_TYPE.CID;
        this.collectionNid = Integer.MAX_VALUE;
        this.useFilter = false;
        this.matchValue = null;
    }
    
    

    public void setSearchByTextConstraint(int searchByTextConstraint) {
        this.searchByTextConstraint = searchByTextConstraint;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public void setIsSearchCaseSensitive(boolean isSearchCaseSensitive) {
        this.isSearchCaseSensitive = isSearchCaseSensitive;
    }

    public void setSearchByType(int searchByType) {
        this.searchByType = searchByType;
    }

    public void setSearchByLanguage(int searchByLanguage) {
        this.searchByLanguage = searchByLanguage;
    }

    public void setCollectionNid(int collectionNid) {
        this.collectionNid = collectionNid;
    }

    public void setMatchValue(Object matchValue) {
        this.matchValue = matchValue;
    }

    public void setRefsetType(TK_REFEX_TYPE refsetType) {
        this.refsetType = refsetType;
    }

    public void setUserFilter(boolean b) {
        this.useFilter = b;
    }

    boolean passCriteriaText(DescriptionVersionBI dvbi) {
        String text = dvbi.getText();
        switch (searchByTextConstraint) {
            case 0: // ------- Does Not Apply
                return true;
            case 1: // Contains
                if (isSearchCaseSensitive) {
                    return text.contains(searchText);
                } else {
                    return text.toUpperCase().contains(searchText.toUpperCase());
                }
            case 2: // Begins With
                if (isSearchCaseSensitive) {
                    return text.startsWith(searchText);
                } else {
                    return text.toUpperCase().startsWith(searchText.toUpperCase());
                }
            case 3: // Ends With
                if (isSearchCaseSensitive) {
                    return text.endsWith(searchText);
                } else {
                    return text.toUpperCase().endsWith(searchText.toUpperCase());
                }
        }
        return false;
    }

    boolean passCriteriaRefestMember(DescriptionVersionBI dvbi, ViewCoordinate vc)
            throws IOException {
        Collection<? extends RefexVersionBI<?>> members = dvbi.getRefexMembersActive(vc, collectionNid);
        if (members == null || members.isEmpty()) {
            return false;
        }

        boolean isMemberFound = false;
        for (RefexVersionBI<?> rvbi : members) {
            if (RefexNidVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                RefexNidVersionBI r = (RefexNidVersionBI) rvbi;
                int cNid1 = r.getNid1();
                if (useFilter && matchValue != null) {
                    if ((Integer) matchValue == cNid1) {
                        isMemberFound = true;
                    }
                } else {
                    isMemberFound = true;
                }
            }
        }
        return isMemberFound;
    }

    boolean passCriteriaType(DescriptionVersionBI dvbi) throws IOException {
        int typeNid = dvbi.getTypeNid();
        switch (searchByType) {
            case 0: // -- Does not apply
                return true;
            case 1: // FSN
                int fsnNid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
                return (typeNid == fsnNid);
            case 2: // Synonym
                int synonymNid = SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid();
                return (typeNid == synonymNid);
            case 3: // Definition
                int definitionNid = SnomedMetadataRf2.DEFINITION_RF2.getLenient().getNid();
                return (typeNid == definitionNid);
        }
        return false;
    }

    boolean passCriteriaLanguage(DescriptionVersionBI dvbi) {
        switch (searchByLanguage) {
            case 0: // -- Does not apply 
                return true;
            case 1: // EN
                return dvbi.getLang().toUpperCase().startsWith("EN");
            case 2: // DA
                return dvbi.getLang().toUpperCase().startsWith("DA");
            case 3: // ES
                return dvbi.getLang().toUpperCase().startsWith("ES");
            case 4: // FR
                return dvbi.getLang().toUpperCase().startsWith("FR");
            case 5: // LIT
                return dvbi.getLang().toUpperCase().startsWith("LT-LT");
            case 6: // LT
                return dvbi.getLang().toUpperCase().startsWith("LT");
            case 7: // NL
                return dvbi.getLang().toUpperCase().startsWith("NL");
            case 8: // PL
                return dvbi.getLang().toUpperCase().startsWith("PL");
            case 9: // SV
                return dvbi.getLang().toUpperCase().startsWith("SV");
            case 10: // ZH  
                return dvbi.getLang().toUpperCase().startsWith("ZH");
        }
        return false;
    }

    boolean testCriteria(DescriptionVersionBI dvbi, ViewCoordinate vc)
            throws IOException {
        if (searchByTextConstraint > 0
                && searchText != null
                && searchText.length() > 0) {
            if (!passCriteriaText(dvbi)) {
                return false;
            }
        }

        if (searchByLanguage > 0) {
            if (!passCriteriaLanguage(dvbi)) {
                return false;
            }
        }

        if (searchByType > 0) {
            if (!passCriteriaType(dvbi)) {
                return false;
            }
        }

        if (collectionNid < Integer.MAX_VALUE) {
            // Integer.MAX_VALUE is "-----"
            if (!passCriteriaRefestMember(dvbi, vc)) {
                return false;
            }
        }
        return true;
    }

}
