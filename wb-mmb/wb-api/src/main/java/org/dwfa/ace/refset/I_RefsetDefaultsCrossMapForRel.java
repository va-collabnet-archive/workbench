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
package org.dwfa.ace.refset;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;

public interface I_RefsetDefaultsCrossMapForRel extends I_RefsetDefaults {

    public I_GetConceptData getRefineFlag();

    public void setRefineFlag(I_GetConceptData refineFlag);

    public I_IntList getRefineFlagPopupIds();

    public void setRefineFlagPopupIds(I_IntList refineFlagPopupIds);

    public I_GetConceptData getAdditionalCode();

    public void setAdditionalCode(I_GetConceptData additionalCode);

    public I_IntList getAdditionalCodePopupIds();

    public void setAdditionalCodePopupIds(I_IntList additionalCodePopupIds);

    public int getDefaultElementNo();

    public void setDefaultElementNo(int defaultElementNo);

    public Integer[] getElementNoPopupItems();

    public void setElementNoPopupItems(Integer[] elementNoPopupItems);

    public int getDefaultBlockNo();

    public void setDefaultBlockNo(int defaultBlockNo);

    public Integer[] getBlockNoPopupItems();

    public void setBlockNoPopupItems(Integer[] blockNoPopupItems);

}
